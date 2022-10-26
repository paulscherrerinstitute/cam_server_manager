package ch.psi.camserver;

import ch.psi.csm.JsonSerializer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class PipelineClient extends InstanceManagerClient{
    
    final public static String PREFIX = "pipeline";

    public PipelineClient(String host, int port) {
        super( host, port, PREFIX);
    }

    public PipelineClient(String url) {
        super(url, PREFIX);
    }

    /**
     * List existing cameras.
     */
    public List<String> getCameras() throws IOException {
        WebTarget resource = client.target(prefix + "/camera");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (List<String>) map.get("cameras");
    }

    /**
     * List existing pipelines.
     */
    public List<String> getPipelines() throws IOException {
        WebTarget resource = client.target(prefix);
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (List<String>) map.get("pipelines");
    }

    /**
     * List running instances.
     */
    public List<String> getInstances() throws IOException {
        Map<String, Object> info = getInfo();
        Map<String, Object> instances = (Map<String, Object>) info.get("active_instances");
        return new ArrayList(instances.keySet());
    }

    /**
     * Return the pipeline configuration.
     */
    public Map<String, Object> getConfig(String pipelineName) throws IOException {
        checkName(pipelineName);
        WebTarget resource = client.target(prefix + "/" + pipelineName + "/config");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, Object>) map.get("config");
    }
    

    /**
     * Return the instance configuration.
     */
    public Map<String, Object> getInstanceConfig(String instanceId) throws IOException {
        checkName(instanceId);
        WebTarget resource = client.target(prefix + "/instance/" + instanceId + "/config");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, Object>) map.get("config");
    }
    
    public Object getInstanceConfigValue(String instanceId, String value) throws IOException {
        Map<String, Object> pars = getInstanceConfig(instanceId);
        return pars.get(value);
    }    

    /**
     * Set instance configuration.
     */
    public void setInstanceConfig(String instanceId, Map<String, Object> config) throws IOException {
        checkName(instanceId);
        String json = JsonSerializer.encode(config);
        WebTarget resource = client.target(prefix + "/instance/" + instanceId + "/config");
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(Entity.json(json));
        json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }

    public void setInstanceConfigValue(String instanceId, String name, Object value) throws IOException {
        Map<String, Object> pars = new HashMap();
        pars.put(name, value);
        setInstanceConfig(instanceId, pars);
    }    
        
    /**
     * Return the instance info.
     */
    public Map<String, Object> getInstanceInfo(String instanceId) throws IOException {
        checkName(instanceId);
        WebTarget resource = client.target(prefix + "/instance/" + instanceId + "/info");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (Map<String, Object>) map.get("info");
    }

    /**
     * Return the instance stream. If the instance does not exist, it will be created and will be
     * read only - no config changes will be allowed. If instanceId then return existing (writable).
     */
    public String getStream(String instanceId) throws IOException {
        checkName(instanceId);
        WebTarget resource = client.target(prefix + "/instance/" + instanceId);
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("stream");
    }

    /**
     * Create a pipeline from a config file. Pipeline config can be changed. Return pipeline
     * instance id and instance stream in a list.
     */
    public List<String> createFromName(String pipelineName, String id) throws IOException {
        checkName(pipelineName);
        WebTarget resource = client.target(prefix + "/" + pipelineName);
        if (id != null) {
            resource = resource.queryParam("instance_id", id);
        }
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(null);
        String json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return Arrays.asList(new String[]{(String) map.get("instance_id"), (String) map.get("stream")});
    }

    /**
     * Create a pipeline from the provided config. Pipeline config can be changed. Return pipeline
     * instance id and instance stream in a list.
     */
    public List<String> createFromConfig(Map<String, Object> config, String id) throws IOException {
        String json = JsonSerializer.encode(config);
        WebTarget resource = client.target(prefix);
        if (id != null) {
            resource = resource.queryParam("instance_id", id);
        }
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(Entity.json(json));
        json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return Arrays.asList(new String[]{(String) map.get("instance_id"), (String) map.get("stream")});
    }

    /**
     * Set config of the pipeline. Return actual applied config.
     */
    public String savePipelineConfig(String pipelineName, Map<String, Object> config) throws IOException {
        checkName(pipelineName);
        String json = JsonSerializer.encode(config);
        WebTarget resource = client.target(prefix + "/" + pipelineName + "/config");
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(Entity.json(json));
        json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("stream");
    }

    /**
     * Collect the background image on the selected camera. Return background id.
     */
    public String captureBackground(String cameraName, Integer images) throws IOException {
        checkName(cameraName);
        WebTarget resource = client.target(prefix + "/camera/" + cameraName + "/background");
        if (images != null) {
            resource = resource.queryParam("n_images", images);
        }
        Response r = resource.request().accept(MediaType.TEXT_HTML).post(null);
        String json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("background_id");
    }

    public String getLastBackground(String cameraName) throws IOException {
        checkName(cameraName);
        WebTarget resource = client.target(prefix + "/camera/" + cameraName + "/background");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("background_id");
    }
    
    public List<String> getBackgrounds(String cameraName) throws IOException {
        checkName(cameraName);
        WebTarget resource = client.target(prefix + "/camera/" + cameraName + "/backgrounds");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (List) map.get("background_ids");
    }    
    
    public BufferedImage getLastBackgroundImage(String cameraName) throws IOException {
        return getBackgroundImage(getLastBackground(cameraName));
    }
    
    public BufferedImage getBackgroundImage(String name) throws IOException {
        WebTarget resource = client.target(prefix + "/background/" + name + "/image");
        byte[] ret = resource.request().accept(MediaType.APPLICATION_OCTET_STREAM).get(byte[].class);
        return ImageIO.read(new ByteArrayInputStream(ret));
    }    
   

    /**
     * Start pipeline streaming, creating a private instance, and set the stream endpoint to the
     * current stream socket.
     */
    public void start(String pipelineName) throws IOException {
        start(pipelineName, false);
    }

    /**
     * Start pipeline streaming, and set the stream endpoint to the current stream socket. If shared
     * is true, start the read-only shared instance of the pipeline.
     */
    public void start(String pipelineName, boolean shared) throws IOException {
        start(pipelineName, shared, null);
    }

    /**
     * Start pipeline streaming, using a shared instance called instanceId. If instance id different
     * than the pipeline name, instance is not readonly.
     */
    public void start(String pipelineName, String instanceId) throws IOException {
        if (!getInstances().contains(instanceId)) {
            start(pipelineName, false, instanceId);
        } else {
            start(instanceId, true, null);
        }
    }

    List<String> start(String pipelineName, boolean shared, String instanceId) throws IOException {
        if (shared) {            
            String url = getStream(pipelineName);
            return Arrays.asList(new String[]{pipelineName, url});
        } else {
            return createFromName(pipelineName, instanceId);
        }
    }

    /**
     * Return if the instance is readonly.
     */
    public Boolean getReadonly(String instanceId) throws IOException {
        return (Boolean) getInstanceInfo(instanceId).get("read_only");
    }

    /**
     * Return if the current instance is readonly.
     */
    public Boolean getActive(String instanceId) throws IOException {
        return (Boolean) getInstanceInfo(instanceId).get("is_stream_active");
    }

    /**
     * Return if the current instance id.
     */
    public String getInstanceId(String instanceId) throws IOException {
        return (String) getInstanceInfo(instanceId).get("instance_id");
    }

    /**
     * Return if the current instance stream address
     */
    public String getStreamAddress(String instanceId) throws IOException {
        return (String) getInstanceInfo(instanceId).get("stream_address");
    }

    /**
     * Return the current instance camera.
     */
    public String getCameraName(String instanceId) throws IOException {
        return (String) getInstanceInfo(instanceId).get("camera_name");
    }

    
    /**
     * Scripts
     */
    public List<String> getScripts() throws IOException {
        WebTarget resource = client.target(prefix+ "/script");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (List<String>) map.get("scripts");
    }
    
    public String getScript(String name) throws IOException {
        WebTarget resource = client.target(prefix+ "/script/" + name + "/script_bytes");
        String json = resource.request().accept(MediaType.TEXT_HTML).get(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
        return (String) map.get("script");
    }
    
    public void setScript(String name, String script) throws IOException {
        WebTarget resource = client.target(prefix+ "/script/" + name + "/script_bytes");
        Response r = resource.request().accept(MediaType.TEXT_HTML).put(Entity.text(script));
        String json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);   
    }               

    public void deleteScript(String name) throws IOException {
        checkName(name);
        WebTarget resource = client.target(prefix+ "/script/" + name + "/script_bytes");
        String json = resource.request().accept(MediaType.TEXT_HTML).delete(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);
    }   
    
    public void setScriptFile(String fileName) throws IOException {
        File file = new File(fileName);
        String script = new String(Files.readAllBytes(file.toPath()));
        String name = file.getName();
        setScript(name, script);
    }     
    
    public String getFunction(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "function");
        return ((ret != null) && (ret instanceof String)) ? (String) ret : null;
    }

    public void setFunction(String instanceId, String function) throws IOException {
        Map<String, Object> pars = new HashMap();
        pars.put("function", function);
        pars.put("reload", true);
        setInstanceConfig(instanceId, pars);
    }     
    
    
    public void setFunctionScript(String instanceId, String fileName) throws IOException {
        setScriptFile(fileName);
        setFunction(instanceId, new File(fileName).getName());
    }

}
