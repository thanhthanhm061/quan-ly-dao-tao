package com.qldt.service.impl;

import com.qldt.model.PhongHoc;
import com.qldt.repository.PhongHocRepository;
import com.qldt.service.PhongHocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PhongHocServiceImpl implements PhongHocService {

    private final PhongHocRepository phongHocRepo;

    @Override
    @Transactional(readOnly = true)
    public List<PhongHoc> findAll() {
        return phongHocRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhongHoc> findAllHoatDong() {
        return phongHocRepo.findByHoatDongTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhongHoc> findById(Long id) {
        return phongHocRepo.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhongHoc> findByMaPhong(String maPhong) {
        return phongHocRepo.findByMaPhong(maPhong);
    }

    @Override
    public PhongHoc save(PhongHoc phongHoc) {
        // Kiểm tra trùng mã phòng khi thêm mới
        if (phongHoc.getId() == null
                && phongHocRepo.existsByMaPhong(phongHoc.getMaPhong())) {
            throw new IllegalArgumentException(
                    "Mã phòng '" + phongHoc.getMaPhong() + "' đã tồn tại");
        }
        return phongHocRepo.save(phongHoc);
    }

    @Override
    public void delete(Long id) {
        PhongHoc p = phongHocRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng học"));
        // Soft delete — chỉ đánh dấu không hoạt động
        p.setHoatDong(false);
        phongHocRepo.save(p);
    }
}