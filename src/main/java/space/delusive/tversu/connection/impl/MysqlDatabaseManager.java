package space.delusive.tversu.connection.impl;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import space.delusive.tversu.connection.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

@Component
@PropertySource(value = "classpath:timingbot.properties")
public class MysqlDatabaseManager implements DatabaseManager {
    private final MysqlDataSource mysqlDataSource = new MysqlDataSource();

    public MysqlDatabaseManager(@Value("${db.url}") String url, @Value("${db.username}") String username, @Value("${db.password}") String password) {
        mysqlDataSource.setUrl(url);
        mysqlDataSource.setUser(username);
        mysqlDataSource.setPassword(password);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return mysqlDataSource.getConnection();
    }
}

