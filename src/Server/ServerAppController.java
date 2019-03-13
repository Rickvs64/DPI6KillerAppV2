package Server;

import Shared.Connector;
import Shared.ISubscriber;
import Shared.NetworkMessage;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;

public class ServerAppController implements ISubscriber {

    private String startupURL = "QiFBgtgUtfw";
    private Connector connector;

    @FXML
    private WebView webview_YouTubeVideo;

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
        listenQueues.add("ChatMessagesFromBridge");
        listenQueues.add("SongRequestsFromBridge");

        List<String> listenTopics = new ArrayList<String>();

        connector = new Connector(this, listenQueues, listenTopics);
    }

    private void playNewVideo(String videoID) {
        // Todo: Can we hide the title and playlist buttons too?
        String url = "https://www.youtube.com/embed/" + videoID + "?controls=0&autoplay=1";
        webview_YouTubeVideo.getEngine().load(url);
    }

    @Override
    public void onMessageReceived(NetworkMessage message) {

    }
}
