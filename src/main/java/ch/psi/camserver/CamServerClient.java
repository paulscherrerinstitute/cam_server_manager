package ch.psi.camserver;

import ch.psi.csm.JsonSerializer;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class CamServerClient {

    final String url;
    Client client;
    final String prefix;

    public CamServerClient(String host, int port, String prefix) {
        this("http://" + host + ":" + port, prefix);
    }

    public CamServerClient(String url, String prefix) {
        url = (url == null) ? "localhost:8889" : url;
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        this.url = url;
        ClientConfig config = new ClientConfig().register(JacksonFeature.class);
        //In order to be able to delete an entity
        config.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);
        client = ClientBuilder.newClient(config);
        this.prefix = getUrl() + "/api/v1/" +  prefix;
    }

    /**
     * Return the REST api endpoint address.
     */
    public String getUrl() {
        return url;
    }

    void checkReturn(Map<String, Object> ret) throws IOException {
        if (!ret.get("state").equals("ok")) {
            throw new IOException(String.valueOf(ret.get("status")));
        }
    }
    
    void checkName(String name) throws IOException {
        if (name == null) {
            throw new IOException("Invalid name");
        }
    }

    
    /**
     * Return the info of the sever
     */
    public Map<String, Object> getInfo() throws IOException {
        WebTarget resource = client.target(prefix + "/info");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, Object>) map.get("info");
    }
    
    public String getVersion() throws IOException {
        WebTarget resource = client.target(prefix + "/version");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("version");
    }    
    
    public String getLogs() throws IOException {
        WebTarget resource = client.target(getUrl() + "/api/v1/logs/txt");
        String logs = resource.request().accept(MediaType.TEXT_PLAIN).get(String.class);
        return logs;
    }  

    public void reset() throws IOException {
        WebTarget resource = client.target(getUrl() + "/api/v1/reset");
        Response r = resource.request().accept(MediaType.TEXT_HTML).get();
        String json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }  
}
