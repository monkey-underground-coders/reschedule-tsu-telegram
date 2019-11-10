package space.delusive.tversu.service.impl;

import org.springframework.stereotype.Component;
import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.DayOfWeek;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;
import space.delusive.tversu.rest.CellRepository;
import space.delusive.tversu.service.FacultyService;
import space.delusive.tversu.service.TimingService;
import space.delusive.tversu.util.DateUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class TimingServiceImpl implements TimingService {
    private final CellRepository cellRepository;
    private final FacultyService facultyService;

    public TimingServiceImpl(CellRepository cellRepository, FacultyService facultyService) {
        this.cellRepository = cellRepository;
        this.facultyService = facultyService;
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
                .min((o1, o2) -> DateUtils.compareTime(o2.getStart(), o1.getStart()));
    }

    @Override
    public List<Cell> getTodayLessons(User user) {
        return getTodayLessonsAsStream(user).collect(Collectors.toList());
    }

    @Override
    public List<Cell> getTomorrowOrMondayLessons(User user) {
        DayOfWeek targetDay = DateUtils.getCurrentDayOfWeek() == DayOfWeek.SATURDAY ?
                DayOfWeek.MONDAY : DateUtils.getCurrentDayOfWeek().next();
        return getLessonsOfDayAsStream(user, targetDay, targetDay == DayOfWeek.MONDAY).collect(Collectors.toList());
    }

    private Stream<Cell> getTodayLessonsAsStream(User user) {
        return getLessonsOfDayAsStream(user, DateUtils.getCurrentDayOfWeek(), false);
    }

    private Stream<Cell> getLessonsOfDayAsStream(User user, DayOfWeek day, boolean isNextWeek) {
        List<Cell> cells = cellRepository.getCells(user.getFaculty(), user.getGroup());
        WeekSign targetWeekSign = isNextWeek ? facultyService.getNextWeekSign(user.getFaculty()) : facultyService.getCurrentWeekSign(user.getFaculty());
        return cells.stream()
                .filter(cell -> cell.getWeekSign() == targetWeekSign || cell.getWeekSign() == WeekSign.ANY)
                .filter(cell -> cell.getDayOfWeek() == day)
                .filter(cell -> cell.getSubgroup() == user.getSubgroup() || cell.getSubgroup() == 0);
    }
}
