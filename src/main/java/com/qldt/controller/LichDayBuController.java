package com.qldt.controller;

import com.qldt.model.LichDayBu;
import com.qldt.model.NhanVien;
import com.qldt.model.PhongHoc;
import com.qldt.model.enums.TrangThaiLichBu;
import com.qldt.repository.LopHocPhanRepository;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.DonNghiService;
import com.qldt.service.LichDayBuApiDTO;
import com.qldt.service.LichDayBuService;
import com.qldt.service.PhongHocService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lich-day-bu")
@RequiredArgsConstructor
public class LichDayBuController {

    private final LichDayBuService lichDayBuService;
    private final DonNghiService donNghiService;
    private final NhanVienRepository nhanVienRepo;
    private final LopHocPhanRepository lhpRepo;
    private final PhongHocService phongHocService;

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Trả Optional — không throw, để từng method tự quyết định
    private Optional<NhanVien> getNhanVien(Authentication auth) {
        return nhanVienRepo.findByNguoiDungUsername(auth.getName());
    }

    @GetMapping
    public String danhSach(Model model, Authentication auth) {
        List<LichDayBu> danhSach;
        if (isAdmin(auth)) {
            danhSach = lichDayBuService.findAll();
        } else {
            Optional<NhanVien> nvOpt = getNhanVien(auth);
            Long khoaId = nvOpt
                    .map(nv -> nv.getKhoa() != null ? nv.getKhoa().getId() : null)
                    .orElse(null);

            danhSach = (khoaId != null)
                    ? lichDayBuService.findByKhoa(khoaId)
                    : List.of();
        }
        model.addAttribute("danhSach", danhSach);
        model.addAttribute("activePage", "lich-day-bu");
        return "lich-day-bu/danh-sach";
    }
    @GetMapping("/cua-toi")
    public String cuaToi(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model, Authentication auth) {

        NhanVien nv = getNhanVien(auth).orElseThrow(() ->
                new IllegalStateException("Không tìm thấy hồ sơ nhân viên: " + auth.getName()));

        if (from == null) from = LocalDate.now().withDayOfMonth(1);
        if (to == null) to = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        model.addAttribute("danhSach", lichDayBuService.findByGiangVienAndKhoangNgay(nv.getId(), from, to));
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("activePage", "lich-day-bu");
        return "lich-day-bu/danh-sach";
    }

    @GetMapping("/xep")
    public String formXep(
            @RequestParam(required = false) Long donNghiId,
            @RequestParam(required = false) Long lhpId,
            @RequestParam(required = false) Long giangVienId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNghiGoc,
            Model model, Authentication auth) {

        model.addAttribute("danhSachLhp", lhpRepo.findAll());
        model.addAttribute("danhSachGV", nhanVienRepo.findAllGiangVien());
        model.addAttribute("thuList", List.of(2, 3, 4, 5, 6, 7));
        model.addAttribute("activePage", "lich-day-bu");
        model.addAttribute("danhSachPhong", phongHocService.findAll()
                        .stream()
                        .filter(PhongHoc::isHoatDong)
                        .toList());
        // Pre-fill từ đơn nghỉ (ưu tiên ngayNghiGoc từ đơn nếu có)
        if (donNghiId != null) {
            donNghiService.findById(donNghiId).ifPresent(don -> {
                model.addAttribute("donNghi", don);
                // Chỉ dùng ngayNghiGoc từ đơn nếu không truyền query param
                if (ngayNghiGoc == null) {
                    model.addAttribute("ngayNghiGoc", don.getNgayBatDau());
                }
            });
        }

        // Pre-fill ngayNghiGoc từ query param (popup TKB) — ghi đè nếu có
        if (ngayNghiGoc != null) {
            model.addAttribute("ngayNghiGoc", ngayNghiGoc);
        }

        // Pre-fill lớp học phần từ popup TKB
        if (lhpId != null) {
            lhpRepo.findById(lhpId).ifPresent(lhp -> model.addAttribute("lhpChon", lhp));
        }

        // Pre-fill giảng viên từ popup TKB
        if (giangVienId != null) {
            nhanVienRepo.findById(giangVienId)
                    .ifPresent(gv -> model.addAttribute("giangVienChon", gv));
        }

        // ===== Đơn nghỉ =====
        if (donNghiId != null && donNghiId > 0) {

            donNghiService.findById(donNghiId).ifPresent(don -> {

                model.addAttribute("donNghi", don);

                if (ngayNghiGoc == null) {
                    model.addAttribute("ngayNghiGoc", don.getNgayBatDau());
                }
            });
        }

        // ===== Lớp học phần =====
        if (lhpId != null && lhpId > 0) {

            lhpRepo.findById(lhpId)
                    .ifPresent(lhp ->
                            model.addAttribute("lhpChon", lhp));
        }

        // ===== Giảng viên =====
        if (giangVienId != null && giangVienId > 0) {

            nhanVienRepo.findById(giangVienId)
                    .ifPresent(gv ->
                            model.addAttribute("giangVienChon", gv));
        }
        return "lich-day-bu/xep-lich";
    }

    @PostMapping("/xep")
    public String xepLich(
            @RequestParam Long lhpId,
            @RequestParam Long giangVienId,
            @RequestParam(required = false) Long donNghiId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNghiGoc,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayDayBu,
            @RequestParam int thuTrongTuan,
            @RequestParam int tietBatDau,
            @RequestParam int soTiet,
            @RequestParam(required = false) String phongHoc,
            @RequestParam(required = false) String ghiChu,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            Long nguoiXepId = isAdmin(auth) ? null
                    : getNhanVien(auth).map(NhanVien::getId).orElse(null);
            lichDayBuService.xepLich(lhpId, giangVienId, donNghiId,
                    ngayNghiGoc, ngayDayBu, thuTrongTuan,
                    tietBatDau, soTiet, phongHoc, ghiChu, nguoiXepId);
            ra.addFlashAttribute("success", "Đã xếp lịch dạy bù thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/lich-day-bu/xep";
        }
        return "redirect:/lich-day-bu";
    }
    @GetMapping("/api/tuan")
    @PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN','SINH_VIEN')")
    @ResponseBody
    public List<LichDayBuApiDTO> lichBuTuan(
            @RequestParam Long giangVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay) {
        LocalDate thu2 = ngay.with(DayOfWeek.MONDAY);
        LocalDate thu7 = thu2.plusDays(5);
        return lichDayBuService.findByGiangVienAndKhoangNgay(giangVienId, thu2, thu7)
                .stream()
                .filter(b -> b.getTrangThai() != TrangThaiLichBu.HUY)
                .map(b -> new LichDayBuApiDTO(
                        b.getNgayDayBu() != null
                                ? b.getNgayDayBu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : null,
                        b.getPhongHoc(),
                        b.getTietBatDau(),
                        b.getTietKetThuc(),
                        b.getGhiChu()
                ))
                .toList();
    }
    @GetMapping("/api/theo-lhp")
    @PreAuthorize("hasAnyRole('ADMIN','NHAN_VIEN','SINH_VIEN')")
    @ResponseBody
    public List<LichDayBuApiDTO> lichBuTheoLhp(@RequestParam Long lhpId) {
        return lichDayBuService.findByLhpId(lhpId)
                .stream()
                .map(b -> new LichDayBuApiDTO(
                        b.getNgayDayBu() != null
                                ? b.getNgayDayBu().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : null,
                        b.getPhongHoc(),
                        b.getTietBatDau(),
                        b.getTietKetThuc(),
                        b.getGhiChu()
                ))
                .toList();
    }
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Long id, Model model) {
        LichDayBu lichBu = lichDayBuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch dạy bù"));
        model.addAttribute("lichBu", lichBu);
        model.addAttribute("activePage", "lich-day-bu");
        return "lich-day-bu/chi-tiet";
    }

    @GetMapping("/{id}/sua")
    public String formSua(@PathVariable Long id, Model model) {
        LichDayBu lichBu = lichDayBuService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lịch dạy bù"));
        model.addAttribute("lichBu", lichBu);
        model.addAttribute("danhSachGV", nhanVienRepo.findAllGiangVien());
        model.addAttribute("thuList", List.of(2, 3, 4, 5, 6, 7));
        model.addAttribute("activePage", "lich-day-bu");
        model.addAttribute("danhSachPhong", phongHocService.findAll()
                .stream()
                .filter(PhongHoc::isHoatDong)
                .toList());
        return "lich-day-bu/sua-lich";
    }

    @PostMapping("/{id}/sua")
    public String capNhat(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayDayBu,
            @RequestParam int thuTrongTuan,
            @RequestParam int tietBatDau,
            @RequestParam int soTiet,
            @RequestParam(required = false) String phongHoc,
            @RequestParam(required = false) String ghiChu,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            Long nguoiSuaId = isAdmin(auth) ? null
                    : getNhanVien(auth).map(NhanVien::getId).orElse(null);
            lichDayBuService.capNhat(id, ngayDayBu, thuTrongTuan,
                    tietBatDau, soTiet, phongHoc, ghiChu, nguoiSuaId);
            ra.addFlashAttribute("success", "Đã cập nhật lịch dạy bù.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lich-day-bu/" + id;
    }

    @PostMapping("/{id}/huy")
    public String huy(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            Long nguoiHuyId = isAdmin(auth) ? null
                    : getNhanVien(auth).map(NhanVien::getId).orElse(null);
            lichDayBuService.huy(id, nguoiHuyId);
            ra.addFlashAttribute("success", "Đã hủy lịch dạy bù.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lich-day-bu";
    }

    @PostMapping("/{id}/hoan-thanh")
    public String hoanThanh(@PathVariable Long id, RedirectAttributes ra) {
        try {
            lichDayBuService.hoanThanh(id);
            ra.addFlashAttribute("success", "Đã đánh dấu hoàn thành.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/lich-day-bu/" + id;
    }
}