package space.delusive.tversu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;
import space.delusive.tversu.rest.CellRepository;
import space.delusive.tversu.rest.FacultyRepository;
import space.delusive.tversu.service.TimingService;
import space.delusive.tversu.util.DateUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TimingServiceImpl implements TimingService {
    private final CellRepository cellRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public TimingServiceImpl(CellRepository cellRepository, FacultyRepository facultyRepository) {
        this.cellRepository = cellRepository;
        this.facultyRepository = facultyRepository;
    }

    @Override
    public Optional<Cell> getCurrentLesson(User user) {
        return getTodayLessonsAsStream(user)
                .filter(cell -> DateUtils.compareTime(cell.getStart(), DateUtils.getCurrentTime()) != -1)
                .filter(cell -> DateUtils.compareTime(cell.getEnd(), DateUtils.getCurrentTime()) != 1)
                .findFirst();
    }

    @Override
    public Optional<Cell> getNextLesson(User user) {
        return getTodayLessonsAsStream(user)
                .filter(cell -> DateUtils.compareTime(cell.getStart(), DateUtils.getCurrentTime()) == -1)
                .min((o1, o2) -> DateUtils.compareTime(o1.getStart(), o2.getStart()));
    }

    @Override
    public List<Cell> getTodayLessons(User user) {
        return getTodayLessonsAsStream(user).collect(Collectors.toList());
    }

    private Stream<Cell> getTodayLessonsAsStream(User user) {
        List<Cell> cells = cellRepository.getCells(user.getFaculty(), user.getGroup());
        WeekSign currentWeekSign = facultyRepository.getCurrentWeekSign(user.getFaculty());
        return cells.stream()
                .filter(cell -> cell.getWeekSign() == currentWeekSign || cell.getWeekSign() == WeekSign.ANY)
                .filter(cell -> cell.getDayOfWeek() == DateUtils.getCurrentDayOfWeek())
                .filter(cell -> cell.getSubgroup() == user.getSubgroup() || cell.getSubgroup() == 0);
    }
}
