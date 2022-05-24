package em.persistence;


import em.model.Task;
import em.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TaskDBRepository implements TaskRepository {
    private SessionFactory sessionFactory;

    public TaskDBRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Task addTask(Task task) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(task);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Add task error: " + ex.getMessage());
                if (tx != null)
                    tx.rollback();
                return null;
            }
            return task;
        }
    }

    @Override
    public List<Task> getTasksForUser(User user) {
        List<Task> tasks = new ArrayList<>();
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                tasks = session.createQuery("from Task where user=" + user.getId(), Task.class).list();
                tx.commit();
            }
            catch (RuntimeException ex) {
                System.err.println("Find all error: " + ex.getMessage());
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
        return tasks;
    }
}
