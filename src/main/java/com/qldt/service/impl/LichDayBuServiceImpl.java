package com.qldt.service.impl;

import com.qldt.model.DonNghi;
import com.qldt.model.LichDayBu;
import com.qldt.model.LopHocPhan;
import com.qldt.model.NhanVien;
import com.qldt.model.enums.TrangThaiLichBu;
import com.qldt.repository.DonNghiRepository;
import com.qldt.repository.LichDayBuRepository;
import com.qldt.repository.LopHocPhanRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.LichDayBuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class LichDayBuServiceImpl implements LichDayBuService {

    private final LichDayBuRepository lichBuRepo;
    private final LopHocPhanRepository lhpRepo;
    private final NhanVienRepository nhanVienRepo;
    private final DonNghiRepository donNghiRepo;

    // Mã chức vụ có quyền xếp lịch bù
    private static final Set<String> MA_CHUC_VU_XEP = Set.of("TK", "PTK", "TBM");

    /* =========================================================
       HELPER — kiểm tra quyền xếp lịch
    ========================================================= */

    /**
     * Admin luôn được xếp.
     * TK / PTK / TBM chỉ được xếp lịch bù cho lớp thuộc khoa mình.
     */
    private void kiemTraQuyenXep(NhanVien nguoiXep, LopHocPhan lhp) {

        // Admin
        if (nguoiXep.getNguoiDung() != null
                && nguoiXep.getNguoiDung().getVaiTro() != null
                && nguoiXep.getNguoiDung().getVaiTro().name().equals("ADMIN")) {
            return;
        }

        String maChucVu = nguoiXep.getChucVu() != null
                ? nguoiXep.getChucVu().getMaChucVu()
                : null;

        if (maChucVu == null || !MA_CHUC_VU_XEP.contains(maChucVu)) {
            throw new IllegalStateException(
                    "Bạn không có quyền xếp lịch dạy bù. "
                            + "Chỉ Admin, Trưởng khoa, Phó trưởng khoa hoặc Trưởng bộ môn mới được xếp."
            );
        }

        // Kiểm tra cùng khoa với lớp học phần
        Long khoaXep = nguoiXep.getKhoa() != null ? nguoiXep.getKhoa().getId() : null;
        Long khoaLhp  = lhp.getMonHoc() != null && lhp.getMonHoc().getKhoa() != null
                ? lhp.getMonHoc().getKhoa().getId() : null;

        if (khoaXep == null || !khoaXep.equals(khoaLhp)) {
            throw new IllegalStateException(
                    "Bạn chỉ có quyền xếp lịch bù cho lớp học phần thuộc khoa mình quản lý."
            );
        }
    }

    /* =========================================================
       HELPER — kiểm tra trùng lịch
    ========================================================= */

    private void kiemTraTrungLich(Long giangVienId, String phongHoc,
                                  LocalDate ngayDayBu, int tietBatDau,
                                  int soTiet, Long excludeId) {

        int tietKetThuc = tietBatDau + soTiet;

        // Trùng giảng viên
        if (lichBuRepo.existsTrungGiangVien(
                giangVienId, ngayDayBu, tietBatDau, tietKetThuc, excludeId)) {
            throw new IllegalStateException(
                    "Giảng viên đã có lịch dạy bù trong khung giờ này (tiết "
                            + tietBatDau + "–" + (tietKetThuc - 1) + " ngày " + ngayDayBu + ")"
            );
        }

        // Trùng phòng
        if (phongHoc != null && !phongHoc.isBlank()
                && lichBuRepo.existsTrungPhong(
                phongHoc, ngayDayBu, tietBatDau, tietKetThuc, excludeId)) {
            throw new IllegalStateException(
                    "Phòng " + phongHoc + " đã được sử dụng trong khung giờ này (tiết "
                            + tietBatDau + "–" + (tietKetThuc - 1) + " ngày " + ngayDayBu + ")"
            );
        }
    }

    /* =========================================================
       CRUD
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findAll() {
        return lichBuRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LichDayBu> findById(Long id) {
        return lichBuRepo.findById(id);
    }

    @Override
    public LichDayBu xepLich(Long lhpId, Long giangVienId, Long donNghiId,
                             LocalDate ngayNghiGoc, LocalDate ngayDayBu,
                             int thuTrongTuan, int tietBatDau, int soTiet,
                             String phongHoc, String ghiChu, Long nguoiXepId) {

        // Load entities
        LopHocPhan lhp = lhpRepo.findById(lhpId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));

        NhanVien giangVien = nhanVienRepo.findById(giangVienId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên"));

        NhanVien nguoiXep = nhanVienRepo.findById(nguoiXepId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người xếp lịch"));

        // Kiểm tra quyền
        kiemTraQuyenXep(nguoiXep, lhp);

        // Validate ngày
        if (ngayDayBu == null) {
            throw new IllegalArgumentException("Ngày dạy bù không được trống");
        }
        if (ngayNghiGoc != null && ngayDayBu.isBefore(ngayNghiGoc)) {
            throw new IllegalArgumentException("Ngày dạy bù phải sau hoặc bằng ngày nghỉ gốc");
        }

        // Validate tiết
        if (tietBatDau < 1 || tietBatDau > 12) {
            throw new IllegalArgumentException("Tiết bắt đầu phải từ 1 đến 12");
        }
        if (soTiet < 1) {
            throw new IllegalArgumentException("Số tiết phải lớn hơn 0");
        }
        if (tietBatDau + soTiet - 1 > 12) {
            throw new IllegalArgumentException("Tiết kết thúc không được vượt quá tiết 12");
        }

        // Kiểm tra trùng lịch
        kiemTraTrungLich(giangVienId, phongHoc, ngayDayBu, tietBatDau, soTiet, null);

        // Load đơn nghỉ nếu có
        DonNghi donNghi = null;
        if (donNghiId != null) {
            donNghi = donNghiRepo.findById(donNghiId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));
        }

        LichDayBu lichBu = LichDayBu.builder()
                .lopHocPhan(lhp)
                .giangVien(giangVien)
                .donNghiLienQuan(donNghi)
                .ngayNghiGoc(ngayNghiGoc)
                .ngayDayBu(ngayDayBu)
                .thuTrongTuan(thuTrongTuan)
                .tietBatDau(tietBatDau)
                .soTiet(soTiet)
                .phongHoc(phongHoc)
                .ghiChu(ghiChu)
                .nguoiXep(nguoiXep)
                .trangThai(TrangThaiLichBu.DA_XEP)
                .build();

        return lichBuRepo.save(lichBu);
    }

    @Override
    public LichDayBu capNhat(Long lichBuId, LocalDate ngayDayBu,
                             int thuTrongTuan, int tietBatDau, int soTiet,
                             String phongHoc, String ghiChu, Long nguoiXepId) {

        LichDayBu lichBu = lichBuRepo.findById(lichBuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch dạy bù"));

        if (lichBu.getTrangThai() == TrangThaiLichBu.HOAN_THANH) {
            throw new IllegalStateException("Không thể chỉnh sửa lịch bù đã hoàn thành");
        }
        if (lichBu.getTrangThai() == TrangThaiLichBu.HUY) {
            throw new IllegalStateException("Không thể chỉnh sửa lịch bù đã hủy");
        }

        NhanVien nguoiXep = nhanVienRepo.findById(nguoiXepId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người xếp lịch"));

        kiemTraQuyenXep(nguoiXep, lichBu.getLopHocPhan());

        // Kiểm tra trùng (loại trừ bản ghi hiện tại)
        kiemTraTrungLich(lichBu.getGiangVien().getId(), phongHoc,
                ngayDayBu, tietBatDau, soTiet, lichBuId);

        lichBu.setNgayDayBu(ngayDayBu);
        lichBu.setThuTrongTuan(thuTrongTuan);
        lichBu.setTietBatDau(tietBatDau);
        lichBu.setSoTiet(soTiet);
        lichBu.setPhongHoc(phongHoc);
        lichBu.setGhiChu(ghiChu);
        lichBu.setNguoiXep(nguoiXep);

        return lichBuRepo.save(lichBu);
    }

    @Override
    public void huy(Long lichBuId, Long nguoiXepId) {

        LichDayBu lichBu = lichBuRepo.findById(lichBuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch dạy bù"));

        if (lichBu.getTrangThai() == TrangThaiLichBu.HOAN_THANH) {
            throw new IllegalStateException("Không thể hủy lịch bù đã hoàn thành");
        }

        NhanVien nguoiXep = nhanVienRepo.findById(nguoiXepId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người xếp lịch"));

        kiemTraQuyenXep(nguoiXep, lichBu.getLopHocPhan());

        lichBu.setTrangThai(TrangThaiLichBu.HUY);
        lichBuRepo.save(lichBu);
    }

    @Override
    public void hoanThanh(Long lichBuId) {

        LichDayBu lichBu = lichBuRepo.findById(lichBuId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch dạy bù"));

        if (lichBu.getTrangThai() != TrangThaiLichBu.DA_XEP) {
            throw new IllegalStateException(
                    "Chỉ có thể đánh dấu hoàn thành lịch bù đang ở trạng thái Đã xếp"
            );
        }

        lichBu.setTrangThai(TrangThaiLichBu.HOAN_THANH);
        lichBuRepo.save(lichBu);
    }

    /* =========================================================
       QUERY
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findByLopHocPhan(Long lhpId) {
        return lichBuRepo.findByLopHocPhanIdOrderByNgayDayBuAsc(lhpId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findByGiangVien(Long giangVienId) {
        return lichBuRepo.findByGiangVienIdOrderByNgayDayBuAsc(giangVienId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findByDonNghi(Long donNghiId) {
        return lichBuRepo.findByDonNghiLienQuanId(donNghiId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findByGiangVienAndKhoangNgay(Long giangVienId,
                                                        LocalDate from, LocalDate to) {
        return lichBuRepo.findByGiangVienAndKhoangNgay(giangVienId, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LichDayBu> findByKhoa(Long khoaId) {
        return lichBuRepo.findByKhoaId(khoaId);
    }
}