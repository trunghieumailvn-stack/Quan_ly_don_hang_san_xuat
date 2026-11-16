package dao;

import model.TienDo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Truy cập bảng TienDoSanXuat (các công đoạn sản xuất của từng đơn hàng). */
public class TienDoDao extends DaoCoSo {

    private static final String SQL_THEM =
            "INSERT INTO TienDoSanXuat (MaDH, ThuTu, TenCongDoan, TrangThai, NgayBatDau, NgayKetThuc, GhiChu) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /** Chuyển một dòng kết quả thành đối tượng TienDo. */
    private TienDo anhXaTienDo(ResultSet dong) throws SQLException {
        TienDo tienDo = new TienDo();
        tienDo.setMaTienDo(dong.getInt("MaTienDo"));
        tienDo.setMaDonHang(dong.getString("MaDH"));
        tienDo.setThuTu(dong.getInt("ThuTu"));
        tienDo.setTenCongDoan(dong.getString("TenCongDoan"));
        tienDo.setTrangThai(dong.getString("TrangThai"));
        tienDo.setNgayBatDau(dong.getTimestamp("NgayBatDau"));
        tienDo.setNgayKetThuc(dong.getTimestamp("NgayKetThuc"));
        tienDo.setGhiChu(dong.getString("GhiChu"));
        return tienDo;
    }

    /** Lấy các công đoạn của một đơn hàng, theo thứ tự công đoạn. */
    public List<TienDo> layTheoDonHang(String maDonHang) {
        return truyVan("SELECT MaTienDo, MaDH, ThuTu, TenCongDoan, TrangThai, NgayBatDau, NgayKetThuc, GhiChu " +
                "FROM TienDoSanXuat WHERE MaDH = ? ORDER BY ThuTu, MaTienDo", this::anhXaTienDo, maDonHang);
    }

    /** Thêm công đoạn. Nếu thuTu <= 0 thì tự xếp vào cuối danh sách của đơn. */
    public boolean them(TienDo tienDo) {
        int thuTu = tienDo.getThuTu();
        if (thuTu <= 0) {
            thuTu = truyVan("SELECT ISNULL(MAX(ThuTu), 0) + 1 FROM TienDoSanXuat WHERE MaDH = ?",
                    dong -> dong.getInt(1), tienDo.getMaDonHang()).stream().findFirst().orElse(1);
        }
        return thucThi(SQL_THEM, tienDo.getMaDonHang(), thuTu, tienDo.getTenCongDoan(), tienDo.getTrangThai(),
                tienDo.getNgayBatDau(), tienDo.getNgayKetThuc(), tienDo.getGhiChu());
    }

    public boolean capNhat(TienDo tienDo) {
        return thucThi("UPDATE TienDoSanXuat SET ThuTu = ?, TenCongDoan = ?, TrangThai = ?, " +
                        "NgayBatDau = ?, NgayKetThuc = ?, GhiChu = ? WHERE MaTienDo = ?",
                tienDo.getThuTu(), tienDo.getTenCongDoan(), tienDo.getTrangThai(),
                tienDo.getNgayBatDau(), tienDo.getNgayKetThuc(), tienDo.getGhiChu(), tienDo.getMaTienDo());
    }

    public boolean xoa(int maTienDo) {
        return thucThi("DELETE FROM TienDoSanXuat WHERE MaTienDo = ?", maTienDo);
    }

    /** Tạo sẵn 4 công đoạn mẫu cho đơn hàng (cả 4 cùng thành công hoặc cùng không tạo). */
    public boolean taoCongDoanMau(String maDonHang) {
        String[] tenCongDoan = {"Chuẩn bị nguyên liệu", "Sản xuất", "Kiểm tra chất lượng", "Đóng gói"};
        return chayGiaoDich(ketNoi -> {
            for (int i = 0; i < tenCongDoan.length; i++) {
                chayLenh(ketNoi, SQL_THEM, maDonHang, i + 1, tenCongDoan[i], TienDo.TRANG_THAI_CHUA_BAT_DAU, null, null, null);
            }
        });
    }
}
