package dao;

import model.SanPham;

import java.util.List;

/** Truy cập bảng SanPham. */
public class SanPhamDao extends DaoCoSo {

    /** Lấy toàn bộ sản phẩm, sắp theo mã. */
    public List<SanPham> layTatCa() {
        return truyVan("SELECT MaSP, TenSP, DonViTinh, DonGiaChuan FROM SanPham ORDER BY MaSP",
                dong -> new SanPham(dong.getString("MaSP"), dong.getString("TenSP"),
                        dong.getString("DonViTinh"), dong.getDouble("DonGiaChuan")));
    }

    public boolean them(SanPham sanPham) {
        return thucThi("INSERT INTO SanPham (MaSP, TenSP, DonViTinh, DonGiaChuan) VALUES (?, ?, ?, ?)",
                sanPham.getMaSanPham(), sanPham.getTenSanPham(),
                sanPham.getDonViTinh(), sanPham.getDonGiaChuan());
    }

    /** Cập nhật theo mã sản phẩm (mã không đổi được). */
    public boolean capNhat(SanPham sanPham) {
        return thucThi("UPDATE SanPham SET TenSP = ?, DonViTinh = ?, DonGiaChuan = ? WHERE MaSP = ?",
                sanPham.getTenSanPham(), sanPham.getDonViTinh(),
                sanPham.getDonGiaChuan(), sanPham.getMaSanPham());
    }

    /** Xóa thất bại (trả về false) nếu sản phẩm đang nằm trong đơn hàng, do khóa ngoại. */
    public boolean xoa(String maSanPham) {
        return thucThi("DELETE FROM SanPham WHERE MaSP = ?", maSanPham);
    }
}
