package com.qldt.repository;

import com.qldt.model.NhanVienChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NhanVienChucVuRepository extends JpaRepository<NhanVienChucVu, Long> {
    List<NhanVienChucVu> findByNhanVienId(Long nhanVienId);
    List<NhanVienChucVu> findByChucVuId(Long chucVuId);

    // Tìm chức vụ đang đảm nhiệm (ngayKetThuc = null)
    List<NhanVienChucVu> findByNhanVienIdAndNgayKetThucIsNull(Long nhanVienId);

    // Tìm chức vụ chính đang đảm nhiệm
    Optional<NhanVienChucVu> findByNhanVienIdAndLaChucVuChinhTrueAndNgayKetThucIsNull(Long nhanVienId);

    // Lịch sử đã kết thúc
    List<NhanVienChucVu> findByNhanVienIdAndNgayKetThucIsNotNull(Long nhanVienId);

    @Query("SELECT nvcv FROM NhanVienChucVu nvcv WHERE nvcv.nhanVien.id = :nhanVienId ORDER BY nvcv.ngayBatDau DESC")
    List<NhanVienChucVu> findLichSuByNhanVien(@Param("nhanVienId") Long nhanVienId);
}
