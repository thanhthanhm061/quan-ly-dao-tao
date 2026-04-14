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
    public List<Khoa> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public Optional<Khoa> findById(Long id) { return repo.findById(id); }

    @Override
    public Khoa save(Khoa khoa) { return repo.save(khoa); }
}
