-- ============================================
--  DATABASE: n3_qlCuaHangTienLoi
-- ============================================
CREATE DATABASE n3_qlCuaHangTienLoi;
GO
USE n3_qlCuaHangTienLoi;
GO

-- ============================================
-- 1. BẢNG KHÁCH HÀNG
-- ============================================
CREATE TABLE KhachHang (
                           maKH NVARCHAR(10) PRIMARY KEY,
                           tenKH NVARCHAR(100),
                           gioiTinh BIT,
                           sdt NVARCHAR(15),
                           ngayTaoTK DATE DEFAULT GETDATE(),
                           diemTichLuy INT DEFAULT 0
);

-- ============================================
-- 2. BẢNG CHỨC VỤ
-- ============================================
CREATE TABLE ChucVu (
                        chucVu NVARCHAR(20) PRIMARY KEY
);

INSERT INTO ChucVu VALUES
                       (N'QUANLY'),
                       (N'THUNGAN');

-- ============================================
-- 3. BẢNG NHÂN VIÊN
-- ============================================
CREATE TABLE NhanVien (
                          maNV NVARCHAR(20) PRIMARY KEY,
                          tenNV NVARCHAR(100),
                          matKhau NVARCHAR(100),
                          gioiTinh BIT,
                          email NVARCHAR(100),
                          sdt NVARCHAR(15),
                          ngayTaoTaiKhoan DATE DEFAULT GETDATE(),
                          chucVu NVARCHAR(20),
                          FOREIGN KEY (chucVu) REFERENCES ChucVu(chucVu)
);

-- ============================================
-- 4. BẢNG LOẠI SẢN PHẨM
-- ============================================
CREATE TABLE LoaiSanPham (
                             loaiSP NVARCHAR(30) PRIMARY KEY
);

INSERT INTO LoaiSanPham VALUES
                            (N'DoAn'),
                            (N'NuocUong'),
                            (N'VatDungSinhHoat'),
                            (N'DoDungVPPHam'),
                            (N'ThucPhamDongLanh');

-- ============================================
-- 5. BẢNG SẢN PHẨM
-- ============================================
CREATE TABLE SanPham (
                         maSP NVARCHAR(20) PRIMARY KEY,
                         tenSP NVARCHAR(100),
                         giaSP FLOAT CHECK (giaSP >= 0),
                         hinhAnhSP NVARCHAR(255),
                         moTaSP NVARCHAR(255),
                         tinhTrangSP BIT DEFAULT 1,
                         loaiSP NVARCHAR(30),
                         FOREIGN KEY (loaiSP) REFERENCES LoaiSanPham(loaiSP)
);

-- ============================================
-- 6. BẢNG LOẠI KHUYẾN MÃI
-- ============================================
CREATE TABLE LoaiKM (
                        loaiKM NVARCHAR(50) PRIMARY KEY
);

INSERT INTO LoaiKM VALUES
                       (N'GiamGiaPhanTramSP'),
                       (N'GiamGiaTienSP'),
                       (N'GiamGiaPhanTramSLSP'),
                       (N'GiamGiaTienSLSP'),
                       (N'TangSPKhiDuTien'),
                       (N'GiamGiaPhanTramSPDuTien'),
                       (N'GiamGiaTienSPDuTien'),
                       (N'GiamGiaPhanTramHDDuTien');

-- ============================================
-- 7. BẢNG KHUYẾN MÃI
-- ============================================
CREATE TABLE KhuyenMai (
                           maKM INT IDENTITY(1,1) PRIMARY KEY,
                           tenKM NVARCHAR(100),
                           moTaKM NVARCHAR(255),
                           ngayBatDau DATE,
                           ngayKetThuc DATE,
                           loaiKM NVARCHAR(50),
                           FOREIGN KEY (loaiKM) REFERENCES LoaiKM(loaiKM)
);

-- ============================================
-- 8. BẢNG HÓA ĐƠN
-- ============================================
CREATE TABLE HoaDon (
                        maHoaDon NVARCHAR(20) PRIMARY KEY,
                        ngayGiaoDich DATE DEFAULT GETDATE(),
                        thongTinChung NVARCHAR(255),
                        tienKhach FLOAT CHECK (tienKhach >= 0),
                        thue FLOAT CHECK (thue >= 0),
                        maKH NVARCHAR(10),
                        maNV NVARCHAR(20),
                        maKM INT NULL,  -- hóa đơn có thể hoặc không có khuyến mãi
                        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
                        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
                        FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM)
);

-- ============================================
-- 9. BẢNG CHI TIẾT HÓA ĐƠN
-- ============================================
CREATE TABLE CT_HoaDon (
                           maHoaDon NVARCHAR(20),
                           maSP NVARCHAR(20),
                           soLuong INT CHECK (soLuong > 0),
                           PRIMARY KEY (maHoaDon, maSP),
                           FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
                           FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
);
GO
