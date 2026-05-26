package com.qldt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "thoi_khoa_bieu")
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
@ToString(exclude = {"lopHocPhan"})
public class ThoiKhoaBieu {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public LopHocPhan getLopHocPhan() {
//        return lopHocPhan;
//    }
//
//    public void setLopHocPhan(LopHocPhan lopHocPhan) {
//        this.lopHocPhan = lopHocPhan;
//    }
//
//    public int getThuTrongTuan() {
//        return thuTrongTuan;
//    }
//
//    public void setThuTrongTuan(int thuTrongTuan) {
//        this.thuTrongTuan = thuTrongTuan;
//    }
//
//    public int getTietBatDau() {
//        return tietBatDau;
//    }
//
//    public void setTietBatDau(int tietBatDau) {
//        this.tietBatDau = tietBatDau;
//    }
//
//    public int getSoTiet() {
//        return soTiet;
//    }
//
//    public void setSoTiet(int soTiet) {
//        this.soTiet = soTiet;
//    }
//
//    public String getPhongHoc() {
//        return phongHoc;
//    }
//
//    public void setPhongHoc(String phongHoc) {
//        this.phongHoc = phongHoc;
//    }
//
//    public LocalDate getTuanBatDau() {
//        return tuanBatDau;
//    }
//
//    public void setTuanBatDau(LocalDate tuanBatDau) {
//        this.tuanBatDau = tuanBatDau;
//    }
//
//    public LocalDate getTuanKetThuc() {
//        return tuanKetThuc;
//    }
//
//    public void setTuanKetThuc(LocalDate tuanKetThuc) {
//        this.tuanKetThuc = tuanKetThuc;
//    }

    @ManyToOne
    @JoinColumn(name = "lhp_id", nullable = false)
    private LopHocPhan lopHocPhan;

    @Column(name = "thu_trong_tuan")
    @Min(2) @Max(7)
    private int thuTrongTuan; // 2=Thứ 2, ..., 7=Thứ 7

    @Column(name = "tiet_bat_dau")
    @Min(1) @Max(12)
    private int tietBatDau;

    @Column(name = "so_tiet")
    @Min(1)
    private int soTiet = 3;

    @Column(name = "phong_hoc", length = 20)
    private String phongHoc;

    @Column(name = "tuan_bat_dau")
    private LocalDate tuanBatDau;

    @Column(name = "tuan_ket_thuc")
    private LocalDate tuanKetThuc;

    @ManyToOne
    @JoinColumn(name = "phong_hoc_id")
    private PhongHoc phongHocRef; // liên kết entity PhongHoc (nullable, ưu tiên hơn phongHoc string)

    @Column(name = "tuan_hoc", length = 200)
    private String tuanHoc; // "1,2,3,5,7" — tuần áp dụng (bỏ tuần 4,6 nếu lễ)

    // Tính tự động từ LopHocPhan.monHoc.soTinChi
    public int getSoTietQuyDoi() {
        if (lopHocPhan == null || lopHocPhan.getMonHoc() == null) return soTiet;
        // Quy ước: 1 tín chỉ lý thuyết = 15 tiết/kỳ = ~1-2 tiết/tuần
        return lopHocPhan.getMonHoc().getSoTinChi() * 15;
    }

    // Tính danh sách tuần từ tuanBatDau → tuanKetThuc
    public List<LocalDate> getDanhSachTuan() {
        if (tuanBatDau == null || tuanKetThuc == null) return List.of();
        List<LocalDate> tuan = new ArrayList<>();
        LocalDate cur = tuanBatDau;
        Set<Integer> tuanBo = parseTuanHoc(); // nếu có filter
        int i = 1;
        while (!cur.isAfter(tuanKetThuc)) {
            if (tuanBo.isEmpty() || tuanBo.contains(i)) tuan.add(cur);
            cur = cur.plusWeeks(1);
            i++;
        }
        return tuan;
    }

    private Set<Integer> parseTuanHoc() {
        if (tuanHoc == null || tuanHoc.isBlank()) return Set.of();
        return Arrays.stream(tuanHoc.split(","))
                .map(String::trim).filter(s -> !s.isBlank())
                .map(Integer::parseInt).collect(Collectors.toSet());
    }

    // Kiểm tra TKB có áp dụng cho tuần chứa ngày date không
    public boolean apDungChoNgay(LocalDate date) {
        if (tuanBatDau == null || tuanKetThuc == null) return false;
        if (date.isBefore(tuanBatDau) || date.isAfter(tuanKetThuc)) return false;
        if (tuanHoc == null || tuanHoc.isBlank()) return true;
        // Tính tuần thứ mấy tính từ tuanBatDau
        long soTuanOffset = ChronoUnit.WEEKS.between(tuanBatDau, date) + 1;
        return parseTuanHoc().contains((int) soTuanOffset);
    }

    public String getTenThu() {
        return switch (thuTrongTuan) {
            case 2 -> "Thứ 2"; case 3 -> "Thứ 3"; case 4 -> "Thứ 4";
            case 5 -> "Thứ 5"; case 6 -> "Thứ 6"; case 7 -> "Thứ 7";
            default -> "CN";
        };
    }

    public boolean trungLich(ThoiKhoaBieu other) {
        if (this.thuTrongTuan != other.thuTrongTuan) return false;
        int end1 = this.tietBatDau + this.soTiet;
        int end2 = other.tietBatDau + other.soTiet;
        return this.tietBatDau < end2 && other.tietBatDau < end1;
    }
}
