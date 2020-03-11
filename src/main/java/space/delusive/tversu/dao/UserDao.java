package space.delusive.tversu.dao;

import space.delusive.tversu.dao.submodel.CourseInfo;
import space.delusive.tversu.entity.User;

import java.util.stream.Stream;

public interface UserDao {
    User getUserById(long id);

    boolean addUser(User user);

    boolean updateUser(User user);

    Stream<CourseInfo> getCoursesCount();
}
