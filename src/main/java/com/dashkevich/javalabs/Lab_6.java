package com.dashkevich.javalabs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Lab_6 {
    private static final Logger logger = LogManager.getLogger(Lab_6.class);

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        System.out.println("Письма");
        System.out.println();

        Dao dao = new Dao();
        dao.openConnection();
        dao.initTestData();

        User shortestLettersUser = dao.getShortestLettersUser();
        logger.info("Пользователь, длина писем которого наименьшая - " + shortestLettersUser);

        List<UserLettersStatProjection> usersStats = dao.getUsersLettersStat();
        for (UserLettersStatProjection userStat : usersStats) {
            logger.info(userStat.toString());
        }

        dao.closeConnection();
    }

    private record User(Integer id, String fio, LocalDate dateOfBirth){
        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", fio='" + fio + '\'' +
                    ", dateOfBirth=" + dateOfBirth +
                    '}';
        }
    }

    private record Letter(
            Integer id,
            Integer sender,
            Integer recipient,
            String subject,
            String body,
            LocalDateTime dateOfSending
    ){}

    private record UserLettersStatProjection(User user, Integer lettersSent, Integer lettersReceived){
        @Override
        public String toString() {
            return "UserLettersStatProjection{" +
                    "user=" + user +
                    ", lettersSent=" + lettersSent +
                    ", lettersReceived=" + lettersReceived +
                    '}';
        }
    }

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

            List<User> users = new ArrayList<>();
            users.add(new User(1, "Иванов Иван Иванович", LocalDate.of(2000, 1, 1)));
            users.add(new User(2, "Петров Пётр Петрович", LocalDate.of(1999, 2, 2)));
            users.add(new User(3, "Тестов Тест Тестович", LocalDate.of(1998, 3, 4)));

            sb = new StringBuilder();
            sb.append("INSERT INTO `letters`.`user` (`id`, `fio`, `date_of_birth`) VALUES");
            sb.append("\n(?, ?, ?),".repeat(users.size()));
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");

            ps = con.prepareStatement(sb.toString());
            for (int i = 0; i < users.size(); i++) {
                ps.setObject(i * 3 + 1, users.get(i).id);
                ps.setObject(i * 3 + 2, users.get(i).fio);
                ps.setObject(i * 3 + 3, users.get(i).dateOfBirth);
            }
            if (showSql) {
                logger.info(ps.toString());
            }
            ps.executeUpdate();

            List<Letter> letters = new ArrayList<>();
            letters.add(new Letter(1, 1, 2, "Тема 1", "Текст письма 1", LocalDateTime.of(2024, 4, 20, 0, 0)));
            letters.add(new Letter(2, 2, 1, "Тема 1", "Текст письма 2", LocalDateTime.of(2024, 5, 20, 0, 0)));
            letters.add(new Letter(3, 2, 1, "Тема 2", "Текст письма 3", LocalDateTime.of(2024, 5, 27, 0, 0)));
            letters.add(new Letter(4, 1, 2, "Тема 3", "Короткий", LocalDateTime.of(2024, 5, 28, 0, 0)));
            letters.add(new Letter(5, 3, 2, "Тема 3", "Очень длинный текст письма, длиннее, чем у всех остальных", LocalDateTime.of(2023, 5, 28, 0, 0)));

            sb = new StringBuilder();
            sb.append("INSERT INTO `letters`.`letter` (`id`, `sender`, `recipient`, `subject`, `body`, `date_of_sending`) VALUES");
            sb.append("\n(?, ?, ?, ?, ?, ?),".repeat(letters.size()));
            sb.deleteCharAt(sb.length() - 1);
            sb.append(";");

            ps = con.prepareStatement(sb.toString());
            for (int i = 0; i < letters.size(); i++) {
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
            logger.info("Тестовые данные записаны");
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
                return new User(rs.getInt(1), rs.getString(2), rs.getDate(3).toLocalDate());
            } else {
                logger.warn("Пользователей не найдено");
                return null;
            }
        }

        /**
         * Информация о пользователях, а также количестве
         * полученных и отправленных ими письмах
         */
        public List<UserLettersStatProjection> getUsersLettersStat() throws SQLException {
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
                        new User(rs.getInt(1), rs.getString(2), rs.getDate(3).toLocalDate()),
                        rs.getInt(4),
                        rs.getInt(5)
                ));
            }
            return result;
        }
    }
}
