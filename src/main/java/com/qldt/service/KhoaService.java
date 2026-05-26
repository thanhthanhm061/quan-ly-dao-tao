package com.qldt.service;

import com.qldt.model.Khoa;
import java.util.*;

public interface KhoaService {
    List<Khoa> findAll();
    List<Khoa> findAllHoatDong();
    Optional<Khoa> findById(Long id);
    Khoa save(Khoa khoa);
    void delete(Long id);
    void doiTrangThai(Long id);
    long count();
    boolean existsByMaKhoa(String maKhoa);
}

