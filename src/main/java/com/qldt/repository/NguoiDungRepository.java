package com.qldt.repository;

import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<NguoiDung> findByVaiTro(VaiTro vaiTro);
}
