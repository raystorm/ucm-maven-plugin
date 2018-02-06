package org.ucmtwine.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;

/**
 *  Restarts WCC as a Managed Server
 *  @author txburton
 *  @version Nov 18, 2016
 */
@Mojo(name = StringConstants.RESTART)
public class RestartWLSServer extends AbstractWLSServerControlMojo
{

   /** Default Constructor - Does Nothing */
   public RestartWLSServer() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() { super.restartServer(); }

}
