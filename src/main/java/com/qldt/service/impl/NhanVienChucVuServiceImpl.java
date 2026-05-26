package com.qldt.service.impl;

import com.qldt.model.NhanVienChucVu;
import com.qldt.repository.NhanVienChucVuRepository;
import com.qldt.service.NhanVienChucVuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class NhanVienChucVuServiceImpl implements NhanVienChucVuService {

    private final NhanVienChucVuRepository repo;


    @Override @Transactional(readOnly = true)
     public List<NhanVienChucVu> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<NhanVienChucVu> findByNhanVienId(Long nhanVienId) {
        return repo.findByNhanVienId(nhanVienId);
    }

    @Override @Transactional(readOnly = true)
    public List<NhanVienChucVu> getLichSu(Long nhanVienId) {
        return repo.findLichSuByNhanVien(nhanVienId);
    }

    @Override @Transactional(readOnly = true)
    public Optional<NhanVienChucVu> findById(Long id) { return repo.findById(id); }

    @Override
    public NhanVienChucVu save(NhanVienChucVu record) {
        // Nếu đánh dấu là chức vụ chính, bỏ đánh dấu các cái cũ
        if (Boolean.TRUE.equals(record.getLaChucVuChinh())) {
            repo.findByNhanVienIdAndLaChucVuChinhTrueAndNgayKetThucIsNull(record.getNhanVien().getId())
                    .ifPresent(old -> {
                        old.setLaChucVuChinh(false);
                        repo.save(old);
                    });
        }
        return repo.save(record);
    }

    @Override
    public void ketThucChucVu(Long id) {
        NhanVienChucVu record = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi"));
        record.setNgayKetThuc(LocalDate.now());
        record.setLaChucVuChinh(false);
        repo.save(record);
    }

    @Override
    public void delete(Long id) { repo.deleteById(id); }
}
