import em.network.utils.AbstractServer;
import em.network.utils.ServerException;
import em.network.utils.TARpcConcurrentServer;
import em.persistence.TaskDBRepository;
import em.persistence.TaskRepository;
import em.persistence.UserDBRepository;
import em.persistence.UserRepository;
import em.server.ServiceImpl;
import em.services.Service;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import java.io.IOException;
import java.util.Properties;

public class StartServer {
    private static int defaultPort = 55555;
    private static SessionFactory sessionFactory;

    public static void main(String[] args) {
        initialize();
        System.out.println("Starting server...");
        Properties serverProps = new Properties();

        try {
            serverProps.load(StartServer.class.getResourceAsStream("/em.server.properties"));
            System.out.println("Server properties set. ");
            serverProps.list(System.out);
        } catch (IOException e) {
            System.err.println("Cannot find em.server.properties " + e);
            return;
        }

        UserRepository userRepository = new UserDBRepository(sessionFactory);
        TaskRepository taskRepository = new TaskDBRepository(sessionFactory);
        Service service = new ServiceImpl(userRepository, taskRepository);

        int taServerPort = defaultPort;
        try {
            taServerPort = Integer.parseInt(serverProps.getProperty("em.server.port"));
        } catch (NumberFormatException nef) {
            System.err.println("Wrong  Port Number" + nef.getMessage());
            System.err.println("Using default port " + defaultPort);
        }
        System.out.println("Starting server on port: " + taServerPort);
        AbstractServer server = new TARpcConcurrentServer(taServerPort, service);

        try {
            server.start();
        } catch (ServerException e) {
            System.err.println("Error starting the server" + e.getMessage());
        } finally {
            try {
                server.stop();
            } catch(ServerException e){
                System.err.println("Error stopping server " + e.getMessage());
            }
        }
    }

    static void initialize() {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            System.err.println(">>>>> Exception: " + e);
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
