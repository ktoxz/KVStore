-- Tạo database
CREATE DATABASE n3_qlCuaHangTienLoi;
GO

USE n3_qlCuaHangTienLoi;
GO

CREATE TABLE KhachHang (
    maKH NVARCHAR(10) PRIMARY KEY,
    sdt NVARCHAR(10),
    tenKH NVARCHAR(100),
    diemTichLuy INT DEFAULT 0,
    ngayTaoTK DATE
);

-- Bảng ChucVu (chuẩn theo UML)
CREATE TABLE ChucVu (
    chucVu NVARCHAR(20) PRIMARY KEY
);

-- Dữ liệu mẫu
INSERT INTO ChucVu VALUES
(N'QUANLY'),
(N'THUNGAN');

CREATE TABLE NhanVien (
    maNV NVARCHAR(20) PRIMARY KEY,
    tenNV NVARCHAR(100),
    matKhau NVARCHAR(100),
    gioiTinh BIT,
    email NVARCHAR(100),
    sdt NVARCHAR(15),
    ngayTaoTaiKhoan DATE,
    chucVu NVARCHAR(20),
    FOREIGN KEY (chucVu) REFERENCES ChucVu(chucVu)
);


CREATE TABLE LoaiSanPham (
    loaiSP NVARCHAR(30) PRIMARY KEY
);

INSERT INTO LoaiSanPham VALUES
(N'DoAn'),
(N'NuocUong'),
(N'VatDungSinhHoat'),
(N'DoDungVPPHam'),
(N'ThucPhamDongLanh');

CREATE TABLE SanPham (
    maSP NVARCHAR(20) PRIMARY KEY,
    tenSP NVARCHAR(100),
    giaSP FLOAT CHECK (giaSP >= 0),
    hinhAnhSP NVARCHAR(255), -- này khum bt
    moTaSP NVARCHAR(255),
    tinhTrangSP BIT,
    loaiSP NVARCHAR(30),
    FOREIGN KEY (loaiSP) REFERENCES LoaiSanPham(loaiSP)
);

CREATE TABLE LoaiKM (
    loaiKM NVARCHAR(50) PRIMARY KEY
);

-- Dữ liệu mẫu cho loại khuyến mãi
INSERT INTO LoaiKM VALUES
(N'GiamGiaPhanTramSP'),
(N'GiamGiaTienSP'),
(N'GiamGiaPhanTramSLSP'),
(N'GiamGiaTienSLSP'),
(N'TangSPKhiDuTien'),
(N'GiamGiaPhanTramSPDuTien'),
(N'GiamGiaTienSPDuTien'),
(N'GiamGiaPhanTramHDDuTien');

CREATE TABLE KhuyenMai (
    maKM INT IDENTITY(1,1) PRIMARY KEY, 
    tenKM NVARCHAR(100),
    moTaKM NVARCHAR(255),
    ngayBatDau DATE,
    ngayKetThuc DATE,
    loaiKM NVARCHAR(50),
    FOREIGN KEY (loaiKM) REFERENCES LoaiKM(loaiKM)
);

CREATE TABLE HoaDon (
    maHoaDon NVARCHAR(20) PRIMARY KEY,
    ngayGiaoDich DATE,
    thongTinChung NVARCHAR(255),
    tienKhach FLOAT CHECK (tienKhach >= 0),
    thue FLOAT CHECK (thue >= 0),
    -- khúc này chưa hiểu lắm
    maKH NVARCHAR(20),
    maNV NVARCHAR(20),
    FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);

CREATE TABLE CT_HoaDon (
    maHoaDon NVARCHAR(20),
    maSP NVARCHAR(20),
    soLuong INT CHECK (soLuong > 0),
    PRIMARY KEY (maHoaDon, maSP),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
);
