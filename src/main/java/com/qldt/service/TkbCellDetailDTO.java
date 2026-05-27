package com.qldt.service;

public record TkbCellDetailDTO(
        Long   tkbId,
        String tenMon,
        String maMon,
        int    soTinChi,
        String tenGiangVien,
        Long   giangVienId,
        String maLhp,
        Long   lhpId,
        String tenThu,
        int    thuSo,
        int    tietBatDau,
        int    tietKetThuc,
        int    soTiet,
        String phongHoc,
        String tuanBatDau,
        String tuanKetThuc,
        int    siSoHienTai,
        int    siSoMax
) {}