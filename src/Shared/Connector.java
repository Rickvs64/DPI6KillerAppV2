package Shared;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.List;

public class Connector {

    // JMS server URL, default broker URL is tcp://localhost:61616
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;

    private Session session;

    // Subscriber to notify upon receiving messages
    private ISubscriber subscriber;

    public Connector(ISubscriber newSub, List<String> listenChannelsQueues, List<String> listenChannelsTopics) {
        try {
            subscriber = newSub;
            connect(listenChannelsQueues, listenChannelsTopics);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    // Connect and set listen channel/topic
    private void connect(List<String> listenChannelsQueues, List<String> listenChannelsTopics) throws JMSException {
        System.setProperty("org.apache.activemq.SERIALIZABLE_PACKAGES", "*");

        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();

        // Creating a non transactional session to send/receive JMS message
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Create QUEUE listeners for EACH given channel
        for (String channel : listenChannelsQueues) {
            // This one is only used for LISTENING
            Destination destination = session.createQueue(channel);

            // MessageConsumer is used for receiving (consuming) messages
            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message msg) {
                    try {
                        receiveGenericMessage(msg);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // Create TOPIC listeners for EACH given channel
        for (String channel : listenChannelsTopics) {
            // This one is only used for LISTENING
            Destination destination = session.createTopic(channel);

            // MessageConsumer is used for receiving (consuming) messages
            MessageConsumer consumer = session.createConsumer(destination);

            consumer.setMessageListener(new MessageListener() {

                @Override
                public void onMessage(Message msg) {
                    try {
                        receiveGenericMessage(msg);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    // Send message to QUEUE
    public void sendMessageToQueue(NetworkMessage message, String sendChannel) throws JMSException {
        Destination destination = session.createQueue(sendChannel);

        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer = session.createProducer(destination);
        ObjectMessage oMessage = session.createObjectMessage(message);

        // Here we are sending our message!
        producer.send(oMessage);
    }

    // Send message to TOPIC
    public void sendMessageToTopic(NetworkMessage message, String sendChannel) throws JMSException {
        Destination destination = session.createTopic(sendChannel);

        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer = session.createProducer(destination);
        ObjectMessage oMessage = session.createObjectMessage(message);

        // Here we are sending our message!
        producer.send(oMessage);
    }

    // Receive generic message
    private void receiveGenericMessage(Message msg) throws JMSException {

        // Now to quickly make sure it's the right type of message.
        if (msg instanceof ObjectMessage) {
            ObjectMessage oMessage = (ObjectMessage) msg;

            NetworkMessage content = (NetworkMessage) oMessage.getObject();

            // Normally should only follow through if this message is relevant to the receiver
            // Currently no restrictions are implemented
            subscriber.onMessageReceived(content);
        }
    }
}
