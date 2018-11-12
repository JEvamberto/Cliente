
import java.io.Serializable;

public class PacoteAck implements Serializable {

    private int seqNum;
    private int ackNum;
    private int connectionID;
    private boolean ack;
    private boolean syn;
    private boolean fyn;
    

    public PacoteAck(int seqNum) {
        super();
        this.seqNum = seqNum;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setPacket(int seqNum) {
        this.seqNum = seqNum;
    }

}
