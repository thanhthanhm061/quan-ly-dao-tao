package com.qldt.model;

import com.qldt.model.enums.LoaiPhong;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data

@Entity
@Table(name = "phong_hoc")

public class PhongHoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phong", length = 20, nullable = false, unique = true)
    private String maPhong; // "A101", "Lab2"

    @Column(name = "ten_phong", length = 100)
    private String tenPhong; // "Phòng học A101"

    @Column(name = "suc_chua")
    private int sucChua; // sức chứa tối đa (số sinh viên)

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_phong")
    private LoaiPhong loaiPhong = LoaiPhong.LY_THUYET;

    @Column(name = "trang_thai")
    private boolean hoatDong = true;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

}