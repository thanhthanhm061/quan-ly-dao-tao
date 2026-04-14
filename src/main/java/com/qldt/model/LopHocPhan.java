package com.qldt.model;

import com.qldt.model.enums.TrangThaiLHP;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "lop_hoc_phan")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LopHocPhan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_lhp", unique = true, nullable = false, length = 30)
    @NotBlank(message = "Mã lớp học phần không được trống")
    private String maLhp;

    @ManyToOne
    @JoinColumn(name = "mon_hoc_id", nullable = false)
    @NotNull(message = "Chưa chọn môn học")
    private MonHoc monHoc;

    @ManyToOne
    @JoinColumn(name = "giang_vien_id", nullable = false)
    @NotNull(message = "Chưa chọn giảng viên")
    private GiangVien giangVien;

    @Column(name = "hoc_ky", length = 20)
    @NotBlank(message = "Học kỳ không được trống")
    private String hocKy;

    @Column(name = "si_so_max")
    @Min(value = 1, message = "Sĩ số tối đa phải lớn hơn 0")
    private int siSoMax = 40;

    @Column(name = "si_so_hien_tai")
    private int siSoHienTai = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiLHP trangThai = TrangThaiLHP.MO;

    @OneToMany(mappedBy = "lopHocPhan", cascade = CascadeType.ALL)
    private List<DangKy> dangKys;

    @OneToMany(mappedBy = "lopHocPhan", cascade = CascadeType.ALL)
    private List<ThoiKhoaBieu> thoiKhoaBieus;

    public boolean isConCho() { return siSoHienTai < siSoMax; }
    public int getSoChoConLai() { return siSoMax - siSoHienTai; }
}
