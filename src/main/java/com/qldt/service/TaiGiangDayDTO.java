package com.qldt.service;

// DTO thống kê
public record TaiGiangDayDTO(
        Long giangVienId,
        String hoTen,
        int tongTiet,
        int soLop,
        int tongTinChi,
        double tyLeTai // tongTiet / gioi han (ví dụ 270 tiết/kỳ)
) {
    public static final int TIET_TOI_DA_KY = 270; // 18 tín × 15 tiết

    public String getMucTai() {
        if (tyLeTai < 0.7) return "Nhẹ";
        if (tyLeTai < 0.9) return "Trung bình";
        if (tyLeTai <= 1.0) return "Đầy";
        return "Quá tải";
    }

    public String getMauTai() {
        return switch (getMucTai()) {
            case "Nhẹ" -> "success";
            case "Trung bình" -> "warning";
            case "Đầy" -> "danger";
            default -> "dark";
        };
    }
}