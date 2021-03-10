import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class FileServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(4444);
            System.out.println("A porta 4444 foi aberta!");
            System.out.println("Servidor esperando receber mensagens de clientes...");
        } catch (IOException ex) {
            System.out.println("Can't setup server on this port number. ");
        }

        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;


//while (true){
            try {
                socket = serverSocket.accept();
                System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " conectado");
            } catch (IOException ex) {
                System.out.println("Can't accept client connection. ");
            }

            //3 - Definir stream de entrada de dados no servidor
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            String mensagem = entrada.readUTF();//receber mensagem em minúsculo do cliente
            System.out.println("Recebe do cliente: "+mensagem);

            try {
                in = socket.getInputStream();
            } catch (IOException ex) {
                System.out.println("Can't get socket input stream. ");
            }

            try {
                //String juncao = "server_1/"+arquivo;

                System.out.println("*** server_1/"+mensagem);
                out = new FileOutputStream("server_1/"+mensagem);
            } catch (FileNotFoundException ex) {
                System.out.println("File not found. ");
            }

            byte[] bytes = new byte[16*1024];

            int count;
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
//}
            out.close();
            in.close();
            socket.close();
            serverSocket.close();

    }
}