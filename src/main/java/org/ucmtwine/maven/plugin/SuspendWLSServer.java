package org.ucmtwine.maven.plugin;


import java.lang.String;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *  Suspends WCC as a Managed Server
 *  @author txburton
 *  @version Nov 18, 2016
 */
@Mojo(name = "suspend" )
public class SuspendWLSServer extends AbstractWLSServerControlMojo
{

   /** Default Constructor - Does Nothing */
   public SuspendWLSServer() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() throws MojoExecutionException, MojoFailureException
   { super.suspendServer(); }

}