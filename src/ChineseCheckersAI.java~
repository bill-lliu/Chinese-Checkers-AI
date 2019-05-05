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
import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.util.ArrayList;


/**
 * ChineseCheckersAI
 * Networking class assignment
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
    private int[] bestScore; //0 is displacement, 1 is priority
    private ArrayList<Integer[]> bestMoveList;
    private final int PHASE_ONE = 0;
    private final int PHASE_TWO = 1;
    private final int PHASE_THREE = 2;
    private String moveSent;
    private ArrayList<Integer[]> moveList;

    //Main function
    public static void main(String [] args) {
        ChineseCheckersAI chineseCheckersAI = new ChineseCheckersAI();
    }

    //Class constructor
    ChineseCheckersAI(){
        setUp();
    }

    /** connectToServer
     * Create and set up a new socket to connect to the server
     * @param ip
     * @param port
     */
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

    /** setUp
     * Initiating function to start up the server
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

        JButton defaultButton = new JButton("DEFAULT");
        defaultButton.addActionListener(actionEvent -> {
            connectToServer("localhost", 6666);
        });
        
        startPanel.add(serverIPLabel);
        startPanel.add(serverIPTextField);
        startPanel.add(portLabel);
        startPanel.add(portTextField);
        startPanel.add(okayButton);
        startPanel.add(defaultButton);
        mainFrame.add(startPanel);
        repaintFrame();
        hardCodeStart();
        hardCodeEnd();
    }

    /** repaintFrame
     * Refreshes the display
     */
    private void repaintFrame() {
        mainFrame.setVisible(true);
        mainFrame.repaint();
    }

    /** joinRoom
     * User UI to enter a room name and player name
     */
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
            String msg;
            do {
                msg = readMessagesFromServer();
            } while (msg == null);
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
                            System.out.println("Message from server was null");
                        }
                } else {
                    System.out.println(msg); //Server error message
                }
            } else {
                System.out.println("Message from server was null");
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

    
    /** readMessageFromServer
     * reads input
     * @return String
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

    
    
    /** play
      * Runs when it is now our turn
      */
    private void play() {
        bestMoveList = new ArrayList<>();
        bestScore = new int[]{-1, 10000, 10000};
        for (int i=0; i<10; i++) {
            moveList = new ArrayList<>();
            move(gamePieces[i][0], gamePieces[i][1], PHASE_ONE);
        }
        moveSent = "MOVE";
        StringBuilder s = new StringBuilder(moveSent);
        for (int i=0; i<bestMoveList.size(); i++) {
            Integer[] move = bestMoveList.get(i);
            String temp = " (" + move[0] + "," + move[1] + ")";
            s.append(temp);
        }
        moveSent = s.toString();
    }

    
    /** runGameLoop
     * main game loop to continually wait for a message and responds
     */
    private void runGameLoop(){
        //This is where we do the looping waiting for stuff
        while (running) {
            try {
                if (input.ready()) { //check for an incoming message
                    String msg = readMessagesFromServer();
                    try {
                        if (msg.indexOf("BOARD") >= 0) {
                            System.out.println(msg);
                            String[] msgSplit = msg.split(" ");
                            resetBoard(msgSplit);
                            play();
                            System.out.println(moveSent);
                            output.println(moveSent);
                            output.flush();
                        } else if (msg.indexOf("ERROR") >= 0) {
                            System.out.println(msg);
                        } else if (msg.indexOf("OK") >= 0) {
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

    
    /** resetBoard
     * Refresh the board after each play
     * @param msgSplit String array of the message from the server
     */
    private void resetBoard(String[] msgSplit) {
        gameBoard = new int[30][30];
        gamePieces = new int[10][2];
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

    
    /** move
     * move function that also calls the score function after each move
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
            if (isLegalMove(r-1, c)) {
                move(r-1, c, PHASE_THREE);
            }
            if (isLegalMove(r-1, c-1)) {
                move(r-1,c-1, PHASE_THREE);
            }
            if (isLegalMove(r, c-1)) {
                move(r, c-1, PHASE_THREE);
            }
            if (isLegalMove(r+1, c)) {
                move(r+1, c, PHASE_THREE);
            }
            if (isLegalMove(r+1, c+1)) {
                move(r+1, c+1, PHASE_THREE);
            }
            if (isLegalMove(r, c+1)) {
                move(r, c+1, PHASE_THREE);
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
        if (isLegalEnd(r, c)) {
            int distance = moveList.get(moveList.size()-1)[0] - moveList.get(0)[0];
            int priority = moveList.get(0)[0];
            int distanceFromCenter = distanceFromCenter((int)moveList.get(moveList.size()-1)[0], (int)moveList.get(moveList.size()-1)[1]);
            boolean isBestScore = false;
            if (distance > bestScore[0]) {
                isBestScore = true;
            } else if (distance == bestScore[0] && priority < bestScore[1]) {
                isBestScore = true;
            } else if (distance == bestScore[0] && priority == bestScore[1] && distanceFromCenter < bestScore[2]) {
                isBestScore = true;
            }
            if (isBestScore) {
                bestMoveList.clear();
                for (int i=0; i<moveList.size(); i++) {
                    bestMoveList.add(moveList.get(i));
                }
                bestScore[0] = distance;
                bestScore[1] = priority;
                bestScore[2] = distanceFromCenter;
            }
        }
        if (gameBoard[r][c] != 1) {
            gameBoard[r][c] = 0;
        }
        moveList.remove(moveList.size()-1);
    }



    //****************Methods for playing the game****************

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
    
    private boolean isLegalEnd(int r, int c) {
        if (r<13 || r>21) {
            return true;
        } else if (r>12 && r<17) {
            if (c<5) {
                return false;
            } else if ((r-c)<4) {
                return false;
            }
            return true;
        } else if (r>17) {
            if (c>13) {
                return false;
            } else if ((r-c)>12) {
                return false;
            }
        }
        return true;
    }
    
    private int distanceFromCenter(int r, int c)  {
        if (r<13 || r>21) {
            return 0;
        } else {
            if (r%2 == 1) {
                int temp = c - ((r-1)/2);
                int lowerBound = ((r-1)/2);
                int upperBound = lowerBound + 2;
                if (temp >= 0 && temp <= 2) {
                    return 0;
                } else if (c<lowerBound) {
                    return lowerBound-c;
                } else {
                    return c-upperBound;
                }    
            } else {
                int temp = c - (r/2);
                int lowerBound = (r/2);
                int upperBound = lowerBound + 3;
                if (temp >= 0 && temp <= 3) {
                    return 0;
                } else if (c<lowerBound) {
                    return lowerBound-c;
                } else {
                    return c-upperBound;
                } 
            }
        }
    }
    
    private void hardCodeStart() {
        start = new int[10][2];
        start[0][0] = 9;
        start[0][1] = 5;
        start[1][0] = 10;
        start[1][1] = 5;
        start[2][0] = 10;
        start[2][1] = 6;
        start[3][0] = 11;
        start[3][1] = 5;
        start[4][0] = 11;
        start[4][1] = 6;
        start[5][0] = 11;
        start[5][1] = 7;
        start[6][0] = 12;
        start[6][1] = 5;
        start[7][0] = 12;
        start[7][1] = 6;
        start[8][0] = 12;
        start[8][1] = 7;
        start[9][0] = 12;
        start[9][1] = 8;
    }

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
}
