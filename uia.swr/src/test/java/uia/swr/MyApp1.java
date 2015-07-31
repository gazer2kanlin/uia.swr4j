package uia.swr;

public class MyApp1 implements App {

    @Override
    public boolean runIn() {
        System.out.println("App1 startup");
        return true;
    }

    @Override
    public boolean monitorIn() {
        System.out.println("App1 shutdown");
        return true;
    }

}
