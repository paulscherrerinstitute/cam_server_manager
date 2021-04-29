package ch.psi.csm;

import ch.psi.camserver.ProxyClient;
import ch.psi.utils.Str;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

/**
 *
 */
public class PanelConfig extends MonitoredPanel {

    ProxyClient proxy;
    Map<String, Object> serverCfg = null;
    List<String> instanceCfgNames;
    Map<String, String> permanentInstances = null;
    String currentConfig = "" ;
    String currentServer = "" ;
    
    final DefaultTableModel modelConfigs;
    final DefaultTableModel modelServers;
    final DefaultTableModel modelFixed;
    final DefaultTableModel modelPermanent;
    final DefaultTableModel modelScripts;
    
    boolean modelPermanentChanged;
    
    final int SMALL_COL_SZIE = 80;
    
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
        tablePermanentInstances.getColumnModel().getColumn(0).setPreferredWidth(SMALL_COL_SZIE);
        tablePermanentInstances.getColumnModel().getColumn(0).setMaxWidth(SMALL_COL_SZIE);
        tablePermanentInstances.getColumnModel().getColumn(0).setResizable(false);    
        
        modelPermanent.addTableModelListener((TableModelEvent e) -> {
            modelPermanentChanged=true;
            updateButtons();
        });
        
        updateButtons();
    }
    
    
    void updateButtons(){
        if (!SwingUtilities.isEventDispatchThread()){
            SwingUtilities.invokeLater(()->{updateButtons();});
            return;
        }
        
        buttonScriptDel.setEnabled(tableUserScripts.getSelectedRow()>=0);
        buttonScriptEdit.setEnabled(tableUserScripts.getSelectedRow()>=0);       
        buttonConfigDel.setEnabled(tableConfigurations.getSelectedRow()>=0);
        buttonConfigEdit.setEnabled(tableConfigurations.getSelectedRow()>=0);
        buttonFixedDel.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonFixedApply.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonFixedUndo.setEnabled(tableFixedInstances.getSelectedRow()>=0);
        buttonPermDelete.setEnabled(tablePermanentInstances.getSelectedRow()>=0);
        buttonPermApply.setEnabled(modelPermanentChanged && (tablePermanentInstances.getSelectedRow()>=0));
        buttonPermUndo.setEnabled(modelPermanentChanged && (tablePermanentInstances.getSelectedRow()>=0));
        
    }
    
    void updateConfigs(){
        new Thread(()->{
            try {
                instanceCfgNames =proxy.getConfigNames();
                modelConfigs.setRowCount(0);
                for (String instance : instanceCfgNames){
                    modelConfigs.addRow(new Object[]{instance});
                }                
                updateButtons();
            } catch (IOException ex) {
                Logger.getLogger(PanelConfig.class.getName()).log(Level.WARNING, null, ex);
            }             
        }).start();
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
    @Override
    protected void onShow() {      
        updateServers();
        updateConfigs();
        updatePermanent();
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
          
    
    void onTableInstancesSelection() throws IOException{
        int row=tableConfigurations.getSelectedRow();
        if (row<0){
            currentConfig = "";
            textConfig.setText("");
        } else {
            currentConfig = String.valueOf(tableConfigurations.getValueAt(row, 0));
            String config = proxy.getConfigStr(currentConfig);
            textConfig.setText(config);            
            SwingUtils.scrollToVisible(textConfig, 0);
        }
        updateButtons();
    }
    
    void onTableServersSelection() throws IOException{
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

        jPanel1 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        tableServers = new javax.swing.JTable();
        buttonFixedUndo = new javax.swing.JButton();
        buttonFixedApply = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        tableFixedInstances = new javax.swing.JTable();
        buttonFixedDel = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableConfigurations = new javax.swing.JTable();
        buttonConfigEdit = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        textConfig = new javax.swing.JTextArea();
        buttonConfigNew = new javax.swing.JButton();
        buttonConfigDel = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tablePermanentInstances = new javax.swing.JTable();
        buttonPermUndo = new javax.swing.JButton();
        buttonPermApply = new javax.swing.JButton();
        buttonPermDelete = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableUserScripts = new javax.swing.JTable();
        buttonScriptNew = new javax.swing.JButton();
        buttonScriptDel = new javax.swing.JButton();
        buttonScriptEdit = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Servers"));

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonFixedDel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFixedUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonFixedApply)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonFixedUndo)
                    .addComponent(buttonFixedApply)
                    .addComponent(buttonFixedDel))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Configurations"));

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

        textConfig.setEditable(false);
        textConfig.setColumns(20);
        textConfig.setRows(5);
        jScrollPane3.setViewportView(textConfig);

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                        .addComponent(buttonConfigNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonConfigDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonConfigEdit)
                        .addContainerGap(18, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(jScrollPane3))
                        .addContainerGap())))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonConfigDel, buttonConfigEdit, buttonConfigNew});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonConfigEdit)
                    .addComponent(buttonConfigNew)
                    .addComponent(buttonConfigDel))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Permanent Instances"));

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonPermDelete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPermUndo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPermApply)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonPermApply, buttonPermDelete, buttonPermUndo});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonPermApply)
                    .addComponent(buttonPermDelete)
                    .addComponent(buttonPermUndo))
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("User Scripts"));

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

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                        .addComponent(buttonScriptNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptEdit)
                        .addGap(0, 11, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonScriptDel, buttonScriptEdit, buttonScriptNew});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonScriptEdit)
                    .addComponent(buttonScriptNew)
                    .addComponent(buttonScriptDel))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(0, 0, 0))
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
        String config;
        try{
            ConfigDialog dlg = new ConfigDialog(SwingUtils.getFrame(this), true, currentConfig,textConfig.getText());
            dlg.setVisible(true);
            if (dlg.getResult()){
                proxy.setConfigStr(currentConfig, dlg.ret);
                onTableInstancesSelection();
            }    
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }        
    }//GEN-LAST:event_buttonConfigEditActionPerformed

    private void buttonConfigNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonConfigNewActionPerformed

    private void buttonConfigDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonConfigDelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonConfigDelActionPerformed

    private void buttonScriptNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptNewActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonScriptNewActionPerformed

    private void buttonScriptDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptDelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_buttonScriptDelActionPerformed

    private void buttonScriptEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptEditActionPerformed
        // TODO add your handling code here:
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTable tableConfigurations;
    private javax.swing.JTable tableFixedInstances;
    private javax.swing.JTable tablePermanentInstances;
    private javax.swing.JTable tableServers;
    private javax.swing.JTable tableUserScripts;
    private javax.swing.JTextArea textConfig;
    // End of variables declaration//GEN-END:variables
}
