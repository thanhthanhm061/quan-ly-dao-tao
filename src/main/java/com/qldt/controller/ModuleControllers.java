package com.qldt.controller;

import com.qldt.model.*;
import com.qldt.model.enums.*;
import com.qldt.repository.GiangVienRepository;
import com.qldt.repository.NguoiDungRepository;
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
        model.addAttribute("monHocs", search != null ? monService.search(search) : monService.findAll());
        model.addAttribute("search", search);
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

    @GetMapping
    public String list(Model model) {
        model.addAttribute("lops", lopService.findAll());
        return "lop/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("lop", new Lop());
        model.addAttribute("khoas", khoaService.findAll());
        return "lop/form";
    }

    @PostMapping("/them")
    public String them(@ModelAttribute Lop lop, RedirectAttributes ra) {
        try { lopService.save(lop); ra.addFlashAttribute("success", "Thêm lớp thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("lop", lopService.findById(id).orElseThrow());
        model.addAttribute("khoas", khoaService.findAll());
        return "lop/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @ModelAttribute Lop lop, RedirectAttributes ra) {
        try { lop.setId(id); lopService.save(lop); ra.addFlashAttribute("success", "Cập nhật thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try { lopService.delete(id); ra.addFlashAttribute("success", "Đã xóa lớp!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop";
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

    @GetMapping
    public String list(@RequestParam(required = false) String hocKy, Model model) {
        List<LopHocPhan> lhps = hocKy != null && !hocKy.isBlank()
            ? lhpService.findByHocKy(hocKy) : lhpService.findAll();
        model.addAttribute("lopHocPhans", lhps);
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);
        return "lophocphan/list";
    }

    @GetMapping("/them")
    public String themForm(Model model) {
        model.addAttribute("lopHocPhan", new LopHocPhan());
        model.addAttribute("monHocs", monService.findAll());
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("trangThais", TrangThaiLHP.values());
        return "lophocphan/form";
    }

    @PostMapping("/them")
    public String them(@Valid @ModelAttribute LopHocPhan lhp, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("monHocs", monService.findAll());
            model.addAttribute("giangViens", gvService.findAll());
            model.addAttribute("trangThais", TrangThaiLHP.values());
            return "lophocphan/form";
        }
        try { lhpService.save(lhp); ra.addFlashAttribute("success", "Thêm lớp học phần thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop-hoc-phan";
    }

    @GetMapping("/sua/{id}")
    public String suaForm(@PathVariable Long id, Model model) {
        model.addAttribute("lopHocPhan", lhpService.findById(id).orElseThrow());
        model.addAttribute("monHocs", monService.findAll());
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("trangThais", TrangThaiLHP.values());
        return "lophocphan/form";
    }

    @PostMapping("/sua/{id}")
    public String sua(@PathVariable Long id, @Valid @ModelAttribute LopHocPhan lhp,
                      BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("monHocs", monService.findAll());
            model.addAttribute("giangViens", gvService.findAll());
            model.addAttribute("trangThais", TrangThaiLHP.values());
            return "lophocphan/form";
        }
        try { lhp.setId(id); lhpService.save(lhp); ra.addFlashAttribute("success", "Cập nhật thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop-hoc-phan";
    }

    @PostMapping("/xoa/{id}")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try { lhpService.delete(id); ra.addFlashAttribute("success", "Đã xóa lớp học phần!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop-hoc-phan";
    }

    @GetMapping("/{id}/danh-sach")
    public String danhSachDangKy(@PathVariable Long id, Model model) {
        LopHocPhan lhp = lhpService.findById(id).orElseThrow();
        model.addAttribute("lopHocPhan", lhp);
        model.addAttribute("dangKys", lhpService.getDanhSachDangKy(id));
        return "lophocphan/danh-sach";
    }

    @PostMapping("/cap-nhat-diem")
    public String capNhatDiem(@RequestParam Long dkId, @RequestParam Long lhpId,
                               @RequestParam(required = false) Double diemQT,
                               @RequestParam(required = false) Double diemThi,
                               RedirectAttributes ra) {
        try { lhpService.capNhatDiem(dkId, diemQT, diemThi); ra.addFlashAttribute("success", "Cập nhật điểm thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin/lop-hoc-phan/" + lhpId + "/danh-sach";
    }
}

// =====================================================================
// THOI KHOA BIEU CONTROLLER
// =====================================================================
@Controller
@RequestMapping("/tkb")
@RequiredArgsConstructor
class ThoiKhoaBieuController {
    private final ThoiKhoaBieuService tkbService;
    private final LopHocPhanService lhpService;
    private final GiangVienService gvService;
    private final SinhVienService svService;
    private final NguoiDungRepository nguoiDungRepo;
    private final GiangVienRepository giangVienRepo;

    @GetMapping
    public String xemTKB(@RequestParam(required = false) Long giangVienId,
                          @RequestParam(required = false) String hocKy,
                          Authentication auth, Model model) {
        model.addAttribute("giangViens", gvService.findAll());
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("giangVienIdChon", giangVienId);
        model.addAttribute("hocKyChon", hocKy);

        if (giangVienId != null && hocKy != null) {
            model.addAttribute("thoiKhoaBieus", tkbService.findByGiangVien(giangVienId, hocKy));
            model.addAttribute("giangVien", gvService.findById(giangVienId).orElse(null));
        }
        return "thoikhoabieu/xem";
    }

    @GetMapping("/cua-toi")
    public String tkbCuaToi(@RequestParam(required = false) String hocKy,
                             Authentication auth, Model model) {
        String username = auth.getName();
        NguoiDung nd = nguoiDungRepo.findByUsername(username).orElseThrow();
        model.addAttribute("danhSachHocKy", lhpService.findAllHocKy());
        model.addAttribute("hocKyChon", hocKy);

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

    @GetMapping("/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String themForm(Model model) {
        model.addAttribute("thoiKhoaBieu", new ThoiKhoaBieu());
        model.addAttribute("lopHocPhans", lhpService.findAll());
        return "thoikhoabieu/form";
    }

    @PostMapping("/them")
    @PreAuthorize("hasRole('ADMIN')")
    public String them(@ModelAttribute ThoiKhoaBieu tkb, RedirectAttributes ra) {
        try { tkbService.save(tkb); ra.addFlashAttribute("success", "Thêm thời khóa biểu thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/tkb";
    }

    @PostMapping("/xoa/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String xoa(@PathVariable Long id, RedirectAttributes ra) {
        try { tkbService.delete(id); ra.addFlashAttribute("success", "Đã xóa!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
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

    @PostMapping("/dang-ky")
    public String dangKy(@RequestParam Long lhpId, Authentication auth, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
            SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElseThrow();
            lhpService.dangKy(sv.getId(), lhpId);
            ra.addFlashAttribute("success", "Đăng ký học phần thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/sinhvien/dang-ky";
    }

    @PostMapping("/huy-dang-ky")
    public String huyDangKy(@RequestParam Long lhpId, Authentication auth, RedirectAttributes ra) {
        try {
            NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElseThrow();
            SinhVien sv = svService.findByNguoiDungId(nd.getId()).orElseThrow();
            lhpService.huyDangKy(sv.getId(), lhpId);
            ra.addFlashAttribute("success", "Đã hủy đăng ký!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/sinhvien/dashboard";
    }
}
