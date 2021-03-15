import com.sun.org.apache.xpath.internal.res.XPATHErrorResources_sv;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class FileServer {

    private static String nomeArquivo;
    private static Socket socket;

    public static void main(String[] args) throws IOException, InterruptedException {

        //1 - Definir o serverSocket (abrir porta de conexão)
        try (ServerSocket serverSocket = new ServerSocket(11111)) {
            int filesize = 6022386;

            while (true) {
                System.out.println("A porta 11111 foi aberta!");
                System.out.println("Servidor 1 pronto para nova conexão\n");

                //2 - Aguardar solicitação de conexão de cliente
                socket = serverSocket.accept();

                //Mostrar endereço IP do cliente conectado
                System.out.println("Cliente " + socket.getInetAddress().getHostAddress() + " aceito");
                System.out.println("Conexão estabelecida\n");

                //3 - Definir stream de entrada de dados no servidor
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String mensagem = entrada.readUTF(); //receber mensagem do cliente

                System.out.println("O arquivo " + mensagem + " foi recebido do cliente!\n");

                String[] splitted = mensagem.split("/");

                nomeArquivo = splitted[1];
                String evento = splitted[0];

                System.out.println("evento " + evento + "\n");
                //System.out.println("nomeArquivo " + nomeArquivo);

                switch (evento) {
                    case "DIR_CREATE": {
                        //System.out.println("É Diretorio " + evento);
                        handleDirCreate(nomeArquivo);
                        break;
                    }
                    case "DIR_DELETE": {
                        //System.out.println("É Diretorio " + evento);
                        handleDirDelete(nomeArquivo);
                        break;
                    }
                    case "ENTRY_DELETE": {
                        //System.out.println("ENTRY_DELETE ************************************ " + evento);
                        //System.out.println("ENTRY_DELETE entrou");
                        handleEntryDelete(nomeArquivo);
                        break;
                    }
                    default: {
                        handleEntryCreateAndModify(nomeArquivo);
                        System.out.println("Terminou a transferência\nIniciando transferência para os outros servidores");
                        break;
                    }
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
                //System.out.println("Chegou aqui");
            }
        }
    }

    public static void handleDirCreate(String nomeArquivo) throws IOException {
        //File DIR_CREATE = new File("server_1/" + nomeArquivo);
        //DIR_CREATE.mkdir();

        Path destinationDir = Paths.get("server_1/" + nomeArquivo);
        Path sourceDir = Paths.get("cliente_obs/" + nomeArquivo);

        copyFolder(sourceDir, destinationDir);
    }

    public static void handleDirDelete(String nomeArquivo) {
        File DIR_DELETE = new File("server_1/" + nomeArquivo);

        if ((DIR_DELETE.exists()) && (DIR_DELETE.isDirectory())) {
            DIR_DELETE.delete();
        }
    }

    public static void handleEntryDelete(String nomeArquivo) {
        File file_delete = new File("server_1/" + nomeArquivo);
        file_delete.delete();
    }

    public static void handleEntryCreateAndModify(String nomeArquivo) throws IOException {
        int filesize = 6022386;
        //long start = System.currentTimeMillis();
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
            bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
            if (bytesRead >= 0) current += bytesRead;
        } while (bytesRead > -1);

        bos.write(mybytearray, 0, current);

        //fos.close();
        bos.close();

        /*fim programa de transferencia de arquivo*/
    }

    public static void copyFolder(Path src, Path dest) throws IOException {
        try (Stream<Path> stream = Files.walk(src)) {
            stream.forEach(source -> copy(source, dest.resolve(src.relativize(source))));
        }
    }

    private static void copy(Path source, Path dest) {
        try {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}