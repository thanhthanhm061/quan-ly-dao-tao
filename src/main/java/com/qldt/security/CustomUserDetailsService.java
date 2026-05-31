// CustomUserDetailsService.java
package com.qldt.security;

import com.qldt.model.NguoiDung;
import com.qldt.repository.NguoiDungRepository;
import com.qldt.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepo;
    private final NhanVienRepository nhanVienRepo;  // ← thêm inject này

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Role chính
        authorities.add(new SimpleGrantedAuthority("ROLE_" + nd.getVaiTro().name()));

        // Nếu là nhân viên → thêm ROLE theo mã chức vụ (TK, PTK, TBM, CNTT...)
        // Nếu là nhân viên → thêm ROLE theo mã chức vụ
        if (nd.getVaiTro().name().equals("NHAN_VIEN")) {
            try {
                nhanVienRepo.findByNguoiDungUsername(username).ifPresent(nv -> {
                    if (nv.getChucVu() != null && nv.getChucVu().getMaChucVu() != null) {
                        String ma = nv.getChucVu().getMaChucVu().toUpperCase();
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + ma));
                        System.out.println(">>> GRANTED: ROLE_" + ma + " cho " + username);
                    }
                });
            } catch (Exception e) {
                // Không có chức vụ → vẫn login được với ROLE_NHAN_VIEN
                System.out.println(">>> Không load được chức vụ cho: " + username + " - " + e.getMessage());
            }
        }

        return User.builder()
                .username(nd.getUsername())
                .password(nd.getMatKhau())
                .authorities(authorities)
                .disabled(!nd.isKichHoat())
                .build();
    }
}