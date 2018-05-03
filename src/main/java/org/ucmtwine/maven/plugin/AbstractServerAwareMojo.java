package org.ucmtwine.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Extend this if you need your goal to be aware of the servers config and
 * interpret the command line argument to select one of the defined servers.
 */
public abstract class AbstractServerAwareMojo extends AbstractComponentMojo 
{

  /** Content server definitions  */
  @Parameter(property = "servers", required = true)
  protected List<IdcServerDefinition> servers;

  /** Chosen server when executing a deploy */
  @Parameter(property = "server", defaultValue = "")
  protected String requestedServer;

  /**
   * Determines the server that is selected out of the list of configured
   * servers. If no -Dserver=id is supplied, the first server is selected.
   *
   * @return The selected server or the first server if none is specified.
   * @throws MojoExecutionException if an invalid server id is given.
   */
  protected IdcServerDefinition getSelectedServer() throws MojoExecutionException 
  {
    //check for .properties file and merge settings
    //should filename be configurable?
    File propFile = new File(project.getBasedir().getAbsolutePath() 
                            + "/ucm-maven-plugin.properties");
    getLog().info("Checking for PropFile: " + propFile.getAbsolutePath());
    if ( propFile.exists() )
    {
       getLog().info("PropFile found.");
       if (null == servers) { servers = new ArrayList<IdcServerDefinition>(); }
       
       Properties props = new Properties();
       try { props.load(new FileInputStream(propFile)); } 
       catch (IOException ioe) 
       { throw new MojoExecutionException("Error reading properties file.", ioe); }
       
       getLog().info("Properties to set: " + props.stringPropertyNames().toString());
       
       String[] serverIdList = props.getProperty("Servers.id").split(",");
       //getLog().info("PropFile Ids: " + Arrays.toString(serverIdList));
       
       for (String id : serverIdList )
       {
          if ( null == id || "".equals(id.trim()) ) { continue; }
          id = id.trim();
          //iterate over properties
          //example file format here: https://github.com/raystorm/ucm-maven-plugin/issues/4
          String prefix = "Servers." + id;
          IdcServerDefinition propServer = new IdcServerDefinition();
          propServer.setId(id);
          
          //check servers for id
          int IDIndex = servers.indexOf(propServer);
          
          //Setup IdcServerDefinition to store properly
          if ( -1 == IDIndex ) 
          { 
             servers.add(propServer);
             IDIndex = servers.size()-1;
          }
          else { propServer = servers.get(IDIndex); } 
          
          //get .properties values
          String url             = props.getProperty(prefix + ".url");
          String user            = props.getProperty(prefix + ".username");
          String pass            = props.getProperty(prefix + ".password");
          String adminHost       = props.getProperty(prefix + ".adminServer.hostname");
          String adminUser       = props.getProperty(prefix + ".adminServer.username");
          String adminPass       = props.getProperty(prefix + ".adminServer.password");
          String adminServerName = props.getProperty(prefix + ".adminServer.serverName");
          String adminWLSName    = props.getProperty(prefix + ".adminServer.wlsServerName");
          
          //store .properties values
          getLog().info("["+prefix+".url]:" + url);
          if ( null != url && !"".equals(url.trim()) ) 
          { propServer.setUrl(url.trim()); }
          
          if ( null != user && !"".equals(user.trim()) ) 
          { propServer.setUsername(user.trim()); }
          
          if ( null != pass && !"".equals(pass.trim()) ) 
          { propServer.setPassword(pass.trim()); }
          
          if ( null != adminHost && !"".equals(adminHost.trim()) ) 
          { propServer.getAdminServer().setHostname(adminHost.trim()); }
          
          if ( null != adminUser && !"".equals(adminUser.trim()) ) 
          { propServer.getAdminServer().setUsername(adminUser.trim()); }
          //fallback to WCC server
          else if ( null == propServer.getAdminServer().getUsername() 
                 || "".equals(propServer.getAdminServer().getUsername().trim()) )
          { propServer.getAdminServer().setUsername(propServer.getUsername()); }
          
          if ( null != adminPass && !"".equals(adminPass.trim().trim()) ) 
          { propServer.getAdminServer().setPassword(adminPass.trim()); }
          //fallback to WCC server
          else if ( null == propServer.getAdminServer().getPassword() 
                 || "".equals(propServer.getAdminServer().getPassword().trim()) )
          { propServer.getAdminServer().setPassword(propServer.getPassword()); }
          
          if ( null != adminServerName && !"".equals(adminServerName.trim()) ) 
          { propServer.getAdminServer().setServerName(adminServerName.trim()); }
          
          if ( null != adminWLSName && !"".equals(adminWLSName.trim()) ) 
          { propServer.getAdminServer().setWlsServerName(adminWLSName.trim()); }
          
          //getLog().info("PropServer: " + propServer.toString());
          //getLog().info(id+": " + servers.get(IDIndex).toString());
          
          //already stored, shouldn't need to re-add.
          servers.set(IDIndex, propServer); //force - re-store anyway

          //getLog().info(id+": " + servers.get(IDIndex).toString());
          //getLog().info("0: " + servers.get(0).toString());
       }
    }

    if (null == servers || servers.size() == 0) 
    { 
       throw new MojoExecutionException("You have not defined any servers " 
                                       + "in your configuration"); 
    }

    IdcServerDefinition server = null;

    // use specified server
    if (requestedServer != null && requestedServer.length() > 0) 
    {
      for (IdcServerDefinition s : servers) 
      {
        if (s.getId().equalsIgnoreCase(requestedServer)) 
        {
          server = s;
          getLog().info("Selected server " + s.getId() + ": " 
                       + server.getUsername() + " @ " + server.getUrl());
        }
      }

    } 
    else 
    {
      server = servers.get(0);
      getLog().info( "No server specified, using first (" + server.getId() + "): " 
                   + server.getUsername() + " @ " + server.getUrl());
    }

    if (server == null) 
    {
      throw new MojoExecutionException("Unable to find the server \"" 
                                      + String.valueOf(requestedServer)
                                      + "\" specified by \"server\" property");
    }
    return server;
  }
}
