package com.qldt.repository;

import com.qldt.model.DonNghi;
import com.qldt.model.enums.TrangThaiDonNghi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DonNghiRepository extends JpaRepository<DonNghi, Long> {

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
}