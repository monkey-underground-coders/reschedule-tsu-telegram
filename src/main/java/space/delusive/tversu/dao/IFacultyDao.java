package space.delusive.tversu.dao;

import java.util.List;
import java.util.Set;

public interface IFacultyDao {
    List<String> getFaculties();
    Set<String> getPrograms(String faculty);
}
