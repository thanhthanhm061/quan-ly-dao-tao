package com.qldt.service;
import com.qldt.model.PhongHoc;
import com.qldt.model.ThoiKhoaBieu;

import java.time.LocalDate;
import java.util.*;

public interface ThoiKhoaBieuService {
    ThoiKhoaBieu save(ThoiKhoaBieu tkb);
    void delete(Long id);
    List<ThoiKhoaBieu> findByLHP(Long lhpId);
    List<ThoiKhoaBieu> findByGiangVien(Long gvId, String hocKy);
    List<ThoiKhoaBieu> findBySinhVien(Long svId, String hocKy);

    // --- Mới ---
    List<ThoiKhoaBieu> findByGiangVienTuan(Long gvId, String hocKy, LocalDate ngayTrongTuan);
    List<ThoiKhoaBieu> findBySinhVienTuan(Long svId, String hocKy, LocalDate ngayTrongTuan);
    List<TaiGiangDayDTO> thongKeTaiGiangDay(String hocKy);
    List<PhongHoc> timPhongTrong(String hocKy, int thu, int tietBd, int soTiet, int sucCanThiet);
    List<PhongHoc> findAllPhong();

}
