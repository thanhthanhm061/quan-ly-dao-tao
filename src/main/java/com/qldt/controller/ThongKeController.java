package com.qldt.controller;


import com.qldt.service.ThongKeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/thong-ke")
public class ThongKeController {

    private final ThongKeService thongKeService;

    public ThongKeController(ThongKeService thongKeService) {
        this.thongKeService = thongKeService;
    }

    @GetMapping("/sinh-vien-theo-khoa")
    public List<Object[]> thongKeSinhVienTheoKhoa() {
        return thongKeService.thongKeSinhVienTheoKhoa();
    }

}