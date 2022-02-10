package ch.psi.csm;

import ch.psi.utils.swing.SwingUtils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;

/**
 *
 */
public class App {

    private static String[] arguments;
    private static List<String> path = new ArrayList<>();
    static boolean simulated;

    //Arguments
    static public String[] getArguments() {
        return arguments;
    }

    /**
     * Returns the command line argument value for a given key, if present. If
     * not defined returns null. If defined multiple times then returns the
     * latest.
     */
    static public String getArgumentValue(String name) {
        List<String> values = getArgumentValues(name);
        int entries = values.size();
        if (entries <= 0) {
            return null;
        }
        return values.get(entries - 1);
    }

    /**
     * Returns true if argument value is set and not empty
     */
    static public boolean isArgumentDefined(String name) {
        return ((getArgumentValue(name) != null) && (getArgumentValue(name).length() > 0));
    }

    /**
     * Returns the command line argument values for a given key. If key is no
     * present then returns an empty list.
     */
    static public List<String> getArgumentValues(String name) {
        ArrayList<String> argumentValues = new ArrayList<>();
        for (String arg : arguments) {
            if (arg.startsWith("-")) {
                arg = arg.substring(1);
            }
            String[] tokens = arg.split("=");
            if ((tokens.length == 2) && (tokens[0].equals(name))) {
                String ret = tokens[1].trim();
                if (ret.length() >= 1) {
                    argumentValues.add(ret);
                }
            }
        }
        return argumentValues;
    }

    static public boolean hasArgument(String name) {
        if (arguments != null) {
            for (String arg : arguments) {
                if (arg.startsWith("-")) {
                    arg = arg.substring(1);
                }
                if (arg.equals(name)) {
                    return true;
                }
                String[] tokens = arg.split("=");
                if ((tokens.length == 2) && (tokens[0].equals(name))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static File getJarFile() {
        return new java.io.File(App.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .getPath());
    }

    public static String getApplicationVersion() {
        try {
            JarInputStream jarStream = new JarInputStream(new FileInputStream(getJarFile()));
            Manifest manifest = jarStream.getManifest();
            Attributes attr = manifest.getMainAttributes();
            String version = attr.getValue("Implementation-Version");
            String buildTime = attr.getValue("Build-Time");
            return version + " (build " + buildTime + ")";
        } catch (Exception ex) {
            return null;
        }
    }

    static String expandUserHome(String folder) {
        return folder.replaceFirst("^~", System.getProperty("user.home"));
    }
    
    public static String getCameraProxy() {
        String url = "localhost";
        if (hasArgument("camera_proxy")){
            url = getArgumentValue("camera_proxy");
        }
        if (url.startsWith("http://")){            
            url = url.substring(7);
        }
        if (!url.contains(":")){
            url = url + ":8888";
        }
        url = "http://" + url;
        return url;        
    }    

    public static String getPipelineProxy() {
        String url = "localhost";
        if (hasArgument("pipeline_proxy")){
            url = getArgumentValue("pipeline_proxy");
        }
        if (url.startsWith("http://")){
            url = url.substring(7);
        }
        if (!url.contains(":")){
            url = url + ":8889";
        }
        url = "http://" + url;
        return url;        
    }    
    
    public static boolean isExpert() {
         return hasArgument("expert");
    }
    
    
    public static String getDataBufferSourcesRepo(){
        if ( hasArgument("dbsr")){
            return getArgumentValue("dbsr");
        }
        return "https://git.psi.ch/archiver_config/sf_databuffer.git";
    }

    public static String getImageBufferSourcesRepo(){
        if ( hasArgument("ibsr")){
            return getArgumentValue("ibsr");
        }
        return "https://git.psi.ch/archiver_config/sf_imagebuffer.git";
    }
    
    static void printStartupMessage() {
        System.out.println("CamServer Management Console");
        String version = getApplicationVersion();
        if (version != null) {
            System.out.println("Version " + version);
        }
        System.out.println("\n");
    }

    static void printHelpMessage() {
        System.out.println("Usage: csm [-arg[=value]] filename");
        System.out.println("Arguments: ");
        System.out.println("\t-camera_proxy=<address[:port]>\tURL of the camera proxy");
        System.out.println("\t-pipeline_proxy=<address[:port]>\tURL of the pipeline proxy");
        System.out.println("\t-?\t\tPrint this help message");
        System.out.println("\n");
    }
        

    public static void main(String args[]) throws Exception {
        arguments = args;
    
        printStartupMessage();
        if (hasArgument("h")) {
            printHelpMessage();
            System.exit(0);
        };
        
        //Console log level is warning, unless otherwise specified
        Level consoleLogLevel = Level.WARNING;        
        try{
            consoleLogLevel = Level.parse(getArgumentValue("clog"));
        } catch (Exception ex){            
        }
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(consoleLogLevel);
            }
        }     
        UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");
                
       

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();

                WindowListener windowListener = new WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        //if (SwingUtils.showOption(e.getWindow(), "Close","Do you want to close the application?", OptionType.YesNo) == OptionResult.Yes) {
                            System.exit(0);
                        //} else {
                        //    ((JFrame) e.getWindow()).setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                        //}
                    }
                };
                mainFrame.addWindowListener(windowListener);
                                
                boolean forceConfig = hasArgument("config");
                SwingUtils.centerComponent(null, mainFrame);
                mainFrame.setVisible(true);
                if (hasArgument("width")){
                    mainFrame.setSize(Integer.getInteger(getArgumentValue("width")), mainFrame.getHeight());
                }
                if (hasArgument("height")){
                    mainFrame.setSize(mainFrame.getWidth(), Integer.getInteger(getArgumentValue("height")));
                }
            }
        });
    }
}
