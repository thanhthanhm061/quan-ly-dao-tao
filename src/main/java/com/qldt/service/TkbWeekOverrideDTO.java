package com.qldt.service;

import java.time.LocalDate;

public record TkbWeekOverrideDTO(
        Integer   thuTrongTuan,
        Integer   tietBatDau,
        Integer   soTiet,
        String    phongHoc,
        LocalDate ngayTrongTuan   // ngày bất kỳ trong tuần cần sửa (từ data-ngay-iso)
) {}
