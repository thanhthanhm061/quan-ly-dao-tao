package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "nhan_vien_chuc_vu")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NhanVienChucVu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "nhan_vien_id", nullable = false)
    private NhanVien nhanVien;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chuc_vu_id", nullable = false)
    private ChucVu chucVu;

    @Column(name = "mo_ta", length = 255)
    private String moTa; // Ghi chú thêm cho vị trí này

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ngay_bat_dau", nullable = false)
    @NotNull(message = "Ngày bắt đầu không được trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayKetThuc; // null = đang đảm nhiệm

    @Column(name = "la_chinh")
    @Builder.Default
    private Boolean laChucVuChinh = false; // Đây có phải chức vụ chính hiện tại không

    public boolean getDangDamNhiem() {
        return ngayKetThuc == null;
    }

    public String getTrangThaiText() {
        return ngayKetThuc == null ? "Đang đảm nhiệm" : "Đã kết thúc";
    }
}
