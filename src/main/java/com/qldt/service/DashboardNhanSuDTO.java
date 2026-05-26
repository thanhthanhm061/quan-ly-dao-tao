package com.qldt.service;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class DashboardNhanSuDTO {

    // Thẻ thống kê chính
    private long tongNhanVien;
    private long dangLamViec;
    private long daNghi;
    private long soKhoa;
    private long soKhoaHoatDong;
    private long soTienSi;           // TS + PGS.TS + GS.TS
    private long soHetHanHopDong;    // hết hạn trong 90 ngày
    private long moiThangNay;        // tuyển dụng tháng này
    private long nghiThangNay;       // nghỉ việc tháng này

    // Tính toán
    private String tiLeHoatDong;     // vd: "93.5"
    private String tiLeTienSi;       // vd: "36"

    // Học vị
    private long soGSSTS;     // GS + PGS
    private long soTienSiThu; // TS thuần
    private long soThacSi;
    private long soCuNhan;

    // Loại hợp đồng
    private List<HopDongItem> loaiHopDong;

    // Nhân viên theo khoa
    private List<KhoaItem> nhanVienTheoKhoa;

    // DS nhân viên mới (top 5)
    // DS sắp hết hạn HĐ (top 5)
    // DS lịch sử chức vụ gần đây (top 5)
    // → Các list này được add trực tiếp vào Model ở Controller

    @Data @Builder
    public static class HopDongItem {
        private String ten;
        private long soLuong;
        private String tiLe;
        private String mauSac; // hex color
    }

    @Data @Builder
    public static class KhoaItem {
        private String tenKhoa;
        private long soNhanVien;
        private long soGiangVien;
        private long soNhanVienHanhChinh;
        private String tiLePhan; // phần trăm so với max, dùng cho progress bar
    }
}