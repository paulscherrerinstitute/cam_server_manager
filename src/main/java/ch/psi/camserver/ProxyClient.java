package ch.psi.camserver;

import ch.psi.csm.JsonSerializer;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class ProxyClient extends InstanceManagerClient{
   
    public ProxyClient(String host, int port) {
        super( host, port, "proxy");
    }

    public ProxyClient(String url) {
        super(url, "proxy");
    }
    
    public Map<String, Map<String, Object>> getServers() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("servers");
    }
    
    public Map<String, Map<String, Object>> getInstances() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("active_instances");
    }
    
    /**
     * Return the configuration.
     */
    public Map<String, Object> getConfig() throws IOException {
        WebTarget resource = client.target(prefix + "/config");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, Object>) map.get("config");
    }
    
    /**
     * Set configuration 
     */
    public void setConfig(Map<String, Object> config) throws IOException {
        String json = JsonSerializer.encode(config);
        WebTarget resource = client.target(prefix + "/config");
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(Entity.json(json));
        json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }    
    

    public static void main(String[] args) throws IOException {
        ProxyClient proxy =  new ProxyClient(null);
         Map<String, Object> info = proxy.getInfo();
         System.out.println(info);
         
    }
}
