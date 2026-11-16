package view;

import dao.KhachHangDao;
import model.KhachHang;

import javax.swing.*;
import java.util.List;

/** Trang quản lý danh mục khách hàng. */
public class QuanLyKhachHang extends DanhMuc<KhachHang> {

    private final KhachHangDao khachHangDao = new KhachHangDao();

    public QuanLyKhachHang() {
        super("khách hàng", new String[]{"Mã KH", "Tên khách hàng", "Số điện thoại", "Địa chỉ"},
                new int[]{90, 300, 160, 340});
    }

    @Override protected List<KhachHang> taiDanhSach() { return khachHangDao.layTatCa(); }

    @Override protected Object[] taoDong(KhachHang kh) {
        return new Object[]{kh.getMaKhachHang(), kh.getTenKhachHang(), kh.getSoDienThoai(), kh.getDiaChi()};
    }

    @Override protected boolean khopTuKhoa(KhachHang kh, String tuKhoa) {
        return (kh.getMaKhachHang() + " " + kh.getTenKhachHang() + " " + kh.getSoDienThoai() + " " + kh.getDiaChi())
                .toLowerCase().contains(tuKhoa);
    }

    @Override protected String layMa(KhachHang kh) { return kh.getMaKhachHang(); }

    /** Form thêm / sửa khách hàng. Nhập sai thì báo lỗi và cho nhập lại, không đóng form. */
    @Override protected KhachHang hienForm(KhachHang cu, List<KhachHang> danhSachHienCo) {
        boolean laThemMoi = cu == null;
        JTextField txtMa = new JTextField(laThemMoi ? goiYMaTiepTheo("KH", 2, danhSachHienCo) : cu.getMaKhachHang(), 24);
        JTextField txtTen = new JTextField(laThemMoi ? "" : cu.getTenKhachHang(), 24);
        JTextField txtSoDienThoai = new JTextField(laThemMoi ? "" : DinhDang.chu(cu.getSoDienThoai()), 24);
        JTextField txtDiaChi = new JTextField(laThemMoi ? "" : DinhDang.chu(cu.getDiaChi()), 24);
        for (JTextField o : new JTextField[]{txtMa, txtTen, txtSoDienThoai, txtDiaChi}) GiaoDien.dinhDangOnhap(o);
        if (!laThemMoi) GiaoDien.khoaOnhap(txtMa);       // mã là khóa chính, không cho sửa

        JPanel form = GiaoDien.taoFormNhap(new String[]{"Mã khách hàng *", "Tên khách hàng *", "Số điện thoại", "Địa chỉ"},
                new JComponent[]{txtMa, txtTen, txtSoDienThoai, txtDiaChi});
        boolean dongY = GiaoDien.hienFormNhap(this, laThemMoi ? "Thêm khách hàng" : "Sửa khách hàng", form, () -> {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty() || txtTen.getText().trim().isEmpty()) return "Hãy nhập mã và tên khách hàng.";
            if (laThemMoi && maDaTonTai(danhSachHienCo, ma)) return "Mã " + ma + " đã tồn tại. Hãy dùng mã khác.";
            return null;
        });
        return dongY ? new KhachHang(txtMa.getText().trim(), txtTen.getText().trim(),
                txtSoDienThoai.getText().trim(), txtDiaChi.getText().trim()) : null;
    }

    @Override protected boolean thucHienThem(KhachHang kh) { return khachHangDao.them(kh); }
    @Override protected boolean thucHienCapNhat(KhachHang kh) { return khachHangDao.capNhat(kh); }
    @Override protected boolean thucHienXoa(KhachHang kh) { return khachHangDao.xoa(kh.getMaKhachHang()); }
}
