/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import static java.awt.image.ImageObserver.HEIGHT;
import static java.awt.image.ImageObserver.WIDTH;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.LinkOption;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Timer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.ContinuousAudioDataStream;

/**
 *
 * @author nicho
 */
public class MainGUI extends javax.swing.JFrame {

    private BackupPath devBackPath= new BackupPath("device");
    private BackupPath srvBackPath= new BackupPath("server");
    private Settings setting= new Settings();
    
    public boolean threadCheckErr;  //used by SrvInfoCheckThread class
    
    public MainGUI() {
        initComponents();
        if(!setting.getBoolValue("startMinimized")) checkPassword();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        devCheckSettings();
        this.setTitle("SBS 1.0");
        
        labelError.setText("");
        lockLabel.setText("");
        
        //Default Settings
        setting.SaveSetting("string", "softwareName", "SBS_1.0.jar");
        setting.SaveSetting("string", "shortcutName", "./SBS_1.0.lnk");
        setting.SaveSetting("string", "defaultServerUrl", "http://www.sbs.com/");
        setting.SaveSetting("bool", "deviceDone", "false");
        
        setting.SaveSetting("int", "checkInternetSem", "0");
        setting.SaveSetting("int", "checkConnSem", "0");
        
        if(!setting.getBoolValue("firstInstallation")) firstTimeReset();
        
        labelAudio.setIcon(null);  //to delete autopilot audio icon
        
        if(setting.getStingValue("lastBackup")!=null && !setting.getBoolValue("sbsLastBackupTray") && setting.getStingValue("lastBackup").length()>2){
            LastBackupTray lbtClass= new LastBackupTray(this, true);
            Thread lbtThread= new Thread(lbtClass);
            lbtThread.start();
        }
        
        SystemTrayMethod();
        
        mainCheckSettings(false);
        
        if(setting.getBoolValue("startMinimized")){
            SwingUtilities.getWindowAncestor(this).setVisible(false);
        }
        //SwingUtilities.getWindowAncestor(this).setIconImage(new ImageIcon(getClass().getClassLoader().getResource("PATH/TO/YourImage.png")));
    }
    
    private void firstTimeReset(){
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Welcome to SBS!");
        JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(robotHello)));
        setting.SaveSetting("bool", "shutdownPc", "false");
        setting.SaveSetting("bool", "restartPc", "false");        
        setting.SaveSetting("bool", "copyHistoryActive", "false");        
        setting.SaveSetting("string", "copyHistoryPath", "");        
        setting.SaveSetting("string", "usrPass", "");        
        setting.SaveSetting("string", "usrCode", "");        
        setting.SaveSetting("bool", "autopilotActive", "false");            
        setting.SaveSetting("string", "autoPilotTime", "");                
        setting.SaveSetting("string", "autoPilotDays", "");
        setting.SaveSetting("bool", "autoStart", "false");
        setting.SaveSetting("string", "serverUrl", setting.getStingValue("defaultServerUrl"));
        setting.SaveSetting("bool", "syncDevSrvBackup", "true");
        setting.SaveSetting("boolean", "startMinimized", "false");
        setting.SaveSetting("boolean", "sbsMessageTrayAgain", "false");
        setting.SaveSetting("boolean", "sbsBackupDoneTray", "false");
        setting.SaveSetting("boolean", "sbsLastBackupTray", "false");
        setting.SaveSetting("boolean", "deviceBackupActive", "false");
        setting.SaveSetting("int", "deviceDayLim", "-1");
        setting.SaveSetting("string", "deviceFoldDest", "");
        setting.SaveSetting("boolean", "deviceAutoFree", "true");
        devBackPath.EraseSet();
        srvBackPath.EraseSet();
        setting.SaveSetting("boolean", "serverBackupActive", "false");
        setting.SaveSetting("int", "deviceBackupNum", "-1");
        setting.SaveSetting("string", "deviceFoldDest", "");
        setting.SaveSetting("string", "deviceGuiMount", "");
        setting.SaveSetting("string", "deviceOldMountLet", "");
        setting.SaveSetting("string", "lastBackup", "");
        setting.SaveSetting("string", "serverUrl", "");
        setting.SaveSetting("string", "defaultServerUrl", "");
        setting.SaveSetting("string", "serverIp", "");
        setting.SaveSetting("string", "serverPort", "");
        setting.SaveSetting("string", "filePortCl", "");
        setting.SaveSetting("string", "autoPilotDays", "");
        setting.SaveSetting("string", "autoPilotTime", "");
        setting.SaveSetting("string", "usrCode", "");
        setting.SaveSetting("string", "usrPass", "");
        setting.SaveSetting("string", "sbsPass", "");
        setting.SaveSetting("string", "copyHistoryPath", "");
        setting.SaveSetting("string", "expireDate", "");
        setting.SaveSetting("string", "macAddr", "");
        setting.SaveSetting("string", "deviceFileName", "");
        setting.SaveSetting("string", "downloadFoldDest", "");
        setting.SaveSetting("string", "softwareName", "");
        setting.SaveSetting("string", "shortcutName", "");
        setting.SaveSetting("string", "serverSoftwareName", "");
        setting.SaveSetting("string", "serverShortcutName", "");
        setting.SaveSetting("string", "sbsPassServer", "");
        setting.SaveSetting("int", "deviceGbLim", "-1");
        setting.SaveSetting("boolean", "deviceAutoFree", "false");
        setting.SaveSetting("boolean", "deviceMountSet", "false");
        setting.SaveSetting("boolean", "serverBackupActive", "false");
        setting.SaveSetting("boolean", "syncDevSrvBackup", "false");
        setting.SaveSetting("boolean", "autopilotActive", "false");
        setting.SaveSetting("boolean", "sbsPassActive", "false");
        setting.SaveSetting("boolean", "copyHistoryActive", "false");
        setting.SaveSetting("boolean", "restartPc", "false");
        setting.SaveSetting("boolean", "shutdownPc", "false");
        setting.SaveSetting("boolean", "autoStart", "false");
        setting.SaveSetting("boolean", "deviceDone", "false");
        setting.SaveSetting("boolean", "deviceBackupActiveBefore", "false");
        setting.SaveSetting("boolean", "serverBackupActiveBefore", "false");
        setting.SaveSetting("boolean", "autopilotActiveBefore", "false");
        setting.SaveSetting("boolean", "autopilotAudio", "false");
        setting.SaveSetting("boolean", "startMinimized", "false");
        setting.SaveSetting("boolean", "autoStartServer", "false");
        setting.SaveSetting("boolean", "serverStartMinimized", "false");
        setting.SaveSetting("boolean", "serverAutoOnline", "false");
        setting.SaveSetting("boolean", "sbsPassCorrectServer", "false");
        setting.SaveSetting("boolean", "sbsPassActiveServer", "false");
        setting.SaveSetting("boolean", "sbsMessageTrayAgain", "false");
        setting.SaveSetting("boolean", "sbsBackupDoneTray", "false");
        setting.SaveSetting("boolean", "sbsLastBackupTray", "false");
        setting.SaveSetting("int", "deviceFoldNum", "-1");
        setting.SaveSetting("int", "srvPort", "-1");
        setting.SaveSetting("int", "serverFoldNum", "-1");
        setting.SaveSetting("int", "srvGbLim", "-1");
        setting.SaveSetting("int", "backupLimit", "-1");
        setting.SaveSetting("long", "deviceTotalTimer", "-1");
        setting.SaveSetting("long", "deviceCopyTmpTimer", "-1");
        setting.SaveSetting("long", "deviceZipTimer", "-1");
        setting.SaveSetting("long", "deviceCopyDestTimer", "-1");
        setting.SaveSetting("int", "checkInternetSem", "0");
        setting.SaveSetting("int", "checkConnSem", "0");

        setting.SaveSetting("boolean", "sbsPassCorrect", "true");  	//default true
        setting.SaveSetting("boolean", "sbsLocked", "true");  		//default true

        setting.SaveSetting("boolean", "firstInstallation", "true");
    }
    
    private void Schedule(){
        if(!setting.getBoolValue("autopilotActive")) return;
        Date today= new Date();
        SimpleDateFormat sdf= new SimpleDateFormat("E");
        String todayStr= sdf.format(today);
        String autoPilotDays= setting.getStingValue("autoPilotDays");
        if(autoPilotDays.contains(todayStr)){
            try{
                ScheduleClass schedStart= new ScheduleClass(progressBarTotal, progressBarTmp, tmpLabelStatus, tmpLabelPercentageTotal, tmpLabelPercentage, labelTmpStatusName, labelLastUpdate);
                DateFormat todayFormatter = new SimpleDateFormat("yyyy-MM-dd");
                todayStr= todayFormatter.format(today);
                String timeStr= setting.getStingValue("autoPilotTime");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String strToParse= todayStr + " " + timeStr;
                Date date= dateFormatter.parse(strToParse);
                if(date.before(today)) date= addOneDay(date);
                System.out.println("Date= " + date);  //debug
                Timer timer = new Timer();
                timer.schedule(schedStart, date);
            } catch (ParseException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    public void SystemTrayMethod(){
        //checking for support
    if(!SystemTray.isSupported()){
        System.out.println("System tray is not supported !!! ");
        return ;
    }
    //get the systemTray of the system
    SystemTray systemTray = SystemTray.getSystemTray();

    //get default toolkit
    //Toolkit toolkit = Toolkit.getDefaultToolkit();
    //get image 
    //Toolkit.getDefaultToolkit().getImage("src/resources/busylogo.jpg");
    
    //Image image = Toolkit.getDefaultToolkit().getImage(sbsIcon);
    Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(sbsIcon));

    //popupmenu
    PopupMenu trayPopupMenu = new PopupMenu();

    //1t menuitem for popupmenu
    MenuItem action = new MenuItem("Open");
    action.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            checkPassword();
            if(setting.getBoolValue("sbsPassActive")){
                if(setting.getBoolValue("sbsPassCorrect")) setVisible(true);
            }
            else setVisible(true);
        }
    });     
    trayPopupMenu.add(action);

    //2nd menuitem of popupmenu
    MenuItem close = new MenuItem("Exit");
    close.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);             
        }
    });
    trayPopupMenu.add(close);

    //setting tray icon
    TrayIcon trayIcon = new TrayIcon(image, "SBS 1.0", trayPopupMenu);
    //adjust to default size as per system recommendation 
    trayIcon.setImageAutoSize(true);
    
    trayIcon.addMouseListener(new MouseAdapter(){
        public void mouseClicked(MouseEvent e){
            if(e.getClickCount()==2){
                checkPassword();
                if(setting.getBoolValue("sbsPassActive")){
                    if(setting.getBoolValue("sbsPassCorrect")) setVisible(true);
                }
                else setVisible(true);
            }
        }
    });
    
    /*
    trayIcon.addMouseListener(new MouseAdapter(){
        public void actionPerformed(MouseEvent e) {
            JOptionPane.showMessageDialog(rootPane, "hello");  //debug
            if (e.getClickCount() > 1) {
                JOptionPane.showMessageDialog(rootPane, "hello");  //debug
            }
        }
    });
    */

    try{
        systemTray.add(trayIcon);
    }catch(AWTException awtException){
        awtException.printStackTrace();
    }

    }
    
    private void copyAutoStart(){
        boolean autoStart= setting.getBoolValue("autoStart");
        if(autoStart){
            Path linkFile= Paths.get(setting.getStingValue("shortcutName"));
            String startUpFold= System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            String destFileName= "\\" + setting.getStingValue("softwareName").replace(".jar", "") + ".lnk";
            Path destFold= Paths.get(startUpFold + destFileName);
            try {
                Files.copy(linkFile, destFold, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            String startUpFold= System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            String destFileName= "";
            try{
                destFileName= "\\" + setting.getStingValue("softwareName").replace(".jar", "") + ".lnk";
            }catch(Exception e){
                System.out.println(e);
            }
            Path destFold= Paths.get(startUpFold + destFileName);
            File tmpFile= new File(destFold.toString());
            if(tmpFile.exists()) tmpFile.delete();
        }
    }
    
    private Date addOneDay(Date date){
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        
        return cal.getTime();
    }
    
    private void checkPassword(){
        boolean passOn= setting.getBoolValue("sbsPassActive");
        if(passOn){
            EnterPasswordGUI enterPass= new EnterPasswordGUI(this, true);
            enterPass.setVisible(true);
            if(!setting.getBoolValue("sbsPassCorrect")) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        }
    }
    
    public void checkLock(){
        boolean lock= setting.getBoolValue("sbsLocked");
        boolean expired= true;
        String tmpExDate= setting.getStingValue("expireDate");
        if(tmpExDate!=null && !tmpExDate.equals("")) expired= tmpExDate.equals("ex") || tmpExDate.equals("un");
        else setting.SaveSetting("string", "expireDate", "un");
        //boolean expired= setting.getStingValue("expireDate").equals("ex") || setting.getStingValue("expireDate").equals("un");
        if(lock){
            if(expired){
                btnAutopilotSett.setEnabled(false);
                btnCopyHistory.setEnabled(false);
                btnStart.setEnabled(false);
                btnDevBackFold.setEnabled(false);
                btnDevFoldDest.setEnabled(false);
                btnDevDays.setEnabled(false);
                btnDevActivate.setEnabled(false);
                btnSbsPass.setEnabled(false);
            }
            lockLabel.setIcon(new ImageIcon(getClass().getResource(lockIcon)));
            lockLabel.setText("Locked");
            btnSrvBackFold.setEnabled(false);
            btnSrvPing.setEnabled(false);
            btnSrvRefrSet.setEnabled(false);
            btnSrvActivate.setEnabled(false);
        }
        else{
            if(setting.getBoolValue("deviceBackupActiveBefore")) setting.SaveSetting("boolean", "deviceBackupActive", "true");
            if(setting.getBoolValue("serverBackupActiveBefore")) setting.SaveSetting("boolean", "serverBackupActive", "true");
            if(setting.getBoolValue("autopilotActiveBefore")) setting.SaveSetting("boolean", "autopilotActive", "true");
            lockLabel.setIcon(null);
            lockLabel.setText("");
            btnAutopilotSett.setEnabled(true);
            btnCopyHistory.setEnabled(true);
            btnStart.setEnabled(true);
            btnDevBackFold.setEnabled(true);
            btnDevFoldDest.setEnabled(true);
            btnDevDays.setEnabled(true);
            btnSrvBackFold.setEnabled(true);
            btnSrvPing.setEnabled(true);
            btnSrvRefrSet.setEnabled(true);
            btnSbsPass.setEnabled(true); 
        }
        devCheckSettings();
    }
    
    private void checkExpire(){
        String exStr= setting.getStingValue("expireDate");
        if(!exStr.equals("ex") && !exStr.equals("un") && exStr.length()>1){
            try {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
                SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
                String todayDate= dtf.format(LocalDateTime.now());
                Date cmrDate= sdf.parse(todayDate);
                Date expireDate= sdf.parse(exStr);
                if(cmrDate.after(expireDate)){
                    setting.SaveSetting("string", "expireDate", "ex");
                }
            } catch (ParseException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        exStr= setting.getStingValue("expireDate");
        if(exStr.length()<1) setting.SaveSetting("string", "expireDate", "un");
        if(exStr.equals("ex")){
            expireDateLabel.setForeground(Color.red);
            expireDateLabel.setText("EXPIRED");
            labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
        }
        else if(exStr.equals("un")){
            expireDateLabel.setForeground(Color.red);
            expireDateLabel.setText("UNKNOWN");
            labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
        }
        else{
            expireDateLabel.setForeground(Color.black);
            expireDateLabel.setText(exStr);
            labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
    }
    
    public boolean mainCheckSettings(boolean fastCheck){        
        boolean srvError= false;
        setting.SaveSetting("bool", "srvError", "false"); 
        labelSrvInfo.setText("");
        labelSrvInfo.setIcon(null);
        labelSrvInfo.setForeground(Color.black);
        //FASTCHEK is used to jump interaction with server when its not needed
        
        //**************Internet Status**************
        /*
        srvError= checkConnServStatus();
        if(srvError) setting.SaveSetting("bool", "srvError", "true");
        */
        setting.SaveSetting("boolean", "intChecking", "true");
        srvCheckInternetThread scit= new srvCheckInternetThread(labelError, expireDateLabel, labelSrvInternet, labelMainInternet, labelSrvInfo, labelMainUserInfo, labelSrvUserInfo, labelMainExpire, labelSrvCheckInfo, this);
        Thread thrScit= new Thread(scit);
        thrScit.start();
        if(setting.getBoolValue("srvError")) srvError=true;
        
        //**************INTERACT WITH SERVER**************
        if(threadCheckErr){
            setting.SaveSetting("bool", "srvError", "true");
            srvError= true;
        }
        if(!fastCheck && !srvError){
            boolean running= true;
            SrvInfoCheckThread sict= new SrvInfoCheckThread(labelSrvConn, labelSrvUserInfo, labelMainUserInfo, labelError, labelMainExpire, running, srvError, this);
            Thread thrSrvCheck= new Thread(sict);
            thrSrvCheck.start();
        }
        String checkExpire= setting.getStingValue("expireDate");
        if(checkExpire.equals("ex") || checkExpire.equals("un")){
            srvError= true;
            setting.SaveSetting("bool", "srvError", "true"); 
        }
        
        //**************CHECK LOCK**************
        checkLock();
            
        //**************Auto Pilot**************
        checkAutoPilotOn();
        Schedule();
        
        //**************Expire Server Check**************
        checkExpire();
        
        //**************Restart/Shut PC**************
        boolean shutPc= setting.getBoolValue("shutdownPc");
        if(shutPc) checkShutPC.setSelected(true);
        else checkShutPC.setSelected(false);
        boolean restartPc= setting.getBoolValue("restartPc");
        if(restartPc) checkRestartPC.setSelected(true);
        else checkRestartPC.setSelected(false);
        
        //**************Auto Start**************
        boolean autoStart= setting.getBoolValue("autoStart");
        if(autoStart) checkAutoStart.setSelected(true);
        else checkAutoStart.setSelected(false);
        copyAutoStart();
        
        //**************Start Minimized**************
        boolean minimized= setting.getBoolValue("startMinimized");
        if(minimized) checkMinimized.setSelected(true);
        else checkMinimized.setSelected(false);
        
        //**************Backup Active**************
        if(!serverBackupActiveCheck()){
            btnSrvDeactivate.setEnabled(false);
            btnSrvActivate.setEnabled(true);
            labelSrvOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            labelSrvOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
        }
        else{
            btnSrvDeactivate.setEnabled(true);
            btnSrvActivate.setEnabled(false);
            labelSrvOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOn)));
            labelSrvOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOn)));
        }
        
        //**************Backup Folder**************
        if(!serverBackFoldCheck()){
            labelSrvFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            srvError=true;
            setting.SaveSetting("bool", "srvError", "true");
        }
        else{
            labelSrvFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        
        //***************Backup and Dest-HDD size***************
        long srvBackSize= srvCheckBackDestSize();
        if(!srvError){
            boolean destSize= srvCheckDestSize(srvBackSize);
            if(!destSize){
                srvError=true;
                setting.SaveSetting("bool", "srvError", "true");
                labelSrvFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            }
        }
        
        
        //***************Last Update***************
        String lastUpdate= setting.getStingValue("lastBackup");
        if(lastUpdate!=null && lastUpdate!=""){
            labelLastUpdate.setText("Last Update: " + lastUpdate);
        }
        
            
        //**************Return values**************
        if(srvError){
            srvStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
            srvStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
            btnSrvDeactivate.setEnabled(false);
            btnSrvActivate.setEnabled(true);
            labelSrvOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            labelSrvOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            setting.SaveSetting("boolean", "serverBackupActive", "false");
            checkAutoPilotOn();
            return false;
        }
        
        else{
            srvStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsGreen)));
            srvStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsGreen)));
            checkAutoPilotOn();
            return true;
        }
    }
    
    public void deactivateServer(){
        //used by srvCheckInternetThread
        srvStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
        srvStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
        btnSrvDeactivate.setEnabled(false);
        btnSrvActivate.setEnabled(true);
        labelSrvOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
        labelSrvOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
        setting.SaveSetting("boolean", "serverBackupActive", "false");
        checkAutoPilotOn();
    }
    
    private void autopilotCheckSettings(){
        boolean pilotOn= setting.getBoolValue("autopilotActive");
        //if(pilotOn) setting.SaveSetting("boolean", "autopilotActiveBefore", "true");
        //else setting.SaveSetting("boolean", "autopilotActiveBefore", "false");
        String days= setting.getStingValue("autoPilotDays");
        String time= setting.getStingValue("autoPilotTime");
        if(days==null || time==null){
            days="";
            time="";
            setting.SaveSetting("string", "autoPilotDays", "");
            setting.SaveSetting("string", "autoPilotTime", "");
        }
        if(days.length()<2 || time.length()<2){
            pilotOn= false;
            setting.SaveSetting("bool", "autopilotActive", "false");
        }
        if(pilotOn){
            labelPilotSchedule.setForeground(Color.black);
            labelPilotNextBack.setForeground(Color.black);
            labelOnOffBtnPilot.setIcon(new ImageIcon(getClass().getResource(labelPilotOnBtn)));
            labelOnOffPilotCheck.setIcon(new ImageIcon(getClass().getResource(labelPilotOnCheck)));
            setting.SaveSetting("bool", "autoStart", "true");
            
            if(days.length()>25){
                labelAutoPilotDays.setText("Everyday");
            }
            else{
                if(days.charAt(days.length()-1)=='-') days= days.substring(0, days.length()-1);
                labelAutoPilotDays.setText(days);
            }
            labelAutoPilotTime.setText(time);
            autopilotCheckOn.setIcon(new ImageIcon(getClass().getResource(autoPilotPlaneCheck)));
        }
        else{
            labelPilotSchedule.setForeground(Color.gray);
            labelPilotNextBack.setForeground(Color.gray);
            labelOnOffBtnPilot.setIcon(new ImageIcon(getClass().getResource(labelPilotOffBtn)));
            labelOnOffPilotCheck.setIcon(new ImageIcon(getClass().getResource(labelPilotOffCheck)));
            
            labelAutoPilotDays.setText("");
            labelAutoPilotTime.setText("");
            autopilotCheckOn.setIcon(null);
        }
        /*
        if(setting.getBoolValue("autopilotAudio")) labelAudio.setIcon(new ImageIcon(getClass().getResource(audioOn)));
        else labelAudio.setIcon(new ImageIcon(getClass().getResource(audioOff)));
        */
        
    }
    
    private void deactivateAutopilot(){
        /*
        setting.SaveSetting("boolean", "autopilotActive", "false");
        setting.SaveSetting("boolean", "autopilotActiveBefore", "false");
        if(setting.getBoolValue("autopilotAudio")){
            try {
                InputStream in = new FileInputStream(autopilotDisSound);
                AudioStream as = new AudioStream(in);
                AudioPlayer.player.start(as);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Autopilot Disconnect");
        JOptionPane.showMessageDialog(rootPane, msgLabel, " Autopilot Info", HEIGHT, new ImageIcon(getClass().getResource(autoPilotPlaneOff)));
        */
        
        /*
        setting.SaveSetting("boolean", "autopilotActive", "false");
        setting.SaveSetting("boolean", "autopilotActiveBefore", "false");
        if(setting.getBoolValue("autopilotAudio")){
            try{
                FileInputStream fis = new FileInputStream(autopilotDisSound);
                AudioStream audioStream = new AudioStream(fis);
                AudioData audiodata = audioStream.getData();
                AudioDataStream audiostream = null;
                ContinuousAudioDataStream continuousaudiostream = null;
                audiostream = new AudioDataStream(audiodata);
                AudioPlayer.player.start(audiostream);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        */
        
        setting.SaveSetting("boolean", "autopilotActive", "false");
        setting.SaveSetting("boolean", "autopilotActiveBefore", "false");
        
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Autopilot Disconnect");
        JOptionPane.showMessageDialog(rootPane, msgLabel, " Autopilot Info", HEIGHT, new ImageIcon(getClass().getResource(autoPilotPlaneOff)));
        
    }
    
    private void checkAutoPilotOn(){
        boolean pilotOn= setting.getBoolValue("autopilotActive");
        
        if(pilotOn && !setting.getBoolValue("deviceBackupActive") && !setting.getBoolValue("serverBackupActive")){
            deactivateAutopilot();
        }
        autopilotCheckSettings();
    }
    
    private boolean devCheckSettings(){
        boolean error= false;
        boolean warning= false;
        if(lockAndExpire()) error=true;
        backupInfoLabel.setForeground(Color.black);
        backupInfoLabel.setText("");
        backupInfoLabel.setIcon(null);
        labelDevBackSize.setForeground(Color.black);
        labelDevBackSize.setText("Backup Size");
        
        //**************Backup Active**************
        if(!deviceBackupActiveCheck()){
            btnDevDeactivate.setEnabled(false);
            btnDevActivate.setEnabled(true);
            labelDevOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            labelDevOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
        }
        else{
            btnDevDeactivate.setEnabled(true);
            btnDevActivate.setEnabled(false);
            labelDevOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOn)));
            labelDevOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOn)));
        }
        
        //**************Backup Folder**************
        if(!deviceBackFoldCheck()){
            labelDevFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            error=true;
        }
        else{
            labelDevFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        
        //**************Folder Dest**************
        if(!deviceDestFoldCheck()){
            labelDevFoldDest.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            error=true;
        }
        else{
            labelDevFoldDest.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        
        //***************Backup size***************
        if(!devCheckBackDestSize()){
            error=true;
        }
        
        //***************Device Size***************
        boolean dim= checkDimensions();
        if(!dim){
            error=true;
            labelDevFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
        }
        
        //***************Days (Storage) Limit***************
        if(setting.getBoolValue("deviceAutoFree")){
            labelDevDayLimitTxt.setText("AF");
            labelDevDayLim.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        else if(setting.getIntValue("deviceGbLim")>0){
            labelDevDayLimitTxt.setText(setting.getIntValue("deviceGbLim").toString());
            labelDevDayLim.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        else{
            labelDevDayLimitTxt.setText("");
            warning=true;
            labelDevDayLim.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
        }
        
        //**************RETURN VALUES**************
        if(error){
            labelDevGenCheck.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            devStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
            devStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsRed)));
            
            btnDevDeactivate.setEnabled(false);
            btnDevActivate.setEnabled(true);
            labelDevOnOff.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            labelDevOnOffMain.setIcon(new ImageIcon(getClass().getResource(labelDeviceOff)));
            setting.SaveSetting("boolean", "deviceBackupActive", "false");
            checkAutoPilotOn();

            return false;
        }
        if(warning){
            labelDevGenCheck.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
            devStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsYellow)));
            devStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsYellow)));
        }
        if(!error && !warning){
            labelDevGenCheck.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            devStatusTrafficLight.setIcon(new ImageIcon(getClass().getResource(trafficLightsGreen)));
            devStatusTrafficLightMain.setIcon(new ImageIcon(getClass().getResource(trafficLightsGreen)));
        }
        checkAutoPilotOn();
        return true;
    }
    
    private boolean srvCheckInternetConn(){
        int count= 0;
        while(count<3){
            String urlToCheckStr= "http://www.google.com";
            HttpURLConnection httpUrlConn;
            try{
                httpUrlConn = (HttpURLConnection) new URL(urlToCheckStr).openConnection();
                httpUrlConn.setRequestMethod("HEAD");
                httpUrlConn.setConnectTimeout(30000);
                httpUrlConn.setReadTimeout(30000);
                return (httpUrlConn.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (Exception ex) {
                count++;
            }
        }
        return false;
    }
    
    private boolean devCheckBackDestSize(){
        long backSize= devBackPath.BackupSize();
        String strDest= setting.getStingValue("deviceFoldDest");
        /*
            if(strDest.contains("C:")){
                    long hddSize = new File("/").getUsableSpace();
                    if(hddSize<(backSize*2)){   //check if in hdd there is double space of backup size.
                        labelDevBackSize.setForeground(Color.red);
                        labelDevBackSize.setText("ERROR: Not enough space on Hard Disk");
                        return false;
                    }
            }
            else{
                long hddSize = new File("/").getUsableSpace();
                if(hddSize<backSize){
                    labelDevBackSize.setForeground(Color.red);
                    labelDevBackSize.setText("ERROR: Not enough space on Hard Disk");
                    return false;
                }
                if(strDest.length()>1){
                    long destSize = new File(strDest).getUsableSpace();
                    if(destSize<backSize && !setting.getBoolValue("deviceAutoFree")){
                        labelDevBackSize.setForeground(Color.red);
                        labelDevBackSize.setText("ERROR: Not enough space on Destination");
                        return false;
                    }
                }
        }
        */
        String size= humanReadableByteCount(backSize, false);
        labelDevBackSize.setForeground(Color.blue);
        labelDevBackSize.setText("Backup Size:  " + size);
        return true;
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
    
    private boolean srvCheckDestSize(long size){
        //true if ok
        long limit= (long) setting.getIntValue("backupLimit");
        limit= limit*1024*1024*1024;  //convert gb to bytes
        if(size>limit){
            labelSrvInfo.setIcon(new ImageIcon(getClass().getResource(overSize)));
            labelSrvInfo.setForeground(Color.red);
            labelSrvBackSize.setForeground(Color.red);
            labelSrvInfo.setText("Oversize");
            return false;
        }
        else{
            labelSrvInfo.setIcon(new ImageIcon(getClass().getResource(storageSize)));
            labelSrvInfo.setForeground(Color.blue);
            labelSrvInfo.setText("Max Size: " + setting.getIntValue("backupLimit") + " GB");
        }
        return true;
    }
    
    private long srvCheckBackDestSize(){
        long backSize= srvBackPath.BackupSize();
        String size= humanReadableByteCount(backSize, false);
        labelSrvBackSize.setForeground(Color.blue);
        labelSrvBackSize.setText("Backup Size:  " + size);
        return backSize;
    }
    
    private static String humanReadableByteCount(long bytes, boolean si) {
        //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    private boolean checkDeviceConnect(){
        //deprecated method
        String gui;
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
                reader.readLine();
                if(gui.equals(setting.getStingValue("deviceGuiMount"))){
                    return true;
                }
                reader.readLine(); //throw empty line
            }
        } catch (InterruptedException ex) { 
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        setting.SaveSetting("boolean", "deviceMountSet", "false");
        return false;
    }
    
    private boolean isDevice(){
        String tmpLetter= setting.getStingValue("deviceFoldDest");
        if(tmpLetter==null){
            setting.SaveSetting("string", "deviceFoldDest", "");
            return false;
        }
        if(!tmpLetter.equals("")){
            tmpLetter= tmpLetter.substring(0, 1);
        }
        if(tmpLetter.equals("C")){
            return false;
        }
        if(tmpLetter.equals("")) return false;
        return true;
    }
    
    private boolean checkDimensions(){
        if(!isDevice()) return true;
        long backSize= devBackPath.BackupSize();
        String letter= setting.getStingValue("deviceFoldDest");
        if(!letter.equals("") || letter!=null){
            letter= letter.substring(0, 1);
        }
        File file = new File(letter+":");
        long totalSpace = file.getTotalSpace();
        if(backSize>totalSpace){
            backupInfoLabel.setForeground(Color.red);
            labelDevBackSize.setForeground(Color.red);
            backupInfoLabel.setIcon(new ImageIcon(getClass().getResource(overSize)));
            backupInfoLabel.setText("Oversize");
            return false;
        }
        return true;
    }
        
    private boolean deviceBackupActiveCheck(){
        return setting.getBoolValue("deviceBackupActive");
    }
    
    private boolean serverBackupActiveCheck(){
        return setting.getBoolValue("serverBackupActive");
    }
    
    private boolean deviceBackFoldCheck(){
        devBackPath.removeDeletedFolder();
        int numFold= setting.getIntValue("deviceFoldNum");
        if(devBackPath.isEmpty() || numFold==0){
            labelDevFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelDevFolToBackNumberTxt.setForeground(Color.red);
            labelDevFolToBackNumberTxt.setText("0");
            return false;
        }
        labelDevFolToBackNumberTxt.setForeground(Color.blue);
        labelDevFolToBackNumberTxt.setText(Integer.toString(numFold));
        return true;
    }
    
    private boolean serverBackFoldCheck(){
        srvBackPath.removeDeletedFolder();
        int numFold= setting.getIntValue("serverFoldNum");
        if(srvBackPath.isEmpty() || numFold==0){
            labelSrvFolToBack.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelSrvFolToBackNumberTxt.setForeground(Color.red);
            labelSrvFolToBackNumberTxt.setText("0");
            return false;
        }
        labelSrvFolToBackNumberTxt.setForeground(Color.blue);
        labelSrvFolToBackNumberTxt.setText(Integer.toString(numFold));
        return true;
    }
    
    private boolean deviceDestFoldCheck(){
        String strDest= setting.getStingValue("deviceFoldDest");
        if(strDest==null || strDest.equals("")){
            return false;
        }
        Path pathDest= Paths.get(strDest);
        if(!setting.getBoolValue("deviceMountSet")){
            if(!Files.exists(pathDest)){
                setting.SaveSetting("String", "deviceFoldDest", "");
                return false;
            }
        }
        return true;
    }
    
    private void updateHistoryTable(){
        UpdateHistory updateClass= new UpdateHistory();
        List<String> listLines= updateClass.getList();
        int listSize= listLines.size();
        if(listSize>0){
            labelTotBackHistory.setForeground(Color.blue);
            labelTotBackHistory.setText("Total Bakcup: " + listSize);
        }
        else{
            labelTotBackHistory.setForeground(Color.red);
            labelTotBackHistory.setText("No Backup History");
        }
        int rowNum= historyTable.getRowCount();
        if(listSize>rowNum){
            DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
            model.setRowCount(listSize);
        }
        Iterator<String> itList= listLines.iterator();
        int rowCounter=0;
        while(itList.hasNext()){
            String tmpArray[]= itList.next().split(",");
            historyTable.getModel().setValueAt(tmpArray[0], rowCounter, 0);
            historyTable.getModel().setValueAt(tmpArray[1], rowCounter, 1);
            historyTable.getModel().setValueAt(tmpArray[2], rowCounter, 2);
            historyTable.getModel().setValueAt(tmpArray[3], rowCounter, 3);
            historyTable.getModel().setValueAt(tmpArray[4], rowCounter, 4);
            historyTable.getModel().setValueAt(tmpArray[5], rowCounter, 5);
            historyTable.getModel().setValueAt(tmpArray[6], rowCounter, 6);
            rowCounter++;
        }
    }
    
    private void resetDeviceSettings(){
        setting.SaveSetting("boolean", "deviceBackupActive", "false");
            setting.SaveSetting("int", "deviceDayLim", "-1");
            setting.SaveSetting("string", "deviceFoldDest", "");
            setting.SaveSetting("boolean", "deviceAutoFree", "true");
            devBackPath.EraseSet();
    }
    
    private void resetMainSettings(){
        setting.SaveSetting("bool", "shutdownPc", "false");
        setting.SaveSetting("bool", "restartPc", "false");        
        setting.SaveSetting("bool", "copyHistoryActive", "false");        
        setting.SaveSetting("string", "copyHistoryPath", "");        
        setting.SaveSetting("string", "usrPass", "");        
        setting.SaveSetting("string", "usrCode", "");        
        setting.SaveSetting("bool", "autopilotActive", "false");            
        setting.SaveSetting("string", "autoPilotTime", "");                
        setting.SaveSetting("string", "autoPilotDays", "");
        setting.SaveSetting("bool", "autoStart", "false");
        setting.SaveSetting("string", "serverUrl", setting.getStingValue("defaultServerUrl"));
        setting.SaveSetting("bool", "syncDevSrvBackup", "true");
        setting.SaveSetting("boolean", "startMinimized", "false");
        setting.SaveSetting("boolean", "sbsMessageTrayAgain", "false");
        setting.SaveSetting("boolean", "sbsBackupDoneTray", "false");
        setting.SaveSetting("boolean", "sbsLastBackupTray", "false");
    }
    
    private void resetServerSettings(){
        setting.SaveSetting("boolean", "serverBackupActive", "false");
        srvBackPath.EraseSet();
    }
    
    private boolean lockAndExpire(){
        if(setting.getStingValue("expireDate")==null || setting.getStingValue("expireDate")==null) return false;
        return(setting.getBoolValue("sbsLocked") && (setting.getStingValue("expireDate").equals("ex") || setting.getStingValue("expireDate").equals("un")));
    }
    
    private void start(){
        //check if Dev backup is enabled
        boolean serverStartCheck= setting.getBoolValue("serverBackupActive");
        boolean deviceStartCheck= setting.getBoolValue("deviceBackupActive");
        StartDevice startDev= new StartDevice(progressBarTotal, progressBarTmp, tmpLabelStatus, tmpLabelPercentageTotal, tmpLabelPercentage, labelTmpStatusName, serverStartCheck, labelLastUpdate);
        Thread thStartDev= new Thread(startDev);
        StartServer startSrv= new StartServer(progressBarTotal, progressBarTmp, tmpLabelStatus, tmpLabelPercentageTotal, tmpLabelPercentage, labelTmpStatusName, deviceStartCheck, labelLastUpdate);
        Thread thStartSrv= new Thread(startSrv);
        //check if both acrivate. If true start both backup (join server after device)
        if(deviceStartCheck && serverStartCheck){
            thStartDev.start();
            thStartSrv.start();
        }
        if(deviceStartCheck && !serverStartCheck){
            thStartDev.start();
        }
        if(!deviceStartCheck && serverStartCheck){
            thStartSrv.start();
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelMain = new javax.swing.JPanel();
        jSeparator5 = new javax.swing.JSeparator();
        labelDevicePane1 = new javax.swing.JLabel();
        btnAutopilotSett = new javax.swing.JButton();
        labelDevOnOffTxtMain1 = new javax.swing.JLabel();
        labelPilotSchedule = new javax.swing.JLabel();
        labelPilotNextBack = new javax.swing.JLabel();
        labelOnOffPilotCheck = new javax.swing.JLabel();
        labelOnOffBtnPilot = new javax.swing.JLabel();
        labelPilotStatus = new javax.swing.JLabel();
        labelAutoPilotDays = new javax.swing.JLabel();
        labelAutoPilotTime = new javax.swing.JLabel();
        btnResetMainSett = new javax.swing.JButton();
        labelDevFoldDestTxt3 = new javax.swing.JLabel();
        labelMainInternet = new javax.swing.JLabel();
        labelDevGenCheckTxt4 = new javax.swing.JLabel();
        labelMainUserInfo = new javax.swing.JLabel();
        labelDevGenCheckTxt5 = new javax.swing.JLabel();
        labelMainExpire = new javax.swing.JLabel();
        btnRefreshMain = new javax.swing.JButton();
        btnUsrInfo = new javax.swing.JButton();
        btnSrvInfo = new javax.swing.JButton();
        btnSbsPass = new javax.swing.JButton();
        btnCopyHistory = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        expireDateLabel = new javax.swing.JLabel();
        checkRestartPC = new javax.swing.JCheckBox();
        checkShutPC = new javax.swing.JCheckBox();
        checkAutoStart = new javax.swing.JCheckBox();
        labelAudio = new javax.swing.JLabel();
        checkMinimized = new javax.swing.JCheckBox();
        panelDevice = new javax.swing.JPanel();
        labelDevicePane = new javax.swing.JLabel();
        labelDevGenCheckTxt = new javax.swing.JLabel();
        labelDevGenCheck = new javax.swing.JLabel();
        labelDevFolToBackTxt = new javax.swing.JLabel();
        labelDevFolToBack = new javax.swing.JLabel();
        labelDevFoldDestTxt = new javax.swing.JLabel();
        labelDevFoldDest = new javax.swing.JLabel();
        labelDevDayLimTxt = new javax.swing.JLabel();
        labelDevDayLim = new javax.swing.JLabel();
        devStatusTrafficLight = new javax.swing.JLabel();
        labelDevStatus = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnDevActivate = new javax.swing.JButton();
        btnDevBackFold = new javax.swing.JButton();
        btnDevFoldDest = new javax.swing.JButton();
        btnDevDeactivate = new javax.swing.JButton();
        btnDevRefresh = new javax.swing.JButton();
        btnDevDays = new javax.swing.JButton();
        btnResetDeviceSettings = new javax.swing.JButton();
        labelDevFolToBackNumberTxt = new javax.swing.JLabel();
        labelDevOnOffTxt = new javax.swing.JLabel();
        labelDevOnOff = new javax.swing.JLabel();
        labelDevDayLimitTxt = new javax.swing.JLabel();
        panelServer = new javax.swing.JPanel();
        labelServerPane = new javax.swing.JLabel();
        labelSrvStatus = new javax.swing.JLabel();
        srvStatusTrafficLight = new javax.swing.JLabel();
        labelSrvOnOffTxt = new javax.swing.JLabel();
        labelSrvOnOff = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        labelSrvFolToBackTxt = new javax.swing.JLabel();
        labelDevFoldDestTxt1 = new javax.swing.JLabel();
        labelSrvFolToBack = new javax.swing.JLabel();
        labelSrvUserInfo = new javax.swing.JLabel();
        labelDevGenCheckTxt2 = new javax.swing.JLabel();
        labelSrvCheckInfo = new javax.swing.JLabel();
        labelDevGenCheckTxt3 = new javax.swing.JLabel();
        btnResetServerSettings = new javax.swing.JButton();
        labelSrvInternet = new javax.swing.JLabel();
        labelDevFoldDestTxt2 = new javax.swing.JLabel();
        labelSrvConn = new javax.swing.JLabel();
        btnSrvActivate = new javax.swing.JButton();
        btnSrvDeactivate = new javax.swing.JButton();
        btnSrvBackFold = new javax.swing.JButton();
        btnSrvPing = new javax.swing.JButton();
        btnSrvDownload = new javax.swing.JButton();
        btnSrvRefrSet = new javax.swing.JButton();
        labelSrvFolToBackNumberTxt = new javax.swing.JLabel();
        panelHistory = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        btnRefreshHistory = new javax.swing.JButton();
        labelTotBackHistory = new javax.swing.JLabel();
        progressBarTmp = new javax.swing.JProgressBar();
        progressBarTotal = new javax.swing.JProgressBar();
        jLabel1 = new javax.swing.JLabel();
        labelTmpStatusName = new javax.swing.JLabel();
        btnStart = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        labelDevicePaneMain = new javax.swing.JLabel();
        labelDevStatusMain = new javax.swing.JLabel();
        devStatusTrafficLightMain = new javax.swing.JLabel();
        labelDevOnOffTxtMain = new javax.swing.JLabel();
        labelDevOnOffMain = new javax.swing.JLabel();
        labelDevBackSize = new javax.swing.JLabel();
        tmpLabelPercentage = new javax.swing.JLabel();
        tmpLabelStatus = new javax.swing.JLabel();
        tmpLabelPercentageTotal = new javax.swing.JLabel();
        labelLastUpdate = new javax.swing.JLabel();
        labelServerPane2 = new javax.swing.JLabel();
        labelSrvStatusMain1 = new javax.swing.JLabel();
        srvStatusTrafficLightMain = new javax.swing.JLabel();
        labelSrvOnOffTxtMain = new javax.swing.JLabel();
        labelSrvOnOffMain = new javax.swing.JLabel();
        labelSrvBackSize = new javax.swing.JLabel();
        labelSrvInfo = new javax.swing.JLabel();
        autopilotCheckOn = new javax.swing.JLabel();
        labelError = new javax.swing.JLabel();
        lockLabel = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JSeparator();
        backupInfoLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                MainGUI.this.windowClosing(evt);
            }
        });

        jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelDevicePane1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevicePane1.setForeground(new java.awt.Color(0, 0, 204));
        labelDevicePane1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/airplane-icon-71x50.png"))); // NOI18N
        labelDevicePane1.setText("Autopilot");

        btnAutopilotSett.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnAutopilotSett.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/plane-settings-icon-30x30.png"))); // NOI18N
        btnAutopilotSett.setText("Settings");
        btnAutopilotSett.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAutopilotSettActionPerformed(evt);
            }
        });

        labelDevOnOffTxtMain1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevOnOffTxtMain1.setText("On/Off");

        labelPilotSchedule.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelPilotSchedule.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/calendar-icon-30x30.png"))); // NOI18N
        labelPilotSchedule.setText("Days");

        labelPilotNextBack.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelPilotNextBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/clock-icon-30x30.png"))); // NOI18N
        labelPilotNextBack.setText("Time");

        labelOnOffPilotCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/on-btn-black-50x50.png"))); // NOI18N

        labelOnOffBtnPilot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/on-btn-black-green-40x40.png"))); // NOI18N
        labelOnOffBtnPilot.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelOnOffBtnPilotMouseClicked(evt);
            }
        });

        labelPilotStatus.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelPilotStatus.setText("Status");

        labelAutoPilotDays.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelAutoPilotDays.setText("days");

        labelAutoPilotTime.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelAutoPilotTime.setText("time");

        btnResetMainSett.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnResetMainSett.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/reset-icon-30x30.png"))); // NOI18N
        btnResetMainSett.setText("Reset Settings");
        btnResetMainSett.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnResetMainSett.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetMainSettActionPerformed(evt);
            }
        });

        labelDevFoldDestTxt3.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevFoldDestTxt3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/web-internet-world-icon-30x30.png"))); // NOI18N
        labelDevFoldDestTxt3.setText("Internet Connection");

        labelMainInternet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevGenCheckTxt4.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevGenCheckTxt4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/admin-icon-30x30.png"))); // NOI18N
        labelDevGenCheckTxt4.setText("User Info");

        labelMainUserInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevGenCheckTxt5.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevGenCheckTxt5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/expire-date-user-login-icon-30x30.png"))); // NOI18N
        labelDevGenCheckTxt5.setText("Expiration");

        labelMainExpire.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        btnRefreshMain.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/refresh-icon-2-30x30.png"))); // NOI18N
        btnRefreshMain.setText("Refresh");
        btnRefreshMain.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnRefreshMain.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnRefreshMain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshMainActionPerformed(evt);
            }
        });

        btnUsrInfo.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnUsrInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/admin-icon-30x30.png"))); // NOI18N
        btnUsrInfo.setText("User Info");
        btnUsrInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnUsrInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnUsrInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUsrInfoActionPerformed(evt);
            }
        });

        btnSrvInfo.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSrvInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/server-info-icon-30x30.png"))); // NOI18N
        btnSrvInfo.setText("Server Info");
        btnSrvInfo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvInfo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvInfo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvInfoActionPerformed(evt);
            }
        });

        btnSbsPass.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSbsPass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/lock-icon-30x30.png"))); // NOI18N
        btnSbsPass.setText("SBS Password");
        btnSbsPass.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSbsPass.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSbsPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSbsPassActionPerformed(evt);
            }
        });

        btnCopyHistory.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnCopyHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/history-folder-30x30.png"))); // NOI18N
        btnCopyHistory.setText("Copy History");
        btnCopyHistory.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnCopyHistory.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnCopyHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCopyHistoryActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/hourglass-icon-30x30.png"))); // NOI18N
        jLabel2.setText("Expiration date");

        expireDateLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        expireDateLabel.setText("25-JAN-2018");

        checkRestartPC.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        checkRestartPC.setText("Restart PC after backup");
        checkRestartPC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkRestartPCActionPerformed(evt);
            }
        });

        checkShutPC.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        checkShutPC.setText("Shutdown PC after backup");
        checkShutPC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkShutPCActionPerformed(evt);
            }
        });

        checkAutoStart.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        checkAutoStart.setText("Auto start");
        checkAutoStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAutoStartActionPerformed(evt);
            }
        });

        labelAudio.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/audio-on-icon-30x30.png"))); // NOI18N
        labelAudio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelAudioMouseClicked(evt);
            }
        });

        checkMinimized.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        checkMinimized.setText("Start minimized");
        checkMinimized.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMinimizedActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMainLayout = new javax.swing.GroupLayout(panelMain);
        panelMain.setLayout(panelMainLayout);
        panelMainLayout.setHorizontalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(btnUsrInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(btnSrvInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                                .addComponent(btnRefreshMain, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(checkRestartPC)
                                    .addComponent(checkShutPC))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnCopyHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(32, 32, 32)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnResetMainSett, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSbsPass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(labelDevFoldDestTxt3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelMainInternet)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                                .addComponent(labelDevGenCheckTxt4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelMainUserInfo))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(expireDateLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(checkAutoStart)))
                        .addGap(27, 27, 27)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkMinimized)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(labelDevGenCheckTxt5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelMainExpire)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelMainLayout.createSequentialGroup()
                                        .addComponent(labelPilotNextBack)
                                        .addGap(18, 18, 18)
                                        .addComponent(labelAutoPilotTime))
                                    .addComponent(labelDevicePane1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE))
                            .addGroup(panelMainLayout.createSequentialGroup()
                                .addComponent(labelDevOnOffTxtMain1)
                                .addGap(18, 18, 18)
                                .addComponent(labelOnOffBtnPilot)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelAudio)
                                .addGap(28, 28, 28)))
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAutopilotSett, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                                .addComponent(labelPilotStatus)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelOnOffPilotCheck))))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addComponent(labelPilotSchedule)
                        .addGap(18, 18, 18)
                        .addComponent(labelAutoPilotDays)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        panelMainLayout.setVerticalGroup(
            panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator5)
            .addGroup(panelMainLayout.createSequentialGroup()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelDevicePane1)
                            .addComponent(labelPilotStatus))
                        .addGap(18, 18, 18)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelPilotSchedule)
                            .addComponent(labelAutoPilotDays))
                        .addGap(18, 18, 18)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelPilotNextBack)
                            .addComponent(labelAutoPilotTime)))
                    .addComponent(labelOnOffPilotCheck))
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(labelDevOnOffTxtMain1)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelAudio)
                            .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnAutopilotSett, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(labelOnOffBtnPilot)))
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelMainLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelDevFoldDestTxt3)
                    .addComponent(labelMainInternet)
                    .addComponent(labelDevGenCheckTxt4)
                    .addComponent(labelMainUserInfo)
                    .addComponent(labelDevGenCheckTxt5)
                    .addComponent(labelMainExpire))
                .addGap(18, 18, 18)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(expireDateLabel)
                    .addComponent(checkAutoStart)
                    .addComponent(checkMinimized))
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSbsPass, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCopyHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(panelMainLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkRestartPC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkShutPC)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnResetMainSett, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnRefreshMain, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSrvInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnUsrInfo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Main", panelMain);

        labelDevicePane.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevicePane.setForeground(new java.awt.Color(0, 0, 204));
        labelDevicePane.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/HDD-USB-50x50.png"))); // NOI18N
        labelDevicePane.setText("Device");

        labelDevGenCheckTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevGenCheckTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Settings-icon-30x30.png"))); // NOI18N
        labelDevGenCheckTxt.setText("General Check");

        labelDevGenCheck.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevFolToBackTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevFolToBackTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/folder-icon-30x30.png"))); // NOI18N
        labelDevFolToBackTxt.setText("Folder to Backup");

        labelDevFolToBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevFoldDestTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevFoldDestTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/backup-device-30x30.png"))); // NOI18N
        labelDevFoldDestTxt.setText("Folder Destination");

        labelDevFoldDest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevDayLimTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevDayLimTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/storage-icon-3-30x30.png"))); // NOI18N
        labelDevDayLimTxt.setText("Storage Limit");

        labelDevDayLim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-yellow-logo-30x30.png"))); // NOI18N

        devStatusTrafficLight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/traffic-light-green-100x40.png"))); // NOI18N

        labelDevStatus.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevStatus.setText("Status");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        btnDevActivate.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        btnDevActivate.setForeground(new java.awt.Color(0, 204, 0));
        btnDevActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/on-green-icon-30x30.png"))); // NOI18N
        btnDevActivate.setText("Activate");
        btnDevActivate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevActivate.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevActivateActionPerformed(evt);
            }
        });

        btnDevBackFold.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnDevBackFold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/folder-icon-30x30.png"))); // NOI18N
        btnDevBackFold.setText("Backup Folder");
        btnDevBackFold.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevBackFold.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevBackFold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevBackFoldActionPerformed(evt);
            }
        });

        btnDevFoldDest.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnDevFoldDest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/backup-device-30x30.png"))); // NOI18N
        btnDevFoldDest.setText("Folder Destination");
        btnDevFoldDest.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevFoldDest.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevFoldDest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevFoldDestActionPerformed(evt);
            }
        });

        btnDevDeactivate.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        btnDevDeactivate.setForeground(new java.awt.Color(255, 0, 0));
        btnDevDeactivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/off-grey-30x30.png"))); // NOI18N
        btnDevDeactivate.setText("Deactivate");
        btnDevDeactivate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevDeactivate.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevDeactivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevDeactivateActionPerformed(evt);
            }
        });

        btnDevRefresh.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnDevRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/refresh-icon-2-30x30.png"))); // NOI18N
        btnDevRefresh.setText("Refresh Settings");
        btnDevRefresh.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevRefresh.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevRefreshActionPerformed(evt);
            }
        });

        btnDevDays.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnDevDays.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/storage-icon-3-30x30.png"))); // NOI18N
        btnDevDays.setText("Storage Limit");
        btnDevDays.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnDevDays.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnDevDays.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDevDaysActionPerformed(evt);
            }
        });

        btnResetDeviceSettings.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnResetDeviceSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/reset-icon-30x30.png"))); // NOI18N
        btnResetDeviceSettings.setText("Reset Settings");
        btnResetDeviceSettings.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnResetDeviceSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetDeviceSettingsActionPerformed(evt);
            }
        });

        labelDevFolToBackNumberTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevFolToBackNumberTxt.setForeground(new java.awt.Color(0, 0, 204));
        labelDevFolToBackNumberTxt.setText("100");

        labelDevOnOffTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevOnOffTxt.setText("On/Off");

        labelDevOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelDevOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelDeviceOnOffEvent(evt);
            }
        });

        labelDevDayLimitTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevDayLimitTxt.setForeground(new java.awt.Color(0, 0, 204));
        labelDevDayLimitTxt.setText("100");

        javax.swing.GroupLayout panelDeviceLayout = new javax.swing.GroupLayout(panelDevice);
        panelDevice.setLayout(panelDeviceLayout);
        panelDeviceLayout.setHorizontalGroup(
            panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelDeviceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDeviceLayout.createSequentialGroup()
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelDevGenCheckTxt)
                            .addComponent(labelDevFoldDestTxt)
                            .addGroup(panelDeviceLayout.createSequentialGroup()
                                .addComponent(labelDevFolToBackTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelDevFolToBackNumberTxt)))
                        .addGap(6, 6, 6)
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelDevFoldDest)
                            .addComponent(labelDevFolToBack)
                            .addComponent(labelDevGenCheck))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 41, Short.MAX_VALUE)
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnResetDeviceSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelDeviceLayout.createSequentialGroup()
                                .addComponent(labelDevDayLimTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelDevDayLimitTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(6, 6, 6)
                                .addComponent(labelDevDayLim))))
                    .addGroup(panelDeviceLayout.createSequentialGroup()
                        .addComponent(labelDevicePane, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(labelDevStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(devStatusTrafficLight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelDevOnOffTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelDevOnOff)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnDevBackFold, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDevActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDevRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnDevFoldDest, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(btnDevDeactivate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnDevDays, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
        );
        panelDeviceLayout.setVerticalGroup(
            panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(panelDeviceLayout.createSequentialGroup()
                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelDeviceLayout.createSequentialGroup()
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(panelDeviceLayout.createSequentialGroup()
                                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(devStatusTrafficLight)
                                    .addGroup(panelDeviceLayout.createSequentialGroup()
                                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelDevOnOff)
                                            .addComponent(labelDevOnOffTxt))
                                        .addGap(9, 9, 9)))
                                .addGap(18, 18, 18)
                                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(panelDeviceLayout.createSequentialGroup()
                                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelDevGenCheck)
                                            .addComponent(labelDevDayLim))
                                        .addGap(18, 18, 18)
                                        .addComponent(labelDevFolToBack))
                                    .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelDevDayLimTxt)
                                        .addComponent(labelDevDayLimitTxt))))
                            .addGroup(panelDeviceLayout.createSequentialGroup()
                                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelDevicePane, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelDevStatus))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(labelDevGenCheckTxt)
                                .addGap(18, 18, 18)
                                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelDevFolToBackTxt)
                                    .addComponent(labelDevFolToBackNumberTxt))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnResetDeviceSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addGroup(panelDeviceLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(labelDevFoldDest)
                                    .addComponent(labelDevFoldDestTxt)))))
                    .addGroup(panelDeviceLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDevActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDevDeactivate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDevBackFold, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDevFoldDest, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(panelDeviceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnDevRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDevDays, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Device", panelDevice);

        labelServerPane.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelServerPane.setForeground(new java.awt.Color(0, 0, 204));
        labelServerPane.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Server-Logo-50x50.png"))); // NOI18N
        labelServerPane.setText("Server");

        labelSrvStatus.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvStatus.setText("Status");

        srvStatusTrafficLight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/traffic-light-green-100x40.png"))); // NOI18N

        labelSrvOnOffTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvOnOffTxt.setText("On/Off");

        labelSrvOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelSrvOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelSrvOnOfflabelDeviceOnOffEvent(evt);
            }
        });

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelSrvFolToBackTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvFolToBackTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/folder-icon-30x30.png"))); // NOI18N
        labelSrvFolToBackTxt.setText("Folder to Backup");

        labelDevFoldDestTxt1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevFoldDestTxt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/web-internet-world-icon-30x30.png"))); // NOI18N
        labelDevFoldDestTxt1.setText("Internet Connection");

        labelSrvFolToBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelSrvUserInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevGenCheckTxt2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevGenCheckTxt2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/server-info-icon-30x30.png"))); // NOI18N
        labelDevGenCheckTxt2.setText("Server Info");

        labelSrvCheckInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevGenCheckTxt3.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevGenCheckTxt3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/admin-icon-30x30.png"))); // NOI18N
        labelDevGenCheckTxt3.setText("User Info");

        btnResetServerSettings.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnResetServerSettings.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/reset-icon-30x30.png"))); // NOI18N
        btnResetServerSettings.setText("Reset Settings");
        btnResetServerSettings.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnResetServerSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetServerSettingsActionPerformed(evt);
            }
        });

        labelSrvInternet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        labelDevFoldDestTxt2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevFoldDestTxt2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/server-connected-online-icon-30x30.png"))); // NOI18N
        labelDevFoldDestTxt2.setText("Server Connection");

        labelSrvConn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Traffic-light-green-logo-30x30.png"))); // NOI18N

        btnSrvActivate.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        btnSrvActivate.setForeground(new java.awt.Color(0, 204, 0));
        btnSrvActivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/on-green-icon-30x30.png"))); // NOI18N
        btnSrvActivate.setText("Activate");
        btnSrvActivate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvActivate.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvActivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvActivateActionPerformed(evt);
            }
        });

        btnSrvDeactivate.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        btnSrvDeactivate.setForeground(new java.awt.Color(255, 0, 0));
        btnSrvDeactivate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/off-grey-30x30.png"))); // NOI18N
        btnSrvDeactivate.setText("Deactivate");
        btnSrvDeactivate.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvDeactivate.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvDeactivate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvDeactivateActionPerformed(evt);
            }
        });

        btnSrvBackFold.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSrvBackFold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/folder-icon-30x30.png"))); // NOI18N
        btnSrvBackFold.setText("Backup Folder");
        btnSrvBackFold.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvBackFold.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvBackFold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvBackFoldActionPerformed(evt);
            }
        });

        btnSrvPing.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSrvPing.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/ping-icon-43x30.png"))); // NOI18N
        btnSrvPing.setText("Ping Server");
        btnSrvPing.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvPing.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvPingActionPerformed(evt);
            }
        });

        btnSrvDownload.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSrvDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/download-backup-drive-icon-30x30.png"))); // NOI18N
        btnSrvDownload.setText("Download Backup");
        btnSrvDownload.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvDownload.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvDownloadActionPerformed(evt);
            }
        });

        btnSrvRefrSet.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSrvRefrSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/refresh-icon-2-30x30.png"))); // NOI18N
        btnSrvRefrSet.setText("Refresh Settings");
        btnSrvRefrSet.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSrvRefrSet.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSrvRefrSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSrvRefrSetActionPerformed(evt);
            }
        });

        labelSrvFolToBackNumberTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelSrvFolToBackNumberTxt.setForeground(new java.awt.Color(0, 0, 204));
        labelSrvFolToBackNumberTxt.setText("100");

        javax.swing.GroupLayout panelServerLayout = new javax.swing.GroupLayout(panelServer);
        panelServer.setLayout(panelServerLayout);
        panelServerLayout.setHorizontalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelServerLayout.createSequentialGroup()
                        .addComponent(labelServerPane, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelSrvStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(srvStatusTrafficLight)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addComponent(labelSrvOnOffTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelSrvOnOff))
                    .addGroup(panelServerLayout.createSequentialGroup()
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(panelServerLayout.createSequentialGroup()
                                    .addComponent(labelDevFoldDestTxt2)
                                    .addGap(36, 36, 36)
                                    .addComponent(labelSrvConn))
                                .addGroup(panelServerLayout.createSequentialGroup()
                                    .addComponent(labelDevFoldDestTxt1)
                                    .addGap(23, 23, 23)
                                    .addComponent(labelSrvInternet)))
                            .addGroup(panelServerLayout.createSequentialGroup()
                                .addComponent(labelSrvFolToBackTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(labelSrvFolToBackNumberTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(labelSrvFolToBack)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnResetServerSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelServerLayout.createSequentialGroup()
                                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(panelServerLayout.createSequentialGroup()
                                        .addComponent(labelDevGenCheckTxt3)
                                        .addGap(39, 39, 39)
                                        .addComponent(labelSrvUserInfo))
                                    .addGroup(panelServerLayout.createSequentialGroup()
                                        .addComponent(labelDevGenCheckTxt2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(labelSrvCheckInfo)))
                                .addGap(6, 6, 6)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSrvActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnSrvBackFold, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                        .addComponent(btnSrvPing, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSrvDeactivate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnSrvDownload, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                    .addComponent(btnSrvRefrSet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(24, 24, 24))
        );
        panelServerLayout.setVerticalGroup(
            panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(panelServerLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelServerLayout.createSequentialGroup()
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(srvStatusTrafficLight)
                            .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelServerPane)
                                .addComponent(labelSrvStatus))
                            .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(labelSrvOnOff)
                                .addComponent(labelSrvOnOffTxt)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelSrvUserInfo)
                            .addComponent(labelDevGenCheckTxt3)
                            .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelSrvFolToBackTxt)
                                .addComponent(labelSrvFolToBackNumberTxt))
                            .addComponent(labelSrvFolToBack))
                        .addGap(18, 18, 18)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelSrvCheckInfo)
                            .addComponent(labelDevGenCheckTxt2)
                            .addComponent(labelDevFoldDestTxt1)
                            .addComponent(labelSrvInternet))
                        .addGap(18, 18, 18)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnResetServerSettings, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                            .addGroup(panelServerLayout.createSequentialGroup()
                                .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelDevFoldDestTxt2)
                                    .addComponent(labelSrvConn))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(panelServerLayout.createSequentialGroup()
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSrvActivate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSrvDeactivate, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSrvDownload, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnSrvBackFold, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelServerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(btnSrvPing, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                            .addComponent(btnSrvRefrSet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Server", panelServer);

        historyTable.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Date", "File Name", "Size", "Backup Type", "Total timer", "Computer Name", "Upload timer"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(historyTable);
        if (historyTable.getColumnModel().getColumnCount() > 0) {
            historyTable.getColumnModel().getColumn(0).setPreferredWidth(55);
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(155);
            historyTable.getColumnModel().getColumn(2).setPreferredWidth(40);
            historyTable.getColumnModel().getColumn(3).setPreferredWidth(35);
            historyTable.getColumnModel().getColumn(4).setPreferredWidth(50);
            historyTable.getColumnModel().getColumn(6).setPreferredWidth(50);
        }

        btnRefreshHistory.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshHistory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/refresh-icon-20x20.png"))); // NOI18N
        btnRefreshHistory.setText("Refresh");
        btnRefreshHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshHistoryActionPerformed(evt);
            }
        });

        labelTotBackHistory.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelTotBackHistory.setText("Press Refresh to show backup history");

        javax.swing.GroupLayout panelHistoryLayout = new javax.swing.GroupLayout(panelHistory);
        panelHistory.setLayout(panelHistoryLayout);
        panelHistoryLayout.setHorizontalGroup(
            panelHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 931, Short.MAX_VALUE)
            .addGroup(panelHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRefreshHistory)
                .addGap(32, 32, 32)
                .addComponent(labelTotBackHistory)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panelHistoryLayout.setVerticalGroup(
            panelHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelHistoryLayout.createSequentialGroup()
                .addGroup(panelHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnRefreshHistory)
                    .addComponent(labelTotBackHistory))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("History", panelHistory);

        jLabel1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/gears-icon-40x40.png"))); // NOI18N
        jLabel1.setText("Total Status");

        labelTmpStatusName.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelTmpStatusName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/hourglass-icon-35x40.png"))); // NOI18N
        labelTmpStatusName.setText("Temporary Status");

        btnStart.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        btnStart.setForeground(new java.awt.Color(0, 0, 204));
        btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/start-icon-40x40.png"))); // NOI18N
        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);

        labelDevicePaneMain.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevicePaneMain.setForeground(new java.awt.Color(0, 0, 204));
        labelDevicePaneMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/HDD-USB-50x50.png"))); // NOI18N
        labelDevicePaneMain.setText("Device");

        labelDevStatusMain.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevStatusMain.setText("Status");

        devStatusTrafficLightMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/traffic-light-green-100x40.png"))); // NOI18N

        labelDevOnOffTxtMain.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevOnOffTxtMain.setText("On/Off");

        labelDevOnOffMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelDevOnOffMain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelDeviceOnOffMainEvent(evt);
            }
        });

        labelDevBackSize.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelDevBackSize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/database-icon-30x30.png"))); // NOI18N
        labelDevBackSize.setText("Backup Size");

        tmpLabelPercentage.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        tmpLabelPercentage.setForeground(new java.awt.Color(0, 0, 255));
        tmpLabelPercentage.setText("0%");

        tmpLabelStatus.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N

        tmpLabelPercentageTotal.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        tmpLabelPercentageTotal.setForeground(new java.awt.Color(0, 0, 255));
        tmpLabelPercentageTotal.setText("0%");

        labelLastUpdate.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelLastUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/hourglass-time-icon-40x40.png"))); // NOI18N
        labelLastUpdate.setText("Last Update: ");

        labelServerPane2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelServerPane2.setForeground(new java.awt.Color(0, 0, 204));
        labelServerPane2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/Server-Logo-50x50.png"))); // NOI18N
        labelServerPane2.setText("Server");

        labelSrvStatusMain1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvStatusMain1.setText("Status");

        srvStatusTrafficLightMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/traffic-light-green-100x40.png"))); // NOI18N

        labelSrvOnOffTxtMain.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvOnOffTxtMain.setText("On/Off");

        labelSrvOnOffMain.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/switch-on-icon-70x30.png"))); // NOI18N
        labelSrvOnOffMain.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                labelSrvOnOffMainlabelDeviceOnOffMainEvent(evt);
            }
        });

        labelSrvBackSize.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvBackSize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/database-icon-30x30.png"))); // NOI18N
        labelSrvBackSize.setText("Backup Size");

        labelSrvInfo.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        labelSrvInfo.setText("infoLabel");

        autopilotCheckOn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SBS/Icons/airplane-icon-57x40.png"))); // NOI18N

        labelError.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelError.setForeground(new java.awt.Color(255, 0, 0));
        labelError.setText("ERROR LABEL");

        lockLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        lockLabel.setForeground(new java.awt.Color(0, 0, 255));
        lockLabel.setText("LOCK LABEL");

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);

        backupInfoLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        backupInfoLabel.setText("OverSize");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelDevicePaneMain, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(12, 12, 12)
                                        .addComponent(labelDevStatusMain)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(devStatusTrafficLightMain))
                                    .addComponent(labelDevBackSize))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(labelDevOnOffTxtMain)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(labelDevOnOffMain))
                                    .addComponent(backupInfoLabel))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(labelServerPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(10, 10, 10)
                                        .addComponent(labelSrvStatusMain1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelSrvBackSize)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(srvStatusTrafficLightMain)
                                        .addGap(16, 16, 16)
                                        .addComponent(labelSrvOnOffTxtMain)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(labelSrvOnOffMain))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(labelSrvInfo))))
                            .addComponent(progressBarTotal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(progressBarTmp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(tmpLabelPercentageTotal)
                                .addGap(63, 63, 63)
                                .addComponent(labelLastUpdate)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(autopilotCheckOn)
                                .addGap(27, 27, 27)
                                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelTmpStatusName)
                                .addGap(51, 51, 51)
                                .addComponent(tmpLabelStatus)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(tmpLabelPercentage))
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelError)
                        .addGap(140, 140, 140)
                        .addComponent(lockLabel)
                        .addGap(241, 241, 241))))
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator4)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelDevicePaneMain, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelDevStatusMain))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addComponent(labelDevOnOffMain)
                                                .addComponent(labelDevOnOffTxtMain))
                                            .addComponent(devStatusTrafficLightMain))
                                        .addGap(12, 12, 12)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(labelDevBackSize)
                                    .addComponent(backupInfoLabel)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(labelServerPane2)
                                        .addComponent(labelSrvStatusMain1))
                                    .addComponent(srvStatusTrafficLightMain)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(9, 9, 9)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(labelSrvOnOffMain)
                                            .addComponent(labelSrvOnOffTxtMain))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(labelSrvInfo, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(labelSrvBackSize))))
                        .addGap(0, 12, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTmpStatusName)
                    .addComponent(tmpLabelStatus))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBarTmp, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tmpLabelPercentage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBarTotal, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autopilotCheckOn)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnStart)
                                .addComponent(labelLastUpdate))
                            .addComponent(tmpLabelPercentageTotal)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelError)
                            .addComponent(lockLabel))))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void labelDeviceOnOffMainEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelDeviceOnOffMainEvent
        //boolean lock= setting.getBoolValue("sbsLocked");
        boolean lockAndExpire= lockAndExpire();
        if(lockAndExpire){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        if(!setting.getBoolValue("deviceBackupActive")){
            if(devCheckSettings()){
                setting.SaveSetting("boolean", "deviceBackupActive", "true");
                setting.SaveSetting("boolean", "deviceBackupActiveBefore", "true");
            }
            else{
               JLabel msgLabel= new JLabel();
               msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
               msgLabel.setText("Cannot activate Device Backup, please resolve errors first");
               JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            setting.SaveSetting("boolean", "deviceBackupActive", "false");
            setting.SaveSetting("boolean", "deviceBackupActiveBefore", "false");
        }
        devCheckSettings();
    }//GEN-LAST:event_labelDeviceOnOffMainEvent

    private void labelDeviceOnOffEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelDeviceOnOffEvent
        //boolean lock= setting.getBoolValue("sbsLocked");
        boolean lockAndExpire= lockAndExpire();
        if(lockAndExpire){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        if(!setting.getBoolValue("deviceBackupActive")){
            if(devCheckSettings()){
                setting.SaveSetting("boolean", "deviceBackupActive", "true");
                setting.SaveSetting("boolean", "deviceBackupActiveBefore", "true");
            }
            else{
               JLabel msgLabel= new JLabel();
               msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
               msgLabel.setText("Cannot activate Device Backup, please resolve errors first");
               JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            setting.SaveSetting("boolean", "deviceBackupActive", "false");
            setting.SaveSetting("boolean", "deviceBackupActiveBefore", "false");
        }
        devCheckSettings();
    }//GEN-LAST:event_labelDeviceOnOffEvent

    private void btnResetDeviceSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetDeviceSettingsActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Reset Device Settings?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            setting.SaveSetting("bool", "syncDevSrvBackup", "false");
            resetDeviceSettings();
        }
        devCheckSettings();
    }//GEN-LAST:event_btnResetDeviceSettingsActionPerformed

    private void btnDevDaysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevDaysActionPerformed
        DaysLimit daysCh= new DaysLimit(this, true, "device");
        daysCh.setVisible(true);
        devCheckSettings();
    }//GEN-LAST:event_btnDevDaysActionPerformed

    private void btnDevRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevRefreshActionPerformed
        devCheckSettings();
        mainCheckSettings(false);
    }//GEN-LAST:event_btnDevRefreshActionPerformed

    private void btnDevDeactivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevDeactivateActionPerformed
        setting.SaveSetting("boolean", "deviceBackupActive", "false");
        setting.SaveSetting("boolean", "deviceBackupActiveBefore", "false");
        devCheckSettings();
    }//GEN-LAST:event_btnDevDeactivateActionPerformed

    private void btnDevFoldDestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevFoldDestActionPerformed
        DestinationFolder destCh= new DestinationFolder(this, true, "device");
        destCh.setVisible(true);
        devCheckSettings();
    }//GEN-LAST:event_btnDevFoldDestActionPerformed

    private void btnDevBackFoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevBackFoldActionPerformed
        FolderChooser fldCh= new FolderChooser(this, true, "device");
        fldCh.setVisible(true);
        devCheckSettings();
        if(setting.getBoolValue("syncDevSrvBackup") && !setting.getBoolValue("srvError")){
            mainCheckSettings(true);
        }
    }//GEN-LAST:event_btnDevBackFoldActionPerformed

    private void btnDevActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDevActivateActionPerformed
        //boolean lock= setting.getBoolValue("sbsLocked");
        boolean lockAndExpire= lockAndExpire();
        if(lockAndExpire){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        boolean checkSetting= devCheckSettings();
        if(checkSetting){
            setting.SaveSetting("boolean", "deviceBackupActive", "true");
            setting.SaveSetting("boolean", "deviceBackupActiveBefore", "true");
        }
        else{
           JLabel msgLabel= new JLabel();
           msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
           msgLabel.setText("Cannot activate Device Backup, please resolve errors first");
           JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        devCheckSettings();
    }//GEN-LAST:event_btnDevActivateActionPerformed

    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
        if(!setting.getBoolValue("deviceBackupActive") && !setting.getBoolValue("serverBackupActive")){
            JLabel msgLabel= new JLabel();
           msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
           msgLabel.setText("Activate at least one backup");
           JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        start();
    }//GEN-LAST:event_btnStartActionPerformed

    private void btnRefreshHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshHistoryActionPerformed
        updateHistoryTable();
    }//GEN-LAST:event_btnRefreshHistoryActionPerformed

    private void labelSrvOnOfflabelDeviceOnOffEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelSrvOnOfflabelDeviceOnOffEvent
        boolean lock= setting.getBoolValue("sbsLocked");
        if(lock){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        if(!setting.getBoolValue("serverBackupActive")){
            if(mainCheckSettings(true)){
                setting.SaveSetting("boolean", "serverBackupActive", "true");
                setting.SaveSetting("boolean", "serverBackupActiveBefore", "true");
            }
            else{
               JLabel msgLabel= new JLabel();
               msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
               msgLabel.setText("Cannot activate Server Backup, please resolve errors first");
               JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            setting.SaveSetting("boolean", "serverBackupActive", "false");
            setting.SaveSetting("boolean", "serverBackupActiveBefore", "false");
        }
        mainCheckSettings(true);
    }//GEN-LAST:event_labelSrvOnOfflabelDeviceOnOffEvent

    private void btnResetServerSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetServerSettingsActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Reset Server Settings?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            setting.SaveSetting("bool", "syncDevSrvBackup", "false");
            resetServerSettings();
        }
        mainCheckSettings(true);
    }//GEN-LAST:event_btnResetServerSettingsActionPerformed

    private void btnSrvActivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvActivateActionPerformed
        boolean lock= setting.getBoolValue("sbsLocked");
        if(lock){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        boolean checkSetting= mainCheckSettings(true);
        if(checkSetting){
            setting.SaveSetting("boolean", "serverBackupActive", "true");
            setting.SaveSetting("boolean", "serverBackupActiveBefore", "true");
        }
        else{
           JLabel msgLabel= new JLabel();
           msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
           msgLabel.setText("Cannot activate Server Backup, please resolve errors first");
           JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        mainCheckSettings(true);
    }//GEN-LAST:event_btnSrvActivateActionPerformed

    private void btnSrvDeactivateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvDeactivateActionPerformed
        setting.SaveSetting("boolean", "serverBackupActive", "false");
        setting.SaveSetting("boolean", "serverBackupActiveBefore", "false");
        mainCheckSettings(true);
    }//GEN-LAST:event_btnSrvDeactivateActionPerformed

    private void btnSrvBackFoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvBackFoldActionPerformed
        FolderChooser fldCh= new FolderChooser(this, true, "server");
        fldCh.setVisible(true);
        mainCheckSettings(true);
        if(setting.getBoolValue("syncDevSrvBackup")){
            devCheckSettings();
        }
    }//GEN-LAST:event_btnSrvBackFoldActionPerformed

    private void btnSrvPingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvPingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSrvPingActionPerformed

    private void labelSrvOnOffMainlabelDeviceOnOffMainEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelSrvOnOffMainlabelDeviceOnOffMainEvent
        boolean lock= setting.getBoolValue("sbsLocked");
        if(lock){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        if(!setting.getBoolValue("serverBackupActive")){
            if(mainCheckSettings(true)){
                setting.SaveSetting("boolean", "serverBackupActive", "true");
                setting.SaveSetting("boolean", "serverBackupActiveBefore", "true");
            }
            else{
               JLabel msgLabel= new JLabel();
               msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
               msgLabel.setText("Cannot activate Server Backup, please resolve errors first");
               JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            setting.SaveSetting("boolean", "serverBackupActive", "false");
            setting.SaveSetting("boolean", "serverBackupActiveBefore", "false");
        }
        mainCheckSettings(true);
    }//GEN-LAST:event_labelSrvOnOffMainlabelDeviceOnOffMainEvent

    private void btnSrvRefrSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvRefrSetActionPerformed
        mainCheckSettings(false);
    }//GEN-LAST:event_btnSrvRefrSetActionPerformed

    private void btnResetMainSettActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetMainSettActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Reset All Settings?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            resetMainSettings();
            resetDeviceSettings();
            resetServerSettings();
        }
        mainCheckSettings(false);
        devCheckSettings();
    }//GEN-LAST:event_btnResetMainSettActionPerformed

    private void labelOnOffBtnPilotMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelOnOffBtnPilotMouseClicked
        boolean lockAndExpire= lockAndExpire();
        if(lockAndExpire){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("SBS is Locked");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(lockIcon)));
            return;
        }
        boolean pilotOn= setting.getBoolValue("autopilotActive");
        boolean activatable= setting.getBoolValue("deviceBackupActive") || setting.getBoolValue("serverBackupActive");
        if(pilotOn){
            deactivateAutopilot();
        }
        else{
            if(!activatable){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Activate Device and/or Server Backup before enable Autopilot");
                JOptionPane.showMessageDialog(rootPane, msgLabel, " Autopilot Info", HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
            String days= setting.getStingValue("autoPilotDays");
            String time= setting.getStingValue("autoPilotTime");
            boolean emptyData= days==null || time==null || days.length()<2 || time.length()<2;
            if(activatable && !emptyData){
                setting.SaveSetting("boolean", "autopilotActive", "true");
                setting.SaveSetting("boolean", "autopilotActiveBefore", "true");
            }
            if(emptyData && activatable){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Set Days and Time before enable Autopilot");
                JOptionPane.showMessageDialog(rootPane, msgLabel, " Autopilot Info", HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        autopilotCheckSettings();
        if(!setting.getBoolValue("sbsLocked")) mainCheckSettings(true);
    }//GEN-LAST:event_labelOnOffBtnPilotMouseClicked

    private void btnAutopilotSettActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAutopilotSettActionPerformed
        AutopilotGUI autoPilot= new AutopilotGUI(this, true);
        autoPilot.setVisible(true);
        if(!setting.getBoolValue("sbsLocked")) mainCheckSettings(true);
    }//GEN-LAST:event_btnAutopilotSettActionPerformed

    private void btnRefreshMainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshMainActionPerformed
        mainCheckSettings(false);
        devCheckSettings();
    }//GEN-LAST:event_btnRefreshMainActionPerformed

    private void btnUsrInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUsrInfoActionPerformed
        UserInfoGUI usrInfoGui= new UserInfoGUI(this, true);
        usrInfoGui.setVisible(true);
        mainCheckSettings(false);
    }//GEN-LAST:event_btnUsrInfoActionPerformed

    private void btnSrvInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvInfoActionPerformed
        ServerInfoGUI srvInfo= new ServerInfoGUI(this, true);
        srvInfo.setVisible(true);
        mainCheckSettings(false);
    }//GEN-LAST:event_btnSrvInfoActionPerformed

    private void btnSbsPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSbsPassActionPerformed
        PasswordGUI passGui= new PasswordGUI(this, true);
        passGui.setVisible(true);
        //mainCheckSettings(true);
    }//GEN-LAST:event_btnSbsPassActionPerformed

    private void btnCopyHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCopyHistoryActionPerformed
        CopyHistoryGUI cpHist= new CopyHistoryGUI(this, true);
        cpHist.setVisible(true);
        mainCheckSettings(true);
    }//GEN-LAST:event_btnCopyHistoryActionPerformed

    private void checkRestartPCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkRestartPCActionPerformed
        boolean restartActive= setting.getBoolValue("restartPc");
        boolean shutActive= setting.getBoolValue("shutdownPc");
        if(!restartActive){
            if(shutActive){
                setting.SaveSetting("bool", "shutdownPc", "false");
                checkShutPC.setSelected(false);
            }
            setting.SaveSetting("bool", "restartPc", "true");
            checkRestartPC.setSelected(true);
        }
        else{
            setting.SaveSetting("bool", "restartPc", "false");
            checkRestartPC.setSelected(false);
        } 
        //mainCheckSettings(false);
    }//GEN-LAST:event_checkRestartPCActionPerformed

    private void checkShutPCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkShutPCActionPerformed
        boolean shutActive= setting.getBoolValue("shutdownPc");
        boolean restartActive= setting.getBoolValue("restartPc");
        if(!shutActive){
            if(restartActive){
                setting.SaveSetting("bool", "restartPc", "false"); 
                checkRestartPC.setSelected(false);
            }
            setting.SaveSetting("bool", "shutdownPc", "true");
            checkShutPC.setSelected(true);
        }
        else{
            setting.SaveSetting("bool", "shutdownPc", "false");
            checkShutPC.setSelected(false);
        }
        //mainCheckSettings(false);
    }//GEN-LAST:event_checkShutPCActionPerformed

    private void checkAutoStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAutoStartActionPerformed
        boolean isActive= setting.getBoolValue("autoStart");
        if(isActive){
            boolean autoPilot= setting.getBoolValue("autopilotActive");
            if(autoPilot){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Cannot deactivate Auto Start while Autopilot is enabled");
                JOptionPane.showMessageDialog(rootPane, msgLabel, " Autopilot Info", HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
            else{
                setting.SaveSetting("bool", "autoStart", "false");
                checkAutoStart.setSelected(false);
            }
        }
        else{
            setting.SaveSetting("bool", "autoStart", "true");
            checkAutoStart.setSelected(true);
        }
        //mainCheckSettings(false);
    }//GEN-LAST:event_checkAutoStartActionPerformed

    private void btnSrvDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSrvDownloadActionPerformed
        DownloadGUI dwnGui= new DownloadGUI(this, true);
        dwnGui.setVisible(true);
        mainCheckSettings(false);
    }//GEN-LAST:event_btnSrvDownloadActionPerformed

    private void labelAudioMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_labelAudioMouseClicked
        if(setting.getBoolValue("autopilotAudio")) setting.SaveSetting("bool", "autopilotAudio", "false");
        else setting.SaveSetting("bool", "autopilotAudio", "true");
        autopilotCheckSettings();
    }//GEN-LAST:event_labelAudioMouseClicked

    private void checkMinimizedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMinimizedActionPerformed
        boolean min= setting.getBoolValue("startMinimized");
        if(min){
            setting.SaveSetting("boolean", "startMinimized", "false");
            checkMinimized.setSelected(false);
        }
        else{
            setting.SaveSetting("boolean", "startMinimized", "true");
            checkMinimized.setSelected(true);
        }
        //mainCheckSettings(false);
    }//GEN-LAST:event_checkMinimizedActionPerformed

    private void windowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_windowClosing
        if(!setting.getBoolValue("sbsMessageTrayAgain")){
            SystemTrayWarning stwClass= new SystemTrayWarning(this, true);
            Thread stwThread= new Thread(stwClass);
            stwThread.start();
        }
    }//GEN-LAST:event_windowClosing

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
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainGUI().setVisible(true);
            }
        });
    }
    
    //***************ICONS DATABASE START***************
        String trafficRedBulb= "/SBS/Icons/Traffic-light-red-logo-30x30.png";
        String trafficGreenBulb= "/SBS/Icons/Traffic-light-green-logo-30x30.png";
        String trafficYellowBulb= "/SBS/Icons/Traffic-light-yellow-logo-30x30.png";
        String trafficLightsGreen= "/SBS/Icons/traffic-light-green-100x40.png";
        String trafficLightsRed= "/SBS/Icons/traffic-light-red-100x40.png";
        String trafficLightsYellow= "/SBS/Icons/traffic-light-yellow-100x40.png";
        String labelDeviceOn= "/SBS/Icons/switch-on-icon-70x30.png";
        String labelDeviceOff= "/SBS/Icons/switch-off-icon-70x30.png";
        String warningIcon= "/SBS/Icons/warning_logo_50x45.png";
        String errorIcon= "/SBS/Icons/error-logo-50x50.png";
        String offlineIcon= "/SBS/Icons/disconnect_icon-40x40.png";
        String labelPilotOnBtn= "/SBS/Icons/on-btn-black-green-40x40.png";
        String labelPilotOffBtn= "/SBS/Icons/off-btn-black-red-40x40.png";
        String labelPilotOffCheck= "/SBS/Icons/off-btn-black-50x50.png";
        String labelPilotOnCheck= "/SBS/Icons/on-btn-black-50x50.png";
        String autoPilotPlane= "/SBS/Icons/airplane-icon-71x50.png";
        String autoPilotPlaneCheck= "/SBS/Icons/airplane-icon-57x40.png";
        String autoPilotPlaneOff= "/SBS/Icons/airplane-icon-no-off-67x50.png";
        String usrErrorIcon= "/SBS/Icons/user-error-icon-40x40.png";
        String usrExpiredIcon= "/SBS/Icons/expired-icon-40x40.png";
        String pcErrorIcon= "/SBS/Icons/pc-error-icon-40x40.png";
        String lockIcon= "/SBS/Icons/lock-icon-30x40.png";
        String serverErrorIcon= "/SBS/Icons/server-error-icon-40x40.png";
        String serverInfoErrorIcon= "/SBS/Icons/server-info-error-40x40.png";
        String internetError= "/SBS/Icons/no-error-internet-40x40.png";
        String audioOn= "/SBS/Icons/audio-on-icon-30x30.png";
        String audioOff= "/SBS/Icons/audio-off-icon-30x30.png";
        String overSize= "/SBS/Icons/over-size-speed-icon-48x30.png";
        String storageSize= "/SBS/Icons/storage-icon-3-30x30.png";
        String robotHello= "/SBS/Icons/robot-hello-70x70.png";
        String sbsIcon= "/sbs-1.0-icon-40x40.png";
                
        //new ImageIcon(getClass().getResource(ICON))
    //***************ICONS DATABASE END*****************
        
        
    //***************SOUNDS DATABASE START***************
        //String autopilotDisSound= "SBS_Autopilot_Disconnect_sound.wav";
        String autopilotDisSound= "/SBS_Autopilot_Disconnect_sound.wav";
    //***************SOUNDS DATABASE END*****************

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel autopilotCheckOn;
    private javax.swing.JLabel backupInfoLabel;
    private javax.swing.JButton btnAutopilotSett;
    private javax.swing.JButton btnCopyHistory;
    private javax.swing.JButton btnDevActivate;
    private javax.swing.JButton btnDevBackFold;
    private javax.swing.JButton btnDevDays;
    private javax.swing.JButton btnDevDeactivate;
    private javax.swing.JButton btnDevFoldDest;
    private javax.swing.JButton btnDevRefresh;
    private javax.swing.JButton btnRefreshHistory;
    private javax.swing.JButton btnRefreshMain;
    private javax.swing.JButton btnResetDeviceSettings;
    private javax.swing.JButton btnResetMainSett;
    private javax.swing.JButton btnResetServerSettings;
    private javax.swing.JButton btnSbsPass;
    private javax.swing.JButton btnSrvActivate;
    private javax.swing.JButton btnSrvBackFold;
    private javax.swing.JButton btnSrvDeactivate;
    private javax.swing.JButton btnSrvDownload;
    private javax.swing.JButton btnSrvInfo;
    private javax.swing.JButton btnSrvPing;
    private javax.swing.JButton btnSrvRefrSet;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnUsrInfo;
    private javax.swing.JCheckBox checkAutoStart;
    private javax.swing.JCheckBox checkMinimized;
    private javax.swing.JCheckBox checkRestartPC;
    private javax.swing.JCheckBox checkShutPC;
    private javax.swing.JLabel devStatusTrafficLight;
    private javax.swing.JLabel devStatusTrafficLightMain;
    private javax.swing.JLabel expireDateLabel;
    private javax.swing.JTable historyTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelAudio;
    private javax.swing.JLabel labelAutoPilotDays;
    private javax.swing.JLabel labelAutoPilotTime;
    private javax.swing.JLabel labelDevBackSize;
    private javax.swing.JLabel labelDevDayLim;
    private javax.swing.JLabel labelDevDayLimTxt;
    private javax.swing.JLabel labelDevDayLimitTxt;
    private javax.swing.JLabel labelDevFolToBack;
    private javax.swing.JLabel labelDevFolToBackNumberTxt;
    private javax.swing.JLabel labelDevFolToBackTxt;
    private javax.swing.JLabel labelDevFoldDest;
    private javax.swing.JLabel labelDevFoldDestTxt;
    private javax.swing.JLabel labelDevFoldDestTxt1;
    private javax.swing.JLabel labelDevFoldDestTxt2;
    private javax.swing.JLabel labelDevFoldDestTxt3;
    private javax.swing.JLabel labelDevGenCheck;
    private javax.swing.JLabel labelDevGenCheckTxt;
    private javax.swing.JLabel labelDevGenCheckTxt2;
    private javax.swing.JLabel labelDevGenCheckTxt3;
    private javax.swing.JLabel labelDevGenCheckTxt4;
    private javax.swing.JLabel labelDevGenCheckTxt5;
    private javax.swing.JLabel labelDevOnOff;
    private javax.swing.JLabel labelDevOnOffMain;
    private javax.swing.JLabel labelDevOnOffTxt;
    private javax.swing.JLabel labelDevOnOffTxtMain;
    private javax.swing.JLabel labelDevOnOffTxtMain1;
    private javax.swing.JLabel labelDevStatus;
    private javax.swing.JLabel labelDevStatusMain;
    private javax.swing.JLabel labelDevicePane;
    private javax.swing.JLabel labelDevicePane1;
    private javax.swing.JLabel labelDevicePaneMain;
    private javax.swing.JLabel labelError;
    private javax.swing.JLabel labelLastUpdate;
    private javax.swing.JLabel labelMainExpire;
    private javax.swing.JLabel labelMainInternet;
    private javax.swing.JLabel labelMainUserInfo;
    private javax.swing.JLabel labelOnOffBtnPilot;
    private javax.swing.JLabel labelOnOffPilotCheck;
    private javax.swing.JLabel labelPilotNextBack;
    private javax.swing.JLabel labelPilotSchedule;
    private javax.swing.JLabel labelPilotStatus;
    private javax.swing.JLabel labelServerPane;
    private javax.swing.JLabel labelServerPane2;
    private javax.swing.JLabel labelSrvBackSize;
    private javax.swing.JLabel labelSrvCheckInfo;
    private javax.swing.JLabel labelSrvConn;
    private javax.swing.JLabel labelSrvFolToBack;
    private javax.swing.JLabel labelSrvFolToBackNumberTxt;
    private javax.swing.JLabel labelSrvFolToBackTxt;
    private javax.swing.JLabel labelSrvInfo;
    private javax.swing.JLabel labelSrvInternet;
    private javax.swing.JLabel labelSrvOnOff;
    private javax.swing.JLabel labelSrvOnOffMain;
    private javax.swing.JLabel labelSrvOnOffTxt;
    private javax.swing.JLabel labelSrvOnOffTxtMain;
    private javax.swing.JLabel labelSrvStatus;
    private javax.swing.JLabel labelSrvStatusMain1;
    private javax.swing.JLabel labelSrvUserInfo;
    private javax.swing.JLabel labelTmpStatusName;
    private javax.swing.JLabel labelTotBackHistory;
    private javax.swing.JLabel lockLabel;
    private javax.swing.JPanel panelDevice;
    private javax.swing.JPanel panelHistory;
    private javax.swing.JPanel panelMain;
    private javax.swing.JPanel panelServer;
    private javax.swing.JProgressBar progressBarTmp;
    private javax.swing.JProgressBar progressBarTotal;
    private javax.swing.JLabel srvStatusTrafficLight;
    private javax.swing.JLabel srvStatusTrafficLightMain;
    private javax.swing.JLabel tmpLabelPercentage;
    private javax.swing.JLabel tmpLabelPercentageTotal;
    private javax.swing.JLabel tmpLabelStatus;
    // End of variables declaration//GEN-END:variables
}
