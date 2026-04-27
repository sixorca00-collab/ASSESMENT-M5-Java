package org.hotelNova.utils;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import  org.hotelnova.config.AppConfig;

public class Helper {

    public static int update(String sql, Object... params) {
        try (Connection conn =AppConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, params);
            return ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, params);
            return ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> query(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> list = new ArrayList<>();

        try (Connection conn = org.hotelnova.config.AppConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParams(ps, params);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapper.apply(rs));
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public static <T> T queryOne(String sql, Function<ResultSet, T> mapper, Object... params) {
        List<T> list = query(sql, mapper, params);
        return list.isEmpty() ? null : list.get(0);
    }

    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}