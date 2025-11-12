package com.enums;

public enum LoaiKM {
    GiamGiaPhanTramSP,    // Giảm theo phần trăm (VD: -10%)
    GiamGiaTienSP;         // Giảm theo số tiền cố định (VD: -5000đ)

    @Override
    public String toString() {
        return switch (this) {
            case GiamGiaPhanTramSP -> "Giảm giá phần trăm sản phẩm";
            case GiamGiaTienSP -> "Giảm giá tiền sản phẩm";
        };
    }
}
