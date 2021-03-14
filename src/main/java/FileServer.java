import com.sun.org.apache.xpath.internal.res.XPATHErrorResources_sv;

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
                System.out.println("Servidor 1 pronto para nova conexão\n");

                //2 - Aguardar solicitação de conexão de cliente
                Socket socket = serverSocket.accept();

                //Mostrar endereço IP do cliente conectado
                System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " aceito");
                System.out.println("Conexão estabelecida\n");

                //3 - Definir stream de entrada de dados no servidor
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String mensagem = entrada.readUTF(); //receber mensagem do cliente

                System.out.println("O arquivo " + mensagem + " foi recebido no cliente!\n");

                String[] splitted = mensagem.split("/");

                String nomeArquivo = splitted[0]; //live

                String evento = splitted[1]; //coding

                /*
                System.out.println("nomeArquivo " + nomeArquivo);
                System.out.println("evento " + evento);
                */

                //4 - Definir stream de saída de dados do servidor
                DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
                saida.writeUTF(nomeArquivo); //Enviar mensagem em maiúsculo para cliente

                if (evento.equals("ENTRY_DELETE")) {
                    //System.out.println("ENTRY_DELETE entrou");
                    File file = new File("server_1/" + nomeArquivo);
                    file.delete();
                } else {
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

                    System.out.println("Terminou a transferência\nIniciando transferência para os outros servidores");
                }
                entrada.close();
                //6 - Fechar sockets de comunicação e conexão
                socket.close();

                connectOthersServes(nomeArquivo, evento);
            }

        }

    }

    public static void connectOthersServes(String nomeArq, String evento) throws IOException {
        //System.out.println("chegou no método");

        Socket socket = new Socket("127.0.0.1", 54322);

        // Enviando nome do arquivo para o servidor
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeUTF(nomeArq);

        // Enviando arquivo para o servidor
        File myFile = new File("server_1/" + nomeArq);

        byte[] mybytearray = new byte[(int) myFile.length()];

        FileInputStream fis = new FileInputStream(myFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(mybytearray, 0, mybytearray.length);

        OutputStream os = socket.getOutputStream();

        os.write(mybytearray, 0, mybytearray.length);

        System.out.println("Arquivo " + nomeArq + " enviado para o Servidor 2!\n");

        os.flush();
        os.close();
        bis.close();
    }
}