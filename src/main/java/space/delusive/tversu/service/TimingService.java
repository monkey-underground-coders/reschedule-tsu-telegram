package space.delusive.tversu.service;

import space.delusive.tversu.entity.Cell;
import space.delusive.tversu.entity.User;

import java.util.List;
import java.util.Optional;

public interface TimingService {
    Optional<Cell> getCurrentLesson(User user);

    Optional<Cell> getNextLesson(User user);

    List<Cell> getTodayLessons(User user);
}
