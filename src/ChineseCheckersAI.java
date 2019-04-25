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
import java.util.Collection; //Why do we need this

import javax.swing.JButton;
import javax.swing.JPanel;
//import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


//main class for the server
public class ChineseCheckersAI {
  
  //stuff we dont need
//  private JButton sendButton, clearButton;
//  private JTextField typeField;
//  private JTextArea msgArea;  
//  private JPanel southPanel;
  
  
	//GUI 
	private JFrame mainFrame;
	private JPanel startPanel;

	//Server 
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	
	
	private boolean running = true; //thread status via boolean

	//Moving + Scoring
	private int bestScore;
	final int PHASE_ONE = 0;
	final int PHASE_TWO = 1;
	
	public static void main(String [] args) {
		ChineseCheckersAI chineseCheckersAI = new ChineseCheckersAI();
	}
	
	ChineseCheckersAI(){
		setUp();
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
		int[][] gameBoard = new int[30][30];//FIX THESE NUMBERS
		
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
//	public String play() {
//		return bestMove(gameTable);
//
//	}

	public int move(int r, int c, int phase) {
		if (phase == PHASE_ONE) {
			if (isLegalMove(r-1, c)) {
				move(r-1, c, PHASE_TWO);
			}
			if (isLegalMove(r-1, c-1)) {
				move(r-1,c-1, PHASE_TWO);
			}
			if (isLegalMove(r, c-1)) {
				move(r, c-1, PHASE_TWO);
			}
			if (isLegalMove(r+1, c)) {
				move(r+1, c, PHASE_TWO);
			}
			if (isLegalMove(r+1, c+1)) {
				move(r+1, c+1, PHASE_TWO);
			}
			if (isLegalMove(r, c+1)) {
				move(r, c+1, PHASE_TWO);
			}
		}
		if (phase == PHASE_TWO|| phase == PHASE_ONE) {
			//If it is an illegal move 1 adjacent, then it is either out of bounds or has a piece there
			//Check the jump piece if it is a legal move because 
			if (!isLegalMove(r-1, c)) {
				if (isLegalMove(r-2, c)) {
					move(r-2, c, PHASE_TWO);
				}
			}
			if (!isLegalMove(r-1, c-1)) {
				if (isLegalMove(r-2, c-2)) {
					move(r-2, c-2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r, c-1)) {
				if (isLegalMove(r, c-2)) {
					move(r, c-2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r+1, c)) {
				if (isLegalMove(r+2, c)) {
					move(r+2, c, PHASE_TWO);
				}
			}
			if (!isLegalMove(r+1, c+1)) {
				if (isLegalMove(r+2, c+2)) {
					move(r+2, c+2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r, c+1)) {
				if (isLegalMove(r, c+2)) {
					move(r, c+2, PHASE_TWO);
				}
			}

		} else {
			//lol fuck
		}
	}

	public boolean isLegalMove(int r, int c){
		return false;
	}
}
