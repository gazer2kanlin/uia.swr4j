package uia.swr;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Kan
 *
 */
public class ChangeOverPointServer extends UnicastRemoteObject implements ChangeOverPoint {

    private static final long serialVersionUID = -2520776277130805399L;

    private static Logger logger = LogManager.getLogger(ChangeOverPointServer.class);

    private final App app;

    private final String localRMIName;

    private final String remoteRMIName;

    private final String remoteAddr;

    private final String remoteURL;

    private boolean remoteAlive;

    private boolean master;

    private ChangeOverPoint.Mode mode;

    private ChangeOverPoint remotePoint;

    private int counter;

    /**
     * Constructor.
     * @param app Application.
     * @param appName Application name.
     * @param remoteAddr Remote address.
     * @param master Master or not.
     * @throws RemoteException RMI exception.
     */
    public ChangeOverPointServer(App app, String appName, String remoteAddr, boolean master) throws RemoteException {
        this(app, appName, appName, remoteAddr, master);
    }

    /**
     * Constructor.
     * @param app Application.
     * @param localRMIName Local RMI name.
     * @param remoteRMIName Remote RMI name.
     * @param remoteAddr Remote address.
     * @param master Master or not.
     * @throws RemoteException RMI exception.
     */
    public ChangeOverPointServer(App app, String localRMIName, String remoteRMIName, String remoteAddr, boolean master) throws RemoteException {
        super();

        this.app = app;
        this.localRMIName = localRMIName;
        this.remoteRMIName = remoteRMIName;
        this.remoteAddr = remoteAddr;
        this.remoteURL = "rmi://" + this.remoteAddr + ":1099/" + this.remoteRMIName;
        this.mode = Mode.STANDBY;
        this.master = master;
    }

    /**
     * Get this server is master or not.
     * @return Master or not.
     */
    public boolean isMaster() {
        return this.master;
    }

    /**
     * Start this server.
     * @return Result.
     */
    public boolean start() {
        String localhost = "localhost";
        String message = String.format("swr> rebind //%1$s:1099/%2$-22s - %3$s",
                localhost,
                this.localRMIName,
                getClass().getName());
        try {
            Naming.rebind(this.localRMIName, this);
            logger.info(message);
        }
        catch (Exception ex) {
            logger.error(message + " failure", ex);
            return false;
        }

        new Thread(() -> checkRunning()).start();

        return true;
    }

    /**
     * Stop this server.
     */
    public void stop() {

    }

    @Override
    public void standBy() throws RemoteException {
        this.mode = Mode.SWITCHING;
        boolean ok = this.app.monitorIn();
        if (ok) {
            this.mode = Mode.STANDBY;
            logger.info("swr> switch to STANDBY mode.");
            new Thread(() -> {
                if (rebindRemote()) {
                    try {
                        ChangeOverPointServer.this.remotePoint.anotherStandBy();
                    }
                    catch (Exception ex) {
                        ChangeOverPointServer.this.remotePoint = null;
                    }
                }
            }).start();
        }
        else {
            this.mode = Mode.RUNNING;
            logger.error("swr> switch to STANDBY mode failure.");
        }
    }

    @Override
    public void runIn() throws RemoteException {
        this.mode = Mode.SWITCHING;
        boolean ok = this.app.runIn();
        if (ok) {
            this.mode = ChangeOverPoint.Mode.RUNNING;
            logger.info("swr> switch to RUNNING mode.");
            new Thread(() -> {
                if (rebindRemote()) {
                    try {
                        ChangeOverPointServer.this.remotePoint.anotherRunIn();
                    }
                    catch (Exception ex) {
                        ChangeOverPointServer.this.remotePoint = null;
                    }
                }
            }).start();
        }
        else {
            this.mode = Mode.STANDBY;
            logger.error("swr> switch to RUNNING mode failure.");
        }
    }

    @Override
    public void anotherStandBy() throws RemoteException {
        if (this.mode == ChangeOverPoint.Mode.STANDBY) {
            this.mode = Mode.SWITCHING;
            this.app.runIn();
            this.mode = Mode.RUNNING;
        }
    }

    @Override
    public void anotherRunIn() throws RemoteException {
        if (this.mode == ChangeOverPoint.Mode.RUNNING) {
            this.mode = Mode.SWITCHING;
            this.app.monitorIn();
            this.mode = Mode.STANDBY;
        }
    }

    @Override
    public Mode getMode() throws RemoteException {
        return this.mode;
    }

    @Override
    public boolean isRemoteAlive() throws RemoteException {
        return this.remoteAlive;
    }

    @Override
    public boolean alive() throws RemoteException {
        return true;
    }

    private boolean rebindRemote() {
        try {
            if (this.remotePoint == null) {
                this.remotePoint = (ChangeOverPoint) Naming.lookup(this.remoteURL);
            }
            this.remotePoint.alive();
            return true;
        }
        catch (Exception ex) {
            this.remotePoint = null;
        }
        return false;
    }

    private void checkRunning() {
        while (true) {
            synchronized (this.remoteURL) {
                try {
                    this.remoteURL.wait(3000);
                }
                catch (Exception ex) {

                }
            }

            this.remoteAlive = rebindRemote();
            try {
                if (!this.remoteAlive) {
                    logger.error("swr> remote " + this.remoteURL + " unknown");
                    if (this.mode == ChangeOverPoint.Mode.STANDBY) {
                        runIn();
                    }
                    continue;
                }

                try {
                    Mode remoteMode = this.remotePoint.getMode();
                    if (Mode.SWITCHING == remoteMode) {
                        continue;
                    }

                    if (this.mode != remoteMode) {
                        logger.debug("swr> " + this.mode);
                        continue;
                    }
                }
                catch (Exception ex) {
                    this.remotePoint = null;
                    continue;
                }

                if (this.mode == ChangeOverPoint.Mode.RUNNING) {
                    if (!this.master) {
                        logger.info("swr> remote:" + this.remoteURL + " in RUNNING. switch self ...");
                        standBy();
                    }
                }
                else {
                    if (this.master) {
                        logger.info("swr> remote:" + this.remoteURL + " is STANDBY. switch self ...");
                        runIn();
                    }
                }
            }
            catch (Exception ex) {
                logger.error("swr> ", ex);
            }
        }
    }
}
