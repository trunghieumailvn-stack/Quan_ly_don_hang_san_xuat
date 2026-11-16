package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp nền cho các lớp Dao. Gom các việc lặp đi lặp lại:
 *  - ghi nhớ lỗi của thao tác gần nhất (để giao diện hiển thị);
 *  - chạy truy vấn SELECT và chuyển từng dòng thành đối tượng;
 *  - chạy lệnh INSERT / UPDATE / DELETE có tham số;
 *  - chạy nhiều lệnh trong một giao dịch (hoặc thành công hết, hoặc hoàn tác hết).
 */
public abstract class DaoCoSo {

    private String loiCuoi;

    /** Thông báo lỗi của thao tác gần nhất, null nếu thao tác đó thành công. */
    public String layLoiCuoi() { return loiCuoi; }

    protected void xoaLoi() { loiCuoi = null; }

    protected void datLoi(String noiDung) { loiCuoi = noiDung; }

    /** In lỗi ra cho người lập trình và người dùng. */
    protected void ghiLoi(SQLException e) {
        e.printStackTrace();
        loiCuoi = KetNoiCsdl.giaiThichLoi(e);
    }

    /** Hàm chuyển một dòng kết quả (ResultSet) thành một đối tượng. */
    @FunctionalInterface
    protected interface AnhXaDong<T> {
        T anhXa(ResultSet dong) throws SQLException;
    }

    /** Một nhóm lệnh SQL chạy chung trên một kết nối trong giao dịch. */
    @FunctionalInterface
    protected interface CongViec {
        void chay(Connection ketNoi) throws SQLException;
    }

    /** Gán danh sách tham số vào câu lệnh. Ngày giờ đổi sang Timestamp, null gán là NULL của SQL. */
    private static void ganThamSo(PreparedStatement lenh, Object[] thamSo) throws SQLException {
        for (int i = 0; i < thamSo.length; i++) {
            Object giaTri = thamSo[i];
            if (giaTri == null) {
                lenh.setNull(i + 1, Types.NVARCHAR);
            } else if (giaTri instanceof java.util.Date) {
                lenh.setTimestamp(i + 1, new Timestamp(((java.util.Date) giaTri).getTime()));
            } else {
                lenh.setObject(i + 1, giaTri);
            }
        }
    }

    /** Chạy một lệnh INSERT / UPDATE / DELETE trên kết nối có sẵn. Trả về số dòng bị ảnh hưởng. */
    protected static int chayLenh(Connection ketNoi, String sql, Object... thamSo) throws SQLException {
        try (PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            ganThamSo(lenh, thamSo);
            return lenh.executeUpdate();
        }
    }

    /** Chạy một lệnh INSERT / UPDATE / DELETE. Trả về true nếu có ít nhất một dòng bị ảnh hưởng. */
    protected boolean thucThi(String sql, Object... thamSo) {
        xoaLoi();
        try (Connection ketNoi = KetNoiCsdl.layKetNoi()) {
            return chayLenh(ketNoi, sql, thamSo) > 0;
        } catch (SQLException e) {
            ghiLoi(e);
            return false;
        }
    }

    /** Chạy câu SELECT, mỗi dòng kết quả được anhXa thành một đối tượng. Lỗi thì trả về danh sách rỗng. */
    protected <T> List<T> truyVan(String sql, AnhXaDong<T> anhXa, Object... thamSo) {
        xoaLoi();
        List<T> danhSach = new ArrayList<>();
        try (Connection ketNoi = KetNoiCsdl.layKetNoi();
             PreparedStatement lenh = ketNoi.prepareStatement(sql)) {
            ganThamSo(lenh, thamSo);
            try (ResultSet ketQua = lenh.executeQuery()) {
                while (ketQua.next()) danhSach.add(anhXa.anhXa(ketQua));
            }
        } catch (SQLException e) {
            ghiLoi(e);
        }
        return danhSach;
    }

    /** Chạy nhiều lệnh trong một giao dịch. Có lỗi ở bất kỳ lệnh nào thì hoàn tác tất cả. */
    protected boolean chayGiaoDich(CongViec congViec) {
        xoaLoi();
        try (Connection ketNoi = KetNoiCsdl.layKetNoi()) {
            ketNoi.setAutoCommit(false);
            try {
                congViec.chay(ketNoi);
                ketNoi.commit();
                return true;
            } catch (SQLException e) {
                ketNoi.rollback();
                throw e;
            }
        } catch (SQLException e) {
            ghiLoi(e);
            return false;
        }
    }
}
