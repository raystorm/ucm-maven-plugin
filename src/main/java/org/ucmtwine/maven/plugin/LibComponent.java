package org.ucmtwine.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

/**
 *  Create the component Library directory
 *  for simplified inclusion in the final component .zip file
 */
@Mojo(name = "lib", defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
      requiresProject = true)
//@Execute(goal="compile", phase = LifecyclePhase.COMPILE)
public class LibComponent extends AbstractLibMojo
{

   @Parameter(defaultValue="${project.build.directory}/lib")
   private String outputDirectory;

   public void execute() throws MojoExecutionException, MojoFailureException
   {
     //String libFolder = getLibFolder();
     getLog().debug("Lib Folder: " + libFolder);

     project.getProperties().setProperty("outputDirectory", libFolder);
     getLog().debug( "Lib Folder Property: "
                   + project.getProperties().getProperty("outputDirectory"));
     getLog().debug( "Output Folder: " + outputDirectory);

     executeMojo(
            plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8"),
            goal("copy-dependencies"),
            configuration(element(name("outputDirectory"), outputDirectory),
                          element(name("includeScope"),    includeScope),
                          element(name("excludeScope"),    excludeScope) //,
                          //element(name("outputDirectory"), libFolder)
                          ),
            executionEnvironment(project, session, pluginManager));
   }

}
