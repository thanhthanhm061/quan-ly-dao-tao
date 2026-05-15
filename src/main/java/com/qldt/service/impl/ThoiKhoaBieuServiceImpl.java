package com.qldt.service.impl;

import com.qldt.model.LopHocPhan;
import com.qldt.model.PhongHoc;
import com.qldt.model.ThoiKhoaBieu;
import com.qldt.repository.LopHocPhanRepository;
import com.qldt.repository.ThoiKhoaBieuRepository;
import com.qldt.service.TKBNotificationService;
import com.qldt.service.TaiGiangDayDTO;
import com.qldt.service.ThoiKhoaBieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThoiKhoaBieuServiceImpl implements ThoiKhoaBieuService {

    private final ThoiKhoaBieuRepository tkbRepo;
    private final LopHocPhanRepository lhpRepo;
    private final ThoiKhoaBieuRepository phongHocRepo;
    private final TKBNotificationService notificationService;

    @Override
    public ThoiKhoaBieu save(ThoiKhoaBieu tkb) {

        if (tkb.getLopHocPhan() == null || tkb.getLopHocPhan().getId() == null) {
            throw new IllegalArgumentException("Chưa chọn lớp học phần");
        }

        // Load lớp học phần đầy đủ
        LopHocPhan lhp = lhpRepo.findById(tkb.getLopHocPhan().getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lớp học phần"));

        tkb.setLopHocPhan(lhp);

        // Kiểm tra giảng viên
        if (lhp.getGiangVien() == null) {
            throw new IllegalArgumentException("Lớp học phần chưa có giảng viên");
        }

        // Quy đổi số tiết nếu chưa nhập
        if (tkb.getSoTiet() == 0) {

            if (lhp.getMonHoc() == null) {
                throw new IllegalArgumentException("Lớp học phần chưa có môn học");
            }

            int tinChi = lhp.getMonHoc().getSoTinChi();

            tkb.setSoTiet(
                    tinChi > 0
                            ? Math.min(tinChi * 3, 5)
                            : 3
            );
        }

        Long gvId = lhp.getGiangVien().getId();
        String hocKy = lhp.getHocKy();

        // ========================
        // KIỂM TRA TRÙNG GIẢNG VIÊN
        // ========================

        List<ThoiKhoaBieu> lichGv =
                tkbRepo.findByGiangVienAndHocKy(gvId, hocKy);

        for (ThoiKhoaBieu existing : lichGv) {

            if ((tkb.getId() == null
                    || !existing.getId().equals(tkb.getId()))
                    && existing.trungLich(tkb)) {

                throw new IllegalStateException(
                        "Giảng viên bị trùng lịch! "
                                + existing.getTenThu()
                                + " tiết "
                                + existing.getTietBatDau()
                                + "-"
                                + (existing.getTietBatDau()
                                + existing.getSoTiet() - 1)
                                + " tại phòng "
                                + existing.getPhongHoc()
                );
            }
        }

        // ========================
        // KIỂM TRA TRÙNG PHÒNG
        // ========================

        if (tkb.getPhongHoc() != null
                && !tkb.getPhongHoc().isBlank()) {

            List<ThoiKhoaBieu> lichPhong =
                    tkbRepo.findByPhongHocAndHocKy(
                            tkb.getPhongHoc(),
                            hocKy
                    );

            for (ThoiKhoaBieu existing : lichPhong) {

                if ((tkb.getId() == null
                        || !existing.getId().equals(tkb.getId()))
                        && existing.trungLich(tkb)) {

                    throw new IllegalStateException(
                            "Phòng "
                                    + tkb.getPhongHoc()
                                    + " đã có lịch vào "
                                    + existing.getTenThu()
                                    + " tiết "
                                    + existing.getTietBatDau()
                                    + "-"
                                    + (existing.getTietBatDau()
                                    + existing.getSoTiet() - 1)
                                    + " ("
                                    + existing.getLopHocPhan()
                                    .getMonHoc()
                                    .getTenMon()
                                    + ")"
                    );
                }
            }
        }

        // ========================
        // LƯU
        // ========================

        ThoiKhoaBieu saved = tkbRepo.save(tkb);

        // Gửi thông báo
        notificationService.guiThongBaoThemLich(saved);

        return saved;
    }

    @Override
    public void delete(Long id) {

        ThoiKhoaBieu tkb = tkbRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lịch"));

        LopHocPhan lhp =
                lhpRepo.findById(tkb.getLopHocPhan().getId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy lớp học phần"));

        lhp.getThoiKhoaBieus()
                .removeIf(t -> t.getId().equals(id));

        lhpRepo.save(lhp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVienTuan(
            Long gvId,
            String hocKy,
            LocalDate ngayTrongTuan
    ) {

        LocalDate thu2 =
                ngayTrongTuan.with(DayOfWeek.MONDAY);

        LocalDate thu7 = thu2.plusDays(5);

        return tkbRepo.findByGiangVienAndTuan(
                gvId,
                hocKy,
                thu2,
                thu7
        );
    }

    @Override
    public List<ThoiKhoaBieu> findBySinhVienTuan(Long svId, String hocKy, LocalDate ngayTrongTuan) {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaiGiangDayDTO> thongKeTaiGiangDay(
            String hocKy
    ) {

        return tkbRepo.thongKeTaiGiangDay(hocKy)
                .stream()
                .map(row -> {

                    long gvId = (Long) row[0];
                    String ten = (String) row[1];

                    int tongTiet =
                            ((Number) row[2]).intValue();

                    int soLop =
                            ((Number) row[3]).intValue();

                    int tinChi =
                            ((Number) row[4]).intValue();

                    double ty =
                            (double) tongTiet
                                    / TaiGiangDayDTO.TIET_TOI_DA_KY;

                    return new TaiGiangDayDTO(
                            gvId,
                            ten,
                            tongTiet,
                            soLop,
                            tinChi,
                            ty
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhongHoc> timPhongTrong(
            String hocKy,
            int thu,
            int tietBd,
            int soTiet,
            int suc
    ) {

        return tkbRepo.findPhongTrong(
                hocKy,
                thu,
                tietBd,
                tietBd + soTiet,
                suc
        );
    }

    @Override
    public List<PhongHoc> findAllPhong() {
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByLHP(Long lhpId) {
        return tkbRepo.findByLopHocPhanId(lhpId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVien(
            Long gvId,
            String hocKy
    ) {

        return tkbRepo.findByGiangVienAndHocKy(
                gvId,
                hocKy
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVien(
            Long svId,
            String hocKy
    ) {

        return tkbRepo.findBySinhVienAndHocKy(
                svId,
                hocKy
        );
    }
}