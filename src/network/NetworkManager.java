package network;

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
        
        int localPort = Integer.valueOf(properties.getProperty("localPort"));
        System.out.println("Local port: " + localPort); 
    }
    
    /*System.out.println("Starting server at port: " + portNumber); 
        try (
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                Socket clientSocket = serverSocket.accept();     
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            )
        {
            String inputLine;
            System.err.println("Server ready!  Waiting for requests...");
            while ((inputLine = in.readLine()) != null) 
            {
                System.out.println("Client request recieved, sending back a move");
                out.writeObject(new ChessMove("White", "Queen", 3, 'e', 7, 'a'));
            }
        } 
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
        
        
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
 
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
