package com.qldt.service.impl;

import com.qldt.model.ChucVu;
import com.qldt.repository.ChucVuRepository;
import com.qldt.service.ChucVuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class ChucVuServiceImpl implements ChucVuService {

    private final ChucVuRepository repo;

    @Override @Transactional(readOnly = true)
    public List<ChucVu> findAll() { return repo.findAllByOrderByTenChucVuAsc(); }

    @Override @Transactional(readOnly = true)
    public List<ChucVu> findAllHoatDong() { return repo.findByTrangThai(true); }

    @Override @Transactional(readOnly = true)
    public List<ChucVu> findByKhoaId(Long khoaId) { return repo.findByKhoaId(khoaId); }

    @Override @Transactional(readOnly = true)
    public List<ChucVu> timKiem(String kw) {
        return kw == null || kw.isBlank() ? findAll() : repo.findByTenChucVuContainingIgnoreCase(kw);
    }

    @Override @Transactional(readOnly = true)
    public Optional<ChucVu> findById(Long id) { return repo.findById(id); }

    @Override
    public ChucVu save(ChucVu cv) {
        if (cv.getId() == null && repo.existsByMaChucVu(cv.getMaChucVu()))
            throw new IllegalArgumentException("Mã chức vụ '" + cv.getMaChucVu() + "' đã tồn tại");
        return repo.save(cv);
    }

    @Override
    public void delete(Long id) {
        ChucVu cv = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chức vụ"));
        if (cv.getNhanVienChucVus() != null && !cv.getNhanVienChucVus().isEmpty())
            throw new IllegalStateException("Không thể xóa! Chức vụ đang được " + cv.getNhanVienChucVus().size() + " nhân viên đảm nhiệm");
        repo.deleteById(id);
    }

    @Override
    public void doiTrangThai(Long id) {
        ChucVu cv = repo.findById(id).orElseThrow();
        cv.setTrangThai(!Boolean.TRUE.equals(cv.getTrangThai()));
        repo.save(cv);
    }

    @Override @Transactional(readOnly = true)
    public long count() { return repo.count(); }
}
