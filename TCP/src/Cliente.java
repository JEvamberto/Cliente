
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;

public class Cliente {

    public static final int TAMANHO_PAYLOAD = 512;
    public static final double PROBABILIDADE = 0.1;
    public static final int JANELA = 4;
    public static final int TIMEOUT = 500;

    
    public String caminhoLauro = "C:\\Users\\Lauro Costa\\Documents\\NetBeansProjects\\Go-Back-N\\GoBackN\\src\\arquivo\\teste.txt";
    public String caminho = "/home/jose/NetBeansProjects/ClienteTCP1/Lauro/src/arquivo/teste.txt";
    private DatagramSocket socketCliente;
    private byte[] resposta = new byte[200];
    public DatagramPacket ack = new DatagramPacket(resposta, resposta.length);
    private ArrayList<PacoteDados> pacotesEnviados = new ArrayList<PacoteDados>();
    private InetAddress ipServidor = InetAddress.getByName("localhost");
    private byte[] arquivo;
    public int seqPacote = 12345;
    public int esperaAck = 12345;
    private int ultimoPacote;
    private int count=0;
    public int fim;
    public Cliente() throws IOException {

        try {

            this.socketCliente = new DatagramSocket(5000);
            this.arquivo = getBytesArquivo();
            this.ultimoPacote = (int) Math.ceil((double) arquivo.length / TAMANHO_PAYLOAD);
            this.ipServidor = InetAddress.getByName("localhost");
            this.fim=this.ultimoPacote;
            this.ultimoPacote=ultimoPacote*512+12345;
            

        } catch (SocketException ex) {
            System.out.println("Erro ao criar o socket");
        }
    }

    public byte[] getBytesArquivo() throws FileNotFoundException, IOException {

        File file = new File(this.caminho);
        FileInputStream fistream = new FileInputStream(file);
        this.arquivo = new byte[(int) file.length()];
        fistream.read(this.arquivo);
        fistream.close();

        return arquivo;
    }

    public void enviaJanela() throws IOException  {
        
         
        //envia ate o tamanho da janela
        while (seqPacote - esperaAck < JANELA && seqPacote < ultimoPacote) {

            byte[] pacoteBytes = new byte[TAMANHO_PAYLOAD];
            
            System.out.println(seqPacote * TAMANHO_PAYLOAD + "   " + (seqPacote * TAMANHO_PAYLOAD + TAMANHO_PAYLOAD));
           
            pacoteBytes = Arrays.copyOfRange(arquivo, count * TAMANHO_PAYLOAD, count * TAMANHO_PAYLOAD + TAMANHO_PAYLOAD);

            PacoteDados pacoteDados = new PacoteDados(seqPacote, pacoteBytes, (seqPacote == ultimoPacote-512 ) ? true : false);

            byte[] sendData = Serializer.toBytes(pacoteDados);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ipServidor, 9876);
          
            System.out.println("Envio de pacote com número de sequência " + seqPacote + " e tamanho " + sendData.length + " bytes");
            pacotesEnviados.add(pacoteDados);
            
            if (Math.random() > PROBABILIDADE) {
                socketCliente.send(packet);
            } else {
                System.out.println("[X] Pacote perdido com número de sequência" + seqPacote);
            }
            seqPacote+=512;
            count++;
          
        }
    }

    public void enviarArquivo() throws IOException, ClassNotFoundException {

        while (true) {

            this.enviaJanela();

            try {

                socketCliente.setSoTimeout(TIMEOUT);
                socketCliente.receive(ack);
                PacoteAck ackObject = (PacoteAck) Serializer.toObject(ack.getData());
                System.out.println("Recebido ack do pacote " + ackObject.getSeqNum());

                if (ackObject.getSeqNum() == (ultimoPacote)) {
                    break;
                }

                esperaAck = Math.max(esperaAck, ackObject.getSeqNum());

            } catch (SocketTimeoutException e) {
                
                
               /* for (int i = 0; i < this.JANELA; i++) {
                    count--;
                }*/

                for (int i = (this.JANELA-1); i >= 0; i--) {

                    byte[] sendData = Serializer.toBytes(pacotesEnviados.get(((this.pacotesEnviados.size()-1)-i)  ));
                    DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ipServidor, 9876);

                    if (Math.random() > PROBABILIDADE) {
                        socketCliente.send(packet);
                    } else {
                        System.out.println("[X] Pacote perdido com número de sequência" + pacotesEnviados.get(i).getSeqNum());

                    }
                    System.out.println("Pacote de reemissão com número de sequência" + pacotesEnviados.get(i).getSeqNum() + " and size " + sendData.length + " bytes");
                
                }
            }
        }
        System.out.println("Finished transmission");
    }

    public static void main(String[] args) throws Exception {

        Cliente c = new Cliente();
        c.enviarArquivo();
        
    }
}
