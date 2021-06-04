package ch.psi.csm;

import ch.psi.camserver.CameraClient;
import ch.psi.camserver.PipelineClient;
import ch.psi.camserver.ProxyClient;
import ch.psi.utils.Str;
import ch.psi.utils.Threading;
import ch.psi.utils.swing.ExtensionFileFilter;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 */
public class BackgroundPanel extends MonitoredPanel {

    PipelineClient pc;
    CameraClient cc;
    Set<String> cameras = new HashSet<>();
    final DefaultTableModel model;
    volatile boolean running = false;
    String camera=null;
    BufferedImage lastBackground;

    public BackgroundPanel() {
        initComponents();
        model = (DefaultTableModel) table.getModel();
    }
    
    
    public void setUrl(String urlPipeline, String urlCamera){
        pc = new PipelineClient(urlPipeline);
        cc = new CameraClient(urlCamera);
    }
    
    public String getUrl(){
       if (pc==null){
           return null;
       }
       return pc.getUrl();
    }   
    
    public void setProxy(PipelineClient proxy){
        this.pc = proxy;

    }
    
    public PipelineClient getProxy(){
       return pc;
    }     
    
    void updateButtons(){
        if (!SwingUtilities.isEventDispatchThread()){
            SwingUtilities.invokeLater(()->{updateButtons();});
            return;
        }        
        boolean updating = (cameraUpdateThread!=null);
        buttonCapture.setEnabled(!updating && (camera!=null));
        buttonSetBackground.setEnabled(false); //(!updating && (camera!=null)); TODO: Implement set_image in CamServer
        buttonShowLast.setEnabled(!updating &&(lastBackground!=null));
        buttonGetImage.setEnabled(!updating &&(camera!=null));
    }
    
    Thread updateCameras(){
        Thread t = new Thread(()->{
            //model.setNumRows(0);
            
            cameras = new HashSet<>();
            try {
                for (String camera: pc.getCameras()){
                    try {
                        if ((camera!=null) && (!camera.isBlank())){
                            cameras.add(camera);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);            
            }
            List<String> names = new ArrayList<>(cameras);
            Collections.sort(names);
            SwingUtilities.invokeLater(()->{        
                model.setNumRows(0);
                for (String camera: names){
                    model.addRow(new Object[]{camera,});
                }
                updateButtons();        
            });
        }, "BP Update Cameras");
        t.start();
        return t;
    }    
    
    volatile Thread cameraUpdateThread;
    int selectedIndex;
            
    Thread updateCamera(boolean force) throws IOException{   
        if (cameraUpdateThread!=null){
            try {
                Threading.stop(cameraUpdateThread, true, 1000);
            } catch (InterruptedException ex) {
                return null;
            }
            cameraUpdateThread=null;
        }
        selectedIndex = table.getSelectedRow();
        String camera = (selectedIndex>=0) ? Str.toString(model.getValueAt(selectedIndex, 0)) : null;  
        if ((force) || (camera != this.camera)){
            this.camera = camera;
            textLast.setText("");
            textGeometryBack.setText("");
            checkOnline.setSelected(false);
            textGeometry.setText("");            
            lastBackground=null;
            if (camera!=null) {
                cameraUpdateThread = new Thread(()->{
                    try{
                        try{
                            textLast.setText(pc.getLastBackground(camera)); 
                        } catch (Exception ex){
                             Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);   
                        }
                        updateButtons();        
                        if (!camera.equals(this.camera)){
                            return;
                        }
                        try{
                            lastBackground = pc.getBackgroundImage(textLast.getText());
                            textGeometryBack.setText(lastBackground.getWidth() + "x" + lastBackground.getHeight());
                        } catch (Exception ex){ 
                            Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);   
                        }   
                        updateButtons();        
                        if (!camera.equals(this.camera)){
                            return;
                        }
                        try{
                            checkOnline.setSelected(cc.isOnline(camera));
                        } catch (Exception ex){
                            Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);   
                        }   
                        if (!camera.equals(this.camera)){
                            return;
                        }
                        try{
                            Dimension g = cc.getGeometry(camera);
                            textGeometry.setText(g.width + "x" + g.height);
                        } catch (Exception ex){
                            Logger.getLogger(BackgroundPanel.class.getName()).log(Level.WARNING, null, ex);   
                        }    
                    } finally {
                        //table.setEnabled(true);
                        updateButtons();    
                        cameraUpdateThread = null;
                    }   
                }, "BP Update Camera");
                //table.setEnabled(false);
                cameraUpdateThread.start();
            }
            updateButtons();
            return cameraUpdateThread;
        }
        return null;
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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        textLast = new javax.swing.JTextField();
        buttonShowLast = new javax.swing.JButton();
        buttonCapture = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        spinnerImages = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        textGeometryBack = new javax.swing.JTextField();
        buttonSetBackground = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        checkOnline = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        buttonGetImage = new javax.swing.JButton();
        textGeometry = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Cameras"));

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
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
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
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Background"));

        jLabel1.setText("Last:");

        textLast.setEditable(false);

        buttonShowLast.setText("Show");
        buttonShowLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonShowLastActionPerformed(evt);
            }
        });

        buttonCapture.setText("Capture");
        buttonCapture.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCaptureActionPerformed(evt);
            }
        });

        jLabel2.setText("Images:");

        spinnerImages.setModel(new javax.swing.SpinnerNumberModel(5, 1, 100, 1));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText("Geometry:");

        textGeometryBack.setEditable(false);

        buttonSetBackground.setText("Set Background");
        buttonSetBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSetBackgroundActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textGeometryBack, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonShowLast))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(buttonCapture)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spinnerImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(textLast)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(buttonSetBackground)
                                .addGap(197, 197, 197)))))
                .addContainerGap())
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel1, jLabel5});

        jPanel2Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonCapture, buttonSetBackground});

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textLast, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonShowLast)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(textGeometryBack, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCapture)
                    .addComponent(jLabel2)
                    .addComponent(spinnerImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonSetBackground)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        checkOnline.setEnabled(false);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("Online:");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel4.setText("Geometry:");

        buttonGetImage.setText("Get Snapshot");
        buttonGetImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonGetImageActionPerformed(evt);
            }
        });

        textGeometry.setEditable(false);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkOnline)
                    .addComponent(textGeometry, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonGetImage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel4});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(checkOnline))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(textGeometry, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(buttonGetImage)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableMouseReleased
        try{
            updateCamera(false);
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_tableMouseReleased

    private void tableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tableKeyReleased
        try{
            updateCamera(false);
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_tableKeyReleased

    private void buttonCaptureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCaptureActionPerformed
        try{
            buttonCapture.setEnabled(false);
            SwingUtilities.invokeLater(()->{    
              try{
                pc.captureBackground(camera, (Integer) spinnerImages.getValue());
                updateCamera(true);
              } catch (Exception ex){
                Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
                showException(ex);
              }
            });
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_buttonCaptureActionPerformed

    private void buttonShowLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonShowLastActionPerformed
        try{
            //BufferedImage img = lastBackground; //pc.getBackgroundImage(textLast.getText());
            //JLabel label = new JLabel(new ImageIcon(img));
            //JScrollPane sp = new JScrollPane(label);            
            //SwingUtils.showDialog(this, textLast.getText() +  " - " +  img.getWidth() + "x" + img.getHeight(), new Dimension (600,400), sp);
            SwingUtils.showDialog(this, textLast.getText(), new Dimension (600,400),  new ImagePanel(lastBackground));
            
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_buttonShowLastActionPerformed

    private void buttonGetImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonGetImageActionPerformed
        try{
            SwingUtils.showDialog(this, camera + " snapshot", new Dimension (600,400),  new ImagePanel(cc.getImage(camera)));
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_buttonGetImageActionPerformed

    private void buttonSetBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSetBackgroundActionPerformed
        try{
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(new ExtensionFileFilter("Image files (*.png, *.bmp, *.gif, *.jpg)", 
                                         new String[]{"png", "bmp", "gif", "tif", "tiff", "jpg", "jpeg"}));
            int rVal = chooser.showOpenDialog(this);
            
            if (rVal == JFileChooser.APPROVE_OPTION) {
                byte[] bytes = Files.readAllBytes(chooser.getSelectedFile().toPath());
                pc.setBackgroundImage(camera+ "_"+ chooser.getSelectedFile().getName(), bytes);
            }
   
        } catch (Exception ex){
            Logger.getLogger(DataBufferPanel.class.getName()).log(Level.WARNING, null, ex);     
            showException(ex);
        }
    }//GEN-LAST:event_buttonSetBackgroundActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCapture;
    private javax.swing.JButton buttonGetImage;
    private javax.swing.JButton buttonSetBackground;
    private javax.swing.JButton buttonShowLast;
    private javax.swing.JCheckBox checkOnline;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner spinnerImages;
    private javax.swing.JTable table;
    private javax.swing.JTextField textGeometry;
    private javax.swing.JTextField textGeometryBack;
    private javax.swing.JTextField textLast;
    // End of variables declaration//GEN-END:variables
}
