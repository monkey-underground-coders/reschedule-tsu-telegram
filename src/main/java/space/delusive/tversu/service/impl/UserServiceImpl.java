package space.delusive.tversu.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Component;
import space.delusive.tversu.BotState;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.repository.UserRepository;
import space.delusive.tversu.service.UserService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public Optional<User> getUserById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> createNewUser(long userId) {
        Date registerDate = Date.valueOf(LocalDate.now());
        val user = new User(userId, BotState.START, null, null, 0, null, 0,
                registerDate, registerDate);
        userRepository.save(user);
        return getUserById(userId);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }
}
