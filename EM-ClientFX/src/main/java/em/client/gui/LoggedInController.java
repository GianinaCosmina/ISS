package em.client.gui;

import em.model.Role;
import em.model.Task;
import em.model.User;
import em.services.EMObserver;
import em.services.Service;
import em.services.ServicesException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LoggedInController implements EMObserver {
    private Service server;
    private User loggedInUser;

    ObservableList<User> allUsersObservableList = FXCollections.observableArrayList();

    @FXML
    private Label loggedAsLabel;
    @FXML
    private TextField nameTextField, usernameTextField, passwordTextField, roleTextField;
    @FXML
    private TableView<User> allUsersTableView;

    public void setServer(Service s) {
        this.server = s;
    }

    public void setUserInformation(User crtUser) throws ServicesException {
        this.loggedAsLabel.setText("Logged as: " + crtUser.getUsername());
        loggedInUser = crtUser;
    }

    public void initialize(Service s) throws ServicesException {
        setServer(s);
    }

    public void initializeEmployeeWindow() {

    }

    public void initializeAdminWindow() throws ServicesException {
        TableColumn<User, String> nameColumn = new TableColumn<>("Name");
        TableColumn<User, String> usernameColumn = new TableColumn<>("Username");
        TableColumn<User, String> roleColumn = new TableColumn<>("Role");

        allUsersTableView.getColumns().addAll(nameColumn, usernameColumn, roleColumn);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadDataFlightTableView();

        allUsersTableView.setItems(allUsersObservableList);
        allUsersTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadDataFlightTableView() throws ServicesException {
        allUsersObservableList.clear();
        for(User user : server.findAllUsers()) {
            if (!Objects.equals(user.getUsername(), loggedInUser.getUsername()))
            allUsersObservableList.add(user);
        }
    }

    public void initializeBossWindow() {

    }

    @Override
    public void sentTask(User user, Task task) throws ServicesException {

    }

    @Override
    public void startedWork(User user) throws ServicesException {

    }

    public void closeButtonOnAction() {
        try {
            server.logout(loggedInUser, this);
            System.exit(0);
        } catch (ServicesException e) {
            System.out.println("Logout error " + e);
        }
    }

    public void addUserAccountOnAction() {
        if (!nameTextField.getText().isBlank() && !usernameTextField.getText().isBlank()
                && !roleTextField.getText().isBlank() && !passwordTextField.getText().isBlank()) {
            List<String> roles = new ArrayList<>(Arrays.asList("ADMIN", "EMPLOYEE", "BOSS"));
            try {
                if (roles.contains(roleTextField.getText())) {
                    switch (roleTextField.getText()) {
                        case "ADMIN" -> {
                            User u = new User(nameTextField.getText(), usernameTextField.getText(), passwordTextField.getText(), Role.ADMIN);
                            server.addUserAccount(u);
                            allUsersObservableList.add(u);
                        }
                        case "EMPLOYEE" -> {
                            User u = new User(nameTextField.getText(), usernameTextField.getText(), passwordTextField.getText(), Role.EMPLOYEE);
                            server.addUserAccount(u);
                            allUsersObservableList.add(u);
                        }
                        case "BOSS" -> {
                            User u = new User(nameTextField.getText(), usernameTextField.getText(), passwordTextField.getText(), Role.BOSS);
                            server.addUserAccount(u);
                            allUsersObservableList.add(u);
                        }
                    }
                    nameTextField.setText("");
                    usernameTextField.setText("");
                    passwordTextField.setText("");
                    roleTextField.setText("");
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Account created with success!", ButtonType.OK);
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid role! Role should be one of: ADMIN/EMPLOYEE/BOSS", ButtonType.OK);
                    alert.show();
                }
            } catch (ServicesException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                alert.show();
            }
        }
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Complete all required inputs first!", ButtonType.OK);
            alert.show();
        }
    }

    public void modifyUserAccountOnAction() {
        if (!nameTextField.getText().isBlank() && !usernameTextField.getText().isBlank()
                && !roleTextField.getText().isBlank() && !passwordTextField.getText().isBlank()) {
            List<String> roles = new ArrayList<>(Arrays.asList("ADMIN", "EMPLOYEE", "BOSS"));
            try {
                if (roles.contains(roleTextField.getText())) {
                    User user = allUsersTableView.getSelectionModel().getSelectedItem();
                    if (user != null) {
                        user.setName(nameTextField.getText());
                        user.setUsername(usernameTextField.getText());
                        user.setPassword(passwordTextField.getText());
                        switch (roleTextField.getText()) {
                            case "EMPLOYEE" -> user.setRole(Role.EMPLOYEE);
                            case "ADMIN" -> user.setRole(Role.ADMIN);
                            case "BOSS" -> user.setRole(Role.BOSS);
                        }
                        server.modifyUserAccount(user);
                        nameTextField.setText("");
                        usernameTextField.setText("");
                        passwordTextField.setText("");
                        roleTextField.setText("");
                        for (int index = 0; index < allUsersObservableList.size(); index++) {
                            if (Objects.equals(allUsersObservableList.get(index).getId(), user.getId())) {
                                allUsersObservableList.set(index, user);
                                break;
                            }
                        }
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Account modified with success!", ButtonType.OK);
                        alert.show();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "Select a user first!", ButtonType.OK);
                        alert.show();
                    }
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid role! Role should be one of: ADMIN/EMPLOYEE/BOSS", ButtonType.OK);
                    alert.show();
                }
            } catch (ServicesException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Complete all required inputs first!", ButtonType.OK);
            alert.show();
        }
    }

    public void deleteUserAccountOnAction() {
        try {
            User user = allUsersTableView.getSelectionModel().getSelectedItem();
            if (user != null) {
                server.deleteUserAccount(user);
                allUsersObservableList.remove(user);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Account deleted with success!", ButtonType.OK);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Select a user first!", ButtonType.OK);
                alert.show();
            }
        }
        catch (ServicesException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK);
            alert.show();
        }
    }
}
