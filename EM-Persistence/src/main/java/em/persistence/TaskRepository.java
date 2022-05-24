package em.persistence;

import em.model.Task;
import em.model.User;

import java.util.List;

public interface TaskRepository {
    Task addTask(Task task);
    List<Task> getTasksForUser(User user);
}
