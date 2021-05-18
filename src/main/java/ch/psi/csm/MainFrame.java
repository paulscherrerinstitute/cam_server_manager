package ch.psi.csm;

import java.util.logging.Logger;

/**
 *
 */
public class MainFrame extends ch.psi.utils.swing.MainFrame {

    static MainFrame instance;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        instance = this;
        setTitle("CamServer Management Console");
        loggerPanel.start();
    }
        
    /**
     * Called once when frame is created, before being visible
     */
    @Override
    protected void onCreate() {
        Logger.getLogger(MainFrame.class.getName()).info("Startup");
        panelCameras.setUrl(App.getCameraProxy());
        panelPipelines.setUrl(App.getPipelineProxy());       
        panelDataBuffer.setUrl(App.getPipelineProxy());       
    }

    /**
     * Called once in the first time the frame is shown
     */
    @Override
    protected void onOpen() {
    }

    /**
     * Called once when the frame is about to be disposed
     */
    @Override
    protected void onDispose() {
    }

    /**
     * Called every time the frame is shown (also before open is called)
     */
    @Override
    protected void onShow() {
    }

    /**
     * Called every time the frame is hidden (also before disposed)
     */
    protected void onHide() {
    }

    /**
     * Called every second if the frame is visible
     */
    @Override
    protected void onTimer() {        
    }

    /**
     * Called when window is being closed
     */
    @Override
    protected void onClosing() {
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        jToolBar2 = new javax.swing.JToolBar();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        panelPipelines = new ch.psi.csm.PanelServer();
        jPanel2 = new javax.swing.JPanel();
        panelCameras = new ch.psi.csm.PanelServer();
        panelDataBuffer = new ch.psi.csm.DataBufferPanel();
        jPanel4 = new javax.swing.JPanel();
        loggerPanel = new ch.psi.utils.swing.LoggerPanel();

        jToolBar1.setRollover(true);

        jToolBar2.setRollover(true);

        panelPipelines.setPipeline(true);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPipelines, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelPipelines, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Pipelines", jPanel1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCameras, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelCameras, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Cameras", jPanel2);
        jTabbedPane1.addTab("DataBuffer", panelDataBuffer);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 771, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Logs", jPanel4);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 640, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private ch.psi.utils.swing.LoggerPanel loggerPanel;
    private ch.psi.csm.PanelServer panelCameras;
    private ch.psi.csm.DataBufferPanel panelDataBuffer;
    private ch.psi.csm.PanelServer panelPipelines;
    // End of variables declaration//GEN-END:variables
}
