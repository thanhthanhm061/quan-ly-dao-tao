package com.qldt.repository;

import com.qldt.model.PhongHoc;
import com.qldt.model.ThoiKhoaBieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ThoiKhoaBieuRepository
        extends JpaRepository<ThoiKhoaBieu, Long> {

    List<ThoiKhoaBieu> findByLopHocPhanId(Long lhpId);

    // =========================
    // LỊCH GIẢNG VIÊN
    // =========================

    @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.giangVien
        JOIN FETCH l.monHoc
        WHERE l.giangVien.id = :gvId
          AND l.hocKy = :hocKy
        ORDER BY t.thuTrongTuan, t.tietBatDau
    """)
    List<ThoiKhoaBieu> findByGiangVienAndHocKy(
            @Param("gvId") Long gvId,
            @Param("hocKy") String hocKy
    );

    // =========================
    // LỊCH THEO PHÒNG
    // =========================

    @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.monHoc
        WHERE t.phongHoc = :phongHoc
          AND l.hocKy = :hocKy
    """)
    List<ThoiKhoaBieu> findByPhongHocAndHocKy(
            @Param("phongHoc") String phongHoc,
            @Param("hocKy") String hocKy
    );

    // =========================
    // LỊCH SINH VIÊN
    // =========================

    @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.monHoc
        JOIN l.dangKys dk
        WHERE dk.sinhVien.id = :svId
          AND l.hocKy = :hocKy
        ORDER BY t.thuTrongTuan, t.tietBatDau
    """)
    List<ThoiKhoaBieu> findBySinhVienAndHocKy(
            @Param("svId") Long svId,
            @Param("hocKy") String hocKy
    );

    // =========================
    // LỊCH GIẢNG VIÊN THEO TUẦN
    // =========================

    @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.giangVien gv
        JOIN FETCH l.monHoc
        WHERE gv.id = :gvId
          AND l.hocKy = :hocKy
          AND t.tuanBatDau <= :ngayKetThuc
          AND t.tuanKetThuc >= :ngayBatDau
        ORDER BY t.thuTrongTuan, t.tietBatDau
    """)
    List<ThoiKhoaBieu> findByGiangVienAndTuan(
            @Param("gvId") Long gvId,
            @Param("hocKy") String hocKy,
            @Param("ngayBatDau") LocalDate ngayBatDau,
            @Param("ngayKetThuc") LocalDate ngayKetThuc
    );

    // =========================
    // THỐNG KÊ TẢI GIẢNG DẠY
    // =========================

    @Query("""
        SELECT l.giangVien.id,
               l.giangVien.hoTen,
               SUM(t.soTiet) as tongTiet,
               COUNT(DISTINCT l.id) as soLop,
               SUM(DISTINCT l.monHoc.soTinChi) as tongTinChi
        FROM ThoiKhoaBieu t
        JOIN t.lopHocPhan l
        WHERE l.hocKy = :hocKy
        GROUP BY l.giangVien.id, l.giangVien.hoTen
        ORDER BY tongTiet DESC
    """)
    List<Object[]> thongKeTaiGiangDay(
            @Param("hocKy") String hocKy
    );

    // =========================
    // PHÒNG ĐANG BẬN
    // =========================

    @Query("""
        SELECT t
        FROM ThoiKhoaBieu t
        JOIN t.lopHocPhan l
        WHERE t.phongHoc = :phong
          AND l.hocKy = :hocKy
          AND t.thuTrongTuan = :thu
          AND t.tietBatDau < :tietKetThuc
          AND (t.tietBatDau + t.soTiet) > :tietBatDau
    """)
    List<ThoiKhoaBieu> findPhongBan(
            @Param("phong") String phong,
            @Param("hocKy") String hocKy,
            @Param("thu") int thu,
            @Param("tietBatDau") int tietBatDau,
            @Param("tietKetThuc") int tietKetThuc
    );

    // =========================
    // TÌM PHÒNG TRỐNG
    // =========================

    @Query("""
        SELECT ph
        FROM PhongHoc ph
        WHERE ph.hoatDong = true
          AND ph.maPhong NOT IN (

              SELECT t.phongHoc
              FROM ThoiKhoaBieu t
              JOIN t.lopHocPhan l

              WHERE l.hocKy = :hocKy
                AND t.phongHoc IS NOT NULL
                AND t.thuTrongTuan = :thu
                AND t.tietBatDau < :tietKetThuc
                AND (t.tietBatDau + t.soTiet) > :tietBatDau
          )

          AND (
                :sucCanThiet = 0
                OR ph.sucChua >= :sucCanThiet
          )

        ORDER BY ph.maPhong
    """)
    List<PhongHoc> findPhongTrong(
            @Param("hocKy") String hocKy,
            @Param("thu") int thu,
            @Param("tietBatDau") int tietBatDau,
            @Param("tietKetThuc") int tietKetThuc,
            @Param("sucCanThiet") int sucCanThiet
    );

        // =========================
        // LỊCH SINH VIÊN THEO TUẦN
        @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.monHoc
        JOIN l.dangKys dk
        WHERE dk.sinhVien.id = :svId
          AND l.hocKy = :hocKy
          AND t.tuanBatDau <= :ngayKetThuc
          AND t.tuanKetThuc >= :ngayBatDau
        ORDER BY t.thuTrongTuan, t.tietBatDau
        """)
        List<ThoiKhoaBieu> findBySinhVienAndTuan(
                @Param("svId") Long svId,
                @Param("hocKy") String hocKy,
                @Param("ngayBatDau") LocalDate ngayBatDau,
                @Param("ngayKetThuc") LocalDate ngayKetThuc
        );
}