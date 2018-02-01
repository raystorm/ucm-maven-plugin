package org.ucmtwine.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *  Helper class to contain the file hda file update method
 *  @author txburton
 *  @version Dec 1, 2016
 */
public class FileUpdateHelper 
{
   /**
    *  replaces the line with the specified prefix with the new value
    * 
    *  @param prefix line to replace (ends with '=', will be added if missing.)
    *  @param newValue  The new value to set after prefix
    *  @param hdaFile  file to be processed
    *  @throws IOException 
    *  @throws MojoExecutionException
    *
    *  TODO: heavily based on UpdateClasspath code, refactor/reuse for both.
    */
   public static void replaceLine(String prefix, final String newValue, final File hdaFile)
          throws IOException, MojoExecutionException
   {
        final File tempFile = new File("temp.hda");
        
        final BufferedReader reader = new BufferedReader(new FileReader(hdaFile));
        final PrintWriter writer = new PrintWriter(new FileWriter(tempFile, false));
        String line;
        
        //force post prefix =
        if ( !prefix.endsWith("=") ) { prefix = prefix+"="; }
        
        while ((line = reader.readLine()) != null)
        {
          if (line.startsWith(prefix)) { writer.println(prefix + newValue); }
          else { writer.println(line); }
        }
        
        reader.close();
        writer.flush();
        writer.close();
        
        /* TODO: consider requiring JDK 7 and using  java.nio.file.Files#move() */
        
        File oldFile = new File(hdaFile.getName()+".old.hda");
        
        if (oldFile.exists()) 
        { 
           oldFile.delete();
           try { Thread.sleep(2000); } //wait 2 seconds for delete operation 
           catch (InterruptedException ignored) { }
           
           //wait 30 seconds checking, once per second for file deletion 
           for (int i = 0; i < 30 && oldFile.exists(); ++i)
           {
              try { Thread.sleep(1000); } //pause 1 second before trying again 
              catch (InterruptedException ignored) { }
              oldFile.delete();
           }
        }
        
        if (oldFile.exists()) 
        { 
           throw new MojoExecutionException( "Unable to remove " 
                                           + oldFile.getName() + ".");
        }
        
        rename(hdaFile, oldFile);
        
        rename(tempFile, hdaFile);
        
        //TODO: should file clean-up be added here?
     }
   
    /**
     *  rename attempt to safely rename the file
     *    adds a 30 second retry to the operation
     *  @param origin
     *  @param dest
     *  @throws MojoExecutionException
     */
    private static void rename(File origin, File dest) throws MojoExecutionException
    {
       boolean renamed = false;
       if ( !(renamed = origin.renameTo(dest)) )
       {  
          /*
           * System.gc(); is usually an Anti-Pattern 
           * TODO: Is there a better way to do this? 
           */
          System.gc(); //remove any lingering Java File Handles
          
          //keep trying for 30 seconds, once per second 
          for (int i = 0; i < 30 && !renamed; ++i)
          {
             try { Thread.sleep(1000); } //pause 1 second before trying again 
             catch (InterruptedException ignored) { }
             renamed = origin.renameTo(dest);
          }
          
          if (!renamed) //fail
          {
             throw new MojoExecutionException( "Unable to rename " + origin.getName()
                                             + " to " + dest.getName());
          }
       }
    }

}