package dao;

import model.TaiKhoan;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** Truy cập bảng TaiKhoan. Mật khẩu luôn được băm SHA-256 trước khi lưu hoặc so sánh. */
public class TaiKhoanDao extends DaoCoSo {

    private static final String COT_TAI_KHOAN = "TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai";

    /** Băm mật khẩu bằng SHA-256, trả về chuỗi hex 64 ký tự (khớp với dữ liệu mẫu trong file SQL). */
    public static String bamMatKhau(String matKhauThuong) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] byteBam = sha.digest(matKhauThuong.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : byteBam) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Chuyển một dòng kết quả thành đối tượng TaiKhoan. */
    private TaiKhoan anhXaTaiKhoan(ResultSet dong) throws SQLException {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(dong.getString("TenDangNhap"));
        taiKhoan.setMatKhau(dong.getString("MatKhau"));
        taiKhoan.setHoTen(dong.getString("HoTen"));
        taiKhoan.setVaiTro(dong.getString("VaiTro"));
        taiKhoan.setHoatDong(dong.getBoolean("TrangThai"));
        return taiKhoan;
    }

    /**
     * Kiểm tra đăng nhập.
     * @return tài khoản nếu đúng; null nếu sai (gọi layLoiCuoi() để biết lý do).
     */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhauThuong) {
        List<TaiKhoan> ketQua = truyVan("SELECT " + COT_TAI_KHOAN + " FROM TaiKhoan WHERE TenDangNhap = ? AND MatKhau = ?",
                this::anhXaTaiKhoan, tenDangNhap, bamMatKhau(matKhauThuong));
        if (layLoiCuoi() != null) return null;            // lỗi kết nối hoặc lỗi SQL
        if (ketQua.isEmpty()) {
            datLoi("Sai tên đăng nhập hoặc mật khẩu.");
            return null;
        }
        TaiKhoan taiKhoan = ketQua.get(0);
        if (!taiKhoan.isHoatDong()) {
            datLoi("Tài khoản này đã bị khóa. Hãy liên hệ quản trị viên.");
            return null;
        }
        return taiKhoan;
    }

    public List<TaiKhoan> layTatCa() {
        return truyVan("SELECT " + COT_TAI_KHOAN + " FROM TaiKhoan ORDER BY VaiTro, TenDangNhap", this::anhXaTaiKhoan);
    }

    /** matKhauThuong là mật khẩu chưa băm; hàm tự băm trước khi lưu. */
    public boolean them(String tenDangNhap, String matKhauThuong, String hoTen, String vaiTro) {
        return thucThi("INSERT INTO TaiKhoan (TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai) VALUES (?, ?, ?, ?, 1)",
                tenDangNhap, bamMatKhau(matKhauThuong), hoTen, vaiTro);
    }

    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi) {
        return thucThi("UPDATE TaiKhoan SET MatKhau = ? WHERE TenDangNhap = ?", bamMatKhau(matKhauMoi), tenDangNhap);
    }

    /** hoatDong = false để khóa tài khoản, true để mở khóa. */
    public boolean datHoatDong(String tenDangNhap, boolean hoatDong) {
        return thucThi("UPDATE TaiKhoan SET TrangThai = ? WHERE TenDangNhap = ?", hoatDong, tenDangNhap);
    }

    public boolean xoa(String tenDangNhap) {
        return thucThi("DELETE FROM TaiKhoan WHERE TenDangNhap = ?", tenDangNhap);
    }
}
