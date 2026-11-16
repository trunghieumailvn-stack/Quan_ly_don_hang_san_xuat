package view;

import dao.TaiKhoanDao;
import model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Màn hình đăng nhập. hienThi() trả về tài khoản đăng nhập thành công, hoặc null nếu người dùng đóng cửa sổ. */
public class FormDangNhap extends JDialog {

    private final TaiKhoanDao taiKhoanDao = new TaiKhoanDao();
    private final JTextField txtTenDangNhap = new JTextField(18);
    private final JPasswordField txtMatKhau = new JPasswordField(18);
    private final JLabel lblLoi = new JLabel(" ");
    private TaiKhoan ketQua;                  // tài khoản đăng nhập thành công, null nếu chưa

    public FormDangNhap() {
        super((Frame) null, "Đăng nhập", true);
        setSize(800, 480);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2));      // nửa trái giới thiệu, nửa phải là form đăng nhập
        add(dungKhungGioiThieu());
        add(dungKhungForm());
    }

    /**
     * Hình minh họa dây chuyền sản xuất: bốn công đoạn nối nhau bằng một đường dọc.
     * Hai bước đầu đã xong, bước thứ ba đang thực hiện, bước cuối chưa bắt đầu (giống các trạng thái trong chương trình).
     */
    private static class DayChuyenSanXuat extends JComponent {
        private static final String[] CAC_BUOC = {"Chuẩn bị nguyên liệu", "Sản xuất", "Kiểm tra chất lượng", "Đóng gói"};
        private static final int SO_BUOC_XONG = 2;
        private static final int CAO_MOI_BUOC = 34;
        private static final int BAN_KINH = 9;

        DayChuyenSanXuat() {
            Dimension kichThuoc = new Dimension(260, CAC_BUOC.length * CAO_MOI_BUOC);
            setPreferredSize(kichThuoc);
            setMinimumSize(kichThuoc);
            setMaximumSize(kichThuoc);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int tamX = BAN_KINH + 1;
            for (int i = 0; i < CAC_BUOC.length; i++) {
                int tamY = i * CAO_MOI_BUOC + CAO_MOI_BUOC / 2;
                boolean daXong = i < SO_BUOC_XONG, dangLam = i == SO_BUOC_XONG;

                if (i < CAC_BUOC.length - 1) {                                // đường nối xuống bước kế tiếp
                    g2.setColor(new Color(255, 255, 255, daXong ? 190 : 70));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(tamX, tamY + BAN_KINH + 3, tamX, tamY + CAO_MOI_BUOC - BAN_KINH - 3);
                }
                if (daXong) {                                                // đã xong: hình tròn trắng có dấu tick
                    g2.setColor(Color.WHITE);
                    g2.fillOval(tamX - BAN_KINH, tamY - BAN_KINH, BAN_KINH * 2, BAN_KINH * 2);
                    g2.setColor(GiaoDien.MAU_CHINH_DAM);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawPolyline(new int[]{tamX - 4, tamX - 1, tamX + 4}, new int[]{tamY, tamY + 3, tamY - 3}, 3);
                } else {                                                     // đang làm: vòng trắng đậm; chưa bắt đầu: vòng mờ
                    g2.setColor(new Color(255, 255, 255, dangLam ? 255 : 90));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(tamX - BAN_KINH, tamY - BAN_KINH, BAN_KINH * 2, BAN_KINH * 2);
                    if (dangLam) g2.fillOval(tamX - 3, tamY - 3, 6, 6);
                }

                g2.setFont(GiaoDien.phongChu(dangLam ? Font.BOLD : Font.PLAIN, 13));
                g2.setColor(daXong ? new Color(191, 219, 254) : new Color(255, 255, 255, dangLam ? 255 : 130));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(CAC_BUOC[i], tamX + BAN_KINH + 14, tamY + (fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    /** Nửa bên trái: nền gradient, logo, lời giới thiệu và hình dây chuyền sản xuất. */
    private JPanel dungKhungGioiThieu() {
        JPanel khung = new GiaoDien.KhungGradient(new GridBagLayout());
        khung.setBorder(new EmptyBorder(30, 36, 30, 36));

        JLabel tieuDe = new JLabel("<html>Quản lý đơn hàng<br>sản xuất</html>");
        tieuDe.setFont(GiaoDien.phongChu(Font.BOLD, 26));
        tieuDe.setForeground(Color.WHITE);
        JLabel moTa = new JLabel("<html>Theo dõi đơn hàng, tiến độ từng công đoạn<br>và khách hàng trong một nơi.</html>");
        moTa.setFont(GiaoDien.phongChu(Font.PLAIN, 13));
        moTa.setForeground(new Color(191, 219, 254));

        khung.add(GiaoDien.xepDoc(
                new GiaoDien.Logo("QS", 56, new Color(255, 255, 255, 36)),
                Box.createVerticalStrut(22), tieuDe,
                Box.createVerticalStrut(10), moTa,
                Box.createVerticalStrut(30), new DayChuyenSanXuat()));
        return khung;
    }

    /** Nửa bên phải: form nhập tên đăng nhập, mật khẩu và nút Đăng nhập. */
    private JPanel dungKhungForm() {
        JPanel khung = new JPanel(new GridBagLayout());
        khung.setBackground(Color.WHITE);
        khung.setBorder(new EmptyBorder(30, 40, 30, 40));

        JLabel tieuDe = new JLabel("Đăng nhập");
        tieuDe.setFont(GiaoDien.phongChu(Font.BOLD, 24));
        tieuDe.setForeground(GiaoDien.MAU_CHU);

        GiaoDien.dinhDangOnhap(txtTenDangNhap);
        GiaoDien.dinhDangOnhap(txtMatKhau);
        txtMatKhau.setEchoChar('\u2022');

        // Ô tích để xem mật khẩu đang gõ
        JCheckBox chkHienMatKhau = new JCheckBox("Hiện mật khẩu");
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
        chkHienMatKhau.setForeground(GiaoDien.MAU_CHU_NHAT);
        chkHienMatKhau.addActionListener(e -> txtMatKhau.setEchoChar(chkHienMatKhau.isSelected() ? (char) 0 : '\u2022'));

        lblLoi.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
        lblLoi.setForeground(GiaoDien.MAU_DO);

        GiaoDien.NutBoGoc btnDangNhap = new GiaoDien.NutBoGoc("Đăng nhập", GiaoDien.MAU_CHINH, Color.WHITE);
        btnDangNhap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnDangNhap.addActionListener(e -> dangNhap());
        getRootPane().setDefaultButton(btnDangNhap);      // bấm Enter cũng đăng nhập

        khung.add(GiaoDien.xepDoc(
                tieuDe, Box.createVerticalStrut(4), GiaoDien.taoNhan("Nhập tài khoản để vào hệ thống"),
                Box.createVerticalStrut(22), taoNhanOnhap("Tên đăng nhập"), Box.createVerticalStrut(6), txtTenDangNhap,
                Box.createVerticalStrut(14), taoNhanOnhap("Mật khẩu"), Box.createVerticalStrut(6), txtMatKhau,
                Box.createVerticalStrut(6), chkHienMatKhau,
                Box.createVerticalStrut(4), lblLoi,
                Box.createVerticalStrut(10), btnDangNhap));
        return khung;
    }

    /** Nhãn nhỏ phía trên mỗi ô nhập. */
    private JLabel taoNhanOnhap(String chu) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(GiaoDien.phongChu(Font.BOLD, 13));
        nhan.setForeground(GiaoDien.MAU_CHU);
        return nhan;
    }

    /** Xử lý khi bấm nút Đăng nhập: đúng thì đóng hộp thoại, sai thì hiện lý do. */
    private void dangNhap() {
        String tenDangNhap = txtTenDangNhap.getText().trim();
        String matKhau = new String(txtMatKhau.getPassword());
        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            lblLoi.setText("Hãy nhập tên đăng nhập và mật khẩu.");
            return;
        }
        TaiKhoan taiKhoan = taiKhoanDao.dangNhap(tenDangNhap, matKhau);
        if (taiKhoan == null) {
            String loi = taiKhoanDao.layLoiCuoi();
            // Chỉ hiện dòng đầu của thông báo lỗi để vừa khung
            lblLoi.setText("<html>" + (loi == null ? "Đăng nhập thất bại." : loi.split("\n")[0]) + "</html>");
            txtMatKhau.setText("");
            txtMatKhau.requestFocusInWindow();
            return;
        }
        ketQua = taiKhoan;
        dispose();
    }

    /** Hiện màn hình đăng nhập và chờ người dùng. Trả về null nếu họ đóng cửa sổ mà chưa đăng nhập. */
    public TaiKhoan hienThi() {
        SwingUtilities.invokeLater(() -> txtTenDangNhap.requestFocusInWindow());
        setVisible(true);
        return ketQua;
    }
}
