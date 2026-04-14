package com.qldt.repository;
import com.qldt.model.DangKy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DangKyRepository extends JpaRepository<DangKy, Long> {
    Optional<DangKy> findBySinhVienIdAndLopHocPhanId(Long svId, Long lhpId);
    List<DangKy> findBySinhVienId(Long svId);
    List<DangKy> findByLopHocPhanId(Long lhpId);
    boolean existsBySinhVienIdAndLopHocPhanId(Long svId, Long lhpId);
    long countByLopHocPhanId(Long lhpId);
}
