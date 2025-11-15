package com.dao;

import com.connectDB.ConnectDB;
import com.entity.KhuyenMai;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DAO_KhuyenMai {

    public List<KhuyenMai> findAll() {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai ORDER BY maKM DESC";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return list;
        
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new KhuyenMai(
                        rs.getInt("maKM"),
                        rs.getString("tenKM"),
                        rs.getString("moTaKM"),
                        rs.getDate("ngayBatDau"),
                        rs.getDate("ngayKetThuc")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list; // Không đóng con
    }

    public List<KhuyenMai> search(String keyword) {
        List<KhuyenMai> list = new ArrayList<>();
        String sql = "SELECT * FROM KhuyenMai WHERE tenKM LIKE ? OR moTaKM LIKE ? ORDER BY maKM DESC";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return list;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String query = "%" + keyword + "%";
            ps.setString(1, query);
            ps.setString(2, query);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new KhuyenMai(
                            rs.getInt("maKM"),
                            rs.getString("tenKM"),
                            rs.getString("moTaKM"),
                            rs.getDate("ngayBatDau"),
                            rs.getDate("ngayKetThuc")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list; // Không đóng con
    }

    public KhuyenMai insert(KhuyenMai km) {
        String sql = "INSERT INTO KhuyenMai (tenKM, moTaKM, ngayBatDau, ngayKetThuc) VALUES (?, ?, ?, ?)";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return null;
        
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getMoTaKM());
            ps.setDate(3, km.getNgayBatDau());
            ps.setDate(4, km.getNgayKetThuc());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        km.setMaKM(rs.getInt(1)); 
                        return km; 
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; // Không đóng con
    }

    public boolean update(KhuyenMai km) {
        String sql = "UPDATE KhuyenMai SET tenKM=?, moTaKM=?, ngayBatDau=?, ngayKetThuc=? WHERE maKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getMoTaKM());
            ps.setDate(3, km.getNgayBatDau());
            ps.setDate(4, km.getNgayKetThuc());
            ps.setInt(5, km.getMaKM());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int maKM) {
        String sql = "DELETE FROM KhuyenMai WHERE maKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
}