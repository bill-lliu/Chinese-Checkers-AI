/* [ChatClient.java]
 * A not-so-pretty implementation of a basic chat client
 * @author Mangat
 * @ version 1.0a
 */

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

class ChatClient {

	private JButton sendButton, clearButton;
	private JTextField typeField;
	private JTextArea msgArea;
	private JPanel southPanel;
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	private boolean running = true; //thread status via boolean

	public static void main(String[] args) {
		new ChatClient().go();
	}

	public void go() {
		JFrame window = new JFrame("Chat Client");
		southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(2, 0));

		sendButton = new JButton("SEND");
		sendButton.addActionListener(new SendButtonListener());
		clearButton = new JButton("QUIT");
		clearButton.addActionListener(new QuitButtonListener());

		JLabel errorLabel = new JLabel("");

		typeField = new JTextField(10);

		msgArea = new JTextArea();

		southPanel.add(typeField);
		southPanel.add(sendButton);
		southPanel.add(errorLabel);
		southPanel.add(clearButton);

		window.add(BorderLayout.CENTER, msgArea);
		window.add(BorderLayout.SOUTH, southPanel);

		window.setSize(400, 400);
		window.setVisible(true);

		// call a method that connects to the server
		connect("127.0.0.1", 5000);
		// after connecting loop and keep appending[.append()] to the JTextArea

		readMessagesFromServer();

	}

	//Attempts to connect to the server and creates the socket and streams
	public Socket connect(String ip, int port) {
		System.out.println("Attempting to make a connection..");

		try {
			mySocket = new Socket("127.0.0.1", 5000); //attempt socket connection (local address). This will wait until a connection is made

			InputStreamReader stream1 = new InputStreamReader(mySocket.getInputStream()); //Stream for network input
			input = new BufferedReader(stream1);
			output = new PrintWriter(mySocket.getOutputStream()); //assign printwriter to network stream

		} catch (IOException e) {  //connection error occured
			System.out.println("Connection to Server Failed");
			e.printStackTrace();
		}

		System.out.println("Connection made.");
		return mySocket;
	}

	//Starts a loop waiting for server input and then displays it on the textArea
	public void readMessagesFromServer() {

		while (running) {  // loop unit a message is received
			try {

				if (input.ready()) { //check for an incoming messge
					String msg;
					msg = input.readLine(); //read the message
					msgArea.append(msg + "\n");
				}

			} catch (IOException e) {
				System.out.println("Failed to receive msg from the server");
				e.printStackTrace();
			}
		}
		try {  //after leaving the main loop we need to close all the sockets
			input.close();
			output.close();
			mySocket.close();
		} catch (Exception e) {
			System.out.println("Failed to close socket");
		}

	}
	//****** Inner Classes for Action Listeners ****

	// send - send msg to server (also flush), then clear the JTextField
	class SendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			//Send a message to the client
			output.println(typeField.getText());
			output.flush();
			typeField.setText("");
		}
	}

	// QuitButtonListener - Quit the program
	class QuitButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			running = false;
		}
	}


}