package com.qldt.service;

import com.qldt.model.PhongHoc;
import java.util.List;
import java.util.Optional;

public interface PhongHocService {
    List<PhongHoc> findAll();
    List<PhongHoc> findAllHoatDong();
    Optional<PhongHoc> findById(Long id);
    Optional<PhongHoc> findByMaPhong(String maPhong);
    PhongHoc save(PhongHoc phongHoc);
    void delete(Long id);
}