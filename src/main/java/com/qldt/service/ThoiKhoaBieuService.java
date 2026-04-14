package com.qldt.service;
import com.qldt.model.ThoiKhoaBieu;
import java.util.*;

public interface ThoiKhoaBieuService {
    ThoiKhoaBieu save(ThoiKhoaBieu tkb);
    void delete(Long id);
    List<ThoiKhoaBieu> findByLHP(Long lhpId);
    List<ThoiKhoaBieu> findByGiangVien(Long gvId, String hocKy);
    List<ThoiKhoaBieu> findBySinhVien(Long svId, String hocKy);
}
