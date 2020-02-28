package space.delusive.tversu.service;

import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.DayOfWeek;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.entity.WeekSign;
import space.delusive.tversu.exception.SoldisWhatTheFuckException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TimingService {
    Optional<Cell> getCurrentLesson(User user) throws SoldisWhatTheFuckException;

    Optional<Cell> getNextLesson(User user) throws SoldisWhatTheFuckException;

    List<Cell> getTodayLessons(User user) throws SoldisWhatTheFuckException;

    List<Cell> getTomorrowOrMondayLessons(User user) throws SoldisWhatTheFuckException;

    Map<DayOfWeek, List<Cell>> getRemainingLessonsOfWeek(User user) throws SoldisWhatTheFuckException;

    List<Cell> getLessonsOfSpecifiedDay(User user, DayOfWeek dayOfWeek, WeekSign weekSign) throws SoldisWhatTheFuckException;
}
