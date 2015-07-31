package uia.swr;

public interface App {

    /**
     * Turn this application into running.
     * @return Success or not.
     */
    public boolean runIn();

    /**
     * Turn this application into monitoring.
     * @return Success or not.
     */
    public boolean monitorIn();
}
