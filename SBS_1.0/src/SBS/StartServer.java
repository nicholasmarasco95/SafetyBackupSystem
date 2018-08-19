/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author nicho
 */
public class StartServer implements Runnable{
    
    private CopyTmp cpTmp;
    private Settings setting= new Settings();
    private ZipBackup zipDeviceBackup;
    private BackupPath backPath;
    private JProgressBar progressBarTmp;
    private JProgressBar progressBarTotal;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentage;
    private JLabel labelTmpStatusName;
    private JLabel labelStatusTotal;
    private LinkedList<Long> timesList;
    private UpdateHistory updateHistory;
    private boolean deviceBackup;
    private long backupSize;
    private Set<Path> backupList;
    private JLabel labelLastUpdate;
    private String filePath;
    private SendFile sendFile;
    private String fileName;  //used by SendFile class only
    private int fileSize;
    private boolean syncFolder;
    
    DateTimeFormatter dateForm = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
    LocalDate date;
    
    public StartServer(JProgressBar progressBarTotal, JProgressBar progressBarTmp, JLabel tmpLabelStatus, JLabel labelStatusTotal, JLabel tmpLabelPercentage, JLabel labelTmpStatusName, boolean deviceBackup, JLabel labelLastUpdate){
        syncFolder= setting.getBoolValue("syncDevSrvBackup");
        this.progressBarTmp= progressBarTmp;
        timesList= new LinkedList<Long>();
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.labelTmpStatusName= labelTmpStatusName;
        this.labelStatusTotal= labelStatusTotal;
        this.progressBarTotal= progressBarTotal;
        this.deviceBackup= deviceBackup;
        this.labelLastUpdate= labelLastUpdate;
        initializeProgressBar();
        date= LocalDate.now();
        fileName= "SBS-Backup-" + dateForm.format(date);
        if(!deviceBackup || !syncFolder){
            fileName= fileName+".zip";  //debug
            filePath= "TmpZip"+"\\"+fileName+".zip";
            backPath= new BackupPath("server");
            backupList= backPath.GetPathSet();
            backupSize= backPath.BackupSize();
            cpTmp= new CopyTmp("server", progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, copyToSBSTotPerc, copyToSBSAddPerc, backupSize, backupList);
        }
    }

    @Override
    public void run() {
        
        long totalTimerStart = System.currentTimeMillis();  //total timer start
        
        if(!deviceBackup || !syncFolder){
            if(deviceBackup) waitDeviceBackup();
            
            //*****************COPY TO SBS FOLDER*****************
            labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(copyingIcon)));
            labelTmpStatusName.setText("Copying to SBS folder");
            long copySrvTmpTimer= cpTmp.Copy();  //copy folder to Backup in tmpFolder
            
            //*****************ZIP*****************
            progressBarTmp.setValue(0);
            labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(zipIcon)));
            labelTmpStatusName.setText("Zipping");
            tmpLabelPercentage.setText("0%");
            tmpLabelStatus.setText("");
            zipDeviceBackup= new ZipBackup("server", progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, zipTotPerc, zipAddPerc, fileName, backupSize);
            long zipSrvTimer= zipDeviceBackup.Zip();
            
            fileSize= (int) new File(filePath).length();
            sendFile= new SendFile(fileName, fileSize, progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, uploadZipTot, uploadAddPerc);
        }
        
        else if(deviceBackup){
            waitDeviceBackup();
            filePath= setting.getStingValue("deviceFileName");
            int index= filePath.lastIndexOf('\\');
            fileName= filePath.substring(index+1);
            fileSize= (int) new File(filePath).length();
            backupSize= fileSize;
            sendFile= new SendFile(fileName, fileSize, progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, uploadZipTot, uploadAddPerc);
        }
        
        //*****************UPLOAD*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setText("Uploading");
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(uploadIcon)));
        tmpLabelPercentage.setText("0%");
        tmpLabelStatus.setText("");
        
        long uploadTimer= sendFile.upload(filePath);
        
        //*****************CLEANING*****************
            progressBarTmp.setValue(0);
            labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(cleanIcon)));
            labelTmpStatusName.setText("Cleaning files");
            tmpLabelPercentage.setText("0%");
            tmpLabelStatus.setText("");
            cleanFolder();
            
            
        long totalSrvTimerEnd = System.currentTimeMillis() - totalTimerStart;  //total timer End
        
            
        //*****************UPDATE HISTORY*****************
        if(deviceBackup){
           long deviceTotal= setting.getLongValue("deviceTotalTimer");
           long totalTimerEnd= totalSrvTimerEnd+deviceTotal;
           fileName= fileName.replace(".zip", "");
           updateHistory= new UpdateHistory(totalTimerEnd, uploadTimer, true, true, fileName, dateForm.format(date), backupSize);
           updateHistory.updateDeviceHistory();
        }
        else{
           fileName= fileName.replace(".zip", "");
           updateHistory= new UpdateHistory(totalSrvTimerEnd, uploadTimer, false, true, fileName, dateForm.format(date), backupSize);
           updateHistory.updateDeviceHistory();
        }
        progressBarTotal.setValue(100);
        labelStatusTotal.setText("100%");
        
        
        //*****************COPY HISTORY*****************
        if(setting.getBoolValue("copyHistoryActive")){
            Path srcFile= Paths.get("./SBS-History.csv");
            Path destPath= Paths.get(setting.getStingValue("copyHistoryPath") + "\\SBS-History.csv");
            try {
                Files.copy(srcFile, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(StartServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //*****************Set Back progressBar*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(hourglassIcon)));
        labelTmpStatusName.setText("Temporary Status");
        String lastUpdate= setting.getStingValue("lastBackup");
        progressBarTotal.setValue(0);
        labelStatusTotal.setText("0%");
        tmpLabelPercentage.setText("0%");
        if(lastUpdate!=null && lastUpdate!=""){
            labelLastUpdate.setText("Last Update: " + lastUpdate);
        }
        
        //*****************Done System Tray*****************
        if(!setting.getBoolValue("sbsBackupDoneTray")){
            BackupCompletedTray bctClass= new BackupCompletedTray(null, true);
            Thread bctThread= new Thread(bctClass);
            bctThread.start();
        }
        
        //shutdown-restart PC
        if(setting.getBoolValue("shutdownPc")){
            try{
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("shutdown -s");
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Pc will shutdown in 1 minute, press no to cancel");
                int ans= JOptionPane.showConfirmDialog(null, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
                if(ans==JOptionPane.NO_OPTION){
                    proc = runtime.exec("shutdown -a");
                    JLabel msgLabel2= new JLabel();
                    msgLabel2.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                    msgLabel2.setText("Shutdown cancelled");
                    JOptionPane.showMessageDialog(null, msgLabel2, null, HEIGHT, new ImageIcon(getClass().getResource(exclamationIcon)));
                }
            } catch (IOException ex) {
               Logger.getLogger(StartDevice.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
        else if(setting.getBoolValue("restartPc")){
            try{
                Runtime runtime = Runtime.getRuntime();
                Process proc = runtime.exec("shutdown -s");
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Pc will restart in 1 minute, press no to cancel");
                int ans= JOptionPane.showConfirmDialog(null, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
                if(ans==JOptionPane.NO_OPTION){
                    proc = runtime.exec("shutdown -a");
                    JLabel msgLabel2= new JLabel();
                    msgLabel2.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                    msgLabel2.setText("Restart cancelled");
                    JOptionPane.showMessageDialog(null, msgLabel2, null, HEIGHT, new ImageIcon(getClass().getResource(exclamationIcon)));
                }
            } catch (IOException ex) {
               Logger.getLogger(StartDevice.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
        
    }
    
    //***************ICONS DATABASE START***************
            String copyingIcon= "/SBS/Icons/copying-icon-40x40.png";
            String copyingDestIcon= "/SBS/Icons/copying-icon-2-40x40.png";
            String zipIcon= "/SBS/Icons/zip-icon-40x40.png";
            String cleanIcon= "/SBS/Icons/clean-erase-logo-40x40.png";
            String freeIcon= "/SBS/Icons/recycle-bin-icon-35x40.png";
            String hourglassIcon= "/SBS/Icons/hourglass-icon-35x40.png";
            String uploadIcon= "/SBS/Icons/folder-upload-server-55x40.png";
            String warningIcon= "/SBS/Icons/warning_logo_50x45.png";
            String exclamationIcon= "/SBS/Icons/Exclamation-point-info-icon-50x50.png";

            //new ImageIcon(getClass().getResource(ICON))
    //***************ICONS DATABASE END*****************
            
    
    //***************TOTAL PROGRESS BAR DATABASE START***************
            int deviceBackupPerc;
            int copyToSBSTotPerc;
            int zipTotPerc;
            int uploadZipTot;
            int cleanTotPerc;
            int otherTotPerc;

            int uploadAddPerc;
            int copyToSBSAddPerc;
            int zipAddPerc;
            int cleanAddPerc;
            int otherAddPerc;
    //***************TOTAL PROGRESS BAR DATABASE END*****************
            
    private void initializeProgressBar(){
        if(deviceBackup && syncFolder){
                deviceBackupPerc = 70;
                uploadZipTot= 25;
                cleanTotPerc= 3;
                otherTotPerc= 2;

                uploadAddPerc= deviceBackupPerc;
                cleanAddPerc= uploadAddPerc + uploadZipTot + cleanTotPerc;
                otherAddPerc= cleanAddPerc + otherTotPerc;
        }
        else if(deviceBackup && !syncFolder){
            deviceBackupPerc = 50;
            copyToSBSTotPerc= 10;
            zipTotPerc= 16;
            uploadZipTot= 20;
            cleanTotPerc= 2;
            otherTotPerc= 2;                                              
            
            copyToSBSAddPerc= deviceBackupPerc;
            zipAddPerc= copyToSBSTotPerc + deviceBackupPerc;
            uploadAddPerc= zipAddPerc + uploadZipTot;
            cleanAddPerc= uploadAddPerc + uploadZipTot + cleanTotPerc;
            otherAddPerc= cleanAddPerc + otherTotPerc;                    
        }
        else{
            copyToSBSTotPerc= 20;
            zipTotPerc= 30;
            uploadZipTot= 40;
            cleanTotPerc= 5;
            otherTotPerc= 5;
            
            copyToSBSAddPerc= 0;
            zipAddPerc= copyToSBSTotPerc;
            uploadAddPerc= zipAddPerc + zipTotPerc;
            cleanAddPerc= uploadAddPerc + cleanTotPerc + uploadZipTot;
            otherAddPerc= cleanAddPerc + otherTotPerc;
        }
    }
    
    
    private void cleanFolder(){
        String cleanFold;
        if(deviceBackup && syncFolder) cleanFold= "deviceTmpCopy";
        else cleanFold= "serverTmpCopy";
        progressBarTmp.setValue(25);
        tmpLabelPercentage.setText("25%");
        progressBarTotal.setValue(cleanAddPerc);
        labelStatusTotal.setText(Integer.toString(cleanAddPerc) + "%");
        try{
            File foldToDel= new File(cleanFold);
            if(foldToDel.exists()){
                FileUtils.deleteDirectory(foldToDel);
            }
            progressBarTmp.setValue(50);
            tmpLabelPercentage.setText("50%");
            foldToDel= new File("TmpZip");
            if(foldToDel.exists()){
                FileUtils.deleteDirectory(foldToDel);
            }
            progressBarTmp.setValue(75);
            tmpLabelPercentage.setText("75%");
        } catch (IOException ex) {
                Logger.getLogger(StartDevice.class.getName()).log(Level.SEVERE, null, ex);
            }
        progressBarTmp.setValue(100);
        tmpLabelPercentage.setText("100%");
        progressBarTotal.setValue(cleanAddPerc);
        labelStatusTotal.setText(Integer.toString(cleanAddPerc) + "%");
    }
    
    private void waitDeviceBackup(){
        while(!setting.getBoolValue("deviceDone")){
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(StartServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
    }
}
