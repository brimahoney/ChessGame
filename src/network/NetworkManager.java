package network;

import chessgame.ChessMove;
import chessgame.Piece;
import chessgame.Position;
import chessgame.TeamColor;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;

public class NetworkManager
{
    private int localPort;
    private int connectToPort;
    private String hostIP;
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public NetworkManager()
    {
        Properties properties = new Properties();
        try
        {   
            FileReader reader = new FileReader("src/chessgame/ChessGame.properties");
            properties.load(reader);
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        
        localPort = Integer.valueOf(properties.getProperty("localPort"));
        System.out.println("Local port: " + localPort); 
    }
    
    public void startListeningForRequests()
    {
        System.out.println("Starting server at port: " + localPort); 
        
        try 
        {
            serverSocket = new ServerSocket(localPort);
            clientSocket = serverSocket.accept();
            
            hostIP = clientSocket.getInetAddress().getHostAddress();
            connectToPort = clientSocket.getPort();
            
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
            
            Object inComingMessage;

            while ((inComingMessage = in.readObject()) != null) 
            {
                System.out.println("Client request recieved, sending back a move");
                out.writeObject(new ChessMessage(MessageType.GAME_REQUEST) {
                    @Override
                    public void setPayload(Object payload) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                    
                    @Override
                    public Object getPayload() {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                }
                //out.writeObject(new ChessMove(new Position('a', 7), new Position('e', 4), TeamColor.BLACK, Piece.QUEEN));
            }
        } 
        catch(ClassNotFoundException cnfe)
        {
            System.out.println("Exception caught when trying to listen on port "
                + localPort);
            System.out.println(cnfe.getMessage());
        }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen on port "
                + localPort + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }    
        /*
        String hostName = ;
        int portNumber = ;
 
        try (
                Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                ObjectInputStream in = new ObjectInputStream(echoSocket.getInputStream());
                BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
            ) 
        {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) 
            {
                out.println(userInput);
                System.out.println("Got move: " + in.readObject().toString());
            }
        }
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }
        catch (IOException e) 
        {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
        catch(ClassNotFoundException cnfe)
        {
            System.err.println("Class Not Found: " + cnfe.getMessage());
            System.exit(1);
        }
    */
}
