package ch.psi.csm;

import ch.psi.utils.Str;
import ch.psi.utils.swing.StandardDialog;
import ch.psi.utils.swing.SwingUtils;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 */
public class InfoDialog extends StandardDialog {

    final DefaultTreeModel model;
    String currentInstance;
    
    public InfoDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocationRelativeTo(parent);
        this.setTitle("Instance Info");
        model =(DefaultTreeModel) tree.getModel();  
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    void setInstance(String name){
        this.setTitle("Instance Info: " + name);
        currentInstance = name;
    }
    
    void update(Map<String,Map<String, Object>> instanceInfo){
        boolean instanceSelected = (currentInstance !=null) && (instanceInfo!=null);
        DefaultMutableTreeNode root = ((DefaultMutableTreeNode)model.getRoot());        
        if (instanceSelected){
            root.setUserObject(currentInstance);
            DefaultMutableTreeNode info;
            DefaultMutableTreeNode config ;  
            DefaultMutableTreeNode stream ;  
            DefaultMutableTreeNode start ;  
            if (root.getChildCount()==0){
                config = new DefaultMutableTreeNode("Config");
                info = new DefaultMutableTreeNode("Info");
                start = new DefaultMutableTreeNode();
                stream = new DefaultMutableTreeNode();
                root.add(stream);
                root.add(start);
                root.add(config);
                root.add(info);
                model.nodeChanged(root);
                SwingUtilities.invokeLater(()->{SwingUtils.expandAll(tree);});                
            } else {
                stream = (DefaultMutableTreeNode) root.getChildAt(0);
                start = (DefaultMutableTreeNode) root.getChildAt(1);
                config = (DefaultMutableTreeNode) root.getChildAt(2);
                info = (DefaultMutableTreeNode) root.getChildAt(3);
            }
               
            Map instanceData = instanceInfo.getOrDefault(currentInstance, new HashMap());
            stream.setUserObject("Stream: " + instanceData.getOrDefault("stream_address", ""));                      
            start.setUserObject("Start : " + instanceData.getOrDefault("last_start_time", ""));                      
            
            Map cfg = (Map) instanceData.getOrDefault("config", new HashMap());            
            if (cfg.size()<config.getChildCount()){
                config.removeAllChildren();
                model.nodeStructureChanged(config);
            }
            int index = 0;
            for (Object key : cfg.keySet()){
                if (index>=config.getChildCount()){
                    config.add(new DefaultMutableTreeNode()); 
                    model.nodeStructureChanged(config);
                }
                ((DefaultMutableTreeNode)config.getChildAt(index++)).setUserObject(Str.toString(key) + ": " + Str.toString(cfg.get(key)));                 
            }              
            
            Map stats = (Map) instanceData.getOrDefault("statistics", new HashMap());                     
            if (stats.size()<info.getChildCount()){
                info.removeAllChildren();
                model.nodeStructureChanged(info);
            }
            
            index = 0;
            for (Object key : stats.keySet()){
                if (index>=info.getChildCount()){
                    info.add(new DefaultMutableTreeNode()); 
                    model.nodeStructureChanged(info);
                }
                ((DefaultMutableTreeNode)info.getChildAt(index++)).setUserObject(Str.toString(key) + ": " + PanelStatus.getDisplayValue(stats.get(key)));                 
            }              
            model.nodeChanged(root);
        } else {
            root.removeAllChildren();
            root.setUserObject("");
            model.nodeStructureChanged(root);
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

        jScrollPane2 = new javax.swing.JScrollPane();
        tree = new javax.swing.JTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("Name");
        tree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jScrollPane2.setViewportView(tree);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree tree;
    // End of variables declaration//GEN-END:variables
}
