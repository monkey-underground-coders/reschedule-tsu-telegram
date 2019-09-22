package space.delusive.tversu.dao.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import space.delusive.tversu.connection.IDatabaseManager;
import space.delusive.tversu.dao.IUserDao;
import space.delusive.tversu.entity.User;

import java.sql.*;

public class SqlUserDao implements IUserDao {
    private final Logger logger = LogManager.getLogger(SqlUserDao.class);
    private final IDatabaseManager databaseManager;

    public SqlUserDao(IDatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public User getUserById(long id) {
        try (Connection connection = databaseManager.getConnection()) {
            String query = "SELECT `id`, `state`, `faculty`, `group`, `subgroup`, `register_date`, `last_message_date` FROM `users` WHERE `id` = ?";
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
            String query = "INSERT INTO `users` (`id`, `state`, `faculty`, `group`, `subgroup`, `register_date`, `last_message_date`) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setLong(1, user.getId());
                preparedStatement.setInt(2, user.getState());
                preparedStatement.setString(3, user.getFaculty());
                preparedStatement.setString(4, user.getGroup());
                preparedStatement.setInt(5, user.getSubgroup());
                preparedStatement.setDate(6, user.getRegisterDate());
                preparedStatement.setDate(7, user.getLastMessageDate());
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
            String query = "UPDATE `users` SET `state` = ?, `faculty` = ?, `group` = ?, `subgroup` = ?, `register_date` = ?, `last_message_date` = ? WHERE `id` = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, user.getState());
                preparedStatement.setString(2, user.getFaculty());
                preparedStatement.setString(3, user.getGroup());
                preparedStatement.setInt(4, user.getSubgroup());
                preparedStatement.setDate(5, user.getRegisterDate());
                preparedStatement.setDate(6, user.getLastMessageDate());
                preparedStatement.setLong(7, user.getId());
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
        String group = resultSet.getString("group");
        int subgroup = resultSet.getInt("subgroup");
        Date registerDate = resultSet.getDate("register_date");
        Date lastMessageDate = resultSet.getDate("last_message_date");
        return new User(id, state, faculty, group, subgroup, registerDate, lastMessageDate);
    }
}
