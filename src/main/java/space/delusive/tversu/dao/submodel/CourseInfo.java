package space.delusive.tversu.dao.submodel;

import lombok.Data;

@Data
public class CourseInfo {
    private final String faculty;
    private final String program;
    private final int course;
    private final int count;
}
