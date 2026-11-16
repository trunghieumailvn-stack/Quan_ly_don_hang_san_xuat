package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Kết nối SQL Server. CHỈ CẦN SỬA PHẦN "CẤU HÌNH" bên dưới cho đúng máy .
 * CSDL và các bảng được tạo bằng file QuanLySanXuat.sql (chạy một lần trong SSMS).
 */
public class KetNoiCsdl {

    // ===================== CẤU HÌNH =====================
    public static final String TEN_MAY_CHU = "localhost";
    public static final String CONG = "1433";                 // chỉ dùng khi tenInstance để trống
    public static final String TEN_INSTANCE = "SQLEXPRESS01"; // tên instance SQL Server
    public static final String TEN_CSDL = "QuanLySanXuat";

    // Đổi thành true để đăng nhập bằng tài khoản Windows, false để dùng tài khoản SQL Server (sa)
    public static final boolean DUNG_XAC_THUC_WINDOWS = false;
    public static final String TEN_DANG_NHAP = "sa";
    public static final String MAT_KHAU = "123456";            // mật khẩu tài khoản sa đã đặt trong SSMS
    // ====================================================

    /** Ghép chuỗi kết nối JDBC từ phần cấu hình ở trên. */
    private static String taoUrl() {
        StringBuilder url = new StringBuilder("jdbc:sqlserver://").append(TEN_MAY_CHU);
        if (TEN_INSTANCE == null || TEN_INSTANCE.trim().isEmpty()) {
            url.append(":").append(CONG);
        } else {
            url.append("\\").append(TEN_INSTANCE.trim());
        }
        url.append(";databaseName=").append(TEN_CSDL);
        url.append(";encrypt=false;trustServerCertificate=true;loginTimeout=10;");
        if (DUNG_XAC_THUC_WINDOWS) {
            url.append("integratedSecurity=true;");
        } else {
            url.append("user=").append(TEN_DANG_NHAP).append(";password=").append(MAT_KHAU).append(";");
        }
        return url.toString();
    }

    /** Mở một kết nối mới tới CSDL. Nơi gọi chịu trách nhiệm đóng (dùng try-with-resources). */
    public static Connection layKetNoi() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Chưa thêm driver JDBC (file mssql-jdbc-*.jar) vào project.");
        }
        return DriverManager.getConnection(taoUrl());
    }

    /**
     * Kiểm tra kết nối và sự tồn tại của các bảng khi khởi động chương trình.
     * @return null nếu mọi thứ sẵn sàng, ngược lại là thông báo lỗi tiếng Việt.
     */
    public static String kiemTraKetNoi() {
        try (Connection kn = layKetNoi(); Statement lenh = kn.createStatement()) {
            lenh.execute("SELECT COUNT(*) FROM TaiKhoan");     // lỗi nếu chưa có bảng
            return null;
        } catch (SQLException e) {
            return giaiThichLoi(e);
        }
    }

    /** Mỗi mục: {các từ khóa cách nhau bằng dấu |, gợi ý tiếng Việt}. Mục nào khớp trước thì được dùng. */
    private static final String[][] CAC_GOI_Y_LOI = {
        {"chưa thêm driver|no suitable driver",
                "Chưa thêm driver JDBC. Hãy đưa file mssql-jdbc-*.jar vào thư mục lib/ và add vào Referenced Libraries."},
        {"integrated authentication|mssql-jdbc_auth|java.library.path",
                "Chưa nhận được file mssql-jdbc_auth-*.x64.dll. Hãy chép file này (cùng phiên bản với file jar, bản x64) vào thư mục bin của JDK, "
                + "sau đó ĐÓNG HẲN rồi mở lại VS Code. Hoặc đặt dungXacThucWindows = false và dùng tài khoản SQL."},
        {"cannot open database",
                "Chưa có CSDL " + TEN_CSDL + ". Hãy mở file QuanLySanXuat.sql trong SSMS và bấm Execute để tạo CSDL."},
        {"login failed", DUNG_XAC_THUC_WINDOWS
                ? "Tài khoản Windows của bạn chưa có quyền đăng nhập SQL Server. Trong SSMS hãy thêm login cho tài khoản này (Security > Logins) với quyền sysadmin."
                : "Sai tên đăng nhập / mật khẩu SQL Server. Kiểm tra tenDangNhap, matKhau trong KetNoiCsdl.java và bật SQL Server Authentication."},
        {"tcp/ip connection|connection refused|connect timed out|the tcp/ip|named instance",
                "Không tìm thấy SQL Server. Bật TCP/IP trong SQL Server Configuration Manager, restart service, bật SQL Server Browser và kiểm tra tenMayChu/cong/tenInstance."},
        {"invalid object name",
                "CSDL chưa có đủ bảng. Hãy chạy file QuanLySanXuat.sql trong SSMS để tạo các bảng."},
        {"primary key|duplicate key",
                "Mã đã tồn tại (trùng khóa chính). Hãy dùng mã khác."},
        {"reference constraint|foreign key",
                "Dữ liệu này đang được dùng ở nơi khác nên không thể xóa hoặc sửa theo cách này."},
        {"check constraint",
                "Giá trị nhập vào vi phạm ràng buộc dữ liệu (ví dụ số lượng phải lớn hơn 0, đơn giá không được âm)."},
        {"tls|ssl",
                "Lỗi bảo mật TLS với SQL Server đời cũ. Cài SQL Server 2014 SP3 (hỗ trợ TLS 1.2) hoặc dùng bản JDBC/JDK phù hợp."},
    };

    /** Chuyển lỗi kỹ thuật của SQL Server thành thông báo tiếng Việt dễ hiểu. */
    public static String giaiThichLoi(Throwable loi) {
        String noiDung = loi.getMessage() == null ? loi.toString() : loi.getMessage();
        String thuong = noiDung.toLowerCase();
        for (String[] muc : CAC_GOI_Y_LOI) {
            for (String tuKhoa : muc[0].split("\\|")) {
                if (thuong.contains(tuKhoa)) return muc[1] + "\n\nChi tiết kỹ thuật:\n" + noiDung;
            }
        }
        return noiDung;
    }
}
