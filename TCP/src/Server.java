
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    
    public static void main(String[] args) {
        DatagramSocket socket;
    
        try {
            byte dados[] = new byte[512+512];
            socket = new DatagramSocket(6669);
            DatagramPacket pkg = new DatagramPacket(dados, dados.length);
            
            while(true){
               System.out.println("Estou esperando clientes");
               socket.receive(pkg);
               Pacote pacote = (Pacote)Serializer.toObject(pkg.getData());
               
                if (pacote.isSyn()) {
                    
                    Servidor serverTrata = new Servidor(pacote,pkg.getPort());
                    
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
