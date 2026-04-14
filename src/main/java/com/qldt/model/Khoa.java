package com.qldt.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "khoa")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Khoa {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_khoa", unique = true, nullable = false, length = 20)
    private String maKhoa;

    @Column(name = "ten_khoa", nullable = false, length = 200)
    private String tenKhoa;

    @OneToMany(mappedBy = "khoa")
    private List<GiangVien> giangViens;

    @OneToMany(mappedBy = "khoa")
    private List<Lop> lops;

    @OneToMany(mappedBy = "khoa")
    private List<MonHoc> monHocs;

    @Override
    public String toString() { return tenKhoa; }
}
