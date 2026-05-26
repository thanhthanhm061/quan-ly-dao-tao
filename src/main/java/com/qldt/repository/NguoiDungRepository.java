package com.qldt.repository;

import com.qldt.model.NguoiDung;
import com.qldt.model.enums.VaiTro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, Long> {
    Optional<NguoiDung> findByUsername(String username);
    Optional<NguoiDung> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<NguoiDung> findByVaiTro(VaiTro vaiTro);
    List<NguoiDung> findByKichHoat(boolean kichHoat);
    List<NguoiDung> findAllByOrderByNgayTaoDesc();

    @Query("SELECT nd FROM NguoiDung nd WHERE " +
            "LOWER(nd.hoTen) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
            "LOWER(nd.username) LIKE LOWER(CONCAT('%',:kw,'%')) OR " +
            "LOWER(nd.email) LIKE LOWER(CONCAT('%',:kw,'%'))")
    List<NguoiDung> timKiem(@Param("kw") String keyword);

    @Query("SELECT nd FROM NguoiDung nd WHERE nd.vaiTro = :vt ORDER BY nd.hoTen")
    List<NguoiDung> findByVaiTroOrderByHoTen(@Param("vt") VaiTro vaiTro);

    @Query("SELECT COUNT(nd) FROM NguoiDung nd WHERE nd.kichHoat = true")
    long demHoatDong();

    @Query("SELECT COUNT(nd) FROM NguoiDung nd WHERE nd.vaiTro = :vt")
    long demTheoVaiTro(@Param("vt") VaiTro vaiTro);
}
