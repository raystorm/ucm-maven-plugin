package org.ucmtwine.maven.plugin;

import org.apache.maven.plugins.annotations.Mojo;

/**
 *  Starts WCC as a Managed Server
 *  @author txburton
 *  @version Nov 18, 2016
 */
@Mojo(name = StringConstants.START)
public class StartWLSServer extends AbstractWLSServerControlMojo
{

   /** Default Constructor - Does Nothing */
   public StartWLSServer() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() { super.startServer(); }

}
