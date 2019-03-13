package Shared.MessageTypes;

import Shared.NetworkMessage;

public class SongRequest extends NetworkMessage {

    private String songID;

    public SongRequest(String sentfrom, String songID) {
        this.sentfrom = sentfrom;
        this.songID = songID;
    }

    public String getSongID() {
        return songID;
    }

}
