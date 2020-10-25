package space.delusive.tversu.service;

import space.delusive.tversu.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(long id);

    Optional<User> createNewUser(long userId);

    void updateUser(User user);
}
