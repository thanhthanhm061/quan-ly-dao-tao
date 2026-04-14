package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "thoi_khoa_bieu")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ThoiKhoaBieu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lhp_id", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "thu_trong_tuan")
    @Min(2) @Max(7)
    private int thuTrongTuan; // 2=Thứ 2, ..., 7=Thứ 7

    @Column(name = "tiet_bat_dau")
    @Min(1) @Max(12)
    private int tietBatDau;

    @Column(name = "so_tiet")
    @Min(1)
    private int soTiet = 3;

    @Column(name = "phong_hoc", length = 20)
    private String phongHoc;

    @Column(name = "tuan_bat_dau")
    private LocalDate tuanBatDau;

    @Column(name = "tuan_ket_thuc")
    private LocalDate tuanKetThuc;

    public String getTenThu() {
        return switch (thuTrongTuan) {
            case 2 -> "Thứ 2"; case 3 -> "Thứ 3"; case 4 -> "Thứ 4";
            case 5 -> "Thứ 5"; case 6 -> "Thứ 6"; case 7 -> "Thứ 7";
            default -> "CN";
        };
    }

    public boolean trungLich(ThoiKhoaBieu other) {
        if (this.thuTrongTuan != other.thuTrongTuan) return false;
        int end1 = this.tietBatDau + this.soTiet;
        int end2 = other.tietBatDau + other.soTiet;
        return this.tietBatDau < end2 && other.tietBatDau < end1;
    }
}
