package com.qldt.repository;

import com.qldt.model.LichDayBu;
import com.qldt.model.enums.TrangThaiLichBu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LichDayBuRepository extends JpaRepository<LichDayBu, Long> {

    // Tất cả lịch bù của một lớp học phần
    List<LichDayBu> findByLopHocPhanIdOrderByNgayDayBuAsc(Long lhpId);

    // Lịch bù của một giảng viên
    List<LichDayBu> findByGiangVienIdOrderByNgayDayBuAsc(Long giangVienId);

    // Lịch bù liên quan đến đơn nghỉ
    List<LichDayBu> findByDonNghiLienQuanId(Long donNghiId);

    // Lịch bù theo trạng thái
    List<LichDayBu> findByTrangThaiOrderByNgayDayBuAsc(TrangThaiLichBu trangThai);

    // Lịch bù của giảng viên trong khoảng ngày
    @Query("""
            SELECT l FROM LichDayBu l
            WHERE l.giangVien.id = :gvId
              AND l.ngayDayBu BETWEEN :from AND :to
              AND l.trangThai <> com.qldt.model.enums.TrangThaiLichBu.HUY
            ORDER BY l.ngayDayBu ASC, l.tietBatDau ASC
            """)
    List<LichDayBu> findByGiangVienAndKhoangNgay(
            @Param("gvId") Long gvId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Kiểm tra trùng phòng trong cùng buổi
    @Query("""
            SELECT COUNT(l) > 0 FROM LichDayBu l
            WHERE l.phongHoc = :phong
              AND l.ngayDayBu = :ngay
              AND l.trangThai <> com.qldt.model.enums.TrangThaiLichBu.HUY
              AND l.tietBatDau < :tietKetThuc
              AND (l.tietBatDau + l.soTiet) > :tietBatDau
              AND (:excludeId IS NULL OR l.id <> :excludeId)
            """)
    boolean existsTrungPhong(
            @Param("phong") String phong,
            @Param("ngay") LocalDate ngay,
            @Param("tietBatDau") int tietBatDau,
            @Param("tietKetThuc") int tietKetThuc,
            @Param("excludeId") Long excludeId);

    // Kiểm tra giảng viên trùng lịch bù
    @Query("""
            SELECT COUNT(l) > 0 FROM LichDayBu l
            WHERE l.giangVien.id = :gvId
              AND l.ngayDayBu = :ngay
              AND l.trangThai <> com.qldt.model.enums.TrangThaiLichBu.HUY
              AND l.tietBatDau < :tietKetThuc
              AND (l.tietBatDau + l.soTiet) > :tietBatDau
              AND (:excludeId IS NULL OR l.id <> :excludeId)
            """)
    boolean existsTrungGiangVien(
            @Param("gvId") Long gvId,
            @Param("ngay") LocalDate ngay,
            @Param("tietBatDau") int tietBatDau,
            @Param("tietKetThuc") int tietKetThuc,
            @Param("excludeId") Long excludeId);

    // Lịch bù thuộc khoa (dùng cho TK/PTK dashboard)
    @Query("""
            SELECT l FROM LichDayBu l
            WHERE l.lopHocPhan.monHoc.khoa.id = :khoaId
            ORDER BY l.ngayDayBu DESC
            """)
    List<LichDayBu> findByKhoaId(@Param("khoaId") Long khoaId);
}