package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;


public class Server {
    static Map<String, ClientHandler> listWait = new LinkedHashMap<>();
    static Map<String, ClientHandler> listMatched = new LinkedHashMap<>();
    static String duaVaoSau;
    
    public static void main(String args[]){
        try(ServerSocket serverSocket = new ServerSocket(666);){
            Socket clienSocket ;
            while(true){
                System.out.println("Server waiting for client");
                clienSocket = serverSocket.accept();
                System.out.println("New client request received : " + clienSocket);
                new ClientHandler(clienSocket).start();
            }
        }
        catch (IOException e) {
        	e.printStackTrace();
        }

    }
}
