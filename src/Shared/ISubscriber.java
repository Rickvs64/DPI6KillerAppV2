package Shared;

public interface ISubscriber {
    void onMessageReceived(NetworkMessage message);
}
