/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.net.Socket;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;

import java.nio.file.attribute.*;
import java.io.*;
import java.util.*;

/**
 * Exemplo para observar um diretório (ou árvore) em busca de alterações nos arquivos.
 */
public class WatchDir {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean recursive;
    private boolean trace = false;

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * Register the given directory with the WatchService
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (trace) {
            Path prev = keys.get(key);
            if (prev == null) {
                System.out.format("Pasta register: %s\n", dir);
            } else {
                if (!dir.equals(prev)) {
                    System.out.format("Pasta update: %s -> %s\n", prev, dir);
                }
            }
        }
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the
     * WatchService.
     */
    private void registerAll(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a WatchService and registers the given directory
     */
    WatchDir(Path dir, boolean recursive) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();
        this.recursive = recursive;

        if (recursive) {
            System.out.format("Observando diretorio %s ...\n", dir);
            registerAll(dir);
            // System.out.println("Done.");
        } else {
            register(dir);
        }

        // enable trace after initial registration
        this.trace = true;
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() throws IOException {
        for (; ; ) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                //System.out.println("name  "+name);
                // System.out.println("child  "+child);

                // print out event
                System.out.format("Evento  %s: %s\n", event.kind().name(), child);

                if (event.kind().name() == "ENTRY_CREATE" || event.kind().name() == "ENTRY_MODIFY" || event.kind().name() == "ENTRY_DELETE") {

                    System.out.println("ENTRY_CREATE");
                    // System.out.format("Evento dentro do if  %s: %s\n", event.kind().name(), child);

                    //1 - Abrir conexão
                    Socket socket = new Socket("127.0.0.1", 11111);

                    if (event.kind().name() == "ENTRY_DELETE"){

                        File myFile = new File(String.valueOf(child));
                        if (myFile.isDirectory()) {
                            System.out.println("Deletando Diretorio");
                            //2 - Definir stream de saída de dados do cliente
                            DataOutputStream nomeArquivo = new DataOutputStream(socket.getOutputStream());
                            nomeArquivo.writeUTF(String.valueOf(name) + "/DIR_DELETE"); //Enviar  mensagem para o servidor

                            //3 - Definir stream de entrada de dados no cliente
                            DataInputStream entrada = new DataInputStream(socket.getInputStream());
                            String novaMensagem = entrada.readUTF();//Receber mensagem em maiúsculo do servidor
                            System.out.println("O diretorio " + novaMensagem + " foi recebido no servido!"); //Mostrar mensagem em maiúsculo no cliente
                            System.out.println("child: " + child + "\n");
                            entrada.close();

                        }else{
                            System.out.println("Deletando Arquivo");

                            //2 - Definir stream de saída de dados do cliente
                            DataOutputStream nomeArquivo = new DataOutputStream(socket.getOutputStream());
                            nomeArquivo.writeUTF(String.valueOf(name) + "/" + event.kind().name()); //Enviar  mensagem para o servidor

                            //3 - Definir stream de entrada de dados no cliente
                            DataInputStream entrada = new DataInputStream(socket.getInputStream());
                            String novaMensagem = entrada.readUTF();//Receber mensagem em maiúsculo do servidor
                            System.out.println("O arquivo " + novaMensagem + " foi recebido no servido!"); //Mostrar mensagem em maiúsculo no cliente
                            System.out.println("child: " + child + "\n");
                            entrada.close();
                        }


                    }

                    if (event.kind().name() == "ENTRY_CREATE" || event.kind().name() == "ENTRY_MODIFY") {
                        // envia o arquivo (transforma em byte array)
                        File myFile = new File(String.valueOf(child));

                        if (myFile.isDirectory()) {
                            System.out.println("Directory");
                            //2 - Definir stream de saída de dados do cliente
                            DataOutputStream nomeArquivo = new DataOutputStream(socket.getOutputStream());
                            nomeArquivo.writeUTF(String.valueOf(name) + "/DIR_CREATE"); //Enviar  mensagem para o servidor

                            //3 - Definir stream de entrada de dados no cliente
                            DataInputStream entrada = new DataInputStream(socket.getInputStream());
                            String novaMensagem = entrada.readUTF();//Receber mensagem em maiúsculo do servidor
                            System.out.println("O diretorio " + novaMensagem + " foi recebido no servido!"); //Mostrar mensagem em maiúsculo no cliente
                            System.out.println("child: " + child + "\n");
                            entrada.close();

                        } else {
                            System.out.println("is not Directory");

                            //2 - Definir stream de saída de dados do cliente
                            DataOutputStream nomeArquivo = new DataOutputStream(socket.getOutputStream());
                            nomeArquivo.writeUTF(String.valueOf(name) + "/" + event.kind().name()); //Enviar  mensagem para o servidor

                            //3 - Definir stream de entrada de dados no cliente
                            DataInputStream entrada = new DataInputStream(socket.getInputStream());
                            String novaMensagem = entrada.readUTF();//Receber mensagem em maiúsculo do servidor
                            System.out.println("O arquivo " + novaMensagem + " foi recebido no servido!"); //Mostrar mensagem em maiúsculo no cliente
                            //System.out.println("child: " + child + "\n");

                            byte[] mybytearray = new byte[(int) myFile.length()];
                            System.out.println("myFile: " + myFile.toString());

                            FileInputStream fis = new FileInputStream(myFile);
                            BufferedInputStream bis = new BufferedInputStream(fis);
                            bis.read(mybytearray, 0, mybytearray.length);
                            OutputStream os = socket.getOutputStream();
                            System.out.println("Enviado");
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            os.close();
                            bis.close();
                            entrada.close();

                        }

                    }

                    //Fechar

                    socket.close();
                }
                // if directory is created, and watching recursively, then
                // register it and its sub-directories
                if (recursive && (kind == ENTRY_CREATE)) {
                    try {
                        if (Files.isDirectory(child, NOFOLLOW_LINKS)) registerAll(child);
                    } catch (IOException x) {
                        // ignore to keep sample readbale
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        new WatchDir(Paths.get("cliente_obs"), true).processEvents();

    }
}
