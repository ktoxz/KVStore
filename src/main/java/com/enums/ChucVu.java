package com.enums;

public enum ChucVu {
    QUANLY,
    THUNGAN;

    @Override
    public String toString() {
        // text hiển thị trên combobox / bảng
        return switch (this) {
            case QUANLY -> "Quản lý";
            case THUNGAN -> "Thu ngân";
        };
    }

    // map chuỗi bất kỳ (từ DB hoặc text hiển thị) -> enum
    public static ChucVu fromAny(String s) {
        if (s == null) return null;
        String x = s.trim();
        for (ChucVu cv : values()) {
            if (cv.name().equalsIgnoreCase(x)
                    || cv.toDbValue().equalsIgnoreCase(x)
                    || cv.toString().equalsIgnoreCase(x)) {
                return cv;
            }
        }
        return null;
    }

    // map enum -> chuỗi lưu xuống DB (bảng ChucVu)
    public String toDbValue() {
        return switch (this) {
            case QUANLY -> "QUANLY";
            case THUNGAN -> "THUNGAN";
        };
    }
}
