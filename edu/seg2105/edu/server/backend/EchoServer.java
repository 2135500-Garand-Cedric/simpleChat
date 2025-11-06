package edu.seg2105.edu.server.backend;
// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 


import ocsf.server.*;
import edu.seg2105.edu.server.ui.ServerConsole;
import edu.seg2105.client.common.*;
import java.io.*;

/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @author Fran&ccedil;ois B&eacute;langer
 * @author Paul Holden
 */
public class EchoServer extends AbstractServer 
{
  //Class variables *************************************************
  
  /**
   * The default port to listen on.
   */
  final public static int DEFAULT_PORT = 5555;

  //Instance variables **********************************************

  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF serverUI; 
  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the echo server.
   *
   * @param port The port number to connect on.
   */
  public EchoServer(int port) 
  {
    super(port);
  }

  
  //Instance methods ************************************************
  
  /**
   * This method handles any messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public void handleMessageFromClient
    (Object msg, ConnectionToClient client)
  {
    String message = msg.toString();
    System.out.println("Message received: " + message + " from " + client.getInfo("loginId"));
    
    if (message.startsWith("#login")) {
      this.handleFirstLoginFromClient(message, client);
    } else {
      this.sendToAllClients(client.getInfo("loginId") + " - " + msg);
    }
    
  }

  /**
   * Helper function to handle the first message sent from a client and set its loginId
   * 
   * @param message The message received from the client (starting with #login)
   * @param client The connection from which the message originated.
   */
  private void handleFirstLoginFromClient(String message, ConnectionToClient client) {
    try {
      String loginId = message.split(" ")[1];
      if (client.getInfo("loginId") == null) {
        client.setInfo("loginId", loginId);
        System.out.println(loginId + " has logged on.");
      } else {
        client.sendToClient("Error: Already logged in.  Terminating Client.");
        try {
          client.close();
        } catch(IOException e) {
          this.serverUI.display
            ("Could not close the client successfully.  Terminating server.");
          System.exit(0);
        }
      }
    } catch (Exception e) {
      System.out.println
        ("Error with the loginId.  Terminating Client.");
      try {
        client.close();
      } catch(IOException ex) {
        this.serverUI.display
          ("Could not close the client successfully.  Terminating server.");
        System.exit(0);
      }
    }
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromServerUI(String message) {
    if (message.startsWith("#")) {
      if (message.equals("#quit")) {
        this.serverQuit();
      } else if (message.equals("#stop")) {
        this.serverStop();
      } else if (message.equals("#close")) {
        this.serverClose();
      } else if (message.startsWith("#setport") && message.split(" ").length == 2) {
        this.serverSetport(message);
      } else if (message.equals("#start")) {
        this.serverStart();
      } else if (message.equals("#getport")) {
        this.serverGetport();
      } else {
        this.serverNoCommand();
      }
    } else {
      this.serverUI.display(message);
      this.sendToAllClients("SERVER MESSAGE> " + message);
    }
  }

  /**
   * Helper functions for all the different commands for the server
   */
  private void serverQuit() {
    try {
      this.close();
    } catch(IOException e) {}
    System.out.println("Server is quitting.");
    System.exit(0);
  }

  private void serverStop() {
    this.stopListening();
  }

  private void serverClose() {
    try {
      this.close();
    } catch(IOException e) {
      this.serverUI.display
        ("Could not close the connections successfully.  Terminating server.");
      System.exit(0);
    }
  }

  private void serverSetport(String message) {
    if (this.isListening()) {
      this.serverUI.display
        ("The #setport command cannot be run when the server is listening to connections.");
    } else {
      try {
        int newPort = Integer.parseInt(message.split(" ")[1]);
        this.setPort(newPort);
      } catch (NumberFormatException e) {
        serverUI.display
          ("Error: <host> is not a valid number");
      }
    }
  }

  private void serverStart() {
    if (this.isListening()) {
      this.serverUI.display
        ("The #start command cannot be run when the server is already listening to connections.");
    } else {
      try {
        this.listen();
      } catch(IOException e) {
        this.serverUI.display
          ("Could not start to listen to connections successfully.  Terminating server.");
        System.exit(0);
      }
    }
  }

  private void serverGetport() {
    this.serverUI.display
      ("Current port number: " + this.getPort());
  }

  private void serverNoCommand() {
    this.serverUI.display
      ("This command does not exist.");
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  @Override
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + this.getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  @Override
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }

  @Override
  protected void clientConnected(ConnectionToClient client) {
    System.out.println
      ("A new client has connected to the server.");
  }

  @Override
  synchronized protected void clientDisconnected(ConnectionToClient client) {
    System.out.println
      (client.getInfo("loginId") + " has disconnected from server.");
  }

  @Override
  synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
    System.out.println
      (client.getInfo("loginId") + " has disconnected from server.");
  }
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
  public static void main(String[] args) 
  {
    int port = 0; //Port to listen on

    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    EchoServer sv = new EchoServer(port);

    ServerConsole console = new ServerConsole(sv);

    // Creating a new thread for the console so that 
    // I can have 1 thread that waits for the console inputs
    // And 1 thread that waits for the connections
    Thread consoleThread = new Thread(
      new Runnable() {
      @Override
      public void run() {
        console.accept();
      }  
    });
    consoleThread.start();

    sv.serverUI = console;
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }
}
//End of EchoServer class
