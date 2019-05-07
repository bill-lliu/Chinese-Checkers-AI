/* [ChineseCheckersAI.java]
 * Client class that connects to a server to act as a bot playing chinese checkers
 * @author Bill Liu, Feng Guo, Victor Lin
 * April 23, 2019
 */

//imports
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.Socket;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

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
    private double[] bestScore; 
    private ArrayList<Integer[]> bestMoveList;
    //Each Phase represents the actions that a piece can legally take
    private final int PHASE_ONE = 0; //Can do whatever
    private final int PHASE_TWO = 1; //Cannot move 1 space
    private final int PHASE_THREE = 2; //Cannot move anymore
    private String moveSent; //To the server
    private ArrayList<Integer[]> moveList; //ArrayList of moves lol

    //Main function
    public static void main(String [] args) {
        ChineseCheckersAI chineseCheckersAI = new ChineseCheckersAI();
    }

    //Class constructor
    ChineseCheckersAI(){
        setUp();
    }

    /** setUp
     * Initiating function to start up the server
     */
    private void setUp(){
        //Initiating display items
        mainFrame = new JFrame("Chinese Checkers AI");
        mainFrame.setVisible(true);
        mainFrame.setSize(400,400);

        //Initiating our 2D array of pieces
        gamePieces = new int[10][2];//XY of our pieces (first index is piece number, second index is r or c)
        
        //Initialize the start and end points
        hardCodeEnd();

        //Setting up startpanel
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
    
    /** connectToServer
     * Create and set up a new socket to connect to the server
     * @param ip
     * @param port
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
                            Thread t = new Thread(new GameFrame(room, username));
                            t.start();
                            //runGameLoop();
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

    
    /** readMessageFromServer
     * Reads input from the server and returns it
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
        //Setting up the limits for each category
        bestScore = new double[]{-1, 10000, -1, 10000};
        //Checks all possible moves for all of our pieces
        for (int i=0; i<10; i++) {
            moveList = new ArrayList<>();
            move(gamePieces[i][0], gamePieces[i][1], PHASE_ONE);
        }
        //Creating the string that we send to the server
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
     * Main game loop to continually wait for a message and responds
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
     * Move function that also calls the score function after each move
     * @param int r
     * @param int c
     * @param int phase The phase of the turn, explained in variable init
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
            //Check the jump piece if it is a legal move because we will never jump over an out of bounds spot back in bounds
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
        //If the move is legal to end in, we score the move
        if (isLegalEnd(r, c)) {
            double distance = (double)moveList.get(moveList.size()-1)[0] - moveList.get(0)[0];
            double priority = (double)moveList.get(0)[0];
            double distanceToEnd = score(moveList);
            double distanceFromCenter = (double)distanceFromCenter((int)moveList.get(moveList.size()-1)[0], (int)moveList.get(moveList.size()-1)[1]);      
            boolean isBestScore = false;
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
                bestMoveList.clear();
                for (int i=0; i<moveList.size(); i++) {
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
        moveList.remove(moveList.size()-1);
    }



    //****************Methods for playing the game****************
    /** isLegalMove
     * Returns true if it is not out of bounds or has a piece on the space already
     * @param int r
     * @param int c
     * @return boolean
     */
    private boolean isLegalMove(int r, int c){
        if (r < 9 || r > 25 || c<1 || c>17) {
            //Out of bounds
            return false;
        } else if (gameBoard[r][c] == 1 || gameBoard[r][c] == 2) {
            //Visited before or has a piece on it
            return false;
        } else if (r < 13) {
            return (c >= 5 && c <= r - 4);
        } else if (r < 17) {
            return (c <= 13 && c >= (r + 1) - 13);
        } else if (r < 21) {
            return (c >= 5 && c <= (r - 4));
        } else if (r < 25) {
            return (c <= 13 && c >= (r-12));
        } else if (r ==25) {
            return (c == 13);
        } else {
            return false;
        }
    }
    
    /** isLegalEnd
     * returns false if it is the end goal of another player that is not directly across from you
     * @param int r
     * @param int c
     * @return boolean
     */
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
    
    /** distanceFromCenter
     * Returns the distance from the center vertical column of the board (middle 3-4 spots)
     * @param int r
     * @param int c
     * @return int
     */
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
    
    /** distance
     * Takes in the start and end position and returns the pythagorean distance
     * @param int[] start
     * @param int[] end
     * @return double
     */
    private double distance(int[] start, int[] end) {
        double distance = 0;
        distance = Math.sqrt(Math.pow((double) (start[0] - end[0]), 2) + Math.pow((double) (start[1] - end[1]), 2));
        return distance;
    }
    
    /** score
     * Takes in an ArrayList of moves and returns the score of that sequence (displacement towards the end goal)
     * @param ArrayList moves
     * @return double 
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
    
    /** hardCodeEnd
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

    class GameFrame extends JFrame implements Runnable {
        JPanel infoPanel;
        BoardPanel boardPanel;
        JScrollPane logPane;
        JLabel status;
        JTextArea log;
        int turn = 0;

        GameFrame(String room, String name) {
            this.setTitle(name + " | Room: " + room);
            this.setSize(1080,540);
            this.setLayout(new GridLayout(1,2));

            infoPanel = new JPanel();
            infoPanel.setLayout(new GridLayout(2,1));
            status = new JLabel("Status: Waiting for turn");
            log = new JTextArea();
            logPane = new JScrollPane(log);

            //Automatically scroll log to the bottom
            DefaultCaret caret = (DefaultCaret)log.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            infoPanel.add(status);
            infoPanel.add(logPane);
            this.add(infoPanel);

            boardPanel = new BoardPanel();
            this.add(boardPanel);
            this.setVisible(true);
        }

        public void run() {
            while (running) {
                try {
                    if (input.ready()) { //check for an incoming message
                        this.status.setText("Status: Moving");
                        String msg = readMessagesFromServer();
                        try {
                            if (msg.indexOf("BOARD") >= 0) {
                                turn++;
                                log.append("Turn " + turn + "\n");
                                String[] msgSplit = msg.split(" ");
                                resetBoard(msgSplit);
                                play();
                                log.append(moveSent + "\n");
                                output.println(moveSent);
                                output.flush();

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
                        this.status.setText("waiting for turn");
                    }
                } catch (IOException e) {
                    //Nothing happens hopefully
                }
            }
        }

    }

    class BoardPanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            //Draw the grid
            for (int r = 9; r <= 25; r++) {
                for (int c = 1; c < 17; c++) {
                    if (validSpace(r,c)) {
                        g.setColor(Color.BLACK);
                        g.drawOval(c * 5, r * 5, 3, 3);
                    }
                }
            }
        }

        private boolean validSpace(int r, int c) {
            if (r < 9 || r > 25 || c<1 || c>17) {
                return false;
            } else if (r < 13) {
                return (c >= 5 && c <= r - 4);
            } else if (r < 17) {
                return (c <= 13 && c >= (r + 1) - 13);
            } else if (r < 21) {
                return (c >= 5 && c <= (r - 4));
            } else if (r < 25) {
                return (c <= 13 && c >= (r-12));
            } else if (r == 25) {
                return (c == 13);
            } else {
                return false;
            }
        }
    }
}
