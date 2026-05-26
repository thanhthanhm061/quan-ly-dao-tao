package com.qldt.repository;

import com.qldt.model.Khoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface KhoaRepository extends JpaRepository<Khoa, Long> {
    Optional<Khoa> findByMaKhoa(String maKhoa);
    boolean existsByMaKhoa(String maKhoa);
    List<Khoa> findByTrangThai(Boolean trangThai);
    List<Khoa> findAllByOrderByTenKhoaAsc();

    @Query("SELECT k FROM Khoa k LEFT JOIN FETCH k.nhanViens WHERE k.trangThai = true ORDER BY k.tenKhoa")
    List<Khoa> findAllHoatDongVoiNhanVien();
}
