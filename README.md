# HỆ THỐNG QUẢN LÝ ĐÀO TẠO - Spring Boot

## Công nghệ
- **Spring Boot 3.2** + **Spring Security** + **Spring Data JPA (Hibernate)**
- **Thymeleaf** + **Bootstrap 5** (giao diện web)
- **MySQL 8.0** (database)
- **Lombok** (giảm boilerplate code)

## Cài đặt & Chạy

### Bước 1: Tạo Database MySQL
```sql
CREATE DATABASE qldt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 2: Cấu hình kết nối
Mở `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=MẬT_KHẨU_MYSQL_CỦA_BẠN
```

### Bước 3: Chạy ứng dụng
```bash
mvn spring-boot:run
```
Mở trình duyệt: **http://localhost:8080**

## Tài khoản mặc định (tự tạo khi khởi động lần đầu)
| Username | Mật khẩu   | Vai trò     |
|----------|------------|-------------|
| admin    | Admin@123  | Quản trị    |
| gv001    | Admin@123  | Giảng viên  |
| sv001    | Admin@123  | Sinh viên   |

## Tính năng
1. **Đăng nhập + Phân quyền** - Spring Security BCrypt, 3 role tự động redirect
2. **Quản lý Sinh viên** - CRUD + tìm kiếm + tự tạo tài khoản
3. **Quản lý Giảng viên** - CRUD + xem lớp đang dạy + cập nhật điểm + tự tạo tài khoản
4. **Quản lý Môn học** - CRUD + ràng buộc xóa
5. **Quản lý Lớp** - CRUD
6. **Lớp học phần** - Mở lớp, đăng ký SV, kiểm tra trùng lịch, cập nhật điểm
7. **Thời khóa biểu** - Hiển thị dạng lưới theo ngày/tiết, kiểm tra trùng lịch GV

## Cấu trúc project
```
src/main/java/com/qldt/
├── QuanLyDaoTaoApplication.java
├── config/
│   ├── SecurityConfig.java      ← Spring Security config
│   └── DataInitializer.java     ← Tự tạo dữ liệu mẫu
├── model/                       ← JPA Entities
│   ├── enums/                   ← VaiTro, GioiTinh, LoaiMon...
│   └── (NguoiDung, SinhVien, GiangVien, Khoa, Lop, MonHoc, LopHocPhan, ThoiKhoaBieu, DangKy)
├── repository/                  ← Spring Data JPA Repositories
├── service/                     ← Business logic interfaces
├── service/impl/                ← Service implementations
├── controller/                  ← Spring MVC Controllers
└── security/
    └── CustomUserDetailsService.java

src/main/resources/
├── application.properties
└── templates/
    ├── layout/base.html         ← Layout chung (sidebar + topbar)
    ├── auth/login.html
    ├── admin/dashboard.html
    ├── sinhvien/               ← list, form, dashboard, dang-ky
    ├── giangvien/              ← list, form, dashboard, danh-sach-sv
    ├── monhoc/                 ← list, form
    ├── lop/                    ← list, form
    ├── lophocphan/             ← list, form, danh-sach
    └── thoikhoabieu/           ← xem, cua-toi, form
```
