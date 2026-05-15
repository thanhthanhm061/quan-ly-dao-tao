// PhongHocDTO.java
package com.qldt.service;

import com.qldt.model.PhongHoc;

public record PhongHocDTO(
        Long id,
        String maPhong,
        String tenPhong,
        int sucChua,
        String loaiPhong
) {
    public static PhongHocDTO from(PhongHoc p) {
        return new PhongHocDTO(
                p.getId(),
                p.getMaPhong(),
                p.getTenPhong(),
                p.getSucChua(),
                p.getLoaiPhong() != null ? p.getLoaiPhong().name() : null
        );
    }
}