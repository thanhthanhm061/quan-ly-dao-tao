package com.qldt.service;

public record CalendarEventDTO(
        // Chung
        String loai,           // "CHINH" | "BU" | "NGHI"
        String tenMon,
        String maLhp,
        Long   lhpId,
        String hoTenGv,
        Long   gvId,
        int    soTinChi,

        // Lịch chính & bù
        Integer thuTrongTuan,
        Integer tietBatDau,
        Integer soTiet,
        String  phongHoc,
        String  gioHoc,
        String  tenThu,

        // Lịch chính: khoảng ngày lặp lại
        String tuanBatDau,
        String tuanKetThuc,

        // Lịch bù: ngày cụ thể
        String ngayDayBu,      // ISO: yyyy-MM-dd
        String ngayNghiGoc,    // ISO: yyyy-MM-dd

        // Thông báo nghỉ
        String ngayNghiBatDau, // ISO: yyyy-MM-dd
        String ngayNghiKetThuc,// ISO: yyyy-MM-dd
        String lyDo,
        String loaiNghi
) {}
