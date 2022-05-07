package em.client.gui;

import em.model.User;
import em.services.Service;
import em.services.ServicesException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoginController {
    private Service server;
    private LoggedInController loggedInController;
    private User crtUser;

    Parent mainTAParent;

    @FXML
    private Label loginMessageLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordPasswordField;

    public void setServer(Service s) {
        server = s;
    }

    public void setController(LoggedInController loggedInController) {
        this.loggedInController = loggedInController;
    }

    public void setParent(Parent parent) {
        this.mainTAParent = parent;
    }

    public void setCurrentUser(User user) {
        crtUser = user;
    }

    public void loginButtonOnAction(ActionEvent actionEvent) {
        if (!usernameTextField.getText().isBlank() && !passwordPasswordField.getText().isBlank()) {
            //Parent root;
            String usrname = usernameTextField.getText();
            String passwd = passwordPasswordField.getText();
            setCurrentUser(new User("", usrname, passwd, null));

            try {
                User user = server.login(crtUser, loggedInController);
                crtUser = user;

                FXMLLoader loggedInLoader;

                switch (user.getRole()) {
                    case EMPLOYEE -> loggedInLoader = new FXMLLoader(getClass().getClassLoader().getResource("employee_view.fxml"));
                    case BOSS ->  loggedInLoader = new FXMLLoader(getClass().getClassLoader().getResource("boss_view.fxml"));
                    case ADMIN ->  loggedInLoader = new FXMLLoader(getClass().getClassLoader().getResource("admin_view.fxml"));
                    default -> throw new IllegalStateException("Unexpected value: " + user.getRole());
                }

                Parent loggedInRoot = loggedInLoader.load();
                LoggedInController loggedInController = loggedInLoader.getController();
                loggedInController.initialize(server);
                loggedInController.setUserInformation(crtUser);

                switch (user.getRole()) {
                    case EMPLOYEE -> loggedInController.initializeEmployeeWindow();
                    case BOSS ->  loggedInController.initializeBossWindow();
                    case ADMIN ->  loggedInController.initializeAdminWindow();
                }

                setController(loggedInController);
                setParent(loggedInRoot);

                Stage stage = new Stage();
                Scene scene = new Scene(mainTAParent, 700, 400);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setScene(scene);
                JavaFXUtils.makeWindowDraggable(scene, stage);
                JavaFXUtils.setScenePosition(scene, stage);

                loggedInController.setUserInformation(crtUser);
                loggedInController.initialize(server);

                stage.show();
                ((Node) (actionEvent.getSource())).getScene().getWindow().hide();
            } catch (ServicesException | IOException ex) {
                System.out.println(ex.getMessage());
                loginMessageLabel.setText(ex.getMessage().replace("\n", ""));
            }
        }
        else {
            loginMessageLabel.setText("Please enter your info!");
        }
    }

    public void cancelButtonOnAction() {
        System.exit(0);
    }
}