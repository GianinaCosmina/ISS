package em.services;

import em.model.Task;
import em.model.User;

public interface EMObserver {
    void sentTask(User user, Task task) throws ServicesException;
    void startedWork(User user) throws ServicesException;
}
