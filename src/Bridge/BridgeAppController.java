package Bridge;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.MessageTypes.ClientInitMessage;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

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

    @Override
    public void onMessageReceived(NetworkMessage message) {
        switch (message.getClass().getSimpleName()) {
            case "ClientInitMessage":
                ClientInitMessage msg = (ClientInitMessage) message;
                log("User \"" + msg.getSentFrom() + "\" has joined the lobby.");
                break;

            default:
                // ...
                break;
        }
    }
}
