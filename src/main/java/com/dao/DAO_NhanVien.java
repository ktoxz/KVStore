package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import com.connectDB.ConnectDB;
import com.entity.NhanVien;
import com.enums.ChucVu;

public class DAO_NhanVien {
    /**
     * Lấy mã nhân viên đầu tiên trong database
     * @return Mã nhân viên hoặc null nếu không có
     */
    public NhanVien getFirstNV() {
    	
    	NhanVien nv = new NhanVien();
    	String sql = "SELECT TOP 1 * FROM NhanVien";
    	Connection con = ConnectDB.getCon();
    	if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!");
            return nv;
        }
    	
        try (
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
    		){
            

            if (rs.next()) {
                String maNV = rs.getString(1);
                String tenNV = rs.getString(2);
                boolean gioiTinh = rs.getBoolean(4);
                String email = rs.getString(5);
                String sdt = rs.getString(6);
                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                String chucVu = rs.getString(8);
                nv = new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan,chucVu);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nv;
    }
    
    
    public List<NhanVien> getAllNV() {
    	List<NhanVien> ls = new ArrayList<NhanVien>();
    	String sql = "SELECT * FROM NhanVien";
    	Connection con = ConnectDB.getCon();
    	
    	if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!");
            return ls;
        }
    	
        try (
        	Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        	){
           

            while(rs.next()) {
                String maNV = rs.getString(1);
                String tenNV = rs.getString(2);
                boolean gioiTinh = rs.getBoolean(4);
                String email = rs.getString(5);
                String sdt = rs.getString(6);
                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                String chucVu = rs.getString(8);
                NhanVien nv = new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan,chucVu);
                ls.add(nv);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ls;
    }
    
	// =========================
	//  ĐẾM NHÂN VIÊN (TÌM THEO NHIỀU TRƯỜNG)
	// =========================
    public int countNhanVien(String keyword) {
	    Connection con = ConnectDB.getCon();
	    if (con == null) return 0;
	
	    boolean hasKw = keyword != null && !keyword.trim().isEmpty();
	    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM NhanVien");
	    if (hasKw) {
	    	sql.append(" WHERE (maNV LIKE ?")
	    	   .append(" OR tenNV LIKE ?")
	    	   .append(" OR sdt LIKE ?")
	    	   .append(" OR email LIKE ?")
	    	   .append(" OR CONVERT(varchar(10), ngayTaoTaiKhoan, 120) LIKE ?")
	    	   .append(" OR chucVu LIKE ?)");
	    }
	
	    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
	        if (hasKw) {
	        	String kw = "%" + keyword.trim() + "%";
	        	int idx = 1;
	        	ps.setString(idx++, kw); // maNV
	        	ps.setString(idx++, kw); // tenNV
	        	ps.setString(idx++, kw); // sdt
	        	ps.setString(idx++, kw); // email
	        	ps.setString(idx++, kw); // ngayTaoTaiKhoan
	        	ps.setString(idx++, kw); // chucVu
	        }
	        try (ResultSet rs = ps.executeQuery()) {
	            if (rs.next()) return rs.getInt(1);
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi đếm nhân viên: " + e.getMessage());
	    }
	    return 0;
	}


	// =========================
	//  LẤY 1 TRANG NHÂN VIÊN (PHÂN TRANG + TÌM THEO NHIỀU TRƯỜNG)
	// =========================
	public List<NhanVien> getNhanVienPage(String keyword, int page, int pageSize) {
	    List<NhanVien> ds = new ArrayList<>();
	    Connection con = ConnectDB.getCon();
	    if (con == null) return ds;
	
	    boolean hasKw = keyword != null && !keyword.trim().isEmpty();
	    StringBuilder sql = new StringBuilder("SELECT * FROM NhanVien");
	    if (hasKw) {
	    	sql.append(" WHERE (maNV LIKE ?")
	    	   .append(" OR tenNV LIKE ?")
	    	   .append(" OR sdt LIKE ?")
	    	   .append(" OR email LIKE ?")
	    	   .append(" OR CONVERT(varchar(10), ngayTaoTaiKhoan, 120) LIKE ?")
	    	   .append(" OR chucVu LIKE ?)");
	    }
	    sql.append(" ORDER BY maNV OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
	
	    if (pageSize <= 0) pageSize = 10;
	    if (page <= 0) page = 1;
	    int offset = (page - 1) * pageSize;
	
	    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
	        int idx = 1;
	        if (hasKw) {
	        	String kw = "%" + keyword.trim() + "%";
	        	ps.setString(idx++, kw); // maNV
	        	ps.setString(idx++, kw); // tenNV
	        	ps.setString(idx++, kw); // sdt
	        	ps.setString(idx++, kw); // email
	        	ps.setString(idx++, kw); // ngayTaoTaiKhoan
	        	ps.setString(idx++, kw); // chucVu
	        }
	        ps.setInt(idx++, offset);
	        ps.setInt(idx, pageSize);
	
	        try (ResultSet rs = ps.executeQuery()) {
	            while (rs.next()) {
	                String maNV = rs.getString(1);
	                String tenNV = rs.getString(2);
	                boolean gioiTinh = rs.getBoolean(4);
	                String email = rs.getString(5);
	                String sdt = rs.getString(6);
	                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
	                String chucVu = ChucVu.fromAny(rs.getString(8)).toString();
	
	                ds.add(new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan, chucVu));
	            }
	        }
	    } catch (SQLException e) {
	        System.err.println("Lỗi khi lấy trang nhân viên: " + e.getMessage());
	    }
	    return ds;
	}

    
    // =========================
    //  SINH MÃ NHÂN VIÊN MỚI
    // =========================
    public String getNextMaNhanVien() {
        Connection con = ConnectDB.getCon();
        if (con == null) {
            return "NV001"; // fallback khi chưa kết nối được DB
        }

        String sql = "SELECT MAX(maNV) AS maxMa FROM NhanVien WHERE maNV LIKE 'NV%'";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String maxMa = rs.getString("maxMa");

                // chưa có nhân viên nào dạng NV...
                if (maxMa == null || maxMa.trim().isEmpty()) {
                    return "NV001";
                }

                maxMa = maxMa.trim();
                String prefix = "NV";
                String numPart = maxMa.startsWith(prefix)
                        ? maxMa.substring(prefix.length())
                        : maxMa; // lỡ ai nhập tay không có NV

                int nextNum;
                try {
                    nextNum = Integer.parseInt(numPart) + 1;
                } catch (NumberFormatException e) {
                    // nếu phần số lỗi thì quay về 1 cho an toàn
                    nextNum = 1;
                }

                // giữ nguyên số lượng chữ số như mã lớn nhất hiện tại (VD: NV001 → 3 số)
                int width = numPart.length() > 0 ? numPart.length() : 3;
                String fmt = "%0" + width + "d";

                return prefix + String.format(fmt, nextNum);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getNextMaNhanVien: " + e.getMessage());
        }

        return "NV001";
    }


    // =========================
    //  THÊM NHÂN VIÊN
    // =========================
    public boolean insertNhanVien(NhanVien nv) {
        // bỏ cột matKhau, cho DB tự nhận NULL (hoặc xử lý ở chỗ khác)
        String sql = "INSERT INTO NhanVien (maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan, chucVu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getMaNV());
            ps.setString(2, nv.getTenNV());
            ps.setBoolean(3, nv.isGioiTinh());
            ps.setString(4, nv.getEmail());
            ps.setString(5, nv.getSdt());

            LocalDate d = nv.getNgayTaoTaiKhoan();
            if (d == null) d = LocalDate.now();
            ps.setDate(6, java.sql.Date.valueOf(d));

            ps.setString(7, nv.getChucVu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhân viên: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  CẬP NHẬT NHÂN VIÊN
    // =========================
    public boolean updateNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET tenNV=?, gioiTinh=?, email=?, sdt=?, ngayTaoTaiKhoan=?, chucVu=? "
                   + "WHERE maNV=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nv.getTenNV());
            ps.setBoolean(2, nv.isGioiTinh());
            ps.setString(3, nv.getEmail());
            ps.setString(4, nv.getSdt());

            LocalDate d = nv.getNgayTaoTaiKhoan();
            if (d == null) d = LocalDate.now();
            ps.setDate(5, java.sql.Date.valueOf(d));

            ps.setString(6, nv.getChucVu());
            ps.setString(7, nv.getMaNV());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nhân viên: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  XÓA NHÂN VIÊN
    // =========================
    public boolean deleteNhanVien(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE maNV=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nhân viên: " + e.getMessage());
        }
        return false;
    }

    // =========================
    //  TÌM NHÂN VIÊN THEO MÃ
    // =========================
    public NhanVien findById(String maNV) {
        String sql = "SELECT * FROM NhanVien WHERE maNV=?";
        Connection con = ConnectDB.getCon();
        if (con == null) return null;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String tenNV = rs.getString(2);
                    boolean gioiTinh = rs.getBoolean(4);
                    String email = rs.getString(5);
                    String sdt = rs.getString(6);
                    LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                    String chucVu = rs.getString(8);
                    return new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan, chucVu);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm nhân viên: " + e.getMessage());
        }
        return null;
    }

}
