package Shared;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    protected String sentfrom;

    public String getSentFrom() {
        return sentfrom;
    }
}
