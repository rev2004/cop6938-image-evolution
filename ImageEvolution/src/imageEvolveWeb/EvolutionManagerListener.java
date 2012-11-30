package imageEvolveWeb;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application Lifecycle Listener implementation class EvolutionManagerListener
 *
 */
public class EvolutionManagerListener implements ServletContextListener {
	
	private static final int MAX_JOBS=2;
	private EvolutionManager em;
	private Thread em_Thread;
	
    /**
     * Default constructor. 
     */
    public EvolutionManagerListener() {
    	em = new EvolutionManager(MAX_JOBS);
    	em_Thread = new Thread(em);
    }
    
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
    	this.em_Thread.start();
    	System.out.println("EvolutionManagerListener started");
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0) {
    	System.out.println("EvolutionManagerListener shutdown");
    }
	
}
