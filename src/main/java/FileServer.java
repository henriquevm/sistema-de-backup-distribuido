import com.sun.org.apache.xpath.internal.res.XPATHErrorResources_sv;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

public class FileServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        //1 - Definir o serverSocket (abrir porta de conexão)
        try (ServerSocket serverSocket = new ServerSocket(11111)) {
            int filesize = 6022386;

            while (true) {
                System.out.println("A porta 54321 foi aberta!");
                System.out.println("Servidor 1 pronto para nova conexão\n");

                //2 - Aguardar solicitação de conexão de cliente
                Socket socket = serverSocket.accept();

                //Mostrar endereço IP do cliente conectado
                System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " aceito");
                System.out.println("Conexão estabelecida\n");

                //3 - Definir stream de entrada de dados no servidor
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String mensagem = entrada.readUTF(); //receber mensagem do cliente

                System.out.println("O arquivo " + mensagem + " foi recebido do cliente!\n");

                String[] splitted = mensagem.split("/");

                String nomeArquivo = splitted[0]; //live

                String evento = splitted[1]; //coding

                System.out.println("nomeArquivo " + nomeArquivo);
                System.out.println("evento " + evento);

                //4 - Definir stream de saída de dados do servidor
                DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                saida.writeUTF(nomeArquivo); //Enviar mensagem em maiúsculo para cliente

                switch (evento) {
                    case "DIR_CREATE":
                        System.out.println("É Diretorio " + evento);
                        File file = new File("server_1/" + nomeArquivo);
                        file.mkdir();
                        break;
                    case "ENTRY_DELETE":
                        //System.out.println("ENTRY_DELETE entrou");
                        File file_delete = new File("server_1/" + nomeArquivo);
                        file_delete.delete();
                        break;
                    default:
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
                    //System.out.println(end - start);

                        bos.close();
                        /*fim programa de transferencia de arquivo*/

                        //5 - Fechar streams de entrada e saída de dados

                        saida.close();

                        System.out.println("Terminou a transferência\nIniciando transferência para os outros servidores");

                }

                entrada.close();
                //6 - Fechar sockets de comunicação e conexão
                socket.close();

                String host = "127.0.0.1";
                int portServer1 = 22222;
                int portServer2 = 33333;

                Thread t1 = new Thread(host, portServer1, nomeArquivo, evento);
                Thread t2 = new Thread(host, portServer2, nomeArquivo, evento);
                t1.start();
                t2.start();
            }

        }

    }
}