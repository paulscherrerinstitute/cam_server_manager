package ch.psi.csm;

import ch.psi.camserver.PipelineClient;
import ch.psi.camserver.ProxyClient;
import ch.psi.utils.Str;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import static org.zeromq.ZMQ.context;

/**
 *
 */
public class PanelConfig extends MonitoredPanel {

    ProxyClient proxy;
    Map<String, Object> serverCfg = null;
    List<String> instanceCfgNames = new ArrayList<>();
    List<String> scriptsNames = new ArrayList<>();
    Map<String, String> permanentInstances = new HashMap<>();
    String currentConfig = "" ;
    String currentServer = "" ;
    boolean isPipeline;
    
    final DefaultTableModel modelConfigs;
    final DefaultTableModel modelServers;
    final DefaultTableModel modelFixed;
    final DefaultTableModel modelPermanent;
    final DefaultTableModel modelScripts;
    
    boolean modelPermanentChanged;
    
    final int SMALL_COL_SZIE = 80;
    
    
   public boolean getPipeline(){
       return isPipeline;
   }

   public void setPipeline(boolean value){
       isPipeline = value;
       panelScripts.setVisible(value);
       splitRight.setDividerSize(value ? splitLeft.getDividerSize(): 0);
   }    
    
    public PanelConfig() {
        initComponents();
        
        buttonConfigEdit.setEnabled(false);
        
        modelConfigs = (DefaultTableModel)  tableConfigurations.getModel();
        modelServers = (DefaultTableModel)  tableServers.getModel();
        modelFixed = (DefaultTableModel)  tableFixedInstances.getModel();
        modelPermanent = (DefaultTableModel)  tablePermanentInstances.getModel();
        modelScripts = (DefaultTableModel)  tableUserScripts.getModel();
        
        tableServers.getColumnModel().getColumn(0).setPreferredWidth(SMALL_COL_SZIE);
        tableServers.getColumnModel().getColumn(0).setMaxWidth(SMALL_COL_SZIE);
        tableServers.getColumnModel().getColumn(0).setResizable(false);
        tableServers.getColumnModel().getColumn(2).setPreferredWidth(SMALL_COL_SZIE);
        tableServers.getColumnModel().getColumn(2).setMaxWidth(SMALL_COL_SZIE);
        tableServers.getColumnModel().getColumn(2).setResizable(false);
        tableFixedInstances.getColumnModel().getColumn(0).setPreferredWidth(SMALL_COL_SZIE);
        tableFixedInstances.getColumnModel().getColumn(0).setMaxWidth(SMALL_COL_SZIE);
        tableFixedInstances.getColumnModel().getColumn(0).setResizable(false);
        tableFixedInstances.getColumnModel().getColumn(2).setPreferredWidth(SMALL_COL_SZIE);
        tableFixedInstances.getColumnModel().getColumn(2).setMaxWidth(SMALL_COL_SZIE);
        tableFixedInstances.getColumnModel().getColumn(2).setResizable(false);                
        
        modelPermanent.addTableModelListener((TableModelEvent e) -> {
            modelPermanentChanged=true;
            updateButtons();
        });
        
        tableConfigurations.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (!e.isPopupTrigger())) {
                    buttonConfigEditActionPerformed(null);
                }
            }
        });
        
        tableUserScripts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (!e.isPopupTrigger())) {
                    buttonScriptEditActionPerformed(null);
                }
            }
        });
                
        tableConfigurations.setDragEnabled(true);
        tableConfigurations.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return DnDConstants.ACTION_COPY_OR_MOVE;
            }
            @Override
            public Transferable createTransferable(JComponent comp) {
                onTableInstancesSelection();
                return (currentConfig != null) ? new StringSelection(currentConfig) : null;
            }
        });
        
        
        tablePermanentInstances.setDropMode(DropMode.INSERT_ROWS);
        tablePermanentInstances.setFillsViewportHeight(true);
        tablePermanentInstances.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return DnDConstants.ACTION_COPY_OR_MOVE;
            }

            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                return info.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if ((!support.isDrop()) ||  (!canImport(support))) {
                    return false;
                }
                try {
                    JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();                    
                    String name = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);         
                    modelPermanent.insertRow(dl.getRow(), new Object[]{name,name});
                } catch (Exception ex) {
                    return false;
                }                
                return true;
            }

        });

        tableFixedInstances.setDropMode(DropMode.INSERT_ROWS);
        tableFixedInstances.setFillsViewportHeight(true);
        tableFixedInstances.setTransferHandler(new TransferHandler() {
            @Override
            public int getSourceActions(JComponent c) {
                return DnDConstants.ACTION_COPY_OR_MOVE;
            }

            @Override
            public boolean canImport(TransferHandler.TransferSupport info) {
                return info.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferHandler.TransferSupport support) {
                if ((!support.isDrop()) ||  (!canImport(support))) {
                    return false;
                }
                try {
                    JTable.DropLocation dl = (JTable.DropLocation)support.getDropLocation();                    
                    String name = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);         
                    modelFixed.insertRow(dl.getRow(), new Object[]{true,name,""});
                } catch (Exception ex) {
                    return false;
                }                
                return true;
            }

        });
        
        updateButtons();
    }
    
    
    void updateButtons(){
        if (!SwingUtilities.isEventDispatchThread()){
            SwingUtilities.invokeLater(()->{updateButtons();});
            return;
        }
        
        buttonScriptEdit.setEnabled(tableUserScripts.getSelectedRow()>=0);      
        buttonScriptDel.setEnabled(tableUserScripts.getSelectedRow()>=0);      
        buttonConfigDel.setEnabled(tableConfigurations.getSelectedRow()>=0);
        buttonConfigEdit.setEnabled(tableConfigurations.getSelectedRow()>=0);
        buttonFixedDel.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonFixedApply.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonFixedUndo.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonPermDelete.setEnabled(tablePermanentInstances.getSelectedRow()>=0);
        buttonPermApply.setEnabled(modelPermanentChanged && (tablePermanentInstances.getSelectedRow()>=0));
        buttonPermUndo.setEnabled(modelPermanentChanged && (tablePermanentInstances.getSelectedRow()>=0));
        
    }
    
    
    Thread updateConfigs(){
        Thread t = new Thread(()->{
            try {
                instanceCfgNames =proxy.getConfigNames();
                Collections.sort(instanceCfgNames); //, String.CASE_INSENSITIVE_ORDER);
                modelConfigs.setRowCount(0);
                for (String instance : instanceCfgNames){
                    modelConfigs.addRow(new Object[]{instance});
                }                
                updateButtons();
            } catch (IOException ex) {
                Logger.getLogger(PanelConfig.class.getName()).log(Level.WARNING, null, ex);
            }             
        });
        t.start();
        return t;
    }
    
    void updateServers(){
        new Thread(()->{
            try {
                serverCfg =proxy.getConfig();
                modelServers.setRowCount(0);
                for (String server : serverCfg.keySet()){
                    Map serverCfg = (Map) this.serverCfg.get(server);
                    boolean expanding = (Boolean) serverCfg.getOrDefault("expanding", true);
                    boolean enabled = (Boolean) serverCfg.getOrDefault("enabled", true);
                    modelServers.addRow(new Object[]{enabled,server,expanding});
                }                
                updateButtons();
            } catch (IOException ex) {
                Logger.getLogger(PanelConfig.class.getName()).log(Level.WARNING, null, ex);
            }             
        }).start();        
    }
    
    void updatePermanent(){
        new Thread(()->{
            try {
                permanentInstances = proxy.getPemanentInstances();
                modelPermanent.setRowCount(0);
                for (String config : permanentInstances.keySet()){
                    modelPermanent.addRow(new Object[]{config, Str.toString(permanentInstances.get(config))});
                }    
                modelPermanentChanged=false;
                updateButtons();
            } catch (IOException ex) {
                Logger.getLogger(PanelConfig.class.getName()).log(Level.WARNING, null, ex);
            }             
        }).start();
    }    
    
    Thread updateScripts(){
        Thread t = new Thread(()->{
            try {
                PipelineClient client = new PipelineClient(getUrl());                
                scriptsNames = client.getScripts();
                Collections.sort(scriptsNames); //, String.CASE_INSENSITIVE_ORDER);
                modelScripts.setRowCount(0);
                for (String script : scriptsNames){
                    modelScripts.addRow(new Object[]{script});
                }    
                updateButtons();
            } catch (IOException ex) {
                Logger.getLogger(PanelConfig.class.getName()).log(Level.WARNING, null, ex);
            }             
        });
        t.start();
        return t;        
    }        
    
    @Override
    protected void onShow() {      
        updateButtons();
        updateServers();
        updateConfigs();
        updatePermanent();
        updateScripts();
    }
    
        
    public void setProxy(ProxyClient proxy){
        this.proxy = proxy;        
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
          
    
    void onTableInstancesSelection(){
        int row=tableConfigurations.getSelectedRow();
        if (row<0){
            currentConfig = "";
        } else {
            currentConfig = String.valueOf(tableConfigurations.getValueAt(row, 0));
        }
        updateButtons();
    }
    
    void onTableServersSelection(){
        int row=tableServers.getSelectedRow();
        if (row<0){
            currentServer = "";
            modelFixed.setNumRows(0);
        } else {
            currentServer = String.valueOf(tableServers.getValueAt(row, 0));
            
        }
        updateButtons();
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitLeft = new javax.swing.JSplitPane();
        psnelServers = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableServers = new javax.swing.JTable();
        buttonFixedUndo = new javax.swing.JButton();
        buttonFixedApply = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableFixedInstances = new javax.swing.JTable();
        buttonFixedDel = new javax.swing.JButton();
        panelInstances = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablePermanentInstances = new javax.swing.JTable();
        buttonPermUndo = new javax.swing.JButton();
        buttonPermApply = new javax.swing.JButton();
        buttonPermDelete = new javax.swing.JButton();
        splitRight = new javax.swing.JSplitPane();
        panelConfigurations = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableConfigurations = new javax.swing.JTable();
        buttonConfigEdit = new javax.swing.JButton();
        buttonConfigNew = new javax.swing.JButton();
        buttonConfigDel = new javax.swing.JButton();
        panelScripts = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableUserScripts = new javax.swing.JTable();
        buttonScriptNew = new javax.swing.JButton();
        buttonScriptDel = new javax.swing.JButton();
        buttonScriptEdit = new javax.swing.JButton();

        splitLeft.setDividerLocation(320);
        splitLeft.setDividerSize(3);
        splitLeft.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitLeft.setResizeWeight(0.5);

        psnelServers.setBorder(javax.swing.BorderFactory.createTitledBorder("Servers"));

        tableServers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Enabed", "Url", "Expanding"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableServers.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableServers.getTableHeader().setReorderingAllowed(false);
        tableServers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableServersMouseReleased(evt);
            }
        });
        tableServers.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableServersKeyReleased(evt);
            }
        });
        jScrollPane4.setViewportView(tableServers);

        buttonFixedUndo.setText("Undo");

        buttonFixedApply.setText("Apply");

        tableFixedInstances.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Enabed", "Instance", "Port"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableFixedInstances.setDropMode(javax.swing.DropMode.INSERT_ROWS);
        tableFixedInstances.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableFixedInstances.getTableHeader().setReorderingAllowed(false);
        tableFixedInstances.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableFixedInstancesMouseReleased(evt);
            }
        });
        tableFixedInstances.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableFixedInstancesKeyReleased(evt);
            }
        });
        jScrollPane6.setViewportView(tableFixedInstances);

        buttonFixedDel.setText("Delete");
        buttonFixedDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonFixedDelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout psnelServersLayout = new javax.swing.GroupLayout(psnelServers);
        psnelServers.setLayout(psnelServersLayout);
        psnelServersLayout.setHorizontalGroup(
            psnelServersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psnelServersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(psnelServersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, psnelServersLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonFixedDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonFixedUndo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonFixedApply)
                        .addGap(0, 11, Short.MAX_VALUE)))
                .addContainerGap())
        );
        psnelServersLayout.setVerticalGroup(
            psnelServersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(psnelServersLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(psnelServersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonFixedUndo)
                    .addComponent(buttonFixedApply)
                    .addComponent(buttonFixedDel))
                .addContainerGap())
        );

        splitLeft.setLeftComponent(psnelServers);

        panelInstances.setBorder(javax.swing.BorderFactory.createTitledBorder("Permanent Instances"));

        tablePermanentInstances.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Instance", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tablePermanentInstances.setDropMode(javax.swing.DropMode.INSERT_ROWS);
        tablePermanentInstances.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tablePermanentInstances.getTableHeader().setReorderingAllowed(false);
        tablePermanentInstances.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tablePermanentInstancesMouseReleased(evt);
            }
        });
        tablePermanentInstances.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tablePermanentInstancesKeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(tablePermanentInstances);

        buttonPermUndo.setText("Undo");
        buttonPermUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPermUndoActionPerformed(evt);
            }
        });

        buttonPermApply.setText("Apply");
        buttonPermApply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPermApplyActionPerformed(evt);
            }
        });

        buttonPermDelete.setText("Delete");
        buttonPermDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonPermDeleteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelInstancesLayout = new javax.swing.GroupLayout(panelInstances);
        panelInstances.setLayout(panelInstancesLayout);
        panelInstancesLayout.setHorizontalGroup(
            panelInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInstancesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelInstancesLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonPermDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPermUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPermApply)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        panelInstancesLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonPermApply, buttonPermDelete, buttonPermUndo});

        panelInstancesLayout.setVerticalGroup(
            panelInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelInstancesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelInstancesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonPermApply)
                    .addComponent(buttonPermDelete)
                    .addComponent(buttonPermUndo))
                .addContainerGap())
        );

        splitLeft.setRightComponent(panelInstances);

        splitRight.setDividerLocation(320);
        splitRight.setDividerSize(3);
        splitRight.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitRight.setResizeWeight(0.5);

        panelConfigurations.setBorder(javax.swing.BorderFactory.createTitledBorder("Configurations"));

        tableConfigurations.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableConfigurations.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableConfigurations.getTableHeader().setReorderingAllowed(false);
        tableConfigurations.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableConfigurationsMouseReleased(evt);
            }
        });
        tableConfigurations.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableConfigurationsKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(tableConfigurations);

        buttonConfigEdit.setText("Edit");
        buttonConfigEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfigEditActionPerformed(evt);
            }
        });

        buttonConfigNew.setText("New");
        buttonConfigNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfigNewActionPerformed(evt);
            }
        });

        buttonConfigDel.setText("Delete");
        buttonConfigDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonConfigDelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelConfigurationsLayout = new javax.swing.GroupLayout(panelConfigurations);
        panelConfigurations.setLayout(panelConfigurationsLayout);
        panelConfigurationsLayout.setHorizontalGroup(
            panelConfigurationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelConfigurationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelConfigurationsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 5, Short.MAX_VALUE)
                        .addComponent(buttonConfigNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonConfigDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonConfigEdit)
                        .addContainerGap(13, Short.MAX_VALUE))
                    .addGroup(panelConfigurationsLayout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        panelConfigurationsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonConfigDel, buttonConfigEdit, buttonConfigNew});

        panelConfigurationsLayout.setVerticalGroup(
            panelConfigurationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelConfigurationsLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelConfigurationsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonConfigEdit)
                    .addComponent(buttonConfigNew)
                    .addComponent(buttonConfigDel))
                .addContainerGap())
        );

        splitRight.setLeftComponent(panelConfigurations);

        panelScripts.setBorder(javax.swing.BorderFactory.createTitledBorder("User Scripts"));

        tableUserScripts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tableUserScripts.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableUserScripts.getTableHeader().setReorderingAllowed(false);
        tableUserScripts.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tableUserScriptsMouseReleased(evt);
            }
        });
        tableUserScripts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tableUserScriptsKeyReleased(evt);
            }
        });
        jScrollPane5.setViewportView(tableUserScripts);

        buttonScriptNew.setText("New");
        buttonScriptNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonScriptNewActionPerformed(evt);
            }
        });

        buttonScriptDel.setText("Delete");
        buttonScriptDel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonScriptDelActionPerformed(evt);
            }
        });

        buttonScriptEdit.setText("Edit");
        buttonScriptEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonScriptEditActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelScriptsLayout = new javax.swing.GroupLayout(panelScripts);
        panelScripts.setLayout(panelScriptsLayout);
        panelScriptsLayout.setHorizontalGroup(
            panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScriptsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(panelScriptsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonScriptNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptEdit)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        panelScriptsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonScriptDel, buttonScriptEdit, buttonScriptNew});

        panelScriptsLayout.setVerticalGroup(
            panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScriptsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 104, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonScriptEdit)
                    .addComponent(buttonScriptNew)
                    .addComponent(buttonScriptDel))
                .addContainerGap())
        );

        splitRight.setRightComponent(panelScripts);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitLeft)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitRight))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitLeft)
            .addComponent(splitRight, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableConfigurationsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableConfigurationsKeyReleased
        try{
            onTableInstancesSelection();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_tableConfigurationsKeyReleased

    private void tableConfigurationsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableConfigurationsMouseReleased
        try{
            onTableInstancesSelection();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_tableConfigurationsMouseReleased

    private void buttonConfigEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigEditActionPerformed
        try{
            if (currentConfig!=null){
                String config = proxy.getNamedConfig(currentConfig);
                ScriptEditor dlg = new ScriptEditor(SwingUtils.getFrame(this), true, currentConfig, config, "json");
                dlg.setVisible(true);
                if (dlg.getResult()){
                    proxy.setNamedConfig(currentConfig, dlg.ret);
                }    
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }        
    }//GEN-LAST:event_buttonConfigEditActionPerformed

    private void buttonConfigNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigNewActionPerformed
        try{
            String name = SwingUtils.getString(this, "Enter configuration name: ", "");
            if (name !=null){
                if (instanceCfgNames.contains(name)){
                    throw new Exception("Configuration name is already used: " + name);
                }
                proxy.setNamedConfig(name, "{}");
                updateConfigs().join();
                if (!instanceCfgNames.contains(name)){
                    throw new Exception("Error adding configuration: " + name);
                }
                int index = instanceCfgNames.indexOf(name);
                tableConfigurations.setRowSelectionInterval(index, index);
                SwingUtils.scrollToVisible(tableConfigurations, index, 0);
                currentConfig = name;
                buttonConfigEditActionPerformed(null);                
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex); 
        }   
    }//GEN-LAST:event_buttonConfigNewActionPerformed

    private void buttonConfigDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigDelActionPerformed
        try{
            if (currentConfig!=null){
                Object[] options = new Object[]{"No", "Yes"};
                if (SwingUtils.showOption(this, "Delete Configuration", "Are you sure to delete the configuration: " + currentConfig  + "?", options, options[0]) == 1){
                    proxy.deleteNamedConfig(currentConfig);
                    updateConfigs();
                }
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        } 
    }//GEN-LAST:event_buttonConfigDelActionPerformed

    private void buttonScriptNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptNewActionPerformed
        try{
            String name = SwingUtils.getString(this, "Enter script name: ", "");
            if (name !=null){
                
                if (!name.endsWith(".py")) {
                    name = name + ".py";
                }

                if (scriptsNames.contains(name)){
                    throw new Exception("Script name is already used: " + name);
                }
                PipelineClient client = new PipelineClient(getUrl());  
                String script = "from cam_server.pipeline.data_processing import functions, processor\n\n" +
                                "def process_image(image, pulse_id, timestamp, x_axis, y_axis, parameters, bsdata=None):\n" +
                                "    ret = processor.process_image(image, pulse_id, timestamp, x_axis, y_axis, parameters, bsdata)\n" +
                                "    return ret";
                client.setScript(name, script);
                updateScripts().join();
                if (!scriptsNames.contains(name)){
                    throw new Exception("Error adding script: " + name);
                }
                int index = scriptsNames.indexOf(name);
                tableUserScripts.setRowSelectionInterval(index, index);
                SwingUtils.scrollToVisible(tableUserScripts, index, 0);
                buttonScriptEditActionPerformed(null);
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }   
    }//GEN-LAST:event_buttonScriptNewActionPerformed

    private void buttonScriptDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptDelActionPerformed
        try{
            int row = tableUserScripts.getSelectedRow();
            if (row>=0){
                String name = Str.toString(modelScripts.getValueAt(row, 0));
                Object[] options = new Object[]{"No", "Yes"};
                if (SwingUtils.showOption(this, "Delete Script", "Are you sure to delete the processing script: " + name  + "?", options, options[0]) == 1){
                    PipelineClient client = new PipelineClient(getUrl());  
                    client.deleteScript(name);
                    updateScripts();
                }
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        } 
    }//GEN-LAST:event_buttonScriptDelActionPerformed

    private void buttonScriptEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptEditActionPerformed
        try{
            int row = tableUserScripts.getSelectedRow();
            if (row>=0){
                String name = Str.toString(modelScripts.getValueAt(row, 0));
                PipelineClient client = new PipelineClient(getUrl());  
                String script = client.getScript(name);
                ScriptEditor dlg = new ScriptEditor(SwingUtils.getFrame(this), true, name,script, "py");
                dlg.setVisible(true);
                if (dlg.getResult()){
                    client.setScript(name, dlg.ret);
                }    
            }
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }        
        
    }//GEN-LAST:event_buttonScriptEditActionPerformed

    private void buttonPermDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPermDeleteActionPerformed
        try{
            modelPermanent.removeRow(tablePermanentInstances.getSelectedRow());
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonPermDeleteActionPerformed

    private void buttonFixedDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonFixedDelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonFixedDelActionPerformed

    private void tableServersKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableServersKeyReleased
        try{
            onTableServersSelection();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_tableServersKeyReleased

    private void tableServersMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableServersMouseReleased
        try{
            onTableServersSelection();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_tableServersMouseReleased

    private void tableFixedInstancesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableFixedInstancesKeyReleased
        updateButtons();
    }//GEN-LAST:event_tableFixedInstancesKeyReleased

    private void tableFixedInstancesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableFixedInstancesMouseReleased
        updateButtons();
    }//GEN-LAST:event_tableFixedInstancesMouseReleased

    private void tablePermanentInstancesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablePermanentInstancesKeyReleased
        updateButtons();
    }//GEN-LAST:event_tablePermanentInstancesKeyReleased

    private void tablePermanentInstancesMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablePermanentInstancesMouseReleased
        updateButtons();
    }//GEN-LAST:event_tablePermanentInstancesMouseReleased

    private void tableUserScriptsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableUserScriptsKeyReleased
        updateButtons();
    }//GEN-LAST:event_tableUserScriptsKeyReleased

    private void tableUserScriptsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableUserScriptsMouseReleased
        updateButtons();
    }//GEN-LAST:event_tableUserScriptsMouseReleased

    private void buttonPermUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPermUndoActionPerformed
        try{
            updatePermanent();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonPermUndoActionPerformed

    private void buttonPermApplyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonPermApplyActionPerformed
        try{
            Map<String,String> map = new HashMap<>();
            for (int i=0; i<modelPermanent.getRowCount();i++){
                map.put((String)modelPermanent.getValueAt(i, 0), (String)modelPermanent.getValueAt(i, 1));
            }
            proxy.setPemanentInstances(map);
            updatePermanent();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonPermApplyActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonConfigDel;
    private javax.swing.JButton buttonConfigEdit;
    private javax.swing.JButton buttonConfigNew;
    private javax.swing.JButton buttonFixedApply;
    private javax.swing.JButton buttonFixedDel;
    private javax.swing.JButton buttonFixedUndo;
    private javax.swing.JButton buttonPermApply;
    private javax.swing.JButton buttonPermDelete;
    private javax.swing.JButton buttonPermUndo;
    private javax.swing.JButton buttonScriptDel;
    private javax.swing.JButton buttonScriptEdit;
    private javax.swing.JButton buttonScriptNew;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPanel panelConfigurations;
    private javax.swing.JPanel panelInstances;
    private javax.swing.JPanel panelScripts;
    private javax.swing.JPanel psnelServers;
    private javax.swing.JSplitPane splitLeft;
    private javax.swing.JSplitPane splitRight;
    private javax.swing.JTable tableConfigurations;
    private javax.swing.JTable tableFixedInstances;
    private javax.swing.JTable tablePermanentInstances;
    private javax.swing.JTable tableServers;
    private javax.swing.JTable tableUserScripts;
    // End of variables declaration//GEN-END:variables
}
