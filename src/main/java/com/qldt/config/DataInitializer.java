package com.qldt.config;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final NguoiDungRepository nguoiDungRepo;
    private final KhoaRepository khoaRepo;
    private final LopRepository lopRepo;
    private final GiangVienRepository giangVienRepo;
    private final SinhVienRepository sinhVienRepo;
    private final MonHocRepository monHocRepo;
    private final LopHocPhanRepository lhpRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (nguoiDungRepo.count() > 0) {
            log.info("Dữ liệu đã tồn tại, bỏ qua khởi tạo.");
            return;
        }

        log.info("Khởi tạo dữ liệu mẫu...");
        String defaultPass = passwordEncoder.encode("Admin@123");

        // ===== TÀI KHOẢN ADMIN =====
        NguoiDung admin = NguoiDung.builder()
            .username("admin").matKhau(defaultPass)
            .hoTen("Quản Trị Viên").email("admin@qldt.edu.vn")
            .vaiTro(VaiTro.ADMIN).kichHoat(true).build();
        nguoiDungRepo.save(admin);

        // ===== KHOA =====
        Khoa khoaCNTT = khoaRepo.save(Khoa.builder().maKhoa("CNTT").tenKhoa("Khoa Công Nghệ Thông Tin").build());
        Khoa khoaKTKT = khoaRepo.save(Khoa.builder().maKhoa("KTKT").tenKhoa("Khoa Kỹ Thuật - Kinh Tế").build());

        // ===== LỚP =====
        Lop lop01 = lopRepo.save(Lop.builder().maLop("CNTT01").tenLop("CNTT Khóa 2022 - Lớp 1").nienKhoa(2022).khoa(khoaCNTT).build());
        Lop lop02 = lopRepo.save(Lop.builder().maLop("CNTT02").tenLop("CNTT Khóa 2022 - Lớp 2").nienKhoa(2022).khoa(khoaCNTT).build());

        // ===== GIẢNG VIÊN =====
        NguoiDung ndGv1 = nguoiDungRepo.save(NguoiDung.builder()
            .username("gv001").matKhau(defaultPass).hoTen("Nguyễn Văn An")
            .email("nguyenvanan@qldt.edu.vn").vaiTro(VaiTro.GIANG_VIEN).kichHoat(true).build());
        GiangVien gv1 = giangVienRepo.save(GiangVien.builder()
            .maGv("GV001").hoTen("Nguyễn Văn An").hocVi("TS").chuyenNganh("Lập trình Java")
            .email("nguyenvanan@qldt.edu.vn").khoa(khoaCNTT).nguoiDung(ndGv1).build());

        NguoiDung ndGv2 = nguoiDungRepo.save(NguoiDung.builder()
            .username("gv002").matKhau(defaultPass).hoTen("Trần Thị Bình")
            .email("tranthibinh@qldt.edu.vn").vaiTro(VaiTro.GIANG_VIEN).kichHoat(true).build());
        GiangVien gv2 = giangVienRepo.save(GiangVien.builder()
            .maGv("GV002").hoTen("Trần Thị Bình").hocVi("ThS").chuyenNganh("Cơ sở dữ liệu")
            .email("tranthibinh@qldt.edu.vn").khoa(khoaCNTT).nguoiDung(ndGv2).build());

        // ===== SINH VIÊN =====
        NguoiDung ndSv1 = nguoiDungRepo.save(NguoiDung.builder()
            .username("sv001").matKhau(defaultPass).hoTen("Lê Văn Cường")
            .email("levancuong@sv.edu.vn").vaiTro(VaiTro.SINH_VIEN).kichHoat(true).build());
        sinhVienRepo.save(SinhVien.builder()
            .maSv("SV001").hoTen("Lê Văn Cường").gioiTinh(GioiTinh.Nam)
            .email("levancuong@sv.edu.vn").lop(lop01).nguoiDung(ndSv1).build());

        NguoiDung ndSv2 = nguoiDungRepo.save(NguoiDung.builder()
            .username("sv002").matKhau(defaultPass).hoTen("Phạm Thị Dung")
            .email("phamthidung@sv.edu.vn").vaiTro(VaiTro.SINH_VIEN).kichHoat(true).build());
        sinhVienRepo.save(SinhVien.builder()
            .maSv("SV002").hoTen("Phạm Thị Dung").gioiTinh(GioiTinh.Nu)
            .email("phamthidung@sv.edu.vn").lop(lop01).nguoiDung(ndSv2).build());

        // ===== MÔN HỌC =====
        MonHoc java = monHocRepo.save(MonHoc.builder().maMon("LTJAVA").tenMon("Lập Trình Java").soTinChi(3).loaiMon(LoaiMon.LY_THUYET).khoa(khoaCNTT).build());
        MonHoc csdl = monHocRepo.save(MonHoc.builder().maMon("CSDL").tenMon("Cơ Sở Dữ Liệu").soTinChi(3).loaiMon(LoaiMon.LY_THUYET).khoa(khoaCNTT).build());
        monHocRepo.save(MonHoc.builder().maMon("CTDL").tenMon("Cấu Trúc Dữ Liệu").soTinChi(3).loaiMon(LoaiMon.LY_THUYET).khoa(khoaCNTT).build());
        monHocRepo.save(MonHoc.builder().maMon("MANG").tenMon("Mạng Máy Tính").soTinChi(3).loaiMon(LoaiMon.LY_THUYET).khoa(khoaCNTT).build());

        // ===== LỚP HỌC PHẦN =====
        lhpRepo.save(LopHocPhan.builder().maLhp("LTJAVA-HK1-24-01").monHoc(java).giangVien(gv1).hocKy("HK1-2024").siSoMax(40).build());
        lhpRepo.save(LopHocPhan.builder().maLhp("CSDL-HK1-24-01").monHoc(csdl).giangVien(gv2).hocKy("HK1-2024").siSoMax(40).build());

        log.info("✅ Khởi tạo dữ liệu mẫu thành công!");
        log.info("Tài khoản: admin/Admin@123 | gv001/Admin@123 | sv001/Admin@123");
    }
}
