package ch.psi.camserver;

import ch.psi.csm.JsonSerializer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Imaging Source implementation connecting to a CameraServer.
 */
public class PipelineClient extends InstanceManagerClient{
    
    final public static String prefix = "pipeline";

    public PipelineClient(String host, int port) {
        super( host, port, prefix);
    }

    public PipelineClient(String url) {
        super(url, prefix);
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

    
    public boolean isRoiEnabled(String instanceId) throws IOException {
        Object roi = getInstanceConfigValue(instanceId, "image_region_of_interest");
        return (roi != null) && (roi instanceof List) && (((List) roi).size() == 4);
    }

    public void resetRoi(String instanceId) throws IOException {
        setInstanceConfigValue(instanceId, "image_region_of_interest", null);
    }

    public void setRoi(String instanceId, int x, int y, int width, int height) throws IOException {
        setInstanceConfigValue(instanceId, "image_region_of_interest", new int[]{x, width, y, height});
    }

    public void setRoi(String instanceId, int[] roi) throws IOException {
        setRoi(instanceId, roi[0], roi[1], roi[2], roi[3]);
    }

    public int[] getRoi(String instanceId) throws IOException {
        Object roi = getInstanceConfigValue(instanceId, "image_region_of_interest");
        if ((roi != null) && (roi instanceof List)) {
            List<Integer> l = (List<Integer>) roi;
            if (l.size() >= 4) {
                return new int[]{l.get(0), l.get(2), l.get(1), l.get(3)};
            }
        }
        return null;
    }

    public void setBinning(String instanceId, int binning_x, int binning_y) throws IOException {
        setBinning(instanceId, binning_x, binning_y, false);
    }

    public void setBinning(String instanceId, int binning_x, int binning_y, boolean mean) throws IOException {
        Map<String, Object> pars = new HashMap();
        pars.put("binning_x", binning_x);
        pars.put("binning_y", binning_y);
        pars.put("binning_mean", mean);
        setInstanceConfig(instanceId, pars);
    }

    public Map getBinning(String instanceId) throws IOException {
        Map ret = new HashMap();
        Map<String, Object> pars = getInstanceConfig(instanceId);
        ret.put("binning_x", pars.getOrDefault("binning_x", 1));
        ret.put("binning_y", pars.getOrDefault("binning_y", 1));
        ret.put("binning_mean", pars.getOrDefault("binning_mean", false));
        return ret;
    }

    public String getBackground(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "image_background");
        if (ret instanceof String){
            return (String) ret;
        }
        return null;
    }

    public void setBackground(String instanceId, String id) throws IOException {
        setInstanceConfigValue(instanceId, "image_background", id);
    }

    private boolean isBackgroundSubtractionEnabled(Object value) throws IOException {
        return !value.equals(false) && !value.equals("false") && !value.equals("");
    }
    
    public boolean isBackgroundSubtractionEnabled(String instanceId) throws IOException {
        Object value = getBackgroundSubtraction(instanceId);
        return isBackgroundSubtractionEnabled(value);
    }
    
    public Object getBackgroundSubtraction(String instanceId) throws IOException {
        return getInstanceConfigValue(instanceId, "image_background_enable");
    }

    public void setBackgroundSubtraction(String instanceId, Object value) throws IOException {
        if (isBackgroundSubtractionEnabled(value)){
            String id = getBackground(instanceId);
            if (id == null) {
                setBackground(instanceId, getLastBackground(instanceId));
            }
        }
        setInstanceConfigValue(instanceId, "image_background_enable", value);
    }    

    public Double getThreshold(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "image_threshold");
        return ((ret != null) && (ret instanceof Number)) ? ((Number) ret).doubleValue() : null;
    }

    public void setThreshold(String instanceId, Double value) throws IOException {
        setInstanceConfigValue(instanceId, "image_threshold", value);
    }

    public Map<String, Object> getGoodRegion(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "image_good_region");
        return ((ret != null) && (ret instanceof Map)) ? (Map) ret : null;
    }

    public void setGoodRegion(String instanceId, Map<String, Object> value) throws IOException {
        setInstanceConfigValue(instanceId, "image_good_region", value);
    }

    public void setGoodRegion(String instanceId, double threshold, double scale) throws IOException {
        Map<String, Object> gr = new HashMap<>();
        gr.put("threshold", threshold);
        gr.put("gfscale", scale);
        setGoodRegion(instanceId, gr);
    }

    public Map<String, Object> getSlicing(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "image_slices");
        return ((ret != null) && (ret instanceof Map)) ? (Map) ret : null;
    }

    public void setSlicing(String instanceId, Map<String, Object> value) throws IOException {
        setInstanceConfigValue(instanceId, "image_slices", value);
    }

    public void setSlicing(String instanceId, int slices, double scale, String orientation) throws IOException {
        Map<String, Object> gr = new HashMap<>();
        gr.put("number_of_slices", slices);
        gr.put("scale", scale);
        gr.put("orientation", orientation);
        setSlicing(instanceId, gr);
    }
        
    public Map<String, Object> getRotation(String instanceId) throws IOException {
        Object ret = getInstanceConfigValue(instanceId, "rotation");
        return ((ret != null) && (ret instanceof Map)) ? (Map) ret : null;
    }

    public void setRotation(String instanceId, Map<String, Object> value) throws IOException {
        setInstanceConfigValue(instanceId, "rotation", value);    
    }
    
     public void setRotation(String instanceId, double angle, int order, String mode) throws IOException {
        Map<String, Object> gr = new HashMap<>();
        gr.put("angle", angle);
        gr.put("order", order);
        gr.put("mode", mode);
        setRotation(instanceId, gr);
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
    
    
    public void sendFunctionScript(String instanceId, String fileName) throws IOException {
        File file = new File(fileName);
        String function = new String(Files.readAllBytes(file.toPath()));
        String name = file.getName();
        
        WebTarget resource = client.target(prefix + "/script/" + name + "/script_bytes");
        Response r = resource.request().accept(MediaType.TEXT_HTML).put(Entity.text(function));
        String json = r.readEntity(String.class);
        Map<String, Object> map = (Map) JsonSerializer.decode(json, Map.class);
        checkReturn(map);                       
    }     
    
    public void setFunctionScript(String instanceId, String fileName) throws IOException {
        sendFunctionScript(instanceId, fileName);
        setFunction(instanceId, new File(fileName).getName());
    }

}
