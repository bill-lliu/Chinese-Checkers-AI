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
  JFrame mainFrame;
  JPanel startPanel;
  
  //Server 
  private Socket mySocket; //socket for connection
  private BufferedReader input; //reader for network stream
  private PrintWriter output;  //printwriter for network output
  
  
  private boolean running = true; //thread status via boolean
  
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
  
  private void setUp(){
    mainFrame = new JFrame("Chinese Checkers AI");
    mainFrame.setVisible(true);
    mainFrame.setSize(400,400);
    
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
}
