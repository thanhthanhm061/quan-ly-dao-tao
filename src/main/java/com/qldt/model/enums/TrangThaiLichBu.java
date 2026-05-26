package com.qldt.model.enums;

public enum TrangThaiLichBu {

    DA_XEP("Đã xếp"),
    HOAN_THANH("Hoàn thành"),
    HUY("Đã hủy");

    private final String tenHienThi;

    TrangThaiLichBu(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }
}