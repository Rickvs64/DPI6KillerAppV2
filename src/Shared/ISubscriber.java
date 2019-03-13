package Shared;

import javax.jms.JMSException;

public interface ISubscriber {
    void onMessageReceived(NetworkMessage message) throws JMSException;
}
