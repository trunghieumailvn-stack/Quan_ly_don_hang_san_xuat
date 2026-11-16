package model;

/** Tài khoản đăng nhập (bảng TaiKhoan). */
public class TaiKhoan {
    // Hai vai trò, phải khớp với ràng buộc CHECK trong file SQL
    public static final String VAI_TRO_ADMIN = "Admin";
    public static final String VAI_TRO_NHAN_VIEN = "Nhân viên";

    private String tenDangNhap;
    private String matKhau;               // luôn là chuỗi đã băm SHA-256, không lưu mật khẩu thật
    private String hoTen;
    private String vaiTro;
    private boolean hoatDong = true;      // false nghĩa là tài khoản đang bị khóa

    public TaiKhoan() {}

    public boolean isAdmin() { return VAI_TRO_ADMIN.equalsIgnoreCase(vaiTro); }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public boolean isHoatDong() { return hoatDong; }
    public void setHoatDong(boolean hoatDong) { this.hoatDong = hoatDong; }
}
