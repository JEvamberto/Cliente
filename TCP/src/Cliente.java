
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
    public static final double TAXA_PERDA = 0.0;
    public static final int JANELA = 4;
    public static final int TIMEOUT = 500;

    private String caminhoLauro = "src\\arquivo\\teste.txt";
    private String caminho = "/home/jose/NetBeansProjects/ClienteTCP1/Lauro/src/arquivo/teste.txt";
    private DatagramSocket socketCliente;
    private byte[] resposta = new byte[200];
    private DatagramPacket ack = new DatagramPacket(resposta, resposta.length);
    private ArrayList<Pacote> pacotesEnviados = new ArrayList<Pacote>();
    private InetAddress ipServidor = InetAddress.getByName("localhost");
    private byte[] arquivo;
    private int seqPacote = 12345;
    private int esperaAck;
    private int ultimoPacote;
    private int count = 0;
    private int connectionID;

    public Cliente() throws IOException {

        try {

            this.socketCliente = new DatagramSocket();
            this.arquivo = getBytesArquivo();
            this.ultimoPacote = (int) Math.ceil((double) arquivo.length / TAMANHO_PAYLOAD);
            this.ipServidor = InetAddress.getByName("localhost");

            this.ultimoPacote = ultimoPacote * 512 + seqPacote;

        } catch (SocketException ex) {
            System.out.println("Erro ao criar o socket");
        }
    }

    public byte[] getBytesArquivo() throws FileNotFoundException, IOException {

        File file = new File(this.caminhoLauro);
        FileInputStream fistream = new FileInputStream(file);
        this.arquivo = new byte[(int) file.length()];
        fistream.read(this.arquivo);
        fistream.close();

        return arquivo;
    }

    public void enviaJanela() throws IOException {

        //envia ate o tamanho da janela
        while (seqPacote - esperaAck < JANELA && seqPacote < ultimoPacote) {

            byte[] pacoteBytes = new byte[TAMANHO_PAYLOAD];

            pacoteBytes = Arrays.copyOfRange(arquivo, count * TAMANHO_PAYLOAD, count * TAMANHO_PAYLOAD + TAMANHO_PAYLOAD);

            Pacote pacoteDados = new Pacote(seqPacote, pacoteBytes, (seqPacote == (ultimoPacote + 1) - 512) ? true : false);
            pacoteDados.setConnectionID(this.connectionID);
           

            byte[] sendData = Serializer.toBytes(pacoteDados);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ipServidor, 9876);

            System.out.println(pacoteDados.toString() + "\n------------------------------------>");
            pacotesEnviados.add(pacoteDados);

            if (Math.random() > TAXA_PERDA) {
                socketCliente.send(packet);
            } else {
                System.out.println("[X] Pacote perdido com número de sequência" + seqPacote);
            }
            seqPacote += 512;
            count++;
        }
    }

    public void transferirArquivo() throws IOException, ClassNotFoundException {

        while (true) {

            this.enviaJanela();

            try {
                //se nao chegar resposta continua no catch

                socketCliente.setSoTimeout(TIMEOUT);
                socketCliente.receive(ack);
                Pacote pacoteAck = (Pacote) Serializer.toObject(ack.getData());

                System.out.println(pacoteAck.toString() + "\n<------------------------------------");

                if (pacoteAck.getAckNum() == ultimoPacote + 1) {
                    break;
                }

                esperaAck = Math.max(esperaAck, pacoteAck.getAckNum());

            } catch (SocketTimeoutException e) {
                //aqui
                if (pacotesEnviados.size() - 1 >= 3) {
                    for (int i = (this.JANELA - 1); i >= 0; i--) {

                        byte[] sendData = Serializer.toBytes(pacotesEnviados.get(((this.pacotesEnviados.size() - 1) - i)));
                        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ipServidor, 9876);

                        if (Math.random() > TAXA_PERDA) {
                            socketCliente.send(packet);
                        } else {
                            System.out.println("[X] Pacote perdido com número de sequência" + pacotesEnviados.get(i).getSeqNum());

                        }
                        System.out.println("Pacote de reemissão com número de sequência" + pacotesEnviados.get(i).getSeqNum() + " e tamanho " + sendData.length + " bytes");

                    }

                } else {
                    //aqui
                    for (int i = 0; i < pacotesEnviados.size(); i++) {

                        byte[] sendData = Serializer.toBytes(pacotesEnviados.get(i));
                        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, ipServidor, 9876);

                        if (Math.random() > TAXA_PERDA) {
                            socketCliente.send(packet);
                        } else {
                            System.out.println("[X] Pacote perdido com número de sequência" + pacotesEnviados.get(i).getSeqNum());

                        }
                        System.out.println("Pacote de reemissão com número de sequência" + pacotesEnviados.get(i).getSeqNum() + " e tamanho " + sendData.length + " bytes");
                    }
                }
            }
        }
        System.out.println("Arquivo transferido");
    }

    private void enviarPacote(byte[] pkg, int portaServidor) {

        try {
            DatagramPacket sendPacket;
            sendPacket = new DatagramPacket(pkg, pkg.length, this.ipServidor, portaServidor);
            socketCliente.send(sendPacket);

        } catch (IOException ex) {
            System.out.println("Não foi possivel enviar o pacote");
        }
    }

    private Pacote receberPacote() {
        
        try {

            byte[] pacoteRecebido = new byte[1024];

            DatagramPacket pacote = new DatagramPacket(pacoteRecebido, pacoteRecebido.length);

            socketCliente.setSoTimeout(TIMEOUT);
            socketCliente.receive(pacote);

            byte[] pkg = pacote.getData();
            return (Pacote) Serializer.toObject(pkg);

        } catch (ClassNotFoundException ex) {
            System.out.println("Não é o arquivo esperado...");
        } catch (SocketTimeoutException e) {
            this.enviarPacoteSyn();
        } catch (IOException ex) {
            System.out.println("Não foi possivel receber o pacote");
        }
        return null;
    }

    public void enviarPacoteSyn() {

        try {

            Pacote pacoteSyn = new Pacote();
            pacoteSyn.setSyn(true);
            pacoteSyn.setSeqNum(this.seqPacote);
            byte[] pacote = Serializer.toBytes(pacoteSyn);
            enviarPacote(pacote, 9876);
            System.out.println(pacoteSyn.toString() + "\n------------------------------------>");
            Pacote resposta = receberPacote();

            if (resposta != null) {

                System.out.println(resposta.toString() + "\n<------------------------------------");
                this.seqPacote = resposta.getAckNum();
                this.esperaAck = resposta.getAckNum();
                this.connectionID = resposta.getConnectionID();

            }

        } catch (IOException ex) {
            System.out.println("Não foi possivel enviar o pacote");
        }

    }

    public static void main(String[] args) throws Exception {

        Cliente c = new Cliente();
        c.enviarPacoteSyn();
        c.transferirArquivo();

    }
}
