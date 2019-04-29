/* [ChatServer.java]
 * You will need to modify this so that received messages are broadcast to all clients
 * @author Mangat
 * @ version 1.0a
 */

//imports for network communication
import java.io.*;
import java.net.*;

class ChatServer {
  
  ServerSocket serverSock;// server socket for connection
  static Boolean running = true;  // controls if the server is accepting clients
  
   /** Main
    * @param args parameters from command line
    */
  public static void main(String[] args) { 
    new ChatServer().go(); //start the server
  }
  
  /** Go
    * Starts the server
    */
  public void go() { 
        System.out.println("Waiting for a client connection..");
    
     Socket client = null;//hold the client connection
        
    try {
      serverSock = new ServerSocket(5000);  //assigns an port to the server
      //serverSock.setSoTimeout(15000);  //15 second timeout
         while(running) {  //this loops to accept multiple clients
           client = serverSock.accept();  //wait for connection
           System.out.println("Client connected");
           //Note: you might want to keep references to all clients if you plan to broadcast messages
           //Also: Queues are good tools to buffer incoming/outgoing messages
           Thread t = new Thread(new ConnectionHandler(client)); //create a thread for the new client and pass in the socket
           t.start(); //start the new thread
         }
    }catch(Exception e) { 
     // System.out.println("Error accepting connection");
      //close all and quit
      try {
        client.close();
      }catch (Exception e1) { 
        System.out.println("Failed to close socket");
      }
      System.exit(-1);
    }
  }
  
  //***** Inner class - thread for client connection
  class ConnectionHandler implements Runnable { 
    private PrintWriter output; //assign printwriter to network stream
    private BufferedReader input; //Stream for network input
    private Socket client;  //keeps track of the client socket
    private boolean running;
    /* ConnectionHandler
     * Constructor
     * @param the socket belonging to this client connection
     */    
    ConnectionHandler(Socket s) { 
      this.client = s;  //constructor assigns client to this    
      try {  //assign all connections to client
        this.output = new PrintWriter(client.getOutputStream());
        InputStreamReader stream = new InputStreamReader(client.getInputStream());
        this.input = new BufferedReader(stream);
      }catch(IOException e) {
        e.printStackTrace();        
      }            
      running=true;
    } //end of constructor
  
    
    /* run
     * executed on start of thread
     */
    public void run() {  

      //Get a message from the client
      String msg="";
      
           //Send a message to the client

      
      //Get a message from the client
      while(running) {  // loop unit a message is received        
        try {
          if (input.ready()) { //check for an incoming messge
            msg = input.readLine();  //get a message from the client
            System.out.println(msg);
            //output.println(msg); //echo the message back to the client ** This needs changing for multiple clients
            output.println("OK");
            output.flush();
          }
          }catch (IOException e) { 
            System.out.println("Failed to receive msg from the client");
            e.printStackTrace();
          }
        }    
      
      //Send a message to the client
      output.println("We got your message! Goodbye.");
      output.flush(); 
      
      //close the socket
      try {
        input.close();
        output.close();
        client.close();
      }catch (Exception e) { 
        System.out.println("Failed to close socket");
      }
    } // end of run()
  } //end of inner class   
} //end of Class