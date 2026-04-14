package com.qldt.service.impl;
import com.qldt.model.MonHoc;
import com.qldt.repository.MonHocRepository;
import com.qldt.service.MonHocService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class MonHocServiceImpl implements MonHocService {
    private final MonHocRepository repo;

    @Override @Transactional(readOnly = true)
    public List<MonHoc> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<MonHoc> search(String kw) {
        return kw == null || kw.isBlank() ? findAll() : repo.findByTenMonContainingIgnoreCase(kw);
    }

    @Override @Transactional(readOnly = true)
    public Optional<MonHoc> findById(Long id) { return repo.findById(id); }

    @Override
    public MonHoc save(MonHoc mon) {
        if (mon.getId() == null && repo.existsByMaMon(mon.getMaMon()))
            throw new IllegalArgumentException("Mã môn '" + mon.getMaMon() + "' đã tồn tại");
        return repo.save(mon);
    }

    @Override
    public void delete(Long id) {
        long count = repo.findById(id).map(m -> (long) m.getLopHocPhans().size()).orElse(0L);
        if (count > 0) throw new IllegalStateException("Không thể xóa! Môn học đang có " + count + " lớp học phần");
        repo.deleteById(id);
    }

    @Override @Transactional(readOnly = true)
    public long count() { return repo.count(); }
}
