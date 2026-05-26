package com.qldt.repository;

import com.qldt.model.ChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChucVuRepository extends JpaRepository<ChucVu, Long> {
    Optional<ChucVu> findByMaChucVu(String maChucVu);
    boolean existsByMaChucVu(String maChucVu);
    List<ChucVu> findByTrangThai(Boolean trangThai);
    List<ChucVu> findByKhoaId(Long khoaId);
    List<ChucVu> findByKhoaIsNull(); // Chức vụ cấp trường (không thuộc khoa cụ thể)
    List<ChucVu> findAllByOrderByTenChucVuAsc();
    List<ChucVu> findByTenChucVuContainingIgnoreCase(String ten);
}