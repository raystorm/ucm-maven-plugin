package org.ucmtwine.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
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

   public void execute() throws MojoExecutionException {
     //String libFolder = getLibFolder();
     getLog().debug("Lib Folder: " + libFolder);

     project.getProperties().setProperty(StringConstants.OUTPUT_DIRECTORY, libFolder);
     getLog().debug( "Lib Folder Property: " + project.getProperties().getProperty(StringConstants.OUTPUT_DIRECTORY));
     getLog().debug( "Output Folder: " + outputDirectory);

     executeMojo(
            plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.8"),
            goal("copy-dependencies"),
            configuration(element(name(StringConstants.OUTPUT_DIRECTORY), outputDirectory),
                          element(name(StringConstants.INCLUDE_SCOPE),    includeScope),
                          element(name(StringConstants.EXCLUDE_SCOPE),    excludeScope) //,
                          //element(name(StringConstants.OUTPUT_DIRECTORY), libFolder)
                          ),
            executionEnvironment(project, session, pluginManager));
   }
}
