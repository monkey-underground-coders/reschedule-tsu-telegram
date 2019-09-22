package space.delusive.tversu.connection.impl;

import com.mysql.cj.jdbc.MysqlDataSource;
import space.delusive.tversu.connection.IDatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

public class MysqlDatabaseManager implements IDatabaseManager {
    private final MysqlDataSource mysqlDataSource = new MysqlDataSource();

    public MysqlDatabaseManager(String url, String username, String password) {
        mysqlDataSource.setUrl(url);
        mysqlDataSource.setUser(username);
        mysqlDataSource.setPassword(password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mysqlDataSource.getConnection();
    }
}

