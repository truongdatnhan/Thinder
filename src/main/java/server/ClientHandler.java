package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import model.Message;
import model.Status;

public class ClientHandler extends Thread {
	private final Socket socket;
	private final ObjectInputStream serverInput;
	private final ObjectOutputStream serverOutput;
	private volatile boolean running = true;
	private List<String> rejected = new ArrayList<>();
	private String username;
	private String peerInfo;

	public ClientHandler(Socket socket) throws IOException {
		this.socket = socket;
		this.serverInput = new ObjectInputStream(socket.getInputStream());
		this.serverOutput = new ObjectOutputStream(socket.getOutputStream());
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPeerInfo() {
		return peerInfo;
	}

	public void setPeerInfo(String peerInfo) {
		this.peerInfo = peerInfo;
	}

	@Override
	public void run() {
		Message received;
		try (socket; serverInput; serverOutput;) {
			while (running) {
				received = (Message) serverInput.readObject();
				System.out.println(received);
				switch (received.getStatus()) {
				case CHAT:
					if (peerInfo != null && !received.getData().isEmpty()) {
						Server.listMatched.get(peerInfo).sendMessage(received);
					}
					break;
				case OK:
					//Server.listMatched.put(username, this);
					break;
				case EXIT:
					if (peerInfo == null) {
						Server.listWait.remove(username);
					} else {
						System.out.println("PeerInfo của " + username +" là " +peerInfo);
						ClientHandler b = Server.listMatched.get(peerInfo);
						if (b != null && b.getPeerInfo().equals(username)) {
							System.out.println("gửi message thoát");
							Message unmatch = new Message(username, null, Status.EXIT);
							b.sendMessage(unmatch);
							removeMatched();
						}
						Server.listWait.remove(username);
					}
					sendList();
					running = false;
					break;
				case REFUSE:{
					ClientHandler b = Server.listMatched.get(peerInfo) != null ? Server.listMatched.get(peerInfo) : Server.listWait.get(peerInfo);
					rejected.add(peerInfo);
					removeMatched();
					Message unmatch = new Message(username, null, Status.UNMATCH);
					b.sendMessage(unmatch);
					System.out.println("gửi message từ chối");
					if (Server.listWait.size() - 1 > rejected.size() && Server.duaVaoSau.equals(username)) {
						matching();
					}
				}
					break;
				case MATCH:
					if (Server.listWait.size() - 1 > rejected.size()) {
						matching();
					}
					break;
				case UNMATCH:{
					ClientHandler b = Server.listMatched.get(peerInfo) != null ? Server.listMatched.get(peerInfo) : Server.listWait.get(peerInfo);
					removeMatched();
					Message unmatch = new Message(username, null, Status.UNMATCH);
					b.sendMessage(unmatch);
				}
					break;
				case CONNECT:
					if (Server.listMatched.get(received.getName()) != null || Server.listWait.get(received.getName()) != null) {
						Message mess = new Message(username, null, Status.EXIST);
						sendMessage(mess);
					} else if (username == null) {
						username = received.getName();
						Server.listWait.put(username, this);
						Message welcome = new Message(username, null, Status.CONNECTED);
						System.out.println(welcome);
						sendMessage(welcome);
						Server.duaVaoSau = username;
						matching();
					}
					break;
				default:
				}
			}
			System.out.println("ClientHandler :" + username + " đã tắt !");
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void sendMessage(Object mess) throws IOException {
		serverOutput.writeObject(mess);
		serverOutput.flush();
	}

	public void matching() throws IOException, InterruptedException {
		System.out.println("Vào ghép đôi");
		System.out.println(username);
		synchronized (this) {
			sendList();
			if (Server.listWait.size() > 1) {
				Server.listWait.remove(username);
				ArrayList<ClientHandler> list = new ArrayList<>(Server.listWait.values());
				list.removeIf(x -> rejected.contains(x.getUsername()));
				Collections.shuffle(list);
				ClientHandler benB = list.get(0);
				setPeerInfo(benB.getUsername());
				benB.setPeerInfo(username);
				Server.listMatched.put(username, this);
				Server.listMatched.put(benB.getUsername(), benB);
				Message match = new Message(peerInfo, null, Status.MATCH);
				sendMessage(match);
				match.setName(username);
				benB.sendMessage(match);
				Server.listWait.remove(benB.getUsername());
				System.out.println("Ghép đôi thành công");
			}
		}
	}

	public void sendList() {
		List<String> listAll = new ArrayList<>();
		listAll.addAll(Server.listWait.keySet());
		listAll.addAll(Server.listMatched.keySet());
		
		Message listM = new Message(peerInfo, null, Status.LIST);
		Server.listWait.values().forEach(x -> {
			try {
				x.sendMessage(listM);
				x.sendMessage(listAll);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		
		Server.listMatched.values().forEach(x -> {
			try {
				x.sendMessage(listM);
				x.sendMessage(listAll);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public void removeMatched() {
		if (Server.listMatched.get(peerInfo) != null) {
			Server.listMatched.get(peerInfo).goToWaitList();
			goToWaitList();
		}
		setPeerInfo(null);
	}
	
	public void goToWaitList() {
		Server.listMatched.remove(username);
		Server.listWait.put(username, this);
	}

}
