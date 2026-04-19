package com.qldt.service;

import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class NguoiDungService {

    private final NguoiDungRepository nguoiDungRepo;
    private final PasswordEncoder passwordEncoder;

    public NguoiDung taoTaiKhoan(String username, String hoTen, String email, VaiTro vaiTro) {

        return nguoiDungRepo.findByUsername(username)
                .orElseGet(() -> {
                    NguoiDung nd = NguoiDung.builder()
                            .username(username)
                            .matKhau(passwordEncoder.encode("Admin@123"))
                            .hoTen(hoTen)
                            .email(email != null ? email : username + "@qldt.edu.vn")
                            .vaiTro(vaiTro)
                            .kichHoat(true)
                            .build();
                    return nguoiDungRepo.save(nd);
                });
    }
}