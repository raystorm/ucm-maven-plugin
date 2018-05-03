package org.ucmtwine.maven.plugin;

import org.apache.maven.plugins.annotations.Parameter;

/**
 *  Object that defines the configured WCC Server
 */
public class IdcServerDefinition 
{

  /** UCM server id */
  @Parameter(required = true)
  private String id;

  /** UCM url and (idc) port e.g. idc://localhost:4444 */
  @Parameter(defaultValue = "idc://localhost:4444")
  private String url;

  /** UCM username */
  @Parameter(defaultValue = "sysadmin")
  private String username;

  /** UCM user's password */
  @Parameter(defaultValue = "idc")
  private String password;

  /** Admin Server info */
  @Parameter private AdminServerDefinition adminServer;
  
  
  public String getId() { return id; }

  public void setId(String id) { this.id = id; }

  public String getUrl() { return url; }

  public void setUrl(String url) { this.url = url; }

  public String getUsername() { return username; }

  public void setUsername(String username) { this.username = username; }

  public String getPassword() { return password; }

  public void setPassword(String password) { this.password = password; }

  public AdminServerDefinition getAdminServer() 
  { 
     if ( null == adminServer ) { adminServer = new AdminServerDefinition(); }
     adminServer.setParentIdcServerDefinition(this);
     return adminServer; 
  }

  public void setAdminServer(AdminServerDefinition adminServer)
  { this.adminServer = adminServer; }
  
  @Override
  public String toString() 
  {
     StringBuilder builder = new StringBuilder();
     builder.append("IdcServerDefinition { \n   ").append(id).append(" :")
                   .append(" \"").append(username).append("@").append(url)
                   .append("\" \n}");
     return builder.toString();
 }

  @Override
  public int hashCode() 
  {
     final int prime = 31;
     int result = 1;
     result = prime * result + ((id == null) ? 0 : id.hashCode());
     return result;
  }
  
  @Override
  public boolean equals(Object obj) 
  {
     if (this == obj) { return true;  }
     if (obj == null) { return false; }
     
     if (getClass() != obj.getClass())  { return false; }
     IdcServerDefinition other = (IdcServerDefinition) obj;
     
     if (id == null && other.id != null) { return false; }
     else if (!id.equals(other.id)) { return false; }
     
     return true;
  }

}
