package com.qldt.service;

import java.time.LocalDate;

public record TkbQuickUpdateDTO(
        Integer thuTrongTuan,
        Integer tietBatDau,
        Integer soTiet,
        String  phongHoc,
        LocalDate tuanBatDau,
        LocalDate tuanKetThuc
) {}
