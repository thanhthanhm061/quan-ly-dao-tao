package com.qldt.service;

import com.qldt.model.DangKy;
import com.qldt.model.LopHocPhan;

import java.util.List;
import java.util.Optional;

public interface LopHocPhanService {

    List<LopHocPhan> findAll();
    List<LopHocPhan> findByHocKy(String hocKy);

    /**
     * Lấy tất cả lớp học phần của một nhân viên (giảng viên).
     * Tham số đổi tên từ gvId → nhanVienId cho rõ nghĩa,
     * nhưng vẫn dùng được ở mọi chỗ cũ chỉ cần đổi tên biến.
     */
    List<LopHocPhan> findByGiangVien(Long nhanVienId);

    Optional<LopHocPhan> findById(Long id);
    LopHocPhan save(LopHocPhan lhp);
    void delete(Long id);

    void dangKy(Long svId, Long lhpId);
    void huyDangKy(Long svId, Long lhpId);
    void huyDangKyAdmin(Long svId, Long lhpId);
    void capNhatDiem(Long dkId, Double diemQT, Double diemThi);

    List<DangKy> getDanhSachDangKy(Long lhpId);
    List<DangKy> getDangKyCuaSinhVien(Long svId);
    List<String> findAllHocKy();
    long count();
}