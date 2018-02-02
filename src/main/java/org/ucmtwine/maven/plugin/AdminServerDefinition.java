package org.ucmtwine.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

/**
 *  Object that defines the configured Admin Server
 */
public class AdminServerDefinition 
{
  /** Port the Adminserver runs on */
   @Parameter(defaultValue = StringConstants.STR_7001)
   private String port;

   /** hostname for the adminserver */
   @Parameter(defaultValue = StringConstants.LOCALHOST)
   private String hostname;

   /** WLS Managed Server name */
   @Parameter(defaultValue = StringConstants.ADMINSERVER)
   private String serverName;
   
  /** WLS username - defaults to WCC value */
  @Parameter(defaultValue = "${servers[0].username}") private String username;

  /** WLS password - defaults to WCC value */
  @Parameter(defaultValue = "${servers[0].password}") private String password;
  
  /** Name of the UCM instance as deployed into WLS */
  @Parameter(defaultValue = StringConstants.UCM) private String wlsServerName;
  
  private IdcServerDefinition parent;

  public String getPort() 
  {
     if ( null == port ) { port = StringConstants.STR_7001; }
     return port; 
  }

  public void setPort(final String port) { this.port = port; }

  public String getHostname() 
  {
     if (null == hostname) { hostname = StringConstants.LOCALHOST; }
     return hostname; 
  }
  
  public void setHostname(final String hostname) { this.hostname = hostname; }

  public String getServerName() 
  {
     if ( null == serverName ) { serverName = StringConstants.ADMINSERVER; }
     return serverName; 
  }

  public void setServerName(final String serverName) { this.serverName = serverName; }

  public String getUsername() 
  {
     if (null == username && null != parent) { username = parent.getUsername(); }
     return username; 
  }

  public void setUsername(final String username) { this.username = username; }

  public String getPassword() 
  { 
     if (null == password && null != parent ) { password = parent.getPassword(); }
     return password; 
  }

  public void setPassword(final String password) { this.password = password; }

  public String getWlsServerName() 
  { 
     if ( null == wlsServerName ) { wlsServerName = StringConstants.UCM; }
     return wlsServerName; 
  }

  public void setWlsServerName(final String wlsName) { this.wlsServerName = wlsName; }
  
  public void setParentIdcServerDefinition(final IdcServerDefinition parent)
  { this.parent = parent; }
}
