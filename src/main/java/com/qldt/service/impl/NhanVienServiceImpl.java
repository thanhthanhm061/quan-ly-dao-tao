package com.qldt.service.impl;

import com.qldt.model.NhanVien;
import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.NguoiDungService;
import com.qldt.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepo;
    private final NguoiDungService nguoiDungService;

    // ── CRUD chung ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NhanVien> findAll() {
        return nhanVienRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NhanVien> findById(Long id) {
        return nhanVienRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NhanVien> findByMaNhanVien(String ma) {
        return nhanVienRepo.findByMaNhanVien(ma);
    }

    /**
     * Lưu nhân viên.
     * - Nếu là nhân viên MỚI (id == null): tự động tạo tài khoản đăng nhập.
     * - Nếu cập nhật: giữ nguyên tài khoản cũ, chỉ cập nhật thông tin.
     */
    @Override
    public NhanVien save(NhanVien nhanVien) {
        boolean isNew = (nhanVien.getId() == null);

        if (isNew) {
            // Kiểm tra trùng mã
            if (nhanVienRepo.findByMaNhanVien(nhanVien.getMaNhanVien()).isPresent()) {
                throw new IllegalArgumentException(
                        "Mã nhân viên '" + nhanVien.getMaNhanVien() + "' đã tồn tại");
            }

            // Tạo tài khoản đăng nhập
            String username = nhanVien.getMaNhanVien().toLowerCase();
            NguoiDung nd = nguoiDungService.taoTaiKhoan(
                    username,
                    nhanVien.getHoTen(),
                    nhanVien.getEmail(),
                    VaiTro.NHAN_VIEN          // tất cả nhân viên (kể cả GV) dùng NHAN_VIEN
            );
            nhanVien.setNguoiDung(nd);
        }

        return nhanVienRepo.save(nhanVien);
    }

    @Override
    public void delete(Long id) {
        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        // Kiểm tra còn lớp học phần không
        if (nv.getLopHocPhans() != null && !nv.getLopHocPhans().isEmpty()) {
            throw new IllegalStateException(
                    "Không thể xóa: nhân viên đang phụ trách "
                            + nv.getLopHocPhans().size() + " lớp học phần");
        }

        nhanVienRepo.delete(nv);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return nhanVienRepo.count();
    }

    // ── Tìm kiếm / lọc ─────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NhanVien> timKiem(String keyword) {
        if (keyword == null || keyword.isBlank()) return nhanVienRepo.findAll();
        return nhanVienRepo.timKiem(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NhanVien> findByKhoaId(Long khoaId) {
        return nhanVienRepo.findByKhoaId(khoaId);
    }

    // ── Chi tiết ────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NhanVien getChiTiet(Long id) {
        return nhanVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy nhân viên với id = " + id));
    }

    // ── Trạng thái ──────────────────────────────────────────────────────

    @Override
    public void doiTrangThai(Long id) {
        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
        nv.setTrangThai(!Boolean.TRUE.equals(nv.getTrangThai()));
        nhanVienRepo.save(nv);
    }

    // ── Theo tài khoản ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Optional<NhanVien> findByNguoiDungId(Long nguoiDungId) {
        return nhanVienRepo.findByNguoiDungId(nguoiDungId);
    }

    // ── Giảng viên ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NhanVien> findAllGiangVien() {
        return nhanVienRepo.findAllGiangVien();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NhanVien> searchGiangVien(String keyword) {
        if (keyword == null || keyword.isBlank()) return nhanVienRepo.findAllGiangVien();
        return nhanVienRepo.searchGiangVien(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public long countGiangVien() {
        return nhanVienRepo.countGiangVien();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NhanVien> findGiangVienByNguoiDungId(Long nguoiDungId) {
        return nhanVienRepo.findGiangVienByNguoiDungId(nguoiDungId);
    }

    @Override
    @Transactional(readOnly = true)
    public NhanVien getChiTietGiangVien(Long id) {
        NhanVien nv = nhanVienRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy giảng viên với id = " + id));
        if (!nv.isGiangVien()) {
            throw new IllegalArgumentException("Nhân viên này không phải giảng viên");
        }
        return nv;
    }
}