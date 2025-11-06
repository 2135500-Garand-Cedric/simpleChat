package edu.seg2105.edu.server.ui;

import java.io.*;
import java.util.Scanner;

import edu.seg2105.edu.server.backend.EchoServer;
import edu.seg2105.client.common.*;

/**
 * Makes it possible to have a console on the server
 * Similar principle and code than the ClientConsole class
 *
 * @author Cedric Garand
 */
public class ServerConsole implements ChatIF 
{
  //Instance variables **********************************************
  
  /**
   * The instance of the server that created this ConsoleChat.
   */
  EchoServer server;
  
  /**
   * Scanner to read from the console
   */
  Scanner fromConsole; 

  
  //Constructors ****************************************************

  /**
   * Constructs an instance of the ServerConsole UI.
   *
   * @param server The server that created the console
   */
  public ServerConsole(EchoServer server) 
  {
    this.server = server;
    // Create scanner object to read from console
    this.fromConsole = new Scanner(System.in); 
  }

  
  //Instance methods ************************************************
  
  /**
   * This method waits for input from the console.  Once it is 
   * received, it sends it to the client's message handler.
   */
  public void accept() {
    try {
      String message;

      while (true) 
      {
        message = this.fromConsole.nextLine();
        this.server.handleMessageFromServerUI(message);
      }
    } catch (Exception ex) {
      System.out.println
        ("Unexpected error while reading from console!");
    }
  }

  /**
   * This method overrides the method in the ChatIF interface.  It
   * displays a message onto the screen.
   *
   * @param message The string to be displayed.
   */
  public void display(String message) 
  {
    System.out.println("SERVER MESSAGE> " + message);
  }
}
//End of ConsoleChat class
