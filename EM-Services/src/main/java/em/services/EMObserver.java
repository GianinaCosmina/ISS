package em.services;

import em.model.Task;
import em.model.User;

import java.time.LocalTime;
import java.util.Map;

public interface EMObserver {
    void taskSent(Task task) throws ServicesException;
    void startedWork(Map<User, LocalTime> user) throws ServicesException;
    void leftWork(User user) throws ServicesException;
}
