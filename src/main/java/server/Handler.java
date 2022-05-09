package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements Runnable{
    private static final int BUFFERBYTE = 8192;
    private byte[]buffer;
    private Path currentDir;
    private DataInputStream is;
    private DataOutputStream os;

    public Handler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        currentDir = Paths.get("A");
        System.out.println("Client Accepted");
        sendServerFiles();
        buffer = new byte[BUFFERBYTE];
    }

    private List<String>getFileNames() throws IOException {
        return Files.list(currentDir)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
    }

    private void sendServerFiles() throws IOException {
        os.writeUTF("#List#");
        List<String>fileNames = getFileNames();
        os.writeInt(fileNames.size());
        for (String fileName : fileNames) {
            os.writeUTF(fileName);
        }
        os.flush();
    }

    @Override
    public void run() {
        try{
            while (true){
               String command = is.readUTF();
                System.out.println("Received command " + command);
                if(command.equals("#upload# ")){
                     String     fileName = is.readUTF();
                     long size = is.readLong();
                     try (FileOutputStream fos = new FileOutputStream(
                             currentDir.resolve(fileName).toFile())){
                         for (int i = 0; i < (size + BUFFERBYTE - 1)/BUFFERBYTE; i++) {
                             int read = is.read(buffer);
                             fos.write(buffer, 0, read);
                             
                         }

                     }
                     sendServerFiles();
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
