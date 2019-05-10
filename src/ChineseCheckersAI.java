/* [ChineseCheckersAI.java]
 * Client class that connects to a server to act as a bot playing chinese checkers
 * @author Bill Liu, Feng Guo, Victor Lin
 * April 23, 2019
 */

//imports

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import java.util.ArrayList;


/**
 * ChineseCheckersAI
 * Networking class assignment
 *
 * @author Feng Guo
 * @author Bill Liu
 * @author Victor Lin
 */
public class ChineseCheckersAI {
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
	private int[][] start;
	private int[][] end;

	//Moving + Scoring
	private double[] bestScore;
	private ArrayList<Integer[]> bestMoveList;
	//Each Phase represents the actions that a piece can legally take
	private final int PHASE_ONE = 0; //Can do whatever
	private final int PHASE_TWO = 1; //Cannot move 1 space
	private final int PHASE_THREE = 2; //Cannot move anymore
	private String moveSent; //To the server
	private ArrayList<Integer[]> moveList; //ArrayList of moves lol

	//Main function
	public static void main(String[] args) {
		ChineseCheckersAI chineseCheckersAI = new ChineseCheckersAI();
	}

	/**
	 * ChineseCheckersAI
	 * This constructor starts the AI.
	 */
	private ChineseCheckersAI() {
		setUp();
	}

	/**
	 * setUp
	 * Initiating function to start up the server
	 */
	private void setUp() {
		//Initiating display items
		mainFrame = new JFrame("Chinese Checkers AI");
		mainFrame.setVisible(true);
		mainFrame.setSize(400, 400);

		//Initiating our 2D array of pieces
		gamePieces = new int[10][2];//XY of our pieces (first index is piece number, second index is r or c)

		//Initialize the start and end points
		hardCodeEnd();

		//Setting up start panel
		startPanel = new JPanel();
		startPanel.setVisible(true);
		JLabel serverIPLabel = new JLabel("Enter the IP Address");
		JTextField serverIPTextField = new JTextField(20);
		JLabel portLabel = new JLabel("Enter the port");
		JTextField portTextField = new JTextField(10);
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(actionEvent -> {
			try {
				//Get inputs
				String ip = serverIPTextField.getText();
				int port = Integer.parseInt(portTextField.getText());
				//Try to connect to server
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
		JButton defaultButton = new JButton("DEFAULT");
		defaultButton.addActionListener(actionEvent -> {
			connectToServer("localhost", 6666);
		});

		//Add everything to the panel + frame
		startPanel.add(serverIPLabel);
		startPanel.add(serverIPTextField);
		startPanel.add(portLabel);
		startPanel.add(portTextField);
		startPanel.add(okayButton);
		startPanel.add(defaultButton);
		mainFrame.add(startPanel);
		repaintFrame();
	}

	/**
	 * connectToServer
	 * Create and set up a new socket to connect to the server
	 *
	 * @param ip String of the IP address of the server
	 * @param port Integer of the port of the server
	 */
	private void connectToServer(String ip, int port) {
		try {
			//Sets up the proper socket, input and output
			mySocket = new Socket(ip, port);
			InputStreamReader stream = new InputStreamReader(mySocket.getInputStream());
			input = new BufferedReader(stream);
			output = new PrintWriter(mySocket.getOutputStream());
			joinRoom();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * repaintFrame
	 * Refreshes the connecting (main) frame
	 */
	private void repaintFrame() {
		mainFrame.setVisible(true);
		mainFrame.repaint();
	}

	/**
	 * joinRoom
	 * User UI to enter a room name and player name
	 */
	private void joinRoom() {
		//Setting up joinPanel
		joinPanel = new JPanel();
		joinPanel.setVisible(true);

		//Setting up components of the panel
		JLabel roomLabel = new JLabel("Enter room name");
		JTextField roomTextField = new JTextField(20);
		JLabel nameLabel = new JLabel("Enter username");
		JTextField nameTextField = new JTextField(10);
		JButton okayButton = new JButton("Okay");
		okayButton.addActionListener(actionEvent -> {
			//Get inputs
			String room = roomTextField.getText();
			String username = nameTextField.getText();
			//Send room to server
			output.println("JOINROOM " + room);
			output.flush();
			String msg;
			do {
				msg = readMessagesFromServer();
			} while (msg == null);
			if (msg != null) {
				if ("OK".equalsIgnoreCase(msg)) {
					//Send name to server
					output.println("CHOOSENAME " + username);
					output.flush();
					do {
						msg = readMessagesFromServer();
					} while (msg == null);
					if (msg != null) {
						if ("OK".equalsIgnoreCase((msg))) {
							//Start the game loop
							Thread t = new Thread(new GameFrame(room, username));
							t.start();
						} else {
							System.out.println(msg);
						}
					} else {
						System.out.println("Message from server was null");
					}
				} else {
					System.out.println(msg); //Server error message
				}
			} else {
				System.out.println("Message from server was null");
			}

		});

		//Adding components of the panel
		joinPanel.add(roomLabel);
		joinPanel.add(roomTextField);
		joinPanel.add(nameLabel);
		joinPanel.add(nameTextField);
		joinPanel.add(okayButton);
		mainFrame.remove(startPanel);
		mainFrame.add(joinPanel);

		//Setting up the ArrayLists that we want to use
		bestMoveList = new ArrayList<>();
		moveList = new ArrayList<>();
		repaintFrame();
	}

	/**
	 * readMessageFromServer
	 * Reads input from the server and returns it
	 *
	 * @return message String containing the message sent from the server
	 */
	private String readMessagesFromServer() {
		try {
			if (input.ready()) { //check for an incoming messge
				String msg;
				msg = input.readLine().trim(); //read the message
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
//  try {  //after leaving the main loop we need to close all the sockets
//   input.close();
//   output.close();
//   mySocket.close();
//  } catch (Exception e) {
//   System.out.println("Failed to close socket");
//  }

	}


	/**
	 * play
	 * Runs when it is now our turn
	 */
	private void play() {
		bestMoveList = new ArrayList<>();
		//Setting up the limits for each category
		bestScore = new double[]{-1, 10000, -1, 10000};
		//Checks all possible moves for all of our pieces
		for (int i = 0; i < 10; i++) {
			moveList = new ArrayList<>();
			move(gamePieces[i][0], gamePieces[i][1], PHASE_ONE);
		}
		//Creating the string that we send to the server
		moveSent = "MOVE";
		StringBuilder s = new StringBuilder(moveSent);
		for (int i = 0; i < bestMoveList.size(); i++) {
			Integer[] move = bestMoveList.get(i);
			String temp = " (" + move[0] + "," + move[1] + ")";
			s.append(temp);
		}
		moveSent = s.toString();
	}

	/**
	 * resetBoard
	 * Refresh the board after each play
	 *
	 * @param msgSplit String array of the message from the server
	 */
	private void resetBoard(String[] msgSplit) {
		//Reset positions of pieces
		gameBoard = new int[30][30];
		gamePieces = new int[10][2];
		for (int i = 3; i < msgSplit.length; i++) {
			//Get coordinates of pieces from string
			String[] coords = msgSplit[i].split(",");
			coords[0] = coords[0].substring(1);
			coords[1] = coords[1].substring(0, coords[1].length() - 1);
			int row = Integer.parseInt(coords[0]);
			int column = Integer.parseInt(coords[1]);
			gameBoard[row][column] = 1;
			//Get our pieces
			if (i < 13) {
				gamePieces[i - 3][0] = row;
				gamePieces[i - 3][1] = column;
			}
		}
	}


	/**
	 * move
	 * Move function that also calls the score function after each move
	 *
	 * @param r Integer of the row of the piece to move
	 * @param c Integer of the column of the piece to move
	 * @param phase The phase of the turn, explained in variable init
	 */
	private void move(int r, int c, int phase) {
		if (gameBoard[r][c] != 1) {
			gameBoard[r][c] = 2;
		}
		Integer[] move = new Integer[2];
		move[0] = r;
		move[1] = c;
		moveList.add(move);
		if (phase == PHASE_ONE) {
			if (isLegalMove(r - 1, c)) {
				move(r - 1, c, PHASE_THREE);
			}
			if (isLegalMove(r - 1, c - 1)) {
				move(r - 1, c - 1, PHASE_THREE);
			}
			if (isLegalMove(r, c - 1)) {
				move(r, c - 1, PHASE_THREE);
			}
			if (isLegalMove(r + 1, c)) {
				move(r + 1, c, PHASE_THREE);
			}
			if (isLegalMove(r + 1, c + 1)) {
				move(r + 1, c + 1, PHASE_THREE);
			}
			if (isLegalMove(r, c + 1)) {
				move(r, c + 1, PHASE_THREE);
			}
		}
		if (phase == PHASE_TWO || phase == PHASE_ONE) {
			//If it is an illegal move 1 adjacent, then it is either out of bounds or has a piece there
			//Check the jump piece if it is a legal move because we will never jump over an out of bounds spot back in bounds
			if (!isLegalMove(r - 1, c)) {
				if (isLegalMove(r - 2, c)) {
					move(r - 2, c, PHASE_TWO);
				}
			}
			if (!isLegalMove(r - 1, c - 1)) {
				if (isLegalMove(r - 2, c - 2)) {
					move(r - 2, c - 2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r, c - 1)) {
				if (isLegalMove(r, c - 2)) {
					move(r, c - 2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r + 1, c)) {
				if (isLegalMove(r + 2, c)) {
					move(r + 2, c, PHASE_TWO);
				}
			}
			if (!isLegalMove(r + 1, c + 1)) {
				if (isLegalMove(r + 2, c + 2)) {
					move(r + 2, c + 2, PHASE_TWO);
				}
			}
			if (!isLegalMove(r, c + 1)) {
				if (isLegalMove(r, c + 2)) {
					move(r, c + 2, PHASE_TWO);
				}
			}

		}
		//If the move is legal to end in, we score the move
		if (isLegalEnd(r, c)) {
			double distance = (double) moveList.get(moveList.size() - 1)[0] - moveList.get(0)[0];
			double priority = (double) moveList.get(0)[0];
			double distanceToEnd = score(moveList);
			double distanceFromCenter = (double) distanceFromCenter((int) moveList.get(moveList.size() - 1)[0], (int) moveList.get(moveList.size() - 1)[1]);
			boolean isBestScore = false;
			//Check if this move is better
			if (distance > bestScore[0]) {
				isBestScore = true;
			} else if (distance == bestScore[0] && priority < bestScore[1]) {
				isBestScore = true;
			} else if (distance == bestScore[0] && priority == bestScore[1] && distanceToEnd > bestScore[2]) {
				isBestScore = true;
			} else if (distance == bestScore[0] && priority == bestScore[1] && distanceToEnd == bestScore[2] && distanceFromCenter < bestScore[3]) {
				isBestScore = true;
			}
			if (isBestScore) {
				//Make this move the new best move
				bestMoveList.clear();
				for (int i = 0; i < moveList.size(); i++) {
					bestMoveList.add(moveList.get(i));
				}
				bestScore[0] = distance;
				bestScore[1] = priority;
				bestScore[2] = distanceToEnd;
				bestScore[3] = distanceFromCenter;
			}
		}
		if (gameBoard[r][c] != 1) {
			gameBoard[r][c] = 0;
		}
		//Remove the last move
		moveList.remove(moveList.size() - 1);
	}


	//****************Methods for playing the game****************

	/**
	 * isLegalMove
	 * Returns true if it is not out of bounds or has a piece on the space already
	 *
	 * @param r
	 * @param c
	 * @return boolean
	 */
	private boolean isLegalMove(int r, int c) {
		if (r < 9 || r > 25 || c < 1 || c > 17) {
			//Out of bounds
			return false;
		} else if (gameBoard[r][c] == 1 || gameBoard[r][c] == 2) {
			//Visited before or has a piece on it
			return false;
		} else if (r < 13) {
			return (c >= 5 && c <= r - 4);
		} else if (r < 17) {
			return (c <= 13 && c >= (r + 1) - 13);
		} else if (r < 22) {
			return (c >= 5 && c <= (r - 4));
		} else if (r < 25) {
			return (c <= 13 && c >= (r - 12));
		} else if (r == 25) {
			return (c == 13);
		} else {
			return false;
		}
	}

	/**
	 * isLegalEnd
	 * returns false if it is the end goal of another player that is not directly across from you
	 *
	 * @param r
	 * @param c
	 * @return boolean
	 */
	private boolean isLegalEnd(int r, int c) {
		if (r < 13 || r > 21) {
			return true;
		} else if (r > 12 && r < 17) {
			if (c < 5) {
				return false;
			} else if ((r - c) < 4) {
				return false;
			}
			return true;
		} else if (r > 17) {
			if (c > 13) {
				return false;
			} else if ((r - c) > 12) {
				return false;
			}
		}
		return true;
	}

	/**
	 * distanceFromCenter
	 * Returns the distance from the center vertical column of the board (middle 3-4 spots)
	 *
	 * @param r Integer of the row of this piece.
	 * @param c Integer of the column of this piece.
	 * @return distance Integer of the distance of this piece from the center of the board.
	 */
	private int distanceFromCenter(int r, int c) {
		if (r < 13 || r > 21) {
			return 0;
		} else {
			if (r % 2 == 1) {
				int temp = c - ((r - 1) / 2);
				int lowerBound = ((r - 1) / 2);
				int upperBound = lowerBound + 2;
				if (temp >= 0 && temp <= 2) {
					return 0;
				} else if (c < lowerBound) {
					return lowerBound - c;
				} else {
					return c - upperBound;
				}
			} else {
				int temp = c - (r / 2);
				int lowerBound = (r / 2);
				int upperBound = lowerBound + 3;
				if (temp >= 0 && temp <= 3) {
					return 0;
				} else if (c < lowerBound) {
					return lowerBound - c;
				} else {
					return c - upperBound;
				}
			}
		}
	}

	/**
	 * distance
	 * Takes in the start and end position and returns the pythagorean distance
	 *
	 * @param start Integer array of size 2 containing the row and column of the start piece
	 * @param end Integer array of size 2 containing the row and column of the end piece
	 * @return distance Integer of the distance from the first piece fo the second piece.
	 */
	private double distance(int[] start, int[] end) {
		double distance = 0;
		distance = Math.sqrt(Math.pow((double) (start[0] - end[0]), 2) + Math.pow((double) (start[1] - end[1]), 2));
		return distance;
	}

	/**
	 * score
	 * Takes in an ArrayList of moves and returns the score of that sequence (displacement towards the end goal)
	 *
	 * @param moves ArrayList of Integer arrays containing the list of moves.
	 * @return score Double of the score.
	 */
	private double score(ArrayList<Integer[]> moves) {
		double distance = 0;
		int[] startMove = {(moves.get(0)[0]), (moves.get(0)[1])};
		int[] endMove = {(moves.get(moves.size() - 1)[0]), (moves.get(moves.size() - 1)[1])};

		double startDistance = Double.MAX_VALUE;
		double endDistance = Double.MAX_VALUE;

		for (int i = 0; i < end.length; i++) {
			//Check if piece checked is already occupied
			if (gameBoard[end[i][0]][end[i][1]] != 1) {
				//Find shortest distance from starting move to goal
				distance = distance(startMove, end[i]);
				if (distance < startDistance) {
					startDistance = distance;
				}
				//Find shortest distance from last move to goal
				distance = distance(endMove, end[i]);
				if (distance < endDistance) {
					endDistance = distance;
				}
			}
		}
		return (startDistance - endDistance);
	}

	/**
	 * hardCodeEnd
	 * Initializes the 10 spots that we end with
	 */
	private void hardCodeEnd() {
		end = new int[10][2];
		end[0][0] = 22;
		end[0][1] = 10;
		end[1][0] = 22;
		end[1][1] = 11;
		end[2][0] = 22;
		end[2][1] = 12;
		end[3][0] = 22;
		end[3][1] = 13;
		end[4][0] = 23;
		end[4][1] = 11;
		end[5][0] = 23;
		end[5][1] = 12;
		end[6][0] = 23;
		end[6][1] = 13;
		end[7][0] = 24;
		end[7][1] = 12;
		end[8][0] = 24;
		end[8][1] = 13;
		end[9][0] = 25;
		end[9][1] = 13;
	}

	/**
	 * GameFrame
	 * This class contains the game loop and the interface for the game display.
	 */
	private class GameFrame extends JFrame implements Runnable {
		JPanel infoPanel;
		BoardPanel boardPanel;
		JScrollPane logPane;
		JLabel stats;
		JTextArea log;
		int turn = 0;

		/**
		 * GameFrame
		 * This constructor creates a new game window.
		 * @param room Room name
		 * @param name Username
		 */
		GameFrame(String room, String name) {
			//Set titlebar to room and username
			this.setTitle(name + " | Room: " + room);

			//Window setup
			this.setSize(1080, 540);
			this.setLayout(new GridLayout(1, 2));

			infoPanel = new JPanel();
			infoPanel.setLayout(new GridLayout(2, 1));
			stats = new JLabel("Waiting for game start");
			log = new JTextArea();
			logPane = new JScrollPane(log);

			//Automatically scroll log to the bottom
			DefaultCaret caret = (DefaultCaret) log.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

			//Add everything to panels
			infoPanel.add(stats);
			infoPanel.add(logPane);
			this.add(infoPanel);

			boardPanel = new BoardPanel();
			this.add(boardPanel);
			this.setVisible(true);
		}

		/**
		 * run
		 * This runs the game loop.
		 */
		public void run() {
			while (running) {
				try {
					if (input.ready()) { //check for an incoming message
						String msg = readMessagesFromServer();
						try {
							if (msg.indexOf("BOARD") >= 0) {
								//Increase turn count
								turn++;
								//Add to log
								log.append("Turn " + turn + "\n");
								log.append(msg + "\n");
								//Split message from server
								String[] msgSplit = msg.split(" ");
								//Game logic
								resetBoard(msgSplit);
								play();
								//Write to log
								log.append(moveSent + "\n");
								//Send to server
								output.println(moveSent);
								output.flush();
								//Update frame
								updateStats();
								boardPanel.repaint();
							} else if (msg.indexOf("ERROR") >= 0) {
								log.append(msg + "\n");
							} else if (msg.indexOf("OK") >= 0) {
								log.append(msg + "\n");
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

		/**
		 * updateStats
		 * This method updates the statistics of the game.
		 */
		private void updateStats() {
			stats.setText("<html>Turn " + turn + "<br/><br/>" +
					"Distance: " + bestScore[0] + "<br/>" +
					"Priority: " + bestScore[1] + "<br/>" +
					"Distance to end: " + bestScore[2] + "<br/>" +
					"Distance from center: " + bestScore[3] + "</html>");
		}
	}

	/**
	 * BoardPanel
	 * This class represents a game board.
	 */
	private class BoardPanel extends JPanel {
		int xDisplacement = 15;
		int yDisplacement = 15;
		int size = 13;

		/**
		 * paintComponent
		 * Draws on the panel
		 */
		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			try {
				//Draw ALL pieces on the grid
				g.setColor(Color.GRAY);
				for (int r = 0; r < gameBoard.length; r++) {
					for (int c = 0; c < gameBoard[r].length; c++) {
						if (gameBoard[r][c] == 1) {
							g.fillOval((c * xDisplacement) + horizontalShift(r), r * yDisplacement, size, size);
						}
					}
				}
				//Draw our pieces on the grid
				g.setColor(Color.RED);
				for (int i = 0; i < gamePieces.length; i++) {
					int r = gamePieces[i][0];
					int c = gamePieces[i][1];
					g.fillOval((c * xDisplacement) + horizontalShift(r), r * yDisplacement, size, size);
				}
				//Draw our move
				g.setColor(Color.CYAN);
				for (int i = 0; i < bestMoveList.size(); i++) {
					Integer[] move = bestMoveList.get(i);
					int r = move[0];
					int c = move[1];
					//Highlight final move with blue
					if (i == bestMoveList.size() - 1) {
						g.setColor(Color.BLUE);
					}
					//Since the first move is highlighted cyan, switch to green
					else if (i == 1) {
						g.setColor(Color.GREEN);
					}
					g.fillOval((c * xDisplacement) + horizontalShift(r), r * yDisplacement, size, size);
				}
			} catch (NullPointerException e) {
				//board not initialized yet
			}
			//Draw the grid outlines
			g.setColor(Color.BLACK);
			for (int r = 9; r <= 25; r++) {
				for (int c = 1; c < 18; c++) {
					//If the space exists, draw
					if (validSpace(r, c)) {
						g.drawOval((c * xDisplacement) + horizontalShift(r), r * yDisplacement, size, size);
					}
				}
			}
		}

		/**
		 * horizontalShift
		 * Gets the horizontal shift for each row of the board
		 * @param r Integer of the row of the piece.
		 * @return displacement Integer of the horizontal displacement for that row.
		 */
		private int horizontalShift(int r) {
			return (int)(((13.0 - r) / 2) * xDisplacement);
		}

		/**
		 * validSpace
		 * Determines if the space specified exists on the board
		 * @param r Integer of the row of the piece
		 * @param c Integer of the column of the piece
		 * @return valid True if valid, false if invalid
		 */
		private boolean validSpace(int r, int c) {
			if (r < 9 || r > 25 || c < 1 || c > 17) {
				return false;
			} else if (r < 13) {
				return (c >= 5 && c <= r - 4);
			} else if (r < 17) {
				return (c <= 13 && c >= (r + 1) - 13);
			} else if (r < 22) {
				return (c >= 5 && c <= (r - 4));
			} else if (r < 25) {
				return (c <= 13 && c >= (r - 12));
			} else if (r == 25) {
				return (c == 13);
			} else {
				return false;
			}
		}
	}
}
