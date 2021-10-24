package com.brennaswitzer.cookbook.util;

import lombok.SneakyThrows;
import lombok.val;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

public final class ResultSetPrinter {
    @SneakyThrows
    public static void printResultSet(ResultSet resultSet) {
        ResultSetMetaData md = resultSet.getMetaData();
        val cc = md.getColumnCount();
        val headers = new ArrayList<String>(cc);
        for (int i = 1; i <= cc; i++) {
            headers.add(md.getColumnName(i));
        }
        val rows = new ArrayList<List<String>>();
        rows.add(headers);
        while (!resultSet.isAfterLast()) {
            val row = new ArrayList<String>(cc);
            for (int i = 1; i <= cc; i++) {
                val v = resultSet.getObject(i);
                row.add(v == null ? "" : v.toString());
            }
            rows.add(row);
            resultSet.next();
        }
        val lengths = new int[cc];
        for (val r : rows) {
            int i = 0;
            for (val c : r) {
                lengths[i] = Math.max(lengths[i], c.length());
                i += 1;
            }
        }
        for (val r : rows) {
            int i = 0;
            for (val c : r) {
                System.out.print(" " + padEnd(c, lengths[i]) + " ");
                i += 1;
            }
            System.out.println();
        }
    }

    private static String padEnd(String s, int length) {
        if (s.length() == length) return s;
        val sb = new StringBuilder(length);
        sb.append(s);
        while (sb.length() < length) sb.append(' ');
        return sb.toString();
    }
}
