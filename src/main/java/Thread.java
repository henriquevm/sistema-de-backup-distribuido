import java.io.*;
import java.net.Socket;

public class Thread extends java.lang.Thread {

    private String host;
    private int port;
    private String nomeArq;


    public Thread(String host, int port, String nomeArquivo){
        this.host = host;
        this.port = port;
        nomeArq = nomeArquivo;
    }

    public void run() {
        System.out.println("Tread: " + this.getName());
        Socket socket = null;
        try {
            socket = new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enviando nome do arquivo para o servidor
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(nomeArq);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enviando arquivo para o servidor
        File myFile = new File("server_1/" + nomeArq);

        byte[] mybytearray = new byte[(int) myFile.length()];

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(myFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedInputStream bis = new BufferedInputStream(fis);
        try {
            bis.read(mybytearray, 0, mybytearray.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Arquivo " + nomeArq + " transferido para servidor de backup!\n");

        try {
            os.flush();
            os.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
