package space.delusive.tversu.service;

import space.delusive.tversu.dto.WeekSign;

import java.util.List;
import java.util.Set;

public interface FacultyService {
    List<String> getFaculties();

    Set<String> getPrograms(String faculty);

    Set<Integer> getCourses(String faculty, String program);

    Set<String> getGroups(String faculty, String program, int course);

    int getSubgroupsCount(String faculty, String program, int course, String group);

    WeekSign getCurrentWeekSign(String faculty);

    WeekSign getNextWeekSign(String faculty);
}
