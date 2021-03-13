import java.io.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;


public class ThreadSockets extends Thread {
    private Socket socket;
    private InputStream in;
    public ThreadSockets(Socket s, InputStream in) {

        this.socket = s;
        this.in = in;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName());//Imprimir o nome da Thread
        try {

            //escrever um dado em algum local
            OutputStream out = null;

            //3 - Definir stream de entrada de dados no servidor
            String mensagem;
            try (DataInputStream entrada = new DataInputStream(socket.getInputStream())) {
                //receber mensagem em minúsculo do cliente
                mensagem = entrada.readUTF();
            }
            System.out.println("Recebe do cliente: "+mensagem);
/*
            try {
                in = socket.getInputStream();
            } catch (IOException ex) {
                System.out.println("Não é possível obter o fluxo de entrada do soquete ");
            }*/

            try {
                System.out.println("*** server_1/"+mensagem);
                out = new FileOutputStream("server_1/"+mensagem);
            } catch (FileNotFoundException ex) {
                System.out.println("File not found. ");
            }

            byte[] bytes = new byte[16*1024];

            int count;
            System.out.println("in.read(bytes) "+in.read(bytes));
            while ((count = in.read(bytes)) > 0) {
                out.write(bytes, 0, count);
            }
//}
            /*// 3 - Fechar streams de entrada e saída de dados
            out.close();
            in.close();
            //4 - Fechar socket de comunicação
            socket.close();
            //serverSocket.close();*/

            /*
            //1 - Definir stream de entrada de dados no servidor
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            String mensagem = entrada.readUTF();//Recebendo mensagem em Minúsculo do Cliente
            String novaMensagem = mensagem.toUpperCase(); //Convertendo em Maiúsculo


            //2 - Definir stream de saída de dados do servidor
            DataOutputStream saida = new DataOutputStream(socket.getOutputStream());
            saida.writeUTF(novaMensagem); //Enviando mensagem em Maiúsculo para Cliente*/

        } catch (IOException ioe) {
            System.out.println("Erro: " + ioe.toString());
        }
    }
}
