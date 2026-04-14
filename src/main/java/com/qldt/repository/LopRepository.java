package com.qldt.repository;
import com.qldt.model.Lop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LopRepository extends JpaRepository<Lop, Long> {
    Optional<Lop> findByMaLop(String maLop);
    boolean existsByMaLop(String maLop);
    List<Lop> findByKhoaId(Long khoaId);
}
