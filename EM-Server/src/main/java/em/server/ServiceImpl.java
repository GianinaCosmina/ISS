package em.server;

import em.model.Role;
import em.model.Task;
import em.model.User;
import em.persistence.TaskRepository;
import em.persistence.UserRepository;
import em.services.EMObserver;
import em.services.Service;
import em.services.ServicesException;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServiceImpl implements Service {
    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private Map<Long, EMObserver> loggedUsers;
    private Map<Long, LocalTime> presentAtWork;
    private final int defaultThreadsNo = 5;

    public ServiceImpl(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.loggedUsers = new ConcurrentHashMap<>();
        this.presentAtWork = new ConcurrentHashMap<>();
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
    public Task sendTask(Task task) throws ServicesException {
        Task task1 = taskRepository.addTask(task);
        notifyEmployee(task);
        return task1;
    }

    private void notifyEmployee(Task task) {
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);

        for(User emp : userRepository.findAll()) {
            if (emp.getRole() == Role.EMPLOYEE && Objects.equals(emp.getUsername(), task.getUser().getUsername())) {
                EMObserver emObserver = loggedUsers.get(emp.getId());
                if (emObserver != null) {
                    executor.execute(() -> {
                        try {
                            System.out.println("Notifying [" + emp + "] about task [" + task.getDescription() + "].");
                            emObserver.taskSent(task);
                        } catch (ServicesException e) {
                            System.err.println("Error notifying boss " + e);
                        }
                    });
                }
            }
        }

        executor.shutdown();
    }

    @Override
    public synchronized void presentToWork(User user) throws ServicesException {
        if (presentAtWork.get(user.getId()) != null) {
            throw new ServicesException("Employee already present at work!");
        }
        LocalTime time = LocalTime.now();
        presentAtWork.put(user.getId(), time);
        notifyBossAboutPresence(user, time);
    }

    private void notifyBossAboutPresence(User user, LocalTime time) {
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);

        for(User boss : userRepository.findAll()){
            if (boss.getRole() == Role.BOSS) {
                EMObserver emObserver = loggedUsers.get(boss.getId());
                if (emObserver != null) {
                    executor.execute(() -> {
                        try {
                            System.out.println("Notifying [" + boss + "] about user [" + user + "].");
                            Map<User, LocalTime> map = new HashMap<>();
                            map.put(user, time);
                            emObserver.startedWork(map);
                        } catch (ServicesException e) {
                            System.err.println("Error notifying boss " + e);
                        }
                    });
                }
            }
        }

        executor.shutdown();
    }

    @Override
    public void leaveWork(User user) throws ServicesException {
        if (presentAtWork.get(user.getId()) == null) {
            throw new ServicesException("Employee isn't working!");
        }
        presentAtWork.remove(user.getId());
        notifyBossAboutLeaving(user);
    }

    private void notifyBossAboutLeaving(User user) {
        ExecutorService executor = Executors.newFixedThreadPool(defaultThreadsNo);

        for(User boss : userRepository.findAll()){
            if (boss.getRole() == Role.BOSS) {
                EMObserver emObserver = loggedUsers.get(boss.getId());
                if (emObserver != null) {
                    executor.execute(() -> {
                        try {
                            System.out.println("Notifying [" + boss + "] about user [" + user + "].");
                            emObserver.leftWork(user);
                        } catch (ServicesException e) {
                            System.err.println("Error notifying boss " + e);
                        }
                    });
                }
            }
        }

        executor.shutdown();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Map<User, LocalTime> getPresentAtWorkEmployees() throws ServicesException {
        List<User> users = userRepository.findAll();
        Map<User, LocalTime> present = new HashMap<>();
        for(User user: users) {
            if (presentAtWork.containsKey(user.getId())) {
                present.put(user, presentAtWork.get(user.getId()));
            }
        }
        return present;
    }

    @Override
    public List<Task> getAllTasksForOneEmployee(User employee) throws ServicesException {
        return taskRepository.getTasksForUser(employee);
    }

    @Override
    public void setEMObserver(EMObserver emObserver) {

    }
}
