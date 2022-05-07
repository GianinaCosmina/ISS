package em.persistence;


import org.hibernate.SessionFactory;

public class TaskDBRepository implements TaskRepository {
    private SessionFactory sessionFactory;

    public TaskDBRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
