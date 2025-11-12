package com.dao;

import com.connectDB.ConnectDB;
import com.entity.CT_KhuyenMai;
import com.entity.KhuyenMai;
import com.enums.LoaiKM;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DAO_CT_KhuyenMai {

    // ====================== LẤY DANH SÁCH ======================

    // Lấy toàn bộ chi tiết theo mã khuyến mãi
    public List<CT_KhuyenMai> findByMaKM(int maKM) throws SQLException {
        String sql = "SELECT maKM, maSP, tiLe, loaiKM FROM CT_KhuyenMai WHERE maKM = ? ORDER BY maSP";
        List<CT_KhuyenMai> list = new ArrayList<>();

        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, maKM);
            rs = ps.executeQuery();

            while (rs.next()) {
                CT_KhuyenMai ct = new CT_KhuyenMai();
                ct.setKhuyenMai(new KhuyenMai(maKM));
                ct.setSanPham(null); // sẽ set sau nếu cần
                ct.setGiaTri(rs.getDouble("tiLe"));
                ct.setLoaiKM(LoaiKM.valueOf(rs.getString("loaiKM")));
                list.add(ct);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }

        return list;
    }

    // Lấy 1 bản ghi chi tiết
    public CT_KhuyenMai findOne(int maKM, String maSP) throws SQLException {
        String sql = "SELECT maKM, maSP, tiLe, loaiKM FROM CT_KhuyenMai WHERE maKM=? AND maSP=?";
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            rs = ps.executeQuery();

            if (rs.next()) {
                CT_KhuyenMai ct = new CT_KhuyenMai();
                ct.setKhuyenMai(new KhuyenMai(maKM));
                ct.setGiaTri(rs.getDouble("tiLe"));
                ct.setLoaiKM(LoaiKM.valueOf(rs.getString("loaiKM")));
                return ct;
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }

        return null;
    }

    // ====================== THÊM / SỬA / XÓA ======================

    public int insert(CT_KhuyenMai ct) throws SQLException {
        String sql = "INSERT INTO CT_KhuyenMai(maKM, maSP, tiLe, loaiKM) VALUES(?,?,?,?)";
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, ct.getKhuyenMai().getMaKM());
            ps.setString(2, ct.getSanPham().getMaSP());
            ps.setDouble(3, ct.getGiaTri());
            ps.setString(4, ct.getLoaiKM().name());
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }
    }

    public int update(CT_KhuyenMai ct) throws SQLException {
        String sql = "UPDATE CT_KhuyenMai SET tiLe=?, loaiKM=? WHERE maKM=? AND maSP=?";
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setDouble(1, ct.getGiaTri());
            ps.setString(2, ct.getLoaiKM().name());
            ps.setInt(3, ct.getKhuyenMai().getMaKM());
            ps.setString(4, ct.getSanPham().getMaSP());
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }
    }

    public int delete(int maKM, String maSP) throws SQLException {
        String sql = "DELETE FROM CT_KhuyenMai WHERE maKM=? AND maSP=?";
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, maKM);
            ps.setString(2, maSP);
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }
    }

    public int deleteAllOf(int maKM) throws SQLException {
        String sql = "DELETE FROM CT_KhuyenMai WHERE maKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setInt(1, maKM);
            return ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }
    }

    // ====================== TRUY VẤN ĐẶC BIỆT ======================

    public List<CT_KhuyenMai> findBySanPham(String maSP) throws SQLException {
        String sql = """
            SELECT ctkm.maKM, ctkm.maSP, ctkm.tiLe, ctkm.loaiKM,
                   km.tenKM, km.moTaKM, km.ngayBatDau, km.ngayKetThuc
            FROM CT_KhuyenMai ctkm
            JOIN KhuyenMai km ON ctkm.maKM = km.maKM
            WHERE ctkm.maSP = ? 
              AND km.ngayBatDau <= ? 
              AND km.ngayKetThuc >= ?
            """;

        List<CT_KhuyenMai> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Connection con = ConnectDB.getInstance().getCon();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = con.prepareStatement(sql);
            ps.setString(1, maSP);
            ps.setDate(2, Date.valueOf(today));
            ps.setDate(3, Date.valueOf(today));
            rs = ps.executeQuery();

            while (rs.next()) {
                KhuyenMai km = new KhuyenMai(
                        rs.getInt("maKM"),
                        rs.getString("tenKM"),
                        rs.getString("moTaKM"),
                        rs.getDate("ngayBatDau"),
                        rs.getDate("ngayKetThuc")
                );

                CT_KhuyenMai ctkm = new CT_KhuyenMai(km, null, rs.getDouble("tiLe"));
                ctkm.setLoaiKM(LoaiKM.valueOf(rs.getString("loaiKM")));
                list.add(ctkm);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignored) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        }

        return list;
    }

    public CT_KhuyenMai findBestForProduct(String maSP) throws SQLException {
        List<CT_KhuyenMai> list = findBySanPham(maSP);
        if (list.isEmpty()) return null;
        return list.stream()
                .max((a, b) -> Double.compare(a.getGiaTri(), b.getGiaTri()))
                .orElse(null);
    }
}
