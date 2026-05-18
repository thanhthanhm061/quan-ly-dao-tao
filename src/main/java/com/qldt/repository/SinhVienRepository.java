package com.qldt.repository;

import com.qldt.model.SinhVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SinhVienRepository extends JpaRepository<SinhVien, Long> {

    // =========================
    // TÌM KIẾM
    // =========================

    Optional<SinhVien> findByMaSv(String maSv);

    boolean existsByMaSv(String maSv);

    List<SinhVien> findByHoTenContainingIgnoreCase(String hoTen);

    List<SinhVien> findByLopId(Long lopId);

    Optional<SinhVien> findByNguoiDungId(Long nguoiDungId);

    long countByLopId(Long lopId);

    // =========================
    // XEM CHI TIẾT SINH VIÊN
    // =========================

    Optional<SinhVien> findById(Long id);

    // =========================
    // XEM CHI TIẾT SINH VIÊN KÈM LỚP VÀ CỐ VẤN HỌC TẬP
    // =========================
    @Query("""
    SELECT sv FROM SinhVien sv
    LEFT JOIN FETCH sv.lop l
    LEFT JOIN FETCH l.coVanHocTap
    WHERE sv.id = :id
""")
    Optional<SinhVien> findByIdWithLopAndCvht(@Param("id") Long id);
    // =========================
    // THỐNG KÊ SINH VIÊN THEO KHOA
    // =========================

    @Query("""
        SELECT 
            sv.lop.khoa.tenKhoa,
            COUNT(sv.id)
        FROM SinhVien sv
        GROUP BY sv.lop.khoa.tenKhoa
        ORDER BY COUNT(sv.id) DESC
    """)
    List<Object[]> thongKeSinhVienTheoKhoa();

}