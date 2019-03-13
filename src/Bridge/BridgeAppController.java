package Bridge;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.MessageTypes.ChatMessage;
import Shared.MessageTypes.ClientInitMessage;
import Shared.MessageTypes.SongRequest;
import Shared.MessageTypes.Whisper;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

public class BridgeAppController implements ISubscriber {

    private String log = "";
    private Connector connector;

    @FXML
    private Text lbl_Logger;

    public BridgeAppController() {
    }

    public void initialize() {
        initConnector();
        log("Initialized connector, ready to go!");
    }

    // Initialize ActiveMQ connection and define listen channels
    private void initConnector() {
        List<String> listenQueues = new ArrayList<String>();
        listenQueues.add("ChatMessagesFromClient");
        listenQueues.add("SongRequestsFromClient");
        listenQueues.add("WhispersFromClient");
        listenQueues.add("InitsFromClient");

        List<String> listenTopics = new ArrayList<String>();

        connector = new Connector(this, listenQueues, listenTopics);
    }

    // Display a string in both console and scrollable text field
    private void log(String message) {
        System.out.println(message);
        log += message + "\n";
        lbl_Logger.setText(log);
    }

    // Received a ClientInitMessage
    private void handleClientInitMessage(ClientInitMessage msg) throws JMSException {
        log("User \"" + msg.getSentFrom() + "\" has joined the lobby.");

        connector.sendMessageToQueue(msg, "InitsFromBridge");
        log("Server has been notified.");
    }

    // Received a ChatMessage
    private void handleChatMessage(ChatMessage msg) throws JMSException {
        log("User \"" + msg.getSentFrom() + "\" sent a message: " + msg.getChatMessage());

        // First send to the server
        connector.sendMessageToQueue(msg, "ChatMessagesFromBridgeForServer");
        log("Server has been notified.");

        // Then send to every client
        connector.sendMessageToTopic(msg, "ChatMessagesFromBridgeForClient");
        log("Clients have been notified.");
    }

    // Received a Whisper (private message)
    private void handleWhisper(Whisper msg) throws JMSException {
        // We'll log that a whisper has been sent but will NOT show the actual private content here
        log("User \"" + msg.getSentFrom() + "\" sent a whisper to \"" + msg.getSentTo() + "\".");

        // Doesn't need to be sent to the server
        // But will have to be sent to one specific client/username
        connector.sendMessageToQueue(msg, "WhispersFromBridge_" + msg.getSentTo());
        log(msg.getSentTo() + " has been notified.");
    }

    // Received a SongRequest
    private void handleSongRequest(SongRequest msg) throws JMSException {
        // Let's first log that a song request has been received
        log("User \"" + msg.getSentFrom() + "\" sent a song request with ID: " + msg.getSongID());

        // First send to the server
        connector.sendMessageToQueue(msg, "SongRequestsFromBridgeForServer");
        log("Server has been notified.");

        // Then send to every client
        connector.sendMessageToTopic(msg, "SongRequestsFromBridgeForClient");
        log("Clients have been notified.");
    }

    @Override
    public void onMessageReceived(NetworkMessage message) throws JMSException {
        switch (message.getClass().getSimpleName()) {
            case "ClientInitMessage":
                handleClientInitMessage((ClientInitMessage) message);
                break;

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
