package space.delusive.tversu.dao;

import space.delusive.tversu.entity.User;

public interface IUserDao {
    User getUserById(long id);
    boolean addUser(User user);
    boolean updateUser(User user);
}
