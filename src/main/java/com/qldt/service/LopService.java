package com.qldt.service;
import com.qldt.model.Lop;
import java.util.*;

public interface LopService {

    List<Lop> findAll();
    Optional<Lop> findById(Long id);
    Lop save(Lop lop);
    void delete(Long id);
    long count();
}
