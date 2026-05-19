package com.qldt.repository;
import com.qldt.model.LopHocPhan;
import com.qldt.model.enums.TrangThaiLHP;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LopHocPhanRepository extends JpaRepository<LopHocPhan, Long> {
    Optional<LopHocPhan> findByMaLhp(String maLhp);
    boolean existsByMaLhp(String maLhp);
    List<LopHocPhan> findByHocKy(String hocKy);
    List<LopHocPhan> findByGiangVienId(Long giangVienId);
    List<LopHocPhan> findByMonHocId(Long monHocId);
    long countByMonHocId(Long monHocId);
    List<LopHocPhan> findByTrangThaiAndThoiGianMoBefore(TrangThaiLHP trangThai, LocalDateTime time);
    List<LopHocPhan> findByTrangThaiAndThoiGianDongBefore(TrangThaiLHP trangThai, LocalDateTime time);
    // Trong LopHocPhanRepository
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM LopHocPhan l WHERE l.id = :id")
    Optional<LopHocPhan> findByIdForUpdate(@Param("id") Long id);
    @Query("SELECT DISTINCT lhp.hocKy FROM LopHocPhan lhp ORDER BY lhp.hocKy DESC")
    List<String> findAllHocKy();
}
