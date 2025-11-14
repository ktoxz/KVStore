package com.enums;

public enum LoaiSP {
	DoAn,
	NuocUong,
	VatDungSinhHoat,
	DoDungVPPham,
	ThucPhamDongLanh;

    @Override
    public String toString() {
        // text hiển thị trên combobox / bảng
        return switch (this) {
            case DoAn -> "Đồ ăn";
            case NuocUong -> "Nước Uống";
            case VatDungSinhHoat -> "Vật dụng sinh hoạt";
            case DoDungVPPham -> "Đồ dùng văn phòng phẩm";
            case ThucPhamDongLanh -> "Thực phẩm đông lạnh";
        };
    }

    // map chuỗi từ DB -> enum (chấp nhận cả name() lẫn toString())
    public static LoaiSP fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (LoaiSP loai : values()) {
            if (loai.name().equalsIgnoreCase(x)
            		|| loai.toDbValue().equalsIgnoreCase(x)
            		|| loai.toString().equalsIgnoreCase(x)) {
            		
                return loai;
            }
        }
        return null;
    }

    // map enum -> chuỗi để lưu xuống DB
    public String toDbValue() {
    	return switch (this) {
	        case DoAn -> "DoAn";
	        case NuocUong -> "NuocUong";
	        case VatDungSinhHoat -> "VatDungSinhHoat";
	        case DoDungVPPham -> "DoDungVPPham";
	        case ThucPhamDongLanh -> "ThucPhamDongLanh";
	    };
    }
}
