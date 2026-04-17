package com.qldt.model;

import com.qldt.model.enums.LoaiMon;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "mon_hoc")
@Data @NoArgsConstructor @AllArgsConstructor @Builder

public class MonHoc {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_mon", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã môn không được trống")
    private String maMon;

    @Column(name = "ten_mon", nullable = false, length = 200)
    @NotBlank(message = "Tên môn không được trống")
    private String tenMon;

    @Column(name = "so_tin_chi", nullable = false)
    @Min(value = 1, message = "Số tín chỉ tối thiểu là 1")
    @Max(value = 10, message = "Số tín chỉ tối đa là 10")
    private int soTinChi = 3;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_mon")
    private LoaiMon loaiMon = LoaiMon.LY_THUYET;

    @Column(columnDefinition = "TEXT")
    private String moTa;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToMany(mappedBy = "monHoc")
    @ToString.Exclude
    private List<LopHocPhan> lopHocPhans;
}
