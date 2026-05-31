package com.qldt.controller;

import com.qldt.model.ChucVu;
import com.qldt.model.Khoa;
import com.qldt.model.NhanVien;
import com.qldt.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;

import java.util.List;

@Controller
@RequestMapping("/admin/nhan-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class NhanVienController {

    private final NhanVienService nhanVienService;
    private final KhoaService khoaService;
    private final ChucVuService chucVuService;
    private final NhanVienChucVuService nvcvService;

    private void addFormData(Model model) {
        model.addAttribute("dsKhoa", khoaService.findAll());
        model.addAttribute("dsChucVu", chucVuService.findAllHoatDong());
        model.addAttribute("dsHocVi",     java.util.List.of("CN", "ThS", "TS", "PGS.TS", "GS.TS"));
        model.addAttribute("dsHocHam",    java.util.List.of("", "GS", "PGS"));
        model.addAttribute("dsGioiTinh",  java.util.List.of("Nam", "Nữ", "Khác"));
        model.addAttribute("dsLoaiHopDong", java.util.List.of("Chính thức", "Hợp đồng", "Thử việc", "Thỉnh giảng"));
    }
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<NhanVien> tatCa = nhanVienService.findAll();
        LocalDate homNay = LocalDate.now();
        LocalDate sau90Ngay = homNay.plusDays(90);
        LocalDate dauThang = homNay.withDayOfMonth(1);

        // ── Tính thống kê ──────────────────────────────────────

        long tongNV = tatCa.size();
        long dangLam = tatCa.stream().filter(nv -> Boolean.TRUE.equals(nv.getTrangThai())).count();
        long daNghi  = tatCa.stream().filter(nv -> !Boolean.TRUE.equals(nv.getTrangThai())).count();

        // Sắp hết hạn HĐ trong 90 ngày
        long soHetHan = tatCa.stream()
                .filter(nv -> nv.getKetThucLamViec() != null
                        && !nv.getKetThucLamViec().isBefore(homNay)
                        && !nv.getKetThucLamViec().isAfter(sau90Ngay))
                .count();

        // Mới tuyển tháng này
        long moiThang = tatCa.stream()
                .filter(nv -> nv.getNgayTuyenDung() != null
                        && !nv.getNgayTuyenDung().isBefore(dauThang))
                .count();

        // Nghỉ tháng này (kết thúc HĐ trong tháng)
        long nghiThang = tatCa.stream()
                .filter(nv -> nv.getKetThucLamViec() != null
                        && !nv.getKetThucLamViec().isBefore(dauThang)
                        && !nv.getKetThucLamViec().isAfter(homNay))
                .count();

        // Học vị
        long soGSSTS = tatCa.stream()
                .filter(nv -> "PGS.TS".equals(nv.getHocVi()) || "GS.TS".equals(nv.getHocVi())
                        || "GS".equals(nv.getHocHam()) || "PGS".equals(nv.getHocHam()))
                .count();
        long soTSThu = tatCa.stream()
                .filter(nv -> "TS".equals(nv.getHocVi()) && nv.getHocHam() == null)
                .count();
        long soThacSi = tatCa.stream().filter(nv -> "ThS".equals(nv.getHocVi())).count();
        long soCuNhan = tatCa.stream()
                .filter(nv -> "CN".equals(nv.getHocVi()) || nv.getHocVi() == null).count();
        long tongTienSi = soGSSTS + soTSThu;

        // Tỉ lệ
        String tiLeHoatDong = tongNV > 0
                ? String.format("%.1f", (dangLam * 100.0 / tongNV)) : "0";
        String tiLeTienSi = dangLam > 0
                ? String.format("%.0f", (tongTienSi * 100.0 / dangLam)) : "0";

        // Loại hợp đồng
        java.util.function.Function<String, Long> demHD = loai ->
                tatCa.stream().filter(nv -> loai.equals(nv.getLoaiHopDong())).count();
        long chinhThuc = demHD.apply("Chính thức");
        long hopDong   = demHD.apply("Hợp đồng");
        long thinhGiang= demHD.apply("Thỉnh giảng");
        long thuViec   = demHD.apply("Thử việc");

        List<DashboardNhanSuDTO.HopDongItem> dsHD = List.of(
                DashboardNhanSuDTO.HopDongItem.builder()
                        .ten("Chính thức").soLuong(chinhThuc)
                        .tiLe(tongNV > 0 ? String.format("%.0f", chinhThuc * 100.0 / tongNV) : "0")
                        .mauSac("#2471a3").build(),
                DashboardNhanSuDTO.HopDongItem.builder()
                        .ten("Hợp đồng").soLuong(hopDong)
                        .tiLe(tongNV > 0 ? String.format("%.0f", hopDong * 100.0 / tongNV) : "0")
                        .mauSac("#27ae60").build(),
                DashboardNhanSuDTO.HopDongItem.builder()
                        .ten("Thỉnh giảng").soLuong(thinhGiang)
                        .tiLe(tongNV > 0 ? String.format("%.0f", thinhGiang * 100.0 / tongNV) : "0")
                        .mauSac("#d68910").build(),
                DashboardNhanSuDTO.HopDongItem.builder()
                        .ten("Thử việc").soLuong(thuViec)
                        .tiLe(tongNV > 0 ? String.format("%.0f", thuViec * 100.0 / tongNV) : "0")
                        .mauSac("#85929e").build()
        );

        // Nhân viên theo khoa
        var dsKhoa = khoaService.findAll();
        long maxNV = dsKhoa.stream()
                .mapToLong(k -> k.getNhanViens() != null ? k.getNhanViens().size() : 0)
                .max().orElse(1);

        List<DashboardNhanSuDTO.KhoaItem> nhanVienTheoKhoa = dsKhoa.stream()
                .filter(k -> k.getNhanViens() != null && !k.getNhanViens().isEmpty())
                .map(k -> {
                    long soNV = k.getNhanViens().size();
                    long soGV = k.getNhanViens().stream()
                            .filter(nv -> nv.getChucVu() != null
                                    && (nv.getChucVu().getMaChucVu().startsWith("GV")
                                    || "TBM".equals(nv.getChucVu().getMaChucVu())
                                    || "TK".equals(nv.getChucVu().getMaChucVu())
                                    || "PTK".equals(nv.getChucVu().getMaChucVu())))
                            .count();
                    return DashboardNhanSuDTO.KhoaItem.builder()
                            .tenKhoa(k.getTenKhoa())
                            .soNhanVien(soNV)
                            .soGiangVien(soGV)
                            .soNhanVienHanhChinh(soNV - soGV)
                            .tiLePhan(String.valueOf(Math.round(soNV * 100.0 / maxNV)))
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getSoNhanVien(), a.getSoNhanVien()))
                .limit(6)
                .collect(java.util.stream.Collectors.toList());

        // Top 5 nhân viên mới nhất
        List<NhanVien> nhanVienMoi = tatCa.stream()
                .filter(nv -> nv.getNgayTuyenDung() != null)
                .sorted((a, b) -> b.getNgayTuyenDung().compareTo(a.getNgayTuyenDung()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        // Top 5 sắp hết hạn HĐ
        List<NhanVien> sapHetHanHD = tatCa.stream()
                .filter(nv -> nv.getKetThucLamViec() != null
                        && !nv.getKetThucLamViec().isBefore(homNay)
                        && !nv.getKetThucLamViec().isAfter(sau90Ngay))
                .sorted((a, b) -> a.getKetThucLamViec().compareTo(b.getKetThucLamViec()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        // Gán soNgayConLai vào model (dùng Map để tránh sửa entity)
        java.util.Map<Long, Long> soNgayConLaiMap = new java.util.HashMap<>();
        sapHetHanHD.forEach(nv -> soNgayConLaiMap.put(nv.getId(),
                java.time.temporal.ChronoUnit.DAYS.between(homNay, nv.getKetThucLamViec())));

        // Top 5 lịch sử chức vụ gần đây
        List<com.qldt.model.NhanVienChucVu> lichSuGanDay = nvcvService.findAll()
                .stream()
                .filter(ls -> ls.getNgayKetThuc() == null) // đang đảm nhiệm
                .sorted((a, b) -> b.getNgayBatDau().compareTo(a.getNgayBatDau()))
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        // ── Đưa vào Model ──────────────────────────────────────
        DashboardNhanSuDTO dto = DashboardNhanSuDTO.builder()
                .tongNhanVien(tongNV)
                .dangLamViec(dangLam)
                .daNghi(daNghi)
                .soKhoa(dsKhoa.size())
                .soKhoaHoatDong(dsKhoa.stream().filter(k -> Boolean.TRUE.equals(k.getTrangThai())).count())
                .soTienSi(tongTienSi)
                .soHetHanHopDong(soHetHan)
                .moiThangNay(moiThang)
                .nghiThangNay(nghiThang)
                .tiLeHoatDong(tiLeHoatDong)
                .tiLeTienSi(tiLeTienSi)
                .soGSSTS(soGSSTS)
                .soTienSiThu(soTSThu)
                .soThacSi(soThacSi)
                .soCuNhan(soCuNhan)
                .loaiHopDong(dsHD)
                .nhanVienTheoKhoa(nhanVienTheoKhoa)
                .build();

        model.addAttribute("thongKe", dto);
        model.addAttribute("nhanVienMoi", nhanVienMoi);
        model.addAttribute("sapHetHanHD", sapHetHanHD);
        model.addAttribute("soNgayConLaiMap", soNgayConLaiMap);
        model.addAttribute("lichSuChucVuGanDay", lichSuGanDay);
        model.addAttribute("loaiHopDong", dsHD);
        model.addAttribute("nhanVienTheoKhoa", nhanVienTheoKhoa);
        model.addAttribute("namHoc", "2025–2026");

        return "nhan-vien/dashboard";
    }
    @GetMapping
    public String danhSach(@RequestParam(required = false) String search,
                           @RequestParam(required = false) Long khoaId,
                           Model model) {
        var ds = search != null ? nhanVienService.timKiem(search)
                : khoaId != null ? nhanVienService.findByKhoaId(khoaId)
                : nhanVienService.findAll();
        model.addAttribute("dsNhanVien", ds);
        model.addAttribute("timKiem", search);
        model.addAttribute("khoaId", khoaId);
        model.addAttribute("dsKhoa", khoaService.findAll());
        model.addAttribute("tongSo", nhanVienService.count());
        return "nhan-vien/danh-sach";
    }
    @GetMapping("/them")
    public String themForm(Model model) {
        NhanVien nv = new NhanVien();

        // Khởi tạo object tránh lỗi binding form
        nv.setKhoa(new Khoa());
        nv.setChucVu(new ChucVu());

        model.addAttribute("nhanVien", nv);
        model.addAttribute("tieuDe", "Thêm Nhân Viên Mới");
        model.addAttribute("coVanHocTaps", nhanVienService.findAllCoVanHocTap());
        addFormData(model);

        return "nhan-vien/them-sua";
    }

    @PostMapping("/them")
    public String them(
            @Valid @ModelAttribute("nhanVien") NhanVien nhanVien,
            BindingResult result,
            Model model,
            RedirectAttributes ra
    ) {

        // Validate form
        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Thêm Nhân Viên Mới");
            addFormData(model);
            return "nhan-vien/them-sua";
        }

        try {

            // Service sẽ tự tạo tài khoản
            NhanVien saved = nhanVienService.save(nhanVien);

            String username = saved.getMaNhanVien().toLowerCase();

            ra.addFlashAttribute(
                    "success",
                    "Thêm nhân viên '" + saved.getHoTen() +
                            "' thành công! Tài khoản đăng nhập: " +
                            username + " / Admin@123"
            );

            return "redirect:/admin/nhan-vien";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("tieuDe", "Thêm Nhân Viên Mới");

            addFormData(model);

            return "nhan-vien/them-sua";

        } catch (Exception e) {

            model.addAttribute("error", "Có lỗi xảy ra khi thêm nhân viên");
            model.addAttribute("tieuDe", "Thêm Nhân Viên Mới");

            addFormData(model);

            return "nhan-vien/them-sua";
        }
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("nhanVien", nhanVienService.findById(id).orElseThrow());
        model.addAttribute("tieuDe", "Chỉnh Sửa Nhân Viên");
        model.addAttribute("coVanHocTaps", nhanVienService.findAllCoVanHocTap());
        addFormData(model);
        return "nhan-vien/them-sua";
    }
    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @Valid @ModelAttribute NhanVien nhanVien,
                      BindingResult result,
                      Model model,
                      RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("tieuDe", "Chỉnh Sửa Nhân Viên");
            model.addAttribute("coVanHocTaps", nhanVienService.findAllCoVanHocTap());
            addFormData(model);
            return "nhan-vien/them-sua";
        }

        try {
            nhanVien.setId(id);  // đảm bảo đúng ID
            nhanVienService.save(nhanVien);
            ra.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tieuDe", "Chỉnh Sửa Nhân Viên");
            model.addAttribute("coVanHocTaps", nhanVienService.findAllCoVanHocTap());
            addFormData(model);
            return "nhan-vien/them-sua";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật nhân viên");
        }

        return "redirect:/admin/nhan-vien";
    }

    @GetMapping("/chi-tiet/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        var nv = nhanVienService.getChiTiet(id);
        model.addAttribute("nhanVien", nv);
        model.addAttribute("lichSuChucVu", nvcvService.getLichSu(id));
        return "nhan-vien/chi-tiet";
    }

    @PostMapping("/doi-trang-thai/{id}")
    public String doiTrangThai(@PathVariable Long id, RedirectAttributes ra) {
        nhanVienService.doiTrangThai(id);
        ra.addFlashAttribute("success", "Đã cập nhật trạng thái nhân viên!");
        return "redirect:/admin/nhan-vien";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            nhanVienService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa nhân viên!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/nhan-vien";
    }
}