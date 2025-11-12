package com.dao;

import com.connectDB.ConnectDB;
import com.entity.ChiTietKhuyenMai;
import com.entity.KhuyenMai;
import com.entity.SanPham;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietKhuyenMai {

    // Đã xóa countByMaKM()
    
    // Đã xóa findByMaKM (phân trang)
    // Thay bằng findByMaKM (tải tất cả)
    public List<ChiTietKhuyenMai> findByMaKM(int maKM) {
        List<ChiTietKhuyenMai> list = new ArrayList<>();
        String sql = "SELECT " +
                "  ct.maKM, ct.maSP, ct.tiLe, ct.loaiKM, " + 
                "  sp.tenSP, sp.giaSP, " + 
                "  km.tenKM " +
                "FROM CT_KhuyenMai ct " + 
                "JOIN SanPham sp ON ct.maSP = sp.maSP " +
                "JOIN KhuyenMai km ON ct.maKM = km.maKM " +
                "WHERE ct.maKM = ? " +
                "ORDER BY ct.maSP ";

        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return list;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKM(rs.getInt("maKM"));
                    km.setTenKM(rs.getString("tenKM"));

                    SanPham sp = new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"));

                    ChiTietKhuyenMai ct = new ChiTietKhuyenMai(
                            km,
                            sp,
                            rs.getDouble("tiLe"),
                            rs.getString("loaiKM") 
                    );
                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
        return list; // Không đóng con
    }

    public ChiTietKhuyenMai findOne(int maKM, String maSP, String loaiKM) {
        String sql = "SELECT " +
                "  ct.maKM, ct.maSP, ct.tiLe, ct.loaiKM, " +
                "  sp.tenSP, sp.giaSP, " +
                "  km.tenKM " +
                "FROM CT_KhuyenMai ct " +
                "JOIN SanPham sp ON ct.maSP = sp.maSP " +
                "JOIN KhuyenMai km ON ct.maKM = km.maKM " +
                "WHERE ct.maKM = ? AND ct.maSP = ? AND ct.loaiKM = ?";
        
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            ps.setString(3, loaiKM);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKM(rs.getInt("maKM"));
                    km.setTenKM(rs.getString("tenKM"));
                    
                    SanPham sp = new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"));
                    
                    return new ChiTietKhuyenMai(
                            km,
                            sp,
                            rs.getDouble("tiLe"),
                            rs.getString("loaiKM")
                    );
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; // Không đóng con
    }
    
    public boolean insert(ChiTietKhuyenMai ct) {
        String sql = "INSERT INTO CT_KhuyenMai(maKM, maSP, tiLe, loaiKM) VALUES(?,?,?,?)";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ct.getKm().getMaKM());
            ps.setString(2, ct.getSp().getMaSP());
            ps.setDouble(3, ct.getTiLe());
            ps.setString(4, ct.getLoaiKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(ChiTietKhuyenMai ct) {
        String sql = "UPDATE CT_KhuyenMai SET tiLe=? WHERE maKM=? AND maSP=? AND loaiKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, ct.getTiLe());
            ps.setInt(2, ct.getKm().getMaKM());
            ps.setString(3, ct.getSp().getMaSP());
            ps.setString(4, ct.getLoaiKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int maKM, String maSP, String loaiKM) {
        String sql = "DELETE FROM CT_KhuyenMai WHERE maKM=? AND maSP=? AND loaiKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            ps.setString(3, loaiKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteAllOf(int maKM) {
        String sql = "DELETE FROM CT_KhuyenMai WHERE maKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}