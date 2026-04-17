package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "giang_vien")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class GiangVien {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_gv", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã giảng viên không được trống")
    private String maGv;

    @Column(name = "ho_ten", nullable = false, length = 100)
    @NotBlank(message = "Họ tên không được trống")
    private String hoTen;

    @Column(name = "hoc_vi", length = 50)
    private String hocVi; // ThS, TS, PGS.TS, GS.TS

    @Column(name = "chuyen_nganh", length = 100)
    private String chuyenNganh;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(length = 100)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @OneToOne
    @JoinColumn(name = "nguoi_dung_id", unique = true)
    private NguoiDung nguoiDung;

    @OneToMany(mappedBy = "giangVien")
    @ToString.Exclude
    private List<LopHocPhan> lopHocPhans;

    public String getHoTenVaHocVi() {
        return (hocVi != null && !hocVi.isBlank() ? hocVi + ". " : "") + hoTen;
    }
}
