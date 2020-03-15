package space.delusive.tversu.dao.rowmapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import space.delusive.tversu.BotState;
import space.delusive.tversu.entity.User;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int i) throws SQLException {
        long id = resultSet.getLong("id");
        int state = resultSet.getInt("state");
        String faculty = resultSet.getString("faculty");
        String program = resultSet.getString("program");
        int course = resultSet.getInt("course");
        String group = resultSet.getString("group");
        int subgroup = resultSet.getInt("subgroup");
        Date registerDate = resultSet.getDate("register_date");
        Date lastMessageDate = resultSet.getDate("last_message_date");
        return new User(id, BotState.getByOrdinal(state), faculty, program, course, group, subgroup, registerDate, lastMessageDate);
    }
}
