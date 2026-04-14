package com.qldt.service.impl;

import com.qldt.model.*;
import com.qldt.model.enums.TrangThaiLHP;
import com.qldt.repository.*;
import com.qldt.service.LopHocPhanService;
import com.qldt.service.ThoiKhoaBieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LopHocPhanServiceImpl implements LopHocPhanService {

    private final LopHocPhanRepository lhpRepo;
    private final DangKyRepository dangKyRepo;
    private final SinhVienRepository svRepo;
    private final ThoiKhoaBieuRepository tkbRepo;

    @Override @Transactional(readOnly = true)
    public List<LopHocPhan> findAll() { return lhpRepo.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<LopHocPhan> findByHocKy(String hocKy) { return lhpRepo.findByHocKy(hocKy); }

    @Override @Transactional(readOnly = true)
    public List<LopHocPhan> findByGiangVien(Long gvId) { return lhpRepo.findByGiangVienId(gvId); }

    @Override @Transactional(readOnly = true)
    public Optional<LopHocPhan> findById(Long id) { return lhpRepo.findById(id); }

    @Override
    public LopHocPhan save(LopHocPhan lhp) {
        if (lhp.getId() == null && lhpRepo.existsByMaLhp(lhp.getMaLhp()))
            throw new IllegalArgumentException("Mã lớp học phần '" + lhp.getMaLhp() + "' đã tồn tại");
        return lhpRepo.save(lhp);
    }

    @Override
    public void delete(Long id) {
        LopHocPhan lhp = lhpRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));
        if (!lhp.getDangKys().isEmpty())
            throw new IllegalStateException("Không thể xóa! Lớp đã có " + lhp.getDangKys().size() + " sinh viên đăng ký");
        lhpRepo.deleteById(id);
    }

    @Override
    public void dangKy(Long svId, Long lhpId) {
        SinhVien sv = svRepo.findById(svId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        LopHocPhan lhp = lhpRepo.findById(lhpId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));

        // Kiểm tra trạng thái lớp
        if (lhp.getTrangThai() != TrangThaiLHP.MO)
            throw new IllegalStateException("Lớp học phần đã đóng đăng ký");

        // Kiểm tra còn chỗ
        if (!lhp.isConCho())
            throw new IllegalStateException("Lớp học phần đã đầy (" + lhp.getSiSoMax() + "/" + lhp.getSiSoMax() + " chỗ)");

        // Kiểm tra đã đăng ký chưa
        if (dangKyRepo.existsBySinhVienIdAndLopHocPhanId(svId, lhpId))
            throw new IllegalStateException("Sinh viên đã đăng ký lớp học phần này rồi");

        // Kiểm tra trùng lịch
        List<ThoiKhoaBieu> lichSV = tkbRepo.findBySinhVienAndHocKy(svId, lhp.getHocKy());
        List<ThoiKhoaBieu> lichLHP = tkbRepo.findByLopHocPhanId(lhpId);
        for (ThoiKhoaBieu svTkb : lichSV) {
            for (ThoiKhoaBieu newTkb : lichLHP) {
                if (svTkb.trungLich(newTkb)) {
                    throw new IllegalStateException("Trùng lịch học! " + svTkb.getTenThu() +
                        " tiết " + svTkb.getTietBatDau() + " với lớp " + lhp.getMaLhp());
                }
            }
        }

        // Lưu đăng ký
        DangKy dk = DangKy.builder().sinhVien(sv).lopHocPhan(lhp).build();
        dangKyRepo.save(dk);
        lhp.setSiSoHienTai(lhp.getSiSoHienTai() + 1);
        lhpRepo.save(lhp);
    }

    @Override
    public void huyDangKy(Long svId, Long lhpId) {
        DangKy dk = dangKyRepo.findBySinhVienIdAndLopHocPhanId(svId, lhpId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin đăng ký"));
        dangKyRepo.deleteById(dk.getId());
        LopHocPhan lhp = lhpRepo.findById(lhpId).orElse(null);
        if (lhp != null) {
            lhp.setSiSoHienTai(Math.max(0, lhp.getSiSoHienTai() - 1));
            lhpRepo.save(lhp);
        }
    }

    @Override
    public void capNhatDiem(Long dkId, Double diemQT, Double diemThi) {
        DangKy dk = dangKyRepo.findById(dkId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký"));
        if (diemQT != null && (diemQT < 0 || diemQT > 10))
            throw new IllegalArgumentException("Điểm quá trình phải từ 0 đến 10");
        if (diemThi != null && (diemThi < 0 || diemThi > 10))
            throw new IllegalArgumentException("Điểm thi phải từ 0 đến 10");
        dk.setDiemQuaTrinh(diemQT);
        dk.setDiemThi(diemThi);
        dk.tinhDiemTongKet();
        dangKyRepo.save(dk);
    }

    @Override @Transactional(readOnly = true)
    public List<DangKy> getDanhSachDangKy(Long lhpId) { return dangKyRepo.findByLopHocPhanId(lhpId); }

    @Override @Transactional(readOnly = true)
    public List<DangKy> getDangKyCuaSinhVien(Long svId) { return dangKyRepo.findBySinhVienId(svId); }

    @Override @Transactional(readOnly = true)
    public List<String> findAllHocKy() { return lhpRepo.findAllHocKy(); }

    @Override @Transactional(readOnly = true)
    public long count() { return lhpRepo.count(); }
}
