package em.server;

import em.model.Task;
import em.model.User;
import em.persistence.TaskRepository;
import em.persistence.UserRepository;
import em.services.EMObserver;
import em.services.Service;
import em.services.ServicesException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceImpl implements Service {
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private Map<Long, EMObserver> loggedUsers;

    public ServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.loggedUsers = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized User login(User user, EMObserver client) throws ServicesException {
        User usr = findUserByUsername(user.getUsername());
        if (user.getPassword().equals(usr.getPassword())) {
            if(loggedUsers.get(usr.getId()) != null)
                throw new ServicesException("Employee already logged in.");
            loggedUsers.put(usr.getId(), client);
            return usr;
        }
        throw new ServicesException("\n...Incorrect password!...\n");
    }

    @Override
    public synchronized void logout(User user, EMObserver client) throws ServicesException {
        User usr = findUserByUsername(user.getUsername());
        EMObserver localClient = loggedUsers.remove(usr.getId());
        if (localClient == null)
            throw new ServicesException("Employee " + usr.getId() + " is not logged in.");
    }

    private User findUserByUsername(String username) throws ServicesException {
        User user = userRepository.findUserByUsername(username);
        if (user == null)
            throw new ServicesException("\n...This employee do not exists!...\n");
        return user;
    }

    @Override
    public User addUserAccount(User user) throws ServicesException {
        User u = userRepository.findUserByUsername(user.getUsername());
        if (u != null) {
            throw new ServicesException("\n...This username already exists!...\n");
        }
        User usr = userRepository.addUser(user);
        if (usr == null) {
            throw new ServicesException("\n...Add user account error...\n");
        }
        return usr;
    }

    @Override
    public User modifyUserAccount(User new_user) throws ServicesException {
        User u = userRepository.findUserByUsername(new_user.getUsername());
        if (u != null) {
            throw new ServicesException("\n...This username already exists!...\n");
        }
        User usr = userRepository.modifyUser(new_user);
        if (usr == null) {
            throw new ServicesException("\n...Modify user account error...\n");
        }
        return usr;
    }

    @Override
    public User deleteUserAccount(User user) throws ServicesException {
        findUserByUsername(user.getUsername());
        userRepository.deleteUser(user);
        return user;
    }

    @Override
    public Task sendTask(User user, Task task) throws ServicesException {
        return null;
    }

    @Override
    public void presentToWork(User user) throws ServicesException {

    }

    @Override
    public void leaveWork(User user, EMObserver client) throws ServicesException {

    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
