package com.qldt.service;

import com.qldt.model.SinhVien;
import java.util.List;
import java.util.Optional;

public interface SinhVienService {
    List<SinhVien> findAll();
    List<SinhVien> search(String keyword);
    Optional<SinhVien> findById(Long id);
    Optional<SinhVien> findByMaSv(String maSv);
    Optional<SinhVien> findByNguoiDungId(Long nguoiDungId);
    SinhVien save(SinhVien sv);
    void delete(Long id);
    boolean existsByMaSv(String maSv);
    long count();
}
