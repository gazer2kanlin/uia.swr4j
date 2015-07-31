package uia.swr;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Change over end point.
 *
 * @author Kan
 *
 */
public interface ChangeOverPoint extends Remote {

    /**
     * App mode.
     *
     * @author Kan
     *
     */
    public enum Mode {
        STANDBY,
        RUNNING,
        SWITCHING
    }

    /**
     * Make APP into RUNNING mode.
     * @throws RemoteException Call exception.
     */
    public void runIn() throws RemoteException;

    /**
     * Make APP into RUNNING mode.
     * @throws RemoteException Call exception.
     */
    public void standBy() throws RemoteException;

    /**
     * Raise after another APP is STANDBY.
     * @throws RemoteException Call exception.
     */
    public void anotherStandBy() throws RemoteException;

    /**
     * Raise after another APP is RUNNING.
     * @throws RemoteException Call exception.
     */
    public void anotherRunIn() throws RemoteException;

    /**
     * Get mode of this service.
     * @return STANDBY, RUNNING or SWITCHING.
     * @throws RemoteException Call exception.
     */
    public Mode getMode() throws RemoteException;

    /**
     * Get if remote alive or not.
     * @return Alive or not.
     * @throws RemoteException
     */
    public boolean isRemoteAlive() throws RemoteException;

    /**
     * Check if alive or not. Return true.
     * @return
     * @throws RemoteException
     */
    public boolean alive() throws RemoteException;
}
