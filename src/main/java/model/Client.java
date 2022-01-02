package model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private ObjectOutputStream clientOutput;
    private ObjectInputStream clientInput;
    private Socket socket;
    private String name;
    private boolean isMatch;
    
	public Client(Socket s,String name) throws IOException {
		this.socket = s;
		this.name = name;
		clientOutput = new ObjectOutputStream(socket.getOutputStream());
		clientInput = new ObjectInputStream(socket.getInputStream());
	}

    public ObjectOutputStream getClientOutput() {
		return clientOutput;
	}

	public void setClientOutput(ObjectOutputStream clientOutput) {
		this.clientOutput = clientOutput;
	}

	public ObjectInputStream getClientInput() {
		return clientInput;
	}

	public void setClientInput(ObjectInputStream clientInput) {
		this.clientInput = clientInput;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMatch() {
		return isMatch;
	}

	public void setMatch(boolean isMatched) {
		this.isMatch = isMatched;
	}

	public void closeAll() throws IOException {
		socket.close();
	}
	
	public Message receiveMessage() throws ClassNotFoundException, IOException {
    	Message mess = (Message) clientInput.readObject();
		return mess;
    }

    public void sendMessage(Message mess) throws IOException {
    	clientOutput.writeObject(mess);
    	clientOutput.flush();
    }
    
}
