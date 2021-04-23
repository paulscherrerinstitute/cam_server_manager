package ch.psi.camserver;

import java.io.IOException;
import java.util.Map;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class ProxyClient extends CamServerClient{
    final public static String prefix = "proxy";
    
    public ProxyClient(String host, int port) {
        super( host, port, prefix);
    }

    public ProxyClient(String url) {
        super(url, prefix);
    }
    
    public Map<String, Map<String, Object>> getServers() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("servers");
    }
    
    public Map<String, Map<String, Object>> getInstances() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("active_instances");
    }
    

    public static void main(String[] args) throws IOException {
        ProxyClient proxy =  new ProxyClient(null);
         Map<String, Object> info = proxy.getInfo();
         System.out.println(info);
         
    }
}
