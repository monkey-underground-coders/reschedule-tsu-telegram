package space.delusive.tversu.dao.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import space.delusive.tversu.dao.UserDao;
import space.delusive.tversu.dao.submodel.CourseInfo;
import space.delusive.tversu.entity.User;

import java.util.Optional;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserDaoImpl implements UserDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> rowMapper;

    @Override
    public User getUserById(long id) {
        String query = "SELECT \"id\", \"state\", \"faculty\", \"group\", \"subgroup\", \"register_date\", \"last_message_date\", \"program\", \"course\" FROM public.\"users\" WHERE \"id\" = ?";
        Optional<User> user = jdbcTemplate.query(query, new Object[]{id}, rowMapper).stream().findAny();
        return user.orElse(null);
    }

    @Override
    public boolean addUser(User user) {
        String query = "INSERT INTO public.\"users\" (\"id\", \"state\", \"faculty\", \"group\", \"subgroup\", \"register_date\", \"last_message_date\", \"program\", \"course\") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(query, user.getId(), user.getState().ordinal(), user.getFaculty(), user.getGroup(), user.getSubgroup(), user.getRegisterDate(), user.getLastMessageDate(), user.getProgram(), user.getCourse())
                == 1;
    }

    @Override
    public boolean updateUser(User user) {
        String query = "UPDATE public.\"users\" SET \"state\" = ?, \"faculty\" = ?, \"group\" = ?, \"subgroup\" = ?, \"register_date\" = ?, \"last_message_date\" = ?, \"program\" = ?, \"course\" = ? WHERE \"id\" = ?";
        return jdbcTemplate.update(query, user.getState().ordinal(), user.getFaculty(), user.getGroup(), user.getSubgroup(), user.getRegisterDate(), user.getLastMessageDate(), user.getProgram(), user.getCourse(), user.getId())
                == 1;
    }

    @Override
    public Stream<CourseInfo> getCoursesCount() {
        String query = "SELECT \"faculty\", \"program\", \"course\", count(*) as \"count\" FROM public.\"users\" WHERE \"faculty\" IS NOT NULL and \"program\" IS NOT NULL and \"course\" IS NOT NULL GROUP BY \"faculty\", \"program\", \"course\"";
        return jdbcTemplate.queryForList(query, CourseInfo.class).stream();
    }

}