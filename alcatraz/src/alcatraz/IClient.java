package alcatraz;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import at.falb.games.alcatraz.api.*;

/**
 * This is the interface for the Server to bind the Client via RMI
 * @author Azimikhah
 * @author Bichler
 * @author Seidl
 */
public interface IClient extends Remote {
    /**
     * Gets called by the server to start game if desired number of player (2,3,4) in registration is achieved
     * @param playerList
     * @param player
     * @throws IClientException
     * @throws RemoteException
     */
    public boolean startGame(ArrayList<RemotePlayer> playerList, RemotePlayer player) throws IClientException, RemoteException;
	
    /**
     * Gets called for propagating a move to the other players
     * @param player 
     * @param prisoner
     * @param roworCol
     * @param row
     * @param col
     * @throws IClientException
     * @throws RemoteException
     */
    public boolean doMoveRemote(Player player, Prisoner prisoner, int rowOrCol, int row, int col) throws IClientException, RemoteException;
}