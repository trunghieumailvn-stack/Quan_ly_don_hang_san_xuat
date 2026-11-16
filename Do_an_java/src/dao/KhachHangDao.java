package dao;

import model.KhachHang;

import java.util.List;

/** Truy cập bảng KhachHang. */
public class KhachHangDao extends DaoCoSo {

    /** Lấy toàn bộ khách hàng, sắp theo tên. */
    public List<KhachHang> layTatCa() {
        return truyVan("SELECT MaKH, TenKH, SoDienThoai, DiaChi FROM KhachHang ORDER BY TenKH",
                dong -> new KhachHang(dong.getString("MaKH"), dong.getString("TenKH"),
                        dong.getString("SoDienThoai"), dong.getString("DiaChi")));
    }

    public boolean them(KhachHang khachHang) {
        return thucThi("INSERT INTO KhachHang (MaKH, TenKH, SoDienThoai, DiaChi) VALUES (?, ?, ?, ?)",
                khachHang.getMaKhachHang(), khachHang.getTenKhachHang(),
                khachHang.getSoDienThoai(), khachHang.getDiaChi());
    }

    /** Cập nhật theo mã khách hàng (mã không đổi được). */
    public boolean capNhat(KhachHang khachHang) {
        return thucThi("UPDATE KhachHang SET TenKH = ?, SoDienThoai = ?, DiaChi = ? WHERE MaKH = ?",
                khachHang.getTenKhachHang(), khachHang.getSoDienThoai(),
                khachHang.getDiaChi(), khachHang.getMaKhachHang());
    }

    /** Xóa thất bại (trả về false) nếu khách hàng còn đơn hàng, do khóa ngoại. */
    public boolean xoa(String maKhachHang) {
        return thucThi("DELETE FROM KhachHang WHERE MaKH = ?", maKhachHang);
    }
}
