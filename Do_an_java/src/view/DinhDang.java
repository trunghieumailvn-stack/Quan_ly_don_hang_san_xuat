package view;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Định dạng ngày giờ, số tiền, chuỗi và mã dùng chung cho toàn bộ giao diện. */
public final class DinhDang {
    private DinhDang() {}

    /** Mẫu ngày giờ người dùng nhập và xem, ví dụ 28/10/2026 08:30. */
    public static final String MAU_NGAY_GIO = "dd/MM/yyyy HH:mm";

    private static final SimpleDateFormat DINH_DANG_NGAY_GIO = new SimpleDateFormat(MAU_NGAY_GIO);
    private static final SimpleDateFormat DINH_DANG_NGAY_GIO_NGAN = new SimpleDateFormat("dd/MM/yy HH:mm");
    private static final Locale VIET = Locale.forLanguageTag("vi-VN");
    private static final DecimalFormat DINH_DANG_TIEN =
            new DecimalFormat("#,##0 'VNĐ'", DecimalFormatSymbols.getInstance(VIET));
    private static final DecimalFormat DINH_DANG_SO =
            new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(VIET));

    static {
        // Không chấp nhận ngày sai như 31/02/2026
        DINH_DANG_NGAY_GIO.setLenient(false);
    }

    /** Ngày giờ đầy đủ. Trả về chuỗi rỗng nếu ngày là null. */
    public static String ngayGio(Date ngay) { return ngayGio(ngay, ""); }

    /** Ngày giờ đầy đủ. Trả về giaTriRong nếu ngày là null (ví dụ "---"). */
    public static String ngayGio(Date ngay, String giaTriRong) {
        return ngay == null ? giaTriRong : DINH_DANG_NGAY_GIO.format(ngay);
    }

    /** Ngày giờ dạng ngắn để hiện trong bảng, ví dụ 28/10/26 08:30. */
    public static String ngayGioNgan(Date ngay, String giaTriRong) {
        return ngay == null ? giaTriRong : DINH_DANG_NGAY_GIO_NGAN.format(ngay);
    }

    /** Đọc ngày giờ người dùng nhập. Chuỗi rỗng trả về null; sai định dạng thì ném ParseException. */
    public static Date phanTichNgayGio(String chuoi) throws ParseException {
        if (chuoi == null || chuoi.trim().isEmpty()) return null;
        return DINH_DANG_NGAY_GIO.parse(chuoi.trim());
    }

    /** Số tiền kèm đơn vị, ví dụ 1.200.000 VNĐ. */
    public static String tien(double soTien) { return DINH_DANG_TIEN.format(soTien); }

    /** Số có dấu phân cách hàng nghìn, không kèm đơn vị. Dùng trong bảng đã ghi (đ) ở tiêu đề cột. */
    public static String so(double giaTri) { return DINH_DANG_SO.format(giaTri); }

    /** Số không có dấu phân cách và không có phần thập phân, để điền vào ô nhập, ví dụ 1200000. */
    public static String soThuan(double giaTri) { return String.format("%.0f", giaTri); }

    /** Đọc số người dùng nhập (cho phép dấu phẩy phân cách hàng nghìn). Trả về null nếu không phải số. */
    public static Double phanTichSo(String chuoi) {
        try {
            return Double.valueOf(chuoi.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Chuỗi an toàn để hiện lên giao diện: null thành chuỗi rỗng. */
    public static String chu(String chuoi) { return chuoi == null ? "" : chuoi; }

    /** Gợi ý mã tiếp theo theo mẫu tiền tố + số, ví dụ có KH05 thì trả về KH06 (soChuSo = 2). */
    public static String goiYMa(String tienTo, int soChuSo, Iterable<String> cacMaHienCo) {
        Pattern mau = Pattern.compile("^" + Pattern.quote(tienTo) + "(\\d+)$", Pattern.CASE_INSENSITIVE);
        int lonNhat = 0;
        for (String ma : cacMaHienCo) {
            Matcher khop = mau.matcher(ma);
            if (khop.matches()) lonNhat = Math.max(lonNhat, Integer.parseInt(khop.group(1)));
        }
        return tienTo + String.format("%0" + soChuSo + "d", lonNhat + 1);
    }
}
