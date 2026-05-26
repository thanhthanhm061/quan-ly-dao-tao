package com.qldt.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "khoa")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Khoa {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_khoa", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã khoa không được trống")
    private String maKhoa;

    @Column(name = "ten_khoa", nullable = false, length = 200)
    @NotBlank(message = "Tên khoa không được trống")
    private String tenKhoa;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "ngay_thanh_lap")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayThanhLap;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "email", length = 100)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "trang_thai")
    @Builder.Default
    private Boolean trangThai = true; // true = hoạt động

    @OneToMany(mappedBy = "khoa", fetch = FetchType.EAGER)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<NhanVien> nhanViens;

//    @OneToMany(mappedBy = "khoa", fetch = FetchType.EAGER)
//    @ToString.Exclude @EqualsAndHashCode.Exclude
//    private List<GiangVien> giangViens;

    @OneToMany(mappedBy = "khoa", fetch = FetchType.EAGER)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Lop> lops;

    @OneToMany(mappedBy = "khoa", fetch = FetchType.EAGER)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<MonHoc> monHocs;

    @Override
    public String toString() { return tenKhoa; }

    @Transient
    public int getSoNhanVien() {
        return nhanViens == null ? 0 : nhanViens.size();
    }
}
