package com.rzodeczko.domain.vo;

import java.io.Serializable;

public record Position(
        Integer rowNo,
        Integer colNo
) implements Serializable {

    public Position() {
        this(null, null);
    }

    public Position(String value) {
        this(parseRowNo(value), parseColNo(value));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Integer getRowNo() { return rowNo; }
    public Position setRowNo(Integer rowNo) { return new Position(rowNo, colNo); }
    public Integer getColNo() { return colNo; }
    public Position setColNo(Integer colNo) { return new Position(rowNo, colNo); }

    private static Integer parseRowNo(String value) {
        String[] array = value.split(", ");
        return array.length == 2 ? Integer.parseInt(array[0].split(": ")[1]) : null;
    }

    private static Integer parseColNo(String value) {
        String[] array = value.split(", ");
        return array.length == 2 ? Integer.parseInt(array[1].split(": ")[1]) : null;
    }

    @Override
    public String toString() {
        return "rowNo: " + rowNo + ", colNo: " + colNo;
    }

    public static class Builder {
        private Integer rowNo;
        private Integer colNo;

        public Builder rowNo(Integer rowNo) { this.rowNo = rowNo; return this; }
        public Builder colNo(Integer colNo) { this.colNo = colNo; return this; }
        public Position build() { return new Position(rowNo, colNo); }
    }
}
