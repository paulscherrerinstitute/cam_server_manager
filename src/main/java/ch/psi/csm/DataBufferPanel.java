package ch.psi.csm;

import ch.psi.camserver.PipelineClient;
import ch.psi.camserver.ProxyClient;
import ch.psi.utils.Str;
import ch.psi.utils.swing.MonitoredPanel;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 */
public class DataBufferPanel extends MonitoredPanel {

    ProxyClient proxy;
    Set<String> permanentPipelineCameras = new HashSet<>();
    final DefaultTableModel model;

    public DataBufferPanel() {
        initComponents();
        model = (DefaultTableModel) table.getModel();
    }
    
    
    public void setUrl(String url){
        setProxy(new ProxyClient(url));
    }
    
    public String getUrl(){
       if (proxy==null){
           return null;
       }
       return proxy.getUrl();
    }   
    
    public void setProxy(ProxyClient proxy){
        this.proxy = proxy;

    }
    
    public ProxyClient getProxy(){
       return proxy;
    }     
    
    void updateButtons(){
        if (!SwingUtilities.isEventDispatchThread()){
            SwingUtilities.invokeLater(()->{updateButtons();});
            return;
        }        
        button.setEnabled(table.getSelectedRow()>=0);
    }
    
    Thread updateCameras(){
        Thread t = new Thread(()->{
            //model.setNumRows(0);
            PipelineClient client = new PipelineClient(getUrl());   
            permanentPipelineCameras = new HashSet<>();
            try {
                for (String pipeline:getProxy().getPemanentInstances().keySet()){
                    try {
                        Map<String, Object> cfg = client.getConfig(pipeline);
                        String camera = Str.toString(cfg.getOrDefault("camera_name", "")).trim();
                        if (!camera.isBlank()){
                            permanentPipelineCameras.add(camera);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);            
            }
            SwingUtilities.invokeLater(()->{        
                model.setNumRows(0);
                for (String camera: permanentPipelineCameras){
                    model.addRow(new Object[]{camera,});
                }
                updateButtons();        
            });
        });
        t.start();
        return t;
    }    
    
    @Override
    protected void onShow(){
        updateCameras();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        button = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Permanent Pipeline Cameras"));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Camera Name"
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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        button.setText("Reconnect to DataBuffer");
        button.setEnabled(false);
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(button)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(137, Short.MAX_VALUE)
                .addComponent(button)
                .addContainerGap(138, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseReleased
        updateButtons();
    }//GEN-LAST:event_tableMouseReleased

    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased
        updateButtons();
    }//GEN-LAST:event_tableKeyReleased

    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
        try{
            String camera = Str.toString(model.getValueAt(table.getSelectedRow(), 0));
            String ret = DataBuffer.reconnectCameraSources(camera);                        
            showScrollableMessage( "Success", "Success reconnecting camera sources: " + camera, ret);
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_buttonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
}
