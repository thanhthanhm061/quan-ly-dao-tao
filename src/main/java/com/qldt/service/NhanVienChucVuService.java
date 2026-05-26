package com.qldt.service;

import com.qldt.model.NhanVienChucVu;
import java.util.*;

public interface NhanVienChucVuService {
    List<NhanVienChucVu> findAll();
    List<NhanVienChucVu> findByNhanVienId(Long nhanVienId);
    List<NhanVienChucVu> getLichSu(Long nhanVienId);
    Optional<NhanVienChucVu> findById(Long id);
    NhanVienChucVu save(NhanVienChucVu record);
    void ketThucChucVu(Long id); // Đặt ngayKetThuc = hôm nay
    void delete(Long id);
}