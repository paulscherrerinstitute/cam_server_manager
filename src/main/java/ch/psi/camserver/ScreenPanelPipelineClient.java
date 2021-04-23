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
public class ScreenPanelPipelineClient extends PipelineClient{

    public ScreenPanelPipelineClient(String host, int port) {
        super( host, port);
    }

    public ScreenPanelPipelineClient(String url) {
        super(url);
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
}
