package com.qldt.model;

import com.qldt.model.enums.TrangThaiLHP;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lop_hoc_phan")
 @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)  // chỉ dùng id để so sánh
@ToString(exclude = {"dangKys", "thoiKhoaBieus"})
public class LopHocPhan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaLhp() {
        return maLhp;
    }

    public void setMaLhp(String maLhp) {
        this.maLhp = maLhp;
    }

    public MonHoc getMonHoc() {
        return monHoc;
    }

    public void setMonHoc(MonHoc monHoc) {
        this.monHoc = monHoc;
    }

    public GiangVien getGiangVien() {
        return giangVien;
    }

    public void setGiangVien(GiangVien giangVien) {
        this.giangVien = giangVien;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public int getSiSoMax() {
        return siSoMax;
    }

    public void setSiSoMax(int siSoMax) {
        this.siSoMax = siSoMax;
    }

    public int getSiSoHienTai() {
        return siSoHienTai;
    }

    public void setSiSoHienTai(int siSoHienTai) {
        this.siSoHienTai = siSoHienTai;
    }

    public TrangThaiLHP getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiLHP trangThai) {
        this.trangThai = trangThai;
    }

    public List<DangKy> getDangKys() {
        return dangKys;
    }

    public void setDangKys(List<DangKy> dangKys) {
        this.dangKys = dangKys;
    }

    public List<ThoiKhoaBieu> getThoiKhoaBieus() {
        return thoiKhoaBieus;
    }

    public void setThoiKhoaBieus(List<ThoiKhoaBieu> thoiKhoaBieus) {
        this.thoiKhoaBieus = thoiKhoaBieus;
    }
    public LocalDateTime getThoiGianMo()   { return thoiGianMo; }
    public void setThoiGianMo(LocalDateTime t) { this.thoiGianMo = t; }
    public LocalDateTime getThoiGianDong() { return thoiGianDong; }
    public void setThoiGianDong(LocalDateTime t) { this.thoiGianDong = t; }
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

    @OneToMany(mappedBy = "lopHocPhan",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            fetch = FetchType.LAZY)
    private List<DangKy> dangKys = new ArrayList<>();

    @OneToMany(mappedBy = "lopHocPhan",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ThoiKhoaBieu> thoiKhoaBieus = new ArrayList<>();


    @Column(name = "thoi_gian_mo")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime thoiGianMo;

    @Column(name = "thoi_gian_dong")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime thoiGianDong;
    public boolean isDangTrongThoiGianDangKy() {
        LocalDateTime now = LocalDateTime.now();
        if (thoiGianMo != null && now.isBefore(thoiGianMo)) return false;
        if (thoiGianDong != null && now.isAfter(thoiGianDong))  return false;
        return trangThai == TrangThaiLHP.MO;
    }
    public boolean isConCho() { return siSoHienTai < siSoMax; }
    public int getSoChoConLai() { return siSoMax - siSoHienTai; }
}
