package em.persistence;

import em.model.User;

import java.util.List;

public interface UserRepository {
    User findUserByUsername(String username);
    User addUser(User user);
    User modifyUser(User user);
    void deleteUser(User user);
    List<User> findAll();
}
