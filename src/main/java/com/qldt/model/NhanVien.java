package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import com.qldt.model.enums.VaiTro;

@Entity
@Table(name = "nhan_vien")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NhanVien {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_nhan_vien", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Mã nhân viên không được trống")
    private String maNhanVien;

    @Column(name = "ho_ten", nullable = false, length = 100)
    @NotBlank(message = "Họ tên không được trống")
    private String hoTen;

    @Column(name = "ngay_sinh")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh; // Nam, Nữ, Khác

    @Column(name = "email", length = 100)
    @Email(message = "Email không đúng định dạng")
    private String email;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 255)
    private String diaChi;

    @Column(name = "ngay_tuyen_dung")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayTuyenDung;

    @Column(name = "loai_hop_dong", length = 100)
    private String loaiHopDong; // Chính thức, Hợp đồng, Thử việc

    @Column(name = "he_so_luong")
    private Double heSoLuong;

    @Column(name = "hoc_vi", length = 50)
    private String hocVi; // ThS, TS, CN...

    @Column(name = "hoc_ham", length = 50)
    private String hocHam; // GS, PGS...

    @Column(name = "chuyen_mon", length = 255)
    private String chuyenMon;

    @Column(name = "bat_dau_lam_viec")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate batDauLamViec;

    @Column(name = "ket_thuc_lam_viec")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ketThucLamViec; // null = chưa kết thúc

    @ManyToOne
    @JoinColumn(name = "khoa_id")
    private Khoa khoa;

    @ManyToOne
    @JoinColumn(name = "chuc_vu_id")
    private ChucVu chucVu; // Chức vụ hiện tại

    @OneToOne
    @JoinColumn(name = "nguoi_dung_id", unique = true)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private NguoiDung nguoiDung;

    @OneToMany(mappedBy = "nhanVien", cascade = CascadeType.ALL)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<NhanVienChucVu> lichSuChucVu; // Lịch sử các chức vụ đã đảm nhiệm

    @Transient
    public String getChuCaiDau() {
        if (hoTen == null || hoTen.isBlank()) return "?";
        String[] parts = hoTen.trim().split("\\s+");
        return String.valueOf(parts[parts.length - 1].charAt(0)).toUpperCase();
    }
    @Column(name = "trang_thai")
    @Builder.Default
    private Boolean trangThai = true;

    // ── Quan hệ mới: lớp học phần do nhân viên (giảng viên) phụ trách ──
    @OneToMany(mappedBy = "giangVien", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<LopHocPhan> lopHocPhans = new ArrayList<>();

    // ── Quan hệ mới: các lớp hành chính mà nhân viên là cố vấn học tập ──
    @OneToMany(mappedBy = "coVanHocTap", fetch = FetchType.LAZY)
    @ToString.Exclude @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Lop> lopCoVan = new ArrayList<>();


    // ── Helper methods ──────────────────────────────────────────────────

    /**
     * Kiểm tra nhân viên này có phải giảng viên không
     * (dựa theo mã chức vụ bắt đầu bằng "GV")
     */
    @Transient
    public boolean isGiangVien() {
        return chucVu != null
                && (chucVu.getMaChucVu().startsWith("gv")
                || chucVu.getMaChucVu().startsWith("GV")
                || "TBM".equals(chucVu.getMaChucVu())
                || "TK".equals(chucVu.getMaChucVu())
                || "PTK".equals(chucVu.getMaChucVu())
                || "CNTT".equals(chucVu.getMaChucVu())); // ← thêm
    }


    @Transient
    public String getHoTenVaHocVi() {

        String hv = (hocVi != null && !hocVi.isBlank())
                ? hocVi + ". "
                : "";

        String cv = (chucVu != null && chucVu.getTenChucVu() != null)
                ? " — " + chucVu.getTenChucVu()
                : "";

        return hv + hoTen + cv;
    }

    public String getTrangThaiText() {
        return Boolean.TRUE.equals(trangThai) ? "Đang làm việc" : "Đã nghỉ";
    }
}