package em.network.rpcprotocol;

import em.model.Task;
import em.model.User;
import em.services.EMObserver;
import em.services.Service;
import em.services.ServicesException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ServicesRpcProxy implements Service {
    private String host;
    private int port;

    private EMObserver client;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;

    private final BlockingQueue<Response> queue_responses;
    private volatile boolean finished;

    public ServicesRpcProxy(String host, int port) {
        this.host = host;
        this.port = port;
        queue_responses = new LinkedBlockingQueue<>();
    }


    @Override
    public User login(User user, EMObserver client) throws ServicesException {
        initializeConnection();

        Request req = new Request.Builder().type(RequestType.LOGIN).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            closeConnection();
            throw new ServicesException(err);
        }
        // LOGGED_IN Response
        this.client = client;
        return (User) response.data();
    }

    @Override
    public void logout(User user, EMObserver client) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.LOGOUT).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        closeConnection();
        if (response.type() == ResponseType.ERROR){
            String err=response.data().toString();
            throw new ServicesException(err);
        }
    }

    @Override
    public User addUserAccount(User user) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.ADD_USER_ACCOUNT).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            throw new ServicesException(err);
        }
        return (User) response.data();
    }

    @Override
    public User modifyUserAccount(User new_user) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.MODIFY_USER_ACCOUNT).data(new_user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            throw new ServicesException(err);
        }
        return (User) response.data();
    }

    @Override
    public User deleteUserAccount(User user) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.DELETE_USER_ACCOUNT).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            throw new ServicesException(err);
        }
        return (User) response.data();
    }

    @Override
    public Task sendTask(Task task) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.SEND_TASK).data(task).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            throw new ServicesException(err);
        }
        return (Task) response.data();
    }

    @Override
    public void presentToWork(User user) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.PRESENT_TO_WORK).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            String err = response.data().toString();
            throw new ServicesException(err);
        }
    }

    @Override
    public void leaveWork(User user) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.LEAVE_WORK).data(user).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR) {
            String err = response.data().toString();
            throw new ServicesException(err);
        }
    }

    @Override
    public List<User> findAllUsers() throws ServicesException {
        Request req = new Request.Builder().type(RequestType.GET_ALL_USERS).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            closeConnection();
            throw new ServicesException(err);
        }
        // ACCOUNT_ADDED Response
        return (List<User>) response.data();
    }

    @Override
    public Map<User, LocalTime> getPresentAtWorkEmployees() throws ServicesException {
        Request req = new Request.Builder().type(RequestType.GET_PRESENT_EMPLOYEES).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            closeConnection();
            throw new ServicesException(err);
        }
        return (Map<User, LocalTime>) response.data();
    }

    @Override
    public List<Task> getAllTasksForOneEmployee(User employee) throws ServicesException {
        Request req = new Request.Builder().type(RequestType.GET_TASKS_FOR_EMPLOYEE).data(employee).build();
        sendRequest(req);
        Response response = readResponse();
        if (response.type() == ResponseType.ERROR){
            String err = response.data().toString();
            closeConnection();
            throw new ServicesException(err);
        }
        // ACCOUNT_ADDED Response
        return (List<Task>) response.data();
    }

    @Override
    public void setEMObserver(EMObserver emObserver) {
        this.client = emObserver;
    }

    private void closeConnection() {
        finished = true;
        try {
            input.close();
            output.close();
            connection.close();
            client = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(Request request) throws ServicesException {
        try {
            output.writeObject(request);
            output.flush();
        } catch (IOException e) {
            throw new ServicesException("Error sending object " + e);
        }

    }

    private Response readResponse() {
        Response response = null;
        try {
            response = queue_responses.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    private void initializeConnection() {
        try {
            connection = new Socket(host,port);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            finished = false;
            startReader();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startReader(){
        Thread tw = new Thread(new ReaderThread());
        tw.start();
    }


    private void handleUpdate(Response response) {
        if (response.type() == ResponseType.ADDED_AS_PRESENT_UPDATE) {
            Map<User, LocalTime> map = (Map<User, LocalTime>) response.data();
            try {
                client.startedWork(map);
            } catch (ServicesException e) {
                e.printStackTrace();
            }
        }
        if (response.type() == ResponseType.GONE_FROM_WORK_UPDATE) {
            User user = (User) response.data();
            try {
                client.leftWork(user);
            } catch (ServicesException e) {
                e.printStackTrace();
            }
        }
        if (response.type() == ResponseType.TASK_SENT_UPDATE) {
            Task task = (Task) response.data();
            try {
                client.taskSent(task);
            } catch (ServicesException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isUpdate(Response response) {
        return response.type() == ResponseType.ADDED_AS_PRESENT_UPDATE ||
                response.type() == ResponseType.GONE_FROM_WORK_UPDATE ||
                response.type() == ResponseType.TASK_SENT_UPDATE;
    }

    private class ReaderThread implements Runnable {
        public void run() {
            while(!finished) {
                try {
                    Object response = input.readObject();
                    System.out.println("Response received: " + response);
                    if (isUpdate((Response) response)) {
                        handleUpdate((Response) response);
                    }
                    else {
                        try {
                            queue_responses.put((Response) response);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Reading error " + e);
                }
            }
        }
    }
}
