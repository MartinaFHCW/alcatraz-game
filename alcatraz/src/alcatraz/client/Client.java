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

public class Client extends UnicastRemoteObject implements IClient, MoveListener {

	private static final long serialVersionUID = 1L;

	private static ClientGUI frame; 
	private static Alcatraz a;
	private int myId;
	private ArrayList<RemotePlayer> playerList = new ArrayList<RemotePlayer>();
	static int startgameInt = 0;

	// ================================================================================
	// ================================================================================
	// MAIN

	public static void main(String[] args) throws RemoteException {
		
		IClient IC = new Client();

		// generate a RemotePlayer instance for ourself
		RemotePlayer p = new RemotePlayer(IC);

		// generate the GUI
		frame = new ClientGUI(p);
		frame.setTitle("Alcatraz");
		// add the gameBoard to our GUI
		frame.setBoard(a.getGameBoard());
		frame.setVisible(true);
	}

	// ================================================================================
	// ================================================================================
	// CONSTRUCTOR

	public Client() throws RemoteException {
		// create an instance of Alcatraz add add this class as listener for
		// moves
		a = new Alcatraz();
		a.addMoveListener(this);
	}


	// ================================================================================
	// ================================================================================
	// METHODS

	// ================================================================================
	// server lookup and register player at server
	/**
	 * @author max
	 * @param p
	 * @return Returns <b>true</b> if the RemotePlayer was successfully registered. <br>
	 * Returns <b>false</b> if the registration failed.
	 */
	public static boolean registerPlayer(RemotePlayer p) {
		
		boolean registerSuccess = false;

		try {
			IServer IS = (IServer) Naming.lookup("rmi://" + p.getServerAdr()
					+ ":1099/RegistrationService");
			System.out.print("Registration proceed... (IP: " + p.getServerAdr()
					+ ")\n");
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
	 * @param p
	 * @return Returns <b>true</b> if the RemotePlayer was successfully unregistered. <br>
	 * Returns <b>false</b> if no such registered RemotePlayer exists.
	 */
	public static boolean unregisterPlayer(RemotePlayer p) {
		boolean unregistrationSuccess = false;

		try {
			IServer IS = (IServer) Naming.lookup("rmi://" + p.getServerAdr() + ":1099/RegistrationService");
			System.out.print("Unregistration proceed...");
			unregistrationSuccess = IS.unregister(p);
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
	 * publishes the Client-Remote-Objects so the moves of the game can be <br>
	 * passed between the Players.
         * @param p
         * @return 
	 */
	public static String publishObject(RemotePlayer p) {

		try {
			String ip = getLocalIp();
			System.out.println("Publish client object");
			if (ip == null) {
				System.out.println("Couldn't find out my IP - are you connected to a network?");
				return null;
			}

			// publish the interface on all IPs
			String rmiUri = "rmi://" + ip + ":1099/" + p.getName();
			LocateRegistry.createRegistry(1099);
                        Naming.rebind(rmiUri, p.getIC());
			System.out.println("Client Services started - (" + rmiUri + ")");
			return rmiUri;

		} catch (RemoteException | MalformedURLException e) {
			System.out.println("Error!");
			e.printStackTrace();
		}

		return null;
	}
	

	// ================================================================================
	// ================================================================================
	// GAME STUFF
    
	/**
         * @param me
         * @return 
         * @throws java.rmi.RemoteException
	 * @see alcatraz.IClient#startGame(java.util.ArrayList)
	 */
	@Override
	public boolean startGame(ArrayList<RemotePlayer> playerList, RemotePlayer me) throws IClientException, RemoteException {
		this.myId = me.getId();
		this.playerList = playerList;
	
		// disable unregister button
		frame.setUnregisterButton(false);
				
		// setup the game
		a.init(playerList.size(), this.myId);
		a.start();

		//show other playernames
		frame.getOutputArea().setText("");
		frame.getOutputArea().append("Players participating in this game: \n");
		for (RemotePlayer s : this.playerList)
		{
			frame.getOutputArea().append(s.getName() + "\n");
		}
		frame.getOutputArea().append(" \n");
		startgameInt = 1;
		return true;
	}

	// ================================================================================
	@Override
	public boolean doMoveRemote(Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws IClientException, RemoteException {
		a.doMove(player, prisoner, rowOrCol, row, col);
		return true;
	}

        @Override
	public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
		System.out.println("moving " + prisoner + " to "
				+ (rowOrCol == Alcatraz.ROW ? "row" : "col") + " "
				+ (rowOrCol == Alcatraz.ROW ? row : col));
		
		for (RemotePlayer p : this.playerList) {
			if (p.getId() == this.myId) {
			}
			else {
				boolean status = false;
				while (! status ) {
					try {
						status = p.getIC().doMoveRemote(player, prisoner, rowOrCol, row, col);
					} catch (IClientException | RemoteException e) {
						System.out.println("Sending move to " + p.getName() + " failed, retrying...");
						// TODO: check 
						// frame.getOutputArea().append("\n" + "player:  " + p.getName() + " was offline.");
						try{
							Thread.sleep(1000);
						}
						catch(InterruptedException ie){ }
					}
				}
			}
		}
	}

        @Override
	public void gameWon(Player player) {
		System.out.println("Player " + player.getId() + " wins. You can now register for another game.");
		
		//reset GUI for registration again
		frame.getOutputArea().append("Player " + player.getId() + " wins. You can now register for another game.");
		frame.setRegisterButton(true);
		
		
	}
	
	//TODO overkill function? maybe delete an use this instead:
	/*
	 * InetAddress address = InetAddress.getLocalHost(); 
	 * String ipAddress = address.getHostAddress();
	 */
	public static String getLocalIp() {
		String ip;
		try {
			// go through all network interfaces
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filter out loopback and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp() )
	                continue;

	            // go through all remaining interfaces
	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                // filter out IPv6 addresses
	                if (addr instanceof Inet6Address)
                		continue;
	                ip = addr.getHostAddress();
	                //System.out.println("My IP: " + ip + " "+showMessage iface.getDisplayName());
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
}
