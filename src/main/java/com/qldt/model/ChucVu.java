package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "chuc_vu")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChucVu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_chuc_vu", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã chức vụ không được trống")
    private String maChucVu; // VD: GV, TP, HT, TK...

    @Column(name = "ten_chuc_vu", nullable = false, length = 200)
    @NotBlank(message = "Tên chức vụ không được trống")
    private String tenChucVu; // VD: Giảng viên, Trưởng phòng...

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "cap_bac", length = 100)
    private String capBac; // VD: Cấp khoa, Cấp trường...

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa; // Chức vụ thuộc khoa nào (nullable = thuộc trường)

    @Column(name = "trang_thai")
    @Builder.Default
    private Boolean trangThai = true;

    @OneToMany(mappedBy = "chucVu")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<NhanVienChucVu> nhanVienChucVus;

    @Override
    public String toString() { return tenChucVu; }
}