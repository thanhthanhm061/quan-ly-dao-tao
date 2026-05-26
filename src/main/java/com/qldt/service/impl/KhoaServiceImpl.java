package com.qldt.service.impl;

import com.qldt.model.Khoa;
import com.qldt.repository.KhoaRepository;
import com.qldt.service.KhoaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class KhoaServiceImpl implements KhoaService {

    private final KhoaRepository repo;

    @Override @Transactional(readOnly = true)
    public List<Khoa> findAll() { return repo.findAllByOrderByTenKhoaAsc(); }

    @Override @Transactional(readOnly = true)
    public List<Khoa> findAllHoatDong() { return repo.findByTrangThai(true); }

    @Override @Transactional(readOnly = true)
    public Optional<Khoa> findById(Long id) { return repo.findById(id); }

    @Override
    public Khoa save(Khoa khoa) {
        if (khoa.getId() == null && repo.existsByMaKhoa(khoa.getMaKhoa()))
            throw new IllegalArgumentException("Mã khoa '" + khoa.getMaKhoa() + "' đã tồn tại");
        return repo.save(khoa);
    }

    @Override
    public void delete(Long id) {
        Khoa khoa = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoa"));
        if (khoa.getNhanViens() != null && !khoa.getNhanViens().isEmpty())
            throw new IllegalStateException("Không thể xóa! Khoa đang có " + khoa.getNhanViens().size() + " nhân viên");
        repo.deleteById(id);
    }

    @Override
    public void doiTrangThai(Long id) {
        Khoa khoa = repo.findById(id).orElseThrow();
        khoa.setTrangThai(!Boolean.TRUE.equals(khoa.getTrangThai()));
        repo.save(khoa);
    }

    @Override @Transactional(readOnly = true)
    public long count() { return repo.count(); }

    @Override @Transactional(readOnly = true)
    public boolean existsByMaKhoa(String maKhoa) { return repo.existsByMaKhoa(maKhoa); }
}
