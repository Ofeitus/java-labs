package com.dashkevich.javalabs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Lab_6 {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        System.out.println("Письма");
        System.out.println();

        Dao dao = new Dao();
        dao.openConnection();
        dao.initTestData();



        dao.closeConnection();
    }

    private static record User(Integer id, String fio, Date dateOfBirth){}

    private static record Letter(
            Integer id,
            Integer sender,
            Integer recipient,
            String subject,
            String body,
            Date dateOfSending
    ){}

    private static class Dao {
        private final Logger logger = LogManager.getLogger(Dao.class);
        private static final Properties props = new Properties();
        private final boolean showSql;
        private Connection con;

        private Dao() throws ClassNotFoundException {
            try {
                props.load(Dao.class.getClassLoader().getResourceAsStream("application.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.showSql = Boolean.parseBoolean(props.getProperty("show_sql"));
            Class.forName(props.getProperty("datasource.driverClassName"));
        }

        public void openConnection() throws SQLException {
            con = DriverManager.getConnection(
                    props.getProperty("datasource.url"),
                    props.getProperty("datasource.user"),
                    props.getProperty("datasource.password")
            );
            logger.info("Соединение установлено");
        }

        public void closeConnection() throws SQLException {
            con.close();
            logger.info("Соединение закрыто");
        }

        public void initTestData() throws SQLException {
            con.setAutoCommit(false);

            PreparedStatement ps;
            StringBuilder sb;

            ps = con.prepareStatement("DELETE FROM `letters`.`letter`");
            ps.executeUpdate();
            ps = con.prepareStatement("DELETE FROM `letters`.`user`");
            ps.executeUpdate();

            sb = new StringBuilder();
            sb.append("INSERT INTO `letters`.`user` (`id`, `fio`, `date_of_birth`) VALUES");
            sb.append("\n(?, ?, ?),".repeat(2));
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");

            List<User> users = new ArrayList<>();
            users.add(new User(1, "Иванов Иван Иванович", new Date(2000, 0, 1)));
            users.add(new User(2, "Петров Пётр Петрович", new Date(1999, 1, 2)));
            ps = con.prepareStatement(sb.toString());
            for (int i = 0; i < 2; i++) {
                ps.setObject(i * 3 + 1, users.get(i).id);
                ps.setObject(i * 3 + 2, users.get(i).fio);
                ps.setObject(i * 3 + 3, users.get(i).dateOfBirth);
            }
            if (showSql) {
                logger.info(ps.toString());
            }
            ps.executeUpdate();

            sb = new StringBuilder();
            sb.append("INSERT INTO `letters`.`letter` (`id`, `sender`, `recipient`, `subject`, `body`, `date_of_sending`) VALUES");
            sb.append("\n(?, ?, ?, ?, ?, ?),".repeat(3));
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");

            List<Letter> letters = new ArrayList<>();
            letters.add(new Letter(1, 1, 2, "Тема 1", "Текст письма 1", new Date(2024, 4, 20)));
            letters.add(new Letter(2, 2, 1, "Тема 1", "Текст письма 2", new Date(2024, 5, 20)));
            letters.add(new Letter(3, 2, 1, "Тема 2", "Текст письма 3", new Date(2024, 5, 27)));
            ps = con.prepareStatement(sb.toString());
            for (int i = 0; i < 3; i++) {
                ps.setObject(i * 6 + 1, letters.get(i).id);
                ps.setObject(i * 6 + 2, letters.get(i).sender);
                ps.setObject(i * 6 + 3, letters.get(i).recipient);
                ps.setObject(i * 6 + 4, letters.get(i).subject);
                ps.setObject(i * 6 + 5, letters.get(i).body);
                ps.setObject(i * 6 + 6, letters.get(i).dateOfSending);
            }
            if (showSql) {
                logger.info(ps.toString());
            }
            ps.executeUpdate();

            con.commit();
            con.setAutoCommit(true);
        }

        /**
         * Пользователь, длина писем которого наименьшая
         */
        public User getShortestLettersUser() throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    ""
            );
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt(1), rs.getString(2), rs.getDate(3));
            } else {
                return null;
            }
        }
    }
}
