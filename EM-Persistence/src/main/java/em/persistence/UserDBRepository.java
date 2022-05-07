package em.persistence;

import em.model.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;

public class UserDBRepository implements UserRepository {
    private SessionFactory sessionFactory;

    public UserDBRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User findUserByUsername(String username) {
        User user = null;
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                user = session.createQuery("from User where username='" + username + "'", User.class)
                        .setMaxResults(1)
                        .uniqueResult();
                tx.commit();
            }
            catch (RuntimeException ex) {
                System.err.println("Login error: " + ex.getMessage());
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
        return user;
    }

    @Override
    public User addUser(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.save(user);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Add user error: " + ex.getMessage());
                if (tx != null)
                    tx.rollback();
                return null;
            }
            return user;
        }
    }

    @Override
    public User modifyUser(User user) {
        User new_user = null;
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.update(user);
                new_user = user;
                tx.commit();
            }
            catch(RuntimeException ex){
                System.err.println("Update error: " + ex.getMessage());
                if (tx != null) {
                    tx.rollback();
                }
            }
            return new_user;
        }
    }

    @Override
    public void deleteUser(User user) {
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                session.delete(user);
                tx.commit();
            } catch (RuntimeException ex) {
                System.err.println("Delete error: " + ex.getMessage());
                if (tx != null)
                    tx.rollback();
            }
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = null;
        try(Session session = sessionFactory.openSession()) {
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                users = session.createQuery("from User", User.class).list();
                tx.commit();
            }
            catch (RuntimeException ex) {
                System.err.println("Find all error: " + ex.getMessage());
                if (tx != null) {
                    tx.rollback();
                }
            }
        }
        return users;
    }
}
