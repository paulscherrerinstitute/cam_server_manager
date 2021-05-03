package ch.psi.csm;

import ch.psi.bsread.Stream;
import ch.psi.bsread.Stream.StreamListener;
import ch.psi.bsread.StreamValue;
import ch.psi.utils.Str;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import org.zeromq.ZMQ;

/**
 *
 */
public class StreamPanel extends MonitoredPanel implements StreamListener {

    final DefaultTableModel model;
    String address;
    boolean pull;
    boolean continuous;
    Stream st;
    
    public StreamPanel() {
        initComponents();
        address = "";
        model = (DefaultTableModel) table.getModel();
    }
    
    
    void updateButtons(){
        SwingUtilities.invokeLater(()->{  
            boolean started = false;
            if (continuous){
                started = isStarted();
                button.setText(started ? "Stop" : "Start");
            } else {
                button.setText("Update");
            }
            button.setEnabled(!address.isBlank());
            comboProtocol.setEnabled(!address.isBlank() && (!started));
        });                
    }
    
    void configure (String address, boolean pull, boolean continuous){
        stop();
        this.address =   (address!=null) ? address : "";
        this.pull=pull;
        this.continuous = continuous;
        textAddress.setText(address);
        if (pull) {
            comboProtocol.setSelectedIndex(1);
        }        
        updateButtons();                    
    }
    
    void clear(){
        configure(null, false, false);
    }

    @Override
    protected void onShow(){
        comboProtocol.setEnabled(!continuous);
    }
        
    public void update() {
        if (!continuous){
            model.setRowCount(0);
            button.setEnabled(false);
            comboProtocol.setEnabled(false);        
            new Thread(() -> {
                try (Stream st = new Stream(address,  (comboProtocol.getSelectedIndex() == 1) ? ZMQ.PULL : ZMQ.SUB)) {
                    st.start();
                    StreamValue sv = st.read(5000);
                    updateTable(sv);
                } catch (Exception ex) {
                    SwingUtils.showException(StreamPanel.this, ex);
                } finally {
                    updateButtons();
                }            
            }).start();
        }
    }
    
    void start(){
        stop();
        model.setRowCount(0);
        st = new Stream(address, pull ? ZMQ.PULL : ZMQ.SUB);
        st.start();
        st.addListener(this);
        updateButtons();
    }
    
    void stop(){
        try{
            if (st!=null){
                st.removeListener(this);
                st.stop();
            }        
        } finally {
            st=null;
            model.setRowCount(0);
        }
        updateButtons();
    }
    
    boolean isStarted(){
        return ((st!=null) && (st.isStarted()));
    }
    
    
    void updateTable(StreamValue sv){
        int index = 0;
        if (sv==null){
            model.setNumRows(0);
            return;
        }
        List<String> keys= sv.getKeys();
        model.setNumRows(keys.size());
        for (String key : keys) {
            Object val = sv.getValue(key);
            String type = val.getClass().getTypeName();
            if (index>=model.getRowCount()){
                model.addRow(new Object[]{"","",""});            
            } else {
                model.setValueAt(key, index, 0);
                model.setValueAt(type, index, 1);
                model.setValueAt(Str.toString(val, 10), index, 2);
            }
            
            index++;
        }        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        textAddress = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        comboProtocol = new javax.swing.JComboBox<>();
        button = new javax.swing.JButton();

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Channel", "Type", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        table.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(table);

        jLabel1.setText("Address:");

        textAddress.setEditable(false);

        jLabel2.setText("Protocol:");

        comboProtocol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SUB", "PULL" }));

        button.setText("Update");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textAddress)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comboProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(textAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(button)
                    .addComponent(comboProtocol, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
        if (continuous){
            if (button.getText().equalsIgnoreCase("start")){
                start();
            } else {
                stop();
            }
        } else {
            update();
        }
    }//GEN-LAST:event_buttonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button;
    private javax.swing.JComboBox<String> comboProtocol;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable table;
    private javax.swing.JTextField textAddress;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onStart() {
        updateButtons();
    }

    @Override
    public void onStop(Throwable ex) {
        st=null;
        updateButtons();
    }

    @Override
    public void onValue(StreamValue value) {
        updateTable(value);
    }
}
