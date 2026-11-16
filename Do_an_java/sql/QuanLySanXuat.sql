/* =====================================================================
   CSDL: QuanLySanXuat  (SQL Server)


   Gồm 6 bảng, chia hai nhóm:
     Danh mục  : TaiKhoan, KhachHang, SanPham
     Nghiệp vụ : DonHang, ChiTietDonHang, TienDoSanXuat

   Quan hệ:
     KhachHang 1 --- n DonHang 1 --- n ChiTietDonHang n --- 1 SanPham
                       DonHang 1 --- n TienDoSanXuat
     TaiKhoan  : độc lập (đăng nhập, phân quyền)

   Tài khoản mặc định:  admin / admin123   và   nhanvien / 123456
   ===================================================================== */

IF DB_ID(N'QuanLySanXuat') IS NULL
    CREATE DATABASE QuanLySanXuat;
GO

USE QuanLySanXuat;
GO

/* ---------------------------------------------------------------------
   (TÙY CHỌN) XÓA SẠCH CÁC BẢNG ĐỂ TẠO LẠI TỪ ĐẦU
   Chỉ bỏ dấu comment khối này khi CSDL đang có bảng theo cấu trúc cũ,
   hoặc khi muốn làm lại từ đầu. LƯU Ý: toàn bộ dữ liệu hiện có sẽ mất.
   ---------------------------------------------------------------------
IF OBJECT_ID(N'dbo.TienDoSanXuat',  N'U') IS NOT NULL DROP TABLE dbo.TienDoSanXuat;
IF OBJECT_ID(N'dbo.ChiTietDonHang', N'U') IS NOT NULL DROP TABLE dbo.ChiTietDonHang;
IF OBJECT_ID(N'dbo.DonHang',        N'U') IS NOT NULL DROP TABLE dbo.DonHang;
IF OBJECT_ID(N'dbo.DonHang_Cu',     N'U') IS NOT NULL DROP TABLE dbo.DonHang_Cu;
IF OBJECT_ID(N'dbo.SanPham',        N'U') IS NOT NULL DROP TABLE dbo.SanPham;
IF OBJECT_ID(N'dbo.KhachHang',      N'U') IS NOT NULL DROP TABLE dbo.KhachHang;
IF OBJECT_ID(N'dbo.TaiKhoan',       N'U') IS NOT NULL DROP TABLE dbo.TaiKhoan;
GO
*/

/* =====================================================================
   NHÓM DANH MỤC
   ===================================================================== */

-- 1. Tài khoản đăng nhập (mật khẩu lưu dạng băm SHA-256, không lưu mật khẩu thật)
IF OBJECT_ID(N'dbo.TaiKhoan', N'U') IS NULL
CREATE TABLE dbo.TaiKhoan (
    TenDangNhap  NVARCHAR(50)  NOT NULL,
    MatKhau      NVARCHAR(100) NOT NULL,                       -- chuỗi băm SHA-256 (64 ký tự hex)
    HoTen        NVARCHAR(100) NOT NULL,
    VaiTro       NVARCHAR(20)  NOT NULL
                 CONSTRAINT DF_TaiKhoan_VaiTro DEFAULT N'Nhân viên',
    TrangThai    BIT           NOT NULL
                 CONSTRAINT DF_TaiKhoan_TrangThai DEFAULT 1,   -- 1 = hoạt động, 0 = bị khóa
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (TenDangNhap),
    CONSTRAINT CK_TaiKhoan_VaiTro CHECK (VaiTro IN (N'Admin', N'Nhân viên'))
);
GO

-- 2. Khách hàng
IF OBJECT_ID(N'dbo.KhachHang', N'U') IS NULL
CREATE TABLE dbo.KhachHang (
    MaKH         NVARCHAR(50)  NOT NULL,
    TenKH        NVARCHAR(255) NOT NULL,
    SoDienThoai  NVARCHAR(20)  NULL,
    DiaChi       NVARCHAR(255) NULL,
    CONSTRAINT PK_KhachHang PRIMARY KEY (MaKH)
);
GO

-- 3. Sản phẩm
IF OBJECT_ID(N'dbo.SanPham', N'U') IS NULL
CREATE TABLE dbo.SanPham (
    MaSP         NVARCHAR(50)  NOT NULL,
    TenSP        NVARCHAR(255) NOT NULL,
    DonViTinh    NVARCHAR(50)  NOT NULL,
    DonGiaChuan  DECIMAL(18,2) NOT NULL
                 CONSTRAINT DF_SanPham_DonGiaChuan DEFAULT 0,  -- giá gợi ý khi lập đơn
    CONSTRAINT PK_SanPham PRIMARY KEY (MaSP),
    CONSTRAINT CK_SanPham_DonGiaChuan CHECK (DonGiaChuan >= 0)
);
GO

/* =====================================================================
   NHÓM NGHIỆP VỤ
   ===================================================================== */

-- 4. Đơn hàng: thông tin chung (khách nào, nhận lúc nào, xong lúc nào)
IF OBJECT_ID(N'dbo.DonHang', N'U') IS NULL
CREATE TABLE dbo.DonHang (
    MaDH               NVARCHAR(50)  NOT NULL,
    MaKH               NVARCHAR(50)  NOT NULL,
    NgayNhan           DATETIME      NOT NULL
                       CONSTRAINT DF_DonHang_NgayNhan DEFAULT GETDATE(),   -- thời gian nhận đơn
    ThoiGianHoanThanh  DATETIME      NULL,                                 -- NULL khi chưa hoàn thành
    TrangThai          NVARCHAR(50)  NOT NULL
                       CONSTRAINT DF_DonHang_TrangThai DEFAULT N'Chưa hoàn thành',
    GhiChu             NVARCHAR(500) NULL,
    CONSTRAINT PK_DonHang PRIMARY KEY (MaDH),
    CONSTRAINT CK_DonHang_TrangThai CHECK (TrangThai IN (N'Chưa hoàn thành', N'Đã hoàn thành')),
    -- Không cho xóa khách hàng khi còn đơn hàng
    CONSTRAINT FK_DonHang_KhachHang FOREIGN KEY (MaKH) REFERENCES dbo.KhachHang (MaKH)
);
GO

-- 5. Chi tiết đơn hàng: mỗi dòng là một sản phẩm trong đơn
IF OBJECT_ID(N'dbo.ChiTietDonHang', N'U') IS NULL
CREATE TABLE dbo.ChiTietDonHang (
    MaDH       NVARCHAR(50)  NOT NULL,
    MaSP       NVARCHAR(50)  NOT NULL,
    SoLuong    INT           NOT NULL,
    DonGia     DECIMAL(18,2) NOT NULL,
    ThanhTien  AS (SoLuong * DonGia),                          -- cột tính toán, SQL Server tự tính
    CONSTRAINT PK_ChiTietDonHang PRIMARY KEY (MaDH, MaSP),     -- một sản phẩm chỉ xuất hiện một dòng trong đơn
    CONSTRAINT CK_ChiTietDonHang_SoLuong CHECK (SoLuong > 0),
    CONSTRAINT CK_ChiTietDonHang_DonGia  CHECK (DonGia >= 0),
    -- Xóa đơn thì xóa luôn các dòng chi tiết
    CONSTRAINT FK_ChiTietDonHang_DonHang FOREIGN KEY (MaDH) REFERENCES dbo.DonHang (MaDH) ON DELETE CASCADE,
    -- Không cho xóa sản phẩm khi còn nằm trong đơn
    CONSTRAINT FK_ChiTietDonHang_SanPham FOREIGN KEY (MaSP) REFERENCES dbo.SanPham (MaSP)
);
GO

-- 6. Tiến độ sản xuất: các công đoạn của từng đơn
IF OBJECT_ID(N'dbo.TienDoSanXuat', N'U') IS NULL
CREATE TABLE dbo.TienDoSanXuat (
    MaTienDo     INT           IDENTITY(1,1) NOT NULL,         -- tự tăng
    MaDH         NVARCHAR(50)  NOT NULL,
    ThuTu        INT           NOT NULL
                 CONSTRAINT DF_TienDoSanXuat_ThuTu DEFAULT 1,  -- thứ tự công đoạn trong đơn
    TenCongDoan  NVARCHAR(100) NOT NULL,
    TrangThai    NVARCHAR(50)  NOT NULL
                 CONSTRAINT DF_TienDoSanXuat_TrangThai DEFAULT N'Chưa bắt đầu',
    NgayBatDau   DATETIME      NULL,
    NgayKetThuc  DATETIME      NULL,
    GhiChu       NVARCHAR(500) NULL,
    CONSTRAINT PK_TienDoSanXuat PRIMARY KEY (MaTienDo),
    CONSTRAINT CK_TienDoSanXuat_TrangThai
        CHECK (TrangThai IN (N'Chưa bắt đầu', N'Đang thực hiện', N'Hoàn thành')),
    -- Xóa đơn thì xóa luôn các công đoạn
    CONSTRAINT FK_TienDoSanXuat_DonHang FOREIGN KEY (MaDH) REFERENCES dbo.DonHang (MaDH) ON DELETE CASCADE
);
GO

/* =====================================================================
   DỮ LIỆU MẪU (chỉ thêm khi các bảng đang trống)
   ===================================================================== */

-- Tài khoản mặc định
IF NOT EXISTS (SELECT 1 FROM dbo.TaiKhoan)
    INSERT INTO dbo.TaiKhoan (TenDangNhap, MatKhau, HoTen, VaiTro, TrangThai) VALUES
    (N'admin',    N'240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', N'Quản trị viên',      N'Admin',     1),
    (N'nhanvien', N'8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', N'Nhân viên kế hoạch', N'Nhân viên', 1);
GO

-- Khách hàng, sản phẩm, đơn hàng, chi tiết đơn, tiến độ
IF NOT EXISTS (SELECT 1 FROM dbo.KhachHang)
   AND NOT EXISTS (SELECT 1 FROM dbo.SanPham)
   AND NOT EXISTS (SELECT 1 FROM dbo.DonHang)
BEGIN
    INSERT INTO dbo.KhachHang (MaKH, TenKH, SoDienThoai, DiaChi) VALUES
    (N'KH01', N'Công ty May Mặc ABC',   N'0901234567', N'Hà Nội'),
    (N'KH02', N'Tập đoàn Nội Thất XYZ', N'0987654321', N'TP. Hồ Chí Minh'),
    (N'KH03', N'Công ty Bao Bì DEF',    N'0911223344', N'Bắc Ninh');

    INSERT INTO dbo.SanPham (MaSP, TenSP, DonViTinh, DonGiaChuan) VALUES
    (N'SP01', N'Áo sơ mi nam K20',    N'Cái',   150000),
    (N'SP02', N'Bàn làm việc gỗ Sồi', N'Bộ',    1200000),
    (N'SP03', N'Hộp carton 3 lớp',    N'Thùng', 15000);

    INSERT INTO dbo.DonHang (MaDH, MaKH, NgayNhan, ThoiGianHoanThanh, TrangThai, GhiChu) VALUES
    (N'DH001', N'KH01', '20261008 08:30:00', '20261015 17:00:00', N'Đã hoàn thành',   N'Giao đúng hạn'),
    (N'DH002', N'KH02', '20261020 09:00:00', NULL,                N'Chưa hoàn thành', N'Ưu tiên sản xuất'),
    (N'DH003', N'KH03', '20261024 14:15:00', NULL,                N'Chưa hoàn thành', N'Giao trước ngày 30'),
    (N'DH004', N'KH02', '20261026 10:00:00', NULL,                N'Chưa hoàn thành', N'Đơn gồm 2 mặt hàng');

    INSERT INTO dbo.ChiTietDonHang (MaDH, MaSP, SoLuong, DonGia) VALUES
    (N'DH001', N'SP01', 500,  150000),
    (N'DH002', N'SP02', 50,   1200000),
    (N'DH003', N'SP03', 2000, 15000),
    (N'DH004', N'SP02', 20,   1200000),
    (N'DH004', N'SP03', 500,  15000);

    INSERT INTO dbo.TienDoSanXuat (MaDH, ThuTu, TenCongDoan, TrangThai, NgayBatDau, NgayKetThuc) VALUES
    (N'DH001', 1, N'Chuẩn bị nguyên liệu', N'Hoàn thành',     '20261008 09:00:00', '20261010 12:00:00'),
    (N'DH001', 2, N'Sản xuất',             N'Hoàn thành',     '20261010 13:00:00', '20261013 17:00:00'),
    (N'DH001', 3, N'Kiểm tra chất lượng',  N'Hoàn thành',     '20261014 08:00:00', '20261014 16:00:00'),
    (N'DH001', 4, N'Đóng gói',             N'Hoàn thành',     '20261015 08:00:00', '20261015 17:00:00'),
    (N'DH002', 1, N'Chuẩn bị nguyên liệu', N'Hoàn thành',     '20261020 10:00:00', '20261022 16:00:00'),
    (N'DH002', 2, N'Sản xuất',             N'Đang thực hiện', '20261023 08:00:00', NULL),
    (N'DH002', 3, N'Kiểm tra chất lượng',  N'Chưa bắt đầu',   NULL,                NULL),
    (N'DH002', 4, N'Đóng gói',             N'Chưa bắt đầu',   NULL,                NULL),
    (N'DH004', 1, N'Chuẩn bị nguyên liệu', N'Đang thực hiện', '20261027 08:00:00', NULL),
    (N'DH004', 2, N'Sản xuất',             N'Chưa bắt đầu',   NULL,                NULL);
END
GO

/* =====================================================================
   KIỂM TRA
   ===================================================================== */
SELECT * FROM dbo.TaiKhoan;
SELECT * FROM dbo.KhachHang;
SELECT * FROM dbo.SanPham;
SELECT * FROM dbo.DonHang;
SELECT * FROM dbo.ChiTietDonHang;
SELECT * FROM dbo.TienDoSanXuat;

-- Tổng hợp mỗi đơn: số mặt hàng, tổng số lượng, tổng tiền
SELECT d.MaDH, k.TenKH, d.NgayNhan, d.TrangThai,
       COUNT(c.MaSP)    AS SoMatHang,
       SUM(c.SoLuong)   AS TongSoLuong,
       SUM(c.ThanhTien) AS TongTien
FROM dbo.DonHang d
JOIN dbo.KhachHang k ON k.MaKH = d.MaKH
LEFT JOIN dbo.ChiTietDonHang c ON c.MaDH = d.MaDH
GROUP BY d.MaDH, k.TenKH, d.NgayNhan, d.TrangThai
ORDER BY d.NgayNhan DESC;
GO
