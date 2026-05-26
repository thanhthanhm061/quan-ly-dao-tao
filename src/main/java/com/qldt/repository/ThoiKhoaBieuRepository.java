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
    // Đổi: JOIN FETCH l.giangVien → giờ là NhanVien, field vẫn tên giangVien
    // =========================

    @Query("""
    SELECT DISTINCT t
    FROM ThoiKhoaBieu t
    JOIN FETCH t.lopHocPhan l
    JOIN FETCH l.giangVien gv
    LEFT JOIN FETCH gv.chucVu
    JOIN FETCH l.monHoc
    WHERE gv.id = :nhanVienId
      AND l.hocKy = :hocKy
    ORDER BY t.thuTrongTuan, t.tietBatDau
""")
    List<ThoiKhoaBieu> findByGiangVienAndHocKy(
            @Param("nhanVienId") Long nhanVienId,
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
    // Đổi: gv.id → nhanVienId, giữ nguyên tuanBatDau/tuanKetThuc
    // =========================

    @Query("""
        SELECT DISTINCT t
        FROM ThoiKhoaBieu t
        JOIN FETCH t.lopHocPhan l
        JOIN FETCH l.giangVien gv
        JOIN FETCH l.monHoc
        WHERE gv.id = :nhanVienId
          AND l.hocKy = :hocKy
          AND t.tuanBatDau <= :ngayKetThuc
          AND t.tuanKetThuc >= :ngayBatDau
        ORDER BY t.thuTrongTuan, t.tietBatDau
    """)
    List<ThoiKhoaBieu> findByGiangVienAndTuan(
            @Param("nhanVienId") Long nhanVienId,
            @Param("hocKy") String hocKy,
            @Param("ngayBatDau") LocalDate ngayBatDau,
            @Param("ngayKetThuc") LocalDate ngayKetThuc
    );

    // =========================
    // LỊCH SINH VIÊN THEO TUẦN
    // =========================

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
    // ── Lịch GV theo tháng ──────────────────────────────────────────────────────
    @Query("""
    SELECT DISTINCT t
    FROM ThoiKhoaBieu t
    JOIN FETCH t.lopHocPhan l
    JOIN FETCH l.giangVien gv
    JOIN FETCH l.monHoc
    WHERE gv.id = :nhanVienId
      AND l.hocKy = :hocKy
      AND t.tuanBatDau <= :ngayKetThucThang
      AND t.tuanKetThuc >= :ngayBatDauThang
    ORDER BY t.thuTrongTuan, t.tietBatDau
""")
    List<ThoiKhoaBieu> findByGiangVienAndThang(
            @Param("nhanVienId") Long nhanVienId,
            @Param("hocKy") String hocKy,
            @Param("ngayBatDauThang") LocalDate ngayBatDauThang,
            @Param("ngayKetThucThang") LocalDate ngayKetThucThang
    );

    // ── Lịch SV theo tháng ──────────────────────────────────────────────────────
    @Query("""
    SELECT DISTINCT t
    FROM ThoiKhoaBieu t
    JOIN FETCH t.lopHocPhan l
    JOIN FETCH l.monHoc
    JOIN l.dangKys dk
    WHERE dk.sinhVien.id = :svId
      AND l.hocKy = :hocKy
      AND t.tuanBatDau <= :ngayKetThucThang
      AND t.tuanKetThuc >= :ngayBatDauThang
    ORDER BY t.thuTrongTuan, t.tietBatDau
""")
    List<ThoiKhoaBieu> findBySinhVienAndThang(
            @Param("svId") Long svId,
            @Param("hocKy") String hocKy,
            @Param("ngayBatDauThang") LocalDate ngayBatDauThang,
            @Param("ngayKetThucThang") LocalDate ngayKetThucThang
    );

    // ── Thống kê tín chỉ đã dạy theo GV ────────────────────────────────────────
    @Query("""
    SELECT COALESCE(SUM(l.monHoc.soTinChi), 0)
    FROM ThoiKhoaBieu t
    JOIN t.lopHocPhan l
    WHERE l.giangVien.id = :nhanVienId
      AND l.hocKy = :hocKy
""")
    Integer sumTinChiDaDay(
            @Param("nhanVienId") Long nhanVienId,
            @Param("hocKy") String hocKy
    );

    // ── Tổng tín chỉ được phân công theo GV ─────────────────────────────────────
    @Query("""
    SELECT COALESCE(SUM(l.monHoc.soTinChi), 0)
    FROM LopHocPhan l
    WHERE l.giangVien.id = :nhanVienId
      AND l.hocKy = :hocKy
""")
    Integer sumTinChiPhanCong(
            @Param("nhanVienId") Long nhanVienId,
            @Param("hocKy") String hocKy
    );

    // =========================
    // THỐNG KÊ TẢI GIẢNG DẠY
    // Đổi: l.giangVien → NhanVien, field hoTen vẫn tên hoTen
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
    // Giữ nguyên ph.maPhong (không đổi sang ph.id)
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
}