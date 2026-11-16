package view;

import dao.KetNoiCsdl;
import dao.TaiKhoanDao;
import model.DangNhap;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Cửa sổ chính: thanh điều hướng bên trái và các trang (Đơn hàng, Khách hàng, Sản phẩm, Tài khoản).
 * Muốn thêm một trang mới chỉ cần gọi themTrang() trong hàm khởi tạo.
 * Chạy chương trình từ hàm main của lớp này.
 */
public class ManHinhChinh extends JFrame {

    /** Thông tin của một trang trong thanh điều hướng. */
    private static class ThongTinTrang {
        final String tieuDe;                  // tiêu đề lớn ở đầu trang
        final String moTa;                    // dòng mô tả nhỏ bên dưới tiêu đề
        final GiaoDien.NutDieuHuong nut;      // nút tương ứng ở thanh bên
        final Runnable taiLai;                // việc cần làm để nạp lại dữ liệu của trang

        ThongTinTrang(String tieuDe, String moTa, GiaoDien.NutDieuHuong nut, Runnable taiLai) {
            this.tieuDe = tieuDe;
            this.moTa = moTa;
            this.nut = nut;
            this.taiLai = taiLai;
        }
    }

    private static final String TRANG_DON_HANG = "donHang";
    private static final String TRANG_KHACH_HANG = "khachHang";
    private static final String TRANG_SAN_PHAM = "sanPham";
    private static final String TRANG_TAI_KHOAN = "taiKhoan";

    private final CardLayout boCucTrang = new CardLayout();
    private final JPanel khungNoiDung = new JPanel(boCucTrang);
    private final Map<String, ThongTinTrang> cacTrang = new LinkedHashMap<>();   // giữ đúng thứ tự thêm vào
    private final JPanel thanhBen = new JPanel();
    private final JLabel lblTieuDeTrang = new JLabel();
    private final JLabel lblMoTaTrang = new JLabel();
    private String trangHienTai = TRANG_DON_HANG;

    public ManHinhChinh() {
        setTitle("Quản lý đơn hàng sản xuất");
        setSize(1366, 840);
        setMinimumSize(new Dimension(1200, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(GiaoDien.MAU_NEN);
        setLayout(new BorderLayout());
        khungNoiDung.setOpaque(false);

        dungThanhBenDau();

        // Khai báo các trang. Trang Tài khoản chỉ có với Admin.
        QuanLyDonHang donHang = new QuanLyDonHang();
        QuanLyKhachHang khachHang = new QuanLyKhachHang();
        QuanLySanPham sanPham = new QuanLySanPham();
        themTrang(TRANG_DON_HANG, "Đơn hàng", GiaoDien.ICON_DON_HANG, "Đơn hàng sản xuất",
                "Theo dõi đơn hàng, sản phẩm và tiến độ từng công đoạn", donHang, donHang::taiLai);
        themTrang(TRANG_KHACH_HANG, "Khách hàng", GiaoDien.ICON_KHACH_HANG, "Khách hàng",
                "Danh sách khách hàng đặt hàng sản xuất", khachHang, khachHang::taiLai);
        themTrang(TRANG_SAN_PHAM, "Sản phẩm", GiaoDien.ICON_SAN_PHAM, "Sản phẩm",
                "Danh mục sản phẩm và đơn giá chuẩn", sanPham, sanPham::taiLai);
        if (DangNhap.laAdmin()) {
            QuanLyTaiKhoan taiKhoan = new QuanLyTaiKhoan();
            themTrang(TRANG_TAI_KHOAN, "Tài khoản", GiaoDien.ICON_TAI_KHOAN, "Tài khoản",
                    "Quản lý người dùng và phân quyền", taiKhoan, taiKhoan::taiLai);
        }

        thanhBen.add(Box.createVerticalGlue());
        thanhBen.add(dungKhungNguoiDung());
        add(thanhBen, BorderLayout.WEST);

        JPanel phanChinh = new JPanel(new BorderLayout());
        phanChinh.setOpaque(false);
        phanChinh.add(dungTieuDeTrang(), BorderLayout.NORTH);
        phanChinh.add(khungNoiDung, BorderLayout.CENTER);
        add(phanChinh, BorderLayout.CENTER);

        hienTrang(TRANG_DON_HANG);
    }

    // ============================================================ THANH BÊN
    /** Phần đầu thanh bên: logo và tên chương trình. Các nút điều hướng được thêm sau bằng themTrang(). */
    private void dungThanhBenDau() {
        thanhBen.setBackground(GiaoDien.MAU_THANH_BEN);
        thanhBen.setLayout(new BoxLayout(thanhBen, BoxLayout.Y_AXIS));
        thanhBen.setBorder(new EmptyBorder(22, 16, 18, 16));
        thanhBen.setPreferredSize(new Dimension(228, 0));

        JLabel tenChuongTrinh = new JLabel("Quản lý sản xuất");
        tenChuongTrinh.setFont(GiaoDien.phongChu(Font.BOLD, 15));
        tenChuongTrinh.setForeground(Color.WHITE);
        JLabel moTa = new JLabel("Đơn hàng và tiến độ");
        moTa.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
        moTa.setForeground(GiaoDien.MAU_CHU_THANH_BEN);

        JPanel thuongHieu = new JPanel(new BorderLayout(10, 0));
        thuongHieu.setOpaque(false);
        thuongHieu.add(new GiaoDien.Logo("QS", 40, GiaoDien.MAU_CHINH), BorderLayout.WEST);
        thuongHieu.add(GiaoDien.xepDoc(tenChuongTrinh, moTa), BorderLayout.CENTER);
        thuongHieu.setAlignmentX(Component.LEFT_ALIGNMENT);
        thuongHieu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        thanhBen.add(thuongHieu);
        thanhBen.add(Box.createVerticalStrut(28));
    }

    /**
     * Đăng ký một trang: thêm nút vào thanh bên và thêm nội dung vào khung chính.
     * @param khoa tên định danh của trang
     * @param tenNut chữ trên nút điều hướng
     * @param icon biểu tượng trên nút (xem GiaoDien.icon...)
     * @param taiLai việc nạp lại dữ liệu, được gọi mỗi khi mở trang hoặc bấm "Làm mới"
     */
    private void themTrang(String khoa, String tenNut, String icon, String tieuDe, String moTa, JPanel trang, Runnable taiLai) {
        GiaoDien.NutDieuHuong nut = new GiaoDien.NutDieuHuong(tenNut, icon);
        nut.addActionListener(e -> hienTrang(khoa));
        thanhBen.add(nut);
        thanhBen.add(Box.createVerticalStrut(4));
        khungNoiDung.add(trang, khoa);
        cacTrang.put(khoa, new ThongTinTrang(tieuDe, moTa, nut, taiLai));
    }

    /** Khung người dùng ở cuối thanh bên: ảnh đại diện, tên, vai trò và hai nút Đổi mật khẩu, Đăng xuất. */
    private JPanel dungKhungNguoiDung() {
        TaiKhoan taiKhoan = DangNhap.layTaiKhoanHienTai();
        GiaoDien.AnhDaiDien anhDaiDien = new GiaoDien.AnhDaiDien(36);
        anhDaiDien.datChuCaiDau(taiKhoan.getHoTen());

        JLabel lblHoTen = new JLabel(taiKhoan.getHoTen());
        lblHoTen.setFont(GiaoDien.phongChu(Font.BOLD, 13));
        lblHoTen.setForeground(Color.WHITE);
        JLabel lblVaiTro = new JLabel(taiKhoan.getVaiTro());
        lblVaiTro.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
        lblVaiTro.setForeground(GiaoDien.MAU_CHU_THANH_BEN);

        JPanel thongTin = new JPanel(new BorderLayout(10, 0));
        thongTin.setOpaque(false);
        thongTin.add(anhDaiDien, BorderLayout.WEST);
        thongTin.add(GiaoDien.xepDoc(lblHoTen, lblVaiTro), BorderLayout.CENTER);

        Color mauNutToi = new Color(30, 41, 59);
        GiaoDien.NutBoGoc btnDoiMatKhau = new GiaoDien.NutBoGoc("Đổi mật khẩu", mauNutToi, GiaoDien.MAU_CHU_THANH_BEN);
        btnDoiMatKhau.addActionListener(e -> doiMatKhau());
        GiaoDien.NutBoGoc btnDangXuat = new GiaoDien.NutBoGoc("Đăng xuất", mauNutToi, GiaoDien.MAU_CHU_THANH_BEN);
        btnDangXuat.addActionListener(e -> dangXuat());
        JPanel cacNut = new JPanel(new GridLayout(1, 2, 6, 0));
        cacNut.setOpaque(false);
        for (GiaoDien.NutBoGoc nut : new GiaoDien.NutBoGoc[]{btnDoiMatKhau, btnDangXuat}) {
            nut.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
            nut.setBorder(new EmptyBorder(8, 6, 8, 6));
            cacNut.add(nut);
        }

        JPanel khung = new JPanel(new BorderLayout(0, 12));
        khung.setOpaque(false);
        khung.setAlignmentX(Component.LEFT_ALIGNMENT);
        khung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        khung.add(thongTin, BorderLayout.NORTH);
        khung.add(cacNut, BorderLayout.SOUTH);
        return khung;
    }

    // ============================================================ TIÊU ĐỀ TRANG
    /** Phần đầu mỗi trang: tiêu đề, mô tả, ngày hôm nay và nút Làm mới. */
    private JPanel dungTieuDeTrang() {
        lblTieuDeTrang.setFont(GiaoDien.phongChu(Font.BOLD, 24));
        lblTieuDeTrang.setForeground(GiaoDien.MAU_CHU);
        lblMoTaTrang.setFont(GiaoDien.phongChu(Font.PLAIN, 13));
        lblMoTaTrang.setForeground(GiaoDien.MAU_CHU_NHAT);

        String ngayHomNay = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(new Date());
        JLabel lblNgay = GiaoDien.taoNhan(Character.toUpperCase(ngayHomNay.charAt(0)) + ngayHomNay.substring(1));
        GiaoDien.NutBoGoc btnLamMoi = GiaoDien.nutPhu("Làm mới");
        btnLamMoi.addActionListener(e -> cacTrang.get(trangHienTai).taiLai.run());
        JPanel phanPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        phanPhai.setOpaque(false);
        phanPhai.add(lblNgay);
        phanPhai.add(btnLamMoi);

        JPanel khung = new JPanel(new BorderLayout());
        khung.setOpaque(false);
        khung.setBorder(new EmptyBorder(18, 24, 12, 24));
        khung.add(GiaoDien.xepDoc(lblTieuDeTrang, Box.createVerticalStrut(2), lblMoTaTrang), BorderLayout.WEST);
        khung.add(phanPhai, BorderLayout.EAST);
        return khung;
    }

    /** Chuyển sang một trang: tô sáng nút, đổi tiêu đề, hiện nội dung và nạp lại dữ liệu. */
    private void hienTrang(String khoa) {
        trangHienTai = khoa;
        for (Map.Entry<String, ThongTinTrang> muc : cacTrang.entrySet()) {
            muc.getValue().nut.datDangChon(muc.getKey().equals(khoa));
        }
        ThongTinTrang trang = cacTrang.get(khoa);
        lblTieuDeTrang.setText(trang.tieuDe);
        lblMoTaTrang.setText(trang.moTa);
        boCucTrang.show(khungNoiDung, khoa);
        trang.taiLai.run();
    }

    // ============================================================ TÀI KHOẢN CỦA TÔI
    /** Đổi mật khẩu của chính người đang đăng nhập (phải nhập đúng mật khẩu hiện tại). */
    private void doiMatKhau() {
        JPasswordField txtMatKhauCu = new JPasswordField(22);
        JPasswordField txtMatKhauMoi = new JPasswordField(22);
        JPasswordField txtNhapLai = new JPasswordField(22);
        GiaoDien.dinhDangOnhap(txtMatKhauCu);
        GiaoDien.dinhDangOnhap(txtMatKhauMoi);
        GiaoDien.dinhDangOnhap(txtNhapLai);
        JPanel form = GiaoDien.taoFormNhap(new String[]{"Mật khẩu hiện tại", "Mật khẩu mới (từ 6 ký tự)", "Nhập lại mật khẩu mới"},
                new JComponent[]{txtMatKhauCu, txtMatKhauMoi, txtNhapLai});
        TaiKhoanDao taiKhoanDao = new TaiKhoanDao();
        String tenDangNhap = DangNhap.layTaiKhoanHienTai().getTenDangNhap();

        boolean dongY = GiaoDien.hienFormNhap(this, "Đổi mật khẩu", form, () -> {
            if (taiKhoanDao.dangNhap(tenDangNhap, new String(txtMatKhauCu.getPassword())) == null) return "Mật khẩu hiện tại chưa đúng.";
            return QuanLyTaiKhoan.kiemTraMatKhauMoi(new String(txtMatKhauMoi.getPassword()), new String(txtNhapLai.getPassword()));
        });
        if (!dongY) return;
        String matKhauMoi = new String(txtMatKhauMoi.getPassword());
        if (taiKhoanDao.doiMatKhau(tenDangNhap, matKhauMoi)) JOptionPane.showMessageDialog(this, "Đã đổi mật khẩu.");
        else GiaoDien.baoLoi(this, "Không đổi được mật khẩu.", taiKhoanDao.layLoiCuoi());
    }

    /** Đăng xuất: đóng cửa sổ chính và quay lại màn hình đăng nhập. */
    private void dangXuat() {
        if (JOptionPane.showConfirmDialog(this, "Đăng xuất khỏi tài khoản " + DangNhap.layTaiKhoanHienTai().getTenDangNhap() + "?",
                "Đăng xuất", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        DangNhap.xoaPhien();
        dispose();
        khoiDong();
    }

    // ============================================================ KHỞI ĐỘNG
    /** Hiện màn hình đăng nhập; đăng nhập đúng thì mở cửa sổ chính, đóng cửa sổ đăng nhập thì thoát chương trình. */
    public static void khoiDong() {
        TaiKhoan taiKhoan = new FormDangNhap().hienThi();
        if (taiKhoan == null) {
            System.exit(0);
            return;
        }
        DangNhap.datTaiKhoanHienTai(taiKhoan);
        new ManHinhChinh().setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception boQua) {
            // Không đặt được giao diện hệ thống thì dùng giao diện mặc định của Java
        }
        UIManager.put("OptionPane.messageFont", GiaoDien.phongChu(Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", GiaoDien.phongChu(Font.PLAIN, 13));

        // Kiểm tra kết nối và các bảng. CSDL được tạo bằng file QuanLySanXuat.sql (chạy một lần trong SSMS).
        String loiKetNoi = KetNoiCsdl.kiemTraKetNoi();

        SwingUtilities.invokeLater(() -> {
            if (loiKetNoi != null) {
                JOptionPane.showMessageDialog(null, loiKetNoi, "Không kết nối được CSDL", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
                return;
            }
            khoiDong();
        });
    }
}
