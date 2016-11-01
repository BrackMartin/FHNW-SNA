package ch.fhnw.sna.mailcollector.gui;

import ch.fhnw.sna.mailcollector.collector.MailCollector;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class CredentialsController {

    @FXML
    public TextField txtUsername;

    @FXML
    public PasswordField txtPassword;

    @FXML
    public Button btnLogin;

    public void initialize() {
        initializeFieldActions();
        txtUsername.setText("matthias.langhard@students.fhnw.ch");
    }

    private void initializeFieldActions() {
        btnLogin.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

            if(txtUsername.getText().equals("") ||txtPassword.getText().equals("")) {
                // ToDo error message
                return;
            }

            downloadMails(txtUsername.getText(), txtPassword.getText());
        });
    }

    private void downloadMails(String username, String password) {
        try {
            MailCollector mailCollector = new MailCollector(username, password);
            mailCollector.downloadMails();

        }catch(Exception exception) {
            System.out.println(exception.getMessage());
            // ToDo show error message
        }
    }
}