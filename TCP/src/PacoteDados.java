import java.io.Serializable;
import java.util.Arrays;


public class PacoteDados implements Serializable {

	public int seqNum;
        public int ackNum;
	public byte[] payload;
        public int connectionID;
        public boolean ack;
        public boolean fyn;
        public boolean syn;


	public PacoteDados(int seq, byte[] payload, boolean fyn) {
		super();
		this.seqNum = seq;
		this.payload = payload;
		this.fyn = fyn;
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

	@Override
	public String toString() {
		return "UDPPacket [seq=" + seqNum + ", data=" + Arrays.toString(payload)
				+ ", last=" + fyn + "]";
	}
	
}
