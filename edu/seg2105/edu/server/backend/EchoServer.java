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
    System.out.println("Message received: " + msg + " from " + client.getInfo("loginId"));
    String message = msg.toString();
    if (message.startsWith("#login")) {
      try {
        int loginId = Integer.parseInt(message.split(" ")[1]);
        if (client.getInfo("loginId") == null) {
          client.setInfo("loginId", loginId);
          System.out.println(loginId + " has logged on.");
        } else {
          client.sendToClient("Error: Already logged in.  Terminating Client.");
          try {
            client.close();
          } catch(IOException e) {
            serverUI.display
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
          serverUI.display
            ("Could not close the client successfully.  Terminating server.");
          System.exit(0);
        }
      }
    } else {
      this.sendToAllClients(client.getInfo("loginId") + " - " + msg);
    }
    
  }

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
      serverUI.display(message);
      this.sendToAllClients("SERVER MESSAGE> " + message);
    }
  }

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
      serverUI.display
        ("Could not close the connections successfully.  Terminating server.");
      System.exit(0);
    }
  }

  private void serverSetport(String message) {
    if (this.isListening()) {
      serverUI.display
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
      serverUI.display
        ("The #start command cannot be run when the server is already listening to connections.");
    } else {
      try {
        this.listen();
      } catch(IOException e) {
        serverUI.display
          ("Could not start to listen to connections successfully.  Terminating server.");
        System.exit(0);
      }
    }
  }

  private void serverGetport() {
    serverUI.display
      ("Current port number: " + this.getPort());
  }

  private void serverNoCommand() {
    serverUI.display
      ("This command does not exist.");
  }
    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }

  protected void clientConnected(ConnectionToClient client) {
    System.out.println
      ("Client " + client + " has connected to server.");
  }

  synchronized protected void clientDisconnected(ConnectionToClient client) {
    // super.clientDisconnected(client);
    System.out.println
      (client.getInfo("loginId") + " has disconnected from server.");
  }

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

    // Run the accept() loop in a separate thread:
    Thread consoleThread = new Thread(() -> console.accept());
    consoleThread.start();

    // Now we store it in the server instance so handleMessageFromServerUI can use it
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
