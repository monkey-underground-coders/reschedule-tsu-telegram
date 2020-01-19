package space.delusive.tversu.service;

import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.DayOfWeek;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TimingService {
    Optional<Cell> getCurrentLesson(User user);

    Optional<Cell> getNextLesson(User user);

    List<Cell> getTodayLessons(User user);

    List<Cell> getTomorrowOrMondayLessons(User user);

    Map<DayOfWeek, List<Cell>> getRemainingLessonsOfWeek(User user);

    List<Cell> getLessonsOfSpecifiedDay(User user, DayOfWeek dayOfWeek, WeekSign weekSign);
}
