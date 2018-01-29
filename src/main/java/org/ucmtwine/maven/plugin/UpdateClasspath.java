package org.ucmtwine.maven.plugin;

import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.ucmtwine.maven.plugin.FileUpdateHelper.replaceLine;

/**
 * Update the component classpath to include the project's dependencies,
 * if they have changed.
 */
@Mojo(name = "classpath", 
      defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
      requiresDependencyResolution = ResolutionScope.COMPILE)
//@Execute(goal = "compile", phase = LifecyclePhase.COMPILE)
public class UpdateClasspath extends AbstractLibMojo
{
   /** Classpath separator used on Linux/Unix Systems */
   private static final char UNIX_SEPARATOR = ':';
   
   /** Classpath separator used on Windows */
   private static final char WIN_SEPARATOR = ';';
   
   /** Default Separator character to use for building the library path. */
   private static final char SEPARATOR = UNIX_SEPARATOR;
   
   /**
    * Gets the a String of the char for separating lib paths
    * TODO: make configurable
    * @return a String representation of the Library Path Separator character  
    */
   private static String getSeparator()
   { return Character.toString(SEPARATOR); }
      
  /**
   * Overwrite classpath?
   *
   * If true, the entire classpath will be rewritten with maven dependencies.
   *
   * If false (default) only new dependencies will be appended if missing.
   *
   * @parameter property="overwriteClasspath" default-value="true"
   */
  private boolean overwriteClasspath;

  public void execute() throws MojoExecutionException, MojoFailureException
  {
     // find componentName
     determineComponentName();

     //String classPathRoot = "$COMPONENT_DIR/" + componentLibFolder;
     String ComponentPrefix = "$COMPONENT_DIR";
     if ( componentLibFolder.charAt(0) != '/'  ) { ComponentPrefix += "/"; }
     String classPathRoot = ComponentPrefix + componentLibFolder;
     final StringBuilder classpath = new StringBuilder();

     if (classPathRoot.endsWith("/"))
     { classPathRoot = classPathRoot.substring(0, classPathRoot.length() - 2); }

     //add Classes Directory from Manifest.hda
     final DataResultSet manifestRs = getResultSetFromHda(getManifestFile(), "Manifest");

     for ( final DataObject row : manifestRs.getRows() )
     {
        final String entryType = row.get("entryType");
        if ( "componentClasses".equals(entryType) )
        {
           String dir = row.get("location");
           if ( dir.startsWith(componentName) )
           { dir = dir.substring(componentName.length() + 1); }
           if ( dir.charAt(0) == '/' ) { dir = dir.substring(1); }
           classpath.append("$COMPONENT_DIR/").append(dir).append(getSeparator()); 
        }
     }

     final String finalClassPath = appendLibrariesToClasspath(classPathRoot, classpath);

     // TODO: need only update classpath if it has changed!
     writeClassPath(finalClassPath);
  }

  @SuppressWarnings("unused")
  private String appendLibrariesToClasspath(final String classPathRoot,
                                            final StringBuilder classpath)
          throws MojoExecutionException
  {
     // add this artifact, if its a jar type
     if (project.getPackaging().equalsIgnoreCase("jar"))
     {
       classpath.append(classPathRoot).append("/").append(project.getArtifactId())
                .append("-").append(project.getVersion()).append(".jar;");
     }

     getLog().debug("prefix(classPathRoot): " + classPathRoot);

     // @formatter:off
     executeMojo(
         plugin("org.apache.maven.plugins", "maven-dependency-plugin", "2.10"),
         goal("build-classpath"),
         configuration(
             element(name("prefix"),         classPathRoot),
             element(name("fileSeparator"),  "/"),
             element(name("pathSeparator"),  getSeparator()),
             element(name("includeScope"),   includeScope),
             element(name("excludeScope"),   excludeScope),
             element(name("outputProperty"), "componentClassPath")
         ),
         executionEnvironment(project, session, pluginManager));
     // @formatter:on

     final String mojoClassPath = project.getProperties()
                                   .getProperty("componentClassPath");

     if (mojoClassPath != null) { classpath.append(mojoClassPath); }

     return classpath.toString();
  }

  /**
   * Writes the classpath to the hda file.
   *
   * @param classpath
   * @throws MojoExecutionException
   */
  private void writeClassPath(final String classpath) throws MojoExecutionException
  {
    getLog().info("New classpath: " + classpath);

    final File hdaFile = new File(componentName + ".hda");

    if (!hdaFile.exists())
    { throw new MojoExecutionException("Hda file does not exist: " + hdaFile.toString()); }

    try { replaceClassPath(classpath, hdaFile); }
    catch (final IOException ioe)
    { getLog().warn("Error replacing manifest classpath entry.", ioe); }
  }

  private void replaceClassPath(String newClassPath, final File hdaFile)
          throws IOException, MojoExecutionException
  {
     if ( null != newClassPath && newClassPath.endsWith(getSeparator()) )
     { newClassPath = newClassPath.substring(0, newClassPath.length()-1); }
     if ( null != newClassPath && newClassPath.endsWith("/") )
     { newClassPath = newClassPath.substring(0, newClassPath.length()-1); }
     
     replaceLine("classpath", newClassPath, hdaFile);
  }

  @SuppressWarnings("unused")
  private SortedSet<String> getExistingClassPath() throws MojoExecutionException
  {
    final SortedSet<String> items = new TreeSet<String>();

    final File componentHda = new File(componentName + ".hda");

    if (!componentHda.exists())
    { throw new MojoExecutionException("Missing hda: " + componentHda.getName()); }

    // TODO: get and process current lib path
    if (!overwriteClasspath) {

    }

    return items;
  }

}