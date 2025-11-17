-- ============================================
--  DATABASE: n3_qlCuaHangTienLoi
-- ============================================
USE master;
GO

-- an toàn: ép ngắt kết nối rồi mới DROP (nếu DB tồn tại)
IF DB_ID('n3_qlCuaHangTienLoi') IS NOT NULL
BEGIN
    ALTER DATABASE n3_qlCuaHangTienLoi SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE n3_qlCuaHangTienLoi;
END
GO
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
                           sdt NVARCHAR(15) UNIQUE ,
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
                          email NVARCHAR(100) UNIQUE,
                          sdt NVARCHAR(15) UNIQUE,
                          ngayTaoTaiKhoan DATE DEFAULT GETDATE(),
                          chucVu NVARCHAR(20),
                          matKhauKichHoat nvarchar(100),
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
                            (N'DoDungVPPham'),
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
                       (N'GiamGiaTienSP');

-- ============================================
-- 7. BẢNG KHUYẾN MÃI
-- ============================================
CREATE TABLE KhuyenMai (
                           maKM INT IDENTITY(1,1) PRIMARY KEY,
                           tenKM NVARCHAR(100),
                           moTaKM NVARCHAR(255),
                           ngayBatDau DATE,
                           ngayKetThuc DATE
);

-- ============================================
-- BẢNG CHI TIẾT KHUYẾN MÃI
-- ============================================

CREATE TABLE CT_KhuyenMai(
                        maKM INT,
                        maSP NVARCHAR(20),
                        tiLe FLOAT CHECK (tiLe >=0),
                        loaiKM NVARCHAR(50),              
                        PRIMARY KEY (maKM, maSP, loaiKM),
                        FOREIGN KEY (loaiKM) REFERENCES LoaiKM(loaiKM),
                        FOREIGN KEY (maKM) REFERENCES KHUYENMAI(maKM) ON DELETE CASCADE,
                        FOREIGN KEY (maSP) REFERENCES SanPham(maSP)
);

-- ============================================
-- 8. BẢNG HÓA ĐƠN
-- ============================================
CREATE TABLE HoaDon (
                        maHoaDon NVARCHAR(20) PRIMARY KEY,
                        ngayGiaoDich DATE DEFAULT GETDATE(),
                        tienKhach FLOAT CHECK (tienKhach >= 0),
                        thue FLOAT CHECK (thue >= 0),
                        maKH NVARCHAR(10),
                        maNV NVARCHAR(20),
                        FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
                        FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
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

-- ============================================
-- THÊM SẢM PHẨM MẪU
-- ============================================
INSERT INTO SanPham (maSP, tenSP, giaSP, hinhAnhSP, moTaSP, tinhTrangSP, loaiSP)
VALUES
-- ====== DO AN ======
(N'SP001', N'Gạo ST25', 18000, N'gao_st25.jpg', N'Gạo thơm dẻo đặc sản Sóc Trăng', 1, N'DoAn'),
(N'SP002', N'Mì Hảo Hảo tôm chua cay', 4500, N'mihaohao.jpg', N'Mì ăn liền vị tôm chua cay', 1, N'DoAn'),
(N'SP003', N'Bánh Oreo 137g', 21000, N'oreo137.jpg', N'Bánh quy kem socola', 1, N'DoAn'),
(N'SP004', N'Phô mai Con Bò Cười 8 miếng', 44000, N'phomai_conboc.jpg', N'Phô mai béo ngậy, bổ sung canxi', 1, N'DoAn'),
(N'SP005', N'Cà phê G7 3in1 hộp 18 gói', 56000, N'cafe_g7_3in1.jpg', N'Cà phê hòa tan đậm đà hương vị Việt', 1, N'DoAn'),

-- ====== NUOC UONG ======
(N'SP006', N'Nước suối Lavie 500ml', 6000, N'lavie500.jpg', N'Nước suối tinh khiết đóng chai', 1, N'NuocUong'),
(N'SP007', N'Trà xanh 0 độ 455ml', 9000, N'traxanh0do.jpg', N'Trà xanh vị chanh thanh mát', 1, N'NuocUong'),
(N'SP008', N'Nước ngọt Pepsi lon 330ml', 11000, N'pepsi330.jpg', N'Nước giải khát có gas hương cola', 1, N'NuocUong'),
(N'SP009', N'Sữa tươi Vinamilk 180ml', 8000, N'vinamilk180.jpg', N'Sữa tươi tiệt trùng nguyên chất', 1, N'NuocUong'),
(N'SP010', N'Nước yến Ngân Nhĩ 190ml', 26000, N'nuocyen190.jpg', N'Nước yến thơm ngon, bổ dưỡng', 1, N'NuocUong'),

-- ====== THUC PHAM DONG LANH ======
(N'SP011', N'Cá basa phi lê 500g', 67000, N'cabasafile.jpg', N'Cá basa đông lạnh tươi ngon', 1, N'ThucPhamDongLanh'),
(N'SP012', N'Thịt heo xay 500g', 85000, N'thitheo500.jpg', N'Thịt heo sạch, đóng gói tiện lợi', 1, N'ThucPhamDongLanh'),
(N'SP013', N'Cánh gà đông lạnh 1kg', 97000, N'canhga1kg.jpg', N'Cánh gà tươi, bảo quản lạnh', 1, N'ThucPhamDongLanh'),
(N'SP014', N'Tôm sú đông lạnh 500g', 115000, N'tomsu500.jpg', N'Tôm sú tươi, giữ nguyên hương vị', 1, N'ThucPhamDongLanh'),
(N'SP015', N'Xúc xích CP 200g', 32000, N'xucxichcp.jpg', N'Xúc xích heo hương vị BBQ', 1, N'ThucPhamDongLanh'),

-- ====== VAT DUNG SINH HOAT ======
(N'SP016', N'Khăn giấy Pulppy 200 tờ', 12000, N'pulppy200.jpg', N'Khăn giấy mềm mại, an toàn da', 1, N'VatDungSinhHoat'),
(N'SP017', N'Bột giặt Omo 2.7kg', 98000, N'omo2_7kg.jpg', N'Bột giặt trắng sạch, hương dễ chịu', 1, N'VatDungSinhHoat'),
(N'SP018', N'Nước rửa chén Sunlight chanh 750ml', 32000, N'sunlight750.jpg', N'Làm sạch dầu mỡ, hương chanh tươi mát', 1, N'VatDungSinhHoat'),
(N'SP019', N'Giấy vệ sinh Bless You 10 cuộn', 62000, N'blessyou10.jpg', N'Giấy vệ sinh mềm mịn, dai', 1, N'VatDungSinhHoat'),
(N'SP020', N'Máy sấy tóc Philips', 285000, N'maysaytoc_philips.jpg', N'Máy sấy tóc công suất 1200W', 1, N'VatDungSinhHoat'),

-- ====== DO DUNG VPP HAM ======
(N'SP021', N'Bút bi Thiên Long TL-08', 5000, N'but_tl08.jpg', N'Bút bi xanh, nét viết êm', 1, N'DoDungVPPHam'),
(N'SP022', N'Sổ tay A5 bìa cứng', 19000, N'sotay_a5.jpg', N'Sổ tay 80 trang bìa dày', 1, N'DoDungVPPHam'),
(N'SP023', N'Kéo văn phòng 18cm', 15000, N'keo18cm.jpg', N'Kéo lưỡi thép không gỉ', 1, N'DoDungVPPHam'),
(N'SP024', N'Giấy in A4 Double A 80gsm', 89000, N'giayin_a4.jpg', N'Giấy in chất lượng cao, trắng mịn', 1, N'DoDungVPPHam'),
(N'SP025', N'Bìa hồ sơ nhựa A4', 6000, N'biahoso_a4.jpg', N'Bìa nhựa trong, bền đẹp', 1, N'DoDungVPPHam');

GO


-- ============================================
-- DATA MẪU CHO BẢNG NHÂN VIÊN (30 NHÂN VIÊN)
-- ============================================
INSERT INTO NhanVien (maNV, tenNV, matKhau, gioiTinh, email, sdt, ngayTaoTaiKhoan, chucVu) VALUES
(N'NV00001', N'Nguyễn Văn An',        N'123456', 1, N'an.nguyen@store.vn',        N'0901000001', '2025-01-01', N'QUANLY'),
(N'NV00002', N'Trần Thị Bích',        N'123456', 0, N'bich.tran@store.vn',        N'0901000002', '2025-01-02', N'THUNGAN');

GO


-- Alter
GO
ALTER TABLE CT_HoaDon ADD thanhTien FLOAT NULL;
GO
ALTER TABLE CT_HoaDon
    ADD maKM INT NULL
        FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM);

alter table NhanVien
add matKhauKichHoat nvarchar(50) 