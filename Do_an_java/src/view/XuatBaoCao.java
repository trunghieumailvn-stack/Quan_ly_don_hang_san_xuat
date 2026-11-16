package view;

import model.ChiTietDonHang;
import model.DonHang;
import model.KhachHang;

import javax.swing.*;
import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Xuất danh sách đơn hàng ra file Excel.
 * File ghi theo định dạng XML Spreadsheet của Microsoft nên mở được trực tiếp bằng Excel, không cần thư viện ngoài.
 */
public final class XuatBaoCao {
    private XuatBaoCao() {}

    private static final String[] CAC_TIEU_DE = {"Mã Đơn Hàng", "Ngày Nhận Đơn", "Trạng Thái", "Ngày Hoàn Thành", "Tiến Độ (%)",
            "Ghi Chú", "Mã Khách Hàng", "Tên Khách Hàng", "Điện Thoại", "Địa Chỉ",
            "Mã Sản Phẩm", "Tên Sản Phẩm", "Đơn Vị", "Số Lượng", "Đơn Giá", "Thành Tiền"};
    private static final int[] DO_RONG_COT = {80, 110, 90, 110, 80, 160, 80, 150, 90, 120, 80, 150, 60, 70, 100, 110};
    private static final int SO_COT_SAN_PHAM = 6;      // 6 cột cuối (mã SP ... thành tiền) thuộc về dòng chi tiết

    /** Thay các ký tự đặc biệt của XML để file không bị lỗi. */
    private static String maHoaXml(String chuoi) {
        if (chuoi == null) return "";
        return chuoi.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    /** Ghi một ô chữ. */
    private static void ghiOChu(PrintWriter ghi, String giaTri) {
        ghi.println(" <Cell><Data ss:Type=\"String\">" + maHoaXml(giaTri) + "</Data></Cell>");
    }

    /** Ghi một ô số; laTien = true thì định dạng kiểu tiền (có VNĐ). */
    private static void ghiOSo(PrintWriter ghi, double giaTri, boolean laTien) {
        ghi.println(" <Cell" + (laTien ? " ss:StyleID=\"Tien\"" : "") + "><Data ss:Type=\"Number\">" + giaTri + "</Data></Cell>");
    }

    /** Ghi một dòng tổng kết ở cuối file. */
    private static void ghiDongTongKet(PrintWriter ghi, String nhan, double giaTri, boolean laTien) {
        ghi.println("<Row><Cell ss:StyleID=\"DamChu\"><Data ss:Type=\"String\">" + nhan + "</Data></Cell><Cell/>"
                + "<Cell" + (laTien ? " ss:StyleID=\"Tien\"" : "") + "><Data ss:Type=\"Number\">" + giaTri + "</Data></Cell></Row>");
    }

    /** Phần đầu file: khai báo XML và các kiểu định dạng (tiêu đề xanh, chữ đậm, số tiền). */
    private static void ghiPhanDau(PrintWriter ghi) {
        ghi.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ghi.println("<?mso-application progid=\"Excel.Sheet\"?>");
        ghi.println("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"");
        ghi.println(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"");
        ghi.println(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"");
        ghi.println(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
        ghi.println("<Styles>");
        ghi.println(" <Style ss:ID=\"TieuDe\"><Font ss:Bold=\"1\" ss:Color=\"#FFFFFF\" ss:Size=\"11\"/>"
                + "<Interior ss:Color=\"#2563EB\" ss:Pattern=\"Solid\"/>"
                + "<Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/></Style>");
        ghi.println(" <Style ss:ID=\"DamChu\"><Font ss:Bold=\"1\" ss:Color=\"#1E3A8A\" ss:Size=\"11\"/></Style>");
        ghi.println(" <Style ss:ID=\"Tien\"><NumberFormat ss:Format=\"#,##0&quot; VNĐ&quot;\"/></Style>");
        ghi.println("</Styles>");
        ghi.println("<Worksheet ss:Name=\"Danh Sách Đơn Hàng\">");
        ghi.println("<Table>");
        for (int rong : DO_RONG_COT) ghi.println(" <Column ss:Width=\"" + rong + "\"/>");
        ghi.println("<Row ss:Height=\"26\">");
        for (String tieuDe : CAC_TIEU_DE) {
            ghi.println(" <Cell ss:StyleID=\"TieuDe\"><Data ss:Type=\"String\">" + maHoaXml(tieuDe) + "</Data></Cell>");
        }
        ghi.println("</Row>");
    }

    /** Ghi thông tin chung của đơn (10 cột đầu) trong một dòng Excel. */
    private static void ghiThongTinDon(PrintWriter ghi, DonHang donHang, KhachHang khachHang) {
        ghiOChu(ghi, donHang.getMaDonHang());
        ghiOChu(ghi, DinhDang.ngayGio(donHang.getNgayNhan()));
        ghiOChu(ghi, donHang.getTrangThai());
        ghiOChu(ghi, DinhDang.ngayGio(donHang.getThoiGianHoanThanh(), "---"));
        int phanTram = donHang.getPhanTramTienDo();
        if (phanTram >= 0) ghiOSo(ghi, phanTram, false); else ghiOChu(ghi, "");
        ghiOChu(ghi, donHang.getGhiChu());
        ghiOChu(ghi, donHang.getMaKhachHang());
        ghiOChu(ghi, donHang.getTenKhachHang());
        ghiOChu(ghi, khachHang != null ? khachHang.getSoDienThoai() : "");
        ghiOChu(ghi, khachHang != null ? khachHang.getDiaChi() : "");
    }

    /**
     * Hiện hộp thoại chọn nơi lưu rồi ghi file. Mỗi sản phẩm của đơn là một dòng trong file.
     * @param danhSachDon các đơn cần xuất (thường là các đơn đang hiển thị sau khi lọc)
     * @param danhSachKhach danh sách khách hàng, để lấy thêm số điện thoại và địa chỉ
     */
    public static void xuat(Component cha, List<DonHang> danhSachDon, List<KhachHang> danhSachKhach) {
        JFileChooser chonFile = new JFileChooser();
        chonFile.setDialogTitle("Lưu file báo cáo Excel");
        chonFile.setSelectedFile(new File("BaoCaoDonHang.xls"));
        if (chonFile.showSaveDialog(cha) != JFileChooser.APPROVE_OPTION) return;

        String duongDan = chonFile.getSelectedFile().getAbsolutePath();
        if (!duongDan.toLowerCase().endsWith(".xls") && !duongDan.toLowerCase().endsWith(".xlsx")) duongDan += ".xls";

        Map<String, KhachHang> khachTheoMa = new HashMap<>();
        for (KhachHang kh : danhSachKhach) khachTheoMa.put(kh.getMaKhachHang(), kh);

        try (PrintWriter ghi = new PrintWriter(new File(duongDan), "UTF-8")) {
            ghiPhanDau(ghi);

            // Phần dữ liệu: mỗi dòng chi tiết của mỗi đơn là một dòng Excel
            long tongSoLuong = 0;
            double tongTien = 0;
            for (DonHang donHang : danhSachDon) {
                KhachHang khachHang = khachTheoMa.get(donHang.getMaKhachHang());
                List<ChiTietDonHang> cacDong = donHang.getDanhSachChiTiet();
                int soDongExcel = Math.max(1, cacDong.size());      // đơn chưa có sản phẩm vẫn ghi một dòng
                for (int i = 0; i < soDongExcel; i++) {
                    ChiTietDonHang chiTiet = cacDong.isEmpty() ? null : cacDong.get(i);
                    ghi.println("<Row ss:Height=\"22\">");
                    ghiThongTinDon(ghi, donHang, khachHang);
                    if (chiTiet != null) {
                        ghiOChu(ghi, chiTiet.getMaSanPham());
                        ghiOChu(ghi, chiTiet.getTenSanPham());
                        ghiOChu(ghi, chiTiet.getDonViTinh());
                        ghiOSo(ghi, chiTiet.getSoLuong(), false);
                        ghiOSo(ghi, chiTiet.getDonGia(), true);
                        ghiOSo(ghi, chiTiet.getThanhTien(), true);
                        tongSoLuong += chiTiet.getSoLuong();
                        tongTien += chiTiet.getThanhTien();
                    } else {
                        for (int k = 0; k < SO_COT_SAN_PHAM; k++) ghiOChu(ghi, "");
                    }
                    ghi.println("</Row>");
                }
            }

            // Phần tổng kết
            ghi.println("<Row></Row>");
            ghiDongTongKet(ghi, "Tổng số đơn hàng:", danhSachDon.size(), false);
            ghiDongTongKet(ghi, "Tổng số lượng sản phẩm:", tongSoLuong, false);
            ghiDongTongKet(ghi, "Tổng thành tiền:", tongTien, true);
            ghi.println("</Table>");
            ghi.println("</Worksheet>");
            ghi.println("</Workbook>");

            JOptionPane.showMessageDialog(cha, "Đã xuất " + danhSachDon.size() + " đơn hàng.\nFile được lưu tại: " + duongDan);
        } catch (Exception e) {
            e.printStackTrace();
            GiaoDien.baoLoi(cha, "Không ghi được file. Hãy kiểm tra file có đang mở trong Excel không.", e.getMessage());
        }
    }
}
