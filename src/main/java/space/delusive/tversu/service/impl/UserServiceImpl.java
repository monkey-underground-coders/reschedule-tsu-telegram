package space.delusive.tversu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import space.delusive.tversu.dao.UserDao;
import space.delusive.tversu.entity.User;
import space.delusive.tversu.service.UserService;

@Component
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    @Autowired
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserById(long id) {
        return userDao.getUserById(id);
    }

    @Override
    public boolean addUser(User user) {
        return userDao.addUser(user);
    }

    @Override
    public boolean updateUser(User user) {
        return userDao.updateUser(user);
    }
}
