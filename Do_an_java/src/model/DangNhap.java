package model;

/** Lưu tài khoản đang đăng nhập trong suốt thời gian chương trình chạy. */
public final class DangNhap {
    private static TaiKhoan taiKhoanHienTai;

    private DangNhap() {}

    public static TaiKhoan layTaiKhoanHienTai() { return taiKhoanHienTai; }
    public static void datTaiKhoanHienTai(TaiKhoan taiKhoan) { taiKhoanHienTai = taiKhoan; }

    /** Gọi khi đăng xuất. */
    public static void xoaPhien() { taiKhoanHienTai = null; }

    /** Dùng để ẩn hoặc hiện các chức năng chỉ dành cho Admin. */
    public static boolean laAdmin() { return taiKhoanHienTai != null && taiKhoanHienTai.isAdmin(); }
}
