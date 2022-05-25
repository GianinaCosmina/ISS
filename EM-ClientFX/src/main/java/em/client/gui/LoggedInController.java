package em.client.gui;

import em.model.Role;
import em.model.Task;
import em.model.User;
import em.services.EMObserver;
import em.services.Service;
import em.services.ServicesException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LoggedInController implements EMObserver {
    private Service server;
    private User loggedInUser;

    ObservableList<User> allUsersObservableList = FXCollections.observableArrayList();
    ObservableList<Map.Entry<User, LocalTime>> presentEmployeesObservableList = FXCollections.observableArrayList();
    ObservableList<String> myTasksObservableList = FXCollections.observableArrayList();
    @FXML
    private Label loggedAsLabel, statusLabel;
    @FXML
    private TextField nameTextField, usernameTextField, passwordTextField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private TableView<User> allUsersTableView;
    @FXML
    private TableView<Map.Entry<User, LocalTime>> presentEmployeesTableView;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private ListView<String> myTasksListView;

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

    public void initializeEmployeeWindow() throws ServicesException {
        this.statusLabel.setText("Status: Gone from work");
        for(Task task : server.getAllTasksForOneEmployee(loggedInUser)) {
            myTasksObservableList.add(task.getDescription());
        }
        myTasksListView.setItems(myTasksObservableList);
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

        roleComboBox.getItems().addAll(
                "ADMIN",
                "EMPLOYEE",
                "BOSS"
        );
    }

    private void loadDataFlightTableView() throws ServicesException {
        allUsersObservableList.clear();
        for(User user : server.findAllUsers()) {
            if (!Objects.equals(user.getUsername(), loggedInUser.getUsername()))
            allUsersObservableList.add(user);
        }
    }

    public void initializeBossWindow() throws ServicesException {
        TableColumn<Map.Entry<User, LocalTime>, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey().getName()));
        TableColumn<Map.Entry<User, LocalTime>, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getKey().getUsername()));
        TableColumn<Map.Entry<User, LocalTime>, String> loginTimeColumn = new TableColumn<>("Login time");
        loginTimeColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().getValue().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

        presentEmployeesTableView.getColumns().addAll(nameColumn, usernameColumn, loginTimeColumn);

        presentEmployeesObservableList.clear();
        presentEmployeesObservableList.addAll(server.getPresentAtWorkEmployees().entrySet());

        presentEmployeesTableView.setItems(presentEmployeesObservableList);
        presentEmployeesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override
    public void taskSent(Task task) throws ServicesException {
        Platform.runLater( () -> {
            myTasksObservableList.add(task.getDescription());
        });
    }

    @Override
    public void startedWork(Map<User, LocalTime> map) {
        Platform.runLater( () -> {
            presentEmployeesObservableList.addAll(map.entrySet());
        });
    }

    @Override
    public void leftWork(User user) {
        Platform.runLater( () -> {
            for (int index = 0; index < presentEmployeesObservableList.size(); index++) {
                if (Objects.equals(presentEmployeesObservableList.get(index).getKey().getId(), user.getId())) {
                    presentEmployeesObservableList.remove(presentEmployeesObservableList.get(index));
                }
            }
        });
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
                && !roleComboBox.getSelectionModel().isEmpty() && !passwordTextField.getText().isBlank()) {
            List<String> roles = new ArrayList<>(Arrays.asList("ADMIN", "EMPLOYEE", "BOSS"));
            try {
                if (roles.contains(roleComboBox.getSelectionModel().getSelectedItem())) {
                    switch (roleComboBox.getSelectionModel().getSelectedItem()) {
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
                    roleComboBox.getSelectionModel().clearSelection();
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
                && !roleComboBox.getSelectionModel().isEmpty() && !passwordTextField.getText().isBlank()) {
            List<String> roles = new ArrayList<>(Arrays.asList("ADMIN", "EMPLOYEE", "BOSS"));
            try {
                if (roles.contains(roleComboBox.getSelectionModel().getSelectedItem())) {
                    User user = allUsersTableView.getSelectionModel().getSelectedItem();
                    if (user != null) {
                        user.setName(nameTextField.getText());
                        user.setUsername(usernameTextField.getText());
                        user.setPassword(passwordTextField.getText());
                        switch (roleComboBox.getSelectionModel().getSelectedItem()) {
                            case "EMPLOYEE" -> user.setRole(Role.EMPLOYEE);
                            case "ADMIN" -> user.setRole(Role.ADMIN);
                            case "BOSS" -> user.setRole(Role.BOSS);
                        }
                        server.modifyUserAccount(user);
                        nameTextField.setText("");
                        usernameTextField.setText("");
                        passwordTextField.setText("");
                        roleComboBox.getSelectionModel().clearSelection();
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

    public void presentButtonOnClick() {
        try {
            server.presentToWork(loggedInUser);
            this.statusLabel.setText("Status: Present to work");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Good luck!", ButtonType.OK);
            alert.show();
        }
        catch (ServicesException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.show();
        }
    }

    public void leaveWorkButtonOnClick() {
        try {
            server.leaveWork(loggedInUser);
            this.statusLabel.setText("Status: Gone from work");
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Have a nice day!", ButtonType.OK);
            alert.show();
        }
        catch (ServicesException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.OK);
            alert.show();
        }
    }

    public void sendTaskButtonOnAction() throws ServicesException {
        Map.Entry<User, LocalTime> map = presentEmployeesTableView.getSelectionModel().getSelectedItem();
        if (map != null) {
            if (!descriptionTextArea.getText().isBlank()) {
                Task task = new Task(descriptionTextArea.getText(), map.getKey());
                server.sendTask(task);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Task sent successfully!", ButtonType.OK);
                alert.show();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Write a description first!", ButtonType.OK);
                alert.show();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Select an employee first!", ButtonType.OK);
            alert.show();
        }
    }
}
