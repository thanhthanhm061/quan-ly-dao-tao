package com.qldt.service.impl;

import com.qldt.model.LopHocPhan;
import com.qldt.model.ThoiKhoaBieu;
import com.qldt.repository.LopHocPhanRepository;
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
    private final LopHocPhanRepository lhpRepo;

    @Override
    public ThoiKhoaBieu save(ThoiKhoaBieu tkb) {
        if (tkb.getLopHocPhan() == null || tkb.getLopHocPhan().getId() == null)
            throw new IllegalArgumentException("Chưa chọn lớp học phần");

        // Load đầy đủ từ DB, tránh giangVien null
        LopHocPhan lhp = lhpRepo.findById(tkb.getLopHocPhan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));
        tkb.setLopHocPhan(lhp);

        if (lhp.getGiangVien() == null)
            throw new IllegalArgumentException("Lớp học phần chưa có giảng viên");

        Long gvId = lhp.getGiangVien().getId();
        String hocKy = lhp.getHocKy();
        List<ThoiKhoaBieu> lichGv = tkbRepo.findByGiangVienAndHocKy(gvId, hocKy);

        for (ThoiKhoaBieu existing : lichGv) {
            if ((tkb.getId() == null || !existing.getId().equals(tkb.getId()))
                    && existing.trungLich(tkb)) {
                throw new IllegalStateException("Giảng viên bị trùng lịch! " +
                        existing.getTenThu() + " tiết " + existing.getTietBatDau() +
                        "-" + (existing.getTietBatDau() + existing.getSoTiet() - 1) +
                        " tại phòng " + existing.getPhongHoc());
            }
        }
        return tkbRepo.save(tkb);
    }

    @Override
    public void delete(Long id) {
        tkbRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByLHP(Long lhpId) {
        return tkbRepo.findByLopHocPhanId(lhpId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findByGiangVien(Long gvId, String hocKy) {
        return tkbRepo.findByGiangVienAndHocKy(gvId, hocKy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThoiKhoaBieu> findBySinhVien(Long svId, String hocKy) {
        return tkbRepo.findBySinhVienAndHocKy(svId, hocKy);
    }
}