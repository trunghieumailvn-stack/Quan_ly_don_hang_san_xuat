package dao;

import model.ChiTietDonHang;
import model.DonHang;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Truy cập các bảng DonHang, ChiTietDonHang (và đọc số công đoạn từ TienDoSanXuat). */
public class DonHangDao extends DaoCoSo {

    private static final String SQL_THEM_DON_HANG =
            "INSERT INTO DonHang (MaDH, MaKH, NgayNhan, ThoiGianHoanThanh, TrangThai, GhiChu) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_THEM_CHI_TIET =
            "INSERT INTO ChiTietDonHang (MaDH, MaSP, SoLuong, DonGia) VALUES (?, ?, ?, ?)";

    /** Chuyển một dòng kết quả (thông tin chung của đơn) thành đối tượng DonHang. */
    private DonHang anhXaDonHang(ResultSet dong) throws SQLException {
        DonHang donHang = new DonHang();
        donHang.setMaDonHang(dong.getString("MaDH"));
        donHang.setMaKhachHang(dong.getString("MaKH"));
        donHang.setTenKhachHang(dong.getString("TenKH"));
        donHang.setNgayNhan(dong.getTimestamp("NgayNhan"));
        donHang.setThoiGianHoanThanh(dong.getTimestamp("ThoiGianHoanThanh"));
        donHang.setTrangThai(dong.getString("TrangThai"));
        donHang.setGhiChu(dong.getString("GhiChu"));
        donHang.setSoCongDoan(dong.getInt("TongCongDoan"));
        donHang.setSoCongDoanXong(dong.getInt("CongDoanXong"));
        return donHang;
    }

    /** Chuyển một dòng kết quả thành một dòng sản phẩm của đơn. */
    private ChiTietDonHang anhXaChiTiet(ResultSet dong) throws SQLException {
        ChiTietDonHang chiTiet = new ChiTietDonHang(dong.getString("MaSP"), dong.getString("TenSP"),
                dong.getString("DonViTinh"), dong.getInt("SoLuong"), dong.getDouble("DonGia"));
        chiTiet.setMaDonHang(dong.getString("MaDH"));
        return chiTiet;
    }

    /**
     * Lấy tất cả đơn hàng, đơn mới nhận xếp trước. Mỗi đơn kèm danh sách sản phẩm
     * và số công đoạn (tổng / đã xong) để tính tiến độ.
     */
    public List<DonHang> layTatCa() {
        // Bước 1: đọc thông tin chung của các đơn
        List<DonHang> danhSach = truyVan(
                "SELECT d.MaDH, d.MaKH, k.TenKH, d.NgayNhan, d.ThoiGianHoanThanh, d.TrangThai, d.GhiChu, " +
                "(SELECT COUNT(*) FROM TienDoSanXuat t WHERE t.MaDH = d.MaDH) AS TongCongDoan, " +
                "(SELECT COUNT(*) FROM TienDoSanXuat t WHERE t.MaDH = d.MaDH AND t.TrangThai = N'Hoàn thành') AS CongDoanXong " +
                "FROM DonHang d LEFT JOIN KhachHang k ON d.MaKH = k.MaKH " +
                "ORDER BY d.NgayNhan DESC, d.MaDH", this::anhXaDonHang);
        if (layLoiCuoi() != null) return danhSach;

        // Bước 2: đọc tất cả dòng chi tiết rồi gắn vào đúng đơn
        Map<String, DonHang> theoMa = new HashMap<>();   // tra nhanh đơn theo mã để gắn chi tiết
        for (DonHang donHang : danhSach) theoMa.put(donHang.getMaDonHang(), donHang);
        List<ChiTietDonHang> cacChiTiet = truyVan(
                "SELECT c.MaDH, c.MaSP, s.TenSP, s.DonViTinh, c.SoLuong, c.DonGia " +
                "FROM ChiTietDonHang c LEFT JOIN SanPham s ON c.MaSP = s.MaSP ORDER BY c.MaDH, c.MaSP", this::anhXaChiTiet);
        for (ChiTietDonHang chiTiet : cacChiTiet) {
            DonHang donHang = theoMa.get(chiTiet.getMaDonHang());
            if (donHang != null) donHang.getDanhSachChiTiet().add(chiTiet);
        }
        return danhSach;
    }

    /** Nếu khách hàng chưa có trong bảng KhachHang (người dùng gõ tên mới) thì tự thêm để không lỗi khóa ngoại. */
    private void damBaoKhachHangTonTai(Connection ketNoi, DonHang donHang) throws SQLException {
        chayLenh(ketNoi, "IF NOT EXISTS (SELECT 1 FROM KhachHang WHERE MaKH = ?) " +
                        "INSERT INTO KhachHang (MaKH, TenKH) VALUES (?, ?)",
                donHang.getMaKhachHang(), donHang.getMaKhachHang(), donHang.getTenKhachHang());
    }

    /** Ghi từng dòng sản phẩm của đơn vào bảng ChiTietDonHang. */
    private void themChiTiet(Connection ketNoi, DonHang donHang) throws SQLException {
        for (ChiTietDonHang dong : donHang.getDanhSachChiTiet()) {
            chayLenh(ketNoi, SQL_THEM_CHI_TIET, donHang.getMaDonHang(), dong.getMaSanPham(),
                    dong.getSoLuong(), dong.getDonGia());
        }
    }

    /** Thêm đơn hàng mới cùng các dòng sản phẩm (trong một giao dịch). */
    public boolean them(DonHang donHang) {
        return chayGiaoDich(ketNoi -> {
            damBaoKhachHangTonTai(ketNoi, donHang);
            chayLenh(ketNoi, SQL_THEM_DON_HANG, donHang.getMaDonHang(), donHang.getMaKhachHang(),
                    donHang.getNgayNhan(), donHang.getThoiGianHoanThanh(), donHang.getTrangThai(), donHang.getGhiChu());
            themChiTiet(ketNoi, donHang);
        });
    }

    /** Sửa đơn hàng: cập nhật thông tin chung rồi ghi lại toàn bộ danh sách sản phẩm. */
    public boolean capNhat(DonHang donHang) {
        return chayGiaoDich(ketNoi -> {
            damBaoKhachHangTonTai(ketNoi, donHang);
            chayLenh(ketNoi, "UPDATE DonHang SET MaKH = ?, NgayNhan = ?, ThoiGianHoanThanh = ?, TrangThai = ?, GhiChu = ? " +
                            "WHERE MaDH = ?",
                    donHang.getMaKhachHang(), donHang.getNgayNhan(), donHang.getThoiGianHoanThanh(),
                    donHang.getTrangThai(), donHang.getGhiChu(), donHang.getMaDonHang());
            chayLenh(ketNoi, "DELETE FROM ChiTietDonHang WHERE MaDH = ?", donHang.getMaDonHang());
            themChiTiet(ketNoi, donHang);
        });
    }

    /** Đánh dấu đơn hoàn thành và chuyển luôn các công đoạn còn lại sang "Hoàn thành". */
    public boolean danhDauHoanThanh(String maDonHang) {
        return chayGiaoDich(ketNoi -> {
            chayLenh(ketNoi, "UPDATE DonHang SET TrangThai = N'Đã hoàn thành', ThoiGianHoanThanh = GETDATE() WHERE MaDH = ?",
                    maDonHang);
            chayLenh(ketNoi, "UPDATE TienDoSanXuat SET NgayBatDau = ISNULL(NgayBatDau, GETDATE()), " +
                            "NgayKetThuc = ISNULL(NgayKetThuc, GETDATE()), TrangThai = N'Hoàn thành' " +
                            "WHERE MaDH = ? AND TrangThai <> N'Hoàn thành'",
                    maDonHang);
        });
    }

    /** Xóa đơn hàng. Chi tiết và tiến độ của đơn tự bị xóa theo nhờ ON DELETE CASCADE trong SQL. */
    public boolean xoa(String maDonHang) {
        return thucThi("DELETE FROM DonHang WHERE MaDH = ?", maDonHang);
    }
}
