package com.qldt.config;

import com.qldt.model.NhanVien;
import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("securityService")
@RequiredArgsConstructor
public class SecurityService {

    private final NhanVienService nhanVienService;
    private final NguoiDungRepository nguoiDungRepo;

    /**
     * Kiểm tra người dùng hiện tại có phải quản lý không (TK / PTK / TBM).
     * Dùng trong @PreAuthorize: @securityService.isQuanLy(authentication)
     */
    public boolean isQuanLy(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;

        NguoiDung nd = nguoiDungRepo.findByUsername(auth.getName()).orElse(null);
        if (nd == null || nd.getVaiTro() != VaiTro.NHAN_VIEN) return false;

        NhanVien nv = nhanVienService.findByNguoiDungId(nd.getId()).orElse(null);
        if (nv == null || nv.getChucVu() == null) return false;

        String ma = nv.getChucVu().getMaChucVu().toUpperCase();
        return ma.equals("TK") || ma.equals("PTK") || ma.equals("TBM") || ma.equals("CNTT");
    }
}