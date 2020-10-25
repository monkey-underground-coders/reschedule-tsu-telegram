package space.delusive.tversu.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import space.delusive.tversu.dto.WeekSign;
import space.delusive.tversu.rest.FacultyRepository;
import space.delusive.tversu.service.FacultyService;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {
    private final FacultyRepository facultyRepository;

    @Override
    public List<String> getFaculties() {
        return facultyRepository.getFaculties();
    }

    @Override
    public Set<String> getPrograms(String faculty) {
        return facultyRepository.getPrograms(faculty);
    }

    @Override
    public Set<Integer> getCourses(String faculty, String program) {
        return facultyRepository.getCourses(faculty, program);
    }

    @Override
    public Set<String> getGroups(String faculty, String program, int course) {
        return facultyRepository.getGroups(faculty, program, course);
    }

    @Override
    public int getSubgroupsCount(String faculty, String program, int course, String group) {
        return facultyRepository.getSubgroupsCount(faculty, program, course, group);
    }

    @Override
    public WeekSign getCurrentWeekSign(String faculty) {
        return facultyRepository.getCurrentWeekSign(faculty);
    }

    @Override
    public WeekSign getNextWeekSign(String faculty) {
        return getCurrentWeekSign(faculty) == WeekSign.MINUS ? WeekSign.PLUS : WeekSign.MINUS;
    }
}
