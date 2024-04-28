package com.dashkevich.javalabs.lab_7;

import com.dashkevich.javalabs.lab_6.dao.LetterDao;
import com.dashkevich.javalabs.lab_6.dao.UserDao;
import com.dashkevich.javalabs.lab_6.model.User;
import com.dashkevich.javalabs.lab_6.projection.UserLettersStatProjection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static com.dashkevich.javalabs.lab_6.Lab_6.testLetters;
import static com.dashkevich.javalabs.lab_6.Lab_6.testUsers;
import static org.junit.jupiter.api.Assertions.*;

public class DaoTest {
	private static final Logger logger = LogManager.getLogger(DaoTest.class);
	private static final Properties props = new Properties();

	private static Connection con;
	private static UserDao userDao;
	private static LetterDao letterDao;

	@BeforeAll
	public static void beforeAll() throws ClassNotFoundException, SQLException {

		try {
			props.load(UserDao.class.getClassLoader().getResourceAsStream("application.properties"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Class.forName(props.getProperty("datasource.driverClassName"));
		con = DriverManager.getConnection(
				props.getProperty("datasource.url"),
				props.getProperty("datasource.user"),
				props.getProperty("datasource.password")
		);
		logger.info("Соединение установлено");
		boolean showSql = Boolean.parseBoolean(props.getProperty("show_sql"));

		userDao = new UserDao(con, showSql);
		letterDao = new LetterDao(con, showSql);

		con.setAutoCommit(false);
		letterDao.deleteAll();
		userDao.deleteAll();
		int savedUsers = userDao.saveAll(testUsers);
		assertEquals(3, savedUsers);
		int savedLetters = letterDao.saveAll(testLetters);
		assertEquals(5, savedLetters);
		con.commit();
		con.setAutoCommit(true);
	}

	@AfterAll
	static void afterAll() throws SQLException {
		int LettersDeleted = letterDao.deleteAll();
		assertEquals(7, LettersDeleted);
		int usersDeleted = userDao.deleteAll();
		assertEquals(3, usersDeleted);
		con.close();
	}

	@Test
	public void getShortestLettersUser() throws SQLException {
		User expected = testUsers.getFirst();
		User actual = userDao.getShortestLettersUser();

		assertEquals(expected, actual);
	}

	@Test
	public void getUsersLettersStats() throws SQLException {
		List<UserLettersStatProjection> actual = userDao.getUsersLettersStats();

		assertEquals(testUsers.get(0), actual.get(0).user());
		assertEquals(testUsers.get(1), actual.get(1).user());
		assertEquals(testUsers.get(2), actual.get(2).user());

		assertEquals(2, actual.get(0).lettersSent());
		assertEquals(2, actual.get(0).lettersReceived());
		assertEquals(2, actual.get(1).lettersSent());
		assertEquals(3, actual.get(1).lettersReceived());
		assertEquals(1, actual.get(2).lettersSent());
		assertEquals(0, actual.get(2).lettersReceived());
	}

	@Test
	public void getUsersReceivedSubject() throws SQLException {
		String subject = "Тема 1";
		List<User> actual = userDao.getUsersReceivedSubject(subject);

		assertEquals(testUsers.get(0), actual.get(0));
		assertEquals(testUsers.get(1), actual.get(1));
	}

	@Test
	public void getUsersNotReceivedSubject() throws SQLException {
		String subject = "Тема 1";
		List<User> actual = userDao.getUsersNotReceivedSubject(subject);

		assertEquals(testUsers.getLast(), actual.getFirst());
	}

	@Test
	public void sendLetterToAllUsers() throws SQLException {
		Integer userId = 3;
		String subject = "Новая тема";
		List<User> users = userDao.getAll();
		users.removeIf(user -> user.id().equals(3));
		int actual = letterDao.sendLetterToAllUsers(userId, users, subject);

		assertEquals(2, actual);
	}
}
