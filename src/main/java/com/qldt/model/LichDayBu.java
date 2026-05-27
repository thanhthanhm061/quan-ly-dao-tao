package com.qldt.model;

import com.qldt.model.enums.TrangThaiLichBu;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_day_bu")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"lopHocPhan", "giangVien", "nguoiXep", "donNghiLienQuan"})
public class LichDayBu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ── Lớp học phần liên quan ───────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lhp_id", nullable = false)
    private LopHocPhan lopHocPhan;

    // ── Giảng viên dạy bù ────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giang_vien_id", nullable = false)
    private NhanVien giangVien;

    // ── Đơn nghỉ liên quan (nullable — có thể xếp bù không từ đơn nghỉ) ──────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_nghi_id")
    private DonNghi donNghiLienQuan;

    // ── Buổi học gốc bị nghỉ ─────────────────────────────────────────────────
    @Column(name = "ngay_nghi_goc", nullable = false)
    @NotNull(message = "Ngày nghỉ gốc không được trống")
    private LocalDate ngayNghiGoc;

    // ── Lịch bù mới ──────────────────────────────────────────────────────────
    @Column(name = "ngay_day_bu", nullable = false)
    @NotNull(message = "Ngày dạy bù không được trống")
    private LocalDate ngayDayBu;

    @Column(name = "thu_trong_tuan")
    @Min(2) @Max(7)
    private int thuTrongTuan; // 2=Thứ 2 ... 7=Thứ 7

    @Column(name = "tiet_bat_dau")
    @Min(1) @Max(12)
    private int tietBatDau;

    @Column(name = "so_tiet")
    @Min(1)
    @Builder.Default
    private int soTiet = 3;

    @Column(name = "phong_hoc", length = 20)
    private String phongHoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phong_hoc_id")
    private PhongHoc phongHocRef; // ưu tiên hơn phongHoc string

    // ── Ghi chú ───────────────────────────────────────────────────────────────
    @Column(name = "ghi_chu", length = 300)
    private String ghiChu;

    // ── Trạng thái ────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private TrangThaiLichBu trangThai = TrangThaiLichBu.DA_XEP;

    // ── Người xếp lịch (Admin hoặc Trưởng khoa/bộ môn) ──────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xep_id" ,  nullable = true)
    private NhanVien nguoiXep;

    // ── Audit ─────────────────────────────────────────────────────────────────
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    public void prePersist() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    public String getTenThu() {
        return switch (thuTrongTuan) {
            case 2 -> "Thứ 2"; case 3 -> "Thứ 3"; case 4 -> "Thứ 4";
            case 5 -> "Thứ 5"; case 6 -> "Thứ 6"; case 7 -> "Thứ 7";
            default -> "CN";
        };
    }

    /** Tiết kết thúc */
    public int getTietKetThuc() {
        return tietBatDau + soTiet - 1;
    }

    /** Kiểm tra trùng lịch với một lịch bù khác */
    public boolean trungLich(LichDayBu other) {
        if (!this.ngayDayBu.equals(other.ngayDayBu)) return false;
        int end1 = this.tietBatDau + this.soTiet;
        int end2 = other.tietBatDau + other.soTiet;
        return this.tietBatDau < end2 && other.tietBatDau < end1;
    }
}