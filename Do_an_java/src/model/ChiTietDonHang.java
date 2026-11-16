package model;

/** Một dòng sản phẩm trong đơn hàng (bảng ChiTietDonHang). */
public class ChiTietDonHang {
    private String maDonHang;
    private String maSanPham;
    private String tenSanPham;    // lấy từ bảng SanPham khi truy vấn
    private String donViTinh;     // lấy từ bảng SanPham khi truy vấn
    private int soLuong;
    private double donGia;

    public ChiTietDonHang() {}

    public ChiTietDonHang(String maSanPham, String tenSanPham, String donViTinh, int soLuong, double donGia) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    /** Thành tiền = số lượng x đơn giá (trong SQL là cột tính toán ThanhTien). */
    public double getThanhTien() { return soLuong * donGia; }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { this.donGia = donGia; }
}
