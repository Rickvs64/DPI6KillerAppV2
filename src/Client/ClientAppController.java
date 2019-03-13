package Client;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.MessageTypes.ClientInitMessage;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

public class ClientAppController implements ISubscriber {

    private String username;
    private Connector connector;

    @FXML
    private TextField txt_Username;

    @FXML
    private Button btn_SubmitUsername;

    @FXML
    private Text lbl_ChatHistory;

    @FXML
    private TextField txt_Chat;

    @FXML
    private Button btn_SubmitChat;

    @FXML
    private TextField txt_SongRequest;

    @FXML
    private Button btn_SubmitRequest;

    public ClientAppController() {
    }

    public void initialize() {
        // Not immediately initializing connector since the "whisper" channel depends on username

        // Immediately disable most controls until the user has defined a username
        setControlsEnabled(false);
    }

    // Initialize ActiveMQ connection and define listen channels
    private void initConnector() {
        List<String> listenQueues = new ArrayList<String>();
        listenQueues.add("WhispersFromBridge_" + username);     // Whispers only intended for this specific user

        List<String> listenTopics = new ArrayList<String>();
        listenTopics.add("ChatMessagesFromBridge");
        listenTopics.add("SongRequestsFromBridge");

        connector = new Connector(this, listenQueues, listenTopics);
    }

    // Enable/disable main controls
    private void setControlsEnabled(boolean enabled) {
        lbl_ChatHistory.setDisable(!enabled);
        txt_Chat.setDisable(!enabled);
        btn_SubmitChat.setDisable(!enabled);
        txt_SongRequest.setDisable(!enabled);
        btn_SubmitRequest.setDisable(!enabled);
        txt_Username.setDisable(enabled);
        btn_SubmitUsername.setDisable(enabled);
    }

    // Username submit button pressed
    @FXML
    private void submitUsername() throws JMSException {
        if (!txt_Username.getText().trim().isEmpty() && txt_Username.getText().trim() != "") {
            username = txt_Username.getText().trim();
            initConnector();
            connector.sendMessageToQueue(new ClientInitMessage(username), "InitsFromClient");
            setControlsEnabled(true);
        }
    }

    @Override
    public void onMessageReceived(NetworkMessage message) {

    }
}
