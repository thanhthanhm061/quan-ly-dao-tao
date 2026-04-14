package com.qldt.repository;
import com.qldt.model.GiangVien;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GiangVienRepository extends JpaRepository<GiangVien, Long> {
    Optional<GiangVien> findByMaGv(String maGv);
    boolean existsByMaGv(String maGv);
    List<GiangVien> findByHoTenContainingIgnoreCase(String hoTen);
    List<GiangVien> findByKhoaId(Long khoaId);
    Optional<GiangVien> findByNguoiDungId(Long nguoiDungId);
}
