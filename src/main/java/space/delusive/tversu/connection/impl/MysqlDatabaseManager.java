package space.delusive.tversu.connection.impl;

import com.mysql.cj.jdbc.MysqlDataSource;
import space.delusive.tversu.connection.IDatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class MysqlDatabaseManager implements IDatabaseManager {
    private final MysqlDataSource mysqlDataSource = new MysqlDataSource();

    public MysqlDatabaseManager(Properties props) {
        mysqlDataSource.setUrl(props.getProperty("db.url"));
        mysqlDataSource.setUser(props.getProperty("db.username"));
        mysqlDataSource.setPassword(props.getProperty("db.password"));
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mysqlDataSource.getConnection();
    }
}

