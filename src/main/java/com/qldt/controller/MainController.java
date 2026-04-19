package com.qldt.controller;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.GiangVienRepository;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.SinhVienRepository;
import com.qldt.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.util.*;

import java.util.Arrays;
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
    private final NguoiDungService nguoiDungService;
    private final GiangVienRepository giangVienRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final SinhVienRepository sinhVienRepo;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tongSV", svService.count());
        model.addAttribute("tongGV", gvService.count());
        model.addAttribute("tongMon", monService.count());
        model.addAttribute("tongLop", lopService.count());
        model.addAttribute("tongLHP", lhpService.count());

        return "admin/dashboard";
    }
    @GetMapping("/fix-accounts")
    public String fixAccounts(RedirectAttributes ra) {
        List<GiangVien> gvList = gvService.findAll();
        int taoMoi = 0, lienKet = 0;

        for (GiangVien gv : gvList) {
            if (gv.getNguoiDung() == null) {
                String username = gv.getMaGv().toLowerCase();

                Optional<NguoiDung> existing = nguoiDungRepo.findByUsername(username);

                if (existing.isPresent()) {
                    gv.setNguoiDung(existing.get());
                    lienKet++;
                } else {
                    NguoiDung nd = nguoiDungService.taoTaiKhoan(
                            username,
                            gv.getHoTen(),
                            gv.getEmail(),
                            VaiTro.GIANG_VIEN
                    );
                    gv.setNguoiDung(nd);
                    taoMoi++;
                }

                giangVienRepo.save(gv);
            }
        }

        ra.addFlashAttribute("success",
                "Tạo mới: " + taoMoi + " | Liên kết lại: " + lienKet + " tài khoản. Mật khẩu: Admin@123");

        return "redirect:/admin/dashboard";
    }

    @GetMapping("/fix-sv-accounts")
    public String fixSvAccounts(RedirectAttributes ra) {
        List<SinhVien> svList = svService.findAll();
        int taoMoi = 0, lienKet = 0;

        for (SinhVien sv : svList) {
            if (sv.getNguoiDung() == null) {
                String username = sv.getMaSv().toLowerCase();
                Optional<NguoiDung> existing = nguoiDungRepo.findByUsername(username);

                if (existing.isPresent()) {
                    sv.setNguoiDung(existing.get());
                    lienKet++;
                } else {
                    NguoiDung nd = nguoiDungService.taoTaiKhoan(
                            username,
                            sv.getHoTen(),
                            sv.getEmail(),
                            VaiTro.SINH_VIEN
                    );
                    sv.setNguoiDung(nd);
                    taoMoi++;
                }
                sinhVienRepo.save(sv); // cần inject SinhVienRepository
            }
        }

        ra.addFlashAttribute("success",
                "SV - Tạo mới: " + taoMoi + " | Liên kết lại: " + lienKet + " tài khoản. Mật khẩu: Admin@123");
        return "redirect:/admin/dashboard";
    }
}

/// =====================================================================
// SINH VIEN CONTROLLER (ADMIN manages)
// =====================================================================
@Controller
@RequestMapping("/admin/sinh-vien")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
class SinhVienController {

    private final SinhVienService svService;
    private final LopService lopService;

    private void addFormData(Model model) {
        List<Lop> lops = lopService.findAll();
        model.addAttribute("lops", lops != null ? lops : Collections.emptyList());
        model.addAttribute("gioiTinhs", GioiTinh.values());
        model.addAttribute("trangThais", TrangThaiSV.values());
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("sinhViens",
                search != null ? svService.search(search) : svService.findAll());
        model.addAttribute("search", search);
        model.addAttribute("tongSo", svService.count());
        return "sinhvien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        SinhVien sv = new SinhVien();
        sv.setLop(new Lop());
        model.addAttribute("sinhVien", sv);
        addFormData(model);
        return "sinhvien/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute SinhVien sv,
                       BindingResult result,
                       Model model,
                       RedirectAttributes ra) {

        if (sv.getLop() == null) sv.setLop(new Lop());

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("sinhVien", sv);
            return "sinhvien/form";
        }

        try {
            svService.save(sv);
            ra.addFlashAttribute("success",
                    "Thêm sinh viên thành công! Tài khoản: "
                            + sv.getMaSv().toLowerCase() + " / Sv@" + sv.getMaSv());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/sinh-vien";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        SinhVien sv = svService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sinh viên"));
        if (sv.getLop() == null) sv.setLop(new Lop());
        model.addAttribute("sinhVien", sv);
        addFormData(model);
        return "sinhvien/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id,
                      @Valid @ModelAttribute SinhVien sv,
                      BindingResult result,
                      Model model,
                      RedirectAttributes ra) {

        if (sv.getLop() == null) sv.setLop(new Lop());

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("sinhVien", sv);
            return "sinhvien/form";
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
            ra.addFlashAttribute("success", "Xóa thành công!");
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

    private void addFormData(Model model) {
        model.addAttribute("khoas", khoaService.findAll());
        model.addAttribute("hocVis", new String[]{"CN", "ThS", "TS", "PGS.TS", "GS.TS"});
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search, Model model) {
        model.addAttribute("giangViens", search != null ? gvService.search(search) : gvService.findAll());
        model.addAttribute("search", search);
        return "giangvien/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("giangVien", new GiangVien());
        addFormData(model);
        return "giangvien/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute GiangVien gv, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            addFormData(model);
            return "giangvien/form";
        }
        try {
            gvService.save(gv);
            ra.addFlashAttribute("success",
                    "Thêm giảng viên '" + gv.getHoTen() + "' thành công! " +
                            "Tài khoản: " + gv.getMaGv().toLowerCase() + " / Gv@" + gv.getMaGv());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/giang-vien";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("giangVien", gvService.findById(id).orElseThrow());
        addFormData(model);
        return "giangvien/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute GiangVien gv,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            addFormData(model);
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