package view;

import model.DangNhap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Khung chung cho các trang danh mục (Khách hàng, Sản phẩm): ô tìm kiếm, bảng và nút Thêm / Sửa / Xóa.
 * Lớp con chỉ cần khai báo cột của bảng, form nhập và cách lưu vào CSDL.
 *
 * @param <T> kiểu dữ liệu của danh mục (KhachHang, SanPham...)
 */
public abstract class DanhMuc<T> extends JPanel {

    private final String doiTuong;                       // tên gọi dùng trong thông báo, ví dụ "khách hàng"
    private final DefaultTableModel moHinhBang;
    private final JTable bang;
    private final GiaoDien.OTimKiem txtTimKiem = new GiaoDien.OTimKiem(26);
    private List<T> tatCa = new ArrayList<>();           // toàn bộ dữ liệu đọc từ CSDL
    private final List<T> dangHienThi = new ArrayList<>(); // dữ liệu sau khi lọc theo từ khóa, đúng thứ tự dòng của bảng

    protected DanhMuc(String doiTuong, String[] cacCot, int[] doRongCot, int... cotCanPhai) {
        this.doiTuong = doiTuong;
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 24, 18, 24));

        moHinhBang = GiaoDien.taoMoHinhBang(cacCot);
        bang = new JTable(moHinhBang);
        GiaoDien.dinhDangBang(bang, doRongCot, cotCanPhai);
        GiaoDien.khiNhapDup(bang, this::suaDong);                    // nhấp đúp vào một dòng để sửa

        txtTimKiem.datGoiY("Tìm " + doiTuong + "...");
        GiaoDien.khiGoChu(txtTimKiem, this::locDuLieu);              // tìm ngay khi gõ

        GiaoDien.NutBoGoc btnThem = new GiaoDien.NutBoGoc("+  Thêm " + doiTuong, GiaoDien.MAU_CHINH, Color.WHITE);
        btnThem.addActionListener(e -> themDong());
        GiaoDien.NutBoGoc btnSua = GiaoDien.nutPhu("Sửa");
        btnSua.addActionListener(e -> suaDong());
        GiaoDien.NutBoGoc btnXoa = new GiaoDien.NutBoGoc("Xóa", GiaoDien.MAU_DO, Color.WHITE);
        btnXoa.addActionListener(e -> xoaDong());
        btnXoa.setVisible(DangNhap.laAdmin());                  // chỉ Admin được xóa

        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setOpaque(false);
        thanhTren.add(txtTimKiem, BorderLayout.WEST);
        thanhTren.add(GiaoDien.taoHangNut(FlowLayout.RIGHT, btnThem, btnSua, btnXoa), BorderLayout.EAST);

        add(thanhTren, BorderLayout.NORTH);
        add(GiaoDien.taoThe(GiaoDien.taoThanhCuon(bang), 6), BorderLayout.CENTER);
    }

    // ============================================================ CÁC PHẦN LỚP CON PHẢI CUNG CẤP
    /** Đọc toàn bộ dữ liệu từ CSDL. */
    protected abstract List<T> taiDanhSach();

    /** Chuyển một đối tượng thành một dòng của bảng. */
    protected abstract Object[] taoDong(T doiTuong);

    /** Đối tượng có khớp từ khóa tìm kiếm không (từ khóa đã chuyển sang chữ thường). */
    protected abstract boolean khopTuKhoa(T doiTuong, String tuKhoa);

    /** Mã định danh (khóa chính) của đối tượng. */
    protected abstract String layMa(T doiTuong);

    /** Hiện form nhập. doiTuongCu == null là thêm mới. Trả về null nếu người dùng bấm Hủy. */
    protected abstract T hienForm(T doiTuongCu, List<T> danhSachHienCo);

    protected abstract boolean thucHienThem(T doiTuong);
    protected abstract boolean thucHienCapNhat(T doiTuong);
    protected abstract boolean thucHienXoa(T doiTuong);

    // ============================================================ DỮ LIỆU
    /** Đọc lại dữ liệu từ CSDL và hiển thị. */
    public void taiLai() {
        tatCa = taiDanhSach();
        locDuLieu();
    }

    /** Lọc dữ liệu theo từ khóa trong ô tìm kiếm rồi đổ vào bảng. */
    private void locDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim().toLowerCase();
        moHinhBang.setRowCount(0);
        dangHienThi.clear();
        for (T doiTuongHienTai : tatCa) {
            if (!tuKhoa.isEmpty() && !khopTuKhoa(doiTuongHienTai, tuKhoa)) continue;
            dangHienThi.add(doiTuongHienTai);
            moHinhBang.addRow(taoDong(doiTuongHienTai));
        }
    }

    /** Lấy đối tượng ở dòng đang chọn; chưa chọn dòng nào thì hiện lời nhắc và trả về null. */
    private T layDongDangChon(String loiNhac) {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= dangHienThi.size()) {
            JOptionPane.showMessageDialog(this, loiNhac);
            return null;
        }
        return dangHienThi.get(dong);
    }

    // ============================================================ THAO TÁC
    private void themDong() {
        T moi = hienForm(null, tatCa);
        if (moi == null) return;
        if (thucHienThem(moi)) taiLai();
        else GiaoDien.baoLoi(this, "Không thêm được " + doiTuong + ". Mã có thể đã tồn tại hoặc CSDL gặp lỗi.", null);
    }

    private void suaDong() {
        T cu = layDongDangChon("Hãy chọn " + doiTuong + " cần sửa.");
        if (cu == null) return;
        T daSua = hienForm(cu, tatCa);
        if (daSua == null) return;
        if (thucHienCapNhat(daSua)) taiLai();
        else GiaoDien.baoLoi(this, "Không cập nhật được " + doiTuong + ".", null);
    }

    private void xoaDong() {
        T canXoa = layDongDangChon("Hãy chọn " + doiTuong + " cần xóa.");
        if (canXoa == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa " + doiTuong + " " + layMa(canXoa) + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (thucHienXoa(canXoa)) taiLai();
        else JOptionPane.showMessageDialog(this, "Không thể xóa vì " + doiTuong + " này đang được dùng trong đơn hàng.\n"
                + "Hãy xóa hoặc sửa các đơn hàng liên quan trước.", "Không xóa được", JOptionPane.WARNING_MESSAGE);
    }

    // ============================================================ TIỆN ÍCH CHO LỚP CON
    /** Gợi ý mã tiếp theo theo mẫu tiền tố + số, ví dụ có KH05 thì trả về KH06. */
    protected String goiYMaTiepTheo(String tienTo, int soChuSo, List<T> danhSach) {
        List<String> cacMa = new ArrayList<>();
        for (T doiTuongHienTai : danhSach) cacMa.add(layMa(doiTuongHienTai));
        return DinhDang.goiYMa(tienTo, soChuSo, cacMa);
    }

    /** Kiểm tra mã đã có trong danh sách chưa (không phân biệt hoa thường). */
    protected boolean maDaTonTai(List<T> danhSach, String ma) {
        for (T doiTuongHienTai : danhSach) {
            if (layMa(doiTuongHienTai).equalsIgnoreCase(ma)) return true;
        }
        return false;
    }
}
