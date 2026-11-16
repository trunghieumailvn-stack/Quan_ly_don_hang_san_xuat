package view;

import dao.SanPhamDao;
import model.SanPham;

import javax.swing.*;
import java.util.List;

/** Trang quản lý danh mục sản phẩm. */
public class QuanLySanPham extends DanhMuc<SanPham> {

    private final SanPhamDao sanPhamDao = new SanPhamDao();

    public QuanLySanPham() {
        super("sản phẩm", new String[]{"Mã SP", "Tên sản phẩm", "Đơn vị tính", "Đơn giá chuẩn"},
                new int[]{90, 340, 140, 200}, 3);
    }

    @Override protected List<SanPham> taiDanhSach() { return sanPhamDao.layTatCa(); }

    @Override protected Object[] taoDong(SanPham sp) {
        return new Object[]{sp.getMaSanPham(), sp.getTenSanPham(), sp.getDonViTinh(), DinhDang.tien(sp.getDonGiaChuan())};
    }

    @Override protected boolean khopTuKhoa(SanPham sp, String tuKhoa) {
        return (sp.getMaSanPham() + " " + sp.getTenSanPham() + " " + sp.getDonViTinh()).toLowerCase().contains(tuKhoa);
    }

    @Override protected String layMa(SanPham sp) { return sp.getMaSanPham(); }

    /** Form thêm / sửa sản phẩm. Nhập sai thì báo lỗi và cho nhập lại, không đóng form. */
    @Override protected SanPham hienForm(SanPham cu, List<SanPham> danhSachHienCo) {
        boolean laThemMoi = cu == null;
        JTextField txtMa = new JTextField(laThemMoi ? goiYMaTiepTheo("SP", 2, danhSachHienCo) : cu.getMaSanPham(), 24);
        JTextField txtTen = new JTextField(laThemMoi ? "" : cu.getTenSanPham(), 24);
        JTextField txtDonViTinh = new JTextField(laThemMoi ? "Cái" : cu.getDonViTinh(), 24);
        JTextField txtDonGia = new JTextField(laThemMoi ? "0" : DinhDang.soThuan(cu.getDonGiaChuan()), 24);
        for (JTextField o : new JTextField[]{txtMa, txtTen, txtDonViTinh, txtDonGia}) GiaoDien.dinhDangOnhap(o);
        if (!laThemMoi) GiaoDien.khoaOnhap(txtMa);       // mã là khóa chính, không cho sửa

        JPanel form = GiaoDien.taoFormNhap(new String[]{"Mã sản phẩm *", "Tên sản phẩm *", "Đơn vị tính", "Đơn giá chuẩn (VNĐ)"},
                new JComponent[]{txtMa, txtTen, txtDonViTinh, txtDonGia});
        boolean dongY = GiaoDien.hienFormNhap(this, laThemMoi ? "Thêm sản phẩm" : "Sửa sản phẩm", form, () -> {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty() || txtTen.getText().trim().isEmpty()) return "Hãy nhập mã và tên sản phẩm.";
            if (laThemMoi && maDaTonTai(danhSachHienCo, ma)) return "Mã " + ma + " đã tồn tại. Hãy dùng mã khác.";
            Double donGia = DinhDang.phanTichSo(txtDonGia.getText());
            if (donGia == null) return "Đơn giá phải là số.";
            if (donGia < 0) return "Đơn giá không được âm.";
            return null;
        });
        if (!dongY) return null;
        String donViTinh = txtDonViTinh.getText().trim();
        return new SanPham(txtMa.getText().trim(), txtTen.getText().trim(), donViTinh.isEmpty() ? "Cái" : donViTinh,
                DinhDang.phanTichSo(txtDonGia.getText()));
    }

    @Override protected boolean thucHienThem(SanPham sp) { return sanPhamDao.them(sp); }
    @Override protected boolean thucHienCapNhat(SanPham sp) { return sanPhamDao.capNhat(sp); }
    @Override protected boolean thucHienXoa(SanPham sp) { return sanPhamDao.xoa(sp.getMaSanPham()); }
}
