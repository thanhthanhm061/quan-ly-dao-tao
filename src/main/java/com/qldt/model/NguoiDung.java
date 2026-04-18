package com.qldt.model;

import com.qldt.model.enums.VaiTro;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nguoi_dung")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "mat_khau", nullable = false)
    private String matKhau;

    @Column(name = "ho_ten", nullable = false, length = 100)
    private String hoTen;

    @Column(unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "vai_tro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "kich_hoat")
    private boolean kichHoat = true;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToOne(mappedBy = "nguoiDung", cascade = CascadeType.ALL)
    @ToString.Exclude
    private SinhVien sinhVien;

    @OneToOne(mappedBy = "nguoiDung", cascade = CascadeType.ALL)
    @ToString.Exclude
    private GiangVien giangVien;

    @PrePersist
    public void prePersist() {
        ngayTao = LocalDateTime.now();
    }
}
