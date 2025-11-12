package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import com.connectDB.ConnectDB;
import com.entity.KhachHang;

public class DAO_KhachHang {
	public ArrayList<KhachHang> getAllKhachHang() {
		ArrayList<KhachHang> dskh = new ArrayList<KhachHang>();
		try {
			ConnectDB.getInstance();
			Connection con = ConnectDB.getCon();
			String sql = "Select * from KhachHang";
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				String maKH = rs.getString(1);      // maKH
				String tenKH = rs.getString(2);     // tenKH
				boolean gioiTinh = rs.getBoolean(3); // gioiTinh
				String sdt = rs.getString(4);       // sdt
				LocalDate ngayTao = rs.getDate(5).toLocalDate(); // ngayTaoTK
				int diemTichLuy = rs.getInt(6);     // diemTichLuy
				KhachHang kh = new KhachHang(maKH, tenKH, gioiTinh, sdt, ngayTao, diemTichLuy);
				dskh.add(kh);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return dskh;
	}
	
	public String phatSinhMaKH() {
	    String maKH = "KH00000001";
	    try {
	        ConnectDB.getInstance();
	        Connection con = ConnectDB.getCon();
	        String sql = "SELECT MAX(CAST(SUBSTRING(maKH, 3, LEN(maKH)) AS INT)) FROM KhachHang";
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        if (rs.next()) {
	            int lastSo = rs.getInt(1);  // lấy phần số
	            lastSo++;
	            maKH = String.format("KH%08d", lastSo);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return maKH;
	}

	// Kiểm tra sđt bị trùng
	public boolean isTrungSoDienThoai(String sdt) {
	    try {
	    	ConnectDB.getInstance();
	        Connection con = ConnectDB.getCon();
	        String sql = "SELECT COUNT(*) FROM KhachHang WHERE sdt = ?";
	        PreparedStatement ps = con.prepareStatement(sql);
	        ps.setString(1, sdt);
	        ResultSet rs = ps.executeQuery();

	        if (rs.next()) {
	            int count = rs.getInt(1);
	            return count > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}

	
	public boolean themKH(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("INSERT INTO KhachHang (maKH, tenKH, gioiTinh, sdt, ngayTaoTK, diemTichLuy) VALUES (?, ?, ?, ?, ?, ?)");
			String maKH = phatSinhMaKH();
			stmt.setString(1, maKH);
			stmt.setString(2, kh.getTenKH());
			stmt.setBoolean(3, kh.isGioiTinh());
			stmt.setString(4, kh.getSdt());
			stmt.setDate(5, java.sql.Date.valueOf(kh.getNgayTao()));
			stmt.setInt(6, 0);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		
		return n>0;
	}
	
	public boolean capNhatKH(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("UPDATE KhachHang SET tenKH=?, gioiTinh=?, diemTichLuy=? WHERE maKH=?");
			stmt.setString(1, kh.getTenKH());
			stmt.setBoolean(2, kh.isGioiTinh());
			stmt.setInt(3, kh.getDiemTichLuy());
			stmt.setString(4, kh.getMaKH());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return n>0;
	}
	
	public KhachHang timKiemKH(String sdt) {
	    KhachHang kh = null;
	    ConnectDB.getInstance();
	    Connection con = ConnectDB.getCon();
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    try {
	        stmt = con.prepareStatement("SELECT * FROM KhachHang WHERE sdt = ?");
	        stmt.setString(1, sdt);
	        rs = stmt.executeQuery();
	        if (rs.next()) {
	            kh = new KhachHang(
	                rs.getString(1),          // maKH
	                rs.getString(2),          // tenKH
	                rs.getBoolean(3),         // gioiTinh
	                rs.getString(4),          // sdt
	                rs.getDate(5).toLocalDate(), // ngayTaoTK
	                rs.getInt(6)              // diemTichLuy
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (stmt != null) stmt.close();
	        } catch (SQLException e2) {
	            e2.printStackTrace();
	        }
	    }
	    return kh; 
	}
	
	/**
	 * Cập nhật điểm tích lũy cho khách hàng
	 * @param maKH Mã khách hàng
	 * @param diemMoi Số điểm tích lũy mới
	 * @return true nếu cập nhật thành công, false nếu thất bại
	 */
	public boolean capNhatDiemTichLuy(String maKH, int diemMoi) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("UPDATE KhachHang SET diemTichLuy=? WHERE maKH=?");
			stmt.setInt(1, diemMoi);
			stmt.setString(2, maKH);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return n > 0;
	}
	
	/**
	 * Thêm điểm tích lũy cho khách hàng dựa trên tổng tiền mua hàng.
	 * Quy tắc mới: 1.000 VNĐ = 1 điểm (1 điểm = 1 VNĐ khi dùng để giảm giá)
	 * @param maKH Mã khách hàng
	 * @param tongTien Tổng tiền dùng để quy đổi điểm (chưa VAT)
	 * @return true nếu cập nhật thành công, false nếu thất bại
	 */
	public boolean themDiemTichLuy(String maKH, double tongTien) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			int diemThem = (int)(tongTien / 1000); // bỏ phần lẻ dưới 1.000
			if (diemThem <= 0) return true; // không cộng gì nhưng cũng không lỗi
			stmt = con.prepareStatement("UPDATE KhachHang SET diemTichLuy = diemTichLuy + ? WHERE maKH = ?");
			stmt.setInt(1, diemThem);
			stmt.setString(2, maKH);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { if (stmt != null) stmt.close(); } catch (SQLException e2) { e2.printStackTrace(); }
		}
		return n > 0;
	}
	
	/**
	 * Trừ điểm tích lũy khi khách hàng sử dụng
	 * @param maKH Mã khách hàng
	 * @param diemTru Số điểm cần trừ
	 * @return true nếu trừ thành công, false nếu thất bại hoặc không đủ điểm
	 */
	public boolean truDiemTichLuy(String maKH, int diemTru) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("SELECT diemTichLuy FROM KhachHang WHERE maKH = ?");
			stmt.setString(1, maKH);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int diemHienTai = rs.getInt(1);
				if (diemHienTai >= diemTru) {
					stmt = con.prepareStatement("UPDATE KhachHang SET diemTichLuy = diemTichLuy - ? WHERE maKH = ?");
					stmt.setInt(1, diemTru);
					stmt.setString(2, maKH);
					n = stmt.executeUpdate();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try { if (stmt != null) stmt.close(); } catch (SQLException e2) { e2.printStackTrace(); }
		}
		return n > 0;
	}
}