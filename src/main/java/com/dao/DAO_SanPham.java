package com.dao;

import com.entity.SanPham;
import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_SanPham {

    // =========================
    //  LẤY TOÀN BỘ SẢN PHẨM
    // =========================
    public List<SanPham> getAllSanPham() {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham";
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql);
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
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
    //  TÌM THEO LOẠI
    // =========================
    public List<SanPham> findByLoai(String loaiSP) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE loaiSP = ?";
        try (Connection con = ConnectDB.getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, loaiSP);
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
            System.err.println("Lỗi khi tìm sản phẩm theo loại: " + e.getMessage());
        }
        return ds;
    }

    // =========================
    //  TÌM THEO TÊN (có chứa)
    // =========================
    public List<SanPham> findByTen(String keyword) {
        List<SanPham> ds = new ArrayList<>();
        String sql = "SELECT * FROM SanPham WHERE tenSP LIKE ?";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {

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
