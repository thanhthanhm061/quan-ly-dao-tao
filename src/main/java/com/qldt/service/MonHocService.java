package com.qldt.service;
import com.qldt.model.MonHoc;
import java.util.*;

public interface MonHocService {
    List<MonHoc> findAll();
    List<MonHoc> search(String keyword);
    Optional<MonHoc> findById(Long id);
    MonHoc save(MonHoc mon);
    void delete(Long id);
    long count();
}
