package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NioServer {

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9090);
        System.out.println("Server started");
        while (true){
            Socket socket = serverSocket.accept();
            Handler handler = new Handler(socket);
            new Thread(handler).start();
        }
    }
}
