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
            WHERE LOWER(nv.hoTen)       LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(nv.maNhanVien)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(nv.email)       LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    List<NhanVien> timKiem(@Param("q") String q);

    // ── Chỉ lấy nhân viên là giảng viên (chức vụ GV*, TBM, TK, PTK) ───
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.maChucVu LIKE 'GV%'
               OR cv.maChucVu IN ('TBM', 'TK', 'PTK', 'CNTT', 'CVHT')
            """)
    List<NhanVien> findAllGiangVien();
    @Query("""
        SELECT nv FROM NhanVien nv
        JOIN nv.chucVu cv
        WHERE cv.maChucVu = 'CVHT'
        """)
    List<NhanVien> findAllCoVanHocTap();

    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE nv.khoa.id = :khoaId
              AND (cv.maChucVu LIKE 'GV%'
               OR cv.maChucVu IN ('TBM', 'TK', 'PTK', 'CNTT', 'CVHT'))
            """)
    List<NhanVien> findGiangVienByKhoa(@Param("khoaId") Long khoaId);

    // ── Tìm giảng viên theo tài khoản ───────────────────────────────────
    @Query("""
            SELECT nv FROM NhanVien nv
            WHERE nv.nguoiDung.id = :nguoiDungId
              AND (nv.chucVu.maChucVu LIKE 'GV%'
               OR nv.chucVu.maChucVu IN ('TBM', 'TK', 'PTK', 'CNTT', 'CVHT'))
            """)
    Optional<NhanVien> findGiangVienByNguoiDungId(@Param("nguoiDungId") Long nguoiDungId);

    // ── Tìm kiếm giảng viên fulltext ────────────────────────────────────
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE (cv.maChucVu LIKE 'GV%' OR cv.maChucVu IN ('TBM','TK','PTK', 'CNTT', 'CVHT'))
              AND (LOWER(nv.hoTen)      LIKE LOWER(CONCAT('%', :q, '%'))
               OR  LOWER(nv.maNhanVien) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    List<NhanVien> searchGiangVien(@Param("q") String q);

    // ── Đếm ─────────────────────────────────────────────────────────────
    @Query("""
            SELECT COUNT(nv) FROM NhanVien nv
            JOIN nv.chucVu cv
            WHERE cv.maChucVu LIKE 'GV%'
               OR cv.maChucVu IN ('TBM', 'TK', 'PTK', 'CNTT', 'CVHT')
            """)
    long countGiangVien();

    // tìm hồ sơ nhân viên findByNguoiDungUsername
    @Query("""
            SELECT nv FROM NhanVien nv
            JOIN nv.nguoiDung nd
            WHERE nd.username = :username
            """)


    Optional<NhanVien> findByNguoiDungUsername(@Param("username") String username
    );
    // Danh sách giảng viên (nhân viên có chức vụ GVC)   findByChucVuMaChucVu
    List<NhanVien> findByChucVuMaChucVu(String maChucVu);

}
