package ch.psi.csm;

import ch.psi.utils.IO;
import ch.psi.utils.Sys;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 *
 */
public class DataBuffer {
    static String dataSourcesRepoFolder;
    
    public static void cloneDataSourcesRepo(boolean imageBuffer) throws GitAPIException, IOException{
        String path = getDataSourcesRepoFolder(imageBuffer);
        File gitFile =  new File(path + "/.git");
        if (!gitFile.exists()){
            String url = imageBuffer ? App.getImageBufferSourcesRepo() : App.getDataBufferSourcesRepo();
            Logger.getLogger(DataBuffer.class.getName()).info("Cloning data sources repo: " + App.getDataBufferSourcesRepo() + " to " + path);
            Git.cloneRepository()
              .setURI(App.getDataBufferSourcesRepo())
              .setDirectory(new File(path)) 
              .call();
        }        
        Logger.getLogger(DataBuffer.class.getName()).info("Pulling data sources: " + gitFile);
        Git git = Git.open(gitFile);      
        git.pull();
    }
    

    public static void updateDataSourcesRepo(boolean imageBuffer) throws GitAPIException, IOException{        
        try{
            cloneDataSourcesRepo(imageBuffer);
        } catch (Exception ex){
            Logger.getLogger(DataBuffer.class.getName()).log(Level.WARNING, null, ex);
            System.out.println(ex);
            String path = getDataSourcesRepoFolder(imageBuffer);
            Logger.getLogger(DataBuffer.class.getName()).severe("Deleting data sources repo: " + path);
            IO.deleteRecursive(path);
            cloneDataSourcesRepo(imageBuffer);
        }
    }

    public static String getDataSourcesRepoFolder(boolean imageBuffer) throws IOException{
        String path = getDataSourcesRepoFolder();
        return path + "/" + (imageBuffer ? "ib" : "db");
    }
    
    public static String getDataSourcesRepoFolder() throws IOException{
        if (dataSourcesRepoFolder == null){
            Path tempDir = Files.createTempDirectory("csm");
            IO.deleteFolderOnExit(tempDir.toFile());
            dataSourcesRepoFolder = tempDir.toString();
        }
        return dataSourcesRepoFolder;
        //return Sys.getUserHome() + "/.csm";
    }

    public static String reconnectDataBufferCameraSources(String cameraName) throws IOException, InterruptedException, GitAPIException{
        return reconnectCameraSources(cameraName, false);
    }
    public static String reconnectImageBufferCameraSources(String cameraName) throws IOException, InterruptedException, GitAPIException{
        return reconnectCameraSources(cameraName, true);
    }
    
    static String reconnectCameraSources(String cameraName, boolean imageBuffer) throws IOException, InterruptedException, GitAPIException{
        updateDataSourcesRepo(imageBuffer);
        Logger.getLogger(DataBuffer.class.getName()).info("Reconnecting camera  to " +  (imageBuffer ? "ImageBuffer: " : "DataBuffer: ") + cameraName);
        //String command = "./bufferutils restart --label " + cameraName;
        
        List<String> pars = new ArrayList<>();
        
        pars.add("./bufferutils");
        pars.add("restart");
        pars.add("--label");        
        pars.add(cameraName);
                
        ProcessBuilder pb = new ProcessBuilder(pars);
        pb.directory(new File(getDataSourcesRepoFolder(imageBuffer)));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        p.waitFor();

        BufferedReader reader;
        StringBuilder builder;
        String line = null;
        
        reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        builder = new StringBuilder();
        while ( (line = reader.readLine()) != null) {
            builder.append(line).append(Sys.getLineSeparator());
        }
        String output = builder.toString();
        return  output;        
    }
    
    
    public static void main(String[] args) {
        try{
            String ret = reconnectCameraSources("test", false);
            System.out.println(ret);
        } catch (Exception ex){
            System.err.println(ex);
        }
    }    
    
}
