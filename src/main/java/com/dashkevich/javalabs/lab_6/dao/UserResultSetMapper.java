package com.dashkevich.javalabs.lab_6.dao;

import com.dashkevich.javalabs.lab_6.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserResultSetMapper {

    public List<User> mapUsers(ResultSet rs) throws SQLException {
        List<User> result = new ArrayList<>();
        while (rs.next()) {
            result.add(mapUser(rs));
        }
        return result;
    }

    public User mapUser(ResultSet rs) throws SQLException {
        return new User(rs.getInt(1), rs.getString(2), rs.getDate(3).toLocalDate());
    }
}
