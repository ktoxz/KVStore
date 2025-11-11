package com.dao;

import com.connectDB.ConnectDB;
import com.entity.ChiTietKhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_ChiTietKhuyenMai {

    // Lấy toàn bộ chi tiết theo mã khuyến mãi
    public List<ChiTietKhuyenMai> findByMaKM(int maKM) throws SQLException {
        String sql = "SELECT maKM, maSP, tiLe, ngayApDung FROM ChiTietKhuyenMai WHERE maKM = ? ORDER BY maSP";
        List<ChiTietKhuyenMai> list = new ArrayList<>();
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietKhuyenMai(
                            rs.getInt("maKM"),
                            rs.getString("maSP"),
                            rs.getDouble("tiLe"),
                            rs.getDate("ngayApDung")
                    ));
                }
            }
        }
        return list;
    }

    // Lấy 1 bản ghi chi tiết
    public ChiTietKhuyenMai findOne(int maKM, String maSP) throws SQLException {
        String sql = "SELECT maKM, maSP, tiLe, ngayApDung FROM ChiTietKhuyenMai WHERE maKM=? AND maSP=?";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ChiTietKhuyenMai(
                            rs.getInt("maKM"),
                            rs.getString("maSP"),
                            rs.getDouble("tiLe"),
                            rs.getDate("ngayApDung")
                    );
                }
            }
        }
        return null;
    }

    // Thêm chi tiết
    public int insert(ChiTietKhuyenMai ct) throws SQLException {
        String sql = "INSERT INTO ChiTietKhuyenMai(maKM, maSP, tiLe, ngayApDung) VALUES(?,?,?,?)";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ct.getMaKM());
            ps.setString(2, ct.getMaSP());
            ps.setDouble(3, ct.getTiLe());
            ps.setDate(4, ct.getNgayApDung());
            return ps.executeUpdate();
        }
    }

    // Cập nhật chi tiết (khóa chính là maKM + maSP)
    public int update(ChiTietKhuyenMai ct) throws SQLException {
        String sql = "UPDATE ChiTietKhuyenMai SET tiLe=?, ngayApDung=? WHERE maKM=? AND maSP=?";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, ct.getTiLe());
            ps.setDate(2, ct.getNgayApDung());
            ps.setInt(3, ct.getMaKM());
            ps.setString(4, ct.getMaSP());
            return ps.executeUpdate();
        }
    }

    // Xóa 1 dòng chi tiết
    public int delete(int maKM, String maSP) throws SQLException {
        String sql = "DELETE FROM ChiTietKhuyenMai WHERE maKM=? AND maSP=?";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            return ps.executeUpdate();
        }
    }

    // Xóa toàn bộ chi tiết của 1 khuyến mãi (dùng khi xóa KM)
    public int deleteAllOf(int maKM) throws SQLException {
        String sql = "DELETE FROM ChiTietKhuyenMai WHERE maKM=?";
        try (Connection con = ConnectDB.getInstance().getCon();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            return ps.executeUpdate();
        }
    }
}
