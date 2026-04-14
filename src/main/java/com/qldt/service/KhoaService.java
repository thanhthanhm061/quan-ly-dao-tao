package com.qldt.service;
import com.qldt.model.Khoa;
import java.util.*;

public interface KhoaService {
    List<Khoa> findAll();
    Optional<Khoa> findById(Long id);
    Khoa save(Khoa khoa);
}
