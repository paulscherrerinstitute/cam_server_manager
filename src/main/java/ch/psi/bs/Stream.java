package ch.psi.bs;

import java.io.IOException;
import ch.psi.bsread.Receiver;
import ch.psi.bsread.Receiver;
import ch.psi.bsread.message.Message;
import ch.psi.bsread.ReceiverConfig;
import ch.psi.bsread.ReceiverConfig;
import ch.psi.bsread.message.ValueImpl;
import ch.psi.bsread.impl.StandardMessageExtractor;
import ch.psi.bsread.message.ChannelConfig;
import ch.psi.utils.ObservableBase;
import ch.psi.utils.Str;
import ch.psi.utils.Threading;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZMQ;
import zmq.msg.MsgAllocator;

/**
 * A device implementing a beam synchronous string, having, for each identifier,
 * a corresponding Scalar or Waveform child.
 */
public class Stream extends ObservableBase<Stream.StreamListener> implements AutoCloseable {

    private static Logger logger = Logger.getLogger(Threading.class.getName());

    public static final int TIMEOUT_START_STREAMING = 10000;

    public static interface StreamListener {

        void onStart();

        void onStop(Throwable ex);

        void onValue(StreamValue value);
    }

    Thread thread;
    final String address;
    final int socketType;
    volatile boolean reading;
    volatile AtomicBoolean started = new AtomicBoolean(false);
    volatile AtomicBoolean closing = new AtomicBoolean(false);
    Receiver receiver;
    volatile StreamValue cache;

    public String getAddress() {
        return address;
    }

    public int getSocketType() {
        return socketType;
    }

    /**
     * If provider is null then uses default provider.
     */
    public Stream(String address, int socketType) {
        this.address = address;
        this.socketType = socketType;
    }

    public Stream(String address) {
        this(address, ZMQ.SUB);
    }

    boolean debug;

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean value) {
        debug = value;
    }

    void receiverTask() {
        try {
            logger.finer("Entering reveiver task");

            onStart();            
            ReceiverConfig config = getReceiverConfig();
 
            //config.setRequestedChannels(channelsConfig);
            logger.fine("Connecting to: " + config.getAddress() + " (" + config.getSocketType() + ")");
            receiver = new Receiver(config);
            receiver.connect();

            while (!Thread.currentThread().isInterrupted() && started.get()) {
                Message msg = receiver.receive();
                if (msg == null) {
                    logger.info("Received null message");
                    started.set(false);
                } else {
                    Map<String, ValueImpl> data = msg.getValues();
                    if (data != null) {
                        if (checkFilter(data)) {
                            reading = false;
                            long pulseId = msg.getMainHeader().getPulseId();
                            long timestamp = msg.getMainHeader().getGlobalTimestamp().getAsMillis();
                            long nanosOffset = msg.getMainHeader().getGlobalTimestamp().getNs() % 1000000L;
                            Map<String, ChannelConfig> channelConfig = msg.getDataHeader().getChannelsMapping();
                            onMessage(pulseId, timestamp, nanosOffset, data,channelConfig);
                        }
                    }
                }
            }
            if (started.get()) {
                logger.finer("Receiver thread was interrupted");
            } else {
                logger.finer("Receiver was closed");
            }
            onStop(null);
        } catch (Throwable ex) {
            ex.printStackTrace();
            logger.log(Level.FINE, null, ex);
            onStop(ex);
        } finally {
            reading = false;
            closeReceiver();
            logger.fine("Quitting receiver task");
        }
    }
    
    ReceiverConfig getReceiverConfig() {
        MsgAllocator allocator = null;
        ReceiverConfig config = new ReceiverConfig(address, true, false, new StandardMessageExtractor<>(new Converter()), allocator);
        config.setSocketType(getSocketType());
        config.setKeepListeningOnStop(false);
        config.setParallelHandlerProcessing(true);

        ArrayList<ch.psi.bsread.configuration.Channel> channelsConfig = new ArrayList<>();          
        /*
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("image",1,0));
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("timestamp",1,0));
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("width",1,0));
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("height",1,0));
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("x_axis",1,0));
        channelsConfig.add( new ch.psi.bsread.configuration.Channel("y_axis",1,0));            
        */
        //config.set
        return config;
    }
 
    

    public void start() {
        if (started.compareAndSet(false, true)) {
            thread = new Thread(() -> {
                receiverTask();
            });
            thread.setName("Stream receiver: " + hashCode());
            thread.setDaemon(true);
            thread.start();
        }
    }

    public void stop() {
        logger.fine("Stopping");
        started.set(false);
        closeReceiver();
        if (thread != null) {
            try {
                long start = System.currentTimeMillis();
                while ((thread != null) && (thread.isAlive())) {
                    if (System.currentTimeMillis() - start > 1000) {
                        logger.log(Level.WARNING, "Receiver did't quit: interrupting thread");
                        //TODO: Killing thread because it blocks  if no message is received
                        Threading.stop(thread, true, 2000);
                        break;
                    }
                    Thread.sleep(10);
                }
            } catch (InterruptedException ex) {
                //TODO: Filtering InterruptedException. But stop() should not throw InterruptedException;
                logger.log(Level.WARNING, null, ex);
            }
            thread = null;
        }
    }

    public boolean isStarted() {
        return started.get();
    }

    public void assertStarted() throws IOException {
        if (!isStarted()) {
            throw new IOException("Stream not started");
        }
    }

    void closeReceiver() {
        if (closing.compareAndSet(false, true)) {
            try {
                if (receiver != null) {
                    logger.log(Level.FINE, "Closing receiver");
                    try {
                        receiver.close();
                    } catch (Exception ex) {
                        logger.log(Level.WARNING, null, ex);
                    }
                    receiver = null;
                }
            } finally {
                closing.compareAndSet(true, false);
            }
        }
    }

    protected void onStart() {
        for (StreamListener listener : getListeners()) {
            try {
                listener.onStart();
            } catch (Exception ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
    }

    protected void onStop(Throwable e) {
        for (StreamListener listener : getListeners()) {
            try {
                listener.onStop(e);
            } catch (Exception ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
    }

    protected void onValue(StreamValue value) {
        for (StreamListener listener : getListeners()) {
            try {
                listener.onValue(value);
            } catch (Exception ex) {
                logger.log(Level.WARNING, null, ex);
            }
        }
    }

    public StreamValue read() throws IOException, InterruptedException, TimeoutException {
        return read(-1);
    }

    public StreamValue read(int timeout) throws TimeoutException, IOException, InterruptedException {
        assertStarted();
        Object cache = this.cache;
        long start = System.currentTimeMillis();
        while (true) {
            if (cache != this.cache) {
                return this.cache;
            }
            if (timeout > 0) {
                if ((System.currentTimeMillis() - start) > timeout) {
                    throw new TimeoutException();
                }
            }
            Thread.sleep(10);
        }
    }

    final Object cacheLock = new Object();

    void setCache(StreamValue value) {
        if (value != cache) {
            synchronized (cacheLock) {
                cache = value;
                onValue(cache);
            }
        }
    }

    public StreamValue take() {
        return cache;
    }

    String filter;
    ArrayList<FilterCondition> filterConditions = new ArrayList<>();

    enum FilterOp {
        equal,
        notEqual,
        less,
        greater,
        greaterOrEqual,
        lessOrEqual
    }

    class FilterCondition {

        String id;
        FilterOp op;
        Object value;

        FilterCondition(String str) throws IllegalArgumentException {
            try {
                String aux = null;
                if (str.contains("==")) {
                    aux = "==";
                    op = FilterOp.equal;
                } else if (str.contains("!=")) {
                    aux = "!=";
                    op = FilterOp.notEqual;
                } else if (str.contains(">=")) {
                    aux = ">=";
                    op = FilterOp.greaterOrEqual;
                } else if (str.contains("<=")) {
                    aux = "<=";
                    op = FilterOp.lessOrEqual;
                } else if (str.contains(">")) {
                    aux = ">";
                    op = FilterOp.greater;
                } else if (str.contains("<")) {
                    aux = "<";
                    op = FilterOp.less;
                }
                String[] tokens = str.split(aux);
                id = tokens[0].trim();
                aux = tokens[1].trim();
                if ((aux.startsWith("\"") && aux.endsWith("\"")) || (aux.startsWith("'") && aux.endsWith("'"))) {
                    value = aux.substring(1, aux.length() - 1);
                } else if (aux.equalsIgnoreCase("false")) {
                    value = Boolean.FALSE;
                } else if (aux.equalsIgnoreCase("true")) {
                    value = Boolean.TRUE;
                } else {
                    value = Double.valueOf(aux);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(str);
            }
        }

        boolean check(Comparable c) {
            if (c instanceof Number) {
                c = (Double) (((Number) c).doubleValue());
            }
            switch (op) {
                case equal:
                    return c.compareTo(value) == 0;
                case notEqual:
                    return c.compareTo(value) != 0;
                case greater:
                    return c.compareTo(value) > 0;
                case less:
                    return c.compareTo(value) < 0;
                case greaterOrEqual:
                    return c.compareTo(value) >= 0;
                case lessOrEqual:
                    return c.compareTo(value) <= 0;
            }
            return false;
        }
    }

    public final void setFilter(String filter) throws IllegalArgumentException {
        this.filter = null;
        filterConditions.clear();
        if (filter != null) {
            try {
                for (String token : filter.split(" AND ")) {
                    filterConditions.add(new FilterCondition(token));
                }
            } catch (IllegalArgumentException ex) {
                filterConditions.clear();
                throw ex;
            }
            this.filter = filter;
        }
    }

    public String getFilter() {
        return filter;
    }

    public boolean checkFilter(Map<String, ValueImpl> data) {
        if (filter != null) {
            try {
                for (FilterCondition filterCondition : filterConditions) {
                    ValueImpl v = data.get(filterCondition.id);
                    Comparable val = (Comparable) v.getValue();
                    if (!filterCondition.check(val)) {
                        return false;
                    }
                }
            } catch (Exception ex) {
                return false;
            }
        }
        return true;
    }

    LinkedHashMap<String, Object> streamCache = new LinkedHashMap<>();

    protected void onMessage(long pulse_id, long timestamp, long nanosOffset, Map<String, ValueImpl> data, Map<String, ChannelConfig> config) {
        streamCache.clear();
        for (String channel : data.keySet()) {
            ValueImpl v = data.get(channel);
            Object val = v.getValue();
            if (debug) {
                System.out.println(channel + ": " + Str.toString(val, 100));
            }
            streamCache.put(channel, val);
        }
        setCache(new StreamValue(pulse_id, timestamp, nanosOffset, new ArrayList(streamCache.keySet()), new ArrayList(streamCache.values()),config));
    }

    StreamValue getCurrentValue() {
        StreamValue cache = take();
        if (cache == null) {
            throw new RuntimeException("No stream data");
        }
        return cache;
    }

    public List<String> getIdentifiers() {
        return getCurrentValue().getIdentifiers();
    }

    public List getValues() {

        return getCurrentValue().getValues();
    }

    public Object getValue(String id) {
        return getCurrentValue().getValue(id);
    }

    public Object getValue(int index) {
        return getCurrentValue().getValue(index);
    }
    
    public  ChannelConfig  getChannelConfig(String id) {
         return getCurrentValue().getChannelConfig(id);
    }     
    
    public  ChannelConfig  getChannelConfig(int index) {
         return getCurrentValue().getChannelConfig(index);
    }     
    
    public int[]  getShape(String id) {
         return getCurrentValue().getShape(id);
    }     
    
    public int[]  getShape(int index) {
         return getCurrentValue().getShape(index);
    }   
    @Override
    public void close() {
        stop();
    }

}
