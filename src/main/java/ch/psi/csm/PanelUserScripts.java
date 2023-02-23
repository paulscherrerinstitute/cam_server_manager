package ch.psi.csm;

import ch.psi.camserver.PipelineClient;
import ch.psi.camserver.ProxyClient;
import ch.psi.utils.Str;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 */
public class PanelUserScripts extends MonitoredPanel {

    ProxyClient proxy;
    List<String> scriptsNames = new ArrayList<>();
    final DefaultTableModel modelScripts;
    List<String> visibleNames = new ArrayList<>();

    public void setUrl(String url){
        setProxy(new ProxyClient(url));
    }       

    public PanelUserScripts() {
        initComponents();

        modelScripts = (DefaultTableModel) tableUserScripts.getModel();

        tableUserScripts.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2) && (!e.isPopupTrigger())) {
                    buttonScriptEditActionPerformed(null);
                }
            }
        });
        updateButtons();
    }

    void updateButtons() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> {
                updateButtons();
            });
            return;
        }
        buttonScriptEdit.setEnabled(tableUserScripts.getSelectedRow() >= 0);
        buttonScriptDel.setEnabled(tableUserScripts.getSelectedRow() >= 0);
    }

    Thread updateScripts() {
        Thread t = new Thread(() -> {
            try {
                PipelineClient client = new PipelineClient(getUrl());
                scriptsNames = client.getScripts();
                Collections.sort(scriptsNames); //, String.CASE_INSENSITIVE_ORDER);
                
            
                visibleNames = List.copyOf(scriptsNames);
                if ((filterName!=null) && (!filterName.isBlank())){
                    visibleNames = visibleNames
                        .stream()
                        .filter(c -> c.toLowerCase().contains(filterName))
                        .collect(Collectors.toList());                            
                }                
                
                modelScripts.setRowCount(0);
                for (String script : visibleNames) {
                    modelScripts.addRow(new Object[]{script});
                }
                updateButtons();
            } catch (Exception ex) {
                Logger.getLogger(PanelUserScripts.class.getName()).log(Level.WARNING, null, ex);
            }
        }, "PC Update Scripts");
        t.start();
        return t;
    }

    @Override
    protected void onShow() {
        updateButtons();
        updateScripts();
    }

    public void setProxy(ProxyClient proxy) {
        this.proxy = proxy;
    }

    public ProxyClient getProxy() {
        return proxy;
    }

    public String getUrl() {
        if (proxy == null) {
            return null;
        }
        return proxy.getUrl();
    }
    
    String filterName;
    void setFilter(String str){        
        if (str==null){
            str="";
        }
        if (!str.equals(filterName)){
            filterName = str;
            updateScripts();
        }
    }
        
    void onFilter(){
        setFilter(textFilter.getText().trim().toLowerCase());
    }        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelScripts = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        tableUserScripts = new javax.swing.JTable();
        buttonScriptNew = new javax.swing.JButton();
        buttonScriptDel = new javax.swing.JButton();
        buttonScriptEdit = new javax.swing.JButton();
        textFilter = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

        panelScripts.setPreferredSize(new java.awt.Dimension(288, 250));

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

        textFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textFilterKeyReleased(evt);
            }
        });

        jLabel5.setText("Filter:");

        javax.swing.GroupLayout panelScriptsLayout = new javax.swing.GroupLayout(panelScripts);
        panelScripts.setLayout(panelScriptsLayout);
        panelScriptsLayout.setHorizontalGroup(
            panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScriptsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelScriptsLayout.createSequentialGroup()
                        .addGap(0, 58, Short.MAX_VALUE)
                        .addComponent(buttonScriptNew)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptDel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonScriptEdit)
                        .addGap(0, 58, Short.MAX_VALUE))
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(panelScriptsLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFilter)))
                .addContainerGap())
        );

        panelScriptsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonScriptDel, buttonScriptEdit, buttonScriptNew});

        panelScriptsLayout.setVerticalGroup(
            panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelScriptsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(textFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelScriptsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonScriptEdit)
                    .addComponent(buttonScriptNew)
                    .addComponent(buttonScriptDel))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelScripts, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelScripts, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonScriptNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptNewActionPerformed
        try {
            String name = SwingUtils.getString(this, "Enter script name: ", "");
            if (name != null) {

                if (!name.endsWith(".py") && !name.endsWith(".c")) {
                    name = name + ".py";
                }

                if (scriptsNames.contains(name)) {
                    throw new Exception("Script name is already used: " + name);
                }
                PipelineClient client = new PipelineClient(getUrl());
                String script = "from cam_server.pipeline.data_processing import functions, processor\n\n"
                        + "def process_image(image, pulse_id, timestamp, x_axis, y_axis, parameters, bsdata=None):\n"
                        + "    ret = processor.process_image(image, pulse_id, timestamp, x_axis, y_axis, parameters, bsdata)\n"
                        + "    return ret";
                client.setScript(name, script);
                updateScripts().join();
                if (!scriptsNames.contains(name)) {
                    throw new Exception("Error adding script: " + name);
                }
                int index = visibleNames.indexOf(name);
                if (index>=0){
                    tableUserScripts.setRowSelectionInterval(index, index);
                    SwingUtils.scrollToVisible(tableUserScripts, index, 0);
                    buttonScriptEditActionPerformed(null);          
                }                 
            }
        } catch (Exception ex) {
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonScriptNewActionPerformed

    private void buttonScriptDelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptDelActionPerformed
        try {
            int row = tableUserScripts.getSelectedRow();
            if (row >= 0) {
                String name = Str.toString(modelScripts.getValueAt(row, 0));
                Object[] options = new Object[]{"No", "Yes"};
                if (SwingUtils.showOption(this, "Delete Script", "Are you sure to delete the processing script: " + name + "?", options, options[0]) == 1) {
                    PipelineClient client = new PipelineClient(getUrl());
                    client.deleteScript(name);
                    updateScripts();
                }
            }
        } catch (Exception ex) {
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_buttonScriptDelActionPerformed

    private void buttonScriptEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonScriptEditActionPerformed
        try {
            int row = tableUserScripts.getSelectedRow();
            if (row >= 0) {
                String name = Str.toString(modelScripts.getValueAt(row, 0));
                PipelineClient client = new PipelineClient(getUrl());
                String script = client.getScript(name);
                String type = name.endsWith(".c") ? "c" : "py";
                ScriptEditor dlg = new ScriptEditor(SwingUtils.getFrame(this), true, name, script, type);
                dlg.setVisible(true);
                if (dlg.getResult()) {
                    client.setScript(name, dlg.ret);
                }
            }
        } catch (Exception ex) {
            SwingUtils.showException(this, ex);
        }

    }//GEN-LAST:event_buttonScriptEditActionPerformed

    private void tableUserScriptsKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableUserScriptsKeyReleased
        updateButtons();
    }//GEN-LAST:event_tableUserScriptsKeyReleased

    private void tableUserScriptsMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableUserScriptsMouseReleased
        updateButtons();
    }//GEN-LAST:event_tableUserScriptsMouseReleased

    private void textFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFilterKeyReleased
        try{
            onFilter();
        } catch (Exception ex){
            SwingUtils.showException(this, ex);
        }
    }//GEN-LAST:event_textFilterKeyReleased


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonScriptDel;
    private javax.swing.JButton buttonScriptEdit;
    private javax.swing.JButton buttonScriptNew;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JPanel panelScripts;
    private javax.swing.JTable tableUserScripts;
    private javax.swing.JTextField textFilter;
    // End of variables declaration//GEN-END:variables
}
