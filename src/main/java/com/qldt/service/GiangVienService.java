package com.qldt.service;
import com.qldt.model.GiangVien;
import java.util.*;

public interface GiangVienService {
    List<GiangVien> findAll();
    List<GiangVien> search(String keyword);
    Optional<GiangVien> findById(Long id);
    Optional<GiangVien> findByNguoiDungId(Long nguoiDungId);
    GiangVien save(GiangVien gv);
    void delete(Long id);
    long count();
}
