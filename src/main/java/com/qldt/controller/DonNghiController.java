package com.qldt.controller;

import com.qldt.model.DonNghi;
import com.qldt.model.NhanVien;
import com.qldt.model.enums.TrangThaiDonNghi;
import com.qldt.repository.NhanVienRepository;
import com.qldt.service.DonNghiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/don-nghi")
@RequiredArgsConstructor
public class DonNghiController {

    private final DonNghiService donNghiService;
    private final NhanVienRepository nhanVienRepo;

    /* =========================================================
       HELPER — kiểm tra role admin
    ========================================================= */
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    /* =========================================================
       HELPER — lấy NhanVien từ session (chỉ gọi khi KHÔNG phải admin)
    ========================================================= */
    private NhanVien getNhanVienHienTai(Authentication auth) {
        String username = auth.getName();
        return nhanVienRepo.findByNguoiDungUsername(username)
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy hồ sơ nhân viên cho tài khoản: " + username));
    }

    /* =========================================================
       HELPER — lấy NhanVien nếu có, trả về Optional (an toàn cho admin)
    ========================================================= */
    private Optional<NhanVien> getNhanVienOptional(Authentication auth) {
        return nhanVienRepo.findByNguoiDungUsername(auth.getName());
    }

    /* =========================================================
       DANH SÁCH — nhân viên xem đơn của mình
    ========================================================= */
    @GetMapping
    public String danhSach(Model model, Authentication auth) {

        if (isAdmin(auth)) {
            model.addAttribute("danhSach", donNghiService.findAll());
            model.addAttribute("nhanVien", null);
        } else {
            NhanVien nv = getNhanVienHienTai(auth);
            model.addAttribute("danhSach", donNghiService.findByNguoiNop(nv.getId()));
            model.addAttribute("nhanVien", nv);
        }

        model.addAttribute("trangThai", TrangThaiDonNghi.values());
        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/danh-sach";
    }

    /* =========================================================
       DANH SÁCH DUYỆT — TK/PTK/TBM/Admin xem đơn chờ duyệt
    ========================================================= */
    @GetMapping("/duyet")
    public String danhSachDuyet(
            @RequestParam(required = false) TrangThaiDonNghi trangThai,
            Model model, Authentication auth) {

        List<DonNghi> danhSach;

        if (isAdmin(auth)) {
            danhSach = (trangThai != null)
                    ? donNghiService.findByTrangThai(trangThai)
                    : donNghiService.findAll();
        } else {
            NhanVien nv = getNhanVienHienTai(auth);
            Long khoaId = nv.getKhoa() != null ? nv.getKhoa().getId() : null;
            if (khoaId == null) {
                model.addAttribute("error", "Bạn chưa được gán vào khoa nào.");
                model.addAttribute("danhSach", List.of());
                return "don-nghi/duyet";
            }
            danhSach = (trangThai != null && trangThai != TrangThaiDonNghi.CHO_DUYET)
                    ? donNghiService.findByTrangThai(trangThai)
                    : donNghiService.findChoDuyetTheoKhoa(khoaId);
        }

        model.addAttribute("danhSach", danhSach);
        model.addAttribute("trangThaiList", TrangThaiDonNghi.values());
        model.addAttribute("trangThaiFilter", trangThai);
        model.addAttribute("activePage", "don-nghi-duyet");
        return "don-nghi/duyet";
    }

    /* =========================================================
       FORM TẠO ĐƠN
       Admin cũng được tạo đơn — dùng NhanVien đầu tiên hoặc null
    ========================================================= */
    @GetMapping("/tao")
    public String formTao(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc,
            Model model, Authentication auth, RedirectAttributes ra) {

        NhanVien nv = null;
        if (!isAdmin(auth)) {
            nv = getNhanVienHienTai(auth);
        }
        // Admin: nv = null, template tự xử lý (ẩn thông tin nhân viên hoặc hiện dropdown)

        model.addAttribute("nhanVien", nv);
        model.addAttribute("loaiNghiList", List.of(
                "Nghỉ phép",
                "Nghỉ bệnh",
                "Nghỉ không lương",
                "Nghỉ việc riêng",
                "Nghỉ thai sản"));
        model.addAttribute("ngayBatDauDefault",
                ngayBatDau != null ? ngayBatDau.toString() : "");
        model.addAttribute("ngayKetThucDefault",
                ngayKetThuc != null ? ngayKetThuc.toString() : "");
        model.addAttribute("isAdmin", isAdmin(auth));
        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/tao-don";
    }

    @PostMapping("/tao")
    public String taoMoi(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayBatDau,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayKetThuc,
            @RequestParam String lyDo,
            @RequestParam(required = false) String loaiNghi,
            @RequestParam(required = false) String fileDinhKem,
            @RequestParam(required = false) Long nhanVienId, // admin truyền nhanVienId qua form
            Authentication auth,
            RedirectAttributes ra) {

        try {
            Long nguoiNopId;
            if (isAdmin(auth)) {
                if (nhanVienId == null) {
                    throw new IllegalArgumentException("Admin cần chọn nhân viên để tạo đơn.");
                }
                nguoiNopId = nhanVienId;
            } else {
                nguoiNopId = getNhanVienHienTai(auth).getId();
            }
            donNghiService.taoMoi(nguoiNopId, ngayBatDau, ngayKetThuc,
                    lyDo, loaiNghi, fileDinhKem);
            ra.addFlashAttribute("success", "Tạo đơn xin nghỉ thành công! Chờ phê duyệt.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/don-nghi/tao";
        }
        return "redirect:/don-nghi";
    }

    /* =========================================================
       CHI TIẾT
    ========================================================= */
    @GetMapping("/{id}")
    public String chiTiet(@PathVariable Long id, Model model, Authentication auth) {

        DonNghi don = donNghiService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn nghỉ"));

        // Admin xem thẳng, không cần hồ sơ nhân viên
        if (isAdmin(auth)) {
            model.addAttribute("don", don);
            model.addAttribute("laNguoiNop", false);
            model.addAttribute("isAdmin", true);
            model.addAttribute("activePage", "don-nghi");
            return "don-nghi/chi-tiet";
        }

        NhanVien nv = getNhanVienHienTai(auth);
        boolean laNguoiNop = don.getNguoiNop().getId().equals(nv.getId());

        if (!laNguoiNop) {
            String maChucVu = nv.getChucVu() != null ? nv.getChucVu().getMaChucVu() : "";
            boolean cungKhoa = nv.getKhoa() != null
                    && don.getNguoiNop().getKhoa() != null
                    && nv.getKhoa().getId().equals(don.getNguoiNop().getKhoa().getId());
            if (!List.of("TK", "PTK", "TBM").contains(maChucVu) || !cungKhoa) {
                model.addAttribute("error", "Bạn không có quyền xem đơn này.");
                return "redirect:/don-nghi";
            }
        }

        model.addAttribute("don", don);
        model.addAttribute("laNguoiNop", laNguoiNop);
        model.addAttribute("isAdmin", false);
        model.addAttribute("activePage", "don-nghi");
        return "don-nghi/chi-tiet";
    }

    /* =========================================================
       HỦY ĐƠN
    ========================================================= */
    @PostMapping("/{id}/huy")
    public String huy(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            // Admin hủy thẳng không cần kiểm tra nhân viên
            if (isAdmin(auth)) {
                donNghiService.huyByAdmin(id);
                ra.addFlashAttribute("success", "Admin đã hủy đơn nghỉ.");
                return "redirect:/don-nghi";
            }
            NhanVien nv = getNhanVienHienTai(auth);
            donNghiService.huy(id, nv.getId());
            ra.addFlashAttribute("success", "Đã hủy đơn nghỉ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi";
    }

    /* =========================================================
       DUYỆT
    ========================================================= */
    @PostMapping("/{id}/duyet")
    public String duyet(@PathVariable Long id,
                        Authentication auth,
                        RedirectAttributes ra) {
        try {
            boolean coQuyenDuyet = auth.getAuthorities().stream()
                    .anyMatch(a -> List.of(
                            "ROLE_ADMIN", "ROLE_TK", "ROLE_PTK", "ROLE_TBM", "ROLE_CNTT"
                    ).contains(a.getAuthority()));

            if (!coQuyenDuyet) {
                ra.addFlashAttribute("error", "Bạn không có quyền duyệt đơn nghỉ.");
                return "redirect:/don-nghi";
            }

            // Admin duyệt không cần hồ sơ nhân viên — truyền null
            Long nguoiDuyetId = isAdmin(auth)
                    ? null
                    : getNhanVienHienTai(auth).getId();

            donNghiService.duyet(id, nguoiDuyetId);
            ra.addFlashAttribute("success", "Đã duyệt đơn nghỉ thành công.");

        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi/duyet";
    }

    /* =========================================================
       TỪ CHỐI
    ========================================================= */
    @PostMapping("/{id}/tu-choi")
    public String tuChoi(@PathVariable Long id,
                         @RequestParam String lyDoTuChoi,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            // Admin từ chối không cần hồ sơ nhân viên — truyền null
            Long nguoiTuChoiId = isAdmin(auth)
                    ? null
                    : getNhanVienHienTai(auth).getId();

            donNghiService.tuChoi(id, nguoiTuChoiId, lyDoTuChoi);
            ra.addFlashAttribute("success", "Đã từ chối đơn nghỉ.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/don-nghi/duyet";
    }
}