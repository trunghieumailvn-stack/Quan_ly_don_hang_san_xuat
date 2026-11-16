package view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Bộ màu, phông chữ, biểu tượng và các component tự vẽ để giao diện đẹp và đồng nhất trên mọi hệ điều hành.
 * Muốn đổi diện mạo cả chương trình, chỉ cần sửa ở file này.
 */
public final class GiaoDien {
    private GiaoDien() {}

    // ============================================================ BẢNG MÀU
    public static final Color MAU_CHINH = new Color(37, 99, 235);
    public static final Color MAU_CHINH_DAM = new Color(30, 58, 138);
    public static final Color MAU_NEN = new Color(244, 247, 251);
    public static final Color MAU_THE = Color.WHITE;
    public static final Color MAU_VIEN = new Color(226, 232, 240);
    public static final Color MAU_CHU = new Color(15, 23, 42);
    public static final Color MAU_CHU_NHAT = new Color(100, 116, 139);
    public static final Color MAU_XANH_LA = new Color(22, 163, 74);
    public static final Color MAU_CAM = new Color(217, 119, 6);
    public static final Color MAU_DO = new Color(220, 38, 38);
    public static final Color MAU_VANG = new Color(234, 179, 8);
    public static final Color MAU_XAM_NHAT = new Color(226, 232, 240);
    public static final Color MAU_NEN_XAM = new Color(241, 245, 249);
    public static final Color MAU_DONG_XEN = new Color(248, 250, 252);
    public static final Color MAU_DONG_CHON = new Color(230, 240, 255);
    public static final Color MAU_DONG_RE = new Color(244, 247, 251);      // dòng bảng khi rê chuột
    public static final Color MAU_THANH_BEN = new Color(15, 23, 42);
    public static final Color MAU_CHU_THANH_BEN = new Color(180, 190, 205);
    public static final Color MAU_THANH_TIEN_DO = new Color(226, 232, 240);

    // Cặp màu (chữ, nền) cho các "viên thuốc" trạng thái trong bảng
    public static final Color MAU_CHU_XANH = new Color(21, 128, 61);
    public static final Color MAU_NEN_XANH = new Color(220, 252, 231);
    public static final Color MAU_CHU_CAM = new Color(180, 83, 9);
    public static final Color MAU_NEN_CAM = new Color(254, 243, 199);
    public static final Color MAU_CHU_DO = new Color(185, 28, 28);
    public static final Color MAU_NEN_DO = new Color(254, 226, 226);

    /** Độ bo góc chung của các thẻ. */
    public static final int DO_BO_GOC_THE = 14;

    private static final Color MAU_VIEN_ONHAP = new Color(203, 213, 225);
    private static final Color MAU_GOI_Y = new Color(148, 163, 184);

    /** Phông chữ chung của chương trình. */
    public static Font phongChu(int kieu, int coChu) {
        return new Font("Segoe UI", kieu, coChu);
    }

    /** Bật khử răng cưa cho hình vẽ và chữ. */
    private static void khuRangCua(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // ============================================================ BIỂU TƯỢNG
    // Biểu tượng nét mảnh tự vẽ (không cần file ảnh), vẽ trong ô 24x24 rồi co giãn theo kích thước yêu cầu.
    public static final String ICON_DON_HANG = "donHang";
    public static final String ICON_KHACH_HANG = "khachHang";
    public static final String ICON_SAN_PHAM = "sanPham";
    public static final String ICON_TAI_KHOAN = "taiKhoan";
    public static final String ICON_DONG_HO = "dongHo";
    public static final String ICON_TICK = "tick";
    public static final String ICON_TIEN = "tien";
    public static final String ICON_TIM_KIEM = "timKiem";

    /** Đường gấp khúc đi qua các điểm (x1, y1, x2, y2...); khep = true thì nối điểm cuối về điểm đầu. */
    private static Path2D.Double duongGap(boolean khep, double... d) {
        Path2D.Double duong = new Path2D.Double();
        duong.moveTo(d[0], d[1]);
        for (int i = 2; i + 1 < d.length; i += 2) duong.lineTo(d[i], d[i + 1]);
        if (khep) duong.closePath();
        return duong;
    }

    /** Vẽ biểu tượng tên "ten" tại (x, y), vuông cạnh kichThuoc pixel. */
    public static void veIcon(Graphics2D g, String ten, int x, int y, int kichThuoc, Color mau) {
        Graphics2D g2 = (Graphics2D) g.create();
        khuRangCua(g2);
        g2.translate(x, y);
        g2.scale(kichThuoc / 24.0, kichThuoc / 24.0);
        g2.setColor(mau);
        g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (ten) {
            case ICON_DON_HANG:                                          // bìa kẹp giấy
                g2.draw(new RoundRectangle2D.Double(5, 4, 14, 17, 4, 4));
                g2.draw(new RoundRectangle2D.Double(9, 2, 6, 4, 2, 2));
                g2.draw(new Line2D.Double(9, 11, 15, 11));
                g2.draw(new Line2D.Double(9, 15, 13, 15));
                break;
            case ICON_KHACH_HANG: {                                      // hình người
                g2.draw(new Ellipse2D.Double(8, 3.5, 8, 8));
                Path2D.Double vai = new Path2D.Double();
                vai.moveTo(5, 20.5);
                vai.curveTo(5, 16, 8, 14.5, 12, 14.5);
                vai.curveTo(16, 14.5, 19, 16, 19, 20.5);
                g2.draw(vai);
                break;
            }
            case ICON_SAN_PHAM:                                          // hộp hàng
                g2.draw(duongGap(true, 12, 3, 20, 7.5, 20, 16.5, 12, 21, 4, 16.5, 4, 7.5));
                g2.draw(duongGap(false, 4, 7.5, 12, 12, 20, 7.5));
                g2.draw(new Line2D.Double(12, 12, 12, 21));
                break;
            case ICON_TAI_KHOAN: {                                       // khiên có dấu tick
                Path2D.Double khien = new Path2D.Double();
                khien.moveTo(12, 3);
                khien.lineTo(19, 6);
                khien.lineTo(19, 11.5);
                khien.curveTo(19, 16, 16, 19.5, 12, 21);
                khien.curveTo(8, 19.5, 5, 16, 5, 11.5);
                khien.lineTo(5, 6);
                khien.closePath();
                g2.draw(khien);
                g2.draw(duongGap(false, 9, 12, 11.2, 14.2, 15, 10));
                break;
            }
            case ICON_DONG_HO:
                g2.draw(new Ellipse2D.Double(4, 4, 16, 16));
                g2.draw(duongGap(false, 12, 8, 12, 12, 15, 14));
                break;
            case ICON_TICK:
                g2.draw(new Ellipse2D.Double(4, 4, 16, 16));
                g2.draw(duongGap(false, 8.5, 12.2, 11, 14.7, 15.5, 9.5));
                break;
            case ICON_TIEN:                                             // tờ tiền
                g2.draw(new RoundRectangle2D.Double(3, 6, 18, 12, 4, 4));
                g2.draw(new Ellipse2D.Double(9.5, 9.5, 5, 5));
                break;
            case ICON_TIM_KIEM:                                          // kính lúp
                g2.draw(new Ellipse2D.Double(4, 4, 11, 11));
                g2.draw(new Line2D.Double(13, 13, 20, 20));
                break;
            default:
                break;
        }
        g2.dispose();
    }

    // ============================================================ HỘP THOẠI THÔNG BÁO
    /** Hiện cảnh báo khi người dùng nhập chưa đúng. */
    public static void canhBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Cần kiểm tra lại", JOptionPane.WARNING_MESSAGE);
    }

    /** Hiện lỗi; loiChiTiet (có thể null) là thông báo lỗi từ Dao, được nối thêm phía dưới. */
    public static void baoLoi(Component cha, String noiDung, String loiChiTiet) {
        String them = (loiChiTiet == null || loiChiTiet.isEmpty()) ? "" : "\n\n" + loiChiTiet;
        JOptionPane.showMessageDialog(cha, noiDung + them, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Hiện form nhập liệu trong hộp thoại OK / Hủy và lặp lại cho đến khi dữ liệu hợp lệ hoặc người dùng bấm Hủy.
     * @param kiemTra trả về thông báo lỗi nếu dữ liệu chưa đúng (form giữ nguyên để sửa), hoặc null nếu hợp lệ
     * @return true nếu người dùng bấm OK và dữ liệu hợp lệ
     */
    public static boolean hienFormNhap(Component cha, String tieuDe, JComponent form, Supplier<String> kiemTra) {
        while (true) {
            if (JOptionPane.showConfirmDialog(cha, form, tieuDe, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return false;
            String loi = kiemTra.get();
            if (loi == null) return true;
            canhBao(cha, loi);
        }
    }

    // ============================================================ BỐ CỤC NHANH
    /** Xếp các component thành một cột, căn trái, nền trong suốt. Dùng Box.createVerticalStrut(n) để chèn khoảng cách. */
    public static JPanel xepDoc(Component... cacThanhPhan) {
        JPanel hop = new JPanel();
        hop.setOpaque(false);
        hop.setLayout(new BoxLayout(hop, BoxLayout.Y_AXIS));
        for (Component thanhPhan : cacThanhPhan) {
            if (thanhPhan instanceof JComponent) ((JComponent) thanhPhan).setAlignmentX(Component.LEFT_ALIGNMENT);
            hop.add(thanhPhan);
        }
        return hop;
    }

    /** Xếp các nút thành một hàng ngang. canLe là FlowLayout.LEFT hoặc FlowLayout.RIGHT. */
    public static JPanel taoHangNut(int canLe, Component... cacNut) {
        JPanel hang = new JPanel(new FlowLayout(canLe, 8, 0));
        hang.setOpaque(false);
        // FlowLayout luôn chừa 8px ở hai đầu; viền âm bù lại để nút thẳng hàng với mép bảng
        hang.setBorder(new EmptyBorder(0, canLe == FlowLayout.LEFT ? -8 : 0, 0, canLe == FlowLayout.RIGHT ? -8 : 0));
        for (Component nut : cacNut) hang.add(nut);
        return hang;
    }

    /** Đặt component vào giữa theo chiều dọc, sát lề trái của vùng chứa. */
    private static JPanel canGiua(JComponent thanhPhan) {
        JPanel khung = new JPanel(new GridBagLayout());
        khung.setOpaque(false);
        GridBagConstraints rb = new GridBagConstraints();
        rb.anchor = GridBagConstraints.WEST;
        rb.weightx = 1;
        khung.add(thanhPhan, rb);
        return khung;
    }

    // ============================================================ NÚT BẤM
    /** Nút bo góc, đổi màu khi rê chuột hoặc nhấn. Nút phụ (xem nutPhu) nền trắng có viền. */
    public static class NutBoGoc extends JButton {
        private final Color mauGoc;
        private boolean laNutPhu;

        public NutBoGoc(String chu, Color mauNenNut, Color mauChuNut) {
            super(chu);
            this.mauGoc = mauNenNut;
            setForeground(mauChuNut);
            setFont(phongChu(Font.BOLD, 13));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(9, 16, 9, 16));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            Color mau = mauGoc;
            if (!isEnabled()) {
                mau = laNutPhu ? MAU_NEN_XAM : new Color(203, 213, 225);            // nút bị vô hiệu: màu xám
            } else if (getModel().isPressed()) {
                mau = laNutPhu ? MAU_XAM_NHAT : mauGoc.darker();                    // đang nhấn: tối hơn
            } else if (getModel().isRollover()) {                                 // rê chuột: sáng hơn một chút
                mau = laNutPhu ? MAU_NEN_XAM : new Color(Math.min(255, mauGoc.getRed() + 14),
                        Math.min(255, mauGoc.getGreen() + 14), Math.min(255, mauGoc.getBlue() + 14), mauGoc.getAlpha());
            }
            g2.setColor(mau);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            if (laNutPhu) {
                g2.setColor(MAU_VIEN_ONHAP);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Nút phụ (nền trắng, có viền), dùng cho các thao tác ít quan trọng như Sửa, Đóng. */
    public static NutBoGoc nutPhu(String chu) {
        NutBoGoc nut = new NutBoGoc(chu, Color.WHITE, MAU_CHU);
        nut.laNutPhu = true;
        return nut;
    }

    /** Nút điều hướng ở thanh bên trái (có biểu tượng); trang đang mở được tô màu chính. */
    public static class NutDieuHuong extends JButton {
        private final String icon;
        private boolean dangChon;

        public NutDieuHuong(String chu, String icon) {
            super(chu);
            this.icon = icon;
            setFont(phongChu(Font.BOLD, 14));
            setForeground(MAU_CHU_THANH_BEN);
            setHorizontalAlignment(LEFT);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(11, 46, 11, 12));                          // chừa chỗ bên trái cho biểu tượng
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        public void datDangChon(boolean chon) {
            dangChon = chon;
            setForeground(chon ? Color.WHITE : MAU_CHU_THANH_BEN);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            if (dangChon) {
                g2.setColor(MAU_CHINH);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            veIcon(g2, icon, 16, (getHeight() - 20) / 2, 20, getForeground());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ============================================================ KHUNG, THẺ
    /** Khung nền trắng, bo góc, viền mảnh (dạng "thẻ"). */
    public static class KhungBoGoc extends JPanel {
        private final int doBoGoc;

        public KhungBoGoc(LayoutManager boCuc, int doBoGoc) {
            super(boCuc);
            this.doBoGoc = doBoGoc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(MAU_THE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, doBoGoc, doBoGoc);
            g2.setColor(MAU_VIEN);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, doBoGoc, doBoGoc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Khung nền gradient xanh, dùng cho màn hình đăng nhập. */
    public static class KhungGradient extends JPanel {
        public KhungGradient(LayoutManager boCuc) { super(boCuc); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0, 0, MAU_CHINH_DAM, getWidth(), getHeight(), MAU_CHINH));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /** Bọc một component vào thẻ trắng bo góc, leDem là khoảng cách giữa viền và nội dung. */
    public static KhungBoGoc taoThe(JComponent noiDung, int leDem) {
        KhungBoGoc the = new KhungBoGoc(new BorderLayout(), DO_BO_GOC_THE);
        the.setBorder(new EmptyBorder(leDem, leDem, leDem, leDem));
        the.add(noiDung, BorderLayout.CENTER);
        return the;
    }

    /** Thẻ thống kê: ô biểu tượng tô nhạt theo màu chủ đề, tiêu đề nhỏ và số liệu lớn. */
    public static JPanel taoTheThongKe(String tieuDe, JLabel giaTri, Color mau, String icon) {
        KhungBoGoc the = new KhungBoGoc(new BorderLayout(12, 0), DO_BO_GOC_THE);
        the.setBorder(new EmptyBorder(12, 14, 12, 8));

        Color mauNenHuyHieu = new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 30);
        JComponent huyHieu = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                khuRangCua(g2);
                g2.setColor(mauNenHuyHieu);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                veIcon(g2, icon, (getWidth() - 22) / 2, (getHeight() - 22) / 2, 22, mau);
                g2.dispose();
            }
        };
        huyHieu.setPreferredSize(new Dimension(40, 40));

        giaTri.setFont(phongChu(Font.BOLD, 17));
        giaTri.setForeground(MAU_CHU);
        the.add(canGiua(huyHieu), BorderLayout.WEST);
        the.add(canGiua(xepDoc(taoNhan(tieuDe), giaTri)), BorderLayout.CENTER);
        return the;
    }

    /** Logo vuông bo góc chứa chữ viết tắt (ví dụ "QS"). */
    public static class Logo extends JComponent {
        private final String chu;
        private final int kichThuoc;
        private final Color mauNen;

        public Logo(String chu, int kichThuoc, Color mauNen) {
            this.chu = chu;
            this.kichThuoc = kichThuoc;
            this.mauNen = mauNen;
            Dimension kt = new Dimension(kichThuoc, kichThuoc);
            setPreferredSize(kt);
            setMinimumSize(kt);
            setMaximumSize(kt);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(mauNen);
            int doBoGoc = kichThuoc * 3 / 10;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), doBoGoc, doBoGoc);
            g2.setColor(Color.WHITE);
            g2.setFont(phongChu(Font.BOLD, kichThuoc * 38 / 100));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(chu, (getWidth() - fm.stringWidth(chu)) / 2, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Hình tròn có chữ cái đầu của tên, dùng làm ảnh đại diện. */
    public static class AnhDaiDien extends JComponent {
        private String chuCaiDau = "?";

        public AnhDaiDien(int kichThuoc) {
            Dimension kt = new Dimension(kichThuoc, kichThuoc);
            setPreferredSize(kt);
            setMinimumSize(kt);
            setMaximumSize(kt);
        }

        public void datChuCaiDau(String ten) {
            chuCaiDau = (ten == null || ten.isEmpty()) ? "?" : ten.substring(0, 1).toUpperCase();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(MAU_CHINH);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.setColor(Color.WHITE);
            g2.setFont(phongChu(Font.BOLD, getWidth() / 2));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(chuCaiDau, (getWidth() - fm.stringWidth(chuCaiDau)) / 2,
                    (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    // ============================================================ Ô NHẬP LIỆU
    /** Viền bo góc của ô nhập; leTrai là khoảng đệm bên trái (lớn hơn khi có biểu tượng). */
    private static Border vienOnhap(Color mauVienO, int leTrai) {
        return new CompoundBorder(new LineBorder(mauVienO, 1, true), new EmptyBorder(6, leTrai, 6, 10));
    }

    public static Border vienOnhapLieu() { return vienOnhap(MAU_VIEN_ONHAP, 10); }

    /** Áp kiểu chung cho ô nhập (JTextField, JPasswordField). Viền đổi sang màu chính khi đang gõ. */
    public static void dinhDangOnhap(JTextComponent o) { dinhDangOnhap(o, 10); }

    private static void dinhDangOnhap(JTextComponent o, int leTrai) {
        Border vienThuong = vienOnhap(MAU_VIEN_ONHAP, leTrai);
        Border vienDangGo = vienOnhap(MAU_CHINH, leTrai);
        o.setFont(phongChu(Font.PLAIN, 13));
        o.setForeground(MAU_CHU);
        o.setCaretColor(MAU_CHINH);
        o.setSelectionColor(new Color(191, 219, 254));
        o.setBorder(vienThuong);
        o.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { o.setBorder(vienDangGo); }
            @Override public void focusLost(FocusEvent e) { o.setBorder(vienThuong); }
        });
        o.setPreferredSize(new Dimension(o.getPreferredSize().width, 34));
    }

    /** Khóa ô nhập (không cho sửa) và đổi sang nền xám, dùng cho các ô mã không được đổi. */
    public static void khoaOnhap(JTextComponent o) {
        o.setEditable(false);
        o.setBackground(MAU_NEN_XAM);
    }

    public static void dinhDangComboBox(JComboBox<?> cb) {
        cb.setFont(phongChu(Font.PLAIN, 13));
        cb.setBackground(Color.WHITE);
        cb.setPreferredSize(new Dimension(Math.max(cb.getPreferredSize().width, 120), 34));
    }

    /** Ô tìm kiếm: có biểu tượng kính lúp và chữ gợi ý mờ hiện ra khi đang trống. */
    public static class OTimKiem extends JTextField {
        private String goiY = "";

        public OTimKiem(int soCot) {
            super(soCot);
            dinhDangOnhap(this, 34);                                           // chừa chỗ bên trái cho kính lúp
        }

        public void datGoiY(String noiDung) {
            goiY = noiDung;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            veIcon(g2, ICON_TIM_KIEM, 11, (getHeight() - 16) / 2, 16, MAU_GOI_Y);
            if (getText().isEmpty() && !goiY.isEmpty()) {
                g2.setColor(MAU_GOI_Y);
                g2.setFont(phongChu(Font.PLAIN, 13));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(goiY, getInsets().left, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    /** Gọi viecCanLam mỗi khi nội dung ô nhập thay đổi (dùng cho tìm kiếm ngay khi gõ). */
    public static void khiGoChu(JTextComponent o, Runnable viecCanLam) {
        o.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { viecCanLam.run(); }
            public void removeUpdate(DocumentEvent e) { viecCanLam.run(); }
            public void changedUpdate(DocumentEvent e) { viecCanLam.run(); }
        });
    }

    /** Gọi viecCanLam khi nhấp đúp vào một dòng của bảng. */
    public static void khiNhapDup(JTable bang, Runnable viecCanLam) {
        bang.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && bang.getSelectedRow() != -1) viecCanLam.run();
            }
        });
    }

    // ============================================================ NHÃN, FORM
    /** Nhãn chữ xám nhạt, dùng làm tiêu đề nhỏ. */
    public static JLabel taoNhan(String chu) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(phongChu(Font.PLAIN, 13));
        nhan.setForeground(MAU_CHU_NHAT);
        return nhan;
    }

    /** Nhãn nhỏ in đậm đặt phía trên một ô nhập. */
    public static JLabel taoNhanNho(String chu) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(phongChu(Font.BOLD, 12));
        nhan.setForeground(MAU_CHU_NHAT);
        return nhan;
    }

    /** Tiêu đề của một khu vực, chữ đậm. */
    public static JLabel taoTieuDeMuc(String chu) {
        JLabel nhan = new JLabel(chu);
        nhan.setFont(phongChu(Font.BOLD, 15));
        nhan.setForeground(MAU_CHU);
        return nhan;
    }

    /** Form hai cột: nhãn bên trái, ô nhập bên phải. cacNhan và cacOnhap phải cùng số phần tử. */
    public static JPanel taoFormNhap(String[] cacNhan, JComponent[] cacOnhap) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(4, 2, 2, 2));
        for (int i = 0; i < cacNhan.length; i++) {
            GridBagConstraints rb = new GridBagConstraints();
            rb.gridy = i;
            rb.gridx = 0;
            rb.anchor = GridBagConstraints.WEST;
            rb.insets = new Insets(6, 0, 6, 16);
            JLabel nhan = new JLabel(cacNhan[i]);
            nhan.setFont(phongChu(Font.BOLD, 13));
            nhan.setForeground(MAU_CHU);
            form.add(nhan, rb);
            rb.gridx = 1;
            rb.weightx = 1;
            rb.fill = GridBagConstraints.HORIZONTAL;
            rb.insets = new Insets(6, 0, 6, 0);
            form.add(cacOnhap[i], rb);
        }
        return form;
    }

    // ============================================================ BẢNG
    /** Thanh cuộn mảnh, bo tròn, không có nút mũi tên. */
    private static class ThanhCuonManh extends BasicScrollBarUI {
        private static JButton nutRong() {
            JButton nut = new JButton();
            Dimension khongKichThuoc = new Dimension(0, 0);
            nut.setPreferredSize(khongKichThuoc);
            nut.setMinimumSize(khongKichThuoc);
            nut.setMaximumSize(khongKichThuoc);
            return nut;
        }

        @Override protected JButton createDecreaseButton(int huong) { return nutRong(); }
        @Override protected JButton createIncreaseButton(int huong) { return nutRong(); }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle vung) { /* nền trong suốt */ }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            return scrollbar.getOrientation() == JScrollBar.VERTICAL ? new Dimension(10, 48) : new Dimension(48, 10);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle vung) {
            if (vung.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            g2.setColor(isDragging || isThumbRollover() ? MAU_GOI_Y : MAU_VIEN_ONHAP);
            if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                g2.fillRoundRect(vung.x + 2, vung.y, vung.width - 4, vung.height, 8, 8);
            } else {
                g2.fillRoundRect(vung.x, vung.y + 2, vung.width, vung.height - 4, 8, 8);
            }
            g2.dispose();
        }
    }

    /** Thanh cuộn không viền, nền trắng, bọc quanh bảng. */
    public static JScrollPane taoThanhCuon(JComponent noiDung) {
        JScrollPane cuon = new JScrollPane(noiDung);
        cuon.setBorder(BorderFactory.createEmptyBorder());
        cuon.getViewport().setBackground(Color.WHITE);
        for (JScrollBar thanh : new JScrollBar[]{cuon.getVerticalScrollBar(), cuon.getHorizontalScrollBar()}) {
            thanh.setUI(new ThanhCuonManh());
            thanh.setBackground(Color.WHITE);
            thanh.setUnitIncrement(16);
        }
        return cuon;
    }

    /** Tạo mô hình bảng chỉ để xem (không cho sửa trực tiếp trong ô). */
    public static DefaultTableModel taoMoHinhBang(String... cacCot) {
        return new DefaultTableModel(cacCot, 0) {
            @Override public boolean isCellEditable(int dong, int cot) { return false; }
        };
    }

    private static final String KHOA_DONG_RE = "dongDangRe";      // thuộc tính của bảng: dòng đang được rê chuột qua
    private static final Border VIEN_O_BANG = new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, MAU_NEN_XAM), new EmptyBorder(0, 10, 0, 10));

    /** Màu nền của một dòng: đang chọn, đang rê chuột hoặc bình thường. */
    private static Color layMauNenDong(JTable bang, int dong, boolean chon) {
        if (chon) return MAU_DONG_CHON;
        Object dongRe = bang.getClientProperty(KHOA_DONG_RE);
        return (dongRe instanceof Integer && (Integer) dongRe == dong) ? MAU_DONG_RE : Color.WHITE;
    }

    /** Tô nền một ô và kẻ đường mảnh ở đáy, dùng cho các ô tự vẽ. */
    private static void veNenDong(Graphics2D g2, JComponent o, Color nen) {
        g2.setColor(nen);
        g2.fillRect(0, 0, o.getWidth(), o.getHeight());
        g2.setColor(MAU_NEN_XAM);
        g2.drawLine(0, o.getHeight() - 1, o.getWidth(), o.getHeight() - 1);
    }

    /** Ô bảng thường: đường kẻ mảnh giữa các dòng, căn lề tùy cột. */
    public static class OBangThuong extends DefaultTableCellRenderer {
        public OBangThuong(int canLe) { setHorizontalAlignment(canLe); }

        @Override
        public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon, boolean coFocus, int dong, int cot) {
            super.getTableCellRendererComponent(bang, giaTri, chon, false, dong, cot);
            setBorder(VIEN_O_BANG);
            setForeground(MAU_CHU);
            setBackground(layMauNenDong(bang, dong, chon));
            return this;
        }
    }

    /** Ô bảng hiển thị trạng thái dạng "viên thuốc" màu có chấm tròn. Dùng themKieu() để khai báo màu cho từng trạng thái. */
    public static class OBangTrangThai extends JLabel implements TableCellRenderer {
        private final Map<String, Color[]> cacKieu = new HashMap<>();   // trạng thái -> {màu chữ, màu nền}
        private Color mauVienThuoc = Color.WHITE;
        private Color mauNenDong = Color.WHITE;

        public OBangTrangThai() {
            setOpaque(false);
            setFont(phongChu(Font.BOLD, 12));
        }

        public OBangTrangThai themKieu(String trangThai, Color mauChuKieu, Color mauNenKieu) {
            cacKieu.put(trangThai.toLowerCase(), new Color[]{mauChuKieu, mauNenKieu});
            return this;
        }

        @Override
        public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon, boolean coFocus, int dong, int cot) {
            String chu = giaTri == null ? "" : giaTri.toString();
            Color[] kieu = cacKieu.get(chu.toLowerCase());
            if (kieu == null) kieu = new Color[]{MAU_CHU_NHAT, MAU_NEN_XAM};   // trạng thái lạ: màu xám
            setText(chu);
            setForeground(kieu[0]);
            mauVienThuoc = kieu[1];
            mauNenDong = layMauNenDong(bang, dong, chon);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            veNenDong(g2, this, mauNenDong);
            String chu = getText();
            if (!chu.isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int cao = 24;
                int rong = Math.min(fm.stringWidth(chu) + 30, getWidth() - 6);
                int x = (getWidth() - rong) / 2, y = (getHeight() - cao) / 2;
                g2.setColor(mauVienThuoc);
                g2.fillRoundRect(x, y, rong, cao, cao, cao);
                g2.setColor(getForeground());
                g2.fillOval(x + 10, y + (cao - 6) / 2, 6, 6);                // chấm tròn đầu viên thuốc
                g2.drawString(chu, x + 21, y + (cao + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }

    /** Vẽ thanh tiến độ bo tròn: nền xám, phần đã xong màu chính (đủ 100% thì màu xanh lá). */
    public static void veThanhTienDo(Graphics2D g2, int x, int y, int rong, int cao, int phanTram) {
        g2.setColor(MAU_THANH_TIEN_DO);
        g2.fillRoundRect(x, y, rong, cao, cao, cao);
        g2.setColor(phanTram >= 100 ? MAU_XANH_LA : MAU_CHINH);
        g2.fillRoundRect(x, y, Math.max(phanTram > 0 ? cao : 0, rong * phanTram / 100), cao, cao, cao);
    }

    /** Thanh tiến độ trong ô bảng. Giá trị là Integer từ 0 đến 100; số âm nghĩa là chưa có tiến độ. */
    public static class OBangTienDo extends JComponent implements TableCellRenderer {
        private int phanTram = -1;
        private Color mauNenDong = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable bang, Object giaTri, boolean chon, boolean coFocus, int dong, int cot) {
            phanTram = giaTri instanceof Integer ? (Integer) giaTri : -1;
            mauNenDong = layMauNenDong(bang, dong, chon);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            khuRangCua(g2);
            veNenDong(g2, this, mauNenDong);
            g2.setFont(phongChu(Font.PLAIN, 12));
            FontMetrics fm = g2.getFontMetrics();
            int yChu = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            if (phanTram < 0) {
                g2.setColor(MAU_CHU_NHAT);
                g2.drawString("Chưa có", 10, yChu);
            } else {
                String chuPhanTram = phanTram + "%";
                int rongChu = fm.stringWidth("100%");
                int rongThanh = Math.max(20, getWidth() - 20 - rongChu - 8);
                veThanhTienDo(g2, 10, (getHeight() - 8) / 2, rongThanh, 8, phanTram);
                g2.setColor(MAU_CHU);
                g2.drawString(chuPhanTram, 10 + rongThanh + 8 + rongChu - fm.stringWidth(chuPhanTram), yChu);
            }
            g2.dispose();
        }
    }

    /** Ghi nhớ dòng đang rê chuột để các ô tô sáng dòng đó. */
    private static void datDongRe(JTable bang, int dong) {
        Object cu = bang.getClientProperty(KHOA_DONG_RE);
        if (cu instanceof Integer && (Integer) cu == dong) return;
        bang.putClientProperty(KHOA_DONG_RE, dong);
        bang.repaint();
    }

    /**
     * Áp kiểu hiện đại cho bảng: không lưới, tiêu đề phẳng, đường kẻ mảnh, dòng sáng lên khi rê chuột, cột số căn phải.
     * @param doRongCot độ rộng mong muốn của từng cột (có thể null)
     * @param cotCanPhai chỉ số các cột cần căn phải (thường là cột số và tiền)
     */
    public static void dinhDangBang(JTable bang, int[] doRongCot, int... cotCanPhai) {
        bang.setFont(phongChu(Font.PLAIN, 13));
        bang.setRowHeight(38);
        bang.setShowGrid(false);
        bang.setIntercellSpacing(new Dimension(0, 0));
        bang.setSelectionBackground(MAU_DONG_CHON);
        bang.setSelectionForeground(MAU_CHU);
        bang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bang.setFillsViewportHeight(true);

        bang.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { datDongRe(bang, bang.rowAtPoint(e.getPoint())); }
        });
        bang.addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { datDongRe(bang, -1); }
        });

        Set<Integer> canPhai = new HashSet<>();
        for (int cot : cotCanPhai) canPhai.add(cot);

        // Tiêu đề bảng
        JTableHeader tieuDe = bang.getTableHeader();
        tieuDe.setReorderingAllowed(false);
        tieuDe.setPreferredSize(new Dimension(0, 38));
        tieuDe.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable b, Object giaTri, boolean chon, boolean coFocus, int dong, int cot) {
                super.getTableCellRendererComponent(b, giaTri, false, false, dong, cot);
                setBackground(MAU_DONG_XEN);
                setForeground(MAU_CHU_NHAT);
                setFont(phongChu(Font.BOLD, 12));
                setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, MAU_VIEN), new EmptyBorder(0, 10, 0, 10)));
                setHorizontalAlignment(canPhai.contains(cot) ? RIGHT : LEFT);
                return this;
            }
        });

        // Ô dữ liệu và độ rộng cột
        OBangThuong oTrai = new OBangThuong(JLabel.LEFT);
        OBangThuong oPhai = new OBangThuong(JLabel.RIGHT);
        for (int i = 0; i < bang.getColumnCount(); i++) {
            bang.getColumnModel().getColumn(i).setCellRenderer(canPhai.contains(i) ? oPhai : oTrai);
            if (doRongCot != null && i < doRongCot.length) bang.getColumnModel().getColumn(i).setPreferredWidth(doRongCot[i]);
        }
    }
}
