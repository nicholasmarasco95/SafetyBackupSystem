/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.Color;
import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author nicho
 */
public class MountDevice extends javax.swing.JDialog {
    
    private Settings setting= new Settings();
    private String gui;

    public MountDevice(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        refresh();
    }
    
    
    private void refresh(){
        if(setting.getBoolValue("deviceMountSet")){
            labelMessage.setText("");
            labelStatus.setText("Ready");
            labelOnOff.setIcon(new ImageIcon(getClass().getResource(switchOn)));
            labelStatus.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            if(isMounted()){
                btnMount.setEnabled(false);
                btnUnmount.setEnabled(true);
            }
            else if(isConnected()){
                btnMount.setEnabled(true);
                btnUnmount.setEnabled(false);
            }
            return;
        }
        
        if(!isDevice()){
            labelMessage.setText("Destination is not a device");
            labelStatus.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelStatus.setText("Not Ready");
            labelOnOff.setForeground(Color.gray);
            labelOnOff.setIcon(new ImageIcon(getClass().getResource(switchOnOffGrayed)));
            btnMount.setEnabled(false);
            btnUnmount.setEnabled(false);
            labelOnOff.setIcon(new ImageIcon(getClass().getResource(switchOff)));
            return;
        }
        
        labelMessage.setText("");
        labelStatus.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        labelOnOff.setIcon(new ImageIcon(getClass().getResource(switchOff)));
        labelStatus.setText("Ready");
        btnMount.setEnabled(false);
        btnUnmount.setEnabled(false);
    }
    
    
    private static boolean isAdmin() {
        //https://stackoverflow.com/questions/10643451/how-to-determine-if-java-program-is-running-as-administrator-at-run-time
        try {
        String command = "reg query \"HKU\\S-1-5-19\"";
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();                            // Wait for for command to finish
        int exitValue = p.exitValue();          // If exit value 0, then admin user.

        if (0 == exitValue) {
            return true;
        } else {
            return false;
        }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    private boolean isDevice(){
        String tmpLetter= setting.getStingValue("deviceFoldDest");
        if(!tmpLetter.equals("") || tmpLetter!=null){
            tmpLetter= tmpLetter.substring(0, 1);
        }
        if(tmpLetter.equals("C")){
            return false;
        }
        return true;
    }
    
    
    private boolean isMounted(){
        Path path= Paths.get(setting.getStingValue("deviceFoldDest"));
        if(Files.exists(path)){
            return true;
        }
        return false;
    }
    
    
    private boolean isConnected(){
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mountvol");
            proc.waitFor(); 
            BufferedReader reader=new BufferedReader( new InputStreamReader(proc.getInputStream())); 
            String line; 
            while((line = reader.readLine()) != null){
                if(line.contains("Possible values for VolumeName along with current mount points are:")){
                    break;
                }
                //throw lines before device info
            }
            reader.readLine(); //throw empty line
            while((line = reader.readLine()) != null){
                line= line.replaceAll("\\s+","");
                String tmpGui= line;
                if(tmpGui.equals(setting.getStingValue("deviceGuiMount"))){
                    return true;
                }
                //reader.readLine(); //throw empty line
            }
        } catch (IOException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    
    private void getGUI(){
        String tmpLetter= setting.getStingValue("deviceFoldDest");
        if(!tmpLetter.equals("") || tmpLetter!=null){
            tmpLetter= tmpLetter.substring(0, 1);
        }
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mountvol");
            proc.waitFor(); 
            BufferedReader reader=new BufferedReader( new InputStreamReader(proc.getInputStream())); 
            String line; 
            while((line = reader.readLine()) != null){
                if(line.contains("Possible values for VolumeName along with current mount points are:")){
                    break;
                }
                //throw lines before device info
            }
            reader.readLine(); //throw empty line
            while((line = reader.readLine()) != null){
                line= line.replaceAll("\\s+","");
                gui= line;
                line= reader.readLine();
                if(!line.contains("C")){
                    if(!line.contains("*")){
                        line= line.replaceAll("\\s+","");
                        line= line.substring(0, 1);
                        if(line.equals(tmpLetter)){
                            setting.SaveSetting("string", "deviceOldMountLet", tmpLetter);
                            setting.SaveSetting("string", "deviceGuiMount", gui);
                        }
                        break;
                    }
                }
                reader.readLine(); //throw empty line
            }
        } catch (IOException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private boolean mountLetterAvailable(String toCheckLetter){
        try{
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mountvol");
            proc.waitFor(); 
            BufferedReader reader=new BufferedReader( new InputStreamReader(proc.getInputStream())); 
            String line; 
            while((line = reader.readLine()) != null){
                if(line.contains("Possible values for VolumeName along with current mount points are:")){
                    break;
                }
                //throw lines before device info
            }
            reader.readLine(); //throw empty line
            while((line = reader.readLine()) != null){
                line= line.replaceAll("\\s+","");
                gui= line;
                line= reader.readLine();
                if(!line.contains("C")){
                    if(!line.contains("*")){
                        line= line.replaceAll("\\s+","");
                        line= line.substring(0, 1);
                        if(line.equals(toCheckLetter)){
                            return false;
                        }
                    }
                }
                reader.readLine(); //throw empty line
            }
        } catch (IOException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    
    private String getAvailableLetter(){
        String tmpLetter= setting.getStingValue("deviceOldMountLet");
        while(!mountLetterAvailable(tmpLetter)){
            char tmpLetterChar= tmpLetter.charAt(0);
            int tmpAscii= (int) tmpLetterChar;
            tmpAscii++;
            tmpLetterChar= (char) tmpAscii;
            tmpLetter= Character.toString(tmpLetterChar);
        }
        return tmpLetter;
    }
    
    
    public void resetMount(){
        String newLetter= getAvailableLetter();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc;
            proc = rt.exec("mountvol " + newLetter + ":" + " " + setting.getStingValue("deviceGuiMount"));
            proc.waitFor(); 
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void Unmount(){
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc;
            proc = rt.exec("mountvol " + "B" + ":" + " /D");
            proc.waitFor();
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void Mount(){
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc;
            proc = rt.exec("mountvol " + "B" + ":" + " " + setting.getStingValue("deviceGuiMount"));
            proc.waitFor(); 
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void firstUnmount(){
        String tmpLetter= setting.getStingValue("deviceFoldDest");
        if(!tmpLetter.equals("") || tmpLetter!=null){
            tmpLetter= tmpLetter.substring(0, 1);
        }
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc;
            proc = rt.exec("mountvol " + tmpLetter + ":" + " /D");
            proc.waitFor();
            changeDestination();   //change destination
        } catch (IOException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            System.err.println(ex);
            Logger.getLogger(MountDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void changeDestination(){
        String tmpDest= setting.getStingValue("deviceFoldDest");
        tmpDest= tmpDest.substring(1);
        setting.SaveSetting("string", "deviceFoldDest", "B" + tmpDest);
    }
    
    
    
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelStatusTxt = new javax.swing.JLabel();
        labelStatus = new javax.swing.JLabel();
        labelOnOff = new javax.swing.JLabel();
        btnMount = new javax.swing.JButton();
        btnUnmount = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnClose = new javax.swing.JButton();
        labelMessage = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        labelStatusTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelStatusTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/info-icon-50x50.png"))); // NOI18N
        labelStatusTxt.setText("Status:");

        labelStatus.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-red-logo-30x30.png"))); // NOI18N
        labelStatus.setText("Not Ready");

        labelOnOff.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-off-icon-70x30.png"))); // NOI18N
        labelOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                OnOffEvents(evt);
            }
        });

        btnMount.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnMount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/arrow-blue-30x30.png"))); // NOI18N
        btnMount.setText("Mount");
        btnMount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMountActionPerformed(evt);
            }
        });

        btnUnmount.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnUnmount.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/arrow-red-30x30.png"))); // NOI18N
        btnUnmount.setText("Unmount");
        btnUnmount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUnmountActionPerformed(evt);
            }
        });

        btnClose.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnClose.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/close-icon-30x30.png"))); // NOI18N
        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        labelMessage.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        labelMessage.setText("Destination is not a device");

        jLabel1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        jLabel1.setText("On/Off");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnMount, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnUnmount, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(labelMessage))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelStatusTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelStatus)
                        .addGap(26, 26, 26)
                        .addComponent(labelOnOff)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(labelOnOff)
                            .addComponent(jLabel1)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelStatusTxt)
                            .addComponent(labelStatus))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUnmount, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnMount, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnMountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMountActionPerformed
        Mount();
        refresh();
    }//GEN-LAST:event_btnMountActionPerformed

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        dispose();
    }//GEN-LAST:event_btnCloseActionPerformed

    private void OnOffEvents(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_OnOffEvents
        boolean active= setting.getBoolValue("deviceMountSet");
        if(active){
            resetMount();
            setting.SaveSetting("boolean", "deviceMountSet", "false");
        }
        else{
            if(isDevice()){
                if(!isAdmin()){
                    JLabel msgLabel= new JLabel();
                    msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                    msgLabel.setText("Mount/Unmount feature works with Admin privileges only");
                    JOptionPane.showMessageDialog(rootPane, msgLabel, "", HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
                }
                else{
                    if(setting.getBoolValue("autopilotActive") || setting.getBoolValue("autoStart")){
                        JLabel msgLabel= new JLabel();
                        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                        msgLabel.setText("Autopilot and Autostart feature will be deactivated");
                        JOptionPane.showMessageDialog(rootPane, msgLabel, "", HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
                        setting.SaveSetting("boolean", "autopilotActive", "false");
                        setting.SaveSetting("boolean", "autoStart", "false");
                    }
                    JLabel msgLabel= new JLabel();
                    msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                    msgLabel.setText("This device will be used by this application only. Do you want activate Mount/Unmount?");
                    int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
                    if(ans==JOptionPane.YES_OPTION){
                        setting.SaveSetting("boolean", "deviceMountSet", "true");
                        JLabel msgLabel2= new JLabel();
                        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                        msgLabel.setText("Don't remove device before deactivate the option");
                        JOptionPane.showMessageDialog(rootPane, msgLabel, "", HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));

                        getGUI();
                        firstUnmount();
                    }
                }
            }
        }
        refresh();
    }//GEN-LAST:event_OnOffEvents

    private void btnUnmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUnmountActionPerformed
        Unmount();
        refresh();
    }//GEN-LAST:event_btnUnmountActionPerformed

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
            java.util.logging.Logger.getLogger(MountDevice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MountDevice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MountDevice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MountDevice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MountDevice dialog = new MountDevice(new javax.swing.JFrame(), true);
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
        String trafficGreenBulb= "/SBS/Icons/Traffic-light-green-logo-30x30.png";
        String trafficRedBulb= "/SBS/Icons/Traffic-light-red-logo-30x30.png";
        String switchOnOffGrayed= "/SBS/Icons/switch-off-icon-grayed_out-70x30.png";
        String switchOn= "/SBS/Icons/switch-on-icon-70x30.png";
        String switchOff= "/SBS/Icons/switch-off-icon-70x30.png";
        String warningIcon= "/SBS/Icons/warning_logo_50x45.png";
    //***************ICONS DATABASE END*****************

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnMount;
    private javax.swing.JButton btnUnmount;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel labelMessage;
    private javax.swing.JLabel labelOnOff;
    private javax.swing.JLabel labelStatus;
    private javax.swing.JLabel labelStatusTxt;
    // End of variables declaration//GEN-END:variables
}
