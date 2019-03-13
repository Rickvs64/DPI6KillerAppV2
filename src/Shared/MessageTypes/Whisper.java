package Shared.MessageTypes;

import Shared.NetworkMessage;

public class Whisper extends NetworkMessage {

    String chatMessage;
    String sentto;

    public Whisper(String sentfrom, String sentto, String chatMessage) {
        this.sentfrom = sentfrom;
        this.sentto = sentto;
        this.chatMessage = chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public String getSentTo() { return sentto; }

}
