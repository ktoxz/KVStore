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
        String sql = "SELECT * FROM SanPham ORDER BY maSP";
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
    //  ĐẾM SẢN PHẨM (CÓ ÁP DỤNG TỪ KHÓA TÌM KIẾM)
    // =========================
    public int countSanPham(String keyword) {
        Connection con = ConnectDB.getCon();
        if (con == null) return 0;

        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM SanPham");
        if (hasKw) {
            sql.append(" WHERE maSP LIKE ?")
               .append(" OR tenSP LIKE ?")
               .append(" OR moTaSP LIKE ?")
               .append(" OR loaiSP LIKE ?")
               .append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
               .append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?");
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(1, kw);
                ps.setString(2, kw);
                ps.setString(3, kw);
                ps.setString(4, kw);
                ps.setString(5, kw);
                ps.setString(6, kw);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm sản phẩm: " + e.getMessage());
        }
        return 0;
    }

    // =========================
    //  LẤY 1 TRANG SẢN PHẨM (PHÂN TRANG + TÌM KIẾM)
    // =========================
    public List<SanPham> getSanPhamPage(String keyword, int page, int pageSize) {
        List<SanPham> ds = new ArrayList<>();
        Connection con = ConnectDB.getCon();
        if (con == null) return ds;

        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        StringBuilder sql = new StringBuilder("SELECT * FROM SanPham");
        if (hasKw) {
            sql.append(" WHERE maSP LIKE ?")
               .append(" OR tenSP LIKE ?")
               .append(" OR moTaSP LIKE ?")
               .append(" OR loaiSP LIKE ?")
               .append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
               .append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?");
        }
        sql.append(" ORDER BY maSP OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        int offset = (page - 1) * pageSize;
        if (offset < 0) offset = 0;

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            ps.setInt(idx++, offset);
            ps.setInt(idx, pageSize);

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
            System.err.println("Lỗi khi lấy trang sản phẩm: " + e.getMessage());
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
    

    // =========================
    //  TÍNH VỊ TRÍ (INDEX 0-BASED) CỦA 1 MÃ TRONG DANH SÁCH ĐÃ LỌC
    // =========================
    public int getIndexById(String keyword, String maSP) {
        Connection con = ConnectDB.getCon();
        if (con == null || maSP == null || maSP.trim().isEmpty()) return -1;

        boolean hasKw = keyword != null && !keyword.trim().isEmpty();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM SanPham");

        if (hasKw) {
            sql.append(" WHERE (")
               .append("maSP LIKE ?")
               .append(" OR tenSP LIKE ?")
               .append(" OR moTaSP LIKE ?")
               .append(" OR loaiSP LIKE ?")
               .append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
               .append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?")
               .append(") AND maSP <= ?");
        } else {
            sql.append(" WHERE maSP <= ?");
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasKw) {
                String kw = "%" + keyword.trim() + "%";
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
                ps.setString(idx++, kw);
            }
            ps.setString(idx, maSP);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt(1);
                    if (cnt <= 0) return -1;   // không có trong danh sách đang lọc
                    return cnt - 1;            // index 0-based
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính index sản phẩm: " + e.getMessage());
        }
        return -1;
    }
    
    // =========================
    //  SINH MÃ MỚI KHÔNG CẦN LOAD HẾT BẢNG
    // =========================
    public String getNextIdSanPham() {
        Connection con = ConnectDB.getCon();
        if (con == null) return "SP001";

        String sql = "SELECT MAX(maSP) FROM SanPham WHERE maSP LIKE 'SP%'";
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                String maxId = rs.getString(1);
                if (maxId != null && maxId.matches("SP\\d+")) {
                    int n = Integer.parseInt(maxId.replaceAll("\\D+", ""));
                    return String.format("SP%03d", n + 1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi sinh mã sản phẩm mới: " + e.getMessage());
        }
        // nếu bảng đang rỗng hoặc lỗi thì quay về SP001
        return "SP001";
    }

    // =========================
    //  SINH MÃ SẢN PHẨM TIẾP THEO DẠNG SPxxx
    // =========================
    public String getNextMaSanPham() {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return "SP001"; // fallback khi chưa kết nối được DB
        }

        String sql = "SELECT MAX(maSP) AS maxMa FROM SanPham WHERE maSP LIKE 'SP%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String maxMa = rs.getString("maxMa");

                // Chưa có sản phẩm nào dạng SP... trong DB
                if (maxMa == null || maxMa.trim().isEmpty()) {
                    return "SP001";
                }

                maxMa = maxMa.trim();
                String prefix = "SP";
                String numPart = maxMa.startsWith(prefix)
                        ? maxMa.substring(prefix.length())
                        : maxMa; // lỡ ai nhập tay không có SP

                int nextNum;
                try {
                    nextNum = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException e) {
                    // Nếu phần số bị lỗi, quay về 1 cho an toàn
                    nextNum = 1;
                }

                // Giữ nguyên số lượng chữ số như mã lớn nhất hiện tại (ví dụ SP001 → 3 số)
                int width = numPart.length() > 0 ? numPart.length() : 3;
                String fmt = "%0" + width + "d";

                return prefix + String.format(fmt, nextNum);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getNextMaSanPham: " + e.getMessage());
        }

        // Nếu có lỗi gì đó thì trả về SP001
        return "SP001";
    }

    
}