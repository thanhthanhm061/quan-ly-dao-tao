package com.qldt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lop")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Lop {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_lop", unique = true, nullable = false, length = 20)
    private String maLop;

    @Column(name = "ten_lop", nullable = false, length = 100)
    private String tenLop;

    @Column(name = "nien_khoa")
    private Integer nienKhoa;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToMany(mappedBy = "lop", fetch = FetchType.LAZY)
    private List<SinhVien> sinhViens = new ArrayList<>();

    @Override
    public String toString() { return maLop + " - " + tenLop; }
    // Thêm vào class Lop
    @ManyToOne
    @JoinColumn(name = "co_van_ht_id")
    private GiangVien coVanHocTap;

    public GiangVien getCoVanHocTap() { return coVanHocTap; }
    public void setCoVanHocTap(GiangVien coVanHocTap) { this.coVanHocTap = coVanHocTap; }
}
