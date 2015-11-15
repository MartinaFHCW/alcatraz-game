package alcatraz.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadConnection;
import spread.SpreadGroup;
import spread.SpreadMessage;
import alcatraz.IClient;
import alcatraz.IClientException;
import alcatraz.IServer;
import alcatraz.IServerException;
import alcatraz.RemotePlayer;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import spread.SpreadException;

/**
 * This class instances a new Server Object.
 * @author Azimikhah
 * @author Bichler
 * @author Seidl
 */
public class Server extends UnicastRemoteObject implements IServer, AdvancedMessageListener {

    // ================================================================================
    // ================================================================================
    // GLOBAL VARIABLES
    private static final long serialVersionUID = 1L;
    private static Server server;
    private int id = 0;
    private final String spreadHost = "localhost";
    private final String spreadGroup = "A3_Seidl";
    private SpreadGroup spreadSelf;
    private final SpreadConnection spread = new SpreadConnection();
    static ArrayList<RemotePlayer> playerList = new ArrayList<RemotePlayer>();

    // ================================================================================
    // ================================================================================
    // CONSTRUCTOR
    public Server() throws RemoteException {}

    /**
     * MAIN method - instances new Sever object and joins spread group
     * @param args
     * @throws RemoteException 
     */
    public static void main(String[] args) throws RemoteException {
        //instances new Server object
        server = new Server();

        //creates random Server ID
        server.id = (int) (Math.random() * 999 + 1);
        System.out.println("My ID: " + server.id);

        //calls the method to join SpreadGroup "A3_Seidl"
        server.joinGroup();
    }

    /**
     * Method for Spread membership handling
     * @return true if joining group is successful, false if not successful
     */
    public boolean joinGroup() {
        try {
            //connecting to spread daemon
            System.out.print("Connecting to spread deamon ("+this.spreadHost+") ... ");
            this.spread.connect(InetAddress.getByName(this.spreadHost), 0, this.id+"", false, true);
            System.out.print("connected ... ");

            //adding this class as listener
            this.spread.add(this);

            //joining the spread group
            SpreadGroup group = new SpreadGroup();
            System.out.println("joining group ");
            group.join(this.spread, this.spreadGroup);

            //remembering myself (Server) for later use
            spreadSelf = this.spread.getPrivateGroup();
            return true;
        }
        catch (UnknownHostException | SpreadException e) {
            System.out.println("Spread error\n" +
                                "  Please make sure that the Spread Daemon is running" +
                                "  This error can also be caused by non-unique server IDs");
            return false;
        }
    }

    /**
     * Listener method (of AdvancedMessageListener) - this method is called when spread has notified us of a membership change
     * @param msg the spread message
     */
    @Override
    public void membershipMessageReceived(SpreadMessage msg) {
        //extracts the membership information from the spread message
        MembershipInfo info = msg.getMembershipInfo();
        //getting the new number of group members
        int num = info.getMembers().length;

        //checks if the method was called because a new member joined
        if (info.isCausedByJoin())  {
            System.out.println("Spread: " + info.getJoined() + " joined group / " + num + " connected / current members:");
            printMemberList(info);

            //checks if it is me who joined the group
            if (info.getJoined().equals(this.spreadSelf) ) {
                System.out.println("  > Message caused by me");

                //checks if I (Server) get the master or not
                //if I'm the only group member, then I call the method to get the master
                if (num == 1) {
                    server.publishObject();
                }
            }
        }
        //checks if the method was called because a member was disconnected
        if (info.isCausedByDisconnect())  {
            System.out.println("Spread: " + info.getDisconnected() + " got disconnected from group / " + num + " connected / current members:");
            printMemberList(info);

            //checks if I (Server) get the master or not
            //if I'm the only remaining group member, then I call the method to get the master
            if (num == 1) {
                server.publishObject();
            }
            //if there exists other remaining group members, I have to check the ID to decide getting the master or not
            else if (num >= 2) {
                //if my ID is the lowest, I call the method to get the master
                if (server.id <= getLowestId(info)) {
                    System.out.println("Becomming master server because of lowest ID");
                    server.publishObject();
                }
            }
        }
        //checks if the method was called because a member has left the group
        if (info.isCausedByLeave())  {
            System.out.println("Spread: " + info.getLeft() + " left group / " + num + " connected / current members:");
            printMemberList(info);

            //checks if I (Server) get the master or not
            //if I'm the only remaining group member, then I call the method to get the master
            if (num == 1) {
                server.publishObject();
            }
            //if there exists other remaining group member, I have to check the ID to decide getting the master or not
            else if (num >= 2) {
                //if my ID is the lowest, I call the method to get the master
                if (server.id <= getLowestId(info)) {
                    System.out.println("Becomming master server because of lowest ID");
                    server.publishObject();
                }
            }
        }
    }

    /**
     * Printing the list of spread group members for informational resons
     * @param info the membership info contains the group members
     */
    private void printMemberList(MembershipInfo info) {
        //looping through all spread group members
        for (SpreadGroup g : info.getMembers() ) {
            System.out.print("  member: "+ g.toString());
            //printing additional information if it's me
            if (g.equals(this.spreadSelf) ) {
                System.out.print("  (me)");
            }
            System.out.println();
        }
    }

    /**
     * Determines the lowest ID of all current spread group members
     * @param info the membership info contains the ID in the string of the member name
     * @return integer - the lowest spread group member ID
     */
    private int getLowestId(MembershipInfo info) {
        //setting the variable for lowest ID to the highest possible integer value
        int lowestId = Integer.MAX_VALUE;
        //looping through all spread group members
        for (SpreadGroup g : info.getMembers() ) {
            //extracting the member ID from the member name string (syntax #<ID>)
            int memberID = Integer.valueOf(g.toString().split("#")[1]);
            //checks if the extraced ID is lower then the previous
            if (memberID <= lowestId) {
                //updates the variable for lowest ID with the new one
                lowestId = memberID;
            }
        }
        //returns the lowest spread group member ID
        return lowestId;
    }

    /**
     * Listener method (of AdvancedMessageListener) for spread message handling
     * This method is called when spread has passed us a regular message
     * @param message contains the spread message with information about the current player list
     */
    @Override
    public void regularMessageReceived(SpreadMessage message) {
        //ignores messages that were sent by myself
        if ( ! message.getSender().equals(this.spreadSelf) ) {
            System.out.println("Received updated PlayerList from" + message.getSender());
            //saves the updated playerList
            playerList = (ArrayList<RemotePlayer>) deserialize(message.getData());
        }
    }

    /**
     * Method for sending the player list to the other spread group members
     * @param obj takes a generic object that gets serialized and then transfered
     */
    public void sendObject(Object obj) {
        //creates a new spread message
        SpreadMessage message = new SpreadMessage();
        message.setReliable();
        //maps the message to the spread group "A3_Seidl"
        message.addGroup(this.spreadGroup);

        try {
            //calls the method for serializing an object and adds the serialized object to the message data
            message.setData(serialize(obj));
            //sends the message via multicast to all group members
            server.spread.multicast(message);
        }
        catch (Exception e) {
            System.out.println("Trouble!");
            e.printStackTrace();
        }
    }

    /**
     * Serializing an object
     * @param obj gets the unserialized object for serializing
     * @return serialized object as a byte array
     */
    public static byte[] serialize(Object obj) {
        try {
            //creates an output stream for the byte array
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            //converts the object to a byte array
            o.writeObject(obj);
            //returning the byte array
            return b.toByteArray();
        }
        catch (Exception e) {
            System.out.println("Trouble!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deserializing a byte array into a generic object
     * @param bytes gets the serialized byte array for deserializing
     * @return deserialized object as a generic object
     */
    public static Object deserialize(byte[] bytes) {
        try {
            //creates an input stream for the generic object
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            //returns the converted generic object
            return o.readObject();
        }
        catch (Exception e) {
            System.out.println("Trouble!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method for registry - RMI
     * Publishes the Server object so the clients can register
     */
    private void publishObject() {
        try {
            //getting IP address from localhost
            InetAddress address = InetAddress.getLocalHost(); 
            String ipAddress = address.getHostAddress();

            System.out.println("Server is starting...");
            System.out.println("Server IP is: " + ipAddress);
            //creates instance of server interface
            IServer IS = new Server();
            //creates and exports a registry instance that accepts requests on port 1099
            LocateRegistry.createRegistry(1099);
            //binds the specified name (rmi://<ipaddress>:1099/RegistrationService) to a new remote object (server interface)
            Naming.rebind("rmi://" + ipAddress + ":1099/RegistrationService", IS);
            System.out.println("RegistrationServer is up and running.");

        } catch (UnknownHostException | RemoteException | MalformedURLException e) {
            System.out.println("Error!");
        }
    }

    /**
     * Registers a player on the Server.
     * @param p contains the player object to register
     * @return true if register was successful, false if not
     * @throws IClientException
     * @throws java.rmi.RemoteException
     * @see alcatraz.IServer#register(alcatraz.Player) 
     */
    @Override
    public boolean register(RemotePlayer p) throws IServerException, RemoteException, IClientException {
        //checks if the desired player name is free
        if (playerList.toString().contains(p.getName())) {
            System.out.println("A player by the name of " + p.getName() + " is already registered.");
            return false;
        }
        //if the player name is free add the player to the list
        playerList.add(p);
        System.out.println("\"" + p.getName() + "\" has registered. "
            + "(GameSize: " + p.getDesiredNumPlayers() + " " + "RMIURI: "
            + p.getRmiUri() + ")");
        
        //counts all registered players that want to play with the same number of players
        int count = 0;
        for (RemotePlayer s : playerList ) {
            if (s.getDesiredNumPlayers() == p.getDesiredNumPlayers()) {
                    count++;
            }
        }

        //if there are enough players, the game gets started
        if (count == p.getDesiredNumPlayers()) {
            System.out.println("Enough players registered to start a game - starting now.");
            //calls method for starting the game with the determined number of players
            startNow(p.getDesiredNumPlayers());
        }

        //calls method for synchronizing the player list with the other servers
        sendObject(playerList);
        return true;
    }

    /**
     * Unregisters a player from the Server
     * @param p contains the player object to unregister
     * @return true if unregister was successful, false if not
     * @throws java.rmi.RemoteException
     * @see alcatraz.IServer#unregister(alcatraz.Player)
     */
    @Override
    public boolean unregister(RemotePlayer p) throws IServerException, RemoteException {
        //looping through player list to find player to unregister
        for (RemotePlayer s : playerList) {
            //if player found in player list
            if (s.getName().equals(p.getName())) {
                //removes player from player list
                playerList.remove(s);
                System.out.println("\"" + p.getName() + "\" has unregistered. ");
                //calls method for publishing/send the new player list to all spread group members
                sendObject(playerList);
                return true;
            }
        }
        return false;
    }

    /**
     * Method for starting a game and informing the players about the game start
     * @param numPlayers contains the number of desired players for the game
     * @return true if game start was successful, false if not
     * @throws RemoteException
     * @throws IClientException
     */
    public boolean startNow(int numPlayers) throws RemoteException, IClientException {
        //creates a temporary game list
        ArrayList<RemotePlayer> gameList = new ArrayList<RemotePlayer>();

        int count = 0;
        //looping through all players in the player list
        for (RemotePlayer p : playerList) {
            //selects the players which want to play with the desired number of players
            if (numPlayers == p.getDesiredNumPlayers()) {
                //gibes each player a unique id
                p.setId(count);
                //adds the player to the temporary game list
                gameList.add(p);
                count++;
            }
            //stops adding players to the list if list is complete
            if (gameList.size() == numPlayers) {
                break;
            }
        }

        //looping through all players in the temporary game list
        for (RemotePlayer p : gameList) {
            System.out.println("Invoking start on \"" + p.getName() + "\" ... ");

            try {
                //creates an instance of the client interface and gets a stub for the remote object (the player from the gaming list)
                IClient IC = (IClient) Naming.lookup(p.getRmiUri());

                //notifies the player about the game start
                if (IC.startGame(gameList, p)) {
                    System.out.print("success\n");
                } else {
                    System.out.print("fail\n");
                    /*if the game cannot be started at the player, he gets removed from the temporary gaming and the player list 
                      and stop with the method*/
                    gameList.remove(p);
                    playerList.remove(p);
                    //calls method for publishing/send the new player list to all spread group members
                    sendObject(playerList);
                    return false;
                }
                //removes the player from the global player list, because he is now already playing a game
                playerList.remove(p);
                //calls method for publishing/send the new player list to all spread group members
                sendObject(playerList);

            } catch (MalformedURLException | NotBoundException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}