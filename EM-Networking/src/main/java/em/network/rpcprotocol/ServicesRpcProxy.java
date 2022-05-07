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
import java.util.List;
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

    }

    private boolean isUpdate(Response response) {
        return false;
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
