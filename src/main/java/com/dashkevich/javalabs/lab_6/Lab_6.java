package com.dashkevich.javalabs.lab_6;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class Lab_6 {
    private static final Logger logger = LogManager.getLogger(Lab_6.class);

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        System.out.println("Письма");
        System.out.println();

        Dao dao = new Dao();
        dao.openConnection();
        dao.initTestData();

        User shortestLettersUser = dao.getShortestLettersUser();
        logger.info("Пользователь, длина писем которого наименьшая - " + shortestLettersUser + "\n");

        List<UserLettersStatProjection> usersStats = dao.getUsersLettersStats();
        StringBuilder sb = new StringBuilder();
        sb.append("Информация о пользователях, а также количестве полученных и отправленных ими письмах:\n");
        for (UserLettersStatProjection userStat : usersStats) {
            sb.append(userStat).append("\n");
        }
        logger.info(sb.toString());

        List<User> users = dao.getUsersReceivedSubject("Тема 1");
        sb = new StringBuilder();
        sb.append("Пользователи, которые получили хотя бы одно сообщение с темой \"Тема 1\":\n");
        for (User user : users) {
            sb.append(user).append("\n");
        }
        logger.info(sb.toString());

        users = dao.getUsersNotReceivedSubject("Тема 1");
        sb = new StringBuilder();
        sb.append("Пользователи, которые не получали сообщения с темой \"Тема 1\":\n");
        for (User user : users) {
            sb.append(user).append("\n");
        }
        logger.info(sb.toString());

        int lettersCount = dao.sendLetterToAllUsers(3, "Новая тема");
        logger.info("Писем отправлено: " + lettersCount + "\n");

        dao.closeConnection();
    }
}
