package com.dao;

import com.connectDB.ConnectDB;
import com.entity.KhuyenMai;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO_KhuyenMai {

    // Lấy connection đang mở từ ConnectDB (KHÔNG để trong try-with-resources)
    private Connection con() throws SQLException {
        return ConnectDB.getInstance().getCon(); // getCon() nên tự reconnect nếu bị closed
    }

    // ======= Loại khuyến mãi =======
    public List<String> findAllLoaiKM() throws SQLException {
        String sql = "SELECT loaiKM FROM LoaiKM ORDER BY loaiKM";
        List<String> rs = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) rs.add(r.getString(1));
        }
        return rs;
    }

    // ======= Khuyến mãi =======
    public List<KhuyenMai> findAll() throws SQLException {
        String sql = """
            SELECT maKM, tenKM, moTaKM, ngayBatDau, ngayKetThuc, loaiKM
            FROM KhuyenMai
            ORDER BY maKM
        """;
        List<KhuyenMai> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql);
             ResultSet r = ps.executeQuery()) {
            while (r.next()) {
                list.add(mapRow(r));
            }
        }
        return list;
    }

    public List<KhuyenMai> search(String kw) throws SQLException {
        String k = "%" + (kw == null ? "" : kw.trim()) + "%";
        String sql = """
            SELECT maKM, tenKM, moTaKM, ngayBatDau, ngayKetThuc, loaiKM
            FROM KhuyenMai
            WHERE CAST(maKM AS NVARCHAR) LIKE ?
               OR tenKM LIKE ?
               OR moTaKM LIKE ?
               OR loaiKM LIKE ?
            ORDER BY maKM
        """;
        List<KhuyenMai> list = new ArrayList<>();
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ps.setString(4, k);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) list.add(mapRow(r));
            }
        }
        return list;
    }

    public KhuyenMai findById(int maKM) throws SQLException {
        String sql = """
            SELECT maKM, tenKM, moTaKM, ngayBatDau, ngayKetThuc, loaiKM
            FROM KhuyenMai
            WHERE maKM = ?
        """;
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, maKM);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? mapRow(r) : null;
            }
        }
    }

    public int insert(KhuyenMai km) throws SQLException {
        String sql = """
            INSERT INTO KhuyenMai(tenKM, moTaKM, ngayBatDau, ngayKetThuc, loaiKM)
            OUTPUT INSERTED.maKM
            VALUES(?,?,?,?,?)
        """;
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getMoTaKM());
            ps.setDate(3, km.getNgayBatDau());
            ps.setDate(4, km.getNgayKetThuc());
            ps.setString(5, km.getLoaiKM());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public int update(KhuyenMai km) throws SQLException {
        String sql = """
            UPDATE KhuyenMai
               SET tenKM = ?, moTaKM = ?, ngayBatDau = ?, ngayKetThuc = ?, loaiKM = ?
             WHERE maKM = ?
        """;
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setString(1, km.getTenKM());
            ps.setString(2, km.getMoTaKM());
            ps.setDate(3, km.getNgayBatDau());
            ps.setDate(4, km.getNgayKetThuc());
            ps.setString(5, km.getLoaiKM());
            ps.setInt(6, km.getMaKM());
            return ps.executeUpdate();
        }
    }

    public int delete(int maKM) throws SQLException {
        String sql = "DELETE FROM KhuyenMai WHERE maKM = ?";
        try (PreparedStatement ps = con().prepareStatement(sql)) {
            ps.setInt(1, maKM);
            return ps.executeUpdate();
        }
    }

    // ======= Helper =======
    private KhuyenMai mapRow(ResultSet r) throws SQLException {
        return new KhuyenMai(
            r.getInt("maKM"),
            r.getString("tenKM"),
            r.getString("moTaKM"),
            r.getDate("ngayBatDau"),
            r.getDate("ngayKetThuc"),
            r.getString("loaiKM")
        );
    }
}
