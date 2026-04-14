package com.qldt.service;
import com.qldt.model.*;
import java.util.*;

public interface LopHocPhanService {
    List<LopHocPhan> findAll();
    List<LopHocPhan> findByHocKy(String hocKy);
    List<LopHocPhan> findByGiangVien(Long gvId);
    Optional<LopHocPhan> findById(Long id);
    LopHocPhan save(LopHocPhan lhp);
    void delete(Long id);
    void dangKy(Long svId, Long lhpId);
    void huyDangKy(Long svId, Long lhpId);
    void capNhatDiem(Long dkId, Double diemQT, Double diemThi);
    List<DangKy> getDanhSachDangKy(Long lhpId);
    List<DangKy> getDangKyCuaSinhVien(Long svId);
    List<String> findAllHocKy();
    long count();
}
