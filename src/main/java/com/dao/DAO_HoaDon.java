package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.connectDB.ConnectDB;
import com.entity.*;

public class DAO_HoaDon {
    private DAO_SanPham daoSP;
    private DAO_KhachHang daoKH;
    private DAO_NhanVien daoNV;

    /**
     * Phát sinh mã hóa đơn tự động theo định dạng HDyyyyMMddxxxxx
     * @return Mã hóa đơn mới
     */

    public DAO_HoaDon() {
        daoSP = new DAO_SanPham();
        daoKH = new DAO_KhachHang();
        daoNV = new DAO_NhanVien();
    }

    public String phatSinhMaHoaDon() {
        String maHD = "";
        try {
            ConnectDB.getInstance();
            Connection con = ConnectDB.getCon();

            // Lấy ngày hiện tại
            LocalDate now = LocalDate.now();
            String datePrefix = String.format("HD%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());

            // Đếm số hóa đơn trong ngày
            String sql = "SELECT COUNT(*) FROM HoaDon WHERE maHoaDon LIKE '" + datePrefix + "%'";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            count++; // Tăng lên 1 cho hóa đơn mới

            // Tạo mã hóa đơn: HDyyyyMMdd + số thứ tự 5 chữ số
            maHD = datePrefix + String.format("%05d", count);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return maHD;
    }

    /**
     * Thêm hóa đơn mới vào database (bao gồm cả chi tiết hóa đơn)
     * @param hoaDon Đối tượng hóa đơn cần thêm
     * @return true nếu thêm thành công, false nếu thất bại
     */
    public boolean themHoaDon(HoaDon hoaDon) {
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        PreparedStatement stmtHD = null;
        PreparedStatement stmtCT = null;

        try {
            con.setAutoCommit(false);

            // 1. Insert HoaDon
            String sqlHD =
                    "INSERT INTO HoaDon (maHoaDon, ngayGiaoDich, tienKhach, thue, maKH, maNV) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
            stmtHD = con.prepareStatement(sqlHD);

            stmtHD.setString(1, hoaDon.getMaHoaDon());
            stmtHD.setDate(2, java.sql.Date.valueOf(hoaDon.getNgayGiaoDich()));
            stmtHD.setDouble(3, hoaDon.getTienKhach());
            stmtHD.setDouble(4, hoaDon.getThue());
            stmtHD.setString(5, hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getMaKH() : null);
            stmtHD.setString(6, hoaDon.getNhanVien().getMaNV());

            if (stmtHD.executeUpdate() <= 0) {
                con.rollback();
                return false;
            }

            // 2. Insert chi tiết hóa đơn (tính KM tại đây)
            String sqlCT =
                    "INSERT INTO CT_HoaDon (maHoaDon, maSP, soLuong, maKM, thanhTien) " +
                            "VALUES (?, ?, ?, ?, ?)";

            stmtCT = con.prepareStatement(sqlCT);

            DAO_CT_KhuyenMai daoCTKM = new DAO_CT_KhuyenMai();

            for (CT_HoaDon ct : hoaDon.getChiTietList()) {

                // tìm KM tối ưu
                CT_KhuyenMai ctkm = daoCTKM.findBestForProduct(ct.getSanPham().getMaSP());
//                KhuyenMai km = daoCTKM.findBestForProduct(ct.getSanPham().getMaSP());


                // tính thanhTien sau KM
                double goc = ct.getSanPham().getGiaSP() * ct.getSoLuong();
                double thanhTien = goc;

                if (ctkm != null) {
                    switch (ctkm.getLoaiKM()) {
                        case GiamGiaPhanTramSP -> thanhTien -= goc * ctkm.getGiaTri() / 100.0;
                        case GiamGiaTienSP -> thanhTien -= ctkm.getGiaTri();
                    }
                    ct.setKhuyenMai(ctkm.getKhuyenMai());
                }

                ct.setThanhTien(thanhTien);

                stmtCT.setString(1, hoaDon.getMaHoaDon());
                stmtCT.setString(2, ct.getSanPham().getMaSP());
                stmtCT.setInt(3, ct.getSoLuong());
                stmtCT.setObject(4, ctkm != null ? ctkm.getKhuyenMai().getMaKM() : null);
                stmtCT.setDouble(5, thanhTien);

                stmtCT.addBatch();
            }

            stmtCT.executeBatch();
            con.commit();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            try { con.rollback(); } catch (SQLException ignored) {}
            return false;

        } finally {
            try {
                if (stmtHD != null) stmtHD.close();
                if (stmtCT != null) stmtCT.close();
                con.setAutoCommit(true);
            } catch (Exception ignored) {}
        }
    }


    /**
     * Lấy danh sách hóa đơn trong một ngày (lọc theo nhân viên nếu maNV != null).
     * Đồng thời load luôn danh sách chi tiết cho từng hóa đơn.
     */
    public List<HoaDon> getHoaDonTrongNgay(LocalDate ngay, String maNV) {
        List<HoaDon> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();

        String sql = "SELECT maHoaDon, ngayGiaoDich, tienKhach, thue, maKH, maNV FROM HoaDon " +
                     "WHERE ngayGiaoDich = ?" +
                     (maNV != null ? " AND maNV = ?" : "");
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, java.sql.Date.valueOf(ngay));
            if (maNV != null) {
                stmt.setString(2, maNV);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String maHD = rs.getString("maHoaDon");
                LocalDate ngayGD = rs.getDate("ngayGiaoDich").toLocalDate();
                double tienKhach = rs.getDouble("tienKhach");
                double thue = rs.getDouble("thue");
                String maKH = rs.getString("maKH");
                String maNhanVien = rs.getString("maNV");

                HoaDon hd = new HoaDon(maHD, ngayGD, tienKhach, thue, null, null);

                if (maKH != null) {
                    KhachHang kh = new KhachHang(maKH);
                    hd.setKhachHang(kh);
                }
                if (maNhanVien != null) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(maNhanVien);
                    hd.setNhanVien(nv);
                }
                list.add(hd);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy hóa đơn bằng mã hóa đơn
     */
    private HoaDon getHoaDon(String maHoaDon) {
        HoaDon hd = null;
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        String sql = """
            SELECT maHoaDon, ngayGiaoDich, tienKhach, thue, maKH, maNV
            FROM HoaDon
            WHERE maHoaDon = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    String maHD = rs.getString("maHoaDon");
                    LocalDate ngayGD = rs.getDate("ngayGiaoDich").toLocalDate();
                    double tienKhach = rs.getDouble("tienKhach");
                    double thue = rs.getDouble("thue");
                    String maKH = rs.getString("maKH");
                    String maNV = rs.getString("maNV");

                    // Tạo đối tượng HoaDon theo constructor của UML
                    hd = new HoaDon(maHD, ngayGD, tienKhach, thue, new KhachHang(maKH), daoNV.getFirstNV());
                    // todo: sửa lại lấy nhân viên đúng theo mã nhân viên
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return hd;
    }



    /**
     * Load danh sách chi tiết cho một hóa đơn.
     */
    public List<CT_HoaDon> loadChiTietHoaDon(String maHoaDon) {
        List<CT_HoaDon> list = new ArrayList<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        String sql = "SELECT ct.maSP, ct.soLuong, ct.thanhTien " +
                        "FROM CT_HoaDon ct " +
                        "WHERE ct.maHoaDon = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maHoaDon);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String maSP = rs.getString("maSP");
                int soLuong = rs.getInt("soLuong");
                double thanhTien = rs.getDouble("thanhTien");
                SanPham sp = daoSP.findById(maSP);
                HoaDon hd = getHoaDon(maHoaDon);
                CT_HoaDon ct = new CT_HoaDon(hd, sp, soLuong, null, thanhTien);
                list.add(ct);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Tính tổng doanh thu (tổng thành tiền sau khuyến mãi) trong một ngày.
     * Sử dụng dữ liệu từ bảng HoaDon và CT_HoaDon.
     */
    public double getTongDoanhThuTrongNgay(LocalDate ngay, String maNV) {
        double tong = 0;
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SUM(ct.soLuong * sp.giaSP) AS tong ")
                .append("FROM HoaDon hd ")
                .append("JOIN CT_HoaDon ct ON hd.maHoaDon = ct.maHoaDon ")
                .append("JOIN SanPham sp ON ct.maSP = sp.maSP ")
                .append("WHERE CAST(hd.ngayGiaoDich AS date) = ?");

        if (maNV != null && !maNV.isBlank()) {
            sql.append(" AND hd.maNV = ?");
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));
            if (maNV != null && !maNV.isBlank()) {
                ps.setString(2, maNV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tong = rs.getDouble("tong");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tong;
    }


    /**
     * Lấy dữ liệu cho pie chart: tổng số lượng bán theo tên sản phẩm trong ngày.
     */
    public Map<String, Integer> getThongKeSanPhamTrongNgay(LocalDate ngay, String maNV) {
        Map<String, Integer> map = new HashMap<>();
        ConnectDB.getInstance();
        Connection con = ConnectDB.getCon();
        if (con == null) return map;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT sp.tenSP, SUM(ct.soLuong) AS soLuong ")
                .append("FROM HoaDon hd ")
                .append("JOIN CT_HoaDon ct ON hd.maHoaDon = ct.maHoaDon ")
                .append("JOIN SanPham sp ON ct.maSP = sp.maSP ")
                .append("WHERE CAST(hd.ngayGiaoDich AS date) = ?");

        if (maNV != null && !maNV.isBlank()) {
            sql.append(" AND hd.maNV = ?");
        }

        sql.append(" GROUP BY sp.tenSP");

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setDate(1, java.sql.Date.valueOf(ngay));

            if (maNV != null && !maNV.isBlank()) {
                ps.setString(2, maNV);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tenSP = rs.getString("tenSP");
                    int soLuong = rs.getInt("soLuong");
                    map.put(tenSP, soLuong);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

}
