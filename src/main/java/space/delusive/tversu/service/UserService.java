package space.delusive.tversu.service;

import space.delusive.tversu.entity.User;

public interface UserService {
    User getUserById(long id);

    boolean addUser(User user);

    boolean updateUser(User user);
}
