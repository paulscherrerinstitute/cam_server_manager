
package ch.psi.csm;

import ch.psi.utils.IO;
import ch.psi.utils.swing.ExtensionFileFilter;
import ch.psi.utils.swing.MonitoredPanel;
import ch.psi.utils.swing.SwingUtils;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

/**
 *
 */
public class ImagePanel extends MonitoredPanel {

    public ImagePanel(BufferedImage image) {
        initComponents();
        setImage (image);
    }
    BufferedImage image;
    
    public void setImage (BufferedImage image){     
        this.image=image;
        if (image!=null){
            label.setIcon(new ImageIcon(image));
            text.setText( image.getWidth() + "x" + image.getHeight());
        } else {
            label.setIcon(null);
            text.setText("");
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

        buttonSave = new javax.swing.JButton();
        text = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        label = new javax.swing.JLabel();

        buttonSave.setText("Save");
        buttonSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveActionPerformed(evt);
            }
        });

        text.setEditable(false);
        text.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jScrollPane1.setViewportView(label);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(text, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSave))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonSave)
                    .addComponent(text, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void buttonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveActionPerformed
        try{
                JFileChooser chooser = new JFileChooser();
                chooser.addChoosableFileFilter(new ExtensionFileFilter("PNG files (*.png)", new String[]{"png"}));
                chooser.addChoosableFileFilter(new ExtensionFileFilter("Bitmap files (*.bmp)", new String[]{"bmp"}));
                chooser.addChoosableFileFilter(new ExtensionFileFilter("GIF files (*.gif)", new String[]{"gif"}));
                chooser.addChoosableFileFilter(new ExtensionFileFilter("TIFF files (*.tif)", new String[]{"tif", "tiff"}));
                chooser.addChoosableFileFilter(new ExtensionFileFilter("JPEG files (*.jpg)", new String[]{"jpg", "jpeg"}));
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) { 
                    String filename = chooser.getSelectedFile().getAbsolutePath();
                    String type = "png";
                    String ext = IO.getExtension(chooser.getSelectedFile());
                    for (String fe : new String[]{"bmp", "jpg"}) {
                        if ((chooser.getFileFilter().getDescription().contains(fe))
                                || (fe.equals(ext))) {
                            type = fe;
                            break;
                        }
                    }
                    if ((ext == null) || (ext.isEmpty())) {
                        filename += "." + type;
                    }
                    File f =new File(filename);
                    if (f.exists()) {
                        if (showOption("Overwrite", "File " + filename + " already exists.\nDo you want to overwrite it?", SwingUtils.OptionType.YesNo) == SwingUtils.OptionResult.No) {
                            return;
                        }
                    }
                    ImageIO.write(image, type, f);
                }
        } catch (Exception ex){
            showException(ex);
        }
    }//GEN-LAST:event_buttonSaveActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonSave;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel label;
    private javax.swing.JTextField text;
    // End of variables declaration//GEN-END:variables
}
