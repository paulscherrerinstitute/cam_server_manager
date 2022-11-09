package ch.psi.bs;

import java.util.ArrayList;
import java.util.List;
import ch.psi.bsread.message.ChannelConfig;
import ch.psi.bsread.message.Type;

/**
 * Entity containing the current value for a stream, including a list of identifiers, their values,
 * a pulse id and a timestamp.
 */
public class StreamValue extends Number {

    final long pulseId;
    final long timestamp;
    final long nanosOffset;
    final List<String> identifiers;
    final List values;
    final java.util.Map<String, ChannelConfig> config;

    StreamValue(long pulseId, long timestamp, List<String> identifiers, List values, java.util.Map<String, ChannelConfig>config) {
        this(pulseId, timestamp, 0, identifiers, values, config);
    }

    StreamValue(long pulseId, long timestamp, long nanosOffset, List<String> identifiers, List values, java.util.Map<String, ChannelConfig>config) {
        this.pulseId = pulseId;
        this.values = values;
        this.timestamp = timestamp;
        this.nanosOffset = nanosOffset;
        this.identifiers = identifiers;
        this.config = config;
    }

    public long getPulseId() {
        return pulseId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getNanosOffset() {
        return nanosOffset;
    }

    public long getTimestampNanos() {
        return (timestamp * 1000000) + nanosOffset;
    }
    
    public  java.util.Map<String, ChannelConfig> getConfig() {
        return config;
    }        

    //Backward compatibility
    public List<String> getIdentifiers() {
        return getKeys();
    }

    public java.util.List<String> getKeys(){
        return new ArrayList<>(identifiers);
    }  
    
    public List getValues() {
        return new ArrayList<>(values);
    }

    public Object getValue(String id) {
        int index = toItemIndex(id);
        if (index < 0) {
            return null;
        }
        return getValue(index);
    }

    public Object getValue(int index) {
        return getValues().get(index);
    }

    public  ChannelConfig  getChannelConfig(String id) {
        return (config==null) ? null : config.get(id);
    }     
    
    public  ChannelConfig  getChannelConfig(int index) {
        return ((index<0) || (index>=identifiers.size())) ? null : config.get(identifiers.get(index));
    }     
    
    public int[]  getShape(String id) {
        ChannelConfig  config = getChannelConfig(id);
        return (config==null) ? null : config.getShape();
    }     
    
    public int[]  getShape(int index) {
        ChannelConfig  config = getChannelConfig(index);
        return (config==null) ? null : config.getShape();
    }        
    
    public Type getType(String id) {
        ChannelConfig  config = getChannelConfig(id);
        return (config==null) ? null : config.getType();
    }     
     
    public Type getType(int index) {
        ChannelConfig  config = getChannelConfig(index);
        return (config==null) ? null : config.getType();
    }        
    @Override
    public String toString() {
        return String.valueOf(pulseId);
    }

    @Override
    public int intValue() {
        return (int) pulseId;
    }

    @Override
    public long longValue() {
        return pulseId;
    }

    @Override
    public float floatValue() {
        return pulseId;
    }

    @Override
    public double doubleValue() {
        return pulseId;
    }

    int toItemIndex(String itemKey){
        for (int i = 0; i < identifiers.size(); i++) {
            if (identifiers.get(i).equals(itemKey)) {
                return i;
            }
        }
        return -1;
    }      
  
}
