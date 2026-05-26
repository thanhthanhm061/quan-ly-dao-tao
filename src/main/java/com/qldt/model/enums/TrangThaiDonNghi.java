package com.qldt.model.enums;

public enum TrangThaiDonNghi {

    CHO_DUYET("Chờ duyệt"),
    DA_DUYET("Đã duyệt"),
    TU_CHOI("Từ chối"),
    DA_HUY("Đã hủy");

    private final String tenHienThi;

    TrangThaiDonNghi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}