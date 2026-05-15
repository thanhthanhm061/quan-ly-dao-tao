package com.qldt.repository;

import com.qldt.model.PhongHoc;
import com.qldt.model.enums.LoaiPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PhongHocRepository extends JpaRepository<PhongHoc, Long> {
    Optional<PhongHoc> findByMaPhong(String maPhong);
    boolean existsByMaPhong(String maPhong);
    List<PhongHoc> findByHoatDongTrue();
    List<PhongHoc> findByLoaiPhong(LoaiPhong loai); // ← sửa PhongHoc.LoaiPhong → LoaiPhong
}