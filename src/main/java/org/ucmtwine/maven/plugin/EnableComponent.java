package org.ucmtwine.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import oracle.stellent.ridc.IdcClient;
import oracle.stellent.ridc.IdcClientException;
import oracle.stellent.ridc.IdcClientManager;
import oracle.stellent.ridc.IdcContext;
import oracle.stellent.ridc.model.DataBinder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/** Enable a deployed component on a server */
@Mojo(name = "enable")
//@Execute(goal="build", phase = LifecyclePhase.PACKAGE)
public class EnableComponent extends AbstractServerAwareMojo
{

  public void execute() throws MojoExecutionException
  {
    IdcServerDefinition server = getSelectedServer();
    
    if (componentName == null)
    {
      throw new MojoExecutionException( "Unable to determine component name. "
                                      + "Does a zip file exist?");
    }

    getLog().info("Enabling component "+componentName+" on " + server.getId());

    IdcClientManager manager = new IdcClientManager();

    try
    {
      @SuppressWarnings("rawtypes")
      IdcClient idcClient = manager.createClient(server.getUrl());

      IdcContext userContext = new IdcContext(server.getUsername(), 
                                              server.getPassword());

      DataBinder binder = idcClient.createBinder();

      // ENABLE COMPONENT  ADMIN_TOGGLE_COMPONENTS
      
      binder.putLocal("IdcService", "ADMIN_TOGGLE_COMPONENTS");
      binder.putLocal("isEnable",   "1");
      
      binder.putLocal("IDC_Id", server.getAdminServer().getWlsServerName());
      binder.putLocal("ComponentNames", componentName);

      idcClient.sendRequest(userContext, binder);
    }
    catch (IdcClientException ice)
    { throw new MojoExecutionException(ice.getMessage()); }
  }
}
