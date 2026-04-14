package com.qldt.repository;
import com.qldt.model.MonHoc;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MonHocRepository extends JpaRepository<MonHoc, Long> {
    Optional<MonHoc> findByMaMon(String maMon);
    boolean existsByMaMon(String maMon);
    List<MonHoc> findByTenMonContainingIgnoreCase(String tenMon);
    long countByKhoaId(Long khoaId);
}
