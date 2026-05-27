package com.qldt.service;

public record TinChiConLaiDTO(
        int tongTinChi,          // Tổng TC các môn trong kỳ
        int tongTietDaXep,       // Tổng tiết đã xếp lịch
        int tongTietCanDay,      // Tổng tiết cần dạy (tongTinChi × 15)
        int tietConLai,          // Tiết chưa xếp
        int tcConLai,            // TC quy đổi còn lại (ceil)
        int phanTramHoanThanh    // % tiến độ (0-100)
) {}