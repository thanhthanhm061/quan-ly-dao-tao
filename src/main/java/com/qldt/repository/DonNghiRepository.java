package com.qldt.repository;

import com.qldt.model.DonNghi;
import com.qldt.model.enums.TrangThaiDonNghi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonNghiRepository extends JpaRepository<DonNghi, Long> {

    // Đơn nghỉ đã duyệt của GV trong khoảng thời gian (cho calendar)
    // SAU
    @Query("""
    SELECT d FROM DonNghi d
    JOIN FETCH d.nguoiNop
    WHERE d.nguoiNop.id = :gvId
      AND d.trangThai = 'DA_DUYET'
      AND d.ngayBatDau <= :to
      AND d.ngayKetThuc >= :from
""")
    List<DonNghi> findDaDuyetByGvAndKhoang(
            @Param("gvId") Long gvId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
    // Đơn nghỉ đã duyệt của khoa trong khoảng thời gian
    @Query("""
    SELECT d FROM DonNghi d
    JOIN FETCH d.nguoiNop
    WHERE d.nguoiNop.khoa.id = :khoaId
      AND d.trangThai = 'DA_DUYET'
      AND d.ngayBatDau <= :to
      AND d.ngayKetThuc >= :from
""")
    List<DonNghi> findDaDuyetByKhoaAndKhoang(
            @Param("khoaId") Long khoaId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Tất cả đơn của một nhân viên
    List<DonNghi> findByNguoiNopIdOrderByNgayTaoDesc(Long nguoiNopId);

    // Lọc theo trạng thái
    List<DonNghi> findByTrangThaiOrderByNgayTaoDesc(TrangThaiDonNghi trangThai);

    // Đơn của nhân viên theo trạng thái
    List<DonNghi> findByNguoiNopIdAndTrangThaiOrderByNgayTaoDesc(
            Long nguoiNopId, TrangThaiDonNghi trangThai);

    // Đơn chờ duyệt thuộc khoa (dùng cho TK/PTK/TBM)
    @Query("""
            SELECT d FROM DonNghi d
            WHERE d.trangThai = :trangThai
              AND d.nguoiNop.khoa.id = :khoaId
            ORDER BY d.ngayTao DESC
            """)
    List<DonNghi> findByTrangThaiAndKhoaId(
            @Param("trangThai") TrangThaiDonNghi trangThai,
            @Param("khoaId") Long khoaId);

    // Kiểm tra trùng thời gian nghỉ (cùng người, cùng khoảng ngày)
    @Query("""
            SELECT COUNT(d) > 0 FROM DonNghi d
            WHERE d.nguoiNop.id = :nguoiNopId
              AND d.trangThai <> com.qldt.model.enums.TrangThaiDonNghi.DA_HUY
              AND d.trangThai <> com.qldt.model.enums.TrangThaiDonNghi.TU_CHOI
              AND d.ngayBatDau <= :ngayKetThuc
              AND d.ngayKetThuc >= :ngayBatDau
            """)
    boolean existsTrungThoiGian(
            @Param("nguoiNopId") Long nguoiNopId,
            @Param("ngayBatDau") LocalDate ngayBatDau,
            @Param("ngayKetThuc") LocalDate ngayKetThuc);

    // Đếm đơn chờ duyệt toàn hệ thống (dùng cho Admin dashboard)
    long countByTrangThai(TrangThaiDonNghi trangThai);

    // ── Query mới có JOIN FETCH (dùng để render Thymeleaf) ───────────────────
    @Query("""
            SELECT d FROM DonNghi d
            JOIN FETCH d.nguoiNop
            LEFT JOIN FETCH d.nguoiDuyet
            ORDER BY d.ngayTao DESC
            """)
    List<DonNghi> findAllWithNhanVien();

    @Query("""
            SELECT d FROM DonNghi d
            JOIN FETCH d.nguoiNop
            LEFT JOIN FETCH d.nguoiDuyet
            WHERE d.id = :id
            """)
    Optional<DonNghi> findByIdWithNhanVien(@Param("id") Long id);

    @Query("""
            SELECT d FROM DonNghi d
            JOIN FETCH d.nguoiNop
            LEFT JOIN FETCH d.nguoiDuyet
            WHERE d.nguoiNop.id = :nguoiNopId
            ORDER BY d.ngayTao DESC
            """)
    List<DonNghi> findByNguoiNopWithNhanVien(@Param("nguoiNopId") Long nguoiNopId);

    @Query("""
            SELECT d FROM DonNghi d
            JOIN FETCH d.nguoiNop
            LEFT JOIN FETCH d.nguoiDuyet
            WHERE d.trangThai = :trangThai
            ORDER BY d.ngayTao DESC
            """)
    List<DonNghi> findByTrangThaiWithNhanVien(@Param("trangThai") TrangThaiDonNghi trangThai);

    @Query("""
            SELECT d FROM DonNghi d
            JOIN FETCH d.nguoiNop
            LEFT JOIN FETCH d.nguoiDuyet
            WHERE d.trangThai = com.qldt.model.enums.TrangThaiDonNghi.CHO_DUYET
              AND d.nguoiNop.khoa.id = :khoaId
            ORDER BY d.ngayTao DESC
            """)
    List<DonNghi> findChoDuyetTheoKhoaWithNhanVien(@Param("khoaId") Long khoaId);
    // Đếm đơn chờ duyệt theo khoa (badge sidebar cho TK/PTK/TBM)
    long countByTrangThaiAndNguoiNop_Khoa_Id(
            TrangThaiDonNghi trangThai, Long khoaId);



}