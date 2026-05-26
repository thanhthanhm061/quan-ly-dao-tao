package com.qldt.service;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoiMatKhauDTO {

    @NotBlank(message = "Mật khẩu cũ không được trống")
    private String matKhauCu;

    @NotBlank(message = "Mật khẩu mới không được trống")
    @Size(min = 6, max = 50, message = "Mật khẩu mới phải từ 6 đến 50 ký tự")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).+$",
            message = "Mật khẩu phải có ít nhất 1 chữ hoa và 1 chữ số")
    private String matKhauMoi;

    @NotBlank(message = "Xác nhận mật khẩu không được trống")
    private String xacNhanMatKhau;

    public boolean isKhop() {
        return matKhauMoi != null && matKhauMoi.equals(xacNhanMatKhau);
    }
}
