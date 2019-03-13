package Shared.MessageTypes;

import Shared.NetworkMessage;

public class ClientInitMessage extends NetworkMessage {

    public ClientInitMessage(String sentfrom) {
        this.sentfrom = sentfrom;
    }
}
