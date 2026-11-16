package view;

import dao.DonHangDao;
import dao.KhachHangDao;
import dao.SanPhamDao;
import model.ChiTietDonHang;
import model.DonHang;
import model.KhachHang;
import model.SanPham;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Form thêm / sửa đơn hàng. Một đơn có thể gồm nhiều sản phẩm. */
public class FormDonHang extends JDialog {

    private final DonHangDao donHangDao = new DonHangDao();
    private final boolean laSua;                           // true: sửa đơn có sẵn, false: thêm đơn mới
    private final DonHang donHangCanSua;                   // null khi thêm mới
    private final List<DonHang> danhSachHienCo;            // các đơn đang có, để kiểm tra trùng mã và gợi ý mã mới
    private final Runnable khiLuuXong;                     // việc cần làm sau khi lưu thành công (tải lại danh sách)
    private final List<ChiTietDonHang> cacDong = new ArrayList<>();      // các dòng sản phẩm đang soạn
    private final Map<String, String> maKhachTheoTen = new LinkedHashMap<>(); // tên khách (chữ thường) -> mã khách

    // Thông tin chung của đơn
    private final JTextField txtMaDonHang = new JTextField();
    private final JComboBox<String> cbKhachHang;
    private final JTextField txtNgayNhan = new JTextField();
    private final JComboBox<String> cbTrangThai =
            new JComboBox<>(new String[]{DonHang.TRANG_THAI_CHUA_HOAN_THANH, DonHang.TRANG_THAI_DA_HOAN_THANH});
    private final JTextField txtGhiChu = new JTextField();

    // Phần nhập sản phẩm
    private final JComboBox<SanPham> cbSanPham;
    private final JTextField txtSoLuong = new JTextField("1");
    private final JTextField txtDonGia = new JTextField("0");
    private final DefaultTableModel moHinhBang =
            GiaoDien.taoMoHinhBang("Mã SP", "Tên sản phẩm", "ĐVT", "Số lượng", "Đơn giá", "Thành tiền");
    private final JTable bang = new JTable(moHinhBang);
    private final JLabel lblSoLuong = new JLabel();
    private final JLabel lblTongTien = new JLabel();
    private boolean dangNapDong = false;                   // true khi đang đổ dữ liệu từ dòng bảng lên ô nhập (tránh tự đổi đơn giá)

    public FormDonHang(Window cha, DonHang donHangCanSua, List<DonHang> danhSachHienCo, Runnable khiLuuXong) {
        super(cha, donHangCanSua == null ? "Thêm đơn hàng" : "Cập nhật đơn hàng", ModalityType.APPLICATION_MODAL);
        this.laSua = donHangCanSua != null;
        this.donHangCanSua = donHangCanSua;
        this.danhSachHienCo = danhSachHienCo;
        this.khiLuuXong = khiLuuXong;

        setSize(900, 760);
        setLocationRelativeTo(cha);
        getContentPane().setBackground(GiaoDien.MAU_NEN);
        setLayout(new BorderLayout());

        // Dữ liệu cho hai ComboBox: khách hàng (gõ thêm được) và sản phẩm (chỉ chọn)
        List<KhachHang> danhSachKhach = new KhachHangDao().layTatCa();
        for (KhachHang kh : danhSachKhach) maKhachTheoTen.put(kh.getTenKhachHang().toLowerCase(), kh.getMaKhachHang());
        cbKhachHang = new JComboBox<>(danhSachKhach.stream().map(KhachHang::getTenKhachHang).toArray(String[]::new));
        List<SanPham> danhSachSanPham = new SanPhamDao().layTatCa();
        cbSanPham = new JComboBox<>(danhSachSanPham.toArray(new SanPham[0]));

        // Khi sửa: chép các dòng sản phẩm của đơn sang danh sách đang soạn (để Hủy không làm đổi dữ liệu gốc)
        if (laSua) {
            for (ChiTietDonHang dong : donHangCanSua.getDanhSachChiTiet()) {
                cacDong.add(new ChiTietDonHang(dong.getMaSanPham(), dong.getTenSanPham(), dong.getDonViTinh(),
                        dong.getSoLuong(), dong.getDonGia()));
            }
        }

        JPanel noiDung = new JPanel(new BorderLayout(0, 14));
        noiDung.setOpaque(false);
        noiDung.setBorder(new EmptyBorder(16, 16, 0, 16));
        noiDung.add(dungKhungThongTin(), BorderLayout.NORTH);
        noiDung.add(dungKhungSanPham(danhSachSanPham.isEmpty()), BorderLayout.CENTER);
        add(noiDung, BorderLayout.CENTER);
        add(dungThanhDuoi(), BorderLayout.SOUTH);

        capNhatBang();
    }

    // ============================================================ THÔNG TIN CHUNG
    private JPanel dungKhungThongTin() {
        txtMaDonHang.setText(laSua ? donHangCanSua.getMaDonHang() : goiYMaDonHang());
        cbKhachHang.setEditable(true);
        cbKhachHang.setSelectedItem(laSua ? donHangCanSua.getTenKhachHang() : "");
        txtNgayNhan.setText(DinhDang.ngayGio(laSua && donHangCanSua.getNgayNhan() != null ? donHangCanSua.getNgayNhan() : new Date()));
        if (laSua) cbTrangThai.setSelectedItem(donHangCanSua.getTrangThai());
        txtGhiChu.setText(laSua ? DinhDang.chu(donHangCanSua.getGhiChu()) : "");

        GiaoDien.dinhDangOnhap(txtMaDonHang);
        GiaoDien.dinhDangOnhap(txtNgayNhan);
        GiaoDien.dinhDangOnhap(txtGhiChu);
        GiaoDien.dinhDangComboBox(cbKhachHang);
        GiaoDien.dinhDangComboBox(cbTrangThai);
        if (laSua) GiaoDien.khoaOnhap(txtMaDonHang);     // mã đơn là khóa chính, không cho sửa

        JPanel khung = new JPanel(new GridBagLayout());
        khung.setOpaque(false);
        datNhanVaOnhap(khung, 0, 0, "Mã đơn hàng *", txtMaDonHang, 1);
        datNhanVaOnhap(khung, 1, 0, "Khách hàng * (gõ tên mới để tự tạo)", cbKhachHang, 1);
        datNhanVaOnhap(khung, 0, 2, "Thời gian nhận đơn * (" + DinhDang.MAU_NGAY_GIO + ")", txtNgayNhan, 1);
        datNhanVaOnhap(khung, 1, 2, "Trạng thái", cbTrangThai, 1);
        datNhanVaOnhap(khung, 0, 4, "Ghi chú", txtGhiChu, 2);
        return GiaoDien.taoThe(khung, 16);
    }

    /** Đặt nhãn ở hàng dong, ô nhập ngay bên dưới (hàng dong + 1). soCot = 2 để ô trải hết chiều ngang. */
    private static void datNhanVaOnhap(JPanel khung, int cot, int dong, String chuNhan, JComponent oNhap, int soCot) {
        GridBagConstraints rb = new GridBagConstraints();
        rb.gridx = cot;
        rb.gridy = dong;
        rb.gridwidth = soCot;
        rb.weightx = 1;
        rb.fill = GridBagConstraints.HORIZONTAL;
        rb.anchor = GridBagConstraints.WEST;
        int leTrai = (cot == 0 && soCot == 1) ? 14 : 0;     // chừa khoảng cách giữa hai cột
        rb.insets = new Insets(dong == 0 ? 0 : 8, 0, 4, leTrai);
        khung.add(GiaoDien.taoNhanNho(chuNhan), rb);
        rb.gridy = dong + 1;
        rb.insets = new Insets(0, 0, 0, leTrai);
        khung.add(oNhap, rb);
    }

    /** Gợi ý mã đơn mới: lấy số lớn nhất trong các mã dạng DHxxx rồi cộng 1 (ví dụ DH004 -> DH005). */
    private String goiYMaDonHang() {
        List<String> cacMa = new ArrayList<>();
        for (DonHang don : danhSachHienCo) cacMa.add(don.getMaDonHang());
        return DinhDang.goiYMa("DH", 3, cacMa);
    }

    // ============================================================ DANH SÁCH SẢN PHẨM
    private JPanel dungKhungSanPham(boolean chuaCoSanPham) {
        JPanel khung = new JPanel(new BorderLayout(0, 10));
        khung.setOpaque(false);
        khung.add(GiaoDien.taoTieuDeMuc("Sản phẩm trong đơn"), BorderLayout.NORTH);

        // ComboBox sản phẩm hiển thị "mã - tên"
        cbSanPham.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> danhSach, Object giaTri, int viTri, boolean chon, boolean coFocus) {
                super.getListCellRendererComponent(danhSach, giaTri, viTri, chon, coFocus);
                if (giaTri instanceof SanPham) setText(((SanPham) giaTri).getMaSanPham() + "  -  " + ((SanPham) giaTri).getTenSanPham());
                return this;
            }
        });
        GiaoDien.dinhDangComboBox(cbSanPham);
        GiaoDien.dinhDangOnhap(txtSoLuong);
        GiaoDien.dinhDangOnhap(txtDonGia);
        txtSoLuong.setPreferredSize(new Dimension(90, 34));
        txtDonGia.setPreferredSize(new Dimension(130, 34));
        // Chọn sản phẩm thì tự điền đơn giá chuẩn (người dùng vẫn sửa được)
        cbSanPham.addActionListener(e -> {
            SanPham sp = (SanPham) cbSanPham.getSelectedItem();
            if (sp != null && !dangNapDong) txtDonGia.setText(DinhDang.soThuan(sp.getDonGiaChuan()));
        });
        if (cbSanPham.getItemCount() > 0) cbSanPham.setSelectedIndex(0);

        GiaoDien.NutBoGoc btnThemDong = new GiaoDien.NutBoGoc("Thêm / cập nhật dòng", GiaoDien.MAU_CHINH, Color.WHITE);
        btnThemDong.addActionListener(e -> themHoacSuaDong());
        GiaoDien.NutBoGoc btnXoaDong = GiaoDien.nutPhu("Xóa dòng đã chọn");
        btnXoaDong.addActionListener(e -> xoaDong());

        // Hàng nhập: sản phẩm, số lượng, đơn giá và hai nút
        JPanel hangNhap = new JPanel(new GridBagLayout());
        hangNhap.setOpaque(false);
        GridBagConstraints rb = new GridBagConstraints();
        rb.fill = GridBagConstraints.HORIZONTAL;
        rb.anchor = GridBagConstraints.WEST;
        rb.gridy = 0;
        rb.insets = new Insets(0, 0, 3, 10);
        rb.gridx = 0; rb.weightx = 1; hangNhap.add(GiaoDien.taoNhanNho("Sản phẩm"), rb);
        rb.gridx = 1; rb.weightx = 0; hangNhap.add(GiaoDien.taoNhanNho("Số lượng"), rb);
        rb.gridx = 2; hangNhap.add(GiaoDien.taoNhanNho("Đơn giá (VNĐ)"), rb);
        rb.gridy = 1;
        rb.insets = new Insets(0, 0, 0, 10);
        rb.gridx = 0; rb.weightx = 1; hangNhap.add(cbSanPham, rb);
        rb.gridx = 1; rb.weightx = 0; hangNhap.add(txtSoLuong, rb);
        rb.gridx = 2; hangNhap.add(txtDonGia, rb);
        rb.gridx = 3; rb.insets = new Insets(0, 0, 0, 8); hangNhap.add(btnThemDong, rb);
        rb.gridx = 4; rb.insets = new Insets(0, 0, 0, 0); hangNhap.add(btnXoaDong, rb);

        if (chuaCoSanPham) {
            btnThemDong.setEnabled(false);
            JLabel canhBao = new JLabel("Chưa có sản phẩm nào. Hãy thêm sản phẩm ở mục Sản phẩm trước.");
            canhBao.setForeground(GiaoDien.MAU_DO);
            canhBao.setFont(GiaoDien.phongChu(Font.PLAIN, 12));
            rb.gridx = 0; rb.gridy = 2; rb.gridwidth = 5; rb.insets = new Insets(6, 0, 0, 0);
            hangNhap.add(canhBao, rb);
        }

        // Bảng các dòng đã thêm; chọn một dòng thì đưa dữ liệu lên ô nhập để sửa
        GiaoDien.dinhDangBang(bang, new int[]{80, 260, 70, 90, 130, 140}, 3, 4, 5);
        bang.setRowHeight(34);
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int dong = bang.getSelectedRow();
            if (dong < 0 || dong >= cacDong.size()) return;
            napDongLenONhap(cacDong.get(dong));
        });

        // Dòng tổng: số mặt hàng ở bên trái, tổng tiền ở bên phải
        lblSoLuong.setFont(GiaoDien.phongChu(Font.PLAIN, 13));
        lblSoLuong.setForeground(GiaoDien.MAU_CHU_NHAT);
        lblTongTien.setFont(GiaoDien.phongChu(Font.BOLD, 18));
        lblTongTien.setForeground(GiaoDien.MAU_CHINH);
        JPanel dongTong = new JPanel(new BorderLayout());
        dongTong.setOpaque(false);
        dongTong.add(lblSoLuong, BorderLayout.WEST);
        dongTong.add(lblTongTien, BorderLayout.EAST);

        JPanel giua = new JPanel(new BorderLayout(0, 10));
        giua.setOpaque(false);
        giua.add(hangNhap, BorderLayout.NORTH);
        giua.add(GiaoDien.taoThe(GiaoDien.taoThanhCuon(bang), 4), BorderLayout.CENTER);
        giua.add(dongTong, BorderLayout.SOUTH);
        khung.add(giua, BorderLayout.CENTER);
        return khung;
    }

    /** Đưa một dòng sản phẩm đang chọn lên các ô nhập để sửa (không tự đổi đơn giá theo giá chuẩn). */
    private void napDongLenONhap(ChiTietDonHang chiTiet) {
        dangNapDong = true;
        for (int i = 0; i < cbSanPham.getItemCount(); i++) {
            if (cbSanPham.getItemAt(i).getMaSanPham().equals(chiTiet.getMaSanPham())) {
                cbSanPham.setSelectedIndex(i);
                break;
            }
        }
        txtSoLuong.setText(String.valueOf(chiTiet.getSoLuong()));
        txtDonGia.setText(DinhDang.soThuan(chiTiet.getDonGia()));
        dangNapDong = false;
    }

    /** Đổ lại bảng từ danh sách cacDong và tính tổng. */
    private void capNhatBang() {
        moHinhBang.setRowCount(0);
        int tongSoLuong = 0;
        double tongTien = 0;
        for (ChiTietDonHang dong : cacDong) {
            moHinhBang.addRow(new Object[]{dong.getMaSanPham(), dong.getTenSanPham(), dong.getDonViTinh(), dong.getSoLuong(),
                    DinhDang.tien(dong.getDonGia()), DinhDang.tien(dong.getThanhTien())});
            tongSoLuong += dong.getSoLuong();
            tongTien += dong.getThanhTien();
        }
        lblSoLuong.setText(cacDong.size() + " mặt hàng, tổng " + tongSoLuong + " sản phẩm");
        lblTongTien.setText(DinhDang.tien(tongTien));
    }

    /** Thêm dòng mới; nếu sản phẩm đã có trong đơn thì cập nhật số lượng và đơn giá của dòng đó. */
    private void themHoacSuaDong() {
        SanPham sanPham = (SanPham) cbSanPham.getSelectedItem();
        if (sanPham == null) return;
        int soLuong;
        Double donGia = DinhDang.phanTichSo(txtDonGia.getText());
        try {
            soLuong = Integer.parseInt(txtSoLuong.getText().trim());
        } catch (NumberFormatException ex) {
            soLuong = 0;
            donGia = null;                                  // báo chung một thông báo lỗi định dạng
        }
        if (donGia == null) {
            GiaoDien.canhBao(this, "Số lượng phải là số nguyên và đơn giá phải là số.");
            return;
        }
        if (soLuong <= 0 || donGia < 0) {
            GiaoDien.canhBao(this, "Số lượng phải lớn hơn 0 và đơn giá không được âm.");
            return;
        }
        ChiTietDonHang dongCoSan = null;
        for (ChiTietDonHang dong : cacDong) {
            if (dong.getMaSanPham().equals(sanPham.getMaSanPham())) dongCoSan = dong;
        }
        if (dongCoSan != null) {
            dongCoSan.setSoLuong(soLuong);
            dongCoSan.setDonGia(donGia);
        } else {
            cacDong.add(new ChiTietDonHang(sanPham.getMaSanPham(), sanPham.getTenSanPham(), sanPham.getDonViTinh(), soLuong, donGia));
        }
        capNhatBang();
        bang.clearSelection();
        txtSoLuong.setText("1");
    }

    private void xoaDong() {
        int dong = bang.getSelectedRow();
        if (dong < 0 || dong >= cacDong.size()) {
            JOptionPane.showMessageDialog(this, "Hãy chọn dòng sản phẩm cần xóa.");
            return;
        }
        cacDong.remove(dong);
        capNhatBang();
    }

    // ============================================================ LƯU
    private JPanel dungThanhDuoi() {
        GiaoDien.NutBoGoc btnHuy = GiaoDien.nutPhu("Hủy");
        btnHuy.addActionListener(e -> dispose());
        GiaoDien.NutBoGoc btnLuu = new GiaoDien.NutBoGoc(laSua ? "Lưu thay đổi" : "Lưu đơn hàng", GiaoDien.MAU_XANH_LA, Color.WHITE);
        btnLuu.addActionListener(e -> luu());
        getRootPane().setDefaultButton(btnLuu);

        JPanel thanh = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        thanh.setOpaque(false);
        thanh.setBorder(new EmptyBorder(0, 8, 4, 8));
        thanh.add(btnHuy);
        thanh.add(btnLuu);
        return thanh;
    }

    /** Đọc chữ trong ComboBox có thể gõ tay (kể cả chữ chưa bấm Enter). */
    private static String layChuComboBox(JComboBox<String> cb) {
        Object giaTri = cb.getEditor().getItem();
        return giaTri == null ? "" : giaTri.toString().trim();
    }

    /** Xác định mã khách: giữ nguyên nếu không đổi tên; dùng khách có sẵn nếu trùng tên; còn lại tạo mã mới từ tên. */
    private String xacDinhMaKhachHang(String tenKhachHang) {
        if (laSua && tenKhachHang.equalsIgnoreCase(donHangCanSua.getTenKhachHang())) return donHangCanSua.getMaKhachHang();
        String maCoSan = maKhachTheoTen.get(tenKhachHang.toLowerCase());
        if (maCoSan != null) return maCoSan;
        String maMoi = "KH_" + tenKhachHang.replaceAll("\\s+", "").toUpperCase();
        return maMoi.length() > 50 ? maMoi.substring(0, 50) : maMoi;       // cột MaKH trong CSDL dài tối đa 50 ký tự
    }

    /** Kiểm tra dữ liệu, ghép thành đối tượng DonHang rồi lưu xuống CSDL. */
    private void luu() {
        String maDonHang = txtMaDonHang.getText().trim();
        String tenKhachHang = layChuComboBox(cbKhachHang);
        if (maDonHang.isEmpty() || tenKhachHang.isEmpty()) {
            GiaoDien.canhBao(this, "Hãy nhập mã đơn hàng và khách hàng.");
            return;
        }
        if (!laSua) {
            for (DonHang don : danhSachHienCo) {
                if (don.getMaDonHang().equalsIgnoreCase(maDonHang)) {
                    GiaoDien.canhBao(this, "Mã đơn hàng " + maDonHang + " đã tồn tại. Hãy dùng mã khác.");
                    return;
                }
            }
        }
        Date ngayNhan;
        try {
            ngayNhan = DinhDang.phanTichNgayGio(txtNgayNhan.getText());
        } catch (ParseException ex) {
            ngayNhan = null;
        }
        if (ngayNhan == null) {
            GiaoDien.canhBao(this, "Thời gian nhận đơn chưa đúng định dạng " + DinhDang.MAU_NGAY_GIO + ". Ví dụ: 28/10/2026 08:30");
            return;
        }
        if (cacDong.isEmpty()) {
            GiaoDien.canhBao(this, "Đơn hàng cần có ít nhất một sản phẩm.");
            return;
        }

        // Đơn hoàn thành thì cần thời gian hoàn thành (giữ giá trị cũ nếu có, không thì lấy giờ hiện tại)
        String trangThai = (String) cbTrangThai.getSelectedItem();
        Date thoiGianHoanThanh = null;
        if (DonHang.TRANG_THAI_DA_HOAN_THANH.equals(trangThai)) {
            thoiGianHoanThanh = (laSua && donHangCanSua.getThoiGianHoanThanh() != null)
                    ? donHangCanSua.getThoiGianHoanThanh() : new Date();
            if (thoiGianHoanThanh.before(ngayNhan)) {
                GiaoDien.canhBao(this, "Thời gian hoàn thành không thể trước thời gian nhận đơn. Hãy kiểm tra lại thời gian nhận đơn.");
                return;
            }
        }

        DonHang donHang = new DonHang();
        donHang.setMaDonHang(maDonHang);
        donHang.setMaKhachHang(xacDinhMaKhachHang(tenKhachHang));
        donHang.setTenKhachHang(tenKhachHang);
        donHang.setNgayNhan(ngayNhan);
        donHang.setTrangThai(trangThai);
        donHang.setThoiGianHoanThanh(thoiGianHoanThanh);
        donHang.setGhiChu(txtGhiChu.getText().trim());
        donHang.setDanhSachChiTiet(new ArrayList<>(cacDong));

        boolean thanhCong = laSua ? donHangDao.capNhat(donHang) : donHangDao.them(donHang);
        if (thanhCong) {
            dispose();
            if (khiLuuXong != null) khiLuuXong.run();
        } else {
            GiaoDien.baoLoi(this, "Không lưu được đơn hàng.", donHangDao.layLoiCuoi());
        }
    }
}
