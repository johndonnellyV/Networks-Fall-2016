import java.net.*;
import java.io.*;
import java.util.*;

//jed126 (John D)

/**
 * Client class for connecting to the server
 * and chatting
 */
public class Client
{

    //i/o config
    private ObjectInputStream in;
    private ObjectOutputStream out;
    public Socket socket;


    //was for private chat
    private Client buddy;

    //the clients respective gui
    private ClientGUI cGUI;
    //server and username
    private String server, username;
    //portnumber
    private int portNumb;

    //was for private chat
    public void setBuddy(Client buddy) {
        this.buddy = buddy;
    }

    /**
     * constructor
     * @param server servername
     * @param portNumb port number
     * @param username string username
     * @param cGUI corresponding gui
     */
    Client(String server, int portNumb, String username, ClientGUI cGUI)
    {
        this.server = server;
        this.portNumb = portNumb;
        this.username = username;

        this.cGUI = cGUI;
    }

    /**\
     * This method starts the client initiating it's i/o etc...
     * @return returns true if nothing bad happened
     */
    public boolean start()
    {

        try
        {
            socket = new Socket(server, portNumb);
        }

        catch(Exception ec)
        {
            printMessage("Error connecting to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort();
        printMessage(msg);


        try
        {
            in  = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            printMessage("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        new ServerListener().start();

        try
        {
            out.writeObject(username);
        }
        catch (IOException eIO) {
            printMessage("Exception doing login : " + eIO);
            disconnect();
            return false;
        }


        return true;
    }

    /**
     * prints a message to the gui
     * @param message message to be sent
     */
    public void printMessage(String message)
    {
            cGUI.append(message + "\n");
    }


    /**
     * Method to send the messages to the server
     * @param message input message string
     */
    void sendOut(String message) {
        try {
            out.writeObject(message);
        }
        catch(IOException e) {
            printMessage("Exception writing to server: " + e);
        }
    }

    /**
     * Disconnect from the server
     */
    private void disconnect() {
        try
        {
            if(out != null)
            {
                out.close();
            }
        }
        catch(Exception e)
        {

        }
        try
        {
            if(in != null)
            {
                in.close();
            }
        }
        catch(Exception e) {

        }
        try{
            if(socket != null)
            {
                socket.close();
            }
        }
        catch(Exception e)
        {

        }

        if(cGUI != null)
        {
            cGUI.disconnect();
        }

    }

    /**
     * Dedicated class to listen for incoming messages from the server
     */
    class ServerListener extends Thread
    {
        //Only method needed.  Listens for inbound messages from the server
        public void run()
        {
            while(true)
            {
                try
                {
                    String message = (String) in.readObject();
                    //prints out to the GUI
                    cGUI.append(message);

                }
                catch(IOException e)
                {
                    printMessage("Server has terminated the connection: " + e);
                    //sets gui to disconnect status
                    cGUI.disconnect();
                    break;
                }

                catch(ClassNotFoundException e2)
                {
                }
            }
        }
    }
}
