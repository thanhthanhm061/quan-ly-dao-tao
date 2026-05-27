package com.qldt.service;

public record LichDayBuApiDTO(
        String ngayDayBu,
        String phongHoc,
        Integer tietBatDau,
        Integer tietKetThuc,
        String ghiChu
) {}