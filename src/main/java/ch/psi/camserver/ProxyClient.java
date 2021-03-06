package ch.psi.camserver;

import ch.psi.csm.JsonSerializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class ProxyClient extends CamServerClient{
   
    final public static String PREFIX = "proxy";
    
    public ProxyClient(String host, int port) {
        super( host, port, PREFIX);
    }

    public ProxyClient(String url) {
        super(url, PREFIX);
    }
    
    public Map<String, Map<String, Object>> getServers() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("servers");
    }
    
    public Map<String, Map<String, Object>> getInstances() throws IOException {
        return (Map<String, Map<String, Object>>) getInfo().get("active_instances");
    }
    
    
    public List<String> getConfigNames() throws IOException {
        WebTarget resource = client.target(prefix+ "/config_names");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (List<String>) map.get("config_names");        
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
     * Return the configuration.
     */
    public String getNamedConfig(String name) throws IOException {
        checkName(name);
        WebTarget resource = client.target(prefix + "/" + name + "/config");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("config");
    }
    
    /**
     * Set configuration 
     */
    public void setNamedConfig(String name, String config) throws IOException {
        checkName(name);
        WebTarget resource = client.target(prefix + "/" + name + "/config");
        Map<String, Object> map = (Map) JsonSerializer.decode(config, Map.class); //Check if serializable
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(Entity.json(config));
        String json = json = r.readEntity(String.class);
        map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }
    
    public void deleteNamedConfig(String name) throws IOException {
        checkName(name);
        WebTarget resource = client.target(prefix + "/" + name + "/config");
        String json = resource.request().accept(MediaType.TEXT_HTML).delete(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
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
    
    
    /**
     * Stop  instance.
     */
    public void stopInstance(String instanceName) throws IOException {
        checkName(instanceName);
        WebTarget resource = client.target(prefix + "/" + instanceName);
        String json = resource.request().accept(MediaType.TEXT_HTML).delete(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }

    /**
     * Stop all the instances in the server.
     */
    public void stopAllInstances(int serverIndex) throws IOException {
        WebTarget resource = client.target(prefix+ "/server/" + serverIndex);
        String json = resource.request().accept(MediaType.TEXT_HTML).delete(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }    
    
    public void stopAllInstances(String server) throws IOException {
        String compactName = server.replace("http", "").replace("/", "").replace(":", "");
        WebTarget resource = client.target(prefix+ "/server/" + compactName);
        String json = resource.request().accept(MediaType.TEXT_HTML).delete(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }        
    
    
    /**
     * Return the configuration.
     */
    public Map<String, String> getPemanentInstances() throws IOException {
        WebTarget resource = client.target(prefix + "/permanent");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, String>) map.get("permanent_instances");
    }
        
    /**
     * Set configuration 
     */
    public void setPemanentInstances(Map<String, String> config) throws IOException {
        String json = JsonSerializer.encode(config);
        WebTarget resource = client.target(prefix + "/permanent");
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
