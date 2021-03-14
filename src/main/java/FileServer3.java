import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer3 {

    public static void main(String[] args){

        try (ServerSocket serverSocket = new ServerSocket(33333)) {
            int filesize = 6022386;

            while (true) {
                System.out.println("A porta 54323 foi aberta!\n");
                System.out.println("Servidor pronto para nova conexão\n");

                Socket socket = serverSocket.accept();

                System.out.println("Servidor 1 aceito\nConexão estabelecida\n");

                // Lendo nome do arquivo
                DataInputStream entrada = new DataInputStream(socket.getInputStream());
                String nomeArquivo = entrada.readUTF();

                long start = System.currentTimeMillis();
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

                System.out.println("Transferência para o servidor 3 bem sucedida!\n");

                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
