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


public class ClientRpcWorker implements Runnable, EMObserver {
    private Service server;
    private Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    private static Response okResponse = new Response.Builder().type(ResponseType.OK).build();

    public ClientRpcWorker(Service server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(connected) {
            try {
                Object request = input.readObject();
                Response response = handleRequest((Request)request);
                if (response != null){
                    sendResponse(response);
                }
            } catch (IOException | ClassNotFoundException | ServicesException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
    }

    private Response handleRequest(Request request) throws ServicesException {
        Response response = null;

        if (request.type() == RequestType.LOGIN){
            System.out.println("Login request..." + request.type());
            User user = (User) request.data();
            try {
                User usr = server.login(user, this);
                return new Response.Builder().type(ResponseType.LOGGED_IN).data(usr).build();
            } catch (ServicesException e) {
                connected = false;
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.LOGOUT){
            System.out.println("Logout request..." + request.type());
            User usr = (User) request.data();
            try {
                server.logout(usr, this);
                connected = false;
                return new Response.Builder().type(ResponseType.LOGGED_OUT).data(usr).build();

            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.ADD_USER_ACCOUNT){
            System.out.println("Add user account request..." + request.type());
            User usr = (User) request.data();
            try {
                server.addUserAccount(usr);
                return new Response.Builder().type(ResponseType.ACCOUNT_ADDED).data(usr).build();

            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.GET_ALL_USERS){
            System.out.println("Get all users request..." + request.type());
            return new Response.Builder().type(ResponseType.GOT_ALL_USERS).data(server.findAllUsers()).build();
        }
        if (request.type() == RequestType.MODIFY_USER_ACCOUNT){
            System.out.println("Modify user account request..." + request.type());
            User usr = (User) request.data();
            try {
                server.modifyUserAccount(usr);
                return new Response.Builder().type(ResponseType.ACCOUNT_MODIFIED).data(usr).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.DELETE_USER_ACCOUNT){
            System.out.println("Delete user account request..." + request.type());
            User usr = (User) request.data();
            try {
                server.deleteUserAccount(usr);
                return new Response.Builder().type(ResponseType.ACCOUNT_DELETED).data(usr).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.PRESENT_TO_WORK){
            System.out.println("Present to work request..." + request.type());
            User usr = (User) request.data();
            try {
                server.presentToWork(usr);
                return new Response.Builder().type(ResponseType.ADDED_AS_PRESENT).data(usr).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.LEAVE_WORK){
            System.out.println("Leave work request..." + request.type());
            User usr = (User) request.data();
            try {
                server.leaveWork(usr);
                return new Response.Builder().type(ResponseType.GONE_FROM_WORK).data(usr).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.GET_PRESENT_EMPLOYEES){
            System.out.println("Get present employees request..." + request.type());
            try {
                Map<User, LocalTime> present = server.getPresentAtWorkEmployees();
                return new Response.Builder().type(ResponseType.GOT_PRESENT_EMPLOYEES).data(present).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.SEND_TASK){
            System.out.println("Send task request..." + request.type());
            Task task = (Task) request.data();
            try {
                Task sendTask = server.sendTask(task);
                return new Response.Builder().type(ResponseType.TASK_SENT).data(sendTask).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }
        if (request.type() == RequestType.GET_TASKS_FOR_EMPLOYEE){
            System.out.println("Get tasks for one employee request..." + request.type());
            User user = (User) request.data();
            try {
                List<Task> tasks = server.getAllTasksForOneEmployee(user);
                return new Response.Builder().type(ResponseType.GOT_TASKS_FOR_EMPLOYEE).data(tasks).build();
            } catch (ServicesException e) {
                return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
            }
        }

        return response;
    }

    private void sendResponse(Response response) throws IOException{
        System.out.println("Sending response: " + response);
        output.writeObject(response);
        output.flush();
    }

    @Override
    public void taskSent(Task task) {
        Response resp = new Response.Builder().type(ResponseType.TASK_SENT_UPDATE).data(task).build();
        try {
            sendResponse(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startedWork(Map<User, LocalTime> map) {
        Response resp = new Response.Builder().type(ResponseType.ADDED_AS_PRESENT_UPDATE).data(map).build();
        try {
            sendResponse(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leftWork(User user) {
        Response resp = new Response.Builder().type(ResponseType.GONE_FROM_WORK_UPDATE).data(user).build();
        try {
            sendResponse(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
