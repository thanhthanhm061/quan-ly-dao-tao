package com.qldt.service.impl;

import com.qldt.model.*;
import com.qldt.model.enums.TrangThaiLHP;
import com.qldt.repository.*;
import com.qldt.service.LopHocPhanService;
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

    /* =========================================================
       AUTO UPDATE TRANG THAI THEO SI SO
    ========================================================= */
    private void capNhatTrangThai(LopHocPhan lhp) {

        // FULL SI SO => DONG
        if (lhp.getSiSoHienTai() >= lhp.getSiSoMax()) {

            lhp.setTrangThai(TrangThaiLHP.DONG);

            // set thời gian đóng nếu chưa có
            if (lhp.getThoiGianDong() == null) {
                lhp.setThoiGianDong(LocalDateTime.now());
            }

        } else {

            // còn chỗ => mở
            lhp.setTrangThai(TrangThaiLHP.MO);

        }
    }

    /* =========================================================
       SCHEDULER
    ========================================================= */
    @Component
    @RequiredArgsConstructor
    @EnableScheduling
    public class LopHocPhanScheduler {

        private final LopHocPhanRepository lhpRepo;

        @Scheduled(fixedRate = 60_000)
        @Transactional
        public void tuDongCapNhatTrangThai() {

            LocalDateTime now = LocalDateTime.now();

            // MỞ lớp tới thời gian mở
            lhpRepo.findByTrangThaiAndThoiGianMoBefore(TrangThaiLHP.DONG, now)
                    .forEach(lhp -> {

                        // chỉ mở nếu còn chỗ
                        if (lhp.getSiSoHienTai() < lhp.getSiSoMax()) {
                            lhp.setTrangThai(TrangThaiLHP.MO);
                        }
                    });

            // ĐÓNG lớp quá thời gian đóng
            lhpRepo.findByTrangThaiAndThoiGianDongBefore(TrangThaiLHP.MO, now)
                    .forEach(lhp ->
                            lhp.setTrangThai(TrangThaiLHP.DONG)
                    );
        }
    }

    /* =========================================================
       CRUD
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<LopHocPhan> findAll() {
        return lhpRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LopHocPhan> findByHocKy(String hocKy) {
        return lhpRepo.findByHocKy(hocKy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LopHocPhan> findByGiangVien(Long gvId) {
        return lhpRepo.findByGiangVienId(gvId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LopHocPhan> findById(Long id) {
        return lhpRepo.findById(id);
    }

    @Override
    public LopHocPhan save(LopHocPhan lhp) {

        if (lhp.getId() == null && lhpRepo.existsByMaLhp(lhp.getMaLhp())) {
            throw new IllegalArgumentException(
                    "Mã lớp học phần '" + lhp.getMaLhp() + "' đã tồn tại"
            );
        }

        capNhatTrangThai(lhp);

        return lhpRepo.save(lhp);
    }

    @Override
    public void delete(Long id) {

        LopHocPhan lhp = lhpRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lớp học phần"));

        if (!lhp.getDangKys().isEmpty()) {
            throw new IllegalStateException(
                    "Không thể xóa! Lớp đã có "
                            + lhp.getDangKys().size()
                            + " sinh viên đăng ký"
            );
        }

        lhp.getThoiKhoaBieus().clear();

        lhpRepo.save(lhp);

        lhpRepo.deleteById(id);
    }

    /* =========================================================
       DANG KY
    ========================================================= */

    @Override
    public void dangKy(Long svId, Long lhpId) {

        SinhVien sv = svRepo.findById(svId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy sinh viên"));

        LopHocPhan lhp = lhpRepo.findByIdForUpdate(lhpId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lớp học phần"));

        // check thời gian
        if (!lhp.isDangTrongThoiGianDangKy()) {

            String msg = "Lớp học phần chưa mở hoặc đã hết thời gian đăng ký";

            if (lhp.getThoiGianMo() != null
                    && LocalDateTime.now().isBefore(lhp.getThoiGianMo())) {

                msg = "Chưa đến thời gian đăng ký. Mở lúc: "
                        + lhp.getThoiGianMo()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            if (lhp.getThoiGianDong() != null
                    && LocalDateTime.now().isAfter(lhp.getThoiGianDong())) {

                msg = "Đã hết thời gian đăng ký lúc: "
                        + lhp.getThoiGianDong()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            throw new IllegalStateException(msg);
        }

        // check trạng thái
        if (lhp.getTrangThai() != TrangThaiLHP.MO) {
            throw new IllegalStateException("Lớp học phần đã đóng đăng ký");
        }

        // check còn chỗ
        if (!lhp.isConCho()) {
            throw new IllegalStateException(
                    "Lớp học phần đã đầy ("
                            + lhp.getSiSoMax()
                            + "/"
                            + lhp.getSiSoMax()
                            + " chỗ)"
            );
        }

        // check đã đăng ký chưa
        if (dangKyRepo.existsBySinhVienIdAndLopHocPhanId(svId, lhpId)) {
            throw new IllegalStateException(
                    "Sinh viên đã đăng ký lớp học phần này rồi"
            );
        }

        // check trùng lịch
        List<ThoiKhoaBieu> lichSV =
                tkbRepo.findBySinhVienAndHocKy(svId, lhp.getHocKy());

        List<ThoiKhoaBieu> lichLHP =
                tkbRepo.findByLopHocPhanId(lhpId);

        for (ThoiKhoaBieu svTkb : lichSV) {

            for (ThoiKhoaBieu newTkb : lichLHP) {

                if (svTkb.trungLich(newTkb)) {

                    throw new IllegalStateException(
                            "Trùng lịch học! "
                                    + svTkb.getTenThu()
                                    + " tiết "
                                    + svTkb.getTietBatDau()
                                    + " với lớp "
                                    + lhp.getMaLhp()
                    );
                }
            }
        }

        // save đăng ký
        DangKy dk = DangKy.builder()
                .sinhVien(sv)
                .lopHocPhan(lhp)
                .build();

        dangKyRepo.save(dk);

        // update sĩ số
        lhp.setSiSoHienTai(lhp.getSiSoHienTai() + 1);

        // update trạng thái
        capNhatTrangThai(lhp);

        lhpRepo.save(lhp);
    }

    /* =========================================================
       HUY DANG KY
    ========================================================= */

    @Override
    public void huyDangKy(Long svId, Long lhpId) {

        LopHocPhan lhp = lhpRepo.findById(lhpId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy lớp học phần"));

        if (!lhp.isDangTrongThoiGianDangKy()) {

            String msg = "Lớp học phần đã đóng đăng ký, không thể hủy";

            if (lhp.getThoiGianDong() != null
                    && LocalDateTime.now().isAfter(lhp.getThoiGianDong())) {

                msg = "Đã hết thời gian đăng ký lúc: "
                        + lhp.getThoiGianDong()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }

            throw new IllegalStateException(msg);
        }

        DangKy dk = dangKyRepo
                .findBySinhVienIdAndLopHocPhanId(svId, lhpId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy đăng ký"));

        if (dk.getDiemTongKet() != null) {
            throw new IllegalStateException(
                    "Không thể hủy đăng ký khi đã có điểm tổng kết"
            );
        }

        dangKyRepo.deleteById(dk.getId());

        // giảm sĩ số
        lhp.setSiSoHienTai(
                Math.max(0, lhp.getSiSoHienTai() - 1)
        );

        // update trạng thái
        capNhatTrangThai(lhp);

        lhpRepo.save(lhp);
    }

    /* =========================================================
       ADMIN HUY
    ========================================================= */

    public void huyDangKyAdmin(Long svId, Long lhpId) {

        DangKy dk = dangKyRepo
                .findBySinhVienIdAndLopHocPhanId(svId, lhpId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy đăng ký"));

        dangKyRepo.deleteById(dk.getId());

        LopHocPhan lhp = lhpRepo.findById(lhpId).orElse(null);

        if (lhp != null) {

            lhp.setSiSoHienTai(
                    Math.max(0, lhp.getSiSoHienTai() - 1)
            );

            capNhatTrangThai(lhp);

            lhpRepo.save(lhp);
        }
    }

    /* =========================================================
       DIEM
    ========================================================= */

    @Override
    public void capNhatDiem(Long dkId, Double diemQT, Double diemThi) {

        DangKy dk = dangKyRepo.findById(dkId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy đăng ký"));

        if (diemQT != null && (diemQT < 0 || diemQT > 10)) {
            throw new IllegalArgumentException(
                    "Điểm quá trình phải từ 0 đến 10"
            );
        }

        if (diemThi != null && (diemThi < 0 || diemThi > 10)) {
            throw new IllegalArgumentException(
                    "Điểm thi phải từ 0 đến 10"
            );
        }

        dk.setDiemQuaTrinh(diemQT);
        dk.setDiemThi(diemThi);

        dk.tinhDiemTongKet();

        dangKyRepo.save(dk);
    }

    /* =========================================================
       QUERY
    ========================================================= */

    @Override
    @Transactional(readOnly = true)
    public List<DangKy> getDanhSachDangKy(Long lhpId) {
        return dangKyRepo.findByLopHocPhanId(lhpId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DangKy> getDangKyCuaSinhVien(Long svId) {
        return dangKyRepo.findBySinhVienId(svId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllHocKy() {
        return lhpRepo.findAllHocKy();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return lhpRepo.count();
    }
}