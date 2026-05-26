package com.qldt.model.enums;

public enum VaiTro {
    ADMIN("Quản Trị Viên", "danger", "bi-shield-fill-check", "#c0392b"),
    NHAN_VIEN("Nhân Viên", "success", "bi-person-badge", "#27ae60"),
    SINH_VIEN("Sinh Viên", "info", "bi-mortarboard", "#1abc9c");

    private final String nhanHienThi;
    private final String mauSac;
    private final String iconClass;
    private final String mauNen;

    VaiTro(String nhanHienThi, String mauSac, String iconClass, String mauNen) {
        this.nhanHienThi = nhanHienThi;
        this.mauSac = mauSac;
        this.iconClass = iconClass;
        this.mauNen = mauNen;
    }

    public String getNhanHienThi() { return nhanHienThi; }
    public String getMauSac()      { return mauSac; }
    public String getIconClass()   { return iconClass; }
    public String getMauNen()      { return mauNen; }
}