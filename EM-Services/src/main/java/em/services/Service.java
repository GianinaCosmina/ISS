package em.services;

import em.model.Task;
import em.model.User;

import java.util.List;

public interface Service {
    User login(User user, EMObserver client) throws ServicesException;
    void logout(User user, EMObserver client) throws ServicesException;
    User addUserAccount(User user) throws ServicesException;
    User modifyUserAccount(User new_user) throws ServicesException;
    User deleteUserAccount(User user) throws ServicesException;
    Task sendTask(User user, Task task) throws ServicesException;
    void presentToWork(User user) throws ServicesException;
    void leaveWork(User user, EMObserver client) throws ServicesException;
    List<User> findAllUsers() throws ServicesException;
}
