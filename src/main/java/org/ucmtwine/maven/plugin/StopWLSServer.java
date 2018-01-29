package org.ucmtwine.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;

/**
 *  Stops WCC as a Managed Server
 *  @author txburton
 *  @version Nov 18, 2016
 */
@Mojo(name = "stop" )
public class StopWLSServer extends AbstractWLSServerControlMojo
{

   /** Default Constructor - Does Nothing */
   public StopWLSServer() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() { super.stopServer(); }

}
