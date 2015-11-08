package alcatraz;

import java.io.Serializable;

/**
 * This class represents a player who wants to play the alcatraz game.
 * 
 * @author alex
 * @author max
 */
public class RemotePlayer implements Serializable{

	private static final long serialVersionUID = -1887483427951420040L;

	private String name;
	private String serverAdr;
	private int id;
	private int desiredNumPlayers;
	private String rmiUri;
	private IClient IC;
	

	// ================================================================================
	// ================================================================================
	// CONSTRUCTOR
	
	public RemotePlayer(IClient IC) {
		this.setIC(IC); 
	}

	// ================================================================================
	// ================================================================================
	// METHODS
	public boolean setDesiredNumPlayers(int i) {
		if (i >= 2 && i <= 4) {
			desiredNumPlayers = i;
			return true;
		}
		return false;
	}

	public int getDesiredNumPlayers() {
		return desiredNumPlayers;
	}

	public void setRmiUri(String uri) {
		rmiUri = uri;
	}

	public String getRmiUri() {
		return rmiUri;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return this.getName();
	}

	public String getServerAdr() {
		return serverAdr;
	}

	public void setServerAdr(String serverAdr) {
		this.serverAdr = serverAdr;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IClient getIC() {
		return IC;
	}

	public void setIC(IClient creator) {
		this.IC = creator;
	}
}
