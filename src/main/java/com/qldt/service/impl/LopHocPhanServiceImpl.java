package com.qldt.service.impl;

import com.qldt.model.*;
import com.qldt.model.enums.TrangThaiLHP;
import com.qldt.repository.*;
import com.qldt.service.LopHocPhanService;
import com.qldt.service.ThoiKhoaBieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Component
    @RequiredArgsConstructor
    @EnableScheduling
    public class LopHocPhanScheduler {

        private final LopHocPhanRepository lhpRepo;

        /** Chạy mỗi phút, tự chuyển trạng thái dựa vào thoi_gian_mo / thoi_gian_dong */
        @Scheduled(fixedRate = 60_000) // 60 giây
        @Transactional
        public void tuDongCapNhatTrangThai() {
            LocalDateTime now = LocalDateTime.now();

            // Mở các lớp đã đến giờ mở
            lhpRepo.findByTrangThaiAndThoiGianMoBefore(TrangThaiLHP.DONG, now)
                    .forEach(lhp -> lhp.setTrangThai(TrangThaiLHP.MO));

            // Đóng các lớp đã qua giờ đóng
            lhpRepo.findByTrangThaiAndThoiGianDongBefore(TrangThaiLHP.MO, now)
                    .forEach(lhp -> lhp.setTrangThai(TrangThaiLHP.DONG));
        }
    }
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
            throw new IllegalStateException("Không thể xóa! Lớp đã có "
                    + lhp.getDangKys().size() + " sinh viên đăng ký");

        // Xóa TKB trước (hoặc để orphanRemoval tự xử lý)
        lhp.getThoiKhoaBieus().clear();
        lhpRepo.save(lhp);
        lhpRepo.deleteById(id);
    }

    @Override
    public void dangKy(Long svId, Long lhpId) {
        SinhVien sv = svRepo.findById(svId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
//        LopHocPhan lhp = lhpRepo.findById(lhpId)
//            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));
        // Trong dangKy() của LopHocPhanServiceImpl — thay findById bằng:
        LopHocPhan lhp = lhpRepo.findByIdForUpdate(lhpId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp học phần"));

        if (!lhp.isDangTrongThoiGianDangKy()) {
            String msg = "Lớp học phần chưa mở hoặc đã hết thời gian đăng ký";
            if (lhp.getThoiGianMo() != null && LocalDateTime.now().isBefore(lhp.getThoiGianMo()))
                msg = "Chưa đến thời gian đăng ký. Mở lúc: "
                        + lhp.getThoiGianMo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            if (lhp.getThoiGianDong() != null && LocalDateTime.now().isAfter(lhp.getThoiGianDong()))
                msg = "Đã hết thời gian đăng ký lúc: "
                        + lhp.getThoiGianDong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            throw new IllegalStateException(msg);
        }
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
        // FIX: validate trang thai lop truoc khi huy
        LopHocPhan lhp = lhpRepo.findById(lhpId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay lop hoc phan"));

        if (!lhp.isDangTrongThoiGianDangKy()) {
            String msg = "Lop hoc phan da dong dang ky, khong the huy";
            if (lhp.getThoiGianDong() != null && LocalDateTime.now().isAfter(lhp.getThoiGianDong()))
                msg = "Da het thoi gian dang ky luc: "
                        + lhp.getThoiGianDong().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            throw new IllegalStateException(msg);
        }

        DangKy dk = dangKyRepo.findBySinhVienIdAndLopHocPhanId(svId, lhpId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thong tin dang ky"));

        if (dk.getDiemTongKet() != null)
            throw new IllegalStateException("Khong the huy dang ky khi da co diem tong ket");

        dangKyRepo.deleteById(dk.getId());
        lhp.setSiSoHienTai(Math.max(0, lhp.getSiSoHienTai() - 1));
        lhpRepo.save(lhp);
    }

    // THEM MOI: admin huy khong bi gioi han trang thai
    public void huyDangKyAdmin(Long svId, Long lhpId) {
        DangKy dk = dangKyRepo.findBySinhVienIdAndLopHocPhanId(svId, lhpId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thong tin dang ky"));
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
