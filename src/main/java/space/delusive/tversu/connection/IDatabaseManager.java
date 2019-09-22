package space.delusive.tversu.connection;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDatabaseManager {
    Connection getConnection() throws SQLException;
}

