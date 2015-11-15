package alcatraz.client;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;

import alcatraz.IClient;
import alcatraz.IClientException;
import alcatraz.IServer;
import alcatraz.IServerException;
import alcatraz.RemotePlayer;
import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;

/**
 * This class instances a new Client Object.
 * 
 * @author Azimikhah
 * @author Bichler
 * @author Seidl
 */
public class Client extends UnicastRemoteObject implements IClient, MoveListener {

    // ================================================================================
    // ================================================================================
    // GLOBAL VARIABLES
    private static final long serialVersionUID = 1L;
    private static ClientGUI frame; 
    private static Alcatraz a;
    private int myId;
    private ArrayList<RemotePlayer> playerList = new ArrayList<RemotePlayer>();
    static int startgameInt = 0;
    private static int registered = 0;
    private static boolean rmiRegistry = false;
    
    // ================================================================================
    // ================================================================================
    // CONSTRUCTOR
    /**
     * This constructor creates an instance of Alcatraz and add this class as listener for moves
     * @throws RemoteException 
     */
    public Client() throws RemoteException {
            //creates an instance of Alcatraz
            a = new Alcatraz();
            //adds this class as listener for moves
            a.addMoveListener(this);
    }

    /**
     * MAIN method - instances new Client object, generates additional Gui and binds it to Alcatraz GUI
     * @param args
     * @throws RemoteException 
     */
    public static void main(String[] args) throws RemoteException {
            //instances new Client object
            IClient IC = new Client();

            //generates a RemotePlayer instance for this client
            RemotePlayer p = new RemotePlayer(IC);

            //generates the additional GUI for registration and information output
            frame = new ClientGUI(p);
            frame.setTitle("Alcatraz");
            //adds the gameBoard (Alcatraz GUI) to the additional GUI
            frame.setBoard(a.getGameBoard());
            frame.setVisible(true);
    }

    /**
     * Gets a stub for the remote object (the registration server) and registers the player at the Server
     * @param p contains the player object to register
     * @return true if the RemotePlayer (Client) was successfully registered, false if the registration failed
     */
    public static boolean registerPlayer(RemotePlayer p) {
        boolean registerSuccess = false;

        try {
            //gets a stub for the remote object (the registration server) associated with the specified name (rmi://<serverIPaddress>:1099/RegistrationService)
            IServer IS = (IServer) Naming.lookup("rmi://" + p.getServerAdr()
                            + ":1099/RegistrationService");
            System.out.print("Registration proceed... (IP: " + p.getServerAdr()
                            + ")\n");
            //calls the register method on the server interface (overridden from registration server)
            registerSuccess = IS.register(p);

        } catch (IServerException ISe) {
            System.err.println("Registration threw Exception: "
                + ISe.getMessage());
            ISe.printStackTrace();
        } catch (NotBoundException | MalformedURLException | RemoteException | IClientException e) {
            System.err.println("Something did not work, see stack trace.");
            e.printStackTrace();
        }

        return registerSuccess;
    }

    /**
     * Gets a stub for the remote object (the registration server) and unregisters the player at the Server
     * @param p contains the player object to unregister
     * @return true if the RemotePlayer was successfully unregistered, false if no such registered RemotePlayer exists
     */
    public static boolean unregisterPlayer(RemotePlayer p) {
        boolean unregistrationSuccess = false;

        try {
            //gets a stub for the remote object (the registration server) associated with the specified name (rmi://<serverIPaddress>:1099/RegistrationService)
            IServer IS = (IServer) Naming.lookup("rmi://" + p.getServerAdr() + ":1099/RegistrationService");
            System.out.print("Unregistration proceed...");
            //calls the unregister method on the server interface (overridden from registration server)
            unregistrationSuccess = IS.unregister(p);
            try { Naming.unbind("rmi://" + p.getServerAdr() + ":1099/RegistrationService"); } catch (Exception e) { }
            System.out.println("Unregistration completed.");

        } catch (IServerException ISe) {
            System.err.println("Unregistration throw Exception: " + ISe.getMessage());
            ISe.printStackTrace();
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            System.err.println("Something did not work, see stack trace.");
            e.printStackTrace();
        }

        return unregistrationSuccess;
    }
	
    /**
     * Publishes the client-remote-cbject so the moves of the game can be passed between the players
     * @param p contains the player object to be published
     * @return the RMI name in URL format from the player (that was published)
     */
    public static String publishObject(RemotePlayer p) {
        try {
            //gets local IP address from the player (client)
            String ip = getLocalIp();
            System.out.println("Publish client object");
            if (ip == null) {
                    System.out.println("Couldn't find out my IP - are you connected to a network?");
                    return null;
            }
            
            //generates the RMI name in URL format for the player to be published
            String rmiUri = "rmi://" + ip + ":1099/" + p.getName();
            if(rmiRegistry == false){
                //creates and exports a registry instance on the player/client that accepts requests on port 1099
                LocateRegistry.createRegistry(1099);
            }
            //binds the specified name (rmi://<ipaddress>:1099/<playerName>) to a new remote object (client interface)
            Naming.rebind(rmiUri, p.getIC());
            System.out.println("Client Services started - (" + rmiUri + ")");
            //returns the RMI name in URL format (rmi://<ipaddress>:1099/<playerName>)
            return rmiUri;

        } catch (RemoteException | MalformedURLException e) {
            System.out.println("Error!");
            e.printStackTrace();
        }

        return null;
    }
	
    /**
     * Gets called from server to notify the player about starting the game and starts the alcatraz game on the client
     * @param playerList contains the list of players to play the game
     * @param me specifies the player to notify
     * @return true if start of alcatraz game was successful
     * @throws java.rmi.RemoteException
     * @see alcatraz.IClient#startGame(java.util.ArrayList)
     */
    @Override
    public boolean startGame(ArrayList<RemotePlayer> playerList, RemotePlayer me) throws IClientException, RemoteException {
        //gets ID and player list
        this.myId = me.getId();
        this.playerList = playerList;

        //disables the unregister button in the GUI
        frame.setUnregisterButton(false);

        //initializes the alcatraz game with the number of players and the ID from this player
        a.init(playerList.size(), this.myId);
        //starts the alcatraz game
        a.start();

        frame.getOutputArea().setText("");
        frame.getOutputArea().append("Players participating in this game: \n");
        //looping through the playerlist for getting their playernames and printing them on the GUI
        for (RemotePlayer s : this.playerList)
        {
            frame.getOutputArea().append(s.getName() + "\n");
        }
        frame.getOutputArea().append(" \n");
        startgameInt = 1;
        return true;
    }

    // ================================================================================
    /**
     * Calls the method for doing the move which has done the other remote player
     * @param player
     * @param prisoner
     * @param rowOrCol
     * @param row
     * @param col
     * @return true if remote move was successful
     * @throws IClientException
     * @throws RemoteException 
     */
    @Override
    public boolean doMoveRemote(Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws IClientException, RemoteException {
        //calls alcatraz method for doing the remote move on this player too
        a.doMove(player, prisoner, rowOrCol, row, col);
        return true;
    }

    /**
     * Overrides the method in the MoveListener Interface at/falb/games/alcatraz/api/MoveListener.moveDone <br>
     * Gets called if player has done his move and calls method for doing the move on all other players
     * @param player
     * @param prisoner
     * @param rowOrCol
     * @param row
     * @param col 
     */
    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        System.out.println("moving " + prisoner + " to "
            + (rowOrCol == Alcatraz.ROW ? "row" : "col") + " "
            + (rowOrCol == Alcatraz.ROW ? row : col));

        //looping throuh all other players in the player list
        for (RemotePlayer p : this.playerList) {
            if (p.getId() == this.myId) {
            }
            else {
                boolean status = false;
                //initializes timeout counter for retrying when sending failed
                int timeoutcounter = 0;
                while (! status ) {
                    try {
                        //increases counter for timeout
                        timeoutcounter++;
                        //calls method for doing the move on the other player too
                        status = p.getIC().doMoveRemote(player, prisoner, rowOrCol, row, col);
                    } catch (IClientException | RemoteException e) {
                        //if client is not responding to do the move, is will be tried for 5 minutes
                        System.out.println("Sending move to " + p.getName() + " failed, retrying...");
                        //if timeout of 10 retries elapsed, player will be removed of player list and sending will be stopped
                        if (timeoutcounter == 10){
                            //calls method to unregister remote player
                            unregisterPlayer(p);
                            //if this is a game with 2 players and the other player's not reachable, the remaining player wins automatically
                            /**if(this.playerList.size() == 2)
                                this.gameWon(player);*/
                            status = true;
                        }
                        try{
                            //waits 5 seconds before retrying to send the move
                            Thread.sleep(5000);
                        }
                        catch(InterruptedException ie){ }
                    }
                }
            }
        }
    }

    /**
     * Overrides the method in the MoveListener Interface at/falb/games/alcatraz/api/MoveListener.gameWon <br>
     * Gets called if player has won the game and then he can register again for playing another game
     * @param player contains the player who won the game
     */
    @Override
    public void gameWon(Player player) {
        System.out.println("Player " + player.getId() + " wins. You can now register for another game.");

        //resets GUI for registration again
        frame.getOutputArea().append("Player " + player.getId() + " wins. You can now register for another game.");
        frame.setRegisterButton(true);
    }

    /**
     * Determines the local IP address
     * @return string containing the ip address
     */
    public static String getLocalIp() {
        String ip;
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            //looping through all network interfaces
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                //filters out loopback and inactive interfaces
                if (iface.isLoopback() || !iface.isUp() )
                continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                //looping through all IP addresses on this interface
                while(addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    //filters out IPv6 addresses
                    if (addr instanceof Inet6Address)
                            continue;
                    //saves IP address
                    ip = addr.getHostAddress();
                    //returns determined IP address
                    return ip;
                }
            }
            return null;
        } catch (SocketException e) {
            return null;
        }
    }
	
    public void showMessage(String msg) {
        showMessage(msg, true);
    }

    public void showMessage(String msg, boolean console) {
        frame.showMessage(msg);
        if (console)
            System.out.println(msg);
    }
    
    public static int registrationStatus(){
        return registered;
    }
}