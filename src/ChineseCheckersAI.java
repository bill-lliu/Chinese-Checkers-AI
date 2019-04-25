
class ChatClient {
  
  
  public static void main(String[] args) { 
    new ChatClient().go();
  }
    
  public void go() {
    JFrame window = new JFrame("Chat Client");
    southPanel = new JPanel();
    southPanel.setLayout(new GridLayout(2,0));
    
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
    
    window.add(BorderLayout.CENTER,msgArea);
    window.add(BorderLayout.SOUTH,southPanel);
    
    window.setSize(400,400);
    window.setVisible(true);
    
    // call a method that connects to the server 
    connect("127.0.0.1",5000);
    // after connecting loop and keep appending[.append()] to the JTextArea
    
    readMessagesFromServer();
    
  }
  
  //Attempts to connect to the server and creates the socket and streams
  public Socket connect(String ip, int port) { 
    System.out.println("Attempting to make a connection..");
    
    try {
      mySocket = new Socket("127.0.0.1",5000); //attempt socket connection (local address). This will wait until a connection is made
      
      InputStreamReader stream1= new InputStreamReader(mySocket.getInputStream()); //Stream for network input
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
    
  while(running) {  // loop unit a message is received
      try {
        
        if (input.ready()) { //check for an incoming messge
          String msg;          
          msg = input.readLine(); //read the message
          msgArea.append(msg+"\n");
        }
        
      }catch (IOException e) { 
        System.out.println("Failed to receive msg from the server");
        e.printStackTrace();
      }
}
     try {  //after leaving the main loop we need to close all the sockets
      input.close();
      output.close();
      mySocket.close();
    }catch (Exception e) { 
      System.out.println("Failed to close socket");
    }
  
  }
  //****** Inner Classes for Action Listeners ****
  
    // send - send msg to server (also flush), then clear the JTextField
    class SendButtonListener implements ActionListener { 
     public void actionPerformed(ActionEvent event)  {
        //Send a message to the client
       output.println(typeField.getText());
       output.flush();             
       typeField.setText(""); 
     }     
   }

  // QuitButtonListener - Quit the program
     class QuitButtonListener implements ActionListener { 
     public void actionPerformed(ActionEvent event)  {
       running=false;
     }     
   }

  
}



/* [ChineseCheckersAI.java]
 * Client class that connects to a server to act as a bot playing chinese checkers
 * @author Bill Liu, Feng Guo, Victor Lin
 * April 23, 2019
 */

//imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.UnknownHostException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.ArrayList;
import java.util.Collection; //Why do we need this - we dont

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;

//main class for the server
public class ChineseCheckersAI {
  
  //stuff we dont need
	/*
	private JButton sendButton, clearButton;
	private JTextField typeField;
	private JTextArea msgArea;  
	private JPanel southPanel;
	*/
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	private boolean running = true; //thread status via boolean
  
	//GUI 
	JFrame mainFrame;
	JPanel startPanel;
	
	//Server 
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	
	
	private boolean running = true; //thread status via boolean
	
	public static void main(String [] args) {
	    new ChineseCheckersAI().setUp();
	}
	
	private void connectToServer(String ip, int port) {
		try {
			mySocket = new Socket(ip, port);	
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*setUp
	 *initiating function to start up the server
	 */
	private void setUp(){
		//initiating display items
		mainFrame = new JFrame("Chinese Checkers AI");
		mainFrame.setVisible(true);
		mainFrame.setSize(400,400);
		
		//initiating game items
		int[][] gameBoard = new int[25][17];//size of triangle [row][column]
		
		startPanel = new JPanel();
		startPanel.setVisible(true);
		JLabel serverIPLabel = new JLabel("Enter the IP Address");
		JTextField serverIPTextField = new JTextField(20);
		JLabel portLabel = new JLabel("Enter the port");
		JTextField portTextField = new JTextField(10);
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				try {
					String ip = serverIPTextField.getText();
					int port = Integer.parseInt(portTextField.getText());
					if (ip != null && !"".equals(ip)) {
						connectToServer(ip, port);
					} else {
						System.out.println("IP Address is blank!");
					}
				} catch (NumberFormatException e) {
					System.out.println("Not a valid port!");
					e.printStackTrace();
				} 
			}
		});
		
		startPanel.add(serverIPLabel);
		startPanel.add(serverIPTextField);
		startPanel.add(portLabel);
		startPanel.add(portTextField);
		startPanel.add(okayButton);
		mainFrame.add(startPanel);
		repaintFrame();
	}
	
	private void repaintFrame() {
		mainFrame.setVisible(true);
		mainFrame.repaint();
	}
	
	
	/*play
	 *main function to run when it is now our turn
	 */
	public String play(gameBoard) {
		return bestMove(gameBoard);
		
	}
}
