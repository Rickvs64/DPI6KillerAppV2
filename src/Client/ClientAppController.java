package Client;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.MessageTypes.ChatMessage;
import Shared.MessageTypes.ClientInitMessage;
import Shared.MessageTypes.SongRequest;
import Shared.MessageTypes.Whisper;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

public class ClientAppController implements ISubscriber {

    private String username = "Undefined";
    private String log = "";
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
        listenTopics.add("ChatMessagesFromBridgeForClient");
        listenTopics.add("SongRequestsFromBridgeForClient");

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

    // Display a string in chat log
    private void log(String message) {
        log += message + "\n";
        lbl_ChatHistory.setText(log);
    }

    // Determine if a message is a whisper (private message)
    private boolean isWhisper(String msg) {
        return (msg.startsWith("/w ") || msg.startsWith("/W "));
    }

    // Send a private whisper
    private void trySendWhisper(String msg) throws JMSException {
        // Attempting to retrieve: /w (([USERNAME])) [Content]
        Integer start = msg.indexOf(" ") + 1;
        Integer end = msg.indexOf(" ", start+1); // Search from start+1
        String intendedReceiver = msg.substring(start, end);

        // Attempting to retrieve: /w [Username] (([CONTENT]))
        start = end + 1;
        String content = msg.substring(start);

        Whisper whisper = new Whisper(username, intendedReceiver, content);
        connector.sendMessageToQueue(whisper, "WhispersFromClient");

        // Interesting detail: this user will not RECEIVE this whisper but we do want to show it in chat history
        // So we'll be adding this one manually, functions as if we received it
        log(whisper.getSentFrom() + "->" + whisper.getSentTo() + ": " + whisper.getChatMessage());
    }

    // Send a public chat message
    private void trySendChat(String msg) throws JMSException {
        connector.sendMessageToQueue(new ChatMessage(username, msg), "ChatMessagesFromClient");
    }

    // Received a ChatMessage
    private void handleChatMessage(ChatMessage msg) {
        log(msg.getSentFrom() + ": " + msg.getChatMessage());
    }

    // Received a Whisper (private message)
    private void handleWhisper(Whisper msg) {
        log(msg.getSentFrom() + "->" + msg.getSentTo() + ": " + msg.getChatMessage());
    }

    // Received a SongRequest
    private void handleSongRequest(SongRequest msg) {
        log("(" + msg.getSentFrom() + ") has requested a song with ID: " + msg.getSongID());
    }

    // Send a song request
    private void trySendSongRequest(String songID) throws JMSException {
        connector.sendMessageToQueue(new SongRequest(username, songID), "SongRequestsFromClient");
    }


    // Username submit button pressed
    @FXML
    private void submitUsername() throws JMSException {
        if (!txt_Username.getText().trim().isEmpty() && txt_Username.getText().trim() != "") {
            // First trim (should be unnecessary but still)
            username = txt_Username.getText().trim();

            // Then remove ALL whitespaces (necessary for whisper functionality)
            username = username.replaceAll("\\s","");

            initConnector();
            connector.sendMessageToQueue(new ClientInitMessage(username), "InitsFromClient");
            setControlsEnabled(true);
            txt_Username.setText(username); // Since it might have been altered by Trim() or Regex
        }
    }

    // Chat message / whisper submit button pressed
    @FXML
    private void submitMessage() throws JMSException {
        String actualMessage = txt_Chat.getText().trim();

        if (isWhisper(actualMessage)) {
            trySendWhisper(actualMessage);
            txt_Chat.setText("");
        }
        else {
            trySendChat(actualMessage);
            txt_Chat.setText("");
        }
    }

    // Song request submit button pressed
    @FXML
    private void submitSongRequest() throws JMSException {
        String actualID = txt_SongRequest.getText().trim();

        trySendSongRequest(actualID);
        txt_SongRequest.setText("");
    }

    @Override
    public void onMessageReceived(NetworkMessage message) {
        switch (message.getClass().getSimpleName()) {
            case "ChatMessage":
                handleChatMessage((ChatMessage) message);
                break;

            case "Whisper":
                handleWhisper((Whisper) message);
                break;

            case "SongRequest":
                handleSongRequest((SongRequest) message);
                break;

            default:
                // ...
                break;
        }
    }
}
