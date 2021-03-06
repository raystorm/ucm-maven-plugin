package org.ucmtwine.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 *  Initialization Mojo for ensuring that necessary config properties are set.
 *
 *  @author txburton
 *  @version Mar 28, 2016
 *
 */
@Mojo(name = "init", defaultPhase = LifecyclePhase.INITIALIZE)
public class InitMojo extends AbstractComponentMojo
{

   /** Default Constructor - Does Nothing */
   public InitMojo() { }

   /* (non-Javadoc)
    * @see org.apache.maven.plugin.Mojo#execute()
    */
   public void execute() throws MojoExecutionException {
      getLog().debug("Initializing Ucm-Maven-Plugin");

      determineComponentName();

      project.getProperties().setProperty("componentName",       componentName);
      session.getUserProperties().setProperty("componentName",   componentName);
      session.getSystemProperties().setProperty("componentName", componentName);
      getLog().debug("Setting componentName: " + componentName);

      //do I need to specifically reset the componentName dependent variables here?
      if ( null == componentFileName || "${componentFileName}".equals(componentFileName) )
      {
         componentFileName = componentName + ".zip";
         project.getProperties().setProperty("componentFileName",       componentFileName);
         session.getUserProperties().setProperty("componentFileName",   componentFileName);
         session.getSystemProperties().setProperty("componentFileName", componentFileName);
         getLog().debug("Setting componentFileName: " + componentFileName);
      }

      if ( null == componentLocation || componentLocation.endsWith("${componentFileName}") )
      {
         componentLocation = project.getBuild().getDirectory() +"/"+ componentFileName;
         project.getProperties().setProperty("componentLocation",       componentLocation);
         session.getUserProperties().setProperty("componentLocation",   componentLocation);
         session.getSystemProperties().setProperty("componentLocation", componentLocation);
         getLog().debug("Setting componentLocation: " + componentLocation);
      }

      final File componentZipFile = getComponentZipAsFile();
      if ( null == componentZipFile || componentZipFile.getName().equals("${componentName}") )
      {
         componentZip = componentLocation;
         project.getProperties().put("componentZip",       componentZip);
         session.getUserProperties().put("componentZip",   componentZip);
         session.getSystemProperties().put("componentZip", componentZip);
         getLog().debug("Setting componentZip: " + componentZipFile);
      }

      //TODO grab and set componentLibFolder here from Manifest.hda
      
   }

}
