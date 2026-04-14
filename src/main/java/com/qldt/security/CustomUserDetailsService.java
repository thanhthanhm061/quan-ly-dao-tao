package com.qldt.security;

import com.qldt.model.NguoiDung;
import com.qldt.repository.NguoiDungRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final NguoiDungRepository nguoiDungRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        return User.builder()
            .username(nd.getUsername())
            .password(nd.getMatKhau())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + nd.getVaiTro().name())))
            .disabled(!nd.isKichHoat())
            .build();
    }
}
