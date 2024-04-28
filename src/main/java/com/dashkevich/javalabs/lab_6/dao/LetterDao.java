package com.dashkevich.javalabs.lab_6.dao;

import com.dashkevich.javalabs.lab_6.model.Letter;
import com.dashkevich.javalabs.lab_6.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class LetterDao {
    private final Logger logger = LogManager.getLogger(LetterDao.class);
    private final boolean showSql;
    private final Connection con;

    public LetterDao(Connection con, boolean showSql) {
        this.showSql = showSql;
        this.con = con;
    }

    public int deleteAll() throws SQLException {
        PreparedStatement ps = con.prepareStatement("DELETE FROM letter");
        return ps.executeUpdate();
    }

    public int saveAll(List<Letter> letters) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `letters`.`letter` (`id`, `sender`, `recipient`, `subject`, `body`, `date_of_sending`) VALUES");
        sb.append("\n(?, ?, ?, ?, ?, ?),".repeat(letters.size()));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");

        PreparedStatement ps = con.prepareStatement(sb.toString());
        for (int i = 0; i < letters.size(); i++) {
            ps.setObject(i * 6 + 1, letters.get(i).id());
            ps.setObject(i * 6 + 2, letters.get(i).sender());
            ps.setObject(i * 6 + 3, letters.get(i).recipient());
            ps.setObject(i * 6 + 4, letters.get(i).subject());
            ps.setObject(i * 6 + 5, letters.get(i).body());
            ps.setObject(i * 6 + 6, letters.get(i).dateOfSending());
        }
        if (showSql) {
            logger.info(ps.toString());
        }
        return ps.executeUpdate();
    }

    /**
     * Направить письмо заданного человека с заданной темой всем адресатам.
     *
     * @param userId отправитель
     * @return кол-во отправленных писем
     */
    public int sendLetterToAllUsers(Integer userId, List<User> users, String subject) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `letters`.`letter` (`sender`, `recipient`, `subject`, `date_of_sending`) VALUES");
        sb.append("\n(?, ?, ?, ?),".repeat(users.size()));
        sb.deleteCharAt(sb.length() - 1);
        sb.append(";");

        PreparedStatement ps = con.prepareStatement(sb.toString());
        for (int i = 0; i < users.size(); i++) {
            ps.setObject(i * 4 + 1, userId);
            ps.setObject(i * 4 + 2, users.get(i).id());
            ps.setObject(i * 4 + 3, subject);
            ps.setObject(i * 4 + 4, LocalDateTime.now());
        }
        if (showSql) {
            logger.info(ps.toString());
        }

        return ps.executeUpdate();
    }
}
