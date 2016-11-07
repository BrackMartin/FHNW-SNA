package ch.fhnw.sna.mailcollector.gui;

import ch.fhnw.sna.mailcollector.collector.MailCollector;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class CredentialsController {

    @FXML
    public TextField txtUsername;

    @FXML
    public PasswordField txtPassword;

    @FXML
    public Button btnLogin;

    @FXML
    public Label lblErrorMsg, lblSuccessMsg, lblLoading;

    @FXML
    public HBox hbError, hbSuccess;

    /**
     * Initialize elements on view
     */
    public void initialize() {
        initializeFieldActions();
        txtUsername.setText("matthias.langhard@students.fhnw.ch");
    }

    /**
     * Adds click listener for button
     */
    private void initializeFieldActions() {
        btnLogin.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            hbError.getStyleClass().removeAll("show");

            if (txtUsername.getText().equals("") || txtPassword.getText().equals("")) {
                lblErrorMsg.setText("Please fill in your credentials.");
                hbError.getStyleClass().add("show");
                return;
            }

            toggleForm(true);
            downloadMails(txtUsername.getText(), txtPassword.getText());
        });
    }

    /**
     * Starts process of downloading the mails
     * @param username
     * @param password
     */
    private void downloadMails(String username, String password) {
        try {
            MailCollector mailCollector = new MailCollector(username, password);
            lblLoading.getStyleClass().add("show");

            Service<String> service = mailCollector.downloadMailsAsync();
            service.start();
            service.setOnSucceeded(event -> {
                lblLoading.getStyleClass().removeAll("show");
                hbError.getStyleClass().removeAll("show");
                hbSuccess.getStyleClass().add("show");
                lblSuccessMsg.setText("Finished collecting successfully");
                System.out.println(event);
                toggleForm(false);
            });

            service.setOnFailed(event -> {
                lblLoading.getStyleClass().removeAll("show");
                hbSuccess.getStyleClass().removeAll("show");
                hbError.getStyleClass().add("show");
                lblErrorMsg.setText(event.getSource().getException().getMessage());
                System.out.println(event.getSource().getException().getMessage());
                toggleForm(false);
            });

        } catch (Exception exception) {
            hbSuccess.getStyleClass().removeAll("show");
            hbError.getStyleClass().add("show");
            lblErrorMsg.setText("An error has occured. Please check log.");
            System.out.println(exception.getStackTrace());
            toggleForm(false);
        }
    }

    /**
     * Disables / Enables form
     * @param disabled
     */
    private void toggleForm(boolean disabled) {
        txtPassword.setDisable(disabled);
        txtUsername.setDisable(disabled);
        btnLogin.setDisable(disabled);
    }
}