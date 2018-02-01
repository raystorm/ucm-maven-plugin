package org.ucmtwine.maven.plugin;


import org.apache.maven.plugins.annotations.Mojo;

/**
 *  Gets the Status of WCC as a Managed Server
 *  @author txburton
 *  @version Nov 18, 2016
 */
@Mojo(name = "status" )
public class StatusWLSServer extends AbstractWLSServerControlMojo
{

   /** Default Constructor - Does Nothing */
   public StatusWLSServer() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() { super.getRuntimeInfo(); }

}
