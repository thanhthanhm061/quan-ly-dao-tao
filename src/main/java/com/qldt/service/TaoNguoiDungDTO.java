package com.qldt.service;

import com.qldt.model.enums.VaiTro;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TaoNguoiDungDTO {

    @NotBlank(message = "Tên đăng nhập không được trống")
    @Pattern(regexp = "^[a-z0-9_]{3,50}$",
            message = "Chỉ gồm chữ thường, số, dấu _ (3-50 ký tự)")
    private String username;

    @NotBlank(message = "Họ tên không được trống")
    private String hoTen;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String soDienThoai;

    @NotNull(message = "Vui lòng chọn vai trò")
    private VaiTro vaiTro;

    @NotBlank(message = "Mật khẩu không được trống")
    @Size(min = 6, message = "Mật khẩu phải ít nhất 6 ký tự")
    private String matKhau;

    private String ghiChu;
    private boolean kichHoat = true;
}

