package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.connectDB.ConnectDB;

public class DAO_ThongKe {

    // ==================== 1. SẢN PHẨM BÁN RA ====================
    public List<Object[]> getSanPhamBanTheoTrang(int page, int rowsPerPage, Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        int offset = (page - 1) * rowsPerPage;
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT 
                    sp.maSP, sp.tenSP, 
                    SUM(ct.soLuong) AS soLuongBan, 
                    SUM(ct.soLuong * sp.giaSP) AS doanhThu
                FROM CT_HoaDon ct
                JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                GROUP BY sp.maSP, sp.tenSP
                ORDER BY doanhThu DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            stmt.setInt(3, offset);
            stmt.setInt(4, rowsPerPage);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maSP"),
                    rs.getString("tenSP"),
                    rs.getInt("soLuongBan"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

 // Tổng số sản phẩm bán trong khoảng thời gian
    public int getTongSoSanPhamBan(Date tuNgay, Date denNgay) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT COUNT(*)
                FROM (
                    SELECT DISTINCT sp.maSP
                    FROM CT_HoaDon ct
                    JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                    JOIN SanPham sp ON ct.maSP = sp.maSP
                    WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                ) AS tmp
            """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    // Top 5 sản phẩm (giữ nguyên)
    public List<Object[]> getTop5SanPham(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT TOP 5 sp.maSP, sp.tenSP, SUM(ct.soLuong) AS tongSoLuong
                FROM CT_HoaDon ct
                JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                GROUP BY sp.maSP, sp.tenSP
                ORDER BY tongSoLuong DESC
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maSP"),
                    rs.getString("tenSP"),
                    rs.getInt("tongSoLuong")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ==================== 2. HÓA ĐƠN THEO NHÂN VIÊN ====================
    public List<Object[]> getThongKeNhanVienTheoTrang(int page, int rowsPerPage, Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        int offset = (page - 1) * rowsPerPage;
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT 
                    nv.maNV, nv.tenNV, 
                    COUNT(hd.maHoaDon) AS soHoaDon,
                    SUM(ct.soLuong * sp.giaSP * (1 + hd.thue/100.0)) AS doanhThu
                FROM NhanVien nv
                JOIN HoaDon hd ON nv.maNV = hd.maNV
                JOIN CT_HoaDon ct ON hd.maHoaDon = ct.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                GROUP BY nv.maNV, nv.tenNV
                ORDER BY doanhThu DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            stmt.setInt(3, offset);
            stmt.setInt(4, rowsPerPage);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maNV"),
                    rs.getString("tenNV"),
                    rs.getInt("soHoaDon"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getTongSoNhanVienCoHoaDon(Date tuNgay, Date denNgay) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT COUNT(DISTINCT nv.maNV)
                FROM NhanVien nv
                JOIN HoaDon hd ON nv.maNV = hd.maNV
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ==================== 3. KHUYẾN MÃI HIỆN HÀNH ====================
    public List<Object[]> getKhuyenMaiHienHanhTheoTrang(int page, int rowsPerPage, Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        int offset = (page - 1) * rowsPerPage;
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT km.maKM, km.tenKM, sp.tenSP, km.ngayBatDau, km.ngayKetThuc,
                       ISNULL(SUM(ct.soLuong), 0) AS soLuongBan,
                       ISNULL(SUM(ct.soLuong * sp.giaSP * (1 + hd.thue/100.0)), 0) AS doanhThu
                FROM KhuyenMai km
                JOIN CT_KhuyenMai ctkm ON km.maKM = ctkm.maKM
                JOIN SanPham sp ON ctkm.maSP = sp.maSP
                LEFT JOIN CT_HoaDon ct ON ct.maSP = sp.maSP
                LEFT JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon 
                    AND hd.ngayGiaoDich BETWEEN ? AND ?
                WHERE km.ngayBatDau <= ? AND km.ngayKetThuc >= ?
                GROUP BY km.maKM, km.tenKM, sp.tenSP, km.ngayBatDau, km.ngayKetThuc
                ORDER BY km.ngayBatDau DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            // 1-2: khoảng thời gian bán để tính số lượng & doanh thu
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            // 3-4: điều kiện khuyến mãi còn hiệu lực trong khoảng ngày lọc
            stmt.setDate(3, new java.sql.Date(denNgay.getTime()));
            stmt.setDate(4, new java.sql.Date(tuNgay.getTime()));
            // 5-6: phân trang
            stmt.setInt(5, offset);
            stmt.setInt(6, rowsPerPage);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("maKM"),
                    rs.getString("tenKM"),
                    rs.getString("tenSP"),
                    rs.getDate("ngayBatDau"),
                    rs.getDate("ngayKetThuc"),
                    rs.getInt("soLuongBan"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

 // Đếm tổng dòng khuyến mãi hiện hành (một dòng = một record trong CT_KhuyenMai)
 // Vì GUI hiển thị mỗi sản phẩm trong khuyến mãi là 1 dòng, ta cần đếm CT_KhuyenMai chứ không phải DISTINCT km.maKM
    public int getTongSoKhuyenMaiHienHanh(Date tuNgay, Date denNgay) {
    	ConnectDB.getInstance();
    	Connection con = ConnectDB.getCon();
    	try {
    		String sql = """
    				    SELECT COUNT(*)
    				    FROM CT_KhuyenMai ctkm
    				    JOIN KhuyenMai km ON km.maKM = ctkm.maKM
    				    WHERE km.ngayBatDau <= ? AND km.ngayKetThuc >= ?
    				""";
    		PreparedStatement stmt = con.prepareStatement(sql);
    		// Lưu ý: thứ tự tham số phải khớp với WHERE ở trên (<= denNgay, >= tuNgay)
    		stmt.setDate(1, new java.sql.Date(denNgay.getTime()));
    		stmt.setDate(2, new java.sql.Date(tuNgay.getTime()));
    		ResultSet rs = stmt.executeQuery();
    		if (rs.next()) return rs.getInt(1);
    	} catch (SQLException e) { e.printStackTrace(); }
    	return 0;
    }


    // ===== Các hàm không phân trang để xuất toàn bộ dữ liệu =====
    public List<Object[]> getTatCaSanPhamBan(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT 
                    sp.maSP, sp.tenSP, 
                    SUM(ct.soLuong) AS soLuongBan, 
                    SUM(ct.soLuong * sp.giaSP) AS doanhThu
                FROM CT_HoaDon ct
                JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                GROUP BY sp.maSP, sp.tenSP
                ORDER BY doanhThu DESC
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maSP"),
                    rs.getString("tenSP"),
                    rs.getInt("soLuongBan"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getTatCaThongKeNhanVien(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT 
                    nv.maNV, nv.tenNV, 
                    COUNT(hd.maHoaDon) AS soHoaDon,
                    SUM(ct.soLuong * sp.giaSP * (1 + hd.thue/100.0)) AS doanhThu
                FROM NhanVien nv
                JOIN HoaDon hd ON nv.maNV = hd.maNV
                JOIN CT_HoaDon ct ON hd.maHoaDon = ct.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                GROUP BY nv.maNV, nv.tenNV
                ORDER BY doanhThu DESC
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getString("maNV"),
                    rs.getString("tenNV"),
                    rs.getInt("soHoaDon"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getTatCaKhuyenMaiHienHanh(Date tuNgay, Date denNgay) {
        List<Object[]> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT km.maKM, km.tenKM, sp.tenSP, km.ngayBatDau, km.ngayKetThuc,
                       ISNULL(SUM(ct.soLuong), 0) AS soLuongBan,
                       ISNULL(SUM(ct.soLuong * sp.giaSP * (1 + hd.thue/100.0)), 0) AS doanhThu
                FROM KhuyenMai km
                JOIN CT_KhuyenMai ctkm ON km.maKM = ctkm.maKM
                JOIN SanPham sp ON ctkm.maSP = sp.maSP
                LEFT JOIN CT_HoaDon ct ON ct.maSP = sp.maSP
                LEFT JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon 
                    AND hd.ngayGiaoDich BETWEEN ? AND ?
                WHERE km.ngayBatDau <= ? AND km.ngayKetThuc >= ?
                GROUP BY km.maKM, km.tenKM, sp.tenSP, km.ngayBatDau, km.ngayKetThuc
                ORDER BY km.ngayBatDau DESC
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            // 1-2: khoảng thời gian bán để tính số lượng & doanh thu
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            // 3-4: điều kiện khuyến mãi còn hiệu lực
            stmt.setDate(3, new java.sql.Date(denNgay.getTime()));
            stmt.setDate(4, new java.sql.Date(tuNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("maKM"),
                    rs.getString("tenKM"),
                    rs.getString("tenSP"),
                    rs.getDate("ngayBatDau"),
                    rs.getDate("ngayKetThuc"),
                    rs.getInt("soLuongBan"),
                    rs.getDouble("doanhThu")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ==================== TỔNG QUAN (giữ nguyên) ====================
    public int getTongSP(Date tuNgay, Date denNgay) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = "SELECT SUM(ct.soLuong) FROM CT_HoaDon ct JOIN HoaDon hd ON hd.maHoaDon = ct.maHoaDon WHERE hd.ngayGiaoDich BETWEEN ? AND ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public int getTongHoaDon(Date tuNgay, Date denNgay) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE ngayGiaoDich BETWEEN ? AND ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    public double getTongDoanhThu(Date tuNgay, Date denNgay) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        try {
            String sql = """
                SELECT SUM(ct.soLuong * sp.giaSP * (1 + hd.thue/100.0))
                FROM CT_HoaDon ct
                JOIN HoaDon hd ON ct.maHoaDon = hd.maHoaDon
                JOIN SanPham sp ON ct.maSP = sp.maSP
                WHERE hd.ngayGiaoDich BETWEEN ? AND ?
                """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDate(1, new java.sql.Date(tuNgay.getTime()));
            stmt.setDate(2, new java.sql.Date(denNgay.getTime()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
    
    
    
}