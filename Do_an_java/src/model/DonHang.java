package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Thông tin chung của một đơn hàng (bảng DonHang).
 * Các sản phẩm của đơn nằm trong danhSachChiTiet (bảng ChiTietDonHang).
 */
public class DonHang {
    // Hai trạng thái của đơn, phải khớp với ràng buộc CHECK trong file SQL
    public static final String TRANG_THAI_DA_HOAN_THANH = "Đã hoàn thành";
    public static final String TRANG_THAI_CHUA_HOAN_THANH = "Chưa hoàn thành";

    private String maDonHang;
    private String maKhachHang;
    private String tenKhachHang;          // lấy từ bảng KhachHang khi truy vấn
    private Date ngayNhan;                // thời gian nhận đơn
    private Date thoiGianHoanThanh;       // null khi đơn chưa hoàn thành
    private String trangThai;
    private String ghiChu;
    private int soCongDoan;               // tổng số công đoạn sản xuất của đơn
    private int soCongDoanXong;           // số công đoạn đã hoàn thành
    private List<ChiTietDonHang> danhSachChiTiet = new ArrayList<>();

    public DonHang() {}

    /** Tổng số lượng của tất cả sản phẩm trong đơn. */
    public int getTongSoLuong() {
        int tong = 0;
        for (ChiTietDonHang dong : danhSachChiTiet) tong += dong.getSoLuong();
        return tong;
    }

    /** Tổng tiền của đơn = cộng thành tiền của từng dòng. */
    public double getTongTien() {
        double tong = 0;
        for (ChiTietDonHang dong : danhSachChiTiet) tong += dong.getThanhTien();
        return tong;
    }

    public boolean isHoanThanh() { return TRANG_THAI_DA_HOAN_THANH.equalsIgnoreCase(trangThai); }

    /** Phần trăm tiến độ từ 0 đến 100. Trả về -1 nếu đơn chưa có công đoạn nào. */
    public int getPhanTramTienDo() {
        if (soCongDoan > 0) return (int) Math.round(soCongDoanXong * 100.0 / soCongDoan);
        return isHoanThanh() ? 100 : -1;
    }

    /** Tóm tắt sản phẩm để hiện trong bảng, ví dụ "Bàn làm việc gỗ Sồi  +1 mặt hàng". */
    public String getTomTatSanPham() {
        if (danhSachChiTiet.isEmpty()) return "---";
        ChiTietDonHang dongDau = danhSachChiTiet.get(0);
        String ten = dongDau.getTenSanPham() != null ? dongDau.getTenSanPham() : dongDau.getMaSanPham();
        int soMatHangThem = danhSachChiTiet.size() - 1;
        return soMatHangThem > 0 ? ten + "  +" + soMatHangThem + " mặt hàng" : ten;
    }

    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { this.maKhachHang = maKhachHang; }
    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }
    public Date getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(Date ngayNhan) { this.ngayNhan = ngayNhan; }
    public Date getThoiGianHoanThanh() { return thoiGianHoanThanh; }
    public void setThoiGianHoanThanh(Date thoiGianHoanThanh) { this.thoiGianHoanThanh = thoiGianHoanThanh; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public int getSoCongDoan() { return soCongDoan; }
    public void setSoCongDoan(int soCongDoan) { this.soCongDoan = soCongDoan; }
    public int getSoCongDoanXong() { return soCongDoanXong; }
    public void setSoCongDoanXong(int soCongDoanXong) { this.soCongDoanXong = soCongDoanXong; }
    public List<ChiTietDonHang> getDanhSachChiTiet() { return danhSachChiTiet; }
    public void setDanhSachChiTiet(List<ChiTietDonHang> danhSachChiTiet) { this.danhSachChiTiet = danhSachChiTiet; }
}
