package com.qldt.model;

import com.qldt.model.enums.TrangThaiDK;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dang_ky",
    uniqueConstraints = @UniqueConstraint(columnNames = {"sinh_vien_id", "lhp_id"}))
 @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"sinhVien", "lopHocPhan"})
public class DangKy {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SinhVien getSinhVien() {
        return sinhVien;
    }

    public void setSinhVien(SinhVien sinhVien) {
        this.sinhVien = sinhVien;
    }

    public LopHocPhan getLopHocPhan() {
        return lopHocPhan;
    }

    public void setLopHocPhan(LopHocPhan lopHocPhan) {
        this.lopHocPhan = lopHocPhan;
    }

    public LocalDateTime getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(LocalDateTime ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public TrangThaiDK getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDK trangThai) {
        this.trangThai = trangThai;
    }

    public Double getDiemQuaTrinh() {
        return diemQuaTrinh;
    }

    public void setDiemQuaTrinh(Double diemQuaTrinh) {
        this.diemQuaTrinh = diemQuaTrinh;
    }

    public Double getDiemThi() {
        return diemThi;
    }

    public void setDiemThi(Double diemThi) {
        this.diemThi = diemThi;
    }

    public Double getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(Double diemTongKet) {
        this.diemTongKet = diemTongKet;
    }

    @ManyToOne
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne
    @JoinColumn(name = "lhp_id", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "ngay_dang_ky")
    private LocalDateTime ngayDangKy;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiDK trangThai = TrangThaiDK.DANG_HOC;

    @Column(name = "diem_qua_trinh")
    private Double diemQuaTrinh;

    @Column(name = "diem_thi")
    private Double diemThi;

    @Column(name = "diem_tong_ket")
    private Double diemTongKet;

    @PrePersist
    public void prePersist() { ngayDangKy = LocalDateTime.now(); }

    public void tinhDiemTongKet() {
        if (diemQuaTrinh != null && diemThi != null)
            this.diemTongKet = diemQuaTrinh * 0.3 + diemThi * 0.7;
    }

    public boolean isDat() { return diemTongKet != null && diemTongKet >= 4.0; }

    public String getXepLoai() {
        if (diemTongKet == null) return "-";
        if (diemTongKet >= 9.0) return "Xuất sắc";
        if (diemTongKet >= 8.0) return "Giỏi";
        if (diemTongKet >= 7.0) return "Khá";
        if (diemTongKet >= 5.0) return "Trung bình";
        return "Yếu/Kém";
    }
}
