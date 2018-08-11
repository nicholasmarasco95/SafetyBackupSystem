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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
public class StartDevice implements Runnable{

    private CopyTmp cpTmp;
    private Settings setting= new Settings();
    private ZipBackup zipDeviceBackup;
    private CopyToDestination copyDest;
    private DaysLimit daysLimit;
    private BackupPath backPath;
    private JProgressBar progressBarTmp;
    private JProgressBar progressBarTotal;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentage;
    private JLabel labelTmpStatusName;
    private JLabel labelStatusTotal;
    //private MountDevice mountClass;
    private LinkedList<Long> timesList;
    private UpdateHistory updateHistory;
    private boolean serverBackup;
    private long backupSize;
    private Set<Path> backupList;
    private JLabel labelLastUpdate;
    private boolean syncFolder;
    
    private String zipNameFile;
    DateTimeFormatter dateForm = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
    LocalDate date;
    
    
    public StartDevice(JProgressBar progressBarTotal, JProgressBar progressBarTmp, JLabel tmpLabelStatus, JLabel labelStatusTotal, JLabel tmpLabelPercentage, JLabel labelTmpStatusName, boolean serverBackup, JLabel labelLastUpdate){
        date= LocalDate.now();
        zipNameFile= "SBS-Backup-" + dateForm.format(date);
        
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.labelTmpStatusName= labelTmpStatusName;
        this.labelStatusTotal= labelStatusTotal;
        this.progressBarTotal= progressBarTotal;
        this.labelLastUpdate= labelLastUpdate;
        syncFolder= setting.getBoolValue("syncDevSrvBackup");
        this.serverBackup= serverBackup;
        this.progressBarTmp= progressBarTmp;
        initializeProgressBar();
        
        backPath= new BackupPath("device");
        backupList= backPath.GetPathSet();
        backupSize= backPath.BackupSize();
        cpTmp= new CopyTmp("device", progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, copyToSBSTotPerc, copyToSBSAddPerc, backupSize, backupList);
        zipDeviceBackup= new ZipBackup("device", progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, zipTotPerc, zipAddPerc, zipNameFile, backupSize);
        copyDest= new CopyToDestination("device", progressBarTotal, progressBarTmp, tmpLabelStatus, labelStatusTotal, tmpLabelPercentage, copyToDestTotPerc, copyToDestAddPerc);
        daysLimit= new DaysLimit(null, true, "device");
        //mountClass= new MountDevice(null, true);
        timesList= new LinkedList<Long>();
    }
    
    @Override
    public void run() {
        
        long totalTimerStart = System.currentTimeMillis();  //total timer start
        setting.SaveSetting("bool", "deviceDone", "false");
        
        //*****************COPY TO SBS FOLDER*****************
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(copyingIcon)));
        labelTmpStatusName.setText("Copying to SBS folder");
        long copyTmpTimer= cpTmp.Copy();  //copy folder to Backup in tmpFolder
        
        
        //*****************ZIP*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(zipIcon)));
        labelTmpStatusName.setText("Zipping");
        tmpLabelPercentage.setText("0%");
        tmpLabelStatus.setText("");
        long zipTimer= zipDeviceBackup.Zip();
        
        /*
        //*****************MOUNT*****************
        if(setting.getBoolValue("deviceMountSet")){
            mountClass.Mount();
        }
        */

        //*****************FREE DESTINATION*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setText("Freeing destination");
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(freeIcon)));
        File zipFile= new File(zipDeviceBackup.GetFolder());
        progressBarTmp.setValue(50);
        long zipFileSize= zipFile.length();
        daysLimit.deleteFilesLimit(zipFileSize);
        progressBarTmp.setValue(100);
        progressBarTotal.setValue(freeAddPerc + freeTotPerc);
        
        
        //*****************COPY TO DESTINATION*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(copyingDestIcon)));
        labelTmpStatusName.setText("Copying to Destination folder");
        tmpLabelPercentage.setText("0%");
        tmpLabelStatus.setText("");
        long copyDestTimer= copyDest.Copy();
        
        /*
        //*****************UNMOUNT*****************
        if(setting.getBoolValue("deviceMountSet")){
            mountClass.Unmount();
        }
        */
        
        
        //*****************CLEANING*****************
        if(!serverBackup || !syncFolder){
            progressBarTmp.setValue(0);
            labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(cleanIcon)));
            labelTmpStatusName.setText("Cleaning files");
            tmpLabelPercentage.setText("0%");
            tmpLabelStatus.setText("");
            cleanFolder();
        }
        
        long totalTimerEnd = System.currentTimeMillis() - totalTimerStart;  //total timer End
        
        
        //*****************UPDATE HISTORY*****************
        if(serverBackup && syncFolder){
            setting.SaveSetting("long", "deviceTotalTimer", Long.toString(totalTimerEnd));
        }
        else{
            updateHistory= new UpdateHistory(totalTimerEnd, -1, true, false, zipNameFile, dateForm.format(date), backupSize);
            updateHistory.updateDeviceHistory();
        }
        if(!serverBackup){
            progressBarTotal.setValue(100);
            labelStatusTotal.setText("100%");
        }
        
        //*****************COPY HISTORY*****************
        if(!serverBackup && setting.getBoolValue("copyHistoryActive")){
            Path srcFile= Paths.get("./SBS-History.csv");
            Path destPath= Paths.get(setting.getStingValue("copyHistoryPath") + "\\SBS-History.csv");
            try {
                Files.copy(srcFile, destPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(StartServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //+++++++++++++++DEBUG START+++++++++++++++
        //JOptionPane.showMessageDialog(null, "Done!");
        //+++++++++++++++DEBUG END+++++++++++++++++
        
        
        //*****************Done System Tray*****************
        if(!serverBackup && !setting.getBoolValue("sbsBackupDoneTray")){
            BackupCompletedTray bctClass= new BackupCompletedTray(null, true);
            Thread bctThread= new Thread(bctClass);
            bctThread.start();
        }
        
        //*****************Set Back progressBar*****************
        progressBarTmp.setValue(0);
        labelTmpStatusName.setIcon(new ImageIcon(getClass().getResource(hourglassIcon)));
        labelTmpStatusName.setText("Temporary Status");
        if(!serverBackup){
           progressBarTotal.setValue(0);
           //update Last Backup Label
           String lastUpdate= setting.getStingValue("lastBackup");
            if(lastUpdate!=null && lastUpdate!=""){
            labelLastUpdate.setText("Last Update: " + lastUpdate);
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
                    Process proc = runtime.exec("shutdown -r");
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
        
        if(serverBackup){
            setting.SaveSetting("bool", "deviceDone", "true");
            setting.SaveSetting("string", "deviceFileName", zipFile.toString());
        }
    }
    
    public List<Long> getListTimes(){
        return timesList;
    }
    
    private void cleanFolder(){
    progressBarTmp.setValue(25);
    tmpLabelPercentage.setText("25%");
    progressBarTotal.setValue(cleanAddPerc);
    labelStatusTotal.setText(Integer.toString(cleanAddPerc) + "%");
    try{
        File foldToDel= new File("deviceTmpCopy");
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
    progressBarTotal.setValue(otherAddPerc);
    labelStatusTotal.setText(Integer.toString(otherAddPerc) + "%");
    }
    
    
    
    //***************ICONS DATABASE START***************
    String copyingIcon= "/SBS/Icons/copying-icon-40x40.png";
    String copyingDestIcon= "/SBS/Icons/copying-icon-2-40x40.png";
    String zipIcon= "/SBS/Icons/zip-icon-40x40.png";
    String cleanIcon= "/SBS/Icons/clean-erase-logo-40x40.png";
    String freeIcon= "/SBS/Icons/recycle-bin-icon-35x40.png";
    String hourglassIcon= "/SBS/Icons/hourglass-icon-35x40.png";
    String warningIcon= "/SBS/Icons/warning_logo_50x45.png";
    String exclamationIcon= "/SBS/Icons/Exclamation-point-info-icon-50x50.png";
                
    //new ImageIcon(getClass().getResource(ICON))
    //***************ICONS DATABASE END*****************
        

    //***************TOTAL PROGRESS BAR DATABASE START***************
    int copyToSBSTotPerc;
    int zipTotPerc;
    int freeTotPerc;
    int copyToDestTotPerc;
    int cleanTotPerc;
    int otherTotPerc;
    
    int copyToSBSAddPerc;
    int zipAddPerc;
    int freeAddPerc;
    int copyToDestAddPerc;
    int cleanAddPerc;
    int otherAddPerc;
    //***************TOTAL PROGRESS BAR DATABASE END*****************
    
    private void initializeProgressBar(){
        if(serverBackup && syncFolder){
            copyToSBSTotPerc= 20;
            zipTotPerc= 30;
            freeTotPerc= 5;
            copyToDestTotPerc= 15;
            //cleanTotPerc;  skip
            //otherTotPerc;  skip   

            copyToSBSAddPerc= 0;
            zipAddPerc= copyToSBSTotPerc;
            freeAddPerc= zipAddPerc + zipTotPerc;
            copyToDestAddPerc= freeAddPerc + freeTotPerc;
            
            //cleanAddPerc= copyToDestAddPerc + copyToDestTotPerc;  //skip
            //otherAddPerc= cleanAddPerc + otherTotPerc;  //skip
        }
        else if(serverBackup && !syncFolder){
            copyToSBSTotPerc= 14;
            zipTotPerc= 20;
            freeTotPerc= 2;
            copyToDestTotPerc= 10;
            cleanTotPerc= 2;  
            otherTotPerc= 2;   

            copyToSBSAddPerc= 0;
            zipAddPerc= copyToSBSTotPerc;
            freeAddPerc= zipAddPerc + zipTotPerc;
            copyToDestAddPerc= freeAddPerc + freeTotPerc;
            cleanAddPerc= copyToDestAddPerc + copyToDestTotPerc;
            otherAddPerc= cleanAddPerc + otherTotPerc;
        }
        else{
            copyToSBSTotPerc= 25;
            zipTotPerc= 40;
            freeTotPerc= 5;
            copyToDestTotPerc= 20;
            cleanTotPerc= 5;
            otherTotPerc= 5;

            copyToSBSAddPerc= 0;
            zipAddPerc= copyToSBSTotPerc;
            freeAddPerc= zipAddPerc + zipTotPerc;
            copyToDestAddPerc= freeAddPerc + freeTotPerc;
            cleanAddPerc= copyToDestAddPerc + copyToDestTotPerc;
            otherAddPerc= cleanAddPerc + otherTotPerc;
        }
    }
    
}
