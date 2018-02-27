import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

//jed126 (John D)

/**
 * GUI class for the client using JFrame
 */
public class ClientGUI extends JFrame implements ActionListener 
{

    //jpanel elements needed for GUI
    private JLabel label;
    private JTextField textField;
    private JTextField serverField, portField;
    private JButton loginButton;
    private JTextArea chatArea;

    //flag for if connected to server or not
    private boolean connected;
    //client object to be stored
    private Client client;

    //portnum and hostname for the server
    private int portNum;
    private String hostName;

    /**
     * constructor for the GUI
     * It's all built in this
     * @param host hostname of the server
     * @param port portnumber of the server
     */
    ClientGUI(String host, int port)
    {

        //calls JFrame constructor to name the file
        super("chatd Networks Project");
        //and port num and hostname
        portNum = port;
        hostName = host;

        //all encompassing panel for the top
        JPanel mainPanel = new JPanel(new GridLayout(3, 3));
        //panel to better organize text
        JPanel textFieldPanel = new JPanel(new GridLayout(3, 7, 3, 5));

        //text fields for server and port info
        serverField = new JTextField(host);
        portField = new JTextField("" + port);
        portField.setHorizontalAlignment(SwingConstants.LEADING);
        portField.setBackground(Color.YELLOW);
        serverField.setBackground(Color.YELLOW);

        textFieldPanel.add(new JLabel("Port #:  "));
        textFieldPanel.add(new JLabel("Enter the server Address:  "));
        //port and server info will go here
        textFieldPanel.add(portField);
        textFieldPanel.add(serverField);
        textFieldPanel.add(new JLabel(""));

        //the upperMain encompasses the text
        mainPanel.add(textFieldPanel);

        label = new JLabel("Enter your username below in the CYAN box then press LOGIN    ", SwingConstants.RIGHT);
        label.setBackground(Color.CYAN);
        mainPanel.add(label);

        //textfield is where you'll type your messages
        textField = new JTextField("");
        textField.setBackground(Color.CYAN);

        //adds the text field to the panel
        mainPanel.add(textField);
        add(mainPanel, BorderLayout.NORTH);


        chatArea = new JTextArea("***WELCOME TO THE CHATD CHATROOM USER!***\n", 180, 180);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(chatArea));

        //make it pop a little
        chatArea.setBackground(Color.YELLOW);

        add(centerPanel, BorderLayout.LINE_START);
        loginButton = new JButton("Login"); //login button
        loginButton.addActionListener(this);
        loginButton.setBackground(Color.GREEN);
        mainPanel.add(loginButton);

        //shouldn't be able to edit the chat area
        chatArea.setEditable(false);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 900);
        setVisible(true);
        textField.requestFocus();

    }

    /**
     * adds the newest message to the GUI
     * @param message
     */
    void append(String message) {
        chatArea.append(message);
        //organize the text
        chatArea.setCaretPosition(chatArea.getText().length() - 1);
    }

    /**
     * For when the connection is lost or quit
     */
    void disconnect() {
        //sets everything to the prelogin state
        //this lets you log in again somewhere

        label.setText("Enter your username below");
        textField.setText("Anonymous");

        portField.setText("" + portNum);
        portField.setEditable(false);
        serverField.setText(hostName);
        serverField.setEditable(false);

        textField.removeActionListener(this);
        connected = false;
    }


    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (connected) {

            String message = textField.getText();
            client.sendOut(message);
            if (message.equalsIgnoreCase("/logout")) {
                //client.display("Logging out");
            }
            if (message.equalsIgnoreCase("/chat")) {
                //client.display("select user");

            }

            textField.setText("");
            return;
        }

        //handles the lone button of the gui
        if (o == loginButton)
        {
            //makes sure username
            String username = textField.getText().trim();

            if (username.length() == 0)
            {
                return;
            }
            //servername
            String serverName = serverField.getText().trim();
            if (serverName.length() == 0)
            {
                return;
            }
            //and port number are all good
            String portNumb = portField.getText().trim();
            if (portNumb.length() == 0)
            {
                return;
            }
            int port = 0;
            try
            {
                port = Integer.parseInt(portNumb);
            }
            catch (Exception en) {
                return;
            }
            //makes the client here that's now associated with this gui
            client = new Client(serverName, port, username, this);
            //quit if client doesn't start
            if (!client.start())
            {
                return;
            }
            textField.setText("");
            label.setText("Enter your message below");
            connected = true;
            //for entering your messages
            textField.addActionListener(this);
        }

    }


    public static void main(String[] args)
    {
        new ClientGUI("127.0.0.1", 5007);
    }

}
