package org.ucmtwine.maven.plugin;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;

/**
 *  @author txburton
 *  @version Mar 23, 2016
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint="component")
public class WCCComponentMavenLifeCycle
       extends AbstractMavenLifecycleParticipant
{

   /** Default Constructor - Does Nothing */
   public WCCComponentMavenLifeCycle() { }

   @Override
   public void afterProjectsRead(final MavenSession session)
         throws MavenExecutionException
   { super.afterProjectsRead(session); }

   @Override
   public void afterSessionStart(final MavenSession session)
         throws MavenExecutionException
   { super.afterSessionStart(session); }

}
