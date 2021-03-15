import java.io.*;
import java.net.Socket;

public class Thread extends java.lang.Thread {

    private String host;
    private int port;
    private String nomeArquivo;
    private String evento;
    private Socket socket;

    public Thread(String host, int port, String nomeArquivo, String evento){
        this.host = host;
        this.port = port;
        this.nomeArquivo = nomeArquivo;
        this.evento = evento;
        this.socket = connectToBackupServer(this.host, this.port);
    }

    public void run() {
        System.out.println("Tread: " + this.getName());

        switch(this.evento){
            case "ENTRY_CREATE":
            case "ENTRY_MODIFY": {
                handleCreateAndModify(this.nomeArquivo);
                break;
            }
            case "ENTRY_DELETE": {
                handleDelete(this.nomeArquivo);
                break;
            }
            case "DIR_CREATE": {
                try {
                    handleDirCreate(nomeArquivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case "DIR_DELETE": {
                try {
                    handleDirDelete(nomeArquivo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }

    public Socket connectToBackupServer(String host, int port){
        Socket socket = null;
        try {
            return new Socket(host, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void handleCreateAndModify(String nomeArquivo){
        // Enviando nome do arquivo para o servidor
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(this.socket.getOutputStream());
            dos.writeUTF(this.evento + "/" + nomeArquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Enviando arquivo para o servidor
        File myFile = new File("server_1/" + nomeArquivo);

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

        System.out.println("Arquivo " + nomeArquivo + " transferido para servidor de backup " + this.getName().split("-")[1] + "\n");

        try {
            os.flush();
            os.close();
            bis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDelete(String nomeArquivo){
        /*
        File file = new File("server_1/" + nomeArquivo);
        file.delete();
         */

        // Enviando mensagem para o servidor

        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(this.socket.getOutputStream());
            dos.writeUTF(this.evento + "/" + nomeArquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleDirCreate(String nomeArquivo) throws IOException {
        DataOutputStream mensagem = new DataOutputStream(socket.getOutputStream());
        mensagem.writeUTF("DIR_CREATE/" + nomeArquivo); //Enviar  mensagem para o servidor
    }

    public void handleDirDelete(String nomeArquivo) throws IOException {
        DataOutputStream mensagem = new DataOutputStream(socket.getOutputStream());
        mensagem.writeUTF( "DIR_DELETE/" + nomeArquivo); //Enviar  mensagem para o servidor
    }
}
