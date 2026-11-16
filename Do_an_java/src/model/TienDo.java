package model;

import java.util.Date;

/** Một công đoạn sản xuất của đơn hàng (bảng TienDoSanXuat). */
public class TienDo {
    // Ba trạng thái của công đoạn, phải khớp với ràng buộc CHECK trong file SQL
    public static final String TRANG_THAI_CHUA_BAT_DAU = "Chưa bắt đầu";
    public static final String TRANG_THAI_DANG_THUC_HIEN = "Đang thực hiện";
    public static final String TRANG_THAI_HOAN_THANH = "Hoàn thành";

    private int maTienDo;
    private String maDonHang;
    private int thuTu;                    // thứ tự công đoạn trong đơn (1, 2, 3...)
    private String tenCongDoan;
    private String trangThai = TRANG_THAI_CHUA_BAT_DAU;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private String ghiChu;

    public TienDo() {}

    public int getMaTienDo() { return maTienDo; }
    public void setMaTienDo(int maTienDo) { this.maTienDo = maTienDo; }
    public String getMaDonHang() { return maDonHang; }
    public void setMaDonHang(String maDonHang) { this.maDonHang = maDonHang; }
    public int getThuTu() { return thuTu; }
    public void setThuTu(int thuTu) { this.thuTu = thuTu; }
    public String getTenCongDoan() { return tenCongDoan; }
    public void setTenCongDoan(String tenCongDoan) { this.tenCongDoan = tenCongDoan; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
