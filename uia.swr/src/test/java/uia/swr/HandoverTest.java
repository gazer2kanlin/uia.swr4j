package uia.swr;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Ignore;
import org.junit.Test;

public class HandoverTest {

    @Test
    @Ignore
    public void testRemote() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        Registry registry = LocateRegistry.getRegistry("192.168.50.1");
        System.setProperty("java.security.policy", System.getProperty("user.dir") + "/swr.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        for (String serv : registry.list()) {
            System.out.println(serv);
        }
        long t = System.currentTimeMillis();
        System.out.println(registry.lookup("PISSVR"));
        System.out.println(System.currentTimeMillis() - t);
    }

    @Test
    @Ignore
    public void test1() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        LocateRegistry.createRegistry(1099);
        System.setProperty("java.security.policy", System.getProperty("user.dir") + "/swr.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        ChangeOverPointServer svrA = new ChangeOverPointServer(new MyApp1(), "MyApp1", "MyApp2", "localhost", true);
        ChangeOverPointServer svrB = new ChangeOverPointServer(new MyApp2(), "MyApp2", "MyApp1", "localhost", false);

        svrB.start();
        svrA.start();

        Thread.sleep(10000);
        svrA.standBy();
        Thread.sleep(10000);
        svrB.standBy();
        Thread.sleep(10000);
        svrA.standBy();
        Thread.sleep(10000);
    }

    @Test
    @Ignore
    public void test2() throws Exception {
        PropertyConfigurator.configure("log4j.properties");

        LocateRegistry.createRegistry(1099);
        System.setProperty("java.security.policy", System.getProperty("user.dir") + "/swr.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        ChangeOverPointServer svrA = new ChangeOverPointServer(new MyApp1(), "PISSVR", "PISSVR", "192.168.50.1", true);

        svrA.start();
        Thread.sleep(30000);
    }
}
