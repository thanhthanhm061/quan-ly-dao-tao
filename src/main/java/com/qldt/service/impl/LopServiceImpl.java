package com.qldt.service.impl;
import com.qldt.model.Lop;
import com.qldt.repository.LopRepository;
import com.qldt.service.LopService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class LopServiceImpl implements LopService {
    private final LopRepository repo;

    @Override @Transactional(readOnly = true)
    public List<Lop> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<Lop> findById(Long id) { return repo.findById(id); }

    @Override
    public Lop save(Lop lop) {
        if (lop.getId() == null && repo.existsByMaLop(lop.getMaLop()))
            throw new IllegalArgumentException("Mã lớp '" + lop.getMaLop() + "' đã tồn tại");
        return repo.save(lop);
    }

    @Override
    public void delete(Long id) {
        Lop lop = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp"));
        if (lop.getSinhViens() != null && !lop.getSinhViens().isEmpty())
            throw new IllegalStateException("Không thể xóa lớp đang có " + lop.getSinhViens().size() + " sinh viên");
        repo.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public long count() { return repo.count(); }
}
