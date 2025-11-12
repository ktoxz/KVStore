package com.dao;

import com.connectDB.ConnectDB;
import com.entity.CT_KhuyenMai;
import com.entity.KhuyenMai;
import com.entity.SanPham;
import com.enums.LoaiKM;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO_ChiTietKhuyenMai (Merged version)
 * Kết hợp chức năng đầy đủ từ DAO_ChiTietKhuyenMai và DAO_CT_KhuyenMai.
 * Hỗ trợ cả CRUD và truy vấn nâng cao (best promotion, product filter,...)
 */
public class DAO_CT_KhuyenMai {

    // ====================== TRUY VẤN CƠ BẢN ======================

    public List<CT_KhuyenMai> findByMaKM(int maKM) {
        List<CT_KhuyenMai> list = new ArrayList<>();
        String sql = """
            SELECT ct.maKM, ct.maSP, ct.tiLe, ct.loaiKM,
                   sp.tenSP, sp.giaSP,
                   km.tenKM
            FROM CT_KhuyenMai ct
            JOIN SanPham sp ON ct.maSP = sp.maSP
            JOIN KhuyenMai km ON ct.maKM = km.maKM
            WHERE ct.maKM = ?
            ORDER BY ct.maSP
            """;

        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return list;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhuyenMai km = new KhuyenMai();
                    km.setMaKM(rs.getInt("maKM"));
                    km.setTenKM(rs.getString("tenKM"));

                    SanPham sp = new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getDouble("giaSP")
                    );

                    CT_KhuyenMai ct = new CT_KhuyenMai(
                            km,
                            sp,
                            rs.getDouble("tiLe"),
                            LoaiKM.valueOf(rs.getString("loaiKM"))
                    );

                    list.add(ct);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CT_KhuyenMai findOne(int maKM, String maSP, String loaiKM) {
        String sql = """
            SELECT ct.maKM, ct.maSP, ct.tiLe, ct.loaiKM,
                   sp.tenSP, sp.giaSP,
                   km.tenKM
            FROM CT_KhuyenMai ct
            JOIN SanPham sp ON ct.maSP = sp.maSP
            JOIN KhuyenMai km ON ct.maKM = km.maKM
            WHERE ct.maKM = ? AND ct.maSP = ? AND ct.loaiKM = ?
            """;

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

                    SanPham sp = new SanPham(
                            rs.getString("maSP"),
                            rs.getString("tenSP"),
                            rs.getDouble("giaSP")
                    );

                    CT_KhuyenMai ct = new CT_KhuyenMai(
                            km,
                            sp,
                            rs.getDouble("tiLe"),
                            LoaiKM.valueOf(rs.getString("loaiKM"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ====================== CRUD ======================

    public boolean insert(CT_KhuyenMai ct) {
        String sql = "INSERT INTO CT_KhuyenMai(maKM, maSP, tiLe, loaiKM) VALUES(?,?,?,?)";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ct.getKhuyenMai().getMaKM());
            ps.setString(2, ct.getSanPham().getMaSP());
            ps.setDouble(3, ct.getGiaTri());
            ps.setString(4, ct.getLoaiKM().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(CT_KhuyenMai ct) {
        String sql = "UPDATE CT_KhuyenMai SET tiLe=? WHERE maKM=? AND maSP=? AND loaiKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, ct.getGiaTri());
            ps.setInt(2, ct.getKhuyenMai().getMaKM());
            ps.setString(3, ct.getSanPham().getMaSP());
            ps.setString(4, ct.getLoaiKM().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAllOf(int maKM) {
        String sql = "DELETE FROM CT_KhuyenMai WHERE maKM=?";
        Connection con = ConnectDB.getInstance().getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, maKM);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ====================== TRUY VẤN NÂNG CAO ======================

    public List<CT_KhuyenMai> findBySanPham(String maSP) {
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

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maSP);
            ps.setDate(2, Date.valueOf(today));
            ps.setDate(3, Date.valueOf(today));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    KhuyenMai km = new KhuyenMai(
                            rs.getInt("maKM"),
                            rs.getString("tenKM"),
                            rs.getString("moTaKM"),
                            rs.getDate("ngayBatDau"),
                            rs.getDate("ngayKetThuc")
                    );

                    String maSp = rs.getString("maSP");
                    SanPham sp = new DAO_SanPham().findById(maSp);
                    LoaiKM loaiKM = LoaiKM.valueOf(rs.getString("loaiKM"));

                    CT_KhuyenMai ctkm = new CT_KhuyenMai(km, sp, rs.getDouble("tiLe"), loaiKM);
                    ctkm.setLoaiKM(LoaiKM.valueOf(rs.getString("loaiKM")));
                    list.add(ctkm);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public CT_KhuyenMai findBestForProduct(String maSP) {
        List<CT_KhuyenMai> list = findBySanPham(maSP);
        if (list.isEmpty()) return null;
        return list.stream()
                .max((a, b) -> Double.compare(a.getGiaTri(), b.getGiaTri()))
                .orElse(null);
    }
}
