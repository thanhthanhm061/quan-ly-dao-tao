package com.qldt.service.impl;

import com.qldt.model.ThoiKhoaBieu;
import com.qldt.repository.ThoiKhoaBieuRepository;
import com.qldt.service.ThoiKhoaBieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ThoiKhoaBieuServiceImpl implements ThoiKhoaBieuService {

    private final ThoiKhoaBieuRepository tkbRepo;

    @Override
    public ThoiKhoaBieu save(ThoiKhoaBieu tkb) {
        if (tkb.getLopHocPhan() == null)
            throw new IllegalArgumentException("Chưa chọn lớp học phần");

        // Kiểm tra trùng lịch giảng viên
        Long gvId = tkb.getLopHocPhan().getGiangVien().getId();
        String hocKy = tkb.getLopHocPhan().getHocKy();
        List<ThoiKhoaBieu> lichGv = tkbRepo.findByGiangVienAndHocKy(gvId, hocKy);

        for (ThoiKhoaBieu existing : lichGv) {
            if (!existing.getId().equals(tkb.getId()) && existing.trungLich(tkb)) {
                throw new IllegalStateException("Giảng viên bị trùng lịch! " +
                    existing.getTenThu() + " tiết " + existing.getTietBatDau() +
                    "-" + (existing.getTietBatDau() + existing.getSoTiet() - 1) +
                    " tại phòng " + existing.getPhongHoc());
            }
        }
        return tkbRepo.save(tkb);
    }

    @Override
    public void delete(Long id) { tkbRepo.deleteById(id); }

    @Override @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByLHP(Long lhpId) { return tkbRepo.findByLopHocPhanId(lhpId); }

    @Override @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVien(Long gvId, String hocKy) {
        return tkbRepo.findByGiangVienAndHocKy(gvId, hocKy);
    }

    @Override @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVien(Long svId, String hocKy) {
        return tkbRepo.findBySinhVienAndHocKy(svId, hocKy);
    }
}
