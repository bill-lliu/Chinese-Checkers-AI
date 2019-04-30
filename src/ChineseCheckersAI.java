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

import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JPanel;
//import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.util.ArrayList;


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
	private JPanel joinPanel;

	//Server 
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	
	
	private boolean running = true; //thread status via boolean

	//Game portions
	private int[][] gameBoard;
	private int[][] gamePieces;

	//Moving + Scoring
	private int[] bestScore; //0 is displacement, 1 is priority
	private ArrayList<Integer[]> bestMoveList;
	private final int PHASE_ONE = 0;
	private final int PHASE_TWO = 1;
	private String moveSent;
	private ArrayList<Integer[]> moveList;

	public static void main(String [] args) {
		ChineseCheckersAI chineseCheckersAI = new ChineseCheckersAI();
	}
	
	ChineseCheckersAI(){
		setUp();
	}
	
	private void connectToServer(String ip, int port) {
		try {
			mySocket = new Socket(ip, port);
			InputStreamReader stream = new InputStreamReader(mySocket.getInputStream());
			input = new BufferedReader(stream);
			output = new PrintWriter(mySocket.getOutputStream());
			joinRoom();
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
		//gameBoard = new int[30][30];//FIX THESE NUMBERS
		gamePieces = new int[10][2];//XY of our pieces (first index is piece number, second index is r or c)
		
		startPanel = new JPanel();
		startPanel.setVisible(true);
		JLabel serverIPLabel = new JLabel("Enter the IP Address");
		JTextField serverIPTextField = new JTextField(20);
		JLabel portLabel = new JLabel("Enter the port");
		JTextField portTextField = new JTextField(10);
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(actionEvent -> {
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

	private void joinRoom() {
		joinPanel = new JPanel();
		joinPanel.setVisible(true);

		JLabel roomLabel = new JLabel("Enter room name");
		JTextField roomTextField = new JTextField(20);
		JLabel nameLabel = new JLabel("Enter username");
		JTextField nameTextField = new JTextField(10);
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(actionEvent -> {
            String room = roomTextField.getText();
            String username = nameTextField.getText();
            output.println("JOINROOM " + room);
            output.flush();
            String msg = readMessagesFromServer();
            if (msg != null) {
            	if ("OK".equalsIgnoreCase(msg)) {
					output.println("CHOOSENAME " + username);
					output.flush();
					do {
						msg = readMessagesFromServer();
					} while (msg == null);
					if (msg != null) {
						if ("OK".equalsIgnoreCase((msg))) {
							//we gucci here
							runGameLoop();
						} else {
							System.out.println(msg);
						}
					} else {
						System.out.println("There was an error somewhere");
					}
				} else {
            		System.out.println(msg); //Server error message
				}
			} else {
            	System.out.println("There was an error somewhere");
			}

        });

		joinPanel.add(roomLabel);
		joinPanel.add(roomTextField);
		joinPanel.add(nameLabel);
		joinPanel.add(nameTextField);
		joinPanel.add(okayButton);
		mainFrame.remove(startPanel);
		mainFrame.add(joinPanel);

		bestMoveList = new ArrayList<>();
		moveList = new ArrayList<>();
		repaintFrame();
	}

	private String readMessagesFromServer() {
		//We're going to have to change this later
		//while (running) {  // loop unit a message is received
			try {
				if (input.ready()) { //check for an incoming messge
					String msg;
					msg = input.readLine(); //read the message
					System.out.println(msg);
					return msg;
				} else {
					return null;
				}
			} catch (IOException e) {
				System.out.println("Failed to receive msg from the server");
				e.printStackTrace();
				return null;
			}
		//}
		//We'll have to move this later
//		try {  //after leaving the main loop we need to close all the sockets
//			input.close();
//			output.close();
//			mySocket.close();
//		} catch (Exception e) {
//			System.out.println("Failed to close socket");
//		}

	}

	/** play
	 *main function to run when it is now our turn
	 */
	private void play() {
		for (int i=0; i<10; i++) {
			move(gamePieces[i][0], gamePieces[i][1], PHASE_ONE);
		}
		moveList.clear();
		moveSent = "MOVE ";
		StringBuilder s = new StringBuilder(moveSent);
		for (int i=0; i<bestMoveList.size(); i++) {
			Integer[] move = bestMoveList.get(i);
			String temp = "(" + move[0] + ", " + move[1] + ")";
			s.append(temp);
		}
		moveSent = s.toString();
	}

	private void runGameLoop(){
		//This is where we do the looping waiting for stuff
		while (running) {
			try {
				if (input.ready()) { //check for an incoming messge
					String msg = readMessagesFromServer();
					try {
						if (msg.indexOf("BOARD") > 0) {
							String[] msgSplit = msg.split(" ");
							resetBoard(msgSplit);
							play();
							output.println(moveSent);
							output.flush();
						} else if (msg.indexOf("ERROR") > 0) {
							System.out.println("Uh oh");
						} else if (msg.indexOf("OK") > 0) {
							System.out.println("Move Successfully sent.");
						}
					} catch (NullPointerException e) {
						System.out.println("Something broke");
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				//Nothing happens hopefully
			}
		}
	}

	private void resetBoard(String[] msgSplit) {
		gameBoard = new int[30][30];
		for (int i=3; i<msgSplit.length; i++) {
			String[] coords = msgSplit[i].split(",");
			coords[0] = coords[0].substring(1);
			coords[1] = coords[1].substring(0,coords[1].length()-1);
			int row = Integer.parseInt(coords[0]);
			int column = Integer.parseInt(coords[1]);
			gameBoard[row][column] = 1;
			if (i<13) {
				gamePieces[i-3][0] = row;
				gamePieces[i-3][1] = column;
			}
		}
	}

	private void move(int r, int c, int phase) {
		if (gameBoard[r][c] != 1) {
			gameBoard[r][c] = 2;
		}
		Integer[] move = new Integer[2];
		move[0] = r;
		move[1] = c;
		moveList.add(move);
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
		if (phase == PHASE_TWO || phase == PHASE_ONE) {
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

		}
		//Want to call scoring here!
		//int score = score(shit);
		//int priority shit //will figure out later

		//If the score is better, changes the stuff
		/*if (score > bestScore[0] || (score == bestScore[0] && priority > bestScore[1])) {
		copy the arraylist lol
		bestMoveList.clear();
		for (int i=0; i<moveList.size(); i++) {
			bestMoveList.add(moveList.get(i));
		}
		}

		 */
		//After scoring is done, board removes current position
		if (gameBoard[r][c] != 1) {
			gameBoard[r][c] = 0;
		}
		moveList.remove(moveList.size()-1);
	}

	private double score(ArrayList<Integer[]> moves) {
		double score = 0;
		Integer[] start = moves.get(0);
		Integer[] end = moves.get(moves.size() - 1);
		score = distance(start, end);
		return score;
	}

	private double distance(Integer[] start, Integer[] end) {
		double distance = 0;

		return distance;
	}

	private boolean isLegalMove(int r, int c){
		if (gameBoard[r][c] == 1 || gameBoard[r][c] == 2) {
			//Visited before or has a piece on it
			return false;
		} else if (r < 9 || r > 25) {
			//Out of bounds
			return false;
		} else if (r < 13) {
			return (c >= 5 && c <= r - 4);
		} else if (r < 17) {
			return (c <= 13 && c >= (r + 1) - 13);
		} else if (r < 21) {
			return (c >= 5 && c <= (r - 4));
		} else if (r < 25) {
			return (c <= 13 && c >= (r-12));
		} else {
			return false;
		}
	}
}
