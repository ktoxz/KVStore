package com.dao;

import com.entity.SanPham;
import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_SanPham {
	
	// =========================
    //  LẤY TOÀN BỘ LOẠI SẢN PHẨM
    // =========================
    public List<String> getAllLoaiSanPham() {
        List<String> ds = new ArrayList<>();
        String sql = "SELECT * FROM LoaiSanPham";
        Connection con = ConnectDB.getCon(); // Dùng kết nối static
        if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!");
            return ds;
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ds.add(rs.getString(1));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // =========================
    //  LẤY TOÀN BỘ SẢN PHẨM (Hàm Main.java cần)
    // =========================
    public List<SanPham> getAllSanPham() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";
        Connection con = ConnectDB.getCon(); // Dùng kết nối static
        if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!\n");
            return ds;
        }

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                SanPham sp = new SanPham(
                        rs.getString("maSP"),
                        rs.getString("tenSP"),
                        rs.getDouble("giaSP"),
                        rs.getString("moTaSP"),
                        rs.getString("hinhAnhSP"),
                        rs.getBoolean("tinhTrangSP"),
                        rs.getString("loaiSP")
                );
                ds.add(sp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    // =========================
    //  THÊM SẢN PHẨM
    // =========================
    public boolean insertSanPham(SanPham sp) {
        String sql = "INSERT INTO SanPham (maSP, tenSP, giaSP, moTaSP, hinhAnhSP, tinhTrangSP, loaiSP) VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getMaSP());
            ps.setString(2, sp.getTenSP());
            ps.setDouble(3, sp.getGiaSP());
            ps.setString(4, sp.getMoTaSP());
            ps.setString(5, sp.getHinhAnhSP());
            ps.setBoolean(6, sp.isTinhTrangSP());
            ps.setString(7, sp.getLoaiSP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  CẬP NHẬT SẢN PHẨM
    // =========================
    public boolean updateSanPham(SanPham sp) {
        String sql = "UPDATE SanPham SET tenSP=?, giaSP=?, moTaSP=?, hinhAnhSP=?, tinhTrangSP=?, loaiSP=? WHERE maSP=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sp.getTenSP());
            ps.setDouble(2, sp.getGiaSP());
            ps.setString(3, sp.getMoTaSP());
            ps.setString(4, sp.getHinhAnhSP());
            ps.setBoolean(5, sp.isTinhTrangSP());
            ps.setString(6, sp.getLoaiSP());
            ps.setString(7, sp.getMaSP());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  XÓA SẢN PHẨM
    // =========================
    public boolean deleteSanPham(String maSP) {
        String sql = "DELETE FROM SanPham WHERE maSP=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  TÌM THEO MÃ
    // =========================
    public SanPham findById(String maSP) {
        String sql = "SELECT * FROM SanPham WHERE maSP=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getDouble("giaSP"),
                            rs.getString("moTaSP"),
                            rs.getString("hinhAnhSP"),
                            rs.getBoolean("tinhTrangSP"),
                            rs.getString("loaiSP")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sản phẩm: " + e.getMessage());
        }
        return null;
    }

    // =========================
    //  TÌM THEO TÊN (có chứa) HOẶC MÃ (Hàm Live Search cần)
    // =========================
    public List<SanPham> searchByNameOrMa(String keyword, int limit) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM SanPham WHERE maSP LIKE ? OR tenSP LIKE ?";
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit); 
            String query = "%" + keyword + "%";
            ps.setString(2, query); 
            ps.setString(3, query); 

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getDouble("giaSP"),
                            rs.getString("moTaSP"),
                            rs.getString("hinhAnhSP"),
                            rs.getBoolean("tinhTrangSP"),
                            rs.getString("loaiSP")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sản phẩm theo tên/mã: " + e.getMessage());
        }
        return ds;
    }

    public List<SanPham> findByTen(String keyword) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT TOP 10 * FROM SanPham WHERE tenSP LIKE ?";
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getDouble("giaSP"),
                            rs.getString("moTaSP"),
                            rs.getString("hinhAnhSP"),
                            rs.getBoolean("tinhTrangSP"),
                            rs.getString("loaiSP")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sản phẩm theo tên: " + e.getMessage());
        }
        return ds;
    }
}