import java.io.*;
import java.net.*;
import java.util.*;

//jed126 (John D)


/**
 * Main server class for the project
 */
public class chatd
{
    //id counter for unique thread ids for clients
    private static int IDCounter = 0;
    //stores the client list
    private ArrayList<ClientProcess> clientList;
    //stores and arraylist of the usernames
    private ArrayList<String> userNames = new ArrayList<>();
    //stores the port number
    private int portNumb;
    //boolean for continous cycling
    private boolean continueRunning;
    //changes names that are taken
    private int takenName = 100;
    //trips true for name taken message
    boolean taken = false;
    /**
     * constructor for the server
     * @param port portnumber
     */
    public chatd(String ports, int port)
    {
        this.portNumb = port;


        clientList = new ArrayList<ClientProcess>();
    }

    /**
     * launches the server
     */
    public void launch() 
    {
        continueRunning = true;

        try
        {
            //makes a new socket
            ServerSocket sSocket = new ServerSocket(portNumb);


            while(continueRunning)
            {
                displayMessage("Server waiting for Clients on port " + portNumb + ".");
                //accept the connection
                Socket socket = sSocket.accept();

                if(!continueRunning)
                    break;
                ClientProcess clientProcess = new ClientProcess(socket);
                for (int x = 0; x < clientList.size(); x++)
                {
                    if(clientList.get(x).username.equals(clientProcess.username))
                    {
                        clientProcess.username = "" + takenName;
                        takenName++;
                        taken = true;
                    }
                }
                clientList.add(clientProcess);
                clientProcess.start();
                if(taken == true) {
                    broadcast("***YOUR NAME WAS TAKEN NEWBIE AND IS NOW:" + takenName + "***", "server");
                }
            }

            //after breaking this will close it down
            try
            {

                sSocket.close();
                for(int i = 0; i < clientList.size(); ++i)
                {
                    ClientProcess clientProcess = clientList.get(i);
                    try {
                        //close all the i/o
                        clientProcess.out.close();

                        clientProcess.in.close();

                        clientProcess.socket.close();
                    }
                    catch(IOException ioE) {

                    }
                }
            }
            catch(Exception e)
            {
                displayMessage("Exception closing the server and clients: " + e);
            }
        }

        catch (IOException e)
        {
            String message = " Exception on ServerSocket: " + e + "\n";
            displayMessage(message);
        }
    }


    private void displayMessage(String msg)
    {
        System.out.println(msg);

    }

    /**
     * Method for broadcasting to specific users
     * Not finished
     * @param message message to transmit
     * @param username username of the client
     */
    public synchronized void broadcast(String message, String username)
    {


        if (username == "server")
        {

        }
        //this was for private chat
        else
            {
            int k = 0;
            while (k < clientList.size())
            {
                if(clientList.get(k).username.equals(username))
                {
                    try 
                    {
                        OutputStream os = clientList.get(k).socket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bw = new BufferedWriter(osw);
                        bw.write(message);
                        bw.flush();
                    }
                    catch(IOException e)
                    {
                        System.out.println("Error setting up private chat");
                    }
                }
            }
        }
        String messageLf = " " + message + "\n";


        System.out.print(messageLf);
        for(int i = clientList.size(); --i >= 0;)
        {
            ClientProcess ct = clientList.get(i);

            if(!ct.writeMessage(messageLf))
            {
                clientList.remove(i);
                displayMessage("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    /**
     * remove somone from the clientlist
     * @param id
     */
    synchronized void removeClient(int id) 
    {
        for(int i = 0; i < clientList.size(); ++i) 
        {

            ClientProcess clientT1 = clientList.get(i);

            if(clientT1.id == id) {

                clientList.remove(i);
                return;
            }
        }
    }

    /**
     * main method launches a server
     * @param args
     */
    public static void main(String[] args) 
    {

        int portNumber = 5007;

        if (args.length != 2) {
            System.err.println("Syntax: java Server <port number>");
            System.exit(1);
        }

        portNumber = Integer.parseInt(args[1]);

        chatd server = new chatd("-port", portNumber);
        server.launch();
    }

    /**
     * threads for each client so many can join
     */
    class ClientProcess extends Thread
    {
        //i/o setup
        Socket socket;
        ObjectInputStream in;
        ObjectOutputStream out;
        //id for the process
        int id;
        //username
        String username;


        /**
         * constructor for the clientprocess
         * only needs a socket for accurate data
         * @param socket socket input
         */
        ClientProcess(Socket socket)
        {

            id = IDCounter++;
            this.socket = socket;

            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                //set up i/o
                out = new ObjectOutputStream(socket.getOutputStream());
                in  = new ObjectInputStream(socket.getInputStream());

                username = (String) in.readObject();
                displayMessage("***username + " + "JUST CONNECTED***");
                userNames.add(username);
            }
            catch (IOException e) {
                displayMessage("Exception creating new Input/output Streams: " + e);
                return;
            }


            catch (ClassNotFoundException e) {
            }

            System.out.println("complete");
        }

        /**
         * run method runs the client process
         */
        public void run() 
        {
            String talkingPartner = "server";
            String message = "";
            boolean keepGoing = true;
            while(keepGoing) 
            {

                try 
                {
                    message = (String) in.readObject();
                    System.out.println(message);
                    displayMessage(message);
                }
                catch (IOException e)
                {
                    displayMessage(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2)
                {
                    break;
                }
                //logout command 
                if (message.equals("/logout"))
                {
                    broadcast("***ATTEMPTING LOGOUT***", talkingPartner);
                    removeClient(id);
                    keepGoing = false;
                }
                //help command
                if (message.equals("/help"))
                {
                    broadcast("***COMMANDS /help /logout /chat***", talkingPartner);

                }
                //private chat command then takes a follow up name
                if (message.equals("/chat"))
                {
                    broadcast("***PLEASE TYPE THE NAME OF A USER***", talkingPartner);
                    String name = "unselected";
                    try {

                        name = (String) in.readObject();
                    }
                    catch (IOException e)
                    {
                        displayMessage(username + " Exception reading Streams: " + e);
                        break;
                    }
                    catch(ClassNotFoundException e2) 
                    {
                        break;
                    }
                    for(int i = 0; i < userNames.size(); i++ )
                    {
                        if(name.equals(userNames.get(i))){
                            broadcast("user found starting chat!", talkingPartner);
                        }
                    }

                }
                //broadcasts the message to everyone for group chat
                broadcast(username + ": " + message, "server");
            }

            //shuts it down
            removeClient(id);
            close();
        }

        /**
         * Close the client process down
         */
        private void close()
        {
            try 
            {
                if(out != null) out.close();
            }
            catch(Exception e) {
                
            }
            try 
            {
                if(in != null) in.close();
            }

            catch(Exception e) {
                
            };
            try 
            {
                if(socket != null) socket.close();
            }
            catch (Exception e) {
                
            }
        }

        /**
         * Writes a message out to the output
         * @param message message to be sent
         * @return returns true if successful
         */
        private boolean writeMessage(String message) 
        {

            if(!socket.isConnected()) 
            {
                close();
                return false;
            }

            try 
            {
                out.writeObject(message);
            }

            catch(IOException e) 
            {
                displayMessage("Error for message to " + username);
                displayMessage(e.toString());
            }
            return true;
        }
    }
}

