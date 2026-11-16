package view;

import dao.DonHangDao;
import dao.TienDoDao;
import model.DonHang;
import model.TienDo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/** Xem và cập nhật các công đoạn sản xuất của một đơn hàng. */
public class FormTienDo extends JDialog {

    private final TienDoDao tienDoDao = new TienDoDao();
    private final DonHangDao donHangDao = new DonHangDao();
    private final DonHang donHang;
    private boolean donDaHoanThanh;               // đơn đã được đánh dấu hoàn thành chưa
    private boolean daHoiHoanThanhDon = false;    // chỉ hỏi "hoàn thành cả đơn?" một lần mỗi lần mở hộp thoại
    private List<TienDo> danhSach;                // các công đoạn đang hiển thị, đúng thứ tự dòng của bảng

    private final DefaultTableModel moHinhBang =
            GiaoDien.taoMoHinhBang("Thứ tự", "Công đoạn", "Bắt đầu", "Kết thúc", "Trạng thái", "Ghi chú");
    private final JTable bang = new JTable(moHinhBang);
    private final ThanhTienDoLon thanhTienDo = new ThanhTienDoLon();
    private final JLabel lblTongKet = new JLabel();
    private GiaoDien.NutBoGoc btnTaoMau;

    public FormTienDo(Window cha, DonHang donHang) {
        super(cha, "Tiến độ sản xuất - " + donHang.getMaDonHang(), ModalityType.APPLICATION_MODAL);
        this.donHang = donHang;
        this.donDaHoanThanh = donHang.isHoanThanh();

        setSize(900, 580);
        setLocationRelativeTo(cha);
        getContentPane().setBackground(GiaoDien.MAU_NEN);
        setLayout(new BorderLayout());

        GiaoDien.dinhDangBang(bang, new int[]{70, 200, 140, 140, 150, 160});
        bang.getColumnModel().getColumn(4).setCellRenderer(new GiaoDien.OBangTrangThai()
                .themKieu(TienDo.TRANG_THAI_CHUA_BAT_DAU, GiaoDien.MAU_CHU_NHAT, GiaoDien.MAU_NEN_XAM)
                .themKieu(TienDo.TRANG_THAI_DANG_THUC_HIEN, GiaoDien.MAU_CHU_CAM, GiaoDien.MAU_NEN_CAM)
                .themKieu(TienDo.TRANG_THAI_HOAN_THANH, GiaoDien.MAU_CHU_XANH, GiaoDien.MAU_NEN_XANH));
        GiaoDien.khiNhapDup(bang, this::suaCongDoan);               // nhấp đúp vào một công đoạn để sửa

        JPanel noiDung = new JPanel(new BorderLayout(0, 12));
        noiDung.setOpaque(false);
        noiDung.setBorder(new EmptyBorder(16, 16, 0, 16));
        noiDung.add(dungKhungDau(), BorderLayout.NORTH);
        noiDung.add(GiaoDien.taoThe(GiaoDien.taoThanhCuon(bang), 4), BorderLayout.CENTER);
        add(noiDung, BorderLayout.CENTER);
        add(dungThanhNut(), BorderLayout.SOUTH);

        taiLai();
    }

    /** Khung phía trên: mã đơn, khách hàng, tóm tắt và thanh tiến độ lớn. */
    private JPanel dungKhungDau() {
        JLabel tieuDe = new JLabel(donHang.getMaDonHang() + "  -  " + DinhDang.chu(donHang.getTenKhachHang()));
        tieuDe.setFont(GiaoDien.phongChu(Font.BOLD, 16));
        tieuDe.setForeground(GiaoDien.MAU_CHU);
        JLabel moTa = GiaoDien.taoNhan(donHang.getTomTatSanPham() + ", nhận đơn " + DinhDang.ngayGio(donHang.getNgayNhan(), "---"));
        lblTongKet.setFont(GiaoDien.phongChu(Font.BOLD, 13));
        lblTongKet.setForeground(GiaoDien.MAU_CHINH);

        JPanel hangTren = new JPanel(new BorderLayout());
        hangTren.setOpaque(false);
        hangTren.add(GiaoDien.xepDoc(tieuDe, Box.createVerticalStrut(2), moTa), BorderLayout.WEST);
        hangTren.add(lblTongKet, BorderLayout.EAST);

        JPanel trong = new JPanel(new BorderLayout(0, 12));
        trong.setOpaque(false);
        trong.add(hangTren, BorderLayout.NORTH);
        trong.add(thanhTienDo, BorderLayout.CENTER);
        return GiaoDien.taoThe(trong, 16);
    }

    /** Hàng nút ở dưới cùng. */
    private JPanel dungThanhNut() {
        btnTaoMau = GiaoDien.nutPhu("Tạo công đoạn mẫu");
        btnTaoMau.addActionListener(e -> {
            if (tienDoDao.taoCongDoanMau(donHang.getMaDonHang())) taiLai();
            else baoLoiThaoTac("Không tạo được công đoạn mẫu.");
        });
        GiaoDien.NutBoGoc btnThem = new GiaoDien.NutBoGoc("+  Thêm công đoạn", GiaoDien.MAU_CHINH, Color.WHITE);
        btnThem.addActionListener(e -> themCongDoan());
        GiaoDien.NutBoGoc btnBuocTiep = new GiaoDien.NutBoGoc("Chuyển bước tiếp theo", GiaoDien.MAU_XANH_LA, Color.WHITE);
        btnBuocTiep.addActionListener(e -> chuyenBuocTiepTheo());
        GiaoDien.NutBoGoc btnSua = new GiaoDien.NutBoGoc("Sửa", GiaoDien.MAU_VANG, GiaoDien.MAU_CHU);
        btnSua.addActionListener(e -> suaCongDoan());
        GiaoDien.NutBoGoc btnXoa = new GiaoDien.NutBoGoc("Xóa", GiaoDien.MAU_DO, Color.WHITE);
        btnXoa.addActionListener(e -> xoaCongDoan());
        GiaoDien.NutBoGoc btnDong = GiaoDien.nutPhu("Đóng");
        btnDong.addActionListener(e -> dispose());

        JPanel thanhDuoi = new JPanel(new BorderLayout());
        thanhDuoi.setOpaque(false);
        thanhDuoi.setBorder(new EmptyBorder(12, 16, 14, 16));
        thanhDuoi.add(GiaoDien.taoHangNut(FlowLayout.LEFT, btnTaoMau), BorderLayout.WEST);
        thanhDuoi.add(GiaoDien.taoHangNut(FlowLayout.RIGHT, btnThem, btnBuocTiep, btnSua, btnXoa, btnDong), BorderLayout.EAST);
        return thanhDuoi;
    }

    // ============================================================ DỮ LIỆU
    /** Đọc lại các công đoạn từ CSDL, cập nhật bảng và thanh tiến độ. */
    private void taiLai() {
        danhSach = tienDoDao.layTheoDonHang(donHang.getMaDonHang());
        moHinhBang.setRowCount(0);
        int soXong = 0;
        for (TienDo tienDo : danhSach) {
            if (TienDo.TRANG_THAI_HOAN_THANH.equals(tienDo.getTrangThai())) soXong++;
            moHinhBang.addRow(new Object[]{tienDo.getThuTu(), tienDo.getTenCongDoan(),
                    DinhDang.ngayGio(tienDo.getNgayBatDau(), "---"), DinhDang.ngayGio(tienDo.getNgayKetThuc(), "---"),
                    tienDo.getTrangThai(), DinhDang.chu(tienDo.getGhiChu())});
        }
        int phanTram = danhSach.isEmpty() ? 0 : (int) Math.round(soXong * 100.0 / danhSach.size());
        thanhTienDo.datPhanTram(phanTram);
        lblTongKet.setText(danhSach.isEmpty() ? "Chưa có công đoạn nào" : soXong + "/" + danhSach.size() + " công đoạn hoàn thành");
        btnTaoMau.setEnabled(danhSach.isEmpty());    // chỉ tạo mẫu khi đơn chưa có công đoạn nào

        // Khi mọi công đoạn đã xong mà đơn chưa hoàn thành thì hỏi có đánh dấu hoàn thành cả đơn không
        if (!danhSach.isEmpty() && soXong == danhSach.size() && !donDaHoanThanh && !daHoiHoanThanhDon) {
            daHoiHoanThanhDon = true;
            int traLoi = JOptionPane.showConfirmDialog(this,
                    "Tất cả công đoạn đã hoàn thành. Đánh dấu cả đơn hàng " + donHang.getMaDonHang() + " là hoàn thành?",
                    "Hoàn thành đơn hàng", JOptionPane.YES_NO_OPTION);
            if (traLoi == JOptionPane.YES_OPTION && donHangDao.danhDauHoanThanh(donHang.getMaDonHang())) donDaHoanThanh = true;
        }
    }

    private void baoLoiThaoTac(String noiDung) {
        GiaoDien.baoLoi(this, noiDung, tienDoDao.layLoiCuoi());
    }

    /** Lấy công đoạn ở dòng đang chọn; chưa chọn thì nhắc và trả về null. */
    private TienDo layCongDoanDangChon() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= danhSach.size()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một công đoạn trong bảng.");
            return null;
        }
        return danhSach.get(dong);
    }

    // ============================================================ THAO TÁC
    private void themCongDoan() {
        TienDo moi = new TienDo();
        moi.setMaDonHang(donHang.getMaDonHang());
        moi.setThuTu(danhSach.size() + 1);              // mặc định xếp vào cuối
        if (hienForm(moi, "Thêm công đoạn")) {
            if (tienDoDao.them(moi)) taiLai(); else baoLoiThaoTac("Không thêm được công đoạn.");
        }
    }

    private void suaCongDoan() {
        TienDo congDoan = layCongDoanDangChon();
        if (congDoan == null) return;
        if (hienForm(congDoan, "Sửa công đoạn")) {
            if (tienDoDao.capNhat(congDoan)) taiLai(); else baoLoiThaoTac("Không cập nhật được công đoạn.");
        }
    }

    private void xoaCongDoan() {
        TienDo congDoan = layCongDoanDangChon();
        if (congDoan == null) return;
        if (JOptionPane.showConfirmDialog(this, "Xóa công đoạn \"" + congDoan.getTenCongDoan() + "\"?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (tienDoDao.xoa(congDoan.getMaTienDo())) taiLai(); else baoLoiThaoTac("Không xóa được công đoạn.");
        }
    }

    /** Chuyển công đoạn đang chọn: Chưa bắt đầu -> Đang thực hiện -> Hoàn thành, tự ghi thời điểm hiện tại. */
    private void chuyenBuocTiepTheo() {
        TienDo congDoan = layCongDoanDangChon();
        if (congDoan == null) return;
        Date bayGio = new Date();
        if (TienDo.TRANG_THAI_CHUA_BAT_DAU.equals(congDoan.getTrangThai())) {
            congDoan.setTrangThai(TienDo.TRANG_THAI_DANG_THUC_HIEN);
            congDoan.setNgayBatDau(bayGio);
        } else if (TienDo.TRANG_THAI_DANG_THUC_HIEN.equals(congDoan.getTrangThai())) {
            congDoan.setTrangThai(TienDo.TRANG_THAI_HOAN_THANH);
            if (congDoan.getNgayBatDau() == null) congDoan.setNgayBatDau(bayGio);
            congDoan.setNgayKetThuc(bayGio);
        } else {
            JOptionPane.showMessageDialog(this, "Công đoạn này đã hoàn thành.");
            return;
        }
        if (tienDoDao.capNhat(congDoan)) taiLai(); else baoLoiThaoTac("Không cập nhật được công đoạn.");
    }

    /**
     * Hiện form nhập và ghi kết quả vào congDoan.
     * Thời gian được tự điền theo trạng thái: đang thực hiện thì có ngày bắt đầu, hoàn thành thì có cả hai ngày.
     * @return true nếu người dùng bấm OK và dữ liệu hợp lệ
     */
    private boolean hienForm(TienDo congDoan, String tieuDe) {
        JTextField txtTen = new JTextField(DinhDang.chu(congDoan.getTenCongDoan()), 24);
        JTextField txtThuTu = new JTextField(String.valueOf(congDoan.getThuTu()), 24);
        JComboBox<String> cbTrangThai = new JComboBox<>(
                new String[]{TienDo.TRANG_THAI_CHUA_BAT_DAU, TienDo.TRANG_THAI_DANG_THUC_HIEN, TienDo.TRANG_THAI_HOAN_THANH});
        cbTrangThai.setSelectedItem(congDoan.getTrangThai());
        JTextField txtBatDau = new JTextField(DinhDang.ngayGio(congDoan.getNgayBatDau()), 24);
        JTextField txtKetThuc = new JTextField(DinhDang.ngayGio(congDoan.getNgayKetThuc()), 24);
        JTextField txtGhiChu = new JTextField(DinhDang.chu(congDoan.getGhiChu()), 24);
        for (JTextField o : new JTextField[]{txtTen, txtThuTu, txtBatDau, txtKetThuc, txtGhiChu}) GiaoDien.dinhDangOnhap(o);
        GiaoDien.dinhDangComboBox(cbTrangThai);

        JPanel form = GiaoDien.taoFormNhap(
                new String[]{"Tên công đoạn *", "Thứ tự", "Trạng thái", "Bắt đầu (" + DinhDang.MAU_NGAY_GIO + ")",
                        "Kết thúc (" + DinhDang.MAU_NGAY_GIO + ")", "Ghi chú"},
                new JComponent[]{txtTen, txtThuTu, cbTrangThai, txtBatDau, txtKetThuc, txtGhiChu});

        // Kiểm tra dữ liệu; nếu hợp lệ thì ghi luôn vào congDoan (chỉ ghi khi mọi thứ đều đúng)
        return GiaoDien.hienFormNhap(this, tieuDe, form, () -> {
            String ten = txtTen.getText().trim();
            if (ten.isEmpty()) return "Hãy nhập tên công đoạn.";
            int thuTu;
            try {
                thuTu = Integer.parseInt(txtThuTu.getText().trim());
            } catch (NumberFormatException ex) {
                return "Thứ tự phải là số nguyên.";
            }
            Date batDau, ketThuc;
            try {
                batDau = DinhDang.phanTichNgayGio(txtBatDau.getText());
                ketThuc = DinhDang.phanTichNgayGio(txtKetThuc.getText());
            } catch (ParseException ex) {
                return "Thời gian chưa đúng định dạng " + DinhDang.MAU_NGAY_GIO + ". Ví dụ: 28/10/2026 08:30";
            }
            // Đồng bộ thời gian với trạng thái đã chọn
            String trangThai = (String) cbTrangThai.getSelectedItem();
            Date bayGio = new Date();
            if (TienDo.TRANG_THAI_CHUA_BAT_DAU.equals(trangThai)) {
                batDau = null;
                ketThuc = null;
            } else if (TienDo.TRANG_THAI_DANG_THUC_HIEN.equals(trangThai)) {
                if (batDau == null) batDau = bayGio;
                ketThuc = null;
            } else {
                if (batDau == null) batDau = bayGio;
                if (ketThuc == null) ketThuc = bayGio;
            }
            if (batDau != null && ketThuc != null && ketThuc.before(batDau)) {
                return "Thời gian kết thúc không thể trước thời gian bắt đầu.";
            }
            congDoan.setTenCongDoan(ten);
            congDoan.setThuTu(thuTu);
            congDoan.setTrangThai(trangThai);
            congDoan.setNgayBatDau(batDau);
            congDoan.setNgayKetThuc(ketThuc);
            congDoan.setGhiChu(txtGhiChu.getText().trim());
            return null;
        });
    }

    /** Thanh tiến độ lớn ở đầu hộp thoại. */
    private static class ThanhTienDoLon extends JComponent {
        private int phanTram;

        ThanhTienDoLon() { setPreferredSize(new Dimension(100, 26)); }

        void datPhanTram(int phanTram) {
            this.phanTram = phanTram;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(GiaoDien.phongChu(Font.BOLD, 13));
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth("100%") + 10;
            int rongThanh = getWidth() - rongChu, cao = 12;
            GiaoDien.veThanhTienDo(g2, 0, (getHeight() - cao) / 2, rongThanh, cao, phanTram);
            g2.setColor(GiaoDien.MAU_CHU);
            g2.drawString(phanTram + "%", rongThanh + 10, (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }
}
