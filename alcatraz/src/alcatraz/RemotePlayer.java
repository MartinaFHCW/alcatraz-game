package alcatraz;

import java.io.Serializable;

/**
 * This class represents a player who wants to play the alcatraz game.
 * 
 * @author Azimikhah
 * @author Bichler
 * @author Seidl
 */
public class RemotePlayer implements Serializable{

    // ================================================================================
    // ================================================================================
    // GLOBAL VARIABLES
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
    /**
     * This constructor creates an instance of the client interface for the player
     * @param IC instance of client interface
     */
    public RemotePlayer(IClient IC) {
        this.setIC(IC); 
    }

    /**
     * Sets the desired number of players for this player
     * @param i contains desired number of player
     * @return true if setting the number was successful, false if not
     */
    public boolean setDesiredNumPlayers(int i) {
        if (i >= 2 && i <= 4) {
            desiredNumPlayers = i;
            return true;
        }
        return false;
    }

    /**
     * Gets the desired number of players for this player
     * @return the desired number of players
     */
    public int getDesiredNumPlayers() {
        return desiredNumPlayers;
    }

    /**
     * Sets the RMI name in URL format for this player
     * @param uri contains the RMI name in URL format
     */
    public void setRmiUri(String uri) {
        rmiUri = uri;
    }

    /**
     * Gets the RMI name in URL format for this player
     * @return the RMI name in URL format
     */
    public String getRmiUri() {
        return rmiUri;
    }
    
    /**
     * Sets the player ID for this player
     * @param id contains the player ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the player ID for this player
     * @return the player ID
     */
    public int getId() {
        return id;
    }

    /**
     * Overrides the generic toString() method for returning the player name
     * @return contains the player name
     */
    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * Sets the registration server address
     * @param serverAdr contains the registration server address
     */
    public void setServerAdr(String serverAdr) {
        this.serverAdr = serverAdr;
    }
    
    /**
     * Gets the registration server address
     * @return the registration server address
     */
    public String getServerAdr() {
        return serverAdr;
    }

    /**
     * Sets the name for this player
     * @param name contains the player name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the name for this player
     * @return the player name for this player
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the instance of the interface for the client
     * @param creator contains the instance of the client interface
     */
    public void setIC(IClient creator) {
        this.IC = creator;
    }

    /**
     * Gets the instance of the client interface
     * @return the instance of the client interface
     */
    public IClient getIC() {
        return IC;
    }
}