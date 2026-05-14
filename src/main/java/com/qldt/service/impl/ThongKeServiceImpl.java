package com.qldt.service.impl;

import com.qldt.repository.SinhVienRepository;
import com.qldt.service.ThongKeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongKeServiceImpl implements ThongKeService {

    private final SinhVienRepository sinhVienRepository;

    public ThongKeServiceImpl(SinhVienRepository sinhVienRepository) {
        this.sinhVienRepository = sinhVienRepository;
    }

    @Override
    public List<Object[]> thongKeSinhVienTheoKhoa() {
        return sinhVienRepository.thongKeSinhVienTheoKhoa();
    }
}
