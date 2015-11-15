package alcatraz;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface for the client to bind the Server via RMI
 * @author Azimikhah
 * @author Bichler
 * @author Seidl
 */
public interface IServer extends Remote {
    /**
     * Interface - registers a player on the registration server
     * @param p a player object to register
     * @return true if the RemotePlayer was successfully registered, false if the registration failed
     * @throws IServerException
     * @throws RemoteException
     * @throws IClientException 
     */
    public boolean register(RemotePlayer p) throws IServerException, RemoteException, IClientException;
    
    /**
     * Interface - unregisters a player from the server
     * @param p a player object to unregister
     * @return true if the RemotePlayer was successfully unregistered, false if no such registered RemotePlayer exists
     * @throws IServerException
     * @throws RemoteException
     */
    public boolean unregister(RemotePlayer p) throws IServerException, RemoteException;
}