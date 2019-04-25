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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

//main class for the server
public class ChineseCheckersAI {
	
	//stuff we dont need
	private JButton sendButton, clearButton;
	private JTextField typeField;
	private JTextArea msgArea;  
	private JPanel southPanel;
	private Socket mySocket; //socket for connection
	private BufferedReader input; //reader for network stream
	private PrintWriter output;  //printwriter for network output
	//private boolean running = true; //thread status via boolean
	
	public static void main(String [] args) {
		//start the boie
	}
	
	ChineseCheckersAI() {
		
	}
	
}
