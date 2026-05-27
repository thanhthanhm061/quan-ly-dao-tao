package com.qldt.model;

import com.qldt.model.enums.VaiTro;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NguoiDung {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @NotBlank(message = "Tên đăng nhập không được trống")
    @Pattern(regexp = "^[a-z0-9_]{3,50}$",
            message = "Tên đăng nhập chỉ gồm chữ thường, số, dấu _ (3-50 ký tự)")
    private String username;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "ho_ten", nullable = false, length = 100)
    @NotBlank(message = "Họ tên không được trống")
    private String hoTen;

    @Column(unique = true, length = 100)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "anh_dai_dien", length = 255)
    private String anhDaiDien; // URL hoặc tên file ảnh

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "kich_hoat", columnDefinition = "BOOLEAN DEFAULT TRUE")
    @Builder.Default
    private boolean kichHoat = true;

    @Column(name = "so_lan_dang_nhap_that_bai", columnDefinition = "INT DEFAULT 0")
    @Builder.Default
    private Integer soLanDangNhapThatBai = 0;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "lan_dang_nhap_cuoi")
    private LocalDateTime lanDangNhapCuoi;

    @Column(name = "bi_khoa_den")
    private LocalDateTime biKhoaDen; // null = không bị khóa

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @OneToOne(mappedBy = "nguoiDung", cascade = CascadeType.ALL)
    @ToString.Exclude
    private SinhVien sinhVien;

    @OneToOne(mappedBy = "nguoiDung", cascade = CascadeType.ALL)
    @ToString.Exclude
    private NhanVien nhanVien;


    @PrePersist
    public void prePersist() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    // Kiểm tra tài khoản có bị khóa tạm không
    public boolean isDangBiKhoa() {
        return biKhoaDen != null && biKhoaDen.isAfter(LocalDateTime.now());
    }

    // Lấy tên hiển thị đầy đủ
    public String getTenDayDu() {
        return hoTen + " (" + username + ")";
    }

    // Lấy chữ cái đầu để hiển thị avatar chữ
    public String getChuCaiDau() {
        if (hoTen == null || hoTen.isBlank()) return "?";
        String[] parts = hoTen.trim().split("\\s+");
        return parts[parts.length - 1].substring(0, 1).toUpperCase();
    }
}
