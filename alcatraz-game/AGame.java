/// package at.falb.fh.vtsys;

import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import spread.*;
import java.net.*;
import java.io.*;


/**
 * A AGame class initializing a local Alcatraz game -- illustrating how
 * to use the Alcatraz API.
 */
public class AGame implements MoveListener, AdvancedMessageListener {
	private Alcatraz other[] = new Alcatraz[4]; //not used
	private int numPlayer = 2;

	private String username;
	private String serverHost;
	private String group = "maulwurf";
	private String spreadHost = "localhost";

	private int myId;
	private Player playerList[];

	SpreadConnection spread = new SpreadConnection();
	private Alcatraz a = new Alcatraz();

	public AGame() {
	}

	public void setOther(int i, Alcatraz t) {
	    this.other[i] = t;
	}

	public int getNumPlayer() {
	    return numPlayer;
	}

	public void setNumPlayer(int numPlayer) {
	    this.numPlayer = numPlayer;
	}

	public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
		System.out.println("moving " + prisoner + " to " + (rowOrCol == Alcatraz.ROW ? "row" : "col") + " " + (rowOrCol == Alcatraz.ROW ? row : col));
		for (int i = 0; i < getNumPlayer() - 1; i++) {
			//sendMessage("p:"+prisoner+" > "+rowOrCol+":"+row+":"+col);
			Move m = new Move(player, prisoner, rowOrCol, row, col);
			sendObject(m);
		}
	}

	public void membershipMessageReceived(SpreadMessage message) {
		String s = new String(message.getData());
		System.out.println("New membership message from " + message.getMembershipInfo().getGroup());
	}

	public void regularMessageReceived(SpreadMessage message) {
		String s = new String(message.getData());
		System.out.println("New message from " + message.getSender() + ": "+ s);

		Move m = (Move) deserialize(message.getData());
		a.doMove(m.getPlayer(), m.getPrisoner(), m.getRowOrCol(), m.getRow(), m.getCol());
	}

	public void undoMove() {
		System.out.println("Undoing move");
	}

	public void gameWon(Player player) {
		System.out.println("Player " + player.getId() + " wins.");
	}

	public void parseAttributes(String[] args) {
		if (args.length > 0)
			this.username = args[0];
		if (args.length > 1)
			this.serverHost = args[1];
		if (args.length > 2)
			this.group = args[2];
		if (args.length > 3)
			this.spreadHost = args[3];
		if (args.length > 4)
			this.myId = Integer.parseInt(args[4]);
	}
	
	public boolean register() {
		// sending register request to server (with username + group)
		try {
			Thread.sleep(2000); // show wait message
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}

		// if successfull 
		// server sends us our id + array with other players (playerId, playerName)

		this.myId = this.myId; // to be replaced later
		Player playerList[] = new Player[2];
		playerList[0] = new Player(0);
		playerList[0].setName("Helga");
		playerList[1] = new Player(1);
		playerList[1].setName("Gerhard");

		playerList[this.myId].setName(this.username);
		setNumPlayer(2);
		// if error return false

		return true;
	}

	
	public boolean startDialog() {
		// Erstellung Array vom Datentyp Object, Hinzufügen der Komponenten		
		JTextField name = new JTextField(this.username);
		JTextField serverHost = new JTextField(this.serverHost);
		JTextField group = new JTextField(this.group);
		JTextField spreadHost = new JTextField(this.spreadHost);
		
		Object[] message = {
		  "Name", name,
		  "Server Host", serverHost,
		  "Spread Group", group,
		  "Spread Host", spreadHost
		};

		JOptionPane pane = new JOptionPane( message, 
		  JOptionPane.PLAIN_MESSAGE, 
		  JOptionPane.OK_CANCEL_OPTION);
		pane.createDialog(null, "Alcatraz").setVisible(true);

		this.username = name.getText();
		this.serverHost = serverHost.getText();
		this.group = group.getText();
		this.spreadHost = spreadHost.getText();
		
		if (this.username.isEmpty() || this.serverHost.isEmpty() || this.group.isEmpty() || this.spreadHost.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean joinGroup() {
		try {
			System.out.println("Connecting to spread deamon...");
			this.spread.connect(InetAddress.getByName(this.spreadHost), 0, this.myId+"", false, true);
			this.spread.add(this);

			System.out.println("Joining group...");
			SpreadGroup group = new SpreadGroup();
			group.join(this.spread, this.group);
			return true;
		} 
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
			return false;
		}
	}

	public void sendMessage(String msg) {
		SpreadMessage message = new SpreadMessage();
		message.setReliable();
		message.addGroup(this.group);
		message.setData(msg.getBytes());
		try {
			this.spread.multicast(message);
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}
	}

	public void sendObject(Object obj) {
		SpreadMessage message = new SpreadMessage();
		message.setReliable();
		message.addGroup(this.group);

		try {
			message.setData(serialize(obj));
			this.spread.multicast(message);
		}
		catch (Exception e) {
			System.out.println("Trouble!");
			e.printStackTrace();
		}
	}


	public void startGame() {
		a.init(getNumPlayer(), this.myId);	// a2.init(2, 1);	// a1.init(3, 0);
//		t1.setOther(0, a2);
//		t1.setOther(1, a3);
		a.showWindow();
		a.addMoveListener(this);
		a.start();
	}

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



	 /* @param args Command line args
	 */
	public static void main(String[] args) {

		AGame t1 = new AGame();

		t1.parseAttributes(args);

		// while (true) {
			t1.startDialog();
			if (t1.register() == true) {
				if (t1.joinGroup() == true) {
					t1.startGame();

					Alcatraz a1 = new Alcatraz();
					//t1.setNumPlayer(2);

//					a1.init(this.playerList.length, this.myId);	// a2.init(2, 1);	// a1.init(3, 0);

//					t1.setOther(0, a2);
//					t1.setOther(1, a3);

//					a1.showWindow();
//					a1.addMoveListener(t1);

//					a1.start();
				}
			}
		//}
	}
}
