package com.qldt.service;

import com.qldt.model.NhanVien;

import java.util.List;
import java.util.Optional;

public interface NhanVienService {

    // ── CRUD chung ──────────────────────────────────────────────────────
    List<NhanVien> findAll();
    Optional<NhanVien> findById(Long id);
    Optional<NhanVien> findByMaNhanVien(String ma);
    NhanVien save(NhanVien nhanVien);
    void delete(Long id);
    long count();

    // ── Tìm kiếm / lọc ─────────────────────────────────────────────────
    List<NhanVien> timKiem(String keyword);
    List<NhanVien> findByKhoaId(Long khoaId);

    // ── Chi tiết (fetch đầy đủ liên kết) ───────────────────────────────
    NhanVien getChiTiet(Long id);

    // ── Trạng thái ──────────────────────────────────────────────────────
    void doiTrangThai(Long id);

    // ── Tìm theo tài khoản đăng nhập ───────────────────────────────────
    Optional<NhanVien> findByNguoiDungId(Long nguoiDungId);

    // ── Các method dành riêng cho GIẢNG VIÊN ───────────────────────────
    // (nhân viên có chức vụ GV*, TBM, TK, PTK)

    /** Lấy tất cả nhân viên là giảng viên */
    List<NhanVien> findAllGiangVien();

    /** Tìm kiếm trong danh sách giảng viên */
    List<NhanVien> searchGiangVien(String keyword);

    List<NhanVien> findAllCoVanHocTap();

    /** Số lượng giảng viên */
    long countGiangVien();

    /** Tìm giảng viên theo nguoiDungId */
    Optional<NhanVien> findGiangVienByNguoiDungId(Long nguoiDungId);

    /** Chi tiết giảng viên (dùng cho trang /admin/giang-vien/chi-tiet) */
    NhanVien getChiTietGiangVien(Long id);
}