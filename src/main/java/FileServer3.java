import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer3 {

    private static Socket socket;

    public static void main(String[] args){

        try (ServerSocket serverSocket = new ServerSocket(33333)) {
            int filesize = 6022386;

            while (true) {
                System.out.println("Porta 33333 aberta!\n");
                System.out.println("Servidor pronto para estabelecer conexão\n");

                socket = serverSocket.accept();

                System.out.println("Servidor 1 aceito\nConexão estabelecida\n");

                // Lendo mensagem
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String mensagem = entrada.readUTF();

                String[] splitted = mensagem.split("/");

                String nomeArquivo = splitted[1];
                String evento = splitted[0];

                switch (evento){
                    case "DIR_CREATE": {
                        handleDirCreate(nomeArquivo);
                        break;
                    }
                    case "DIR_DELETE": {
                        handleDirDelete(nomeArquivo);
                        break;
                    }
                    case "ENTRY_CREATE":
                    case "ENTRY_MODIFY": {
                        handleCreateAndModify(nomeArquivo);
                        socket.close();
                        break;
                    }
                    case "ENTRY_DELETE": {
                        handleDelete(nomeArquivo);
                        socket.close();
                        break;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void handleCreateAndModify(String nomeArquivo) throws IOException {
        int filesize = 6022386;
        int bytesRead;
        int current = 0;

        // recebendo o arquivo
        byte[] mybytearray = new byte[filesize];

        InputStream is = socket.getInputStream();

        FileOutputStream fos = new FileOutputStream("server_3/" + nomeArquivo);

        BufferedOutputStream bos = new BufferedOutputStream(fos);

        bytesRead = is.read(mybytearray, 0, mybytearray.length);
        current = bytesRead;

        do {
            bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
            if (bytesRead >= 0) current += bytesRead;
        } while (bytesRead > -1);

        bos.write(mybytearray, 0, current);
        long end = System.currentTimeMillis();
        //System.out.println(end - start);

        bos.close();

        //System.out.println("Transferência para o servidor 2 realizada\n");
    }

    public static void handleDelete(String nomeArquivo) {
        File file_delete = new File("server_3/" + nomeArquivo);
        file_delete.delete();
    }

    public static void handleDirCreate(String nomeArquivo) {
        File DIR_CREATE = new File("server_3/" + nomeArquivo);
        DIR_CREATE.mkdir();
    }

    public static void handleDirDelete(String nomeArquivo) {
        File DIR_DELETE = new File("server_3/" + nomeArquivo);
        if ((DIR_DELETE.exists()) && (DIR_DELETE.isDirectory())){
            DIR_DELETE.delete();
        }
    }

}
