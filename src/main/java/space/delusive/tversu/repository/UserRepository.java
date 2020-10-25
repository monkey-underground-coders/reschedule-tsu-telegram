package space.delusive.tversu.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import space.delusive.tversu.entity.CourseInfo;
import space.delusive.tversu.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    @Query("SELECT new space.delusive.tversu.entity.CourseInfo(u.faculty, u.program, u.course, CAST(COUNT(u.course) as int)) " +
            "FROM User as u " +
            "WHERE faculty IS NOT NULL " +
                "and program IS NOT NULL " +
                "and course IS NOT NULL " +
            "GROUP BY u.faculty, u.program, u.course")
    List<CourseInfo> getCoursesInfo();
}
