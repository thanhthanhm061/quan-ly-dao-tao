package com.qldt.repository;
import com.qldt.model.LopHocPhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface LopHocPhanRepository extends JpaRepository<LopHocPhan, Long> {
    Optional<LopHocPhan> findByMaLhp(String maLhp);
    boolean existsByMaLhp(String maLhp);
    List<LopHocPhan> findByHocKy(String hocKy);
    List<LopHocPhan> findByGiangVienId(Long giangVienId);
    List<LopHocPhan> findByMonHocId(Long monHocId);
    long countByMonHocId(Long monHocId);

    @Query("SELECT DISTINCT lhp.hocKy FROM LopHocPhan lhp ORDER BY lhp.hocKy DESC")
    List<String> findAllHocKy();
}
