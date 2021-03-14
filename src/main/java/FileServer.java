import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class FileServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        //1 - Definir o serverSocket (abrir porta de conexão)
        try (ServerSocket serverSocket = new ServerSocket(54321)) {
            int filesize = 6022386;

            while (true) {
                System.out.println("A porta 54321 foi aberta!");
                System.out.println("Servidor esperando receber mensagem de cliente...");

                //2 - Aguardar solicitação de conexão de cliente
                Socket socket = serverSocket.accept();
                //Mostrar endereço IP do cliente conectado
                System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " conectado");

                //3 - Definir stream de entrada de dados no servidor
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String mensagem = entrada.readUTF();//receber mensagem do cliente

                System.out.println("O arquivo " + mensagem + " foi recebido no cliente!");

                String[] splitted= mensagem.split("/");

                String nomeArquivo= splitted[0]; //live

                String evento = splitted[1]; //coding

                System.out.println("nomeArquivo "+nomeArquivo);
                System.out.println("evento "+evento);

                if(evento=="ENTRY_DELETE"){
                    File file = new File(nomeArquivo);
                    file.delete();
                }else{
                    //4 - Definir stream de saída de dados do servidor
                    DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                    saida.writeUTF(nomeArquivo); //Enviar mensagem em maiúsculo para cliente

                    /*inicio programa de transferencia de arquivo*/
                    long start = System.currentTimeMillis();
                    int bytesRead;
                    int current = 0;

                    // recebendo o arquivo
                    byte[] mybytearray = new byte[filesize];
                    InputStream is = socket.getInputStream();
                    FileOutputStream fos = new FileOutputStream("server_1/" + nomeArquivo);
                    BufferedOutputStream bos = new BufferedOutputStream(fos);
                    bytesRead = is.read(mybytearray, 0, mybytearray.length);
                    current = bytesRead;

                    do {
                        bytesRead =
                                is.read(mybytearray, current, (mybytearray.length - current));
                        if (bytesRead >= 0) current += bytesRead;
                    } while (bytesRead > -1);

                    bos.write(mybytearray, 0, current);
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);

                    bos.close();
                    /*fim programa de transferencia de arquivo*/

                    //5 - Fechar streams de entrada e saída de dados

                    saida.close();
                }
                entrada.close();
                //6 - Fechar sockets de comunicação e conexão
                socket.close();

            }

        }

    }
}