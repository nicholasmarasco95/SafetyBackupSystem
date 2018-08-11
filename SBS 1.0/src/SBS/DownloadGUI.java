/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nicho
 */
public class DownloadGUI extends javax.swing.JDialog {
    
    private Settings setting= new Settings();
    String ip;
    int port;
    String userId;
    String pass;
    String mac;
    SimpleDateFormat dateForm = new SimpleDateFormat("MMM-dd-yyyy");
    private String selFile= null;

    /**
     * Creates new form DownloadGUI
     */
    public DownloadGUI(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ip= setting.getStingValue("serverIp");
        port= Integer.parseInt(setting.getStingValue("serverPort"));
        userId= setting.getStingValue("usrCode");
        pass= setting.getStingValue("usrPass");
        mac= getMacAddress();
        labelFileNum.setText("");
        btnDownlaod.setEnabled(false);
        this.setTitle("Download Backup");
    }
    
    private void updateTable(String list){
        String[] filesArray= list.split("~");
        int rowCounter=0;
        int rowNum= tableFiles.getRowCount();
        if(filesArray.length>rowNum){
            DefaultTableModel model = (DefaultTableModel) tableFiles.getModel();
            model.setRowCount(filesArray.length);
        }
        for(int i=0; i<filesArray.length; i++){
            String tmpArray[]= filesArray[i].split(",");
            tableFiles.getModel().setValueAt(tmpArray[0], rowCounter, 0);
            tableFiles.getModel().setValueAt(convertDate(new Long(tmpArray[1].replaceAll("\\s","")).longValue()), rowCounter, 1);
            tableFiles.getModel().setValueAt(humanReadableByteCount(Long.parseLong(tmpArray[2].replaceAll("\\s","")), false), rowCounter, 2);
            rowCounter++;
        }
        labelFileNum.setText(Integer.toString(filesArray.length));
    }
    
    private String convertDate(long longTime){
        Date date= new Date(longTime);
        return dateForm.format(date);
    }
    
    private static String humanReadableByteCount(long bytes, boolean si) {
        //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    private String getFileListServer(){
        String fileList= "";
        String action= "sendList";  //user wants list only
        try{
            Socket sock= new Socket(ip, port);
            OutputStream os= sock.getOutputStream();
            BufferedReader br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter pw= new PrintWriter(os, true);
            pw.println(action+","+userId+","+pass+","+mac);  //info to sent to server (in this order)
            br.readLine(); //throw expire date message
            String msgIn= br.readLine();
            if(msgIn.equals("usnotreg")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User Not Registed");
                JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
                this.dispose();
            }
            if(msgIn.equals("uspasserr")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Wrong Password");
                JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
                this.dispose();
            }
            if(msgIn.equals("usisexp")){
                //do nothing, user can download his files
            }
            if(msgIn.equals("usrmacerr")){
                //do nothing, user can download his files
            }
            else{
                fileList= msgIn;
            }
        } catch (IOException ex) {
            Logger.getLogger(DownloadGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return fileList;
    }
    
    private String getMacAddress(){
        try {
            InetAddress addr = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
            if (ni == null)
                return null;
            
            byte[] mac = ni.getHardwareAddress();
            if (mac == null)
                return null;
            
            StringBuilder sb = new StringBuilder(18);
            for (byte b : mac) {
                if (sb.length() > 0)
                    sb.append(':');
                sb.append(String.format("%02x", b));
            }
            setting.SaveSetting("string", "macAddr", sb.toString());
            return sb.toString();
        } catch (SocketException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableFiles = new javax.swing.JTable();
        btnRefresh = new javax.swing.JButton();
        btnClose2 = new javax.swing.JButton();
        btnDownlaod = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        labelFileNum = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabel1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/list-icon-30x30.png"))); // NOI18N
        jLabel1.setText("File List");

        tableFiles.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        tableFiles.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Name", "Date", "Size"
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
        tableFiles.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableFilesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tableFiles);
        if (tableFiles.getColumnModel().getColumnCount() > 0) {
            tableFiles.getColumnModel().getColumn(0).setPreferredWidth(150);
            tableFiles.getColumnModel().getColumn(1).setPreferredWidth(30);
            tableFiles.getColumnModel().getColumn(2).setPreferredWidth(10);
        }

        btnRefresh.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/refresh-icon-30x30.png"))); // NOI18N
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        btnClose2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnClose2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/close-icon-30x30.png"))); // NOI18N
        btnClose2.setText("Close");
        btnClose2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClose2ActionPerformed(evt);
            }
        });

        btnDownlaod.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnDownlaod.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/download-backup-drive-icon-30x30.png"))); // NOI18N
        btnDownlaod.setText("Download");
        btnDownlaod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownlaodActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/folder-icon-30x30.png"))); // NOI18N
        jLabel2.setText("Number of Files:");

        labelFileNum.setFont(new java.awt.Font("Microsoft New Tai Lue", 1, 16)); // NOI18N
        labelFileNum.setText("000");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnRefresh))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(labelFileNum)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnClose2)
                        .addGap(18, 18, 18)
                        .addComponent(btnDownlaod)))
                .addContainerGap())
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1))
                    .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnClose2)
                    .addComponent(btnDownlaod)
                    .addComponent(jLabel2)
                    .addComponent(labelFileNum))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        String fileList= getFileListServer();
        updateTable(fileList);
        System.out.println("File List: " + fileList); //debug
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnClose2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClose2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_btnClose2ActionPerformed

    private void btnDownlaodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownlaodActionPerformed
        DestinationFolder destFold= new DestinationFolder(null, true, "download");
        destFold.setVisible(true);
        if(setting.getStingValue("downloadFoldDest").length()>2){
            DownloadStatusGUI downStatusGui= new DownloadStatusGUI(null, true, selFile);
            downStatusGui.setVisible(true);
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("No Destination folder");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
    }//GEN-LAST:event_btnDownlaodActionPerformed

    private void tableFilesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tableFilesMouseClicked
        int selRow= tableFiles.getSelectedRow();
        selFile= tableFiles.getValueAt(selRow, 0).toString();
        btnDownlaod.setEnabled(true);
    }//GEN-LAST:event_tableFilesMouseClicked

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
            java.util.logging.Logger.getLogger(DownloadGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(DownloadGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(DownloadGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(DownloadGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                DownloadGUI dialog = new DownloadGUI(new javax.swing.JFrame(), true);
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
    String listIcon= "/SBS/Icons/list-icon-30x30.png";
    String errorIcon= "/SBS/Icons/error-logo-50x50.png";
    
        //new ImageIcon(getClass().getResource(ICON))
    //***************ICONS DATABASE END*****************

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClose2;
    private javax.swing.JButton btnDownlaod;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel labelFileNum;
    private javax.swing.JTable tableFiles;
    // End of variables declaration//GEN-END:variables
}
