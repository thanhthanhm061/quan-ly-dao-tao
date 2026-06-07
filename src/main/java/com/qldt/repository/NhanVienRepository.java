package com.qldt.repository;

import com.qldt.model.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {

    // ── Tìm theo tài khoản ──────────────────────────────────────────────
    Optional<NhanVien> findByNguoiDungId(Long nguoiDungId);
    Optional<NhanVien> findByMaNhanVien(String maNhanVien);
    Optional<NhanVien> findByEmail(String email);

    // ── Tìm theo khoa ───────────────────────────────────────────────────
    List<NhanVien> findByKhoaId(Long khoaId);

    // ── Tìm kiếm fulltext ───────────────────────────────────────────────
    @Query("""
            SELECT nv FROM NhanVien nv
            WHERE LOWER(nv.hoTen)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(nv.email)      LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<NhanVien> timKiem(@Param("q") String q);

    // ── Giảng viên: dùng flag laNhanSuGiangDay thay vì hardcode mã ──────
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.laNhanSuGiangDay = true
              AND nv.trangThai = true
            """)
    List<NhanVien> findAllGiangVien();

    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE nv.khoa.id = :khoaId
              AND cv.laNhanSuGiangDay = true
              AND nv.trangThai = true
            """)
    List<NhanVien> findGiangVienByKhoa(@Param("khoaId") Long khoaId);

    @Query("""
            SELECT nv FROM NhanVien nv
            WHERE nv.nguoiDung.id = :nguoiDungId
              AND nv.chucVu.laNhanSuGiangDay = true
            """)
    Optional<NhanVien> findGiangVienByNguoiDungId(@Param("nguoiDungId") Long nguoiDungId);

    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.laNhanSuGiangDay = true
              AND (LOWER(nv.hoTen)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR  LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    List<NhanVien> searchGiangVien(@Param("q") String q);

    @Query("""
            SELECT COUNT(nv) FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.laNhanSuGiangDay = true
              AND nv.trangThai = true
            """)
    long countGiangVien();

    // ── Cố vấn học tập ──────────────────────────────────────────────────
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.maChucVu = 'CVHT'
            """)
    List<NhanVien> findAllCoVanHocTap();

    // ── Tìm hồ sơ theo username ─────────────────────────────────────────
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.nguoiDung nd
            WHERE nd.username = :username
            """)
    Optional<NhanVien> findByNguoiDungUsername(@Param("username") String username);

    // ── Tìm theo mã chức vụ (giữ lại để tương thích) ───────────────────
    List<NhanVien> findByChucVuMaChucVu(String maChucVu);
}