package com.qldt.model;

import com.qldt.model.enums.TrangThaiDonNghi;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "don_nghi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"nguoiNop", "nguoiDuyet"})
public class DonNghi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ── Người nộp đơn (giảng viên / nhân viên) ──────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_nop_id", nullable = false)
    private NhanVien nguoiNop;

    // ── Thời gian nghỉ ───────────────────────────────────────────────────────
    @Column(name = "ngay_bat_dau", nullable = false)
    @NotNull(message = "Ngày bắt đầu không được trống")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    @NotNull(message = "Ngày kết thúc không được trống")
    private LocalDate ngayKetThuc;

    // ── Lý do & loại nghỉ ───────────────────────────────────────────────────
    @Column(name = "ly_do", nullable = false, length = 500)
    @NotBlank(message = "Lý do không được trống")
    @Size(max = 500, message = "Lý do không quá 500 ký tự")
    private String lyDo;

    @Column(name = "loai_nghi", length = 50)
    private String loaiNghi; // "Nghỉ phép", "Nghỉ bệnh", "Nghỉ không lương", ...

    // ── Trạng thái & duyệt ──────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    @Builder.Default
    private TrangThaiDonNghi trangThai = TrangThaiDonNghi.CHO_DUYET;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_duyet_id")
    private NhanVien nguoiDuyet; // null nếu chưa duyệt

    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @Column(name = "ly_do_tu_choi", length = 300)
    private String lyDoTuChoi; // lý do từ chối (nếu có)

    // ── File đính kèm ────────────────────────────────────────────────────────
    @Column(name = "file_dinh_kem", length = 255)
    private String fileDinhKem; // tên file hoặc URL

    // ── Audit ────────────────────────────────────────────────────────────────
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

    // ── Helper ───────────────────────────────────────────────────────────────

    /** Số ngày nghỉ (inclusive) */
    public long getSoNgayNghi() {
        if (ngayBatDau == null || ngayKetThuc == null) return 0;
        return ngayBatDau.datesUntil(ngayKetThuc.plusDays(1)).count();
    }

    /** Đơn có thể hủy không (chỉ khi còn CHO_DUYET) */
    public boolean isCoTheHuy() {
        return trangThai == TrangThaiDonNghi.CHO_DUYET;
    }

    /** Đơn đã được duyệt */
    public boolean isDaDuyet() {
        return trangThai == TrangThaiDonNghi.DA_DUYET;
    }
}