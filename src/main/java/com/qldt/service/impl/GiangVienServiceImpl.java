package com.qldt.service.impl;
import com.qldt.model.GiangVien;
import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import com.qldt.repository.*;
import com.qldt.service.GiangVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor @Transactional
public class GiangVienServiceImpl implements GiangVienService {
    private final GiangVienRepository repo;
    private final NguoiDungRepository nguoiDungRepo;
    private final PasswordEncoder passwordEncoder;

    @Override @Transactional(readOnly = true)
    public List<GiangVien> findAll() { return repo.findAll(); }

    @Override @Transactional(readOnly = true)
    public List<GiangVien> search(String kw) {
        return kw == null || kw.isBlank() ? findAll() : repo.findByHoTenContainingIgnoreCase(kw);
    }

    @Override @Transactional(readOnly = true)
    public Optional<GiangVien> findById(Long id) { return repo.findById(id); }

    @Override @Transactional(readOnly = true)
    public Optional<GiangVien> findByNguoiDungId(Long id) { return repo.findByNguoiDungId(id); }

    @Override
    public GiangVien save(GiangVien gv) {
        if (gv.getId() == null) {
            if (repo.existsByMaGv(gv.getMaGv()))
                throw new IllegalArgumentException("Mã giảng viên '" + gv.getMaGv() + "' đã tồn tại");

            // Chỉ tạo tài khoản nếu chưa có
            if (gv.getNguoiDung() == null) {
                String username = gv.getMaGv().toLowerCase();

                // Dùng lại nếu username đã tồn tại, tránh duplicate
                NguoiDung nd = nguoiDungRepo.findByUsername(username)
                        .orElseGet(() -> {
                            NguoiDung newNd = NguoiDung.builder()
                                    .username(username)
                                    .matKhau(passwordEncoder.encode("Gv@" + gv.getMaGv()))
                                    .hoTen(gv.getHoTen())
                                    .email(gv.getEmail())
                                    .vaiTro(VaiTro.GIANG_VIEN)
                                    .kichHoat(true)
                                    .build();
                            return nguoiDungRepo.save(newNd);
                        });

                gv.setNguoiDung(nd);
            }
        }
        return repo.save(gv);
    }

    @Override
    public void delete(Long id) {
        GiangVien gv = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giảng viên"));
        if (gv.getLopHocPhans() != null && !gv.getLopHocPhans().isEmpty())
            throw new IllegalStateException("Không thể xóa! Giảng viên đang phụ trách "
                    + gv.getLopHocPhans().size() + " lớp học phần");
        repo.deleteById(id);
        if (gv.getNguoiDung() != null)
            nguoiDungRepo.deleteById(gv.getNguoiDung().getId());
    }

    @Override @Transactional(readOnly = true)
    public long count() { return repo.count(); }
}