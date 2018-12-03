
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private int porta;
    private String caminho;
    private DatagramSocket socket;
    private byte[] dados;
    private DatagramPacket pkg;
    

    public Server(int porta, String caminho) {

        try {
            
            this.dados = new byte[1024];
            this.pkg = new DatagramPacket(dados, dados.length);
            this.porta = porta;
            this.caminho = caminho;
            this.socket = new DatagramSocket(porta);
       
        } catch (SocketException ex) {
            System.out.println("Não foi possivel abrir o aocket na porta "+ porta);
        }

    }

    public static void main(String[] args) {
        
        String caminhoLauro = "src\\arquivo\\";
        String caminho = "/home/jose/NetBeansProjects/ClienteTCP1/Lauro/src/arquivo/";
        int portaServidor = 6669;
        
        Server server = new Server(portaServidor, caminhoLauro);
        
        try {
        
            while (true) {
                
                System.out.println("Estou esperando clientes");
                server.socket.receive(server.pkg);
                Pacote pacote = (Pacote) Serializer.toObject(server.pkg.getData());

                if (pacote.isSyn()) {

                    new Comunicacao(server.caminho, pacote, server.pkg.getPort());

                }

            }

        } catch (SocketException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
