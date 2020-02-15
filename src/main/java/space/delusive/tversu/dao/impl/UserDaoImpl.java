package space.delusive.tversu.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import space.delusive.tversu.dao.UserDao;
import space.delusive.tversu.entity.User;

import java.sql.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper = (resultSet, i) -> {
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
    };

    @Override
    public User getUserById(long id) {
        String query = "SELECT \"id\", \"state\", \"faculty\", \"group\", \"subgroup\", \"register_date\", \"last_message_date\", \"program\", \"course\" FROM public.\"users\" WHERE \"id\" = ?";
        Optional<User> user = jdbcTemplate.query(query, new Object[]{id}, rowMapper).stream().findAny();
        return user.orElse(null);
    }

    @Override
    public boolean addUser(User user) {
        String query = "INSERT INTO public.\"users\" (\"id\", \"state\", \"faculty\", \"group\", \"subgroup\", \"register_date\", \"last_message_date\", \"program\", \"course\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(query, user.getId(), user.getState(), user.getFaculty(), user.getGroup(), user.getSubgroup(), user.getRegisterDate(), user.getLastMessageDate(), user.getProgram(), user.getCourse())
                == 1;
    }

    @Override
    public boolean updateUser(User user) {
        String query = "UPDATE public.\"users\" SET \"state\" = ?, \"faculty\" = ?, \"group\" = ?, \"subgroup\" = ?, \"register_date\" = ?, \"last_message_date\" = ?, \"program\" = ?, \"course\" = ? WHERE \"id\" = ?";
        return jdbcTemplate.update(query, user.getState(), user.getFaculty(), user.getGroup(), user.getSubgroup(), user.getRegisterDate(), user.getLastMessageDate(), user.getProgram(), user.getCourse(), user.getId())
                == 1;
    }

}