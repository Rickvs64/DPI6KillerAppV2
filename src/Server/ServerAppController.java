package Server;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.MessageTypes.ChatMessage;
import Shared.MessageTypes.ClientInitMessage;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;

public class ServerAppController implements ISubscriber {

    private String startupURL = "QiFBgtgUtfw";
    private String log = "";
    private Connector connector;

    @FXML
    private WebView webview_YouTubeVideo;

    @FXML
    private Text lbl_ChatWindow;

    public ServerAppController() {

    }

    public void initialize() {
        if (startupURL != "") {
            playNewVideo(startupURL);
        }

        initConnector();
    }

    // Initialize ActiveMQ connection and define listen channels
    private void initConnector() {
        List<String> listenQueues = new ArrayList<String>();
        listenQueues.add("ChatMessagesFromBridgeForServer");
        listenQueues.add("SongRequestsFromBridgeForServer");
        listenQueues.add("InitsFromBridge");

        List<String> listenTopics = new ArrayList<String>();

        connector = new Connector(this, listenQueues, listenTopics);
    }

    private void playNewVideo(String videoID) {
        // Todo: Can we hide the title and playlist buttons too?
        String url = "https://www.youtube.com/embed/" + videoID + "?controls=0&autoplay=1";
        webview_YouTubeVideo.getEngine().load(url);
    }

    // Show new message in text window
    private void log(String message) {
        System.out.println(message);
        log += message + "\n";
        lbl_ChatWindow.setText(log);
    }

    // Received a ClientInitMessage
    private void handleClientInitMessage(ClientInitMessage msg) {
        log("(" + msg.getSentFrom() + " has joined the chat.)");
    }

    // Received a ChatMessage
    private void handleChatMessage(ChatMessage msg) {
        log(msg.getSentFrom() + ": " + msg.getChatMessage());
    }

    @Override
    public void onMessageReceived(NetworkMessage message) {
        switch (message.getClass().getSimpleName()) {
            case "ClientInitMessage":
                handleClientInitMessage((ClientInitMessage) message);
                break;

            case "ChatMessage":
                handleChatMessage((ChatMessage) message);
                break;

            default:
                // ...
                break;
        }
    }
}
