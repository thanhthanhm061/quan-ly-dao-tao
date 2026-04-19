package com.qldt.model;

import com.qldt.model.enums.GioiTinh;
import com.qldt.model.enums.TrangThaiSV;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "sinh_vien")
 @NoArgsConstructor @AllArgsConstructor @Builder
public class SinhVien {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaSv() {
        return maSv;
    }

    public void setMaSv(String maSv) {
        this.maSv = maSv;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public GioiTinh getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(GioiTinh gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TrangThaiSV getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiSV trangThai) {
        this.trangThai = trangThai;
    }

    public Lop getLop() {
        return lop;
    }

    public void setLop(Lop lop) {
        this.lop = lop;
    }

    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }

    public void setNguoiDung(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    public List<DangKy> getDangKys() {
        return dangKys;
    }

    public void setDangKys(List<DangKy> dangKys) {
        this.dangKys = dangKys;
    }

    @Column(name = "ma_sv", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã sinh viên không được trống")
    private String maSv;

    @Column(name = "ho_ten", nullable = false, length = 100)
    @NotBlank(message = "Họ tên không được trống")
    private String hoTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Enumerated(EnumType.STRING)
    @Column(name = "gioi_tinh")
    private GioiTinh gioiTinh = GioiTinh.Nam;

    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "so_dien_thoai", length = 15)
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String soDienThoai;

    @Column(length = 100)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiSV trangThai = TrangThaiSV.DANG_HOC;

    @ManyToOne
    @JoinColumn(name = "lop_id")
    private Lop lop;

    @OneToOne
    @JoinColumn(name = "nguoi_dung_id", unique = true)
    private NguoiDung nguoiDung;

    @OneToMany(mappedBy = "sinhVien", cascade = CascadeType.ALL)
    private List<DangKy> dangKys;
}
