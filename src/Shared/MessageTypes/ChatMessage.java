package Shared.MessageTypes;

import Shared.NetworkMessage;

public class ChatMessage extends NetworkMessage {

    String chatMessage;

    public ChatMessage(String sentfrom, String chatMessage) {
        this.sentfrom = sentfrom;
        this.chatMessage = chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }
}
