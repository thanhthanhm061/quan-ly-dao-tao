package com.qldt.service.impl;

import com.qldt.model.DonNghi;
import com.qldt.model.NhanVien;
import com.qldt.model.enums.TrangThaiDonNghi;
import com.qldt.repository.DonNghiRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.DonNghiService;
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
public class DonNghiServiceImpl implements DonNghiService {

    private final DonNghiRepository donNghiRepo;
    private final NhanVienRepository nhanVienRepo;

    // Mã chức vụ có quyền duyệt đơn nghỉ
    private static final Set<String> MA_CHUC_VU_DUYET = Set.of("TK", "PTK", "TBM");

    /* =========================================================
       HELPER — kiểm tra quyền duyệt
    ========================================================= */

    /**
     * Trả về true nếu nguoiDuyet có quyền duyệt đơn của nguoiNop.
     * Điều kiện:
     *   - Admin (VaiTro.ADMIN) → luôn có quyền
     *   - Chức vụ TK / PTK / TBM + cùng khoa với người nộp
     */
    private void kiemTraQuyenDuyet(NhanVien nguoiDuyet, NhanVien nguoiNop) {

        // Admin luôn có quyền
        if (nguoiDuyet.getNguoiDung() != null
                && nguoiDuyet.getNguoiDung().getVaiTro() != null
                && nguoiDuyet.getNguoiDung().getVaiTro().name().equals("ADMIN")) {
            return;
        }

        // Kiểm tra chức vụ
        String maChucVu = nguoiDuyet.getChucVu() != null
                ? nguoiDuyet.getChucVu().getMaChucVu()
                : null;

        if (maChucVu == null || !MA_CHUC_VU_DUYET.contains(maChucVu)) {
            throw new IllegalStateException(
                    "Bạn không có quyền duyệt đơn nghỉ. "
                            + "Chỉ Trưởng khoa, Phó trưởng khoa, Trưởng bộ môn hoặc Admin mới được duyệt."
            );
        }

        // Kiểm tra cùng khoa
        Long khoaDuyet = nguoiDuyet.getKhoa() != null ? nguoiDuyet.getKhoa().getId() : null;
        Long khoaNop   = nguoiNop.getKhoa()   != null ? nguoiNop.getKhoa().getId()   : null;

        if (khoaDuyet == null || !khoaDuyet.equals(khoaNop)) {
            throw new IllegalStateException(
                    "Bạn chỉ có quyền duyệt đơn của nhân viên trong khoa mình quản lý."
            );
        }
    }

    /* =========================================================
       CRUD
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<DonNghi> findAll() {
        return donNghiRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DonNghi> findById(Long id) {
        return donNghiRepo.findById(id);
    }

    @Override
    public DonNghi taoMoi(Long nguoiNopId, LocalDate ngayBatDau, LocalDate ngayKetThuc,
                          String lyDo, String loaiNghi, String fileDinhKem) {

        NhanVien nguoiNop = nhanVienRepo.findById(nguoiNopId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        // Validate ngày
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được trống");
        }
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
        if (ngayBatDau.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Không thể tạo đơn nghỉ cho ngày đã qua");
        }

        // Kiểm tra trùng thời gian
        if (donNghiRepo.existsTrungThoiGian(nguoiNopId, ngayBatDau, ngayKetThuc)) {
            throw new IllegalStateException(
                    "Bạn đã có đơn nghỉ trong khoảng thời gian này. Vui lòng kiểm tra lại."
            );
        }

        DonNghi don = DonNghi.builder()
                .nguoiNop(nguoiNop)
                .ngayBatDau(ngayBatDau)
                .ngayKetThuc(ngayKetThuc)
                .lyDo(lyDo)
                .loaiNghi(loaiNghi)
                .fileDinhKem(fileDinhKem)
                .trangThai(TrangThaiDonNghi.CHO_DUYET)
                .build();

        return donNghiRepo.save(don);
    }

    @Override
    public void huy(Long donNghiId, Long nguoiNopId) {

        DonNghi don = donNghiRepo.findById(donNghiId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));

        // Chỉ người nộp mới được hủy
        if (!don.getNguoiNop().getId().equals(nguoiNopId)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn này");
        }

        if (!don.isCoTheHuy()) {
            throw new IllegalStateException(
                    "Không thể hủy đơn đã được duyệt hoặc từ chối. Trạng thái hiện tại: "
                            + don.getTrangThai().getTenHienThi()
            );
        }

        don.setTrangThai(TrangThaiDonNghi.DA_HUY);
        donNghiRepo.save(don);
    }

    /* =========================================================
       DUYỆT / TỪ CHỐI
    ========================================================= */

    @Override
    public void duyet(Long donNghiId, Long nguoiDuyetId) {

        DonNghi don = donNghiRepo.findById(donNghiId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));

        if (don.getTrangThai() != TrangThaiDonNghi.CHO_DUYET) {
            throw new IllegalStateException(
                    "Đơn không ở trạng thái chờ duyệt. Trạng thái hiện tại: "
                            + don.getTrangThai().getTenHienThi()
            );
        }

        NhanVien nguoiDuyet = nhanVienRepo.findById(nguoiDuyetId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người duyệt"));

        // Kiểm tra quyền
        kiemTraQuyenDuyet(nguoiDuyet, don.getNguoiNop());

        don.setTrangThai(TrangThaiDonNghi.DA_DUYET);
        don.setNguoiDuyet(nguoiDuyet);
        don.setNgayDuyet(java.time.LocalDateTime.now());

        donNghiRepo.save(don);
    }

    @Override
    public void tuChoi(Long donNghiId, Long nguoiDuyetId, String lyDoTuChoi) {

        if (lyDoTuChoi == null || lyDoTuChoi.isBlank()) {
            throw new IllegalArgumentException("Phải nhập lý do từ chối");
        }

        DonNghi don = donNghiRepo.findById(donNghiId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));

        if (don.getTrangThai() != TrangThaiDonNghi.CHO_DUYET) {
            throw new IllegalStateException(
                    "Đơn không ở trạng thái chờ duyệt. Trạng thái hiện tại: "
                            + don.getTrangThai().getTenHienThi()
            );
        }

        NhanVien nguoiDuyet = nhanVienRepo.findById(nguoiDuyetId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người duyệt"));

        kiemTraQuyenDuyet(nguoiDuyet, don.getNguoiNop());

        don.setTrangThai(TrangThaiDonNghi.TU_CHOI);
        don.setNguoiDuyet(nguoiDuyet);
        don.setNgayDuyet(java.time.LocalDateTime.now());
        don.setLyDoTuChoi(lyDoTuChoi.trim());

        donNghiRepo.save(don);
    }

    /* =========================================================
       QUERY
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<DonNghi> findByNguoiNop(Long nguoiNopId) {
        return donNghiRepo.findByNguoiNopIdOrderByNgayTaoDesc(nguoiNopId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonNghi> findByTrangThai(TrangThaiDonNghi trangThai) {
        return donNghiRepo.findByTrangThaiOrderByNgayTaoDesc(trangThai);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonNghi> findChoDuyetTheoKhoa(Long khoaId) {
        return donNghiRepo.findByTrangThaiAndKhoaId(TrangThaiDonNghi.CHO_DUYET, khoaId);
    }

    @Override
    @Transactional(readOnly = true)
    public long demChoDuyet() {
        return donNghiRepo.countByTrangThai(TrangThaiDonNghi.CHO_DUYET);
    }
}