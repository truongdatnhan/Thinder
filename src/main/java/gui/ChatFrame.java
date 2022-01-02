package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import model.Client;
import model.Message;
import model.Status;

import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JList;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;


public class ChatFrame extends JFrame {

	public int i = 0;
	public JPanel mainPanel;
	public JPanel insideCenter;
	public String ipAddress;
	public int port;
	private JScrollPane scroll;
	private JButton btnSend;
	private JList<String> list;
	private JButton btnConnect;
	private JButton btnMatch;
	private JButton btnDisconnect;
	private JTextArea ChatArea;
	private JTextField txMessage;
	private JTextField txName;
	private Client client;
	private JLabel lbBrand;
	private JLabel lbLogo;
	private volatile boolean running = true;

	public ChatFrame() throws UnknownHostException, IOException {
		setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (client != null) {
					try {
						running = false;
						Message send = new Message(null, null, Status.EXIT);
						System.out.println(send);
						client.sendMessage(send);
						client.closeAll();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});

		
		this.setIconImage(ImageIO.read(new File("logo.png")));
		setResizable(false);
		setSize(800, 600);

		setLocationRelativeTo(null);
		setTitle("Thinder");
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new BorderLayout(0, 0));
		setContentPane(mainPanel);

		ipAddress = "localhost";
		port = 666;

		insideCenter = new JPanel();
		insideCenter.setBorder(new LineBorder(Color.WHITE, 0, true));
		insideCenter.setPreferredSize(new Dimension(180, 650));
		insideCenter.setBackground(new Color(51, 123, 232));
		insideCenter.setLayout(null);
		mainPanel.add(insideCenter, BorderLayout.CENTER);

		ChatArea = new JTextArea();
		ChatArea.setEditable(false);

		Thread readMessage = new Thread(() -> {
			while (running) {
				try {
					Message receivedMessage = client.receiveMessage();
					System.out.println(receivedMessage);
					switch (receivedMessage.getStatus()) {
					case MATCH:
						int action = JOptionPane.showConfirmDialog(null,
								"Bạn có muốn ghép đôi với " + receivedMessage.getName() + "?",
								"Ghép đôi thành công", JOptionPane.YES_NO_OPTION);
						if (action == JOptionPane.OK_OPTION) {
							Message accept = new Message(client.getName(), null, Status.OK);
							client.sendMessage(accept);
							client.setMatch(true);
							btnMatch.setEnabled(false);
							btnDisconnect.setEnabled(true);
							ChatArea.setText("");
						} else {
							Message refuse = new Message(client.getName(), null, Status.REFUSE);
							client.sendMessage(refuse);
							btnMatch.setEnabled(true);
						}
						break;
					case CHAT:
						ChatArea.append(receivedMessage.getName() + " : " + receivedMessage.getData() + "\n");
						break;
					case EXIST:
						JOptionPane.showMessageDialog(null, "Tên trùng với người khác !", "Thông báo",
								JOptionPane.ERROR_MESSAGE);
						break;
					case UNMATCH:
						if(client.isMatch()) {
							JOptionPane.showMessageDialog(null,
									"Người kia đã thoát, bạn sẽ quay lại hàng chờ !", "Thông báo",
									JOptionPane.ERROR_MESSAGE);
						}else {
							JOptionPane.showMessageDialog(null,
									"Người kia đã từ chối ghép đôi, bạn sẽ quay lại hàng chờ !", "Thông báo",
									JOptionPane.ERROR_MESSAGE);
						}
						client.setMatch(false);
						btnMatch.setEnabled(true);
						btnDisconnect.setEnabled(false);
						break;
					case EXIT:
						JOptionPane.showMessageDialog(null,
								"Người kia đã thoát khỏi phòng chat, bạn sẽ quay lại hàng chờ !",
								"Thông báo", JOptionPane.ERROR_MESSAGE);
						client.setMatch(false);
						btnMatch.setEnabled(true);
						btnDisconnect.setEnabled(false);
						break;
					case CONNECTED:
						btnConnect.setEnabled(false);
						txName.setEditable(false);
						break;
					default:
						List<String> listAll = (List<String>) client.getClientInput().readObject();
						DefaultListModel<String> listModel = new DefaultListModel<String>();
						listModel.clear();
						listModel.addAll(listAll);
						list.setModel(listModel);
					}

				} catch (IOException | ClassNotFoundException e1) {
					System.out.println();
				}
			}

		});
		
		scroll = new JScrollPane(ChatArea);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(29, 70, 619, 348);
		insideCenter.add(scroll);

		txMessage = new JTextField();
		txMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (txMessage.getText().length() != 0) {
					btnSend.setEnabled(true);
				} else {
					btnSend.setEnabled(false);
				}
			}
		});
		txMessage.setBounds(29, 474, 619, 63);
		insideCenter.add(txMessage);
		txMessage.setColumns(10);

		btnSend = new JButton("Gửi");
		btnSend.setEnabled(false);
		btnSend.addActionListener(e -> {
			try {
				if (client == null) {
					JOptionPane.showMessageDialog(null, "Chưa kết nối mà send !", "Thông báo",
							JOptionPane.ERROR_MESSAGE);
				} else {
					if (client.isMatch()) {
						if (!txMessage.getText().isEmpty()) {
							Message send = new Message(client.getName(), txMessage.getText(), Status.CHAT);
							client.sendMessage(send);
							ChatArea.append(client.getName() + " : " + txMessage.getText() + "\n");
							txMessage.setText("");
							btnSend.setEnabled(false);
						} else {
							JOptionPane.showMessageDialog(null, "Tin nhắn không được để trống !", "Thông báo",
									JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(null, "Chưa ghép đôi mà send !", "Thông báo",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		});
		btnSend.setBounds(668, 474, 108, 63);
		insideCenter.add(btnSend);

		list = new JList();
		list.setBounds(668, 70, 108, 348);
		insideCenter.add(list);

		txName = new JTextField();
		txName.setBounds(29, 21, 248, 28);
		insideCenter.add(txName);
		txName.setColumns(10);

		btnConnect = new JButton("Kết nối");
		btnConnect.addActionListener(e -> {

			if (txName.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Không được để trống tên !", "Thông báo",
						JOptionPane.ERROR_MESSAGE);
			} else {
				try {
					if (client == null) {
						client = new Client(new Socket(ipAddress, port), txName.getText());
						Message welcome = new Message(txName.getText(), null, Status.CONNECT);
						client.sendMessage(welcome);
						readMessage.start();
					} else {
						Message welcome = new Message(txName.getText(), null, Status.CONNECT);
						client.sendMessage(welcome);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		});
		btnConnect.setBounds(305, 20, 85, 30);
		insideCenter.add(btnConnect);

		btnMatch = new JButton("Ghép đôi");
		btnMatch.setEnabled(false);
		btnMatch.setBounds(400, 20, 85, 30);
		btnMatch.addActionListener(e -> {
				if (!client.isMatch()) {
					try {
						Message match = new Message(client.getName(), null, Status.MATCH);
						client.sendMessage(match);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					JOptionPane.showMessageDialog(null,
							"Bạn đang ghép đôi !",
							"Thông báo", JOptionPane.ERROR_MESSAGE);
				}
		});
		insideCenter.add(btnMatch);
		
		btnDisconnect = new JButton("Ngắt kết nối");
		btnDisconnect.addActionListener(e -> {
			if(client.isMatch()) {
				try {
					Message unMatch = new Message(client.getName(), null, Status.UNMATCH);
					client.sendMessage(unMatch);
					client.setMatch(false);
					btnDisconnect.setEnabled(false);
					btnMatch.setEnabled(true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnDisconnect.setBounds(495, 20, 95, 30);
		btnDisconnect.setEnabled(false);
		insideCenter.add(btnDisconnect);
		
		lbBrand = new JLabel("Thinder");
		lbBrand.setFont(new Font("Chalet", Font.PLAIN, 33));
		lbBrand.setBounds(661, 12, 115, 43);
		insideCenter.add(lbBrand);
		
		BufferedImage myPicture = ImageIO.read(new File("logo_small.png"));
		lbLogo = new JLabel(new ImageIcon(myPicture));
		lbLogo.setBounds(600, 5, 55, 55);
		insideCenter.add(lbLogo);
	}
}
