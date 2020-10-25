package space.delusive.tversu.dto;

import lombok.Value;

/**
 * Is used for metrics aggregation only
 */

@Value
public class CourseInfo {
    String faculty;
    String program;
    int course;
    int count;
}
