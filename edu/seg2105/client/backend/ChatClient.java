// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package edu.seg2105.client.backend;

import ocsf.client.*;

import java.io.*;

import edu.seg2105.client.common.*;

/**
 * This class overrides some of the methods defined in the abstract
 * superclass in order to give more functionality to the client.
 *
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;
 * @author Fran&ccedil;ois B&eacute;langer
 */
public class ChatClient extends AbstractClient
{
  //Instance variables **********************************************
  
  /**
   * The interface type variable.  It allows the implementation of 
   * the display method in the client.
   */
  ChatIF clientUI; 

  /**
   * Login Id of the client trying to connect to the server.
   * To give a way for the server to identify connections and messages sent.
   */
  String loginId;

  
  //Constructors ****************************************************
  
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   * @param loginId The loginId of the client.
   */
  
  public ChatClient(String host, int port, ChatIF clientUI, String loginId) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.loginId = loginId;
    this.clientUI = clientUI;
    try {
      this.openConnection();
    } catch(IOException e) {
      System.out.println
        ("ERROR - Can't setup connection! Terminating client.");
      System.exit(0);
    }
  }

  
  //Instance methods ************************************************
    
  /**
   * This method handles all data that comes in from the server.
   *
   * @param msg The message from the server.
   */
  public void handleMessageFromServer(Object msg) 
  {
    String message = msg.toString();
    if (message.startsWith("SERVER MESSAGE>")) {
      System.out.println(message);
      return;
    }
    this.clientUI.display(message);
  }

  /**
   * This method handles all data coming from the UI            
   *
   * @param message The message from the UI.    
   */
  public void handleMessageFromClientUI(String message)
  {
    if (message.startsWith("#")) {
      if (message.equals("#quit")) {
        this.clientQuit();
      } else if (message.equals("#logoff")) {
        this.clientLogoff();
      } else if (message.startsWith("#sethost") && message.split(" ").length == 2) {
        this.clientSethost(message);
      } else if (message.startsWith("#setport") && message.split(" ").length == 2) {
        this.clientSetport(message);
      } else if (message.equals("#login")) {
        this.clientLogin();
      } else if (message.equals("#gethost")) {
        this.clientGethost();
      } else if (message.equals("#getport")) {
        this.clientGetport();
      } else {
        this.clientNoCommand();
      }
    } else {
      try {
        this.sendToServer(message);
      } catch(IOException e) {
        this.clientUI.display
          ("Could not send message to server.  Terminating client.");
        this.quit();
      }
    }
  }

  /**
   * Helper functions for all the different commands for the client
   */
  private void clientQuit() {
    this.quit();
  }

  private void clientLogoff() {
    try {
      this.closeConnection();
    } catch(IOException e) {
      this.clientUI.display
        ("Error disconnecting from server.  Terminating client.");
      System.exit(0);
    }
  }

  private void clientSethost(String message) {
    if (this.isConnected()) {
      this.clientUI.display
        ("The #sethost command cannot be run when the client is connected to the server.");
    } else {
      String newHost = message.split(" ")[1];
      this.setHost(newHost);
    }
  }

  private void clientSetport(String message) {
    if (this.isConnected()) {
      this.clientUI.display
        ("The #setport command cannot be run when the client is connected to the server.");
    } else {
      try {
        int newPort = Integer.parseInt(message.split(" ")[1]);
        this.setPort(newPort);
      } catch (NumberFormatException e) {
        this.clientUI.display
          ("Error: <host> is not a valid number");
      }
    }
  }

  private void clientLogin() {
    if (this.isConnected()) {
      this.clientUI.display
        ("The #login command cannot be run when the client is connected to the server.");
    } else {
      try {
        this.openConnection();
      } catch(IOException e) {
        this.clientUI.display
          ("Could not connect to the server.  Terminating client.");
        System.exit(0);
      }
    }
  }

  private void clientGethost() {
    this.clientUI.display
      ("Current host name: " + this.getHost());
  }

  private void clientGetport() {
    this.clientUI.display
      ("Current port number: " + this.getPort());
  }

  private void clientNoCommand() {
    this.clientUI.display
      ("This command does not exist.");
  }
  
  /**
   * This method terminates the client.
   */
  public void quit()
  {
    try {
      this.closeConnection();
    } catch(IOException e) {}
    System.exit(0);
  }

  /**
   * Callback to show a message when the connection is closed.
   */
  @Override
  protected void connectionClosed() {
    this.clientUI.display
      ("Connection closed.");
  }

  /**
   * Callback to show a message and quit when the server shuts down.
   */
  @Override
  protected void connectionException(Exception exception) {
    this.clientUI.display
      ("The server has shut down.  Terminating client.");
    System.exit(0);
  }

  /**
   * Send a the loginId to the server when the connection is first established
   */
  @Override
  protected void connectionEstablished() {
    try {
      this.sendToServer("#login " + this.loginId);
      System.out.println(this.loginId + " has logged on.");
    }
    catch(IOException e)
    {
      this.clientUI.display
        ("Could not send message to server.  Terminating client.");
      this.quit();
    }
  }
}
//End of ChatClient class
