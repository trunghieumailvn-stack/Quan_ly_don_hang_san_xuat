package view;

import dao.TaiKhoanDao;
import model.DangNhap;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** Trang quản lý tài khoản. Chỉ Admin nhìn thấy trang này. */
public class QuanLyTaiKhoan extends JPanel {

    private static final String CHU_HOAT_DONG = "Hoạt động";
    private static final String CHU_DA_KHOA = "Đã khóa";
    private static final int DO_DAI_MAT_KHAU_TOI_THIEU = 6;

    private final TaiKhoanDao taiKhoanDao = new TaiKhoanDao();
    private final DefaultTableModel moHinhBang = GiaoDien.taoMoHinhBang("Tên đăng nhập", "Họ tên", "Vai trò", "Trạng thái");
    private final JTable bang = new JTable(moHinhBang);
    private List<TaiKhoan> danhSach = new ArrayList<>();

    public QuanLyTaiKhoan() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 24, 18, 24));

        GiaoDien.dinhDangBang(bang, new int[]{200, 300, 140, 140});
        bang.getColumnModel().getColumn(3).setCellRenderer(new GiaoDien.OBangTrangThai()
                .themKieu(CHU_HOAT_DONG, GiaoDien.MAU_CHU_XANH, GiaoDien.MAU_NEN_XANH)
                .themKieu(CHU_DA_KHOA, GiaoDien.MAU_CHU_DO, GiaoDien.MAU_NEN_DO));

        GiaoDien.NutBoGoc btnThem = new GiaoDien.NutBoGoc("+  Thêm tài khoản", GiaoDien.MAU_CHINH, Color.WHITE);
        btnThem.addActionListener(e -> themTaiKhoan());
        GiaoDien.NutBoGoc btnDatLaiMatKhau = GiaoDien.nutPhu("Đặt lại mật khẩu");
        btnDatLaiMatKhau.addActionListener(e -> datLaiMatKhau());
        GiaoDien.NutBoGoc btnKhoa = GiaoDien.nutPhu("Khóa / mở khóa");
        btnKhoa.addActionListener(e -> khoaMoKhoa());
        GiaoDien.NutBoGoc btnXoa = new GiaoDien.NutBoGoc("Xóa", GiaoDien.MAU_DO, Color.WHITE);
        btnXoa.addActionListener(e -> xoaTaiKhoan());

        JLabel nhanGoiY = GiaoDien.taoNhan("Mật khẩu được mã hóa SHA-256 trước khi lưu");
        nhanGoiY.setFont(GiaoDien.phongChu(Font.ITALIC, 12));
        JPanel thanhTren = new JPanel(new BorderLayout());
        thanhTren.setOpaque(false);
        thanhTren.add(GiaoDien.taoHangNut(FlowLayout.LEFT, btnThem, btnDatLaiMatKhau, btnKhoa, btnXoa), BorderLayout.WEST);
        thanhTren.add(nhanGoiY, BorderLayout.EAST);

        add(thanhTren, BorderLayout.NORTH);
        add(GiaoDien.taoThe(GiaoDien.taoThanhCuon(bang), 6), BorderLayout.CENTER);
    }

    /** Đọc lại danh sách tài khoản từ CSDL và hiển thị. */
    public void taiLai() {
        danhSach = taiKhoanDao.layTatCa();
        moHinhBang.setRowCount(0);
        for (TaiKhoan tk : danhSach) {
            moHinhBang.addRow(new Object[]{tk.getTenDangNhap(), tk.getHoTen(), tk.getVaiTro(),
                    tk.isHoatDong() ? CHU_HOAT_DONG : CHU_DA_KHOA});
        }
    }

    /**
     * Kiểm tra mật khẩu mới theo quy tắc chung (dùng cả khi thêm, đặt lại và đổi mật khẩu).
     * @return thông báo lỗi, hoặc null nếu mật khẩu hợp lệ
     */
    public static String kiemTraMatKhauMoi(String matKhau, String nhapLai) {
        if (matKhau.length() < DO_DAI_MAT_KHAU_TOI_THIEU) return "Mật khẩu cần từ " + DO_DAI_MAT_KHAU_TOI_THIEU + " ký tự.";
        if (!matKhau.equals(nhapLai)) return "Hai lần nhập mật khẩu chưa giống nhau.";
        return null;
    }

    /** Lấy tài khoản ở dòng đang chọn; chưa chọn thì nhắc và trả về null. */
    private TaiKhoan layTaiKhoanDangChon() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= danhSach.size()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một tài khoản trong bảng.");
            return null;
        }
        return danhSach.get(dong);
    }

    /** Có phải tài khoản đang đăng nhập không (không cho tự khóa hoặc tự xóa chính mình). */
    private boolean laChinhMinh(TaiKhoan taiKhoan) {
        TaiKhoan hienTai = DangNhap.layTaiKhoanHienTai();
        return hienTai != null && hienTai.getTenDangNhap().equalsIgnoreCase(taiKhoan.getTenDangNhap());
    }

    private static String doc(JPasswordField o) { return new String(o.getPassword()); }

    // ============================================================ THAO TÁC
    private void themTaiKhoan() {
        JTextField txtTenDangNhap = new JTextField(22);
        JTextField txtHoTen = new JTextField(22);
        JComboBox<String> cbVaiTro = new JComboBox<>(new String[]{TaiKhoan.VAI_TRO_NHAN_VIEN, TaiKhoan.VAI_TRO_ADMIN});
        JPasswordField txtMatKhau = new JPasswordField(22);
        JPasswordField txtNhapLai = new JPasswordField(22);
        for (JTextField o : new JTextField[]{txtTenDangNhap, txtHoTen, txtMatKhau, txtNhapLai}) GiaoDien.dinhDangOnhap(o);
        GiaoDien.dinhDangComboBox(cbVaiTro);
        JPanel form = GiaoDien.taoFormNhap(
                new String[]{"Tên đăng nhập *", "Họ tên *", "Vai trò", "Mật khẩu * (từ 6 ký tự)", "Nhập lại mật khẩu *"},
                new JComponent[]{txtTenDangNhap, txtHoTen, cbVaiTro, txtMatKhau, txtNhapLai});

        boolean dongY = GiaoDien.hienFormNhap(this, "Thêm tài khoản", form, () -> {
            String tenDangNhap = txtTenDangNhap.getText().trim();
            if (tenDangNhap.length() < 3 || tenDangNhap.contains(" ")) return "Tên đăng nhập cần từ 3 ký tự và không có khoảng trắng.";
            if (txtHoTen.getText().trim().isEmpty()) return "Hãy nhập họ tên.";
            return kiemTraMatKhauMoi(doc(txtMatKhau), doc(txtNhapLai));
        });
        if (!dongY) return;
        if (taiKhoanDao.them(txtTenDangNhap.getText().trim(), doc(txtMatKhau), txtHoTen.getText().trim(),
                (String) cbVaiTro.getSelectedItem())) taiLai();
        else GiaoDien.baoLoi(this, "Không thêm được tài khoản. Tên đăng nhập có thể đã tồn tại.", taiKhoanDao.layLoiCuoi());
    }

    private void datLaiMatKhau() {
        TaiKhoan taiKhoan = layTaiKhoanDangChon();
        if (taiKhoan == null) return;
        JPasswordField txtMatKhau = new JPasswordField(22);
        JPasswordField txtNhapLai = new JPasswordField(22);
        GiaoDien.dinhDangOnhap(txtMatKhau);
        GiaoDien.dinhDangOnhap(txtNhapLai);
        JPanel form = GiaoDien.taoFormNhap(new String[]{"Mật khẩu mới * (từ 6 ký tự)", "Nhập lại mật khẩu *"},
                new JComponent[]{txtMatKhau, txtNhapLai});

        if (!GiaoDien.hienFormNhap(this, "Đặt lại mật khẩu cho " + taiKhoan.getTenDangNhap(), form,
                () -> kiemTraMatKhauMoi(doc(txtMatKhau), doc(txtNhapLai)))) return;
        if (taiKhoanDao.doiMatKhau(taiKhoan.getTenDangNhap(), doc(txtMatKhau))) JOptionPane.showMessageDialog(this, "Đã đặt lại mật khẩu.");
        else GiaoDien.baoLoi(this, "Không đổi được mật khẩu.", taiKhoanDao.layLoiCuoi());
    }

    private void khoaMoKhoa() {
        TaiKhoan taiKhoan = layTaiKhoanDangChon();
        if (taiKhoan == null) return;
        if (laChinhMinh(taiKhoan)) { GiaoDien.canhBao(this, "Bạn không thể tự khóa tài khoản đang đăng nhập."); return; }
        if (taiKhoanDao.datHoatDong(taiKhoan.getTenDangNhap(), !taiKhoan.isHoatDong())) taiLai();
        else GiaoDien.baoLoi(this, "Không cập nhật được trạng thái tài khoản.", taiKhoanDao.layLoiCuoi());
    }

    private void xoaTaiKhoan() {
        TaiKhoan taiKhoan = layTaiKhoanDangChon();
        if (taiKhoan == null) return;
        if (laChinhMinh(taiKhoan)) { GiaoDien.canhBao(this, "Bạn không thể tự xóa tài khoản đang đăng nhập."); return; }
        if (JOptionPane.showConfirmDialog(this, "Xóa tài khoản " + taiKhoan.getTenDangNhap() + "?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (taiKhoanDao.xoa(taiKhoan.getTenDangNhap())) taiLai();
        else GiaoDien.baoLoi(this, "Không xóa được tài khoản.", taiKhoanDao.layLoiCuoi());
    }
}
