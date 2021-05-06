package ch.psi.csm;

import ch.psi.utils.IO;
import ch.psi.utils.Sys;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
    
    
    public static void cloneDataSourcesRepo(String path) throws GitAPIException, IOException{
        File gitFile =  new File(path + "/.git");
        if (!gitFile.exists()){
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
    

    public static void updateDataSourcesRepo() throws GitAPIException, IOException{
        String path = getDataSourcesRepoFolder();
        try{
            cloneDataSourcesRepo(path);
        } catch (Exception ex){
            Logger.getLogger(DataBuffer.class.getName()).log(Level.WARNING, null, ex);
            System.out.println(ex);
            Logger.getLogger(DataBuffer.class.getName()).severe("Deleting data sources repo: " + path);
            IO.deleteRecursive(path);
            cloneDataSourcesRepo(path);
        }
    }

    public static String getDataSourcesRepoFolder(){
        return Sys.getUserHome() + "/.csm";
    }

    public static String reconnectCameraSources(String cameraName) throws IOException, InterruptedException, GitAPIException{
        updateDataSourcesRepo();
        Logger.getLogger(DataBuffer.class.getName()).info("Reconnecting camera  to DataBuffer: " + cameraName);
        String command = "./bufferutils restart --label " + cameraName;
        
        List<String> pars = new ArrayList<>();
        
        pars.add("bufferutils");
        pars.add("restart");
        pars.add("--label");        
        pars.add(cameraName);
                
        ProcessBuilder pb = new ProcessBuilder(pars);
        pb.directory(new File(getDataSourcesRepoFolder()));
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
            String ret = reconnectCameraSources("test");
            System.out.println(ret);
        } catch (Exception ex){
            System.err.println(ex);
        }
    }    
    
}
