package space.delusive.tversu.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.delusive.tversu.connection.DatabaseManager;
import space.delusive.tversu.dao.UserDao;
import space.delusive.tversu.entity.User;

import java.sql.*;

@Component
public class UserDaoImpl implements UserDao {
    private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

    private final DatabaseManager databaseManager;

    @Autowired
    public UserDaoImpl(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User getUserById(long id) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT `id`, `state`, `faculty`, `group`, `subgroup`, `register_date`, `last_message_date`, `program`, `course` FROM `users` WHERE `id` = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) return null;
                    return extractUserFromResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return null;
    }

    @Override
    public boolean addUser(User user) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "INSERT INTO `users` (`id`, `state`, `faculty`, `group`, `subgroup`, `register_date`, `last_message_date`, `program`, `course`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setInt(2, user.getState());
                preparedStatement.setString(3, user.getFaculty());
                preparedStatement.setString(4, user.getGroup());
                preparedStatement.setInt(5, user.getSubgroup());
                preparedStatement.setDate(6, user.getRegisterDate());
                preparedStatement.setDate(7, user.getLastMessageDate());
                preparedStatement.setString(8, user.getProgram());
                preparedStatement.setInt(9, user.getCourse());
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return false;
    }

    @Override
    public boolean updateUser(User user) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "UPDATE `users` SET `state` = ?, `faculty` = ?, `group` = ?, `subgroup` = ?, `register_date` = ?, `last_message_date` = ?, `program` = ?, `course` = ? WHERE `id` = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, user.getState());
                preparedStatement.setString(2, user.getFaculty());
                preparedStatement.setString(3, user.getGroup());
                preparedStatement.setInt(4, user.getSubgroup());
                preparedStatement.setDate(5, user.getRegisterDate());
                preparedStatement.setDate(6, user.getLastMessageDate());
                preparedStatement.setString(7, user.getProgram());
                preparedStatement.setInt(8, user.getCourse());
                preparedStatement.setLong(9, user.getId());
                preparedStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return false;
    }

    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException {
        long id = resultSet.getLong("id");
        int state = resultSet.getInt("state");
        String faculty = resultSet.getString("faculty");
        String program = resultSet.getString("program");
        int course = resultSet.getInt("course");
        String group = resultSet.getString("group");
        int subgroup = resultSet.getInt("subgroup");
        Date registerDate = resultSet.getDate("register_date");
        Date lastMessageDate = resultSet.getDate("last_message_date");
        return new User(id, state, faculty, program, course, group, subgroup, registerDate, lastMessageDate);
    }
}
