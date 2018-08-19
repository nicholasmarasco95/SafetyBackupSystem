/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.Color;
import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author nicho
 */
public class DaysLimit extends javax.swing.JDialog {
    
    //next update this file will be named "StorageLimit"

    private String limitType;
    private Settings setting = new Settings();
    
    public DaysLimit(java.awt.Frame parent, boolean modal, String limitType) {
        super(parent, modal);
        this.limitType= limitType;
        this.setTitle("Set Storage Limit");
        initComponents();
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        CheckDaysSettings();
        labelStorAutoFreeTxt.setForeground(Color.black);
        labelStorLimitTxtFooter.setForeground(Color.black);
    }
    
    private void CheckDaysSettings(){
        if(CheckAutoFree()){
           labelStorFreeOnOff.setIcon(new ImageIcon(getClass().getResource(switchOn))); 
           textFieldStorLimit.setEditable(false);
           labelStorLimitOnOff.setIcon(new ImageIcon(getClass().getResource(switchOff)));
           labelStorLimit.setText("AUTO");
           labelStorLimitOnOffLabel.setForeground(Color.gray); labelStorLimitTxt.setForeground(Color.gray); labelDayLimitTxtGB.setForeground(Color.gray); labelStorLimit.setForeground(Color.blue);
        }
        else{
            labelStorFreeOnOff.setIcon(new ImageIcon(getClass().getResource(switchOff)));
            textFieldStorLimit.setEditable(true);
            labelStorLimitOnOffLabel.setForeground(Color.black); labelStorLimitTxt.setForeground(Color.black); labelDayLimitTxtGB.setForeground(Color.black);
            if(CheckGBLimit()){
                labelStorLimitOnOff.setIcon(new ImageIcon(getClass().getResource(switchOn)));
                labelStorLimit.setText(setting.getIntValue("deviceGbLim").toString() + " GB");
                labelStorLimit.setForeground(Color.green);
            }
            else{
                labelStorLimitOnOff.setIcon(new ImageIcon(getClass().getResource(switchOff)));
                labelStorLimit.setText("OFF");
                labelStorLimit.setForeground(Color.orange);
            }
        }
    }
    
    private boolean CheckAutoFree(){
        return (setting.getBoolValue("deviceAutoFree"));
    }
    
    private boolean CheckGBLimit(){
        if(setting.getIntValue("deviceGbLim")<=0){
            return false;
        }
        return true;
    }
    
    private File getOldestFile(){
        List fileList= getFileList();
        Iterator<File> itFile= fileList.iterator();
        if(!itFile.hasNext()){
            return null;
        }
        File tmpOld= itFile.next();
        while(itFile.hasNext()){
            File tmpFile= itFile.next();
            if(tmpFile.lastModified() < tmpOld.lastModified()){
                tmpOld= tmpFile;
            }
        }
        return tmpOld;
    }
    
    private List getFileList(){
        File folder= new File(setting.getStingValue("deviceFoldDest"));
        File[] totTree= folder.listFiles();
        List<File> fileList= new LinkedList<File>();
        for (int i=0; i<totTree.length; i++){
            String toCheckFile= totTree[i].toString();
            if(toCheckFile.endsWith(".zip")){
                if(toCheckFile.contains("SBS-Backup-")){
                    fileList.add(totTree[i]);
                }
            }
        }
        return fileList;
    }
    
    
    private long FilesListSize(){
        //get the fileList and calculate total size of SBS files.
        long size=0;
        List fileList= getFileList();
        Iterator<File> itFile= fileList.iterator();
        while(itFile.hasNext()){
            File tmpFile= itFile.next();
            size+=tmpFile.length();
        }
        return size;
    }
    
    private int GetLimit(){
        String strLimit= textFieldStorLimit.getText();
        int limit= -1;
        try{
            limit= Integer.parseInt(strLimit);
            if(limit<=0){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Limit must be greater than 0");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            textFieldStorLimit.setText("");
            }
        }
        catch (NumberFormatException nfe){
            if(!CheckGBLimit()){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Limit must be a number");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            textFieldStorLimit.setText("");
           }
        }
        return limit;
    }
    
    private void deleteGbLimit(long backupSize){
        long gbLimit = (long) setting.getIntValue("deviceGbLim") * 1073741824;  //convert "gbLimitInt" to bytes
        long filesSize= FilesListSize();
        long totSize= filesSize + backupSize;  //sum all SBS files size + backup size
        while(totSize>gbLimit && filesSize>0){
            File fileToDel= getOldestFile();
            fileToDel.delete();
            totSize= FilesListSize() + backupSize;
        }
    }
    
    
    private void AutoFree(long backupSize){
        String strDest= setting.getStingValue("deviceFoldDest");
        long destSize = new File(strDest).getUsableSpace();
        long filesSize= FilesListSize();
        long totSize= filesSize + backupSize;  //sum all SBS files size + backup size
        while(totSize>destSize && filesSize>0){
            File fileToDel= getOldestFile();
            fileToDel.delete();
            totSize= FilesListSize() + backupSize;
        }
    }
    
    public void deleteFilesLimit(long backupSize){
        if(CheckAutoFree()){
            AutoFree(backupSize);
            return;
        }
        if(CheckGBLimit()){
            deleteGbLimit(backupSize);
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

        buttonGroup3 = new javax.swing.ButtonGroup();
        jCheckBoxMenuItem2 = new javax.swing.JCheckBoxMenuItem();
        labelStorLimitTxt = new javax.swing.JLabel();
        btnClose = new javax.swing.JButton();
        labelStorAutoFreeTxt = new javax.swing.JLabel();
        labelStorFreeOnOff = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        textFieldStorLimit = new javax.swing.JTextField();
        labelDayLimitTxtGB = new javax.swing.JLabel();
        labelStorLimitOnOff = new javax.swing.JLabel();
        labelStorLimitOnOffLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        labelStorLimitTxtFooter = new javax.swing.JLabel();
        labelStorLimit = new javax.swing.JLabel();

        jCheckBoxMenuItem2.setSelected(true);
        jCheckBoxMenuItem2.setText("jCheckBoxMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        labelStorLimitTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelStorLimitTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/storage-icon-2-50x50.png"))); // NOI18N
        labelStorLimitTxt.setText("Storage Limit");

        btnClose.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/close-icon-30x30.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        labelStorAutoFreeTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelStorAutoFreeTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/storage-icon-50x50.png"))); // NOI18N
        labelStorAutoFreeTxt.setText("Free the space automatically to add backup");

        labelStorFreeOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelStorFreeOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelStorFreeOnOfflabelDeviceOnOffEvent(evt);
            }
        });

        textFieldStorLimit.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        textFieldStorLimit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                textFieldStorLimitClick(evt);
            }
        });
        textFieldStorLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldStorLimitActionPerformed(evt);
            }
        });

        labelDayLimitTxtGB.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDayLimitTxtGB.setText("GB");

        labelStorLimitOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelStorLimitOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelStorLimitOnOfflabelDeviceOnOffEvent(evt);
            }
        });

        labelStorLimitOnOffLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelStorLimitOnOffLabel.setText("On/Off");

        labelStorLimitTxtFooter.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelStorLimitTxtFooter.setText("Storage Limit");

        labelStorLimit.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelStorLimit.setForeground(new java.awt.Color(0, 0, 255));
        labelStorLimit.setText("AUTO");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(labelStorLimitTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldStorLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelDayLimitTxtGB)
                        .addGap(54, 54, 54)
                        .addComponent(labelStorLimitOnOffLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStorLimitOnOff)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelStorAutoFreeTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelStorFreeOnOff))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelStorLimitTxtFooter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelStorLimit)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose)))
                .addContainerGap())
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelStorLimitTxt)
                            .addComponent(textFieldStorLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(labelDayLimitTxtGB)
                            .addComponent(labelStorLimitOnOffLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(labelStorLimitOnOff)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(labelStorAutoFreeTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelStorFreeOnOff)
                        .addGap(18, 18, 18)))
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose)
                    .addComponent(labelStorLimitTxtFooter, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelStorLimit, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(7, 7, 7))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void labelStorFreeOnOfflabelDeviceOnOffEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelStorFreeOnOfflabelDeviceOnOffEvent
        if(CheckAutoFree()){
            setting.SaveSetting("boolean", "deviceAutoFree", "false");
            setting.SaveSetting("int", "deviceGbLim", "-1");
        }
        else{
            setting.SaveSetting("boolean", "deviceAutoFree", "true");
            setting.SaveSetting("int", "deviceGbLim", "-1");
        }
        CheckDaysSettings();
    }//GEN-LAST:event_labelStorFreeOnOfflabelDeviceOnOffEvent

    private void labelStorLimitOnOfflabelDeviceOnOffEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelStorLimitOnOfflabelDeviceOnOffEvent
        if(!CheckAutoFree()){
            int limit = GetLimit();
            if(limit<=0){
                setting.SaveSetting("int", "deviceGbLim", "-1");
                CheckDaysSettings();
            }
            else{
                setting.SaveSetting("int", "deviceGbLim", Integer.toString(limit));
            }
        }
        else{
           JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Deactivate \"Auto Free\"");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
            textFieldStorLimit.setText(""); 
        }
        textFieldStorLimit.setText("");
        CheckDaysSettings();
    }//GEN-LAST:event_labelStorLimitOnOfflabelDeviceOnOffEvent

    private void textFieldStorLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldStorLimitActionPerformed
        setting.SaveSetting("int", "deviceGbLim", "-1");
        CheckDaysSettings();
    }//GEN-LAST:event_textFieldStorLimitActionPerformed

    private void textFieldStorLimitClick(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_textFieldStorLimitClick
        setting.SaveSetting("int", "deviceGbLim", "-1");
        CheckDaysSettings();
    }//GEN-LAST:event_textFieldStorLimitClick

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(DaysLimit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DaysLimit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DaysLimit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DaysLimit.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DaysLimit dialog = new DaysLimit(new javax.swing.JFrame(), true, null);  //to debug set null value to "device"
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    
    
    //***************ICONS DATABASE START***************
    String switchOn= "/SBS/Icons/switch-on-icon-70x30.png";
    String switchOff= "/SBS/Icons/switch-off-icon-70x30.png";
    String errorIcon= "/SBS/Icons/error-logo-50x50.png";
    String warningIcon= "/SBS/Icons/warning_logo_50x45.png";
    //***************ICONS DATABASE END*****************    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel labelDayLimitTxtGB;
    private javax.swing.JLabel labelStorAutoFreeTxt;
    private javax.swing.JLabel labelStorFreeOnOff;
    private javax.swing.JLabel labelStorLimit;
    private javax.swing.JLabel labelStorLimitOnOff;
    private javax.swing.JLabel labelStorLimitOnOffLabel;
    private javax.swing.JLabel labelStorLimitTxt;
    private javax.swing.JLabel labelStorLimitTxtFooter;
    private javax.swing.JTextField textFieldStorLimit;
    // End of variables declaration//GEN-END:variables
}
