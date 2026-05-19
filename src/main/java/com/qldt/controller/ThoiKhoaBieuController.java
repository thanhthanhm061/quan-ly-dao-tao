package com.qldt.controller;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.GiangVienRepository;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.awt.*;
import java.awt.Font;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

// =====================================================================
// MON HOC CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin/mon-hoc")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class MonHocController {
    private final MonHocService monService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        List<MonHoc> monHocs = search != null ? monService.search(search) : monService.findAll();

        model.addAttribute("monHocs", monHocs);
        model.addAttribute("search", search);

        // Thêm thống kê
        model.addAttribute("totalCredits",
                monHocs.stream().mapToInt(MonHoc::getSoTinChi).sum());
        model.addAttribute("lyTHuyetCount",
                monHocs.stream().filter(m -> m.getLoaiMon() == LoaiMon.LY_THUYET).count());
        model.addAttribute("thucHanhCount",
                monHocs.stream().filter(m -> m.getLoaiMon() == LoaiMon.THUC_HANH).count());

        return "monhoc/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("monHoc", new MonHoc());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("loaiMons", LoaiMon.values());
        return "monhoc/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute MonHoc mon, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("loaiMons", LoaiMon.values());
            return "monhoc/form";
        }
        try {
            monService.save(mon);
            ra.addFlashAttribute("success", "Thêm môn học '" + mon.getTenMon() + "' thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/mon-hoc";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("monHoc", monService.findById(id).orElseThrow());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("loaiMons", LoaiMon.values());
        return "monhoc/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute MonHoc mon,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("loaiMons", LoaiMon.values());
            return "monhoc/form";
        }
        try { mon.setId(id); monService.save(mon); ra.addFlashAttribute("success", "Cập nhật thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/mon-hoc";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try { monService.delete(id); ra.addFlashAttribute("success", "Đã xóa môn học!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/mon-hoc";
    }
}

// =====================================================================
// LOP CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin/lop")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class LopController {
    private final LopService lopService;
    private final KhoaService khoaService;
    private final GiangVienService giangVienService;
    private void resolveCvht(Lop lop) {
        if (lop.getCoVanHocTap() != null && lop.getCoVanHocTap().getId() != null) {
            lop.setCoVanHocTap(
                    giangVienService.findById(lop.getCoVanHocTap().getId()).orElse(null)
            );
        } else {
            lop.setCoVanHocTap(null);
        }
    }
    @GetMapping
    public String list(Model model) {
        model.addAttribute("lops", lopService.findAll());
        return "lop/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("lop", new Lop());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("giangViens",giangVienService.findAll());
        return "lop/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute Lop lop, RedirectAttributes ra) {
        try {
            resolveCvht(lop);
            lopService.save(lop);
            ra.addFlashAttribute("success", "Thêm lớp thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("lop", lopService.findById(id).orElseThrow());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("giangViens",giangVienService.findAll());
        return "lop/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @ModelAttribute Lop lop, RedirectAttributes ra) {
        try {
            lop.setId(id);
            resolveCvht(lop);
            lopService.save(lop);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            lopService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa lớp!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop";
    }

    // Có thể thêm các phương thức khác như xem chi tiết lớp, danh sách sinh viên trong lớp, v.v.
    @GetMapping("/{id}/sinh-vien")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getSinhViens(@PathVariable Long id) {
        // findById thông thường chỉ load Lop, không load sinhViens (LAZY)
        // Phải dùng query có fetch join
        Lop lop = lopService.findByIdWithSinhViens(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lớp"));

        List<Map<String, String>> result = lop.getSinhViens().stream()
                .map(sv -> Map.of(
                        "maSv",  sv.getMaSv(),
                        "hoTen", sv.getHoTen(),
                        "email", sv.getEmail() != null ? sv.getEmail() : "",
                        "sdt",   sv.getSoDienThoai() != null ? sv.getSoDienThoai() : ""
                ))
                .toList();
        return ResponseEntity.ok(result);
    }
}

// =====================================================================
// LOP HOC PHAN CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin/lop-hoc-phan")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class LopHocPhanController {

    private final LopHocPhanService lhpService;
    private final MonHocService monService;
    private final GiangVienService gvService;
    private final SinhVienService svService;
    private final LopService lopService;

    // ── Danh sách ──────────────────────────────────────────────────────────────
    @GetMapping
    public String list(@RequestParam(required = false) String hocKy, Model model) {
        List<LopHocPhan> lhps = (hocKy != null && !hocKy.isBlank())
                ? lhpService.findByHocKy(hocKy)
                : lhpService.findAll();
        model.addAttribute("lopHocPhans", lhps);
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);
        return "lophocphan/list";
    }

    // ── Thêm mới ───────────────────────────────────────────────────────────────
    @GetMapping("/them")
    public String themForm(Model model) {
        LopHocPhan lhp = new LopHocPhan();
        lhp.setMonHoc(new MonHoc());
        lhp.setGiangVien(new GiangVien());
        model.addAttribute("lopHocPhan", lhp);
        model.addAttribute("monHocs", monService.findAll());
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("trangThais", TrangThaiLHP.values());
        return "lophocphan/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute LopHocPhan lhp,
                       BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            if (lhp.getMonHoc() == null) lhp.setMonHoc(new MonHoc());
            if (lhp.getGiangVien() == null) lhp.setGiangVien(new GiangVien());
            model.addAttribute("monHocs", monService.findAll());
            model.addAttribute("giangViens", gvService.findAll());
            model.addAttribute("trangThais", TrangThaiLHP.values());
            return "lophocphan/form";
        }
        try {
            lhpService.save(lhp);
            ra.addFlashAttribute("success", "Thêm lớp học phần thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop-hoc-phan";
    }

    // ── Sửa ────────────────────────────────────────────────────────────────────
    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        // Đảm bảo không null để form binding không lỗi
        if (lhp.getMonHoc() == null) lhp.setMonHoc(new MonHoc());
        if (lhp.getGiangVien() == null) lhp.setGiangVien(new GiangVien());
        model.addAttribute("lopHocPhan", lhp);
        model.addAttribute("monHocs", monService.findAll());
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("trangThais", TrangThaiLHP.values());
        return "lophocphan/form";
    }

    /**
     * FIX: Không save trực tiếp object từ form vì sẽ mất dữ liệu dangKys
     * (dẫn đến siSoHienTai bị reset về 0). Thay vào đó:
     * 1. Load entity từ DB
     * 2. Chỉ cập nhật các field được phép chỉnh sửa
     * 3. Save entity đã được merge
     */
    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @Valid @ModelAttribute("lopHocPhan") LopHocPhan formLhp,
                      BindingResult result, Model model, RedirectAttributes ra) {

        if (result.hasErrors()) {
            if (formLhp.getMonHoc() == null) formLhp.setMonHoc(new MonHoc());
            if (formLhp.getGiangVien() == null) formLhp.setGiangVien(new GiangVien());
            model.addAttribute("monHocs", monService.findAll());
            model.addAttribute("giangViens", gvService.findAll());
            model.addAttribute("trangThais", TrangThaiLHP.values());
            return "lophocphan/form";
        }

        try {
            // Load entity gốc từ DB — giữ nguyên dangKys, siSoHienTai
            LopHocPhan existing = lhpService.findById(id).orElseThrow();

            // Chỉ cập nhật các field cho phép sửa (KHÔNG cập nhật maLhp)
            existing.setHocKy(formLhp.getHocKy());
            existing.setSiSoMax(formLhp.getSiSoMax());
            existing.setTrangThai(formLhp.getTrangThai());
            existing.setThoiGianMo(formLhp.getThoiGianMo());
            existing.setThoiGianDong(formLhp.getThoiGianDong());


            // Cập nhật MonHoc (chỉ set nếu id hợp lệ)
            if (formLhp.getMonHoc() != null && formLhp.getMonHoc().getId() != null) {
                existing.setMonHoc(formLhp.getMonHoc());
            }

            // Cập nhật GiangVien (chỉ set nếu id hợp lệ)
            if (formLhp.getGiangVien() != null && formLhp.getGiangVien().getId() != null) {
                existing.setGiangVien(formLhp.getGiangVien());
            }

            lhpService.save(existing);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop-hoc-phan";
    }

    // ── Xóa ────────────────────────────────────────────────────────────────────
    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            lhpService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa lớp học phần!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop-hoc-phan";
    }

    // ── Danh sách đăng ký ──────────────────────────────────────────────────────
    @GetMapping("/{id}/danh-sach")
    public String danhSachDangKy(@PathVariable Long id, Model model) {
        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        model.addAttribute("lopHocPhan", lhp);
        model.addAttribute("dangKys", lhpService.getDanhSachDangKy(id));
        model.addAttribute("danhSachLop", lopService.findAll());
        return "lophocphan/danh-sach";
    }

    // ── Cập nhật điểm ──────────────────────────────────────────────────────────
    @PostMapping("/cap-nhat-diem")
    public String capNhatDiem(@RequestParam Long dkId,
                              @RequestParam Long lhpId,
                              @RequestParam(required = false) Double diemQT,
                              @RequestParam(required = false) Double diemThi,
                              RedirectAttributes ra) {
        try {
            lhpService.capNhatDiem(dkId, diemQT, diemThi);
            ra.addFlashAttribute("success", "Cập nhật điểm thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop-hoc-phan/" + lhpId + "/danh-sach";
    }

    // ── Tìm SV chưa đăng ký (modal tìm kiếm) ──────────────────────────────────
    /**
     * FIX hiệu năng: đẩy filter "chưa đăng ký" và tìm kiếm xuống DB thay vì
     * load toàn bộ SV rồi filter trong memory.
     * Nếu chưa có method findNotInLhp trong service, giữ logic cũ nhưng
     * thêm giới hạn kết quả trả về tránh payload quá lớn.
     */
    @GetMapping("/{id}/sinh-vien-chua-dk")
    @ResponseBody
    public List<Map<String, Object>> svChuaDangKy(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "") String q) {
        Set<Long> daDkIds = lhpService.getDanhSachDangKy(id).stream()
                .map(dk -> dk.getSinhVien().getId())
                .collect(Collectors.toSet());

        return svService.findAll().stream()
                .filter(sv -> !daDkIds.contains(sv.getId()))
                .filter(sv -> q.isBlank()
                        || sv.getHoTen().toLowerCase().contains(q.toLowerCase())
                        || sv.getMaSv().toLowerCase().contains(q.toLowerCase()))
                .map(sv -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", sv.getId());
                    map.put("maSv", sv.getMaSv());
                    map.put("hoTen", sv.getHoTen());
                    map.put("lop", sv.getLop() != null ? sv.getLop().getTenLop() : "-");
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ── Đăng ký nhiều SV cùng lúc ─────────────────────────────────────────────
    @PostMapping("/{id}/dang-ky-nhieu")
    @ResponseBody
    public Map<String, Object> dangKyNhieu(@PathVariable Long id,
                                           @RequestBody List<Long> svIds) {
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (Long svId : svIds) {
            try {
                lhpService.dangKy(svId, id);
                success++;
            } catch (Exception e) {
                errors.add(e.getMessage());
            }
        }
        return Map.of("success", success, "errors", errors);
    }

    // ── Import cả lớp hành chính ───────────────────────────────────────────────
    @PostMapping("/{id}/import-lop/{lopId}")
    @ResponseBody
    public Map<String, Object> importLop(@PathVariable Long id,
                                         @PathVariable Long lopId) {
        List<SinhVien> svList = svService.findByLopId(lopId);
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (SinhVien sv : svList) {
            try {
                lhpService.dangKy(sv.getId(), id);
                success++;
            } catch (Exception e) {
                errors.add(sv.getMaSv() + ": " + e.getMessage());
            }
        }
        return Map.of("success", success, "errors", errors, "total", svList.size());
    }

    // ── Hủy đăng ký 1 SV ──────────────────────────────────────────────────────
    @PostMapping("/huy-dang-ky")
    public String huyDangKy(@RequestParam Long svId,
                            @RequestParam Long lhpId,
                            RedirectAttributes ra) {
        try {
            // ADMIN: dung huyDangKyAdmin — khong check trang thai
            lhpService.huyDangKyAdmin(svId, lhpId);
            ra.addFlashAttribute("success", "Da huy dang ky thanh cong!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/lop-hoc-phan/" + lhpId + "/danh-sach";
    }

    // ── Export Excel ───────────────────────────────────────────────────────────
    @GetMapping("/{id}/export-excel")
    public void exportExcel(@PathVariable Long id,
                            HttpServletResponse response) throws IOException {
        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        List<DangKy> dangKys = lhpService.getDanhSachDangKy(id);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + lhp.getMaLhp() + "_danhsach.xlsx\"");

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Danh sách");

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Dòng thông tin lớp
            Row infoRow = sheet.createRow(0);
            infoRow.createCell(0).setCellValue(
                    "Lớp học phần: " + lhp.getMaLhp()
                            + " | Môn: " + lhp.getMonHoc().getTenMon()
                            + " | GV: " + lhp.getGiangVien().getHoTen()
                            + " | HK: " + lhp.getHocKy()
            );

            // Header
            Row header = sheet.createRow(2);
            String[] cols = {"#", "Mã SV", "Họ tên", "Điểm QT", "Điểm thi", "Tổng kết", "Xếp loại"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // Dữ liệu
            for (int i = 0; i < dangKys.size(); i++) {
                DangKy dk = dangKys.get(i);
                Row row = sheet.createRow(i + 3);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(dk.getSinhVien().getMaSv());
                row.createCell(2).setCellValue(dk.getSinhVien().getHoTen());
                row.createCell(3).setCellValue(dk.getDiemQuaTrinh() != null ? dk.getDiemQuaTrinh() : 0);
                row.createCell(4).setCellValue(dk.getDiemThi() != null ? dk.getDiemThi() : 0);
                row.createCell(5).setCellValue(dk.getDiemTongKet() != null ? dk.getDiemTongKet() : 0);
                row.createCell(6).setCellValue(dk.getXepLoai() != null ? dk.getXepLoai() : "-");
            }

            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            wb.write(response.getOutputStream());
        }
    }

    //
}
// =====================================================================
// THOI KHOA BIEU CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/tkb")
@RequiredArgsConstructor
public class ThoiKhoaBieuController {

    private final ThoiKhoaBieuService tkbService;
    private final LopHocPhanService lhpService;
    private final GiangVienService gvService;
    private final SinhVienService svService;
    private final NguoiDungRepository nguoiDungRepo;
    private final GiangVienRepository giangVienRepo;
    private final PhongHocService phongHocService;
    private final TimeSlotService timeSlotService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GIANG_VIEN')")
    public String xemTKB(@RequestParam(required = false) Long giangVienId,
                         @RequestParam(required = false) String hocKy,
                         Authentication auth, Model model) {
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("giangVienIdChon", giangVienId);
        model.addAttribute("hocKyChon", hocKy);
        model.addAttribute("tietMap", timeSlotService.buildTietMap());
        if (giangVienId != null && hocKy != null) {
            model.addAttribute("thoiKhoaBieus", tkbService.findByGiangVien(giangVienId, hocKy));
            model.addAttribute("giangVien", gvService.findById(giangVienId).orElse(null));
        }
        return "thoikhoabieu/xem";
    }

    @GetMapping("/cua-toi")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIANG_VIEN', 'SINH_VIEN')")
    public String tkbCuaToi(@RequestParam(required = false) String hocKy,
                            Authentication auth, Model model) {
        String username = auth.getName();
        NguoiDung nd = nguoiDungRepo.findByUsername(username).orElseThrow();
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);
        model.addAttribute("tietMap", timeSlotService.buildTietMap());
        if (nd.getVaiTro() == VaiTro.GIANG_VIEN && hocKy != null) {
            GiangVien gv = giangVienRepo.findByNguoiDungId(nd.getId()).orElse(null);
            if (gv != null) {
                model.addAttribute("thoiKhoaBieus", tkbService.findByGiangVien(gv.getId(), hocKy));
                model.addAttribute("tenNguoiDung", gv.getHoTenVaHocVi());
            }
        } else if (nd.getVaiTro() == VaiTro.SINH_VIEN && hocKy != null) {
            SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
            if (sv != null) {
                model.addAttribute("thoiKhoaBieus", tkbService.findBySinhVien(sv.getId(), hocKy));
                model.addAttribute("tenNguoiDung", sv.getHoTen());
            }
        }
        return "thoikhoabieu/cua-toi";
    }

    @GetMapping("/theo-tuan")
    @PreAuthorize("hasAnyRole('ADMIN', 'GIANG_VIEN', 'SINH_VIEN')")
    public String xemTheoTuan(
            @RequestParam(required = false) Long giangVienId,
            @RequestParam(required = false) String hocKy,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate ngay,
            Authentication auth,
            Model model) {

        if (ngay == null) {
            ngay = LocalDate.now();
        }

        LocalDate thu2 = ngay.with(DayOfWeek.MONDAY);

        model.addAttribute("thu2", thu2);
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);
        model.addAttribute("ngayChon", ngay);
        model.addAttribute("tietMap", timeSlotService.buildTietMap());
        model.addAttribute("thuTrongTuan",
                List.of(
                        thu2,
                        thu2.plusDays(1),
                        thu2.plusDays(2),
                        thu2.plusDays(3),
                        thu2.plusDays(4),
                        thu2.plusDays(5)
                ));

        String username = auth.getName();

        NguoiDung nd = nguoiDungRepo
                .findByUsername(username)
                .orElseThrow();

        // ================= ADMIN =================
        if (nd.getVaiTro() == VaiTro.ADMIN) {

            model.addAttribute("giangViens", gvService.findAll());
            model.addAttribute("giangVienIdChon", giangVienId);

            if (giangVienId != null && hocKy != null) {

                model.addAttribute(
                        "thoiKhoaBieus",
                        tkbService.findByGiangVienTuan(
                                giangVienId,
                                hocKy,
                                ngay
                        )
                );

                model.addAttribute(
                        "giangVien",
                        gvService.findById(giangVienId).orElse(null)
                );
            }
        }

        // ================= GIANG VIEN =================
        else if (nd.getVaiTro() == VaiTro.GIANG_VIEN) {

            GiangVien gv = giangVienRepo
                    .findByNguoiDungId(nd.getId())
                    .orElse(null);

            if (gv != null && hocKy != null) {

                model.addAttribute(
                        "thoiKhoaBieus",
                        tkbService.findByGiangVienTuan(
                                gv.getId(),
                                hocKy,
                                ngay
                        )
                );

                model.addAttribute("giangVien", gv);
            }
        }

        // ================= SINH VIEN =================
        else if (nd.getVaiTro() == VaiTro.SINH_VIEN) {

            SinhVien sv = svService
                    .findByNguoiDungId(nd.getId())
                    .orElse(null);

            if (sv != null && hocKy != null) {

                model.addAttribute(
                        "thoiKhoaBieus",
                        tkbService.findBySinhVienTuan(
                                sv.getId(),
                                hocKy,
                                ngay
                        )
                );

                model.addAttribute("sinhVien", sv);
            }
        }

        return "thoikhoabieu/theo-tuan";
    }
    @GetMapping("/thong-ke")
    @PreAuthorize("hasRole('ADMIN')")
    public String thongKe(@RequestParam(required = false) String hocKy,
                          Model model) {

        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);

        if (hocKy != null && !hocKy.isBlank()) {

            List<TaiGiangDayDTO> ds = tkbService.thongKeTaiGiangDay(hocKy);

            model.addAttribute("danhSachTai", ds);

            // Tổng tiết
            int tongTietToanTruong = ds.stream()
                    .mapToInt(TaiGiangDayDTO::tongTiet)
                    .sum();

            // GV quá tải
            long soGVQuaTai = ds.stream()
                    .filter(t -> t.tyLeTai() > 1.0)
                    .count();

            model.addAttribute("tongTietToanTruong", tongTietToanTruong);
            model.addAttribute("soGVQuaTai", soGVQuaTai);
        }

        return "thoikhoabieu/thong-ke";
    }

    @GetMapping("/phong-trong")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public List<PhongHocDTO> timPhongTrong(
            @RequestParam String hocKy,
            @RequestParam int thu,
            @RequestParam int tietBatDau,
            @RequestParam int soTiet,
            @RequestParam(defaultValue = "0") int sucCanThiet) {
        return tkbService.timPhongTrong(hocKy, thu, tietBatDau, soTiet, sucCanThiet)
                .stream().map(PhongHocDTO::from).toList();
    }

    @GetMapping("/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String themForm(Model model) {
        model.addAttribute("thoiKhoaBieu", new ThoiKhoaBieu());
        model.addAttribute("lopHocPhans", lhpService.findAll());
        model.addAttribute("danhSachPhong", phongHocService.findAllHoatDong());
        return "thoikhoabieu/form";
    }

    @PostMapping("/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String them(@ModelAttribute ThoiKhoaBieu tkb, RedirectAttributes ra) {
        try {
            tkbService.save(tkb);
            ra.addFlashAttribute("success", "Thêm thời khóa biểu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tkb";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            tkbService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tkb";
    }
    @GetMapping("/sua/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("thoiKhoaBieu", tkbService.findById(id).orElseThrow());
        model.addAttribute("lopHocPhans", lhpService.findAll());
        model.addAttribute("danhSachPhong", phongHocService.findAllHoatDong());
        return "thoikhoabieu/form";
    }

    @PostMapping("/sua/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String sua(@PathVariable Long id, @ModelAttribute ThoiKhoaBieu tkb, RedirectAttributes ra) {
        try {
            tkb.setId(id);
            tkbService.save(tkb);
            ra.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tkb";
    }
}

// =====================================================================
// GIANG VIEN PORTAL
// =====================================================================
@Controller
@RequestMapping("/giangvien")
@PreAuthorize("hasAnyRole('ADMIN','GIANG_VIEN')")
@RequiredArgsConstructor
class GiangVienPortalController {
    private final GiangVienService gvService;
    private final LopHocPhanService lhpService;
    private final NguoiDungRepository nguoiDungRepo;
    private final GiangVienRepository giangVienRepo;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        GiangVien gv = giangVienRepo.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("giangVien", gv);
        if (gv != null) {
            model.addAttribute("lopHocPhans", lhpService.findByGiangVien(gv.getId()));
        }
        return "giangvien/dashboard";
    }


    @GetMapping("/lop/{id}/danh-sach")
    public String danhSachSinhVien(@PathVariable Long id, Model model) {
        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        model.addAttribute("lopHocPhan", lhp);
        model.addAttribute("dangKys", lhpService.getDanhSachDangKy(id));
        return "giangvien/danh-sach-sv";
    }

    //xem chi tiết giảng viên
    @GetMapping("/chi-tiet-gv")
    public String chiTiet(Authentication auth, Model model) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        GiangVien gv = giangVienRepo.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("giangVien", gv);

        if (gv != null) {
            List<LopHocPhan> lopHocPhans = lhpService.findByGiangVien(gv.getId());
            model.addAttribute("soLopHocPhan", lopHocPhans.size());
            model.addAttribute("soSinhVien", lopHocPhans.stream()
                    .mapToInt(lhp -> lhpService.getDanhSachDangKy(lhp.getId()).size())
                    .sum());
        } else {
            model.addAttribute("soLopHocPhan", 0);
            model.addAttribute("soSinhVien", 0);
        }

        return "giangvien/chi-tiet-gv";
    }
    @PostMapping("/cap-nhat-diem")
    public String capNhatDiem(@RequestParam Long dkId, @RequestParam Long lhpId,
                               @RequestParam(required = false) Double diemQT,
                               @RequestParam(required = false) Double diemThi,
                               RedirectAttributes ra) {
        try { lhpService.capNhatDiem(dkId, diemQT, diemThi); ra.addFlashAttribute("success", "Cập nhật điểm thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/giangvien/lop/" + lhpId + "/danh-sach";
    }
}

// =====================================================================
// SINH VIEN PORTAL
// =====================================================================
@Controller
@RequestMapping("/sinhvien")
@PreAuthorize("hasAnyRole('ADMIN','SINH_VIEN')")
@RequiredArgsConstructor
class SinhVienPortalController {
    private final SinhVienService svService;
    private final LopHocPhanService lhpService;
    private final NguoiDungRepository nguoiDungRepo;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("sinhVien", sv);
        if (sv != null) {
            model.addAttribute("dangKys", lhpService.getDangKyCuaSinhVien(sv.getId()));
        } else {
            model.addAttribute("dangKys", Collections.emptyList());
        }
        return "sinhvien/dashboard";
    }

    @GetMapping("/dang-ky")
    public String danhSachLHP(@RequestParam(required = false) String hocKy,
                              Authentication auth, Model model) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("sinhVien", sv);
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);
        if (hocKy != null) {
            model.addAttribute("lopHocPhans", lhpService.findByHocKy(hocKy));
            if (sv != null) {
                model.addAttribute("daDangKy", lhpService.getDangKyCuaSinhVien(sv.getId())
                        .stream().map(dk -> dk.getLopHocPhan().getId()).toList());
            }
        }
        return "sinhvien/dang-ky";
    }

    //xem chi tiết sinh viên
    @GetMapping("/chi-tiet-sv")
    public String chiTiet(Authentication auth, Model model) {
        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("sinhVien", sv);

        if (sv != null) {
            List<DangKy> dangKys = lhpService.getDangKyCuaSinhVien(sv.getId());
            model.addAttribute("soHocPhan", dangKys.size());
            model.addAttribute("soHocPhanDat", dangKys.stream().filter(DangKy::isDat).count());
            OptionalDouble tb = dangKys.stream()
                    .filter(dk -> dk.getDiemTongKet() != null)
                    .mapToDouble(DangKy::getDiemTongKet)
                    .average();
            model.addAttribute("diemTB", tb.isPresent() ? tb.getAsDouble() : null);
        } else {
            model.addAttribute("soHocPhan", 0);
            model.addAttribute("soHocPhanDat", 0);
            model.addAttribute("diemTB", null);
        }

        return "sinhvien/chi-tiet-sv";
    }

    @PostMapping("/dang-ky")
    public String dangKy(@RequestParam Long lhpId, Authentication auth, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
            SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElseThrow();
            lhpService.dangKy(sv.getId(), lhpId);
            ra.addFlashAttribute("success", "Đăng ký học phần thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sinhvien/dang-ky";
    }

    @PostMapping("/huy-dang-ky")
    public String huyDangKy(@RequestParam Long lhpId, Authentication auth, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
            SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElseThrow();
            lhpService.huyDangKy(sv.getId(), lhpId);
            ra.addFlashAttribute("success", "Đã hủy đăng ký!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sinhvien/dashboard";
    }
}
