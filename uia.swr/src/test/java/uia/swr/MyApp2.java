package uia.swr;

public class MyApp2 implements App {

    @Override
    public boolean runIn() {
        System.out.println("App2 startup");
        return true;
    }

    @Override
    public boolean monitorIn() {
        System.out.println("App2 shutdown");
        return true;
    }

}
