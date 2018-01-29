package org.ucmtwine.maven.plugin;

import oracle.stellent.ridc.model.DataObject;
import oracle.stellent.ridc.model.DataResultSet;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *  Build the component zip
 */
@Mojo(name = "build", defaultPhase = LifecyclePhase.PACKAGE,
      requiresProject = true)
//@Execute(goal="lib", phase = LifecyclePhase.PREPARE_PACKAGE)
public class BuildComponent extends AbstractComponentMojo
{
  private static final int BUFFER = 2048;
  
  /** project level variable  */
  @Parameter( defaultValue = "${project}", readonly = true, required = true )
  private MavenProject project;

  /**
   * The Directory where compiled classes are found
   */
  @Parameter(property="classes", defaultValue = "${project.build.outputDirectory}")
  private String classes;

  /**
   * A filter of file and directory names to exclude when packaging the
   * component.
   *
   * @parameter default="\.svn|\.git|\._.*|\.DS_Store|thumbs\.db|lockwait\. dat"
   */
  private String excludeFiles;

  public void execute() throws MojoExecutionException, MojoFailureException
  {
    final DataResultSet manifestRs = getResultSetFromHda(getManifestFile(), "Manifest");

    final ZipOutputStream zipStream;

    final Map<String, String> zipListing = new TreeMap<String, String>();

    zipListing.put("manifest.hda", "manifest.hda");
    for (final DataObject row : manifestRs.getRows()) { addToZipList(zipListing, row); }

    if (componentName == null)
    {
      throw new MojoExecutionException("No component name specified "
                                      +"or auto detected");
    }

    //TODO: adjust this to build in the project.build.directory and/or configured location
    //File componentZipFile = new File(componentName + ".zip");
    final File componentZipFile = new File(componentLocation);

    getLog().info("Saving " + componentZipFile.getName() + " with contents:");
    
    try
    {
       //Make sure the build directory exists
       if ( null != componentZipFile.getParentFile() 
         && !componentZipFile.getParentFile().exists() )
       { componentZipFile.getParentFile().mkdirs(); }
       zipStream = new ZipOutputStream(new FileOutputStream(componentZipFile,
                                                            false));
    }
    catch (final FileNotFoundException e)
    { throw new MojoExecutionException("Unable to open zip file for output", e); }

    for (final Iterator<String> i = zipListing.keySet().iterator(); i.hasNext();)
    {
      //TODO: Add fix for Component Classes to use /target/classes
      final String fileSystemPath = i.next();
      final String zipPath = zipListing.get(fileSystemPath);
      getLog().info("  " + zipPath);
      
      final File path = new File(fileSystemPath);
      if ( !path.exists() ) { continue; } //if path doesn't exist, skip it

      try { addFileToZip(zipStream, path, zipPath); }
      catch (final IOException e)
      {
        throw new MojoExecutionException("Unable to close stream for: "
                                        + fileSystemPath, e);
      }
    }

    try { zipStream.close(); }
    catch (final IOException e)
    { throw new MojoExecutionException("Unable to close zip file", e); }
    
    //we've built the component now add it to the project for upstream
    final Artifact artifact = project.getArtifact();
    artifact.setFile(componentZipFile);
  }

  /**
   * Add the file to the zip output stream
   *
   * @param zipStream
   * @param fileSystemPath
   * @param zipPath
   * @throws MojoExecutionException
   * @throws IOException
   */
  private void addFileToZip(final ZipOutputStream zipStream, final File fileSystemPath,
                            final String zipPath)
          throws MojoExecutionException, IOException
  {
    if (!fileSystemPath.canRead())
    { throw new MojoExecutionException("file cannot be read: " + fileSystemPath); }

    if ( fileSystemPath.exists() && fileSystemPath.isDirectory() )
    { addFolderToZip(zipStream, fileSystemPath, zipPath); }
    else
    {
      InputStream in = null;
      try
      {
        in = new FileInputStream(fileSystemPath);

        final ZipEntry entry = new ZipEntry(zipPath);
        zipStream.putNextEntry(entry);

        final byte[] buf = new byte[BUFFER];
        int num = 0;
        while ((num = in.read(buf)) > 0) { zipStream.write(buf, 0, num);  }
      }
      catch (final FileNotFoundException e)
      { throw new MojoExecutionException("file not found: " + fileSystemPath); }
      catch (final IOException e)
      {
        throw new MojoExecutionException("error writing to zip: "
                                        + fileSystemPath);
      }
      finally
      {
        in.close();
        zipStream.closeEntry();
      }
    }
  }

  private void addFolderToZip(final ZipOutputStream zipStream, final File fileSystemPath,
                              String zipPath)
          throws MojoExecutionException, IOException
  {
    // get all items in folder, exclude those in excludeFiles
    if (zipPath.endsWith("/") || zipPath.endsWith("\\"))
    { zipPath = zipPath.substring(0, zipPath.length() - 1); }

    // It is also possible to filter the list of returned files.
    // This example does not return any files that start with `.'.
    final FilenameFilter filter = getFileFilter();

    for (final File entry : fileSystemPath.listFiles(filter))
    {
      final String newZipPath = zipPath + "/" + entry.getName();
      if (entry.isDirectory()) { addFolderToZip(zipStream, entry, newZipPath); }
      else { addFileToZip(zipStream, entry, newZipPath); }
    }
  }

  /**
   * Return the filter used to enforce the <code>excludeFiles</code> config
   * parameter.
   *
   * @return
   */
  private FilenameFilter getFileFilter()
  {
    if (excludeFiles == null)
    {
       excludeFiles = ".*\\.svn|.*\\.git|\\._.*|\\.DS_Store|thumbs\\.db|lockwait\\.dat";
    }
    return new FilenameFilter()
    {
      public boolean accept(final File dir, final String name)
      { return !name.matches(excludeFiles); }
    };
  }

  /**
   * Adds a manifest listing to the zip listing, if the listing is a component,
   * read the component's .hda and add any component specific resources.
   *
   * @param zipListing
   * @param manifestEntry
   * @throws MojoExecutionException
   */
  private void addToZipList(final Map<String, String> zipListing, final DataObject manifestEntry)
            throws MojoExecutionException
  {
    final String entryType = manifestEntry.get("entryType");
    String location  = manifestEntry.get("location");

    // remove component dir prefix
    if (location.startsWith(componentName))
    { location = location.replaceFirst(componentName+"/", ""); }

    final String zipPrefix = "component/"+componentName+"/";
    String fileSystemLocation = location;
    String zipLocation = zipPrefix + location;    

    if (entryType.equals("componentClasses"))
    { 
      getLog().debug("Classes Dir : " + classes);
      
      if ( null == classes ) { classes = "target/classes/"; }
      fileSystemLocation = classes;
    }
    
    if (entryType.equals("componentLib"))
    { 
       fileSystemLocation = libFolder; 
       
       final File libFile = new File(libFolder);
       //if ( !libFile.exists() || libFile.listFiles().length == 0) { return; }
       if ( !libFile.exists() ) { return; }
    }
    
    if (entryType.equals("images"))
    { 
       fileSystemLocation = "images/" + location;
       zipLocation = fileSystemLocation;
    }
    
    zipListing.put(fileSystemLocation, zipLocation);
    
    if (entryType.equals("component"))
    {
      final File componentHdaFile = new File(location);
      addComponentResourcesToZipList(zipListing, componentHdaFile);
    }

  }

  /**
   * Adds all files needed within a component to the zip listing.
   *
   * @param zipListing
   * @param componentHdaFile
   * @throws MojoExecutionException
   */
  private void addComponentResourcesToZipList(final Map<String, String> zipListing,
                                              final File componentHdaFile)
          throws MojoExecutionException
  {
    final String componentName = componentHdaFile.getName().replaceAll(".hda", "");

    // if component name not set yet, set it.
    if (this.componentName == null) { this.componentName = componentName; }

    final String baseZipPath = "component/" + componentName + "/";

    // read ResourceDefinition from hda file.
    final DataResultSet componentResources = getResultSetFromHda(componentHdaFile,
                                                           "ResourceDefinition");

    for (final DataObject resourceRow : componentResources.getRows())
    {
      final String type = resourceRow.get("type");
      final String fileName = resourceRow.get("filename");

      // template entries have multiple files
      // so they need to be included by folder.
      if (type != null && type.equals("template"))
      {
        final String templateFolder = new File(fileName).getParent();
        zipListing.put(templateFolder, baseZipPath + templateFolder);
      }
      else { zipListing.put(fileName, baseZipPath + fileName); }
    }
  }
}
