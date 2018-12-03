
import java.io.Serializable;
import java.util.Arrays;

public class Pacote implements Serializable {

    public int seqNum;
    public int ackNum;
    public byte[] payload;
    public int connectionID;
    public boolean ack;
    public boolean fyn;
    public boolean syn;

    public Pacote(int seq, byte[] payload, boolean fyn) {
        super();
        this.seqNum = seq;
        this.payload = payload;
        this.fyn = fyn;
    }

    Pacote() {
    
    }

    Pacote(int i) {
        this.ackNum = i ;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seq) {
        this.seqNum = seq;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] data) {
        this.payload = data;
    }

    public boolean isFyn() {
        return fyn;
    }

    public void setFyn(boolean last) {
        this.fyn = last;
    }

    public int getAckNum() {
        return ackNum;
    }

    public void setAckNum(int ackNum) {
        this.ackNum = ackNum;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    public boolean isAck() {
        return ack;
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public boolean isSyn() {
        return syn;
    }

    public void setSyn(boolean syn) {
        this.syn = syn;
    }
    
    
    

    private String siglasChaves() {

        String aux = "";

        if (this.syn) {
            aux += ", SYN";
        }

        if (this.fyn) {
            aux += ", FIN";
        }
        
        if (this.ack) {
            aux += ", ACK";
        }


        return aux;
    }

    @Override
    public String toString() {
       
        if(siglasChaves().equals("")){
            return "seq=" + this.getSeqNum() + ", ack=" + this.ackNum + ", id=" + this.connectionID ;
       
        }else{
            
             return " seq=" + this.getSeqNum() + ", ack=" + this.ackNum + ", id=" + this.connectionID + this.siglasChaves();
        }
    
    
    }

}
