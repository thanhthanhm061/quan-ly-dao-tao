package com.qldt.service;

import com.qldt.model.ChucVu;
import java.util.*;

public interface ChucVuService {
    List<ChucVu> findAll();
    List<ChucVu> findAllHoatDong();
    List<ChucVu> findByKhoaId(Long khoaId);
    List<ChucVu> timKiem(String keyword);
    Optional<ChucVu> findById(Long id);
    ChucVu save(ChucVu chucVu);
    void delete(Long id);
    void doiTrangThai(Long id);
    long count();
}