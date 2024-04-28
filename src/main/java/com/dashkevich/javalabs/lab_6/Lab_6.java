package com.dashkevich.javalabs.lab_6;

import com.dashkevich.javalabs.lab_6.dao.LetterDao;
import com.dashkevich.javalabs.lab_6.dao.UserDao;
import com.dashkevich.javalabs.lab_6.model.Letter;
import com.dashkevich.javalabs.lab_6.model.User;
import com.dashkevich.javalabs.lab_6.projection.UserLettersStatProjection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

public class Lab_6 {
    private static final Logger logger = LogManager.getLogger(Lab_6.class);
    private static final Properties props = new Properties();

    public static final List<User> testUsers = List.of(
            new User(1, "Иванов Иван Иванович",LocalDate.of(2000, 1, 1)),
            new User(2, "Петров Пётр Петрович", LocalDate.of(1999, 2, 2)),
            new User(3, "Тестов Тест Тестович", LocalDate.of(1998, 3, 4))
    );

    public static final List<Letter> testLetters = List.of(
            new Letter(1, 1, 2, "Тема 1", "Текст письма 1",LocalDateTime.of(2024, 4, 20, 0, 0)),
            new Letter(2, 2, 1, "Тема 1", "Текст письма 2", LocalDateTime.of(2024, 5, 20, 0, 0)),
            new Letter(3, 2, 1, "Тема 2", "Текст письма 3", LocalDateTime.of(2024, 5, 27, 0, 0)),
            new Letter(4, 1, 2, "Тема 3", "Короткий", LocalDateTime.of(2024, 5, 28, 0, 0)),
            new Letter(5, 3, 2, "Тема 3", "Очень длинный текст письма, длиннее, чем у всех остальных", LocalDateTime.of(2023, 5, 28, 0, 0))
    );

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        System.out.println("Письма");
        System.out.println();

        try {
            props.load(UserDao.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Class.forName(props.getProperty("datasource.driverClassName"));
        Connection con = DriverManager.getConnection(
                props.getProperty("datasource.url"),
                props.getProperty("datasource.user"),
                props.getProperty("datasource.password")
        );
        logger.info("Соединение установлено");
        boolean showSql = Boolean.parseBoolean(props.getProperty("show_sql"));

        UserDao userDao = new UserDao(con, showSql);
        LetterDao letterDao = new LetterDao(con, showSql);

        con.setAutoCommit(false);
        letterDao.deleteAll();
        userDao.deleteAll();
        userDao.saveAll(testUsers);
        letterDao.saveAll(testLetters);
        con.commit();
        con.setAutoCommit(true);

        User shortestLettersUser = userDao.getShortestLettersUser();
        logger.info("Пользователь, длина писем которого наименьшая - " + shortestLettersUser + "\n");

        List<UserLettersStatProjection> usersStats = userDao.getUsersLettersStats();
        StringBuilder sb = new StringBuilder();
        sb.append("Информация о пользователях, а также количестве полученных и отправленных ими письмах:\n");
        for (UserLettersStatProjection userStat : usersStats) {
            sb.append(userStat).append("\n");
        }
        logger.info(sb.toString());

        List<User> users = userDao.getUsersReceivedSubject("Тема 1");
        sb = new StringBuilder();
        sb.append("Пользователи, которые получили хотя бы одно сообщение с темой \"Тема 1\":\n");
        for (User user : users) {
            sb.append(user).append("\n");
        }
        logger.info(sb.toString());

        users = userDao.getUsersNotReceivedSubject("Тема 1");
        sb = new StringBuilder();
        sb.append("Пользователи, которые не получали сообщения с темой \"Тема 1\":\n");
        for (User user : users) {
            sb.append(user).append("\n");
        }
        logger.info(sb.toString());

        users = userDao.getAll();
        users.removeIf(user -> user.id().equals(3));
        int lettersCount = letterDao.sendLetterToAllUsers(3, users, "Новая тема");
        logger.info("Писем отправлено: " + lettersCount + "\n");

        con.close();
        logger.info("Соединение закрыто");
    }
}
