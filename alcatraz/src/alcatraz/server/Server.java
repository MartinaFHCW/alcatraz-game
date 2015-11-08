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
import java.rmi.registry.LocateRegistry;

public class Server extends UnicastRemoteObject implements IServer, AdvancedMessageListener {

	private static final long serialVersionUID = 1L;
	
	private static Server server;
	
	private int id = 0;
	private String spreadHost = "localhost";
	private String spreadGroup = "A3_Seidl";
	private SpreadGroup spreadSelf;
	private SpreadConnection spread = new SpreadConnection();
	

	// ================================================================================
	// ================================================================================
	// GLOBAL VARIABLES

	static ArrayList<RemotePlayer> playerList = new ArrayList<RemotePlayer>();

	// ================================================================================
	// ================================================================================
	// CONSTRUCTOR

	public Server() throws RemoteException {

	}

	// ================================================================================
	// ================================================================================
	// MAIN
	public static void main(String[] args) throws RemoteException {
		server = new Server();
		
		server.id = (int) (Math.random() * 999 + 1);
		System.out.println("My ID: " + server.id);

		// if spread group is successful
		server.joinGroup();
	}
	
	// ================================================================================
	// ================================================================================
	// Spread membership handling
	
	public boolean joinGroup() {
		try {
			// connecting to spread daemon
			System.out.print("Connecting to spread deamon ("+this.spreadHost+") ... ");
			this.spread.connect(InetAddress.getByName(this.spreadHost), 0, this.id+"", false, true);
			System.out.print("connected ... ");
			
			// adding this class as listener
			this.spread.add(this);
			
			// joining the spread group
			SpreadGroup group = new SpreadGroup();
			System.out.println("joining group ");
			group.join(this.spread, this.spreadGroup);
			
			// remembering myself for later use
			spreadSelf = this.spread.getPrivateGroup();
			return true;
		} 
		catch (Exception e) {
			System.out.println("Spread error\n" +
					"  Please make sure that the Spread Daemon is running" +
					"  This error can also be caused by non-unique server IDs");
			e.printStackTrace();
			return false;
		}
	}

	// Listener function
	// This function is called when spread has notified us of a membership change
	public void membershipMessageReceived(SpreadMessage msg) {
		// extract the Membership information from the Spread message
		MembershipInfo info = msg.getMembershipInfo();
		int num = info.getMembers().length;   // get the new number of members
		
		// check if the method was called because a new member joined
		if (info.isCausedByJoin())  {
			System.out.println("Spread: " + info.getJoined() + " joined group / " + num + " connected / current members:");
			printMemberList(info);
			
			// is it me who joined the group?
			if (info.getJoined().equals(this.spreadSelf) ) {
				System.out.println("  > Message caused by me");
				
				// to master or not to master?
				// start server if I'm the only member
				if (num == 1) {
					server.publishObject();
				}
			}
		}
		// check if the method was called because a member was disconnected
		if (info.isCausedByDisconnect())  {
			System.out.println("Spread: " + info.getDisconnected() + " got disconnected from group / " + num + " connected / current members:");
			printMemberList(info);
			
			// to master or not to master?
			// start server if I'm the only remaining member
			if (num == 1) {
				server.publishObject();
			}
			// if there are more than one
			else if (num >= 2) {
				// start server if I'm the member with the lowest id
				if (server.id <= getLowestId(info)) {
					System.out.println("Becomming master server because of lowest ID");
					server.publishObject();
				}
			}
		}
		// check if the method was called because a member has left the group
		if (info.isCausedByLeave())  {
			System.out.println("Spread: " + info.getLeft() + " left group / " + num + " connected / current members:");
			printMemberList(info);
			
			// to master or not to master?
			// start server if I'm the only remaining member
			if (num == 1) {
				server.publishObject();
			}
			// if there are more than one
			else if (num >= 2) {
				// start server if I have the lowest id
				if (server.id <= getLowestId(info)) {
					System.out.println("Becomming master server because of lowest ID");
					server.publishObject();
				}
			}
		}
	}
	
	// this method prints the list of members for informational reasons
	private void printMemberList(MembershipInfo info) {
		for (SpreadGroup g : info.getMembers() ) {
			System.out.print("  member: "+ g.toString());
			if (g.equals(this.spreadSelf) ) {
				System.out.print("  (me)");
			}
			System.out.println();
		}
	}
	
	// determine the lowest if of all current spread members
	private int getLowestId(MembershipInfo info) {
		int lowestId = Integer.MAX_VALUE;  // set lowestId to highest possible Integer value
		for (SpreadGroup g : info.getMembers() ) {
			// parse the id of all members - didn't find a better way to get the id
			int memberID = Integer.valueOf(g.toString().split("#")[1]);
			//System.out.print("\n" + lowestId + " <= " + memberID);

			if (memberID <= lowestId) {
				//System.out.print(" -- true");
				lowestId = memberID;
			}
		}
		return lowestId;
	}
	
	// ================================================================================
	// ================================================================================
	// Spread message handling

	// Listener method
	// This method is called when spread has passed us a regular message
	public void regularMessageReceived(SpreadMessage message) {
		// ignore messages that were sent by myself
		if ( ! message.getSender().equals(this.spreadSelf) ) {
			System.out.println("Received updated PlayerList from" + message.getSender());
			// save the updated playerList
			playerList = (ArrayList<RemotePlayer>) deserialize(message.getData());
		}
	}

	// method for sending the playerList to other spread members;
	// takes an generic object that gets serialized and then transfered
	public void sendObject(Object obj) {
		SpreadMessage message = new SpreadMessage();
		message.setReliable();
		message.addGroup(this.spreadGroup);

		try {
			message.setData(serialize(obj));
			server.spread.multicast(message);
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}
	}
	
	// method for serializing an object and returning it as a byte array
	public static byte[] serialize(Object obj) {
		try {
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(obj);
			return b.toByteArray();
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}
		return null;
    }

	// method for trying to deserializing a byte array into an generic object 
	public static Object deserialize(byte[] bytes) {
		try {
			ByteArrayInputStream b = new ByteArrayInputStream(bytes);
			ObjectInputStream o = new ObjectInputStream(b);
			return o.readObject();
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}
		return null;
	}
	
	// ================================================================================
	// ================================================================================
	// REGISTRY STUFF

	/**
	 * Publish the Server Object so the clients can register.
	 *
	 * @author 
	 */
	private void publishObject() {
		try {
			// get IP address
			InetAddress address = InetAddress.getLocalHost(); 
			String ipAddress = address.getHostAddress();
			
			// start the server
			System.out.println("Server is starting...");
			System.out.println("Server IP is: " + ipAddress);
			IServer IS = new Server();
                        LocateRegistry.createRegistry(1099);
			Naming.rebind("rmi://" + ipAddress + ":1099/RegistrationService", IS);
                        //test
			
			// success
			System.out.println("RegistrationServer is up and running.");
			
		} catch (Exception e) {
			System.out.println("Error!");
			e.printStackTrace();
		}
	}

	// ================================================================================
	// ================================================================================
	// METHODS

	/**
	 * Registers a player on the Server.
	 * @throws IClientException 
	 * 
	 * @see alcatraz.IServer#register(alcatraz.Player)
	 */
	@Override
	public boolean register(RemotePlayer p) throws IServerException, RemoteException, IClientException {
				
		// check if the desired player name is free
		if (playerList.toString().contains(p.getName())) {
			System.out.println("A player by the name of " + p.getName() + " is already registered.");
			return false;
		}
		// if the player name is free add the player to the list
		playerList.add(p);
		System.out.println("\"" + p.getName() + "\" has registered. "
				+ "(GameSize: " + p.getDesiredNumPlayers() + " " + "RMIURI: "
				+ p.getRmiUri() + ")");
		
		// count all registered players that want to play with the same number of players
		int count = 0;
		for (RemotePlayer s : playerList ) {
			if (s.getDesiredNumPlayers() == p.getDesiredNumPlayers()) {
				count++;
			}
		}
		
		// if there are enough players, start a game right away
		if (count == p.getDesiredNumPlayers()) {
			System.out.println("Enough players registered to start a game - starting now.");
			startNow(p.getDesiredNumPlayers());
		}
		
		// synchronize list with the other servers
		sendObject(playerList);
		return true;
	}

	/**
	 * Unregisters a player from the server.
	 * 
	 * @see alcatraz.IServer#unregister(alcatraz.Player)
	 * @author max
	 */
	@Override
	public boolean unregister(RemotePlayer p) throws IServerException, RemoteException {
		
		for (RemotePlayer s : playerList) {
			if (s.getName().equals(p.getName())) {
				playerList.remove(s);
				System.out.println("\"" + p.getName() + "\" has unregistered. ");
				sendObject(playerList);
				return true;
			}
		}
		return false;
	}

	/**
	 * Method for starting a game and informing the players about the game start.
	 *
	 * @author 
	 * @param numPlayers
	 * @return
	 * @throws RemoteException
	 * @throws IClientException
	 */
	public boolean startNow(int numPlayers) throws RemoteException,
			IClientException {
		ArrayList<RemotePlayer> gameList = new ArrayList<RemotePlayer>();
		
		// add Players to a temporary gameList
		int count = 0;
		for (RemotePlayer p : playerList) {
			if (numPlayers == p.getDesiredNumPlayers()) {
				p.setId(count); // give each player a unique id
				gameList.add(p);
				count++;
			}
			// stop adding players when the list is complete
			if (gameList.size() == numPlayers) {
				break;
			}
		}

		// go through all players in the gamelist
		for (RemotePlayer p : gameList) {
			System.out.println("Invoking start on \"" + p.getName() + "\" ... ");

			//
			try {
				IClient IC = (IClient) Naming.lookup(p.getRmiUri());

				// notify the players about the game start
				if (IC.startGame(gameList, p)) {
					System.out.print("success\n");
				} else {
					// if the game cannot be started at a player remove him 
					// from the list and stop with function
					System.out.print("fail\n");
					playerList.remove(p);
					sendObject(playerList);
					return false;
				}
				// remove the players from the global playerList
				playerList.remove(p);
				sendObject(playerList);

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}

		}
		return true;
	}
}
