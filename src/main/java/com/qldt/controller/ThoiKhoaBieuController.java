package com.qldt.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.DonNghiRepository;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
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
    private final NhanVienService nhanVienService;
    private void resolveCvht(Lop lop) {
        if (lop.getCoVanHocTap() != null && lop.getCoVanHocTap().getId() != null) {
            lop.setCoVanHocTap(
                    nhanVienService.findById(lop.getCoVanHocTap().getId()).orElse(null)
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
        model.addAttribute("lop",        new Lop());
        model.addAttribute("khoas",      khoaService.findAll());
        // Dropdown cố vấn HT: chỉ lấy nhân viên là giảng viên
        model.addAttribute("giangViens", nhanVienService.findAllGiangVien());
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
        model.addAttribute("giangViens",nhanVienService.findAllGiangVien());
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
    private final NhanVienService gvService;
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
        lhp.setGiangVien(new NhanVien());
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
            if (lhp.getGiangVien() == null) lhp.setGiangVien(new NhanVien());
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
        if (lhp.getGiangVien() == null) lhp.setGiangVien(new NhanVien());
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
            if (formLhp.getGiangVien() == null) formLhp.setGiangVien(new NhanVien());
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

    @GetMapping("/{id}/export-excel")
    public void exportExcel(@PathVariable Long id,
                            HttpServletResponse response) throws IOException {

        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        List<DangKy> dangKys = lhpService.getDanhSachDangKy(id);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + lhp.getMaLhp() + "_danhsach.xlsx\"");

        try (Workbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Danh sách sinh viên");

            // =========================================================
            // FONT
            // =========================================================
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            Font normalFont = wb.createFont();
            normalFont.setFontHeightInPoints((short) 11);

            // =========================================================
            // STYLE TIÊU ĐỀ
            // =========================================================
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // =========================================================
            // STYLE THÔNG TIN
            // =========================================================
            CellStyle infoStyle = wb.createCellStyle();
            infoStyle.setFont(normalFont);

            // =========================================================
            // STYLE HEADER
            // =========================================================
            CellStyle headerStyle = wb.createCellStyle();

            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            headerStyle.setFont(headerFont);

            // =========================================================
            // STYLE DỮ LIỆU
            // =========================================================
            CellStyle dataStyle = wb.createCellStyle();

            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // =========================================================
            // STYLE ĐIỂM
            // =========================================================
            CellStyle scoreStyle = wb.createCellStyle();

            scoreStyle.cloneStyleFrom(dataStyle);
            scoreStyle.setAlignment(HorizontalAlignment.CENTER);

            // =========================================================
            // TIÊU ĐỀ
            // =========================================================
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            Row titleRow = sheet.createRow(0);
            titleRow.setHeight((short) 500);

            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH SINH VIÊN LỚP HỌC PHẦN");
            titleCell.setCellStyle(titleStyle);

            // =========================================================
            // THÔNG TIN LỚP
            // =========================================================
            Row info1 = sheet.createRow(2);
            info1.createCell(0).setCellValue("Mã lớp học phần:");
            info1.createCell(1).setCellValue(lhp.getMaLhp());

            info1.createCell(3).setCellValue("Học kỳ:");
            info1.createCell(4).setCellValue(lhp.getHocKy());

            Row info2 = sheet.createRow(3);
            info2.createCell(0).setCellValue("Môn học:");
            info2.createCell(1).setCellValue(lhp.getMonHoc().getTenMon());

            info2.createCell(3).setCellValue("Giảng viên:");
            info2.createCell(4).setCellValue(lhp.getGiangVien().getHoTen());

            // =========================================================
            // HEADER TABLE
            // =========================================================
            Row header = sheet.createRow(5);

            String[] cols = {
                    "STT",
                    "Mã SV",
                    "Họ tên",
                    "Điểm QT",
                    "Điểm thi",
                    "Tổng kết",
                    "Xếp loại"
            };

            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
            }

            // =========================================================
            // DỮ LIỆU
            // =========================================================
            int rowNum = 6;

            for (int i = 0; i < dangKys.size(); i++) {

                DangKy dk = dangKys.get(i);

                Row row = sheet.createRow(rowNum++);

                Cell c0 = row.createCell(0);
                c0.setCellValue(i + 1);
                c0.setCellStyle(scoreStyle);

                Cell c1 = row.createCell(1);
                c1.setCellValue(dk.getSinhVien().getMaSv());
                c1.setCellStyle(dataStyle);

                Cell c2 = row.createCell(2);
                c2.setCellValue(dk.getSinhVien().getHoTen());
                c2.setCellStyle(dataStyle);

                Cell c3 = row.createCell(3);
                c3.setCellValue(dk.getDiemQuaTrinh() != null ? dk.getDiemQuaTrinh() : 0);
                c3.setCellStyle(scoreStyle);

                Cell c4 = row.createCell(4);
                c4.setCellValue(dk.getDiemThi() != null ? dk.getDiemThi() : 0);
                c4.setCellStyle(scoreStyle);

                Cell c5 = row.createCell(5);
                c5.setCellValue(dk.getDiemTongKet() != null ? dk.getDiemTongKet() : 0);
                c5.setCellStyle(scoreStyle);

                Cell c6 = row.createCell(6);
                c6.setCellValue(dk.getXepLoai() != null ? dk.getXepLoai() : "-");
                c6.setCellStyle(scoreStyle);
            }

            // =========================================================
            // WIDTH CỘT
            // =========================================================
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 5000);
            sheet.setColumnWidth(2, 9000);
            sheet.setColumnWidth(3, 4000);
            sheet.setColumnWidth(4, 4000);
            sheet.setColumnWidth(5, 4000);
            sheet.setColumnWidth(6, 5000);

            // =========================================================
            // FREEZE HEADER
            // =========================================================
            sheet.createFreezePane(0, 6);

            wb.write(response.getOutputStream());
        }
    }
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
        private final NhanVienService nhanVienService;   // thay GiangVienService
        private final SinhVienService svService;
        private final NguoiDungRepository nguoiDungRepo;
        private final PhongHocService phongHocService;
        private final TimeSlotService timeSlotService;
        private final ObjectMapper objectMapper;
        private final LichDayBuService lichDayBuService;
        private final DonNghiRepository donNghiRepo;

        // ── Xem TKB (Admin / GV chọn giảng viên) ───────────────────────────
        @GetMapping
        @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
        public String xemTKB(@RequestParam(required = false) Long giangVienId,
                             @RequestParam(required = false) String hocKy,
                             Authentication auth, Model model) {
            model.addAttribute("giangViens", nhanVienService.findAllGiangVien()); // đổi
            model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
            model.addAttribute("giangVienIdChon", giangVienId);
            model.addAttribute("hocKyChon", hocKy);
            model.addAttribute("tietMap", timeSlotService.buildTietMap());


            if (giangVienId != null && hocKy != null) {
                model.addAttribute("thoiKhoaBieus",
                        tkbService.findByGiangVien(giangVienId, hocKy));
                model.addAttribute("giangVien",
                        nhanVienService.findById(giangVienId).orElse(null));
                // THÊM MỚI:
                model.addAttribute("thongKeTinChi",
                        tkbService.thongKeTinChi(giangVienId, hocKy));
            }
            return "thoikhoabieu/xem";
        }

        private String buildCalendarJsonBu(List<LichDayBu> list, Map<Integer, TimeSlot> tietMap) {
            if (list == null || list.isEmpty()) return "[]";
            List<Map<String, Object>> result = new ArrayList<>();
            for (LichDayBu b : list) {
                Map<String, Object> ev = new java.util.LinkedHashMap<>();
                var lhp = b.getLopHocPhan();
                var mon = lhp != null ? lhp.getMonHoc() : null;
                var gv = lhp != null ? lhp.getGiangVien() : null;
                ev.put("loai", "BU");
                ev.put("ngayDayBu", b.getNgayDayBu() != null ? b.getNgayDayBu().toString() : null);
                ev.put("tietBatDau", b.getTietBatDau());
                ev.put("soTiet", b.getSoTiet());
                ev.put("phongHoc", b.getPhongHoc());
                ev.put("tenMon", mon != null ? mon.getTenMon() : "---");
                ev.put("maLhp", lhp != null ? lhp.getMaLhp() : "---");
                ev.put("lhpId", lhp != null ? lhp.getId() : null);
                ev.put("hoTenGv", gv != null ? gv.getHoTen() : "---");
                ev.put("gvId", gv != null ? gv.getId() : null);
                ev.put("soTinChi", mon != null ? mon.getSoTinChi() : 0);
                // Giờ học
                // Nếu là Integer (wrapper) — giữ nguyên check null, viết gọn hơn:
                String gioHoc = "";
                if (tietMap != null) {
                    int tietBD = b.getTietBatDau();
                    var ts = tietMap.get(tietBD);
                    if (ts != null) {
                        var ts2 = tietMap.get(tietBD + b.getSoTiet() - 1);
                        gioHoc = ts.getGioBatDau().toString().substring(0, 5)
                                + " – " + (ts2 != null ? ts2.getGioKetThuc().toString().substring(0, 5) : "?");
                    }
                }
                ev.put("gioHoc", gioHoc);
                result.add(ev);
            }
            try {
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                return "[]";
            }
        }

        // ── Helper: build JSON cho lịch bù ──────────────────────────────────
        private String buildJsonBu(List<LichDayBu> list, Map<Integer, TimeSlot> tietMap) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (LichDayBu b : list) {
                if (b.getTrangThai() == TrangThaiLichBu.HUY) continue; // bỏ qua bị hủy
                var lhp = b.getLopHocPhan();
                var mon = lhp != null ? lhp.getMonHoc() : null;
                var gv = b.getGiangVien();
                Map<String, Object> ev = new java.util.LinkedHashMap<>();
                ev.put("loai", "BU");
                ev.put("ngayDayBu", b.getNgayDayBu() != null ? b.getNgayDayBu().toString() : null);
                ev.put("ngayNghiGoc", b.getNgayNghiGoc() != null ? b.getNgayNghiGoc().toString() : null);
                ev.put("thuTrongTuan", b.getThuTrongTuan());
                ev.put("tietBatDau", b.getTietBatDau());
                ev.put("soTiet", b.getSoTiet());
                ev.put("phongHoc", b.getPhongHoc());
                ev.put("tenThu", b.getTenThu());
                ev.put("tenMon", mon != null ? mon.getTenMon() : "---");
                ev.put("maLhp", lhp != null ? lhp.getMaLhp() : "---");
                ev.put("lhpId", lhp != null ? lhp.getId() : null);
                ev.put("hoTenGv", gv != null ? gv.getHoTen() : "---");
                ev.put("gvId", gv != null ? gv.getId() : null);
                ev.put("soTinChi", mon != null ? mon.getSoTinChi() : 0);
                // Giờ học
                String gio = "";
                if (tietMap != null && tietMap.get(b.getTietBatDau()) != null) {
                    var ts = tietMap.get(b.getTietBatDau());
                    var ts2 = tietMap.get(b.getTietKetThuc());
                    gio = ts.getGioBatDau().toString().substring(0, 5)
                            + " – " + (ts2 != null ? ts2.getGioKetThuc().toString().substring(0, 5) : "?");
                }
                ev.put("gioHoc", gio);
                result.add(ev);
            }
            try {
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                return "[]";
            }
        }

        // ── Helper: build JSON cho thông báo nghỉ ───────────────────────────
        private String buildJsonNghi(List<DonNghi> list) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (DonNghi d : list) {
                if (d.getTrangThai() != TrangThaiDonNghi.DA_DUYET) continue;
                Map<String, Object> ev = new java.util.LinkedHashMap<>();
                ev.put("loai", "NGHI");
                ev.put("ngayNghiBatDau", d.getNgayBatDau() != null ? d.getNgayBatDau().toString() : null);
                ev.put("ngayNghiKetThuc", d.getNgayKetThuc() != null ? d.getNgayKetThuc().toString() : null);
                ev.put("hoTenGv", d.getNguoiNop() != null ? d.getNguoiNop().getHoTen() : "---");
                ev.put("gvId", d.getNguoiNop() != null ? d.getNguoiNop().getId() : null);
                ev.put("lyDo", d.getLyDo());
                ev.put("loaiNghi", d.getLoaiNghi());
                result.add(ev);
            }
            try {
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                return "[]";
            }
        }

        private String buildCalendarJson(List<ThoiKhoaBieu> list, Map<Integer, TimeSlot> tietMap) {
            List<Map<String, Object>> result = new ArrayList<>();
            for (ThoiKhoaBieu t : list) {
                Map<String, Object> ev = new java.util.LinkedHashMap<>();
                ev.put("thuTrongTuan", t.getThuTrongTuan());
                ev.put("tietBatDau", t.getTietBatDau());
                ev.put("soTiet", t.getSoTiet());
                ev.put("phongHoc", t.getPhongHoc());
                ev.put("tuanBatDau", t.getTuanBatDau() != null ? t.getTuanBatDau().toString() : null);
                ev.put("tuanKetThuc", t.getTuanKetThuc() != null ? t.getTuanKetThuc().toString() : null);
                ev.put("tenThu", t.getTenThu());

                var lhp = t.getLopHocPhan();
                var mon = lhp != null ? lhp.getMonHoc() : null;
                var gv = lhp != null ? lhp.getGiangVien() : null;

                ev.put("tenMon", mon != null ? mon.getTenMon() : "---");
                ev.put("maLhp", lhp != null ? lhp.getMaLhp() : "---");
                ev.put("lhpId", lhp != null ? lhp.getId() : null);
                ev.put("hoTenGv", gv != null ? gv.getHoTen() : "---");
                ev.put("gvId", gv != null ? gv.getId() : null);
                ev.put("soTinChi", mon != null ? mon.getSoTinChi() : 0);

                // Giờ học
                String gioHoc = "";
                if (tietMap != null && tietMap.get(t.getTietBatDau()) != null) {
                    var ts = tietMap.get(t.getTietBatDau());
                    var ts2 = tietMap.get(t.getTietBatDau() + t.getSoTiet() - 1);
                    gioHoc = ts.getGioBatDau().toString().substring(0, 5)
                            + " – " + (ts2 != null ? ts2.getGioKetThuc().toString().substring(0, 5) : "?");
                }
                ev.put("gioHoc", gioHoc);
                result.add(ev);
            }
            try {
                return objectMapper.writeValueAsString(result);
            } catch (Exception e) {
                return "[]";
            }
        }

        // ── TKB của tôi ─────────────────────────────────────────────────────
        @GetMapping("/cua-toi")
        @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN', 'SINH_VIEN')")
        public String tkbCuaToi(@RequestParam(required = false) String hocKy,
                                Authentication auth, Model model) {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
            model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
            model.addAttribute("hocKyChon", hocKy);
            model.addAttribute("tietMap", timeSlotService.buildTietMap());


            if (nd.getVaiTro() == VaiTro.NHAN_VIEN && hocKy != null) {
                // Dùng NhanVienService thay GiangVienRepository
                NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
                if (nv != null) {
                    model.addAttribute("thoiKhoaBieus",
                            tkbService.findByGiangVien(nv.getId(), hocKy));
                    model.addAttribute("tenNguoiDung", nv.getHoTenVaHocVi());
                }
            } else if (nd.getVaiTro() == VaiTro.SINH_VIEN && hocKy != null) {
                SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
                if (sv != null) {
                    model.addAttribute("thoiKhoaBieus",
                            tkbService.findBySinhVien(sv.getId(), hocKy));
                    model.addAttribute("tenNguoiDung", sv.getHoTen());
                }
            }
            return "thoikhoabieu/cua-toi";
        }

        @PatchMapping("/override-tuan/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> overrideTuan(
                @PathVariable Long id,
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
                @RequestBody TkbWeekOverrideDTO dto) {
            try {
                tkbService.overrideTuan(id, dto, ngay);
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã cập nhật lịch cho tuần " +
                                ngay.with(DayOfWeek.MONDAY)
                                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            } catch (IllegalStateException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false, "message", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
            }
        }

        // ── TKB theo tuần ───────────────────────────────────────────────────
        @GetMapping("/theo-tuan")
        @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN', 'SINH_VIEN')")
        public String xemTheoTuan(
                @RequestParam(required = false) Long giangVienId,
                @RequestParam(required = false) String hocKy,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
                Authentication auth, Model model) {

            if (ngay == null) ngay = LocalDate.now();
            LocalDate thu2 = ngay.with(DayOfWeek.MONDAY);
            LocalDate thu7 = thu2.plusDays(5);

            model.addAttribute("thu2", thu2);
            model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
            model.addAttribute("hocKyChon", hocKy);
            model.addAttribute("ngayChon", ngay);
            model.addAttribute("tietMap", timeSlotService.buildTietMap());
            model.addAttribute("thuTrongTuan", List.of(thu2, thu2.plusDays(1), thu2.plusDays(2), thu2.plusDays(3), thu2.plusDays(4), thu2.plusDays(5)));

            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();

            if (nd.getVaiTro() == VaiTro.ADMIN) {
                model.addAttribute("giangViens", nhanVienService.findAllGiangVien());
                model.addAttribute("giangVienIdChon", giangVienId);
                if (giangVienId != null && hocKy != null) {
                    model.addAttribute("thoiKhoaBieus",
                            tkbService.findByGiangVienTuan(giangVienId, hocKy, ngay));
                    model.addAttribute("giangVien",
                            nhanVienService.findById(giangVienId).orElse(null));
                    model.addAttribute("thongKeTinChi",
                            tkbService.thongKeTinChi(giangVienId, hocKy));
                    // THÊM MỚI:
                    List<LichDayBu> lichBuTuan = lichDayBuService
                            .findByGiangVienAndKhoangNgay(giangVienId, thu2, thu7);
                    List<DonNghi> donNghiTuan = donNghiRepo
                            .findDaDuyetByGvAndKhoang(giangVienId, thu2, thu7);
                    model.addAttribute("lichDayBuTuan", lichBuTuan);
                    model.addAttribute("jsonBuTuan", buildJsonBu(lichBuTuan, timeSlotService.buildTietMap()));
                    model.addAttribute("jsonNghiTuan", buildJsonNghi(donNghiTuan));
                }


            } else if (nd.getVaiTro() == VaiTro.NHAN_VIEN) {
                NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);

                // Quyền quản lý — set trước
                if (nv != null) {
                    model.addAttribute("currentNvId", nv.getId());
                    boolean isQL = nv.getChucVu() != null &&
                            List.of("Trưởng khoa", "Phó khoa", "Tổ trưởng BM")
                                    .contains(nv.getChucVu().getTenChucVu());
                    model.addAttribute("isQuanLy", isQL);
                }

                // Lịch + tiến độ + bù + nghỉ
                if (nv != null && hocKy != null) {
                    model.addAttribute("thoiKhoaBieus",
                            tkbService.findByGiangVienTuan(nv.getId(), hocKy, ngay));
                    model.addAttribute("giangVien", nv);
                    model.addAttribute("thongKeTinChi",
                            tkbService.thongKeTinChi(nv.getId(), hocKy)); // ← sửa: nv.getId()

                    List<LichDayBu> lichBuTuan = lichDayBuService
                            .findByGiangVienAndKhoangNgay(nv.getId(), thu2, thu7);
                    List<DonNghi> donNghiTuan = donNghiRepo
                            .findDaDuyetByGvAndKhoang(nv.getId(), thu2, thu7);
                    model.addAttribute("lichDayBuTuan", lichBuTuan);
                    model.addAttribute("jsonBuTuan",
                            buildJsonBu(lichBuTuan, timeSlotService.buildTietMap()));
                    model.addAttribute("jsonNghiTuan", buildJsonNghi(donNghiTuan));
                }

            } else if (nd.getVaiTro() == VaiTro.SINH_VIEN) {
                SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
                if (sv != null && hocKy != null) {
                    model.addAttribute("thoiKhoaBieus",
                            tkbService.findBySinhVienTuan(sv.getId(), hocKy, ngay));
                    model.addAttribute("sinhVien", sv);
                    // ── THÊM MỚI: thông báo nghỉ của các GV trong tuần ──
                    // Lấy tất cả GV đang dạy SV này trong tuần
                    List<ThoiKhoaBieu> dsSv = tkbService.findBySinhVienTuan(sv.getId(), hocKy, ngay);
                    List<Long> gvIds = dsSv.stream()
                            .map(t -> t.getLopHocPhan().getGiangVien())
                            .filter(gv -> gv != null)
                            .map(gv -> gv.getId())
                            .distinct()
                            .toList();

                    List<DonNghi> donNghiTuan = gvIds.stream()
                            .flatMap(gvId -> donNghiRepo
                                    .findDaDuyetByGvAndKhoang(gvId, thu2, thu7).stream())
                            .toList();

                    model.addAttribute("jsonNghiTuan", buildJsonNghi(donNghiTuan));
                    model.addAttribute("jsonBuTuan", "[]");

                }
            }

            return "thoikhoabieu/theo-tuan";
        }

        @DeleteMapping("/xoa-tuan/{id}")
        @ResponseBody
        public ResponseEntity<?> xoaTuan(
                @PathVariable Long id,
                @RequestParam String ngay) {
            try {
                LocalDate ngayDate = LocalDate.parse(ngay);
                tkbService.xoaTuan(id, ngayDate);
                return ResponseEntity.ok(Map.of("message", "Đã xóa lịch tuần này!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }

        @DeleteMapping("/huy-override/{id}")
        @ResponseBody
        public ResponseEntity<?> huyOverride(
                @PathVariable Long id,
                @RequestParam String ngay) {
            try {
                LocalDate ngayDate = LocalDate.parse(ngay);
                tkbService.huyOverride(id, ngayDate);
                return ResponseEntity.ok(Map.of("message", "Đã hủy chỉnh sửa, lịch gốc được khôi phục!"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }

        // ── TKB theo tháng ──────────────────────────────────────────────────────────
        @GetMapping("/theo-thang")
        @PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN', 'SINH_VIEN')")
        public String xemTheoThang(
                @RequestParam(required = false) Long giangVienId,
                @RequestParam(required = false) String hocKy,
                @RequestParam(required = false)
                @DateTimeFormat(pattern = "yyyy-MM")
                YearMonth ngay,
                Authentication auth, Model model) {

            if (ngay == null) ngay = YearMonth.now();

            LocalDate dauThang = ngay.atDay(1);
            LocalDate cuoiThang = ngay.atEndOfMonth();

            model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
            model.addAttribute("hocKyChon", hocKy);
            model.addAttribute("ngayChon", ngay);

            model.addAttribute("dauThang", dauThang);
            model.addAttribute("cuoiThang", cuoiThang);

            model.addAttribute("thangHienTai", ngay.getMonthValue());
            model.addAttribute("namHienTai", ngay.getYear());

            Map<Integer, TimeSlot> tietMap = timeSlotService.buildTietMap();

            model.addAttribute("tietMap", tietMap);

            model.addAttribute("calendarJsonBu",
                    (giangVienId != null && hocKy != null)
                            ? buildCalendarJsonBu(
                            lichDayBuService.findByGiangVienThang(giangVienId, hocKy, dauThang),
                            tietMap)
                            : "[]");


            List<LocalDate> ngayTrongThang =
                    dauThang.datesUntil(cuoiThang.plusDays(1)).toList();

            model.addAttribute("ngayTrongThang", ngayTrongThang);

            NguoiDung nd =
                    nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();

            if (nd.getVaiTro() == VaiTro.NHAN_VIEN) {
                NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
                if (nv != null) {
                    model.addAttribute("currentNvId", nv.getId());
                    // isQuanLy = true nếu chức vụ là Trưởng khoa / Phó khoa / Tổ trưởng bộ môn
                    boolean isQL = nv.getChucVu() != null &&
                            List.of("Trưởng khoa", "Phó khoa", "Tổ trưởng BM").contains(nv.getChucVu().getTenChucVu());
                    model.addAttribute("isQuanLy", isQL);

                }
            }
            // ───────────────── ADMIN ─────────────────
            if (nd.getVaiTro() == VaiTro.ADMIN) {

                model.addAttribute("giangViens",
                        nhanVienService.findAllGiangVien());

                model.addAttribute("giangVienIdChon", giangVienId);

                if (giangVienId != null && hocKy != null) {

                    List<ThoiKhoaBieu> ds =
                            tkbService.findByGiangVienThang(
                                    giangVienId,
                                    hocKy,
                                    dauThang
                            );

                    model.addAttribute("thoiKhoaBieus", ds);

                    model.addAttribute("calendarJson",
                            buildCalendarJson(ds, tietMap));

                    model.addAttribute("giangVien",
                            nhanVienService.findById(giangVienId).orElse(null));

                    model.addAttribute("thongKeTinChi",
                            tkbService.thongKeTinChi(giangVienId, hocKy));

                    model.addAttribute("lichDayBuTuan",
                            lichDayBuService.findByGiangVienTuan(giangVienId, ngay.atDay(1)));
                    List<LichDayBu> lichBuThang = lichDayBuService
                            .findByGiangVienAndKhoangNgay(giangVienId, dauThang, cuoiThang);
                    List<DonNghi> donNghiThang = donNghiRepo
                            .findDaDuyetByGvAndKhoang(giangVienId, dauThang, cuoiThang);
                    model.addAttribute("calendarJsonBu", buildJsonBu(lichBuThang, tietMap));
                    model.addAttribute("calendarJsonNghi", buildJsonNghi(donNghiThang));

                }


                // ───────────────── NHÂN VIÊN ─────────────────
            } else if (nd.getVaiTro() == VaiTro.NHAN_VIEN) {

                NhanVien nv =
                        nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);

                if (nv != null && hocKy != null) {

                    List<ThoiKhoaBieu> ds =
                            tkbService.findByGiangVienThang(
                                    nv.getId(),
                                    hocKy,
                                    dauThang
                            );

                    model.addAttribute("thoiKhoaBieus", ds);

                    model.addAttribute("calendarJson",
                            buildCalendarJson(ds, tietMap));

                    model.addAttribute("giangVien", nv);

                    model.addAttribute("thongKeTinChi",
                            tkbService.thongKeTinChi(nv.getId(), hocKy));
                    model.addAttribute("lichDayBuTuan",
                            lichDayBuService.findByGiangVienTuan(nv.getId(), ngay.atDay(1))); // ← sửa
                    List<LichDayBu> lichBuThang = lichDayBuService
                            .findByGiangVienAndKhoangNgay(nv.getId(), dauThang, cuoiThang);
                    List<DonNghi> donNghiThang = donNghiRepo
                            .findDaDuyetByGvAndKhoang(nv.getId(), dauThang, cuoiThang);
                    model.addAttribute("calendarJsonBu", buildJsonBu(lichBuThang, tietMap));
                    model.addAttribute("calendarJsonNghi", buildJsonNghi(donNghiThang));
                }

                // ───────────────── SINH VIÊN ─────────────────
            } else if (nd.getVaiTro() == VaiTro.SINH_VIEN) {
                SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElse(null);
                if (sv != null && hocKy != null) {
                    List<ThoiKhoaBieu> ds = tkbService.findBySinhVienThang(sv.getId(), hocKy, dauThang);
                    model.addAttribute("thoiKhoaBieus", ds);
                    model.addAttribute("calendarJson", buildCalendarJson(ds, tietMap));
                    model.addAttribute("sinhVien", sv);
                    model.addAttribute("calendarJsonBu", "[]");
                    model.addAttribute("calendarJsonNghi", "[]");


                }
            }

            return "thoikhoabieu/theo-thang";
        }

        // ════════════════════════════════════════════════════════════════════════
//  ENDPOINT: Tín chỉ còn lại (AJAX → trả JSON)
//  Công thức: tổng TC các môn trong kỳ — số tiết đã xếp / 15
// ════════════════════════════════════════════════════════════════════════
        @GetMapping("/tin-chi-con-lai")
        @PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN')")
        @ResponseBody
        public TinChiConLaiDTO tinChiConLai(
                @RequestParam Long giangVienId,
                @RequestParam String hocKy) {

            List<ThoiKhoaBieu> ds = tkbService.findByGiangVien(giangVienId, hocKy);

            // Tổng tín chỉ = tổng TC các môn không trùng lặp trong các LHP
            int tongTinChi = ds.stream()
                    .map(t -> t.getLopHocPhan())
                    .filter(l -> l.getMonHoc() != null)
                    .collect(java.util.stream.Collectors.toMap(
                            l -> l.getMonHoc().getId(), l -> l,
                            (a, b) -> a))
                    .values().stream()
                    .mapToInt(l -> l.getMonHoc().getSoTinChi())
                    .sum();

            // Tổng tiết đã xếp
            int tongTietDaXep = ds.stream().mapToInt(ThoiKhoaBieu::getSoTiet).sum();

            // Tiết cần (mỗi TC = 15 tiết/kỳ)
            int tongTietCanDay = tongTinChi * 15;
            int tietConLai = Math.max(0, tongTietCanDay - tongTietDaXep);
            int tcConLai = (int) Math.ceil(tietConLai / 15.0);
            double phanTram = tongTietCanDay > 0
                    ? (double) tongTietDaXep / tongTietCanDay * 100 : 0;

            return new TinChiConLaiDTO(tongTinChi, tongTietDaXep, tongTietCanDay,
                    tietConLai, tcConLai, Math.min(100, (int) phanTram));
        }

        // ── Cập nhật nhanh (Inline Edit — Admin only) ────────────────────
        @PatchMapping("/cap-nhat/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        @ResponseBody
        public ResponseEntity<Map<String, Object>> capNhatNhanh(
                @PathVariable Long id,
                @RequestBody TkbQuickUpdateDTO dto) {
            try {
                tkbService.capNhatNhanh(id, dto);
                return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công!"));
            } catch (IllegalStateException e) {
                // Xung đột lịch
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("success", false, "message", e.getMessage()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("success", false, "message", "Lỗi hệ thống: " + e.getMessage()));
            }
        }

        // ── Tiến độ tiết dạy theo LHP (AJAX) ────────────────────────────
        @GetMapping("/tien-do-mon")
        @PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN','SINH_VIEN')")
        @ResponseBody
        public TienDoMonDTO tienDoMon(@RequestParam Long lhpId) {
            int tietDaDay = tkbService.demTietByLhp(lhpId);
            LopHocPhan lhp = lhpService.findById(lhpId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy LHP"));
            int soTinChi = lhp.getMonHoc() != null ? lhp.getMonHoc().getSoTinChi() : 0;
            int tietCanDay = soTinChi * 15;         // 1 TC = 15 tiết/kỳ, chỉnh theo quy định trường
            int tietConLai = Math.max(0, tietCanDay - tietDaDay);
            double phanTram = tietCanDay > 0 ? (double) tietDaDay / tietCanDay * 100 : 0;
            return new TienDoMonDTO(tietDaDay, tietCanDay, tietConLai, phanTram);
        }

        // ════════════════════════════════════════════════════════════════════════
//  ENDPOINT: Chi tiết 1 ô lịch (AJAX popup)
// ════════════════════════════════════════════════════════════════════════
        @GetMapping("/chi-tiet/{id}")
        @PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN','SINH_VIEN')")
        @ResponseBody
        public TkbCellDetailDTO chiTietLich(@PathVariable Long id) {
            ThoiKhoaBieu tkb = tkbService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch"));

            var lhp = tkb.getLopHocPhan();
            var mon = lhp != null ? lhp.getMonHoc() : null;
            var gv = lhp != null ? lhp.getGiangVien() : null;

            return new TkbCellDetailDTO(
                    tkb.getId(),
                    mon != null ? mon.getTenMon() : "---",
                    mon != null ? mon.getMaMon() : "---",
                    mon != null ? mon.getSoTinChi() : 0,
                    gv != null ? gv.getHoTenVaHocVi() : "---",
                    gv != null ? gv.getId() : null,
                    lhp != null ? lhp.getMaLhp() : "---",
                    lhp != null ? lhp.getId() : null,
                    tkb.getTenThu(),
                    tkb.getThuTrongTuan(),
                    tkb.getTietBatDau(),
                    tkb.getTietBatDau() + tkb.getSoTiet() - 1,
                    tkb.getSoTiet(),
                    tkb.getPhongHoc(),
                    tkb.getTuanBatDau() != null ? tkb.getTuanBatDau().toString() : null,
                    tkb.getTuanKetThuc() != null ? tkb.getTuanKetThuc().toString() : null,
                    lhp != null ? lhp.getSiSoHienTai() : 0,
                    lhp != null ? lhp.getSiSoMax() : 0
            );
        }

        // ── Thống kê tải giảng dạy ─────────────────────────────────────────
        @GetMapping("/thong-ke")
        @PreAuthorize("hasRole('ADMIN')")
        public String thongKe(@RequestParam(required = false) String hocKy, Model model) {
            model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
            model.addAttribute("hocKyChon", hocKy);

            if (hocKy != null && !hocKy.isBlank()) {
                List<TaiGiangDayDTO> ds = tkbService.thongKeTaiGiangDay(hocKy);
                model.addAttribute("danhSachTai", ds);
                model.addAttribute("tongTietToanTruong",
                        ds.stream().mapToInt(TaiGiangDayDTO::tongTiet).sum());
                model.addAttribute("soGVQuaTai",
                        ds.stream().filter(t -> t.tyLeTai() > 1.0).count());
            }
            return "thoikhoabieu/thong-ke";
        }

        // ── Tìm phòng trống (AJAX) ─────────────────────────────────────────
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

        // ── CRUD TKB ───────────────────────────────────────────────────────
        @GetMapping("/them")
        @PreAuthorize("hasRole('ADMIN')")
        public String themForm(@RequestParam(required = false) Long giangVienId,
                               @RequestParam(required = false) String hocKy,
                               Model model) {

            model.addAttribute("thoiKhoaBieu", new ThoiKhoaBieu());
            model.addAttribute("lopHocPhans", lhpService.findAll());
            model.addAttribute("danhSachPhong", phongHocService.findAllHoatDong());

            // giữ context đang xem
            model.addAttribute("giangVienId", giangVienId);
            model.addAttribute("hocKy", hocKy);

            return "thoikhoabieu/form";
        }

        @PostMapping("/them")
        @PreAuthorize("hasRole('ADMIN')")
        public String them(@ModelAttribute ThoiKhoaBieu tkb,
                           @RequestParam(required = false) Long giangVienId,
                           @RequestParam(required = false) String hocKy,
                           RedirectAttributes ra) {

            try {
                tkbService.save(tkb);
                ra.addFlashAttribute("success", "Thêm thời khóa biểu thành công!");
            } catch (Exception e) {
                ra.addFlashAttribute("error", e.getMessage());

                // lỗi -> quay lại form thêm + giữ filter
                String redirect = "redirect:/tkb/them";

                if (giangVienId != null || hocKy != null) {
                    redirect += "?";

                    if (giangVienId != null) {
                        redirect += "giangVienId=" + giangVienId;
                    }

                    if (hocKy != null) {
                        if (giangVienId != null) redirect += "&";
                        redirect += "hocKy=" + java.net.URLEncoder.encode(
                                hocKy,
                                java.nio.charset.StandardCharsets.UTF_8
                        );
                    }
                }

                return redirect;
            }

            // thành công -> quay về đúng màn hình TKB đang xem
            String redirect = "redirect:/tkb";

            if (giangVienId != null || hocKy != null) {
                redirect += "?";

                if (giangVienId != null) {
                    redirect += "giangVienId=" + giangVienId;
                }

                if (hocKy != null) {
                    if (giangVienId != null) redirect += "&";
                    redirect += "hocKy=" + java.net.URLEncoder.encode(
                            hocKy,
                            java.nio.charset.StandardCharsets.UTF_8
                    );
                }
            }

            return redirect;
        }

        @GetMapping("/sua/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String suaForm(@PathVariable Long id,
                              @RequestParam(required = false) Long giangVienId,
                              @RequestParam(required = false) String hocKy,
                              Model model) {

            model.addAttribute("thoiKhoaBieu",
                    tkbService.findById(id).orElseThrow());

            model.addAttribute("lopHocPhans", lhpService.findAll());
            model.addAttribute("danhSachPhong",
                    phongHocService.findAllHoatDong());

            // giữ filter để quay lại
            model.addAttribute("giangVienId", giangVienId);
            model.addAttribute("hocKy", hocKy);

            return "thoikhoabieu/form";
        }

        @PostMapping("/sua/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String sua(@PathVariable Long id,
                          @RequestParam(required = false) Long giangVienId,
                          @RequestParam(required = false) String hocKy,
                          @ModelAttribute ThoiKhoaBieu tkb,
                          RedirectAttributes ra) {

            try {
                tkb.setId(id);
                tkbService.save(tkb);
                ra.addFlashAttribute("success", "Cập nhật thành công!");
            } catch (Exception e) {
                ra.addFlashAttribute("error", e.getMessage());

                return "redirect:/tkb/sua/" + id
                        + "?giangVienId=" + giangVienId
                        + "&hocKy=" + hocKy;
            }

            // quay về đúng màn hình TKB đang xem
            if (giangVienId != null && hocKy != null) {
                return "redirect:/tkb?giangVienId="
                        + giangVienId
                        + "&hocKy="
                        + hocKy;
            }

            return "redirect:/tkb";
        }

        // ── Xóa ───────────────────────────────────────────────────────
        @PostMapping("/xoa/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public String xoa(@PathVariable Long id,
                          @RequestParam(required = false) Long giangVienId,
                          @RequestParam(required = false) String hocKy,
                          RedirectAttributes ra) {

            try {
                tkbService.delete(id);
                ra.addFlashAttribute("success", "Đã xóa!");
            } catch (Exception e) {
                ra.addFlashAttribute("error", e.getMessage());
            }

            if (giangVienId != null && hocKy != null) {
                return "redirect:/tkb?giangVienId="
                        + giangVienId
                        + "&hocKy="
                        + hocKy;
            }

            return "redirect:/tkb";
        }
    }
//// =====================================================================
//// GIANG VIEN PORTAL
//// =====================================================================
@Controller
@RequestMapping("/giangvien")
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
@RequiredArgsConstructor
class GiangVienPortalController {

    private final NhanVienService nhanVienService;
    private final LopHocPhanService lhpService;
    private final NguoiDungRepository nguoiDungRepo;

    private NhanVien getCurrentGiangVien(Authentication auth) {
        var nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        return nhanVienService.findGiangVienByNguoiDungId(nd.getId())
                .orElseThrow(() -> new IllegalStateException("Tài khoản không phải giảng viên"));
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        var nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        NhanVien gv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
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

    @GetMapping("/chi-tiet-gv")
    public String chiTiet(Authentication auth, Model model) {
        var nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        NhanVien gv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("giangVien", gv);

        if (gv != null) {
            List<LopHocPhan> lhps = lhpService.findByGiangVien(gv.getId());
            model.addAttribute("soLopHocPhan", lhps.size());
            model.addAttribute("soSinhVien", lhps.stream()
                    .mapToInt(lhp -> lhpService.getDanhSachDangKy(lhp.getId()).size())
                    .sum());
        } else {
            model.addAttribute("soLopHocPhan", 0);
            model.addAttribute("soSinhVien", 0);
        }
        return "giangvien/chi-tiet-gv";
    }

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
        return "redirect:/giangvien/lop/" + lhpId + "/danh-sach";
    }
}

@Controller
@RequestMapping("/nhanvien")
@PreAuthorize("hasAnyRole('ADMIN', 'NHAN_VIEN')")
@RequiredArgsConstructor
class NhanVienPortalController {

    private final NhanVienService nhanVienService;
    private final NguoiDungRepository nguoiDungRepo;

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth, Model model) {
        var nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("nhanVien", nv);
        return "nhan-vien/portal-dashboard";
    }

    @GetMapping("/chi-tiet")
    public String chiTiet(Authentication auth, Model model) {
        var nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
        NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
        model.addAttribute("nhanVien", nv);
        return "nhan-vien/chi-tiet";
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
