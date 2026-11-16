package view;

import dao.DonHangDao;
import dao.KhachHangDao;
import dao.TienDoDao;
import model.ChiTietDonHang;
import model.DonHang;
import model.DangNhap;
import model.TienDo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Trang "Đơn hàng" (màn hình chính): thẻ thống kê, bộ lọc và tìm kiếm, danh sách đơn,
 * và bên dưới là chi tiết sản phẩm cùng tiến độ của đơn đang chọn.
 */
public class QuanLyDonHang extends JPanel {

    // Các kiểu tìm kiếm trong ComboBox "Tìm theo"
    private static final String TIM_TAT_CA = "Tất cả";
    private static final String TIM_MA_DON_HANG = "Mã đơn hàng";
    private static final String TIM_MA_SAN_PHAM = "Mã sản phẩm";
    private static final String TIM_TEN_SAN_PHAM = "Tên sản phẩm";
    private static final String TIM_KHACH_HANG = "Khách hàng";
    private static final String TIM_SO_LUONG = "Số lượng";
    private static final String TIM_DON_GIA = "Đơn giá";

    private static final String MOI_TRANG_THAI = "Mọi trạng thái";

    /** Chữ gợi ý trong ô tìm kiếm theo từng kiểu tìm. */
    private static final Map<String, String> GOI_Y_TIM_KIEM = new HashMap<>();
    static {
        GOI_Y_TIM_KIEM.put(TIM_SO_LUONG, "Ví dụ: 500   >100   50-200");
        GOI_Y_TIM_KIEM.put(TIM_DON_GIA, "Ví dụ: 150000   <=20000   10000-50000");
        GOI_Y_TIM_KIEM.put(TIM_MA_DON_HANG, "Nhập mã đơn hàng");
        GOI_Y_TIM_KIEM.put(TIM_MA_SAN_PHAM, "Nhập mã sản phẩm");
        GOI_Y_TIM_KIEM.put(TIM_TEN_SAN_PHAM, "Nhập tên sản phẩm");
        GOI_Y_TIM_KIEM.put(TIM_KHACH_HANG, "Nhập tên khách hàng");
    }

    private final DonHangDao donHangDao = new DonHangDao();
    private final KhachHangDao khachHangDao = new KhachHangDao();
    private final TienDoDao tienDoDao = new TienDoDao();

    private List<DonHang> danhSachDonHang = new ArrayList<>();   // toàn bộ đơn đọc từ CSDL
    private final List<DonHang> dangHienThi = new ArrayList<>(); // các đơn sau khi lọc, đúng thứ tự dòng của bảng
    private boolean dangDungLaiBang = false;                     // true khi đang đổ lại bảng (bỏ qua sự kiện chọn dòng)

    private final DefaultTableModel moHinhBang = GiaoDien.taoMoHinhBang("Mã ĐH", "Khách hàng", "Sản phẩm", "SL",
            "Thành tiền (đ)", "Nhận đơn", "Hoàn thành", "Tiến độ", "Trạng thái", "Ghi chú");
    private final JTable bang = new JTable(moHinhBang);
    private final JComboBox<String> cbTimTheo =
            new JComboBox<>(new String[]{TIM_TAT_CA, TIM_MA_DON_HANG, TIM_MA_SAN_PHAM, TIM_TEN_SAN_PHAM, TIM_KHACH_HANG, TIM_SO_LUONG, TIM_DON_GIA});
    private final GiaoDien.OTimKiem txtTimKiem = new GiaoDien.OTimKiem(18);
    private final JComboBox<String> cbLocTrangThai =
            new JComboBox<>(new String[]{MOI_TRANG_THAI, DonHang.TRANG_THAI_CHUA_HOAN_THANH, DonHang.TRANG_THAI_DA_HOAN_THANH});
    private final JComboBox<String> cbLocThoiGian =
            new JComboBox<>(new String[]{"Mọi thời gian", "Hôm nay", "7 ngày qua", "30 ngày qua"});

    private final JLabel lblTongDon = new JLabel("0");
    private final JLabel lblChuaHoanThanh = new JLabel("0");
    private final JLabel lblDaHoanThanh = new JLabel("0");
    private final JLabel lblTongGiaTri = new JLabel("0 VNĐ");

    private final JLabel lblTieuDeChiTiet = GiaoDien.taoTieuDeMuc("Chi tiết đơn hàng");
    private final JLabel lblTieuDeTienDo = GiaoDien.taoTieuDeMuc("Tiến độ sản xuất");
    private final DefaultTableModel moHinhChiTiet =
            GiaoDien.taoMoHinhBang("Mã SP", "Tên sản phẩm", "ĐVT", "SL", "Đơn giá (đ)", "Thành tiền (đ)");
    private final DefaultTableModel moHinhTienDo = GiaoDien.taoMoHinhBang("Công đoạn", "Trạng thái", "Kết thúc");

    public QuanLyDonHang() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(4, 24, 18, 24));

        // Phần trên: thẻ thống kê + bộ lọc. Giữa: bảng đơn. Dưới: chi tiết và tiến độ.
        add(GiaoDien.xepDoc(dungHangThongKe(), Box.createVerticalStrut(12), dungTheLoc()), BorderLayout.NORTH);
        add(dungKhungBang(), BorderLayout.CENTER);
        add(dungHangChiTiet(), BorderLayout.SOUTH);

        capNhatGoiY();
    }

    /** Cửa sổ chứa trang này, dùng làm cha cho các hộp thoại. */
    private Window cuaSoCha() { return SwingUtilities.getWindowAncestor(this); }

    // ============================================================ DỰNG GIAO DIỆN
    /** Hàng 4 thẻ thống kê. */
    private JPanel dungHangThongKe() {
        JPanel hang = new JPanel(new GridLayout(1, 4, 12, 0));
        hang.setOpaque(false);
        hang.add(GiaoDien.taoTheThongKe("Tổng số đơn", lblTongDon, GiaoDien.MAU_CHINH, GiaoDien.ICON_DON_HANG));
        hang.add(GiaoDien.taoTheThongKe("Chưa hoàn thành", lblChuaHoanThanh, GiaoDien.MAU_CAM, GiaoDien.ICON_DONG_HO));
        hang.add(GiaoDien.taoTheThongKe("Đã hoàn thành", lblDaHoanThanh, GiaoDien.MAU_XANH_LA, GiaoDien.ICON_TICK));
        hang.add(GiaoDien.taoTheThongKe("Tổng giá trị", lblTongGiaTri, GiaoDien.MAU_THANH_BEN, GiaoDien.ICON_TIEN));
        hang.setPreferredSize(new Dimension(0, 74));
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, 74));
        return hang;
    }

    /** Thẻ bộ lọc: kiểu tìm kiếm, ô từ khóa, lọc trạng thái, lọc thời gian nhận đơn và nút Xuất Báo Cáo. */
    private JPanel dungTheLoc() {
        GiaoDien.KhungBoGoc the = new GiaoDien.KhungBoGoc(new BorderLayout(), GiaoDien.DO_BO_GOC_THE);
        the.setBorder(new EmptyBorder(10, 14, 10, 14));

        for (JComboBox<String> cb : java.util.Arrays.asList(cbTimTheo, cbLocTrangThai, cbLocThoiGian)) {
            GiaoDien.dinhDangComboBox(cb);
        }
        cbTimTheo.setToolTipText("Tìm theo");
        cbLocTrangThai.setToolTipText("Lọc theo trạng thái");
        cbLocThoiGian.setToolTipText("Lọc theo thời gian nhận đơn");
        cbTimTheo.addActionListener(e -> { capNhatGoiY(); locDuLieu(); });
        cbLocTrangThai.addActionListener(e -> locDuLieu());
        cbLocThoiGian.addActionListener(e -> locDuLieu());
        GiaoDien.khiGoChu(txtTimKiem, this::locDuLieu);                // lọc ngay khi gõ

        JPanel nhomTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        nhomTrai.setOpaque(false);
        nhomTrai.add(cbTimTheo);
        nhomTrai.add(txtTimKiem);
        nhomTrai.add(cbLocTrangThai);
        nhomTrai.add(cbLocThoiGian);

        GiaoDien.NutBoGoc btnXuatExcel = new GiaoDien.NutBoGoc("Xuất Báo Cáo", GiaoDien.MAU_XANH_LA, Color.WHITE);
        btnXuatExcel.addActionListener(e -> XuatBaoCao.xuat(this, new ArrayList<>(dangHienThi), khachHangDao.layTatCa()));

        the.add(nhomTrai, BorderLayout.CENTER);
        the.add(GiaoDien.taoHangNut(FlowLayout.RIGHT, btnXuatExcel), BorderLayout.EAST);
        the.setPreferredSize(new Dimension(0, 60));
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        return the;
    }

    /** Đổi chữ gợi ý trong ô tìm kiếm theo kiểu tìm đang chọn. */
    private void capNhatGoiY() {
        txtTimKiem.datGoiY(GOI_Y_TIM_KIEM.getOrDefault((String) cbTimTheo.getSelectedItem(), "Mã đơn, khách hàng, sản phẩm"));
    }

    /** Thẻ chứa thanh nút thao tác và bảng danh sách đơn hàng. */
    private JPanel dungKhungBang() {
        GiaoDien.dinhDangBang(bang, new int[]{70, 135, 150, 55, 125, 125, 125, 105, 150, 80}, 3, 4);
        bang.setRowHeight(34);
        bang.getColumnModel().getColumn(7).setCellRenderer(new GiaoDien.OBangTienDo());
        bang.getColumnModel().getColumn(8).setCellRenderer(new GiaoDien.OBangTrangThai()
                .themKieu(DonHang.TRANG_THAI_DA_HOAN_THANH, GiaoDien.MAU_CHU_XANH, GiaoDien.MAU_NEN_XANH)
                .themKieu(DonHang.TRANG_THAI_CHUA_HOAN_THANH, GiaoDien.MAU_CHU_CAM, GiaoDien.MAU_NEN_CAM));

        // Chọn dòng nào thì hiện chi tiết và tiến độ của đơn đó ở phần dưới
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !dangDungLaiBang) hienChiTiet();
        });
        GiaoDien.khiNhapDup(bang, this::suaDon);                       // nhấp đúp để sửa đơn

        GiaoDien.NutBoGoc btnThem = new GiaoDien.NutBoGoc("+  Thêm đơn hàng", GiaoDien.MAU_CHINH, Color.WHITE);
        btnThem.addActionListener(e -> themDon());
        GiaoDien.NutBoGoc btnSua = GiaoDien.nutPhu("Sửa");
        btnSua.addActionListener(e -> suaDon());
        GiaoDien.NutBoGoc btnTienDo = GiaoDien.nutPhu("Cập nhật tiến độ");
        btnTienDo.addActionListener(e -> capNhatTienDo());
        GiaoDien.NutBoGoc btnHoanThanh = new GiaoDien.NutBoGoc("Đánh dấu hoàn thành", GiaoDien.MAU_XANH_LA, Color.WHITE);
        btnHoanThanh.addActionListener(e -> hoanThanhDon());
        GiaoDien.NutBoGoc btnXoa = new GiaoDien.NutBoGoc("Xóa", GiaoDien.MAU_DO, Color.WHITE);
        btnXoa.addActionListener(e -> xoaDon());
        btnXoa.setVisible(DangNhap.laAdmin());                    // chỉ Admin được xóa đơn

        JLabel nhanGoiY = GiaoDien.taoNhan("Nhấp đúp vào một dòng để sửa đơn");
        nhanGoiY.setFont(GiaoDien.phongChu(Font.ITALIC, 12));

        JPanel thanhCongCu = new JPanel(new BorderLayout());
        thanhCongCu.setOpaque(false);
        thanhCongCu.setBorder(new EmptyBorder(4, 4, 10, 4));
        thanhCongCu.add(GiaoDien.taoHangNut(FlowLayout.LEFT, btnThem, btnSua, btnTienDo, btnHoanThanh, btnXoa), BorderLayout.WEST);
        thanhCongCu.add(nhanGoiY, BorderLayout.EAST);

        GiaoDien.KhungBoGoc the = new GiaoDien.KhungBoGoc(new BorderLayout(), GiaoDien.DO_BO_GOC_THE);
        the.setBorder(new EmptyBorder(10, 10, 6, 10));
        the.add(thanhCongCu, BorderLayout.NORTH);
        the.add(GiaoDien.taoThanhCuon(bang), BorderLayout.CENTER);
        return the;
    }

    /** Hàng dưới cùng: bảng chi tiết sản phẩm (trái) và bảng tiến độ (phải) của đơn đang chọn. */
    private JPanel dungHangChiTiet() {
        JTable bangChiTiet = new JTable(moHinhChiTiet);
        GiaoDien.dinhDangBang(bangChiTiet, new int[]{70, 160, 60, 50, 100, 115}, 3, 4, 5);
        bangChiTiet.setRowHeight(30);

        JTable bangTienDo = new JTable(moHinhTienDo);
        GiaoDien.dinhDangBang(bangTienDo, new int[]{200, 130, 110});
        bangTienDo.setRowHeight(30);
        bangTienDo.getColumnModel().getColumn(1).setCellRenderer(new GiaoDien.OBangTrangThai()
                .themKieu(TienDo.TRANG_THAI_CHUA_BAT_DAU, GiaoDien.MAU_CHU_NHAT, GiaoDien.MAU_NEN_XAM)
                .themKieu(TienDo.TRANG_THAI_DANG_THUC_HIEN, GiaoDien.MAU_CHU_CAM, GiaoDien.MAU_NEN_CAM)
                .themKieu(TienDo.TRANG_THAI_HOAN_THANH, GiaoDien.MAU_CHU_XANH, GiaoDien.MAU_NEN_XANH));

        JPanel hang = new JPanel(new GridLayout(1, 2, 12, 0));
        hang.setOpaque(false);
        hang.add(taoKhungBangNho(lblTieuDeChiTiet, bangChiTiet));
        hang.add(taoKhungBangNho(lblTieuDeTienDo, bangTienDo));
        hang.setPreferredSize(new Dimension(0, 210));
        return hang;
    }

    /** Thẻ gồm tiêu đề và một bảng nhỏ bên dưới. */
    private JPanel taoKhungBangNho(JLabel tieuDe, JTable bangNho) {
        JPanel noiDung = new JPanel(new BorderLayout(0, 8));
        noiDung.setOpaque(false);
        noiDung.add(tieuDe, BorderLayout.NORTH);
        noiDung.add(GiaoDien.taoThanhCuon(bangNho), BorderLayout.CENTER);
        return GiaoDien.taoThe(noiDung, 12);
    }

    // ============================================================ DỮ LIỆU
    /** Đọc lại danh sách đơn từ CSDL rồi lọc và hiển thị. */
    public void taiLai() {
        danhSachDonHang = donHangDao.layTatCa();
        if (donHangDao.layLoiCuoi() != null) {
            GiaoDien.baoLoi(this, "Không tải được danh sách đơn hàng:", donHangDao.layLoiCuoi());
        }
        locDuLieu();
    }

    /** Điều kiện tìm theo số: 500 (bằng), >100, >=100, <50, <=50 hoặc 50-200 (trong khoảng). */
    private static class DieuKienSo {
        final String toanTu;
        final double soThuNhat, soThuHai;

        DieuKienSo(String toanTu, double soThuNhat, double soThuHai) {
            this.toanTu = toanTu;
            this.soThuNhat = soThuNhat;
            this.soThuHai = soThuHai;
        }

        boolean kiemTra(double giaTri) {
            switch (toanTu) {
                case ">":  return giaTri > soThuNhat;
                case ">=": return giaTri >= soThuNhat;
                case "<":  return giaTri < soThuNhat;
                case "<=": return giaTri <= soThuNhat;
                case "-":  return giaTri >= Math.min(soThuNhat, soThuHai) && giaTri <= Math.max(soThuNhat, soThuHai);
                default:   return Math.abs(giaTri - soThuNhat) < 0.0001;
            }
        }
    }

    /** Đổi chuỗi như "150.000" hoặc "150,000" thành số 150000 (bỏ dấu phân cách hàng nghìn). */
    private static double chuyenSo(String chuoi) {
        return Double.parseDouble(chuoi.replace(".", "").replace(",", ""));
    }

    /** Đọc từ khóa thành điều kiện số. Trả về null nếu từ khóa không phải số hợp lệ. */
    private static DieuKienSo phanTichDieuKienSo(String tuKhoa) {
        String chuoi = tuKhoa.replace(" ", "").replace("VNĐ", "").replace("vnđ", "");
        String toanTu = "=";
        if (chuoi.startsWith(">=")) { toanTu = ">="; chuoi = chuoi.substring(2); }
        else if (chuoi.startsWith("<=")) { toanTu = "<="; chuoi = chuoi.substring(2); }
        else if (chuoi.startsWith(">")) { toanTu = ">"; chuoi = chuoi.substring(1); }
        else if (chuoi.startsWith("<")) { toanTu = "<"; chuoi = chuoi.substring(1); }
        else if (chuoi.startsWith("=")) { chuoi = chuoi.substring(1); }
        try {
            if (toanTu.equals("=") && chuoi.indexOf('-') > 0) {      // dạng khoảng: 50-200
                String[] hai = chuoi.split("-");
                if (hai.length == 2) return new DieuKienSo("-", chuyenSo(hai[0]), chuyenSo(hai[1]));
            }
            return new DieuKienSo(toanTu, chuyenSo(chuoi), 0);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Chuỗi (không phân biệt hoa thường) có chứa từ khóa đã viết thường không. */
    private static boolean chua(String chuoi, String tuKhoaThuong) {
        return chuoi != null && chuoi.toLowerCase().contains(tuKhoaThuong);
    }

    /** Đơn có ít nhất một dòng sản phẩm thỏa điều kiện không. */
    private static boolean coDongThoa(DonHang donHang, Predicate<ChiTietDonHang> dieuKien) {
        for (ChiTietDonHang dong : donHang.getDanhSachChiTiet()) {
            if (dieuKien.test(dong)) return true;
        }
        return false;
    }

    /** Đơn có khớp từ khóa theo kiểu tìm đang chọn không. Đơn nhiều sản phẩm khớp nếu có ít nhất một dòng khớp. */
    private boolean khopTimKiem(DonHang donHang, String kieuTim, String tuKhoa) {
        if (tuKhoa.isEmpty()) return true;
        String tk = tuKhoa.toLowerCase();
        switch (kieuTim) {
            case TIM_MA_DON_HANG: return chua(donHang.getMaDonHang(), tk);
            case TIM_KHACH_HANG: return chua(donHang.getTenKhachHang(), tk);
            case TIM_MA_SAN_PHAM: return coDongThoa(donHang, dong -> chua(dong.getMaSanPham(), tk));
            case TIM_TEN_SAN_PHAM: return coDongThoa(donHang, dong -> chua(dong.getTenSanPham(), tk));
            case TIM_SO_LUONG:
            case TIM_DON_GIA: {
                DieuKienSo dieuKien = phanTichDieuKienSo(tuKhoa);
                if (dieuKien == null) return false;                  // gõ chưa ra số thì chưa có kết quả
                boolean theoSoLuong = TIM_SO_LUONG.equals(kieuTim);
                return coDongThoa(donHang, dong -> dieuKien.kiemTra(theoSoLuong ? dong.getSoLuong() : dong.getDonGia()));
            }
            default:                                                  // "Tất cả": thử mọi trường chữ
                return chua(donHang.getMaDonHang(), tk) || chua(donHang.getTenKhachHang(), tk)
                        || coDongThoa(donHang, dong -> chua(dong.getMaSanPham(), tk) || chua(dong.getTenSanPham(), tk));
        }
    }

    /** Mốc thời gian sớm nhất của bộ lọc "thời gian nhận đơn". Trả về null nếu chọn "Mọi thời gian". */
    private Date layMocThoiGian() {
        Calendar lich = Calendar.getInstance();
        switch (cbLocThoiGian.getSelectedIndex()) {
            case 1:                                                   // hôm nay: từ 00:00 sáng nay
                lich.set(Calendar.HOUR_OF_DAY, 0);
                lich.set(Calendar.MINUTE, 0);
                lich.set(Calendar.SECOND, 0);
                lich.set(Calendar.MILLISECOND, 0);
                return lich.getTime();
            case 2: lich.add(Calendar.DAY_OF_MONTH, -7); return lich.getTime();
            case 3: lich.add(Calendar.DAY_OF_MONTH, -30); return lich.getTime();
            default: return null;
        }
    }

    /** Đơn ở dòng đang chọn của bảng, hoặc null nếu chưa chọn. */
    private DonHang layDonDangChon() {
        int dong = bang.getSelectedRow();
        return (dong >= 0 && dong < dangHienThi.size()) ? dangHienThi.get(dong) : null;
    }

    /** Áp bộ lọc và từ khóa lên danh sách đơn, đổ vào bảng, cập nhật thẻ thống kê. Giữ nguyên dòng đang chọn nếu còn. */
    private void locDuLieu() {
        DonHang donDangChon = layDonDangChon();
        String maDangChon = donDangChon == null ? null : donDangChon.getMaDonHang();
        String tuKhoa = txtTimKiem.getText().trim();
        String kieuTim = (String) cbTimTheo.getSelectedItem();
        String trangThaiLoc = (String) cbLocTrangThai.getSelectedItem();
        Date mocThoiGian = layMocThoiGian();

        dangDungLaiBang = true;
        moHinhBang.setRowCount(0);
        dangHienThi.clear();
        int soChua = 0, soXong = 0;
        double tongGiaTri = 0;

        for (DonHang don : danhSachDonHang) {
            if (!MOI_TRANG_THAI.equals(trangThaiLoc) && !trangThaiLoc.equalsIgnoreCase(don.getTrangThai())) continue;
            if (mocThoiGian != null && (don.getNgayNhan() == null || don.getNgayNhan().before(mocThoiGian))) continue;
            if (!khopTimKiem(don, kieuTim, tuKhoa)) continue;

            dangHienThi.add(don);
            moHinhBang.addRow(new Object[]{
                    don.getMaDonHang(), don.getTenKhachHang(), don.getTomTatSanPham(), don.getTongSoLuong(),
                    DinhDang.so(don.getTongTien()), DinhDang.ngayGioNgan(don.getNgayNhan(), "---"),
                    DinhDang.ngayGioNgan(don.getThoiGianHoanThanh(), "---"), don.getPhanTramTienDo(),
                    don.getTrangThai(), DinhDang.chu(don.getGhiChu())});
            if (don.isHoanThanh()) soXong++; else soChua++;
            tongGiaTri += don.getTongTien();
        }

        lblTongDon.setText(String.valueOf(dangHienThi.size()));
        lblChuaHoanThanh.setText(String.valueOf(soChua));
        lblDaHoanThanh.setText(String.valueOf(soXong));
        lblTongGiaTri.setText(DinhDang.tien(tongGiaTri));

        // Chọn lại đơn đã chọn trước đó (nếu nó vẫn còn trong kết quả lọc)
        if (maDangChon != null) {
            for (int i = 0; i < dangHienThi.size(); i++) {
                if (dangHienThi.get(i).getMaDonHang().equals(maDangChon)) { bang.setRowSelectionInterval(i, i); break; }
            }
        }
        dangDungLaiBang = false;
        hienChiTiet();
    }

    /** Hiện các sản phẩm và công đoạn của đơn đang chọn ở hai bảng phía dưới. */
    private void hienChiTiet() {
        moHinhChiTiet.setRowCount(0);
        moHinhTienDo.setRowCount(0);
        DonHang donHang = layDonDangChon();
        if (donHang == null) {
            lblTieuDeChiTiet.setText("Chi tiết đơn hàng");
            lblTieuDeTienDo.setText("Tiến độ sản xuất");
            return;
        }
        lblTieuDeChiTiet.setText("Chi tiết đơn hàng " + donHang.getMaDonHang());
        for (ChiTietDonHang dong : donHang.getDanhSachChiTiet()) {
            moHinhChiTiet.addRow(new Object[]{dong.getMaSanPham(), dong.getTenSanPham(), dong.getDonViTinh(), dong.getSoLuong(),
                    DinhDang.so(dong.getDonGia()), DinhDang.so(dong.getThanhTien())});
        }
        List<TienDo> cacCongDoan = tienDoDao.layTheoDonHang(donHang.getMaDonHang());
        int soXong = 0;
        for (TienDo congDoan : cacCongDoan) {
            if (TienDo.TRANG_THAI_HOAN_THANH.equals(congDoan.getTrangThai())) soXong++;
            moHinhTienDo.addRow(new Object[]{congDoan.getThuTu() + ". " + congDoan.getTenCongDoan(), congDoan.getTrangThai(),
                    DinhDang.ngayGioNgan(congDoan.getNgayKetThuc(), "---")});
        }
        lblTieuDeTienDo.setText(cacCongDoan.isEmpty() ? "Tiến độ sản xuất: chưa có công đoạn"
                : "Tiến độ sản xuất: " + soXong + "/" + cacCongDoan.size() + " công đoạn");
    }

    // ============================================================ THAO TÁC
    /** Lấy đơn đang chọn; chưa chọn thì hiện lời nhắc và trả về null. */
    private DonHang yeuCauChonDon(String loiNhac) {
        DonHang donHang = layDonDangChon();
        if (donHang == null) JOptionPane.showMessageDialog(this, loiNhac);
        return donHang;
    }

    private void themDon() {
        new FormDonHang(cuaSoCha(), null, danhSachDonHang, this::taiLai).setVisible(true);
    }

    private void suaDon() {
        DonHang donHang = yeuCauChonDon("Hãy chọn một đơn hàng để sửa.");
        if (donHang != null) new FormDonHang(cuaSoCha(), donHang, danhSachDonHang, this::taiLai).setVisible(true);
    }

    private void capNhatTienDo() {
        DonHang donHang = yeuCauChonDon("Hãy chọn một đơn hàng để cập nhật tiến độ.");
        if (donHang == null) return;
        new FormTienDo(cuaSoCha(), donHang).setVisible(true);
        taiLai();                                                     // tiến độ đổi nên cần tải lại để cập nhật thanh tiến độ
    }

    private void hoanThanhDon() {
        DonHang donHang = yeuCauChonDon("Hãy chọn đơn hàng cần đánh dấu hoàn thành.");
        if (donHang == null) return;
        if (donHang.isHoanThanh()) {
            JOptionPane.showMessageDialog(this, "Đơn hàng này đã hoàn thành từ trước.");
            return;
        }
        if (donHangDao.danhDauHoanThanh(donHang.getMaDonHang())) taiLai();
        else GiaoDien.baoLoi(this, "Không cập nhật được đơn hàng.", donHangDao.layLoiCuoi());
    }

    private void xoaDon() {
        DonHang donHang = yeuCauChonDon("Hãy chọn đơn hàng cần xóa.");
        if (donHang == null) return;
        int traLoi = JOptionPane.showConfirmDialog(this,
                "Xóa đơn hàng " + donHang.getMaDonHang() + "? Danh sách sản phẩm và tiến độ của đơn cũng bị xóa theo.",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (traLoi != JOptionPane.YES_OPTION) return;
        if (donHangDao.xoa(donHang.getMaDonHang())) taiLai();
        else GiaoDien.baoLoi(this, "Không xóa được đơn hàng.", donHangDao.layLoiCuoi());
    }
}
