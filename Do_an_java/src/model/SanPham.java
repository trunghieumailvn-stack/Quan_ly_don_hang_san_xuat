package model;

/** Sản phẩm trong danh mục (bảng SanPham). */
public class SanPham {
    private String maSanPham;
    private String tenSanPham;
    private String donViTinh;
    private double donGiaChuan;   // giá gợi ý, tự điền vào khi lập đơn

    public SanPham() {}

    public SanPham(String maSanPham, String tenSanPham, String donViTinh, double donGiaChuan) {
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.donViTinh = donViTinh;
        this.donGiaChuan = donGiaChuan;
    }

    public String getMaSanPham() { return maSanPham; }
    public void setMaSanPham(String maSanPham) { this.maSanPham = maSanPham; }
    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }
    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }
    public double getDonGiaChuan() { return donGiaChuan; }
    public void setDonGiaChuan(double donGiaChuan) { this.donGiaChuan = donGiaChuan; }

    @Override
    public String toString() { return tenSanPham; }
}
