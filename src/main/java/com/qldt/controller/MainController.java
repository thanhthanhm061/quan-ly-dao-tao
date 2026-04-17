package com.qldt.controller;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.List;

// =====================================================================
// AUTH CONTROLLER
// =====================================================================
@Controller
class AuthController {
    @GetMapping("/login")
    public String login() { return "auth/login"; }

    @GetMapping("/access-denied")
    public String accessDenied() { return "auth/access-denied"; }

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null) return "redirect:/login";
        String role = auth.getAuthorities().iterator().next().getAuthority();
        return switch (role) {
            case "ROLE_ADMIN"      -> "redirect:/admin/dashboard";
            case "ROLE_GIANG_VIEN" -> "redirect:/giangvien/dashboard";
            case "ROLE_SINH_VIEN"  -> "redirect:/sinhvien/dashboard";
            default -> "redirect:/login";
        };
    }
}

// =====================================================================
// ADMIN DASHBOARD CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class AdminController {
    private final SinhVienService svService;
    private final GiangVienService gvService;
    private final MonHocService monService;
    private final LopService lopService;
    private final LopHocPhanService lhpService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tongSV", svService.count());
        model.addAttribute("tongGV", gvService.count());
        model.addAttribute("tongMon", monService.count());
        model.addAttribute("tongLop", lopService.count());
        model.addAttribute("tongLHP", lhpService.count());
        return "admin/dashboard";
    }
}

// =====================================================================
// SINH VIEN CONTROLLER (ADMIN manages)
// =====================================================================
@Controller
@RequestMapping("/admin/sinh-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class SinhVienController {
    private final SinhVienService svService;
    private final LopService lopService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("sinhViens", search != null ? svService.search(search) : svService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("tongSo", svService.count());
        return "sinhvien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {

        SinhVien sv = new SinhVien();
        sv.setLop(new Lop());

        model.addAttribute("sinhVien",  sv);
        model.addAttribute("lops", lopService.findAll());
        model.addAttribute("gioiTinhs", List.of(GioiTinh.values()));
        model.addAttribute("trangThais", List.of(TrangThaiSV.values()));
        return "sinhvien/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute SinhVien sv, BindingResult result,
                       Model model, RedirectAttributes ra) {

        if (sv.getLop() == null) {
            sv.setLop(new Lop());
        }
        if (result.hasErrors()) {
            model.addAttribute("lops", lopService.findAll());
            model.addAttribute("gioiTinhs", List.of(GioiTinh.values()));
            model.addAttribute("trangThais", List.of(TrangThaiSV.values()));
            return "sinhvien/form";
        }
        try {
            svService.save(sv);
            ra.addFlashAttribute("success", "Thêm sinh viên '" + sv.getHoTen() + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sinh-vien";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        SinhVien sv = svService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));

        if (sv.getLop() == null) {
            sv.setLop(new Lop());
        }
        model.addAttribute("sinhVien", sv);
        model.addAttribute("lops", lopService.findAll());
        model.addAttribute("gioiTinhs", List.of(GioiTinh.values()));
        model.addAttribute("trangThais", List.of(TrangThaiSV.values()));
        return "sinhvien/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute SinhVien sv,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("lops", lopService.findAll());
            model.addAttribute("gioiTinhs", List.of(GioiTinh.values()));
            model.addAttribute("trangThais", List.of(TrangThaiSV.values()));
            return "sinhvien/form";
        }
        if (sv.getLop() == null) {
            sv.setLop(new Lop());
        }
        try {
            sv.setId(id);
            svService.save(sv);
            ra.addFlashAttribute("success", "Cập nhật sinh viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sinh-vien";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            svService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa sinh viên!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/sinh-vien";
    }
}

// =====================================================================
// GIANG VIEN CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/admin/giang-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class GiangVienController {
    private final GiangVienService gvService;
    private final KhoaService khoaService;

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("giangViens", search != null ? gvService.search(search) : gvService.findAll());
        model.addAttribute("search", search);
        return "giangvien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("giangVien", new GiangVien());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("hocVis", new String[]{"CN", "ThS", "TS", "PGS.TS", "GS.TS"});
        return "giangvien/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute GiangVien gv, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("hocVis", new String[]{"CN", "ThS", "TS", "PGS.TS", "GS.TS"});
            return "giangvien/form";
        }
        try {
            gvService.save(gv);
            ra.addFlashAttribute("success", "Thêm giảng viên '" + gv.getHoTen() + "' thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giang-vien";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("giangVien", gvService.findById(id).orElseThrow());
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("hocVis", new String[]{"CN", "ThS", "TS", "PGS.TS", "GS.TS"});
        return "giangvien/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute GiangVien gv,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("khoas", khoaService.findAll());
            model.addAttribute("hocVis", new String[]{"CN", "ThS", "TS", "PGS.TS", "GS.TS"});
            return "giangvien/form";
        }
        try {
            gv.setId(id);
            gvService.save(gv);
            ra.addFlashAttribute("success", "Cập nhật giảng viên thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giang-vien";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            gvService.delete(id);
            ra.addFlashAttribute("success", "Đã xóa giảng viên!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giang-vien";
    }
}
