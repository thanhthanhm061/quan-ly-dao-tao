package com.qldt.service.impl;

import com.qldt.model.LopHocPhan;
import com.qldt.model.PhongHoc;
import com.qldt.model.ThoiKhoaBieu;
import com.qldt.repository.LopHocPhanRepository;
import com.qldt.repository.ThoiKhoaBieuRepository;
import com.qldt.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ThoiKhoaBieuServiceImpl implements ThoiKhoaBieuService {

    private final ThoiKhoaBieuRepository tkbRepo;
    private final LopHocPhanRepository lhpRepo;
    private final TKBNotificationService notificationService;

    // GiangVienRepository đã bị XÓA — dùng NhanVien qua LopHocPhan.getGiangVien()

    @Override
    public ThoiKhoaBieu save(ThoiKhoaBieu tkb) {

        if (tkb.getLopHocPhan() == null || tkb.getLopHocPhan().getId() == null) {
            throw new IllegalArgumentException("Chưa chọn lớp học phần");
        }

        LopHocPhan lhp = lhpRepo.findById(tkb.getLopHocPhan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));

        tkb.setLopHocPhan(lhp);

        if (lhp.getGiangVien() == null) {
            throw new IllegalArgumentException("Lớp học phần chưa có giảng viên");
        }

        // Quy đổi số tiết nếu chưa nhập
        if (tkb.getSoTiet() == 0) {
            if (lhp.getMonHoc() == null) {
                throw new IllegalArgumentException("Lớp học phần chưa có môn học");
            }
            int tinChi = lhp.getMonHoc().getSoTinChi();
            tkb.setSoTiet(tinChi > 0 ? Math.min(tinChi * 3, 5) : 3);
        }

        // Dùng NhanVien.id thay GiangVien.id
        Long nhanVienId = lhp.getGiangVien().getId();
        String hocKy   = lhp.getHocKy();

        // ── Kiểm tra trùng giảng viên ──────────────────────────────────
        List<ThoiKhoaBieu> lichGv = tkbRepo.findByGiangVienAndHocKy(nhanVienId, hocKy);

        for (ThoiKhoaBieu existing : lichGv) {
            if ((tkb.getId() == null || !existing.getId().equals(tkb.getId()))
                    && existing.trungLich(tkb)) {
                throw new IllegalStateException(
                        "Giảng viên bị trùng lịch! "
                                + existing.getTenThu()
                                + " tiết " + existing.getTietBatDau()
                                + "-" + (existing.getTietBatDau() + existing.getSoTiet() - 1)
                                + " tại phòng " + existing.getPhongHoc());
            }
        }

        // ── Kiểm tra trùng phòng ───────────────────────────────────────
        if (tkb.getPhongHoc() != null && !tkb.getPhongHoc().isBlank()) {
            List<ThoiKhoaBieu> lichPhong =
                    tkbRepo.findByPhongHocAndHocKy(tkb.getPhongHoc(), hocKy);

            for (ThoiKhoaBieu existing : lichPhong) {
                if ((tkb.getId() == null || !existing.getId().equals(tkb.getId()))
                        && existing.trungLich(tkb)) {
                    throw new IllegalStateException(
                            "Phòng " + tkb.getPhongHoc()
                                    + " đã có lịch vào " + existing.getTenThu()
                                    + " tiết " + existing.getTietBatDau()
                                    + "-" + (existing.getTietBatDau() + existing.getSoTiet() - 1)
                                    + " (" + existing.getLopHocPhan().getMonHoc().getTenMon() + ")");
                }
            }
        }

        ThoiKhoaBieu saved = tkbRepo.save(tkb);
        notificationService.guiThongBaoThemLich(saved);
        return saved;
    }

    @Override
    public void delete(Long id) {
        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch"));

        LopHocPhan lhp = lhpRepo.findById(tkb.getLopHocPhan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));

        lhp.getThoiKhoaBieus().removeIf(t -> t.getId().equals(id));
        lhpRepo.save(lhp);
    }
    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByLopHocPhanId(Long lhpId) {
        return tkbRepo.findByLopHocPhanId(lhpId);
    }
    public void capNhatNhanh(Long id, TkbQuickUpdateDTO dto) {
        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lịch ID=" + id));

        // Kiểm tra xung đột phòng
        if (dto.phongHoc() != null && !dto.phongHoc().isBlank()) {

            boolean trung = tkbRepo.existsConflict(
                    dto.thuTrongTuan(),
                    dto.tietBatDau(),
                    dto.tietBatDau() + dto.soTiet() - 1,
                    dto.phongHoc(),
                    id
            );

            if (trung) {
                throw new IllegalStateException(
                        "Phòng " + dto.phongHoc()
                                + " đã có lịch vào Thứ "
                                + dto.thuTrongTuan()
                                + " tiết "
                                + dto.tietBatDau()
                );
            }
        }

        if (dto.thuTrongTuan() != null)
            tkb.setThuTrongTuan(dto.thuTrongTuan());

        if (dto.tietBatDau() != null)
            tkb.setTietBatDau(dto.tietBatDau());

        if (dto.soTiet() != null)
            tkb.setSoTiet(dto.soTiet());

        if (dto.phongHoc() != null)
            tkb.setPhongHoc(dto.phongHoc());

        if (dto.tuanBatDau() != null)
            tkb.setTuanBatDau(dto.tuanBatDau());

        if (dto.tuanKetThuc() != null)
            tkb.setTuanKetThuc(dto.tuanKetThuc());

        tkbRepo.save(tkb);
    }
    @Override
    @Transactional
    public void overrideTuan(Long id, TkbWeekOverrideDTO dto, LocalDate ngayTrongTuan) {
        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch ID=" + id));

        LocalDate thu2Tuan = ngayTrongTuan.with(DayOfWeek.MONDAY);
        LocalDate thu7Tuan = thu2Tuan.plusDays(5);

        // Nếu đây là bản override → xóa override cũ, tìm về bản gốc
        if (tkb.isOverride()) {
            Long parentId = tkb.getOverrideParentId();
            tkbRepo.delete(tkb);
            tkbRepo.flush();
            if (parentId != null) {
                tkb = tkbRepo.findById(parentId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Không tìm thấy bản gốc ID=" + parentId));
            }
        }

        final ThoiKhoaBieu goc = tkb;
        final Long gocId = goc.getId();

        // Kiểm tra xung đột phòng
        if (dto.phongHoc() != null && !dto.phongHoc().isBlank()) {
            boolean trung = tkbRepo.existsConflictInRange(
                    dto.thuTrongTuan() != null ? dto.thuTrongTuan() : goc.getThuTrongTuan(),
                    dto.tietBatDau()   != null ? dto.tietBatDau()   : goc.getTietBatDau(),
                    dto.soTiet()       != null ? dto.soTiet()        : goc.getSoTiet(),
                    dto.phongHoc(), gocId, thu2Tuan, thu7Tuan);
            if (trung) throw new IllegalStateException(
                    "Phòng " + dto.phongHoc() + " đã có lịch trong tuần " +
                            thu2Tuan.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "!");
        }

        // Bản gốc vẫn giữ nguyên, query sẽ tự lọc
        ThoiKhoaBieu override = ThoiKhoaBieu.builder()
                .lopHocPhan(goc.getLopHocPhan())
                .thuTrongTuan(dto.thuTrongTuan() != null ? dto.thuTrongTuan() : goc.getThuTrongTuan())
                .tietBatDau(dto.tietBatDau()    != null ? dto.tietBatDau()   : goc.getTietBatDau())
                .soTiet(dto.soTiet()             != null ? dto.soTiet()        : goc.getSoTiet())
                .phongHoc(dto.phongHoc() != null && !dto.phongHoc().isBlank()
                        ? dto.phongHoc() : goc.getPhongHoc())
                .tuanBatDau(thu2Tuan)
                .tuanKetThuc(thu7Tuan)
                .isOverride(true)
                .overrideParentId(gocId)
                .build();
        tkbRepo.save(override);
    }


    public int demTietByLhp(Long lhpId) {
        return tkbRepo.findByLopHocPhanId(lhpId)
                .stream()
                .mapToInt(ThoiKhoaBieu::getSoTiet)
                .sum();
    }


    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVien(Long nhanVienId, String hocKy) {
        return tkbRepo.findByGiangVienAndHocKy(nhanVienId, hocKy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVienTuan(Long nhanVienId, String hocKy,
                                                  LocalDate ngayTrongTuan) {
        LocalDate thu2 = ngayTrongTuan.with(DayOfWeek.MONDAY);
        LocalDate thu7 = thu2.plusDays(5);
        return tkbRepo.findByGiangVienAndTuan(nhanVienId, hocKy, thu2, thu7);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVien(Long svId, String hocKy) {
        return tkbRepo.findBySinhVienAndHocKy(svId, hocKy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVienTuan(Long svId, String hocKy,
                                                 LocalDate ngayTrongTuan) {
        LocalDate thu2 = ngayTrongTuan.with(DayOfWeek.MONDAY);
        LocalDate thu7 = thu2.plusDays(5);
        return tkbRepo.findBySinhVienAndTuan(svId, hocKy, thu2, thu7);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByLHP(Long lhpId) {
        return tkbRepo.findByLopHocPhanId(lhpId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ThoiKhoaBieu> findById(Long id) {
        return tkbRepo.findById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TaiGiangDayDTO> thongKeTaiGiangDay(String hocKy) {
        return tkbRepo.thongKeTaiGiangDay(hocKy)
                .stream()
                .map(row -> {
                    long nvId    = (Long)   row[0];
                    String ten   = (String) row[1];
                    int tongTiet = ((Number) row[2]).intValue();
                    int soLop    = ((Number) row[3]).intValue();
                    int tinChi   = ((Number) row[4]).intValue();
                    double ty    = (double) tongTiet / TaiGiangDayDTO.TIET_TOI_DA_KY;
                    return new TaiGiangDayDTO(nvId, ten, tongTiet, soLop, tinChi, ty);
                })
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVienThang(Long nhanVienId, String hocKy, LocalDate ngayTrongThang) {
        LocalDate dauThang = ngayTrongThang.withDayOfMonth(1);
        LocalDate cuoiThang = ngayTrongThang.withDayOfMonth(ngayTrongThang.lengthOfMonth());
        return tkbRepo.findByGiangVienAndThang(nhanVienId, hocKy, dauThang, cuoiThang);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVienThang(Long svId, String hocKy, LocalDate ngayTrongThang) {
        LocalDate dauThang = ngayTrongThang.withDayOfMonth(1);
        LocalDate cuoiThang = ngayTrongThang.withDayOfMonth(ngayTrongThang.lengthOfMonth());
        return tkbRepo.findBySinhVienAndThang(svId, hocKy, dauThang, cuoiThang);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> thongKeTinChi(Long nhanVienId, String hocKy) {
        int phanCong = Optional.ofNullable(tkbRepo.sumTinChiPhanCong(nhanVienId, hocKy)).orElse(0);
        int daDay    = Optional.ofNullable(tkbRepo.sumTinChiDaDay(nhanVienId, hocKy)).orElse(0);
        // Tính số tiết đã dạy (tính từ TKB đã qua tuần hiện tại)
        return Map.of(
                "phanCong", phanCong,
                "daDay",    daDay,
                "conLai",   Math.max(0, phanCong - daDay)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhongHoc> timPhongTrong(String hocKy, int thu, int tietBd,
                                        int soTiet, int suc) {
        return tkbRepo.findPhongTrong(hocKy, thu, tietBd, tietBd + soTiet, suc);
    }

    @Override
    public List<PhongHoc> findAllPhong() {
        return List.of();
    }
    @Override
    @Transactional
    public void xoaTuan(Long id, LocalDate ngayTrongTuan) {
        LocalDate thu2 = ngayTrongTuan.with(DayOfWeek.MONDAY);
        LocalDate thu7 = thu2.plusDays(5);

        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch ID=" + id));

        if (tkb.isOverride()) {
            // Xóa override → bản gốc tự hiện lại
            tkbRepo.delete(tkb);
        } else {
            // Bản gốc → tạo 1 override đặc biệt đánh dấu "đã xóa tuần này"
            LocalDate tuanBdGoc = tkb.getTuanBatDau();
            LocalDate tuanKtGoc = tkb.getTuanKetThuc();

            boolean tuanNayLaDau  = tuanBdGoc != null
                    && !tuanBdGoc.isBefore(thu2) && !tuanBdGoc.isAfter(thu7);
            boolean tuanNayLaCuoi = tuanKtGoc != null
                    && !tuanKtGoc.isBefore(thu2) && !tuanKtGoc.isAfter(thu7);
            boolean chiMotTuan    = tuanNayLaDau && tuanNayLaCuoi;

            if (chiMotTuan) {
                tkbRepo.delete(tkb);
            } else if (tuanNayLaDau) {
                tkbRepo.updateTuanBatDau(tkb.getId(),
                        thu7.plusDays(1).with(DayOfWeek.MONDAY));
            } else if (tuanNayLaCuoi) {
                tkbRepo.updateTuanKetThuc(tkb.getId(), thu2.minusDays(1));
            } else {
                tkbRepo.updateTuanKetThuc(tkb.getId(), thu2.minusDays(1));
                ThoiKhoaBieu sau = ThoiKhoaBieu.builder()
                        .lopHocPhan(tkb.getLopHocPhan())
                        .thuTrongTuan(tkb.getThuTrongTuan())
                        .tietBatDau(tkb.getTietBatDau())
                        .soTiet(tkb.getSoTiet())
                        .phongHoc(tkb.getPhongHoc())
                        .tuanBatDau(thu7.plusDays(1).with(DayOfWeek.MONDAY))
                        .tuanKetThuc(tuanKtGoc)
                        .build();
                tkbRepo.save(sau);
            }
        }
    }

    @Override
    @Transactional
    public void huyOverride(Long id, LocalDate ngayTrongTuan) {
        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch ID=" + id));

        if (!tkb.isOverride()) {
            throw new IllegalStateException("Bản ghi này không phải override, không thể hủy!");
        }

        tkbRepo.delete(tkb);
        // Bản gốc (đã bị thu hẹp trước đó) tự hiện lại khi reload
    }
}