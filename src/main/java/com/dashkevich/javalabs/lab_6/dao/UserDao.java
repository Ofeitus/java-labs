package com.dashkevich.javalabs.lab_6.dao;

import com.dashkevich.javalabs.lab_6.model.User;
import com.dashkevich.javalabs.lab_6.projection.UserLettersStatProjection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDao {
    private final Logger logger = LogManager.getLogger(UserDao.class);
    private final boolean showSql;
    private final UserResultSetMapper userResultSetMapper;
    private final Connection con;

    public UserDao(Connection con, boolean showSql) {
        this.showSql = showSql;
        this.con = con;
        userResultSetMapper = new UserResultSetMapper();
    }

    public List<User> getAll() throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM user;");
        if (showSql) {
            logger.info(ps.toString());
        }
        return userResultSetMapper.mapUsers(ps.executeQuery());
    }

    public int saveAll(List<User> users) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `letters`.`user` (`id`, `fio`, `date_of_birth`) VALUES");
        sb.append("\n(?, ?, ?),".repeat(users.size()));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");

        PreparedStatement ps = con.prepareStatement(sb.toString());
        for (int i = 0; i < users.size(); i++) {
            ps.setObject(i * 3 + 1, users.get(i).id());
            ps.setObject(i * 3 + 2, users.get(i).fio());
            ps.setObject(i * 3 + 3, users.get(i).dateOfBirth());
        }
        if (showSql) {
            logger.info(ps.toString());
        }
        return ps.executeUpdate();
    }

    public int deleteAll() throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM user");
        return ps.executeUpdate();
    }

    /**
     * Пользователь, длина писем которого наименьшая
     */
    public User getShortestLettersUser() throws SQLException {
        PreparedStatement ps = con.prepareStatement("""
                    SELECT u.*
                    FROM user u
                    JOIN letter l ON u.id = l.sender
                    GROUP BY u.id
                    ORDER BY sum(char_length(l.body))
                    LIMIT 1;"""
        );
        if (showSql) {
            logger.info(ps.toString());
        }
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return userResultSetMapper.mapUser(rs);
        } else {
            return null;
        }
    }

    /**
     * Информация о пользователях, а также количестве
     * полученных и отправленных ими письмах
     */
    public List<UserLettersStatProjection> getUsersLettersStats() throws SQLException {
        PreparedStatement ps = con.prepareStatement("""
                    SELECT u.*, sum(if(u.id = l.sender, 1, 0)), sum(if(u.id = l.recipient, 1, 0))
                    FROM user u
                    CROSS JOIN letter l
                    GROUP BY u.id
                    ORDER BY u.id;"""
        );
        if (showSql) {
            logger.info(ps.toString());
        }
        ResultSet rs = ps.executeQuery();
        List<UserLettersStatProjection> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new UserLettersStatProjection(
                    userResultSetMapper.mapUser(rs),
                    rs.getInt(4),
                    rs.getInt(5)
            ));
        }
        return result;
    }

    /**
     * Пользователи, которые получили хотя бы одно сообщение с заданной темой.
     */
    public List<User> getUsersReceivedSubject(String subject) throws SQLException {
        PreparedStatement ps = con.prepareStatement("""
                    SELECT u.*, l.subject
                    FROM user u
                    JOIN letter l ON u.id = l.recipient
                    GROUP BY u.id
                    HAVING l.subject = ?
                    ORDER BY u.id;"""
        );
        ps.setObject(1, subject);
        if (showSql) {
            logger.info(ps.toString());
        }
        return userResultSetMapper.mapUsers(ps.executeQuery());
    }

    /**
     * Пользователи, которые не получали сообщения с заданной темой.
     */
    public List<User> getUsersNotReceivedSubject(String subject) throws SQLException {
        PreparedStatement ps = con.prepareStatement("""
                    SELECT u.*, sum(if(l.subject = ?, 1, 0))
                    FROM user u
                    LEFT JOIN letter l ON u.id = l.recipient
                    GROUP BY u.id
                    HAVING sum(if(l.subject = ?, 1, 0)) = 0
                    ORDER BY u.id;"""
        );
        ps.setObject(1, subject);
        ps.setObject(2, subject);
        if (showSql) {
            logger.info(ps.toString());
        }
        return userResultSetMapper.mapUsers(ps.executeQuery());
    }
}
