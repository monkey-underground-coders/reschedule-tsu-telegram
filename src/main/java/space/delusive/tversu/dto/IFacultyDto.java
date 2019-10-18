package space.delusive.tversu.dto;

import java.util.List;
import java.util.Set;

public interface IFacultyDto {
    List<String> getFaculties();

    Set<String> getPrograms(String faculty);

    Set<Integer> getCourses(String faculty, String program);

    Set<String> getGroups(String faculty, String program, int course);

    int getSubgroupsCount(String faculty, String program, int course, String group);
}
