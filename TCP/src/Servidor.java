
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor implements Runnable {

    public static final int TAMANHO_CABECALHO = 512;
    public static final int TAMANHO_PAYLOAD = 200;
    public static final double PROBABILIDADE = 0.1;
    public static final int PORTA_SERVIDOR = 9876;

    private DatagramSocket socketServidor;
    private byte[] pacoteRecebido;
    private InetAddress ipLocal;
    private Thread t1;

    ArrayList<byte[]> received = new ArrayList<>();
    int esperaPor = 12345;

    public Servidor() throws UnknownHostException {

        try {
            socketServidor = new DatagramSocket(Servidor.PORTA_SERVIDOR);
            pacoteRecebido = new byte[TAMANHO_CABECALHO + TAMANHO_PAYLOAD];
            ipLocal = InetAddress.getLocalHost();
            t1 = new Thread(this);
            t1.start();

        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void esperarPacotes() throws IOException, ClassNotFoundException {

        System.out.println("Esperando pelo pacote");

        boolean end = false;

        while (!end) {

            byte[] pacoteRecebido = this.receberPacote();
            PacoteDados pacote = (PacoteDados) Serializer.toObject(pacoteRecebido);
            //pacote.setSeqNum(esperaPor);

            System.out.println("Pacote com número de sequência " + pacote.getSeqNum() + " recebido (último: " + pacote.isFyn() + " )");

            if (pacote.getSeqNum() == esperaPor && pacote.isFyn()) {

                //se for o ultimo pacote
                esperaPor+=512;
                received.add(pacote.getPayload());
                System.out.println("Último pacote recebido");
                end = true;

            } else if (pacote.getSeqNum() == esperaPor) {

                esperaPor+=512;
                received.add(pacote.getPayload());
                System.out.println("pacote armazenado em buffer");

            } else {
                //se nao for o pacote que que estava esperando
                System.out.println("################################Pacote descartado (fora de ordem)");
               
            }

            // enviar ack
            PacoteAck ackObject = new PacoteAck(esperaPor);
            byte[] ackBytes = Serializer.toBytes(ackObject);

            if (Math.random() > PROBABILIDADE) {
                this.enviarPacote(ackBytes, 5000);
            } else {
                System.out.println("[X] Ack perdido com o número de sequência" + ackObject.getSeqNum());
            }

            System.out.println("Enviando ACK para seq " + esperaPor + " com " + ackBytes.length + " bytes");

        }
    }

    private void enviarPacote(byte[] pkg, int portaCliente) {

        try {
            DatagramPacket sendPacket;
            sendPacket = new DatagramPacket(pkg, pkg.length, this.ipLocal, portaCliente);
            socketServidor.send(sendPacket);

        } catch (IOException ex) {
            System.out.println("Não foi possivel enviar o pacote");
        }
    }

    private byte[] receberPacote() {

        try {

            DatagramPacket pacote = new DatagramPacket(pacoteRecebido, pacoteRecebido.length);
            socketServidor.receive(pacote);
            byte[] pkg = pacote.getData();

            return pkg;
        } catch (IOException ex) {
            System.out.println("Não foi possivel receber o pacote");
        }
        return null;
    }

    @Override
    public void run() {
        try {
          
            this.esperarPacotes();

        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.salvarArquivo("");
    }

    public void salvarArquivo(String caminho) {

        String caminhoLauro = "C:\\Users\\Lauro Costa\\Documents\\NetBeansProjects\\Go-Back-N\\GoBackN\\src\\arquivo\\";

        caminho =  "/home/jose/NetBeansProjects/ClienteTCP1/Lauro/src/arquivo/teste.txt";
        byte[] arquivo = new byte[this.received.size() * 512];

        System.out.println(arquivo.length);

        String nome = caminho + "nomeArquivo" + ".txt";
        System.out.println(nome);
        File SalvaNoDiretorio = new File(nome);
        
        
        int i = 0;
        //posicao vai andar de acordo com cada byte que vai ser colocado no vetor de byte completo
        int posicao = 0;
       
        while (i < received.size()) {
            for (int j = 0; j < received.get(i).length; j++) {
                arquivo[posicao] = received.get(i)[j];
                posicao++;
            }
            i++;
        }
        try {
            Files.write(SalvaNoDiretorio.toPath(), arquivo);
        } catch (IOException ex) {
            System.out.println("erro ao tentar salvar arquivo");
        }

    }


public static void main(String[] args) throws Exception {

        Servidor server = new Servidor();

    }
}
