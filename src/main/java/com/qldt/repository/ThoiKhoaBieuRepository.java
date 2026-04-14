package com.qldt.repository;
import com.qldt.model.ThoiKhoaBieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ThoiKhoaBieuRepository extends JpaRepository<ThoiKhoaBieu, Long> {
    List<ThoiKhoaBieu> findByLopHocPhanId(Long lhpId);

    @Query("SELECT t FROM ThoiKhoaBieu t JOIN t.lopHocPhan l WHERE l.giangVien.id = :gvId AND l.hocKy = :hocKy ORDER BY t.thuTrongTuan, t.tietBatDau")
    List<ThoiKhoaBieu> findByGiangVienAndHocKy(@Param("gvId") Long gvId, @Param("hocKy") String hocKy);

    @Query("SELECT t FROM ThoiKhoaBieu t JOIN t.lopHocPhan l JOIN l.dangKys dk WHERE dk.sinhVien.id = :svId AND l.hocKy = :hocKy ORDER BY t.thuTrongTuan, t.tietBatDau")
    List<ThoiKhoaBieu> findBySinhVienAndHocKy(@Param("svId") Long svId, @Param("hocKy") String hocKy);
}
