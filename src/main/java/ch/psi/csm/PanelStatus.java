package ch.psi.csm;

import ch.psi.camserver.CamServerClient;
import ch.psi.camserver.PipelineClient;
import ch.psi.camserver.ProxyClient;
import ch.psi.utils.NamedThreadFactory;
import ch.psi.utils.Str;
import ch.psi.utils.swing.SwingUtils;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.TextEditor;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
public class PanelStatus extends MonitoredPanel {
    ProxyClient proxy;
    ScheduledExecutorService schedulerPolling;       
    final DefaultTableModel model;
    final DefaultTableModel modelInstances;    
    String currentServer;
    String currentInstance;
    InfoDialog infoDialog;
    boolean isPipeline;
    
    
    public PanelStatus() {
        initComponents();
        model = (DefaultTableModel) table.getModel();
        modelInstances = (DefaultTableModel) tableInstances.getModel();        
        
        
        tableInstances.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    if ((e.getClickCount() == 2) && (!e.isPopupTrigger())) {
                        if (currentInstance != null) {
                            if ((infoDialog==null) || !(infoDialog.isShowing())){
                                infoDialog = new InfoDialog(getFrame(), false);
                                infoDialog.setVisible(true);
                            }                            
                            infoDialog.setInstance(currentInstance);
                            infoDialog.update(instanceInfo);
                        }
                    }
                } catch (Exception ex) {
                    showException(ex);
                }
            }
        });
        
        for (int i=1; i<model.getColumnCount();i++){
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment( JLabel.CENTER );
            table.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
        }
        
        for (int i=1; i<modelInstances.getColumnCount();i++){
            DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
            centerRenderer.setHorizontalAlignment( JLabel.CENTER );
            tableInstances.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
        }        
    }
    
    public void setProxy(ProxyClient proxy){
        this.proxy = proxy;
        textProxy.setText(getUrl());
    }
    
    public ProxyClient getProxy(){
       return proxy;
    }   
            
    public String getUrl(){
       if (proxy==null){
           return null;
       }
       return proxy.getUrl();
    }    
    
   public boolean getPipeline(){
       return isPipeline;
   }

   public void setPipeline(boolean value){
       isPipeline = value;
       buttonConfig.setVisible(value);
   }

   @Override
    protected void onShow() {
        schedulerPolling = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("PanelServers update thread"));
        schedulerPolling.scheduleWithFixedDelay( () -> {
            if (isShowing()){
                update();                           
            }  else {
                Logger.getLogger(getClass().getName()).info("");
            }     
        } , 10, 2000, TimeUnit.MILLISECONDS);
        updateControls();        
    }

    @Override
    protected void onHide() {
        schedulerPolling.shutdownNow();
    }
    
    void update(){
        try {
            Map<String, Object> info = proxy.getInfo();
            Map<String, Map<String, Object>> servers = (Map<String, Map<String, Object>>) info.get("servers");
            Map<String, Map<String, Object>> active_instances = (Map<String, Map<String, Object>>) info.get("active_instances");
            
            SwingUtilities.invokeLater(()->{
                update(servers, active_instances);
            });                 
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, null, ex);
        }
    }    
   
    final LinkedHashMap<String,Map<String, Object>> serverInfo = new LinkedHashMap();
    final Map<String,Map<String, Object>> instanceInfo = new HashMap<>();
    
    void update(Map<String, Map<String, Object>> servers, Map<String, Map<String, Object>> active_instances){
        try {            
            ArrayList<String> keys = new ArrayList<>(servers.keySet());
            Collections.sort(keys);
            serverInfo.clear();
            instanceInfo.clear();
            instanceInfo.putAll(active_instances);
            
            if (model.getRowCount() != keys.size()){
                model.setRowCount(keys.size());
            }
                
            for (int i=0; i<keys.size(); i++){
                String url = keys.get(i);
                Map<String, Object> info = servers.get(url);
                String version = getDisplayValue(info.get("version"));
                String load = getDisplayValue(info.get("load"));
                String cpu = getDisplayValue(info.get("cpu"));
                String memory = getDisplayValue(info.get("memory"));
                String tx = getDisplayValue(info.get("tx")); 
                String rx = getDisplayValue(info.get("rx")); 
                //String instances =  String.valueOf(info.get("instances"));                
                model.setValueAt(url, i, 0);
                model.setValueAt(version, i, 1);
                model.setValueAt(load, i, 2);
                model.setValueAt(cpu, i, 3);
                model.setValueAt(memory, i, 4);
                model.setValueAt(tx, i, 5);
                model.setValueAt(rx, i, 6);                
                serverInfo.put(url, info);
            }   
            updateControls();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static String getDisplayValue( Object obj){
        if (obj==null) {
            return "";
        }        
        if (obj instanceof Number){
            Number n= (Number)obj;        
            Double d = n.doubleValue() ;
            if (d >= 1e12) {
                return String.format("%1.2fT", d / 1e12);
            } else if (d >= 1e9) {
                return String.format("%1.2fG", d / 1e9);
            } else if (d >= 1e6) {
                return String.format("%1.2fM", d / 1e6);
            } else if (d >= 1e3) {
                return String.format("%1.2fK", d / 1e3);
            } else if ((n instanceof Float)||(n instanceof Double)){
                return String.format("%1.2f", d);
            }
        }
        return String.valueOf(obj);
    }
    
    void updateControls(){
        int serverIndex = table.getSelectedRow();
        boolean serverSelected = serverIndex>=0;        
        currentServer = serverSelected ? model.getValueAt(serverIndex, 0).toString() : null;         
        try{            
            
            buttonServerLogs.setEnabled(serverSelected);
            buttonServerRestart.setEnabled(serverSelected);
            buttonServerStop.setEnabled(serverSelected);       
            List<String> instances = new ArrayList<>();
            if (serverSelected){
                try{                    
                    instances = (List) serverInfo.get(currentServer).get("instances");
                } catch (Exception ex){                
                }
            }

            if (modelInstances.getRowCount() != instances.size()){
                modelInstances.setRowCount(instances.size());
            }           
            for (int i =0; i< instances.size(); i++){
                String instanceName = instances.get(i);
                modelInstances.setValueAt(instanceName, i, 0);  
                Map data = instanceInfo.getOrDefault(instanceName, new HashMap());
                Map stats = (Map) data.getOrDefault("statistics", new HashMap());                     
                modelInstances.setValueAt(data.getOrDefault("stream_address", ""), i, 1);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("time", "")), i, 2);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("clients", "")), i, 3);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("throughput", "")), i, 4);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("rx", "")), i, 5);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("tx", "")), i, 6);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("cpu", "")), i, 7);
                modelInstances.setValueAt(getDisplayValue(stats.getOrDefault("memory", "")), i, 8);
            }
        } catch (Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        int instanceIndex = tableInstances.getSelectedRow();
        boolean instanceSelected = instanceIndex>=0;
        currentInstance = instanceSelected ? modelInstances.getValueAt(instanceIndex, 0).toString() : null;            
        try{     
            buttonInstanceStop.setEnabled(instanceSelected);
            buttonRead.setEnabled(instanceSelected);
            buttonConfig.setEnabled(instanceSelected);
        } catch (Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        }     

        if ((infoDialog!=null) &&  infoDialog.isShowing()){
            infoDialog.setInstance(currentInstance);        
            infoDialog.update(instanceInfo);        
        }
    }
    

    static boolean isPush(Map instanceData){
        Map cfg = (Map) instanceData.getOrDefault("config", new HashMap());    
        return cfg.getOrDefault("mode", "").equals("PUSH") || cfg.getOrDefault("pipeline_type", "").equals("store");
    }
    
    
    
    public static void main(String[] args) {
        String server = "http://localhost:8889";
        PanelStatus pn = new PanelStatus();
        pn.setProxy(new ProxyClient(server));
        SwingUtils.showFrame(null, pn.getUrl(), null, pn);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelProxy = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        buttonServerLogs = new javax.swing.JButton();
        buttonServerRestart = new javax.swing.JButton();
        buttonServerStop = new javax.swing.JButton();
        panelServer = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tableInstances = new javax.swing.JTable();
        buttonRead = new javax.swing.JButton();
        buttonConfig = new javax.swing.JButton();
        buttonInstanceStop = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        buttonProxyLogs = new javax.swing.JButton();
        buttonProxyRestart = new javax.swing.JButton();
        textProxy = new javax.swing.JTextField();

        panelProxy.setBorder(javax.swing.BorderFactory.createTitledBorder("Servers"));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Host", "Version", "Load", "CPU", "Memory", "TX", "RX"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableMouseReleased(evt);
            }
        });
        table.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(table);

        buttonServerLogs.setText("Get Logs");
        buttonServerLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonServerLogsActionPerformed(evt);
            }
        });

        buttonServerRestart.setText("Restart");
        buttonServerRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonServerRestartActionPerformed(evt);
            }
        });

        buttonServerStop.setText("Stop All ");
        buttonServerStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonServerStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelProxyLayout = new javax.swing.GroupLayout(panelProxy);
        panelProxy.setLayout(panelProxyLayout);
        panelProxyLayout.setHorizontalGroup(
            panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProxyLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(buttonServerLogs, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(buttonServerStop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(buttonServerRestart, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelProxyLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonServerLogs, buttonServerRestart, buttonServerStop});

        panelProxyLayout.setVerticalGroup(
            panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelProxyLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelProxyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelProxyLayout.createSequentialGroup()
                        .addComponent(buttonServerLogs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonServerRestart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonServerStop)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        panelServer.setBorder(javax.swing.BorderFactory.createTitledBorder("Instances"));

        tableInstances.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Instance", "Stream", "Time", "Clients", "Bps", "RX", "TX", "CPU", "Mem"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableInstances.getTableHeader().setReorderingAllowed(false);
        tableInstances.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableInstancesMouseReleased(evt);
            }
        });
        tableInstances.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableInstancesKeyReleased(evt);
            }
        });
        jScrollPane3.setViewportView(tableInstances);

        buttonRead.setText("Inspect");
        buttonRead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonReadActionPerformed(evt);
            }
        });

        buttonConfig.setText("Config");
        buttonConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfigActionPerformed(evt);
            }
        });

        buttonInstanceStop.setText("Stop");
        buttonInstanceStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonInstanceStopActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelServerLayout = new javax.swing.GroupLayout(panelServer);
        panelServer.setLayout(panelServerLayout);
        panelServerLayout.setHorizontalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 474, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonRead, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonInstanceStop, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        panelServerLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonConfig, buttonInstanceStop, buttonRead});

        panelServerLayout.setVerticalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelServerLayout.createSequentialGroup()
                        .addComponent(buttonRead)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonInstanceStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonConfig)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Proxy"));

        buttonProxyLogs.setText("Get Logs");
        buttonProxyLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonProxyLogsActionPerformed(evt);
            }
        });

        buttonProxyRestart.setText("Restart");
        buttonProxyRestart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonProxyRestartActionPerformed(evt);
            }
        });

        textProxy.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(textProxy)
                .addGap(18, 18, 18)
                .addComponent(buttonProxyLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonProxyRestart, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonProxyLogs, buttonProxyRestart});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonProxyLogs)
                    .addComponent(buttonProxyRestart)
                    .addComponent(textProxy, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelProxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, 0))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelProxy, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelServer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseReleased
        updateControls();
    }//GEN-LAST:event_tableMouseReleased

    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased
        updateControls();
    }//GEN-LAST:event_tableKeyReleased

    private void buttonServerLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonServerLogsActionPerformed
        try{
            if (currentServer==null){
                throw new Exception("No server selected");
            }            
            schedulerPolling.submit(()->{
                try{
                    CamServerClient client = new CamServerClient(currentServer, "");
                    String logs = client.getLogs();
                    TextEditor editor = new TextEditor();
                    editor.setText(logs);
                    editor.setEditorFont(editor.getEditorFont().deriveFont(10.0f));
                    editor.setContentWidth(3000);
                    editor.setReadOnly(true);
                    SwingUtils.showDialog(PanelStatus.this, "Servers Logs - " + currentServer, new Dimension(800,600), editor);
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonServerLogsActionPerformed

    private void buttonServerStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonServerStopActionPerformed
        try{
            if (currentServer==null){
                throw new Exception("No server selected");
            }            
            schedulerPolling.submit(()->{
                try{
                    //InstanceManagerClient client = new InstanceManagerClient(currentServer, PipelineClient.prefix);
                    //client.stopAllInstances();
                    proxy.stopAllInstances(currentServer);
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonServerStopActionPerformed

    private void buttonServerRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonServerRestartActionPerformed
        try{
            if (currentServer==null){
                throw new Exception("No server selected");
            }            
            schedulerPolling.submit(()->{
                try{
                    CamServerClient client = new CamServerClient(currentServer,"");
                    client.reset();
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonServerRestartActionPerformed

    private void buttonInstanceStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonInstanceStopActionPerformed
        try{
            if (currentServer==null){
                throw new Exception("No server selected");
            }
            if (currentInstance==null){
                throw new Exception("No insatance selected");
            }
                        
            schedulerPolling.submit(()->{
                try{
                    //InstanceManagerClient client = new InstanceManagerClient(currentServer, PipelineClient.prefix);
                    //client.stopInstance(currentInstance);
                    proxy.stopInstance(currentInstance);
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonInstanceStopActionPerformed

    private void tableInstancesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableInstancesKeyReleased
        updateControls();
    }//GEN-LAST:event_tableInstancesKeyReleased

    private void tableInstancesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableInstancesMouseReleased
        updateControls();
    }//GEN-LAST:event_tableInstancesMouseReleased

    private void buttonReadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonReadActionPerformed
        try{
            Map instanceData = instanceInfo.get(currentInstance);
            String address = (String)instanceData.get("stream_address");
            StreamDialog dlg = new StreamDialog(SwingUtils.getFrame(this), true, address, isPush(instanceData));
            dlg.setVisible(true);
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonReadActionPerformed

    private void buttonConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigActionPerformed
        try{            
            Map instanceData = instanceInfo.get(currentInstance);
            Map cfg = (Map) instanceData.getOrDefault("config", new HashMap());    
            String json = JsonSerializer.encode(cfg, true);
            ConfigDialog dlg = new ConfigDialog(SwingUtils.getFrame(this), true, currentInstance,json);
            dlg.setVisible(true);
            if (dlg.getResult()){
                json = dlg.ret;
                cfg = (Map) JsonSerializer.decode(json, Map.class);
                PipelineClient client = new PipelineClient(currentServer);
                client.setInstanceConfig(currentInstance, cfg);
            }        
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }        
    }//GEN-LAST:event_buttonConfigActionPerformed

    private void buttonProxyLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonProxyLogsActionPerformed
        try{
            schedulerPolling.submit(()->{
                try{
                    String logs = proxy.getLogs();
                    TextEditor editor = new TextEditor();
                    editor.setText(logs);
                    editor.setEditorFont(editor.getEditorFont().deriveFont(10.0f));
                    editor.setContentWidth(3000);
                    editor.setReadOnly(true);
                    SwingUtils.showDialog(PanelStatus.this, "Proxy Logs ", new Dimension(800,600), editor);
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonProxyLogsActionPerformed

    private void buttonProxyRestartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonProxyRestartActionPerformed
        try{
            schedulerPolling.submit(()->{
                try{
                    proxy.reset();
                } catch (Exception ex){
                    SwingUtils.showException(this, ex);
                }                    
            });           
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonProxyRestartActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonConfig;
    private javax.swing.JButton buttonInstanceStop;
    private javax.swing.JButton buttonProxyLogs;
    private javax.swing.JButton buttonProxyRestart;
    private javax.swing.JButton buttonRead;
    private javax.swing.JButton buttonServerLogs;
    private javax.swing.JButton buttonServerRestart;
    private javax.swing.JButton buttonServerStop;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JPanel panelProxy;
    private javax.swing.JPanel panelServer;
    private javax.swing.JTable table;
    private javax.swing.JTable tableInstances;
    private javax.swing.JTextField textProxy;
    // End of variables declaration//GEN-END:variables
}
