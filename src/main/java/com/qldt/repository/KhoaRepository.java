package com.qldt.repository;
import com.qldt.model.Khoa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KhoaRepository extends JpaRepository<Khoa, Long> {
    Optional<Khoa> findByMaKhoa(String maKhoa);
    boolean existsByMaKhoa(String maKhoa);
}
