package em.client;

import em.client.gui.JavaFXUtils;
import em.client.gui.LoginController;
import em.network.rpcprotocol.ServicesRpcProxy;
import em.services.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Properties;


public class StartClientFX extends Application {
    private static int defaultChatPort = 55555;
    private static String defaultServer = "localhost";


    public void start(Stage primaryStage) throws Exception {
        System.out.println("Starting client...");
        Properties clientProps = new Properties();

        try {
            clientProps.load(StartClientFX.class.getResourceAsStream("/em.client.properties"));
            System.out.println("Client properties set. ");
            clientProps.list(System.out);
        } catch (IOException e) {
            System.err.println("Cannot find ta.client.properties " + e);
            return;
        }

        String serverIP = clientProps.getProperty("em.server.host", defaultServer);
        int serverPort = defaultChatPort;

        try {
            serverPort = Integer.parseInt(clientProps.getProperty("em.server.port"));
        } catch (NumberFormatException ex) {
            System.err.println("Wrong port number " + ex.getMessage());
            System.out.println("Using default port: " + defaultChatPort);
        }
        System.out.println("Using server IP " + serverIP);
        System.out.println("Using server port " + serverPort);

        Service server = new ServicesRpcProxy(serverIP, serverPort);

        // initialize client fx
        FXMLLoader loginLoader = new FXMLLoader(getClass().getClassLoader().getResource("login_view.fxml"));
        Parent loginRoot = loginLoader.load();
        LoginController loginController = loginLoader.getController();
        loginController.setServer(server);

        Scene scene = new Scene(loginRoot, 520, 400);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        JavaFXUtils.makeWindowDraggable(scene, primaryStage);
        JavaFXUtils.setScenePosition(scene, primaryStage);
        primaryStage.show();
    }
}


