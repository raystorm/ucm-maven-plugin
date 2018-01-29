package org.ucmtwine.maven.plugin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.maven.plugin.MojoExecutionException;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Hashtable;


/**
 *  Abstract MOJO for controlling the Adminserver via JMX
 *  
 *  converted from: 
 *    http://verticalhorizons.in/jmx-client-code-to-start-stop-suspend-and-resume-weblogic-server-using-serverruntimembean/
 *
 *  @author txburton
 *  @version Nov 16, 2016
 */
public abstract class AbstractWLSServerControlMojo extends AbstractServerAwareMojo
{
   private static final Log log = LogFactory.getLog(AbstractWLSServerControlMojo.class);
   
   private MBeanServerConnection connection;

   private JMXConnector connector;

   private ObjectName service;

   public AbstractWLSServerControlMojo() { }
   
   public MBeanServerConnection getConnection() { return connection; }
   
   public JMXConnector getConnector() { return connector; }

   public void setConnector(final JMXConnector connector)
   { this.connector = connector; }

   public ObjectName getService() { return service; }

   /* Initialize connection to the Runtime MBean Server */
   protected void init() throws MalformedURLException,
                                MalformedObjectNameException, 
                                MojoExecutionException
   {
      final IdcServerDefinition server = getSelectedServer();
      final AdminServerDefinition adminServer = server.getAdminServer();
      
      final String protocol = "t3";
      final int port = Integer.valueOf(adminServer.getPort());
      final String jndiroot = "/jndi/";
      /* AdminServer runtime itself * /
      String mserver = "weblogic.management.mbeanservers.runtime";
      /* Domain Control instead of server runtime */
      final String mserver = "weblogic.management.mbeanservers.domainruntime";
      //*/ 
            
      final JMXServiceURL serviceURL = new JMXServiceURL(protocol,
                                                   adminServer.getHostname(), 
                                                   port, jndiroot + mserver);
      final Hashtable<String, String> env = new Hashtable<String, String>();
      
      String user = adminServer.getUsername();
      String pass = adminServer.getPassword();
      if (null == user) { user = server.getUsername(); }
      if (null == pass) { pass = server.getPassword(); }
      
      env.put(Context.SECURITY_PRINCIPAL, user);
      env.put(Context.SECURITY_CREDENTIALS, pass);
      env.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, 
              "weblogic.management.remote");
      
      getLog().info("connecting to WLS server.");
      try { connector = JMXConnectorFactory.connect(serviceURL, env); }
      catch(final Throwable e)
      {
         final String msg = "Unable to create WLS connector.";
         getLog().error(msg, e);
         throw new MojoExecutionException(msg, e);
      }
      try { connection = connector.getMBeanServerConnection(); }
      catch(final Throwable e)
      {
         final String msg = "Unable to create WLS connection.";
         getLog().error(msg, e);
         throw new MojoExecutionException(msg, e);
      }

      /*
      //print mbean list
      Set<ObjectName> mbeans = connection.queryNames(null, null);
      StringBuilder sb = new StringBuilder((mbeans.size()*2+1));
      sb.append("WLS Beeans: ");
      boolean first = true;
      for (ObjectName mbeanName : mbeans) 
      { 
         if (first) { first = false; }
         else { sb.append(", "); } 
         sb.append(System.getProperty("line.separator"))
           .append(mbeanName); 
      }
      getLog().info(sb.toString());
      */
      
      getLog().info("connecting to WCC Server controller");  
      service = new ObjectName("com.bea:Name="+adminServer.getWlsServerName()+","
                              //+ "Location="+adminServer.getWlsServerName()+","
                              + "Type=ServerLifeCycleRuntime");
      
      getLog().info("connected to WCC Server controller");
   }
   
   /** Helper method to simplify getting current run status */ 
   private String getCurrentState() throws Exception 
   { return (String) connection.getAttribute(service, "State"); }
   
   /** Helper method to simplify calling the services */
   private void invokeService(final String serviceName) throws Exception
   { connection.invoke(service, serviceName, new Object[] {}, new String[] {}); }
        
   public void getRuntimeInfo() 
   {
      getLog().info("Getting runtime info....");
      try 
      {
         init();
         final String state = getCurrentState();
         getLog().info("Server state: " + state);
      } 
      catch(final Exception e) { getLog().error("issue getting runtime info", e); }
   }
        
   public void suspendServer() 
   {
      getLog().info("Suspending managed server....");
      try 
      {
         init();
         invokeService("suspend");
         final String state = getCurrentState();
         getLog().info("Server state: " + state);
      } 
      catch(final Exception e) { getLog().error("issue suspending server", e); }
   }
        
   public void resumeServer() 
   {
      getLog().info("Resuming managed server....");
      try 
      {
         init();
         invokeService("resume");
         final String state = getCurrentState();
         getLog().info("Server state: " + state);
      } 
      catch(final Exception e) { getLog().error("issue resuming.", e); }
   }
        
   public void stopServer() 
   {
      getLog().info("Shutting down managed server....");
      try 
      {
          init();
          invokeService("forceShutdown");
          final String state = getCurrentState();
          getLog().info("Server state: " + state);
      } 
      catch(final Exception e) { getLog().error("issue shutting down", e); }
   }
        
   public void startServer() 
   {
      getLog().info("Starting managed server....");
      try 
      { 
         init();
         invokeService("start");
         final String state = getCurrentState();
         getLog().info("Server state: " + state);
      } 
      catch(final Exception e) { getLog().error("issue starting", e); }
   }
   
   public void restartServer() 
   {
      getLog().info("Starting managed server....");
      try 
      { 
         init();
         String state = getCurrentState();
         getLog().info("Starting state: " + state);
         invokeService("forceShutdown");
         state = getCurrentState();
         getLog().info("Initial post Shutdown state: " + state);

         synchronized(this)
         {
            /* 
             * Wait up to 30 seconds, for shutdown
             * //TODO: make timeout configurable 
             */
            Date now = new Date();
            Date timeout = new Date(now.getTime() + 30000);
            while ( !"SHUTDOWN".equals(state) )
            {
               this.wait(1000);
               now = new Date();
               if ( now.getTime() > timeout.getTime() ) { break; }
               state = getCurrentState(); //check current state
            }
            
            /* SHUTDOWN or assumed shutdown */
            invokeService("start");
            getLog().info("Waiting for server restart.");
            
            /* 
             * initiate start - 15 minute wait
             * //TODO: make timeout configurable
             */
            now = new Date();
            timeout = new Date(now.getTime() + (15*60*1000));
            int counter = 0;
            while ( !"RUNNING".equals(state) )
            {
               this.wait(1000);
               ++counter;
               now = new Date();
               if ( now.getTime() > timeout.getTime() ) { break; }
               state = getCurrentState(); //check current state
               if ( 0 == (counter % 10 )) //display an update roughly every 10 seconds
               { getLog().info(state+" | Still waiting for Startup."); }
            }
         }
         if ( !"RUNNING".equals(state) )
         {
            final String msg = "Timeout reached while waiting for WCC restart!";
            throw new MojoExecutionException(msg);
         }
      }
      catch(final Exception e) { getLog().error("issue starting", e); }
   }
   
}
