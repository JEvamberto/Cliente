
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

    public final int TAMANHO_CABECALHO = 512;
    public final int TAMANHO_PAYLOAD = 512;
    public final double TAXA_PERDA = 0.0;
    public final int PORTA_SERVIDOR = 9876;

    private DatagramSocket socketServidor;
    private byte[] pacoteRecebido;
    private InetAddress ipLocal;
    private Thread t1;
    int esperaPor;
    int seqNum = 4321;

    ArrayList<byte[]> partesArquivo = new ArrayList<>();

    public Servidor() throws UnknownHostException {

        try {
            socketServidor = new DatagramSocket(this.PORTA_SERVIDOR);
            pacoteRecebido = new byte[TAMANHO_CABECALHO + TAMANHO_PAYLOAD];
            ipLocal = InetAddress.getLocalHost();
            t1 = new Thread(this);
            t1.start();

        } catch (SocketException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void esperarPacotes() throws IOException, ClassNotFoundException {

        boolean end = false;

        while (!end) {

            byte[] pacoteRecebido = this.receberPacote();
            Pacote pacote = (Pacote) Serializer.toObject(pacoteRecebido);
            //pacote.setSeqNum(esperaPor);

            System.out.println(pacote.toString() + "\n<------------------------------------");

            if (pacote.isSyn()) {

                Pacote synack = new Pacote(pacote.getSeqNum() + 1);
                synack.setSeqNum(this.seqNum);
                synack.setAck(true);
                synack.setSyn(true);
                synack.setConnectionID(1);
                this.esperaPor = synack.getAckNum();

                byte[] ackBytes = Serializer.toBytes(synack);

                if (Math.random() > TAXA_PERDA) {
                    this.enviarPacote(ackBytes, 5000);
                    System.out.println(synack.toString() + "\n------------------------------------>");
                    break;

                } else {
                    System.out.println("[X] Ack perdido com o número de sequência" + synack.getSeqNum());
                }

            }

            if (pacote.getSeqNum() == esperaPor && pacote.isFyn()) {

                //se for o ultimo pacot
                esperaPor += 512;
                partesArquivo.add(pacote.getPayload());
                this.salvarArquivo("");
                end = true;

                
            } else if (pacote.getSeqNum() == esperaPor) {

                esperaPor += 512;
                partesArquivo.add(pacote.getPayload());

            } else {
                //se nao for o pacote que que estava esperando
                System.out.println("Pacote descartado (fora de ordem)");

            }

            // enviar ack
            
            Pacote pacoteAck = new Pacote(pacote.getSeqNum() + 512);
            pacoteAck.setConnectionID(pacote.getConnectionID());
            pacoteAck.setAck(true);
            
            byte[] ackBytes = Serializer.toBytes(pacoteAck);

            if (Math.random() > TAXA_PERDA) {
                this.enviarPacote(ackBytes, 5000);
                System.out.println("  "+pacoteAck.toString() + "\n------------------------------------>");
            } else {
                System.out.println("[X] Ack perdido com o número de sequência" + pacoteAck.getSeqNum());
            }

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

    private void salvarArquivo(String caminho) {

        String caminhoLauro = "src\\arquivo\\";

        caminho = "/home/jose/NetBeansProjects/ClienteTCP1/Lauro/src/arquivo/teste.txt";
        byte[] arquivo = new byte[this.partesArquivo.size() * 512];

        System.out.println(arquivo.length);

        String nome = caminhoLauro + "nomeArquivo" + ".txt";
        System.out.println(nome);
        File SalvaNoDiretorio = new File(nome);

        int i = 0;
        //posicao vai andar de acordo com cada byte que vai ser colocado no vetor de byte completo
        int posicao = 0;

        while (i < partesArquivo.size()) {
            for (int j = 0; j < partesArquivo.get(i).length; j++) {
                arquivo[posicao] = partesArquivo.get(i)[j];
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

    @Override
    public void run() {
        while (true) {
            try {

                this.esperarPacotes();

            } catch (IOException ex) {
                System.out.println("Não foi possivel receber o pacote...");
            } catch (ClassNotFoundException ex) {
                System.out.println("Arquivo não é o esperado");
            }
        }

    }

    public static void main(String[] args) throws Exception {

        Servidor server = new Servidor();

    }
}
