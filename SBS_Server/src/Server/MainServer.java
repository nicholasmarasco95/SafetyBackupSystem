/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author nicho
 */
public class MainServer extends javax.swing.JFrame {

    /**
     * Creates new form MainServer
     */
    private ServerConnect srvCon= null;
    private String selUsr= null;
    private int connPort=-1;
    private int filePort=-1;
    private String remotePass= "";
    private String ip;
    private Settings setting= new Settings();
    
    public MainServer() {
        initComponents();
        if(setting.getBoolValue("srvRestartCheck")) start();
        if(setting.getBoolValue("serverAutoOnline") && !setting.getBoolValue("srvRestartCheck")) start();
        setting.SaveSetting("bool", "srvRestartCheck", "false");
        if(!setting.getBoolValue("serverStartMinimized")) checkPassword();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        stopBtn.setEnabled(false);
        btnUpUsr.setEnabled(false);
        labelInfo.setText("");
        cmrNumberLabel.setText("");
        activeConnLabel.setText("0");
        refreshSettings();
        
        setting.SaveSetting("string", "serverSoftwareName", "SBS_server_1.0.jar");
        setting.SaveSetting("string", "serverShortcutName", "./SBS_server_1.0.lnk");
        setting.SaveSetting("bool", "writeFileSem", "false");
        
        SystemTrayMethod();
        if(setting.getBoolValue("serverStartMinimized")){
            SwingUtilities.getWindowAncestor(this).setVisible(false);
            //setVisible(false);
        }
        
        if(setting.getBoolValue("serverAutoOnline")) start();
        
    }
    
    private boolean refreshSettings(){
        boolean checkFlag= true;
        
        //************IP CHECK************
        if(getIp()){
           srvIpLabel.setForeground(Color.blue);
           srvIpRemoteLabel.setForeground(Color.blue);
           srvIpLabel.setText(ip);
           srvIpRemoteLabel.setText(ip);
        }
        else{
            labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(offlineIcon)));
            labelInfo.setForeground(Color.red);
            labelInfo.setText("No internet Connection");
            srvIpLabel.setForeground(Color.red);
            srvIpLabel.setText("No internet Connection");
            srvIpRemoteLabel.setForeground(Color.red);
            srvIpRemoteLabel.setText("No internet Connection");
            checkFlag= false;
        }
        
        //************PORT CHECK************
        if(getPortSett()){
            connPortLabel.setForeground(Color.blue);
            connPortLabel.setText(Integer.toString(connPort));
            filePortLabel.setForeground(Color.blue);
            filePortLabel.setText(Integer.toString(filePort));
            if(checkFlag){
                labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(engineStartIcon)));
                labelInfo.setForeground(Color.blue);
                labelInfo.setText("Server Ready");
            }
        }
        else{
            if(checkFlag){
                labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(brakeIcon)));
                labelInfo.setForeground(Color.red);
                labelInfo.setText("Port Error");
                connPortLabel.setForeground(Color.red);
                connPortLabel.setText("NOT SET");
                filePortLabel.setForeground(Color.red);
                filePortLabel.setText("NOT SET");
                checkFlag= false;
            }
        }
        
        //************REMOTE PORT CHECK************
        if(getRemotePortSett()){
            remotePassLabel.setForeground(Color.blue);
            remotePassLabel.setText("SET");
            if(checkFlag){
                if(setting.getBoolValue("srvRemoteActive")){
                    remoteOnOffHome.setIcon(new ImageIcon(getClass().getResource(turnOnIcon)));
                    remoteSwitchOnOff.setIcon(new ImageIcon(getClass().getResource(SwitchOnIcon)));
                }
                else{
                    remoteOnOffHome.setIcon(new ImageIcon(getClass().getResource(turnOffIcon)));
                    remoteSwitchOnOff.setIcon(new ImageIcon(getClass().getResource(SwitchOffIcon)));
                }
            }
        }
        else{
            setting.SaveSetting("bool", "srvRemoteActive", "false");
            remoteSwitchOnOff.setIcon(new ImageIcon(getClass().getResource(SwitchOffIcon)));
            remoteOnOffHome.setIcon(new ImageIcon(getClass().getResource(turnOffIcon)));
            remotePassLabel.setForeground(Color.red);
            remotePassLabel.setText("NOT SET");
        }
        
        //**************Auto Start**************
        boolean autoStart= setting.getBoolValue("autoStartServer");
        if(autoStart) checkAutoStart.setSelected(true);
        else checkAutoStart.setSelected(false);
        copyAutoStart();
        
        //**************Start Minimized**************
        boolean minimized= setting.getBoolValue("serverStartMinimized");
        if(minimized) checkMinimized.setSelected(true);
        else checkMinimized.setSelected(false);
        
        //**************Auto Online**************
        boolean autoOnline= setting.getBoolValue("serverAutoOnline");
        if(autoOnline) checkAutoOnline.setSelected(true);
        else checkAutoOnline.setSelected(false);
        
        //************STORAGE LIMIT************
        int limit= setting.getIntValue("srvGbLim");
        if(limit>0){
            labelLimitStorage.setForeground(Color.BLUE);
            labelLimitStorage.setText(Integer.toString(limit) + " GB");
        }
        else{
            labelLimitStorage.setForeground(Color.orange);
            labelLimitStorage.setText("OFF");
        }
        
        //************START/STOP BTN************
        if(srvCon==null){
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        }
        else{
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        }
        
        return checkFlag;
    }
    
    private void checkPassword(){
        boolean passOn= setting.getBoolValue("sbsPassActiveServer");
        if(passOn){
            EnterPasswordGUI enterPass= new EnterPasswordGUI(this, true);
            enterPass.setVisible(true);
            if(!setting.getBoolValue("sbsPassCorrectServer")) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
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
    
    //Image image = Toolkit.getDefaultToolkit().getImage(sbsServerIcon);
    
    Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(sbsServerIcon));

    //popupmenu
    PopupMenu trayPopupMenu = new PopupMenu();

    //1t menuitem for popupmenu
    MenuItem action = new MenuItem("Open");
    action.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            checkPassword();
            if(setting.getBoolValue("sbsPassActiveServer")){
                if(setting.getBoolValue("sbsPassCorrectServer")) setVisible(true);
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
            checkPassword();
            if(setting.getBoolValue("sbsPassActiveServer")){
                if(setting.getBoolValue("sbsPassCorrectServer")) System.exit(0);
            }
            else System.exit(0);
        }
    });
    trayPopupMenu.add(close);

    //setting tray icon
    TrayIcon trayIcon = new TrayIcon(image, "SBS Server 1.0", trayPopupMenu);
    //adjust to default size as per system recommendation 
    trayIcon.setImageAutoSize(true);
    
    trayIcon.addMouseListener(new MouseAdapter(){
        public void mouseClicked(MouseEvent e){
            if(e.getClickCount()==2){
                checkPassword();
                if(setting.getBoolValue("sbsPassActiveServer")){
                    if(setting.getBoolValue("sbsPassCorrectServer")) setVisible(true);
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
        boolean autoStart= setting.getBoolValue("autoStartServer");
        if(autoStart){
            Path linkFile= Paths.get(setting.getStingValue("serverShortcutName"));
            String startUpFold= System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            String destFileName= "\\" + setting.getStingValue("serverSoftwareName").replace(".jar", "") + ".lnk";
            Path destFold= Paths.get(startUpFold + destFileName);
            try {
                Files.copy(linkFile, destFold, LinkOption.NOFOLLOW_LINKS, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            String startUpFold= System.getProperty("java.io.tmpdir").replace("Local\\Temp\\", "Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup");
            String destFileName= "";
            try{
                destFileName= "\\" + setting.getStingValue("serverSoftwareName").replace(".jar", "") + ".lnk";
            }catch(Exception e){
                System.out.println(e);
            }
            Path destFold= Paths.get(startUpFold + destFileName);
            File tmpFile= new File(destFold.toString());
            if(tmpFile.exists()) tmpFile.delete();
        }
    }
    
    private boolean getRemotePortSett(){
        String tmpRemotePass= setting.getStingValue("srvRemotePass");
        if(tmpRemotePass!=null && tmpRemotePass.length()>2){
            remotePass= tmpRemotePass;
            return true;
        }
        remotePass= "";
        setting.SaveSetting("String", "srvRemotePass", "");
        return false;
    }
    
    private boolean getPortSett(){
        int tmpConnPort= setting.getIntValue("connPort");
        int tmpFilePort= setting.getIntValue("filePort");
        if(tmpConnPort>0 && tmpFilePort>0 && tmpConnPort!=tmpFilePort){
            connPort= tmpConnPort;
            filePort= tmpFilePort;
            return true;
        }
        filePort= -1;
        setting.SaveSetting("int", "filePort", "-1");
        connPort= -1;
        setting.SaveSetting("int", "connPort", "-1");
        return false;
    }
    
    private boolean getIp(){
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            ip = in.readLine(); //you get the IP as a String
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
    
    private void updateCmrTable(){
        CmrFileManager cmrFileMan= new CmrFileManager();
        List<String> listLines= cmrFileMan.getList();
        int listSize= listLines.size();
        if(listSize<0){
            cmrNumberLabel.setForeground(Color.red);
            cmrNumberLabel.setText("No Customers");
        }
        int rowNum= cmrTable.getRowCount();
        if(listSize>rowNum){
            DefaultTableModel model = (DefaultTableModel) cmrTable.getModel();
            model.setRowCount(listSize);
        }
        Iterator<String> itList= listLines.iterator();
        int rowCounter=0;
        while(itList.hasNext()){
            String tmpArray[]= itList.next().split(",");
            if(!tmpArray[0].equals("000")){
                cmrTable.getModel().setValueAt(tmpArray[0], rowCounter, 0);
                cmrTable.getModel().setValueAt(tmpArray[1], rowCounter, 1);
                cmrTable.getModel().setValueAt(tmpArray[2], rowCounter, 2);
                cmrTable.getModel().setValueAt(tmpArray[3], rowCounter, 3);
                cmrTable.getModel().setValueAt(tmpArray[5], rowCounter, 4);
                cmrTable.getModel().setValueAt(tmpArray[8], rowCounter, 5);
                cmrTable.getModel().setValueAt(tmpArray[4], rowCounter, 6);
                rowCounter++;
            }
            else{
                listSize-=1;
            }
        }
        if(listSize>0){
            cmrNumberLabel.setForeground(Color.blue);
            cmrNumberLabel.setText("Total Customers: " + listSize);
        }
    }
    
    private boolean checkPort(){
        if(connPort<=0 || connPort<1024 || connPort>5000 || filePort<=0 || filePort<1024 || filePort>5000) return false;
        return true;
    }
    
    private boolean getPortInput(){
        String connStr= connPortField.getText();
        String fileStr= filePortField.getText();
        if(connStr==null || connStr.length()<2 || fileStr==null || fileStr.length()<2) return false;
        int intConn=0;
        int intFile=0;
        try{
            intConn= Integer.parseInt(connStr);
            intFile= Integer.parseInt(fileStr);
        }catch  (NumberFormatException e) {
            return false;
        }
        if(intConn>5000 || intFile>5000 || intConn==intFile) return false;
        connPort= intConn;
        filePort= intFile;
        return true;
    }
    
    private boolean getRemotePortInput(){
        String tmpRemotePass= remotePassField.getText();
        if(tmpRemotePass==null || tmpRemotePass.length()<2) return false;
        remotePass= tmpRemotePass;
        return true;
    }
    
    private void start(){
        if(refreshSettings()){
            srvCon= new ServerConnect(connPort, filePort, false, activeConnLabel);
            Thread thr= new Thread(srvCon);
            thr.start();
            refreshSettings();
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Cannot Start server");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon))); 
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

        HistoryPanel = new javax.swing.JTabbedPane();
        MainSettingsPanel = new javax.swing.JPanel();
        btnSbsPass = new javax.swing.JButton();
        checkAutoStart = new javax.swing.JCheckBox();
        checkMinimized = new javax.swing.JCheckBox();
        checkAutoOnline = new javax.swing.JCheckBox();
        labelDevDayLimTxt = new javax.swing.JLabel();
        labelLimitStorage = new javax.swing.JLabel();
        btnStorageLimit = new javax.swing.JButton();
        ServerSettingsPanel = new javax.swing.JPanel();
        connPortField = new javax.swing.JTextField();
        setPortLabel1 = new javax.swing.JLabel();
        storageSettingsLabelTxt = new javax.swing.JLabel();
        connPortSaveBtn = new javax.swing.JButton();
        connPortLabelTxt = new javax.swing.JLabel();
        connPortLabel = new javax.swing.JLabel();
        srvIpLabelTxt = new javax.swing.JLabel();
        srvIpLabel = new javax.swing.JLabel();
        filePortLabelTxt = new javax.swing.JLabel();
        filePortLabel = new javax.swing.JLabel();
        setPortLabel2 = new javax.swing.JLabel();
        filePortField = new javax.swing.JTextField();
        CustomersPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cmrTable = new javax.swing.JTable();
        btnRefreshCmr = new javax.swing.JButton();
        cmrNumberLabel = new javax.swing.JLabel();
        btnAddUser = new javax.swing.JButton();
        btnUpUsr = new javax.swing.JButton();
        RemotePanel = new javax.swing.JPanel();
        remoteSettingsLabelTxt = new javax.swing.JLabel();
        srvIpRemoteLabelTxt = new javax.swing.JLabel();
        srvIpRemoteLabel = new javax.swing.JLabel();
        remotePortSaveBtn = new javax.swing.JButton();
        remoteOnOffLabel = new javax.swing.JLabel();
        remoteSwitchOnOff = new javax.swing.JLabel();
        remotePassLabelTxt = new javax.swing.JLabel();
        remotePassLabel = new javax.swing.JLabel();
        remoteSetPassLabel = new javax.swing.JLabel();
        remotePassField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        userIdLabel = new javax.swing.JLabel();
        historyUsrIdField = new javax.swing.JTextField();
        showBtn = new javax.swing.JButton();
        backupNumberLabel = new javax.swing.JLabel();
        statusInfoLabelTxt = new javax.swing.JLabel();
        serverStatusLabelTxt = new javax.swing.JLabel();
        labelSrvStatus = new javax.swing.JLabel();
        activeConnectionLabelTxt = new javax.swing.JLabel();
        activeConnLabel = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        startBtn = new javax.swing.JButton();
        stopBtn = new javax.swing.JButton();
        labelInfo = new javax.swing.JLabel();
        btnRefreshSet = new javax.swing.JButton();
        remoteLabelHome = new javax.swing.JLabel();
        remoteOnOffHome = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        btnSbsPass.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSbsPass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/lock-icon-30x30.png"))); // NOI18N
        btnSbsPass.setText("Lock Password");
        btnSbsPass.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSbsPass.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSbsPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSbsPassActionPerformed(evt);
            }
        });

        checkAutoStart.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        checkAutoStart.setText("Auto start");
        checkAutoStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAutoStartActionPerformed(evt);
            }
        });

        checkMinimized.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        checkMinimized.setText("Start minimized");
        checkMinimized.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkMinimizedActionPerformed(evt);
            }
        });

        checkAutoOnline.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        checkAutoOnline.setText("Auto Online");
        checkAutoOnline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkAutoOnlineActionPerformed(evt);
            }
        });

        labelDevDayLimTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevDayLimTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/storage-icon-2-40x40.png"))); // NOI18N
        labelDevDayLimTxt.setText("Storage Limit");

        labelLimitStorage.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelLimitStorage.setForeground(new java.awt.Color(0, 0, 255));
        labelLimitStorage.setText("100  GB");

        btnStorageLimit.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnStorageLimit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/storage-icon-2-30x30.png"))); // NOI18N
        btnStorageLimit.setText("Storage Limit");
        btnStorageLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStorageLimitActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout MainSettingsPanelLayout = new javax.swing.GroupLayout(MainSettingsPanel);
        MainSettingsPanel.setLayout(MainSettingsPanelLayout);
        MainSettingsPanelLayout.setHorizontalGroup(
            MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(MainSettingsPanelLayout.createSequentialGroup()
                .addGroup(MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(MainSettingsPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkMinimized)
                            .addGroup(MainSettingsPanelLayout.createSequentialGroup()
                                .addComponent(labelDevDayLimTxt)
                                .addGap(18, 18, 18)
                                .addComponent(labelLimitStorage))
                            .addGroup(MainSettingsPanelLayout.createSequentialGroup()
                                .addComponent(checkAutoStart)
                                .addGap(163, 163, 163)
                                .addComponent(checkAutoOnline)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainSettingsPanelLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnStorageLimit)
                        .addGap(36, 36, 36)))
                .addComponent(btnSbsPass, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        MainSettingsPanelLayout.setVerticalGroup(
            MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, MainSettingsPanelLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkAutoStart)
                    .addComponent(checkAutoOnline))
                .addGap(18, 18, 18)
                .addComponent(checkMinimized)
                .addGap(18, 18, 18)
                .addGroup(MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDevDayLimTxt)
                    .addComponent(labelLimitStorage))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(MainSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSbsPass, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnStorageLimit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        HistoryPanel.addTab("Main Settings", MainSettingsPanel);

        setPortLabel1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        setPortLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/port_icon-30x30.png"))); // NOI18N
        setPortLabel1.setText("Set Port");

        storageSettingsLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        storageSettingsLabelTxt.setForeground(new java.awt.Color(0, 0, 255));
        storageSettingsLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/web-internet-world-icon-40x40.png"))); // NOI18N
        storageSettingsLabelTxt.setText("Connection Settings");

        connPortSaveBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        connPortSaveBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/save-floppy-icon-30x30.png"))); // NOI18N
        connPortSaveBtn.setText("Save");
        connPortSaveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connPortSaveBtnActionPerformed(evt);
            }
        });

        connPortLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        connPortLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/port_icon-40x40.png"))); // NOI18N
        connPortLabelTxt.setText("Conn Port:");

        connPortLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        connPortLabel.setForeground(new java.awt.Color(255, 0, 0));
        connPortLabel.setText("NOT SET");

        srvIpLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/ip_icon_40x40.png"))); // NOI18N
        srvIpLabelTxt.setText("Server IP:");

        srvIpLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpLabel.setForeground(new java.awt.Color(255, 0, 0));
        srvIpLabel.setText("NOT SET");

        filePortLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        filePortLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/port_icon-40x40.png"))); // NOI18N
        filePortLabelTxt.setText("File Port:");

        filePortLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        filePortLabel.setForeground(new java.awt.Color(255, 0, 0));
        filePortLabel.setText("NOT SET");

        setPortLabel2.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        setPortLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/port_icon-30x30.png"))); // NOI18N
        setPortLabel2.setText("Set Port");

        javax.swing.GroupLayout ServerSettingsPanelLayout = new javax.swing.GroupLayout(ServerSettingsPanel);
        ServerSettingsPanel.setLayout(ServerSettingsPanelLayout);
        ServerSettingsPanelLayout.setHorizontalGroup(
            ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                        .addComponent(srvIpLabelTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(srvIpLabel)
                        .addContainerGap(366, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ServerSettingsPanelLayout.createSequentialGroup()
                        .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                                .addComponent(storageSettingsLabelTxt)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(connPortSaveBtn))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ServerSettingsPanelLayout.createSequentialGroup()
                                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(connPortLabelTxt)
                                    .addComponent(filePortLabelTxt))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(filePortLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(setPortLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(filePortField, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                                        .addComponent(connPortLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(setPortLabel1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(connPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(16, 16, 16))))
        );
        ServerSettingsPanelLayout.setVerticalGroup(
            ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ServerSettingsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storageSettingsLabelTxt)
                    .addComponent(connPortSaveBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvIpLabelTxt)
                    .addComponent(srvIpLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connPortLabelTxt)
                    .addComponent(connPortLabel)
                    .addComponent(setPortLabel1)
                    .addComponent(connPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ServerSettingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filePortLabelTxt)
                    .addComponent(filePortLabel)
                    .addComponent(setPortLabel2)
                    .addComponent(filePortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        HistoryPanel.addTab("Server Settings", ServerSettingsPanel);

        cmrTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
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
                "User", "Name", "Last", "Expiry date", "Password", "Mac", "Email"
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
        cmrTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                selectedRow(evt);
            }
        });
        jScrollPane1.setViewportView(cmrTable);
        if (cmrTable.getColumnModel().getColumnCount() > 0) {
            cmrTable.getColumnModel().getColumn(0).setPreferredWidth(2);
            cmrTable.getColumnModel().getColumn(1).setPreferredWidth(50);
            cmrTable.getColumnModel().getColumn(2).setPreferredWidth(50);
            cmrTable.getColumnModel().getColumn(3).setPreferredWidth(55);
            cmrTable.getColumnModel().getColumn(4).setPreferredWidth(50);
            cmrTable.getColumnModel().getColumn(5).setPreferredWidth(5);
            cmrTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        }

        btnRefreshCmr.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshCmr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/refresh-icon-2-30x30.png"))); // NOI18N
        btnRefreshCmr.setText("Refresh");
        btnRefreshCmr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshCmrActionPerformed(evt);
            }
        });

        cmrNumberLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        cmrNumberLabel.setText("Tot Cmr");

        btnAddUser.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/admin-icon-30x30.png"))); // NOI18N
        btnAddUser.setText("Add User");
        btnAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddUserActionPerformed(evt);
            }
        });

        btnUpUsr.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnUpUsr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/user_update_edit_30x30.png"))); // NOI18N
        btnUpUsr.setText("Update User");
        btnUpUsr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpUsrActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout CustomersPanelLayout = new javax.swing.GroupLayout(CustomersPanel);
        CustomersPanel.setLayout(CustomersPanelLayout);
        CustomersPanelLayout.setHorizontalGroup(
            CustomersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
            .addGroup(CustomersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRefreshCmr)
                .addGap(18, 18, 18)
                .addComponent(cmrNumberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                .addComponent(btnAddUser)
                .addGap(29, 29, 29)
                .addComponent(btnUpUsr)
                .addContainerGap())
        );
        CustomersPanelLayout.setVerticalGroup(
            CustomersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, CustomersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(CustomersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(CustomersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRefreshCmr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmrNumberLabel))
                    .addComponent(btnUpUsr, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(CustomersPanelLayout.createSequentialGroup()
                        .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        HistoryPanel.addTab("Customers", CustomersPanel);

        remoteSettingsLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        remoteSettingsLabelTxt.setForeground(new java.awt.Color(0, 0, 255));
        remoteSettingsLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/remote-control-icon-40x40.png"))); // NOI18N
        remoteSettingsLabelTxt.setText("Remote Settings");

        srvIpRemoteLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpRemoteLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/ip_icon_40x40.png"))); // NOI18N
        srvIpRemoteLabelTxt.setText("Server IP:");

        srvIpRemoteLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpRemoteLabel.setForeground(new java.awt.Color(255, 0, 0));
        srvIpRemoteLabel.setText("NOT SET");

        remotePortSaveBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        remotePortSaveBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/save-floppy-icon-30x30.png"))); // NOI18N
        remotePortSaveBtn.setText("Save");
        remotePortSaveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remotePortSaveBtnActionPerformed(evt);
            }
        });

        remoteOnOffLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        remoteOnOffLabel.setText("On/Off");

        remoteSwitchOnOff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/switch-on-icon-70x30.png"))); // NOI18N
        remoteSwitchOnOff.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                remoteSwitchOnOffMouseClicked(evt);
            }
        });

        remotePassLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        remotePassLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/password-icon-40x40.png"))); // NOI18N
        remotePassLabelTxt.setText("Remote Pass:");

        remotePassLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        remotePassLabel.setForeground(new java.awt.Color(255, 0, 0));
        remotePassLabel.setText("NOT SET");

        remoteSetPassLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        remoteSetPassLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/password-icon-30x30.png"))); // NOI18N
        remoteSetPassLabel.setText("Set Pass");

        javax.swing.GroupLayout RemotePanelLayout = new javax.swing.GroupLayout(RemotePanel);
        RemotePanel.setLayout(RemotePanelLayout);
        RemotePanelLayout.setHorizontalGroup(
            RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RemotePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RemotePanelLayout.createSequentialGroup()
                        .addComponent(remoteSettingsLabelTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(remoteOnOffLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(remoteSwitchOnOff))
                    .addGroup(RemotePanelLayout.createSequentialGroup()
                        .addComponent(srvIpRemoteLabelTxt)
                        .addGap(37, 37, 37)
                        .addComponent(srvIpRemoteLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(RemotePanelLayout.createSequentialGroup()
                        .addComponent(remotePassLabelTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(remotePassLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 123, Short.MAX_VALUE)
                        .addComponent(remoteSetPassLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(remotePassField, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RemotePanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(remotePortSaveBtn)))
                .addContainerGap())
        );
        RemotePanelLayout.setVerticalGroup(
            RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RemotePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(remoteSwitchOnOff)
                    .addGroup(RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(remoteSettingsLabelTxt)
                        .addComponent(remoteOnOffLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvIpRemoteLabelTxt)
                    .addComponent(srvIpRemoteLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(RemotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(remotePassLabelTxt)
                    .addComponent(remotePassLabel)
                    .addComponent(remoteSetPassLabel)
                    .addComponent(remotePassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(remotePortSaveBtn)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        HistoryPanel.addTab("Remote", RemotePanel);

        historyTable.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N
        historyTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Date", "File Name", "Size"
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
        jScrollPane2.setViewportView(historyTable);
        if (historyTable.getColumnModel().getColumnCount() > 0) {
            historyTable.getColumnModel().getColumn(0).setPreferredWidth(30);
            historyTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            historyTable.getColumnModel().getColumn(2).setPreferredWidth(20);
        }

        userIdLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        userIdLabel.setForeground(new java.awt.Color(0, 0, 255));
        userIdLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/admin-icon-40x40.png"))); // NOI18N
        userIdLabel.setText("User ID");

        showBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        showBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/history-folder-30x30.png"))); // NOI18N
        showBtn.setText("Show");
        showBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBtnActionPerformed(evt);
            }
        });

        backupNumberLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 16)); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 584, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userIdLabel)
                .addGap(18, 18, 18)
                .addComponent(historyUsrIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(showBtn)
                .addGap(18, 18, 18)
                .addComponent(backupNumberLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userIdLabel)
                    .addComponent(historyUsrIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backupNumberLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE))
        );

        HistoryPanel.addTab("History", jPanel1);

        statusInfoLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        statusInfoLabelTxt.setForeground(new java.awt.Color(0, 0, 255));
        statusInfoLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/speed-icon-40x40.png"))); // NOI18N
        statusInfoLabelTxt.setText("Status Info");

        serverStatusLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        serverStatusLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/Server-Logo-40x40.png"))); // NOI18N
        serverStatusLabelTxt.setText("Server Status");

        labelSrvStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/check_engine_icon-40x29.png"))); // NOI18N

        activeConnectionLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        activeConnectionLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/web-internet-world-icon-40x40.png"))); // NOI18N
        activeConnectionLabelTxt.setText("Active Connection");

        activeConnLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 24)); // NOI18N
        activeConnLabel.setForeground(new java.awt.Color(0, 0, 204));
        activeConnLabel.setText("100");

        startBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        startBtn.setForeground(new java.awt.Color(0, 0, 255));
        startBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/on-power-start-icon-40x40.png"))); // NOI18N
        startBtn.setText("Start");
        startBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startBtnActionPerformed(evt);
            }
        });

        stopBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        stopBtn.setForeground(new java.awt.Color(255, 0, 0));
        stopBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/stop-icon-2-40x40.png"))); // NOI18N
        stopBtn.setText("Stop");
        stopBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopBtnActionPerformed(evt);
            }
        });

        labelInfo.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        labelInfo.setText("Info Error");

        btnRefreshSet.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/refresh-icon-30x30.png"))); // NOI18N
        btnRefreshSet.setText("Refresh Settings");
        btnRefreshSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshSetActionPerformed(evt);
            }
        });

        remoteLabelHome.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        remoteLabelHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/remote-control-icon-40x40.png"))); // NOI18N
        remoteLabelHome.setText("Remote");

        remoteOnOffHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Server/Icons/on-btn-black-40x40.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(HistoryPanel)
            .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(jSeparator4, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusInfoLabelTxt)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnRefreshSet)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(startBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(activeConnectionLabelTxt)
                                    .addComponent(serverStatusLabelTxt))
                                .addGap(20, 20, 20)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelSrvStatus)
                                    .addComponent(activeConnLabel))
                                .addGap(51, 51, 51)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(remoteLabelHome)
                                        .addGap(18, 18, 18)
                                        .addComponent(remoteOnOffHome))
                                    .addComponent(labelInfo))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(HistoryPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusInfoLabelTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serverStatusLabelTxt)
                    .addComponent(labelSrvStatus)
                    .addComponent(labelInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(remoteOnOffHome)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(activeConnectionLabelTxt)
                        .addComponent(activeConnLabel)
                        .addComponent(remoteLabelHome)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(startBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(stopBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnRefreshSet, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void startBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnActionPerformed
        start();
    }//GEN-LAST:event_startBtnActionPerformed

    private void btnRefreshCmrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshCmrActionPerformed
        updateCmrTable();
    }//GEN-LAST:event_btnRefreshCmrActionPerformed

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        AddUserGUI addUsr= new AddUserGUI(this,true);
        addUsr.setVisible(true);
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void selectedRow(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_selectedRow
        int selRow= cmrTable.getSelectedRow();
        selUsr= cmrTable.getValueAt(selRow, 0).toString();
        btnUpUsr.setEnabled(true);
    }//GEN-LAST:event_selectedRow

    private void btnUpUsrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpUsrActionPerformed
        if(selUsr!=null){
            UpdateUserGUI upUsr= new UpdateUserGUI(this, true, selUsr);
            upUsr.setVisible(true);
            cmrTable.clearSelection();
            selUsr= null;
            btnUpUsr.setEnabled(false);
        }
    }//GEN-LAST:event_btnUpUsrActionPerformed

    private void stopBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopBtnActionPerformed
        if(srvCon!=null){
            srvCon.closeScoket();
            srvCon=null;
            refreshSettings();
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Server not started");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            System.exit(1);
        }
        activeConnLabel.setText("0");
    }//GEN-LAST:event_stopBtnActionPerformed

    private void connPortSaveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connPortSaveBtnActionPerformed
        if(srvCon==null){
            if(getPortInput()){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Conn Port: " + connPort);
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                
                JLabel msgLabel2= new JLabel();
                msgLabel2.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel2.setText("File Port: " + filePort);
                JOptionPane.showMessageDialog(null, msgLabel2, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));

                setting.SaveSetting("int", "connPort", Integer.toString(connPort));
                setting.SaveSetting("int", "filePort", Integer.toString(filePort));
            }
            else{
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Port must be <5000 and connPort != filePort and connPort != remotePort && filePort != remotePort");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Stop Server before change settings");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        connPortField.setText("");
        filePortField.setText("");
        refreshSettings();
    }//GEN-LAST:event_connPortSaveBtnActionPerformed

    private void btnRefreshSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshSetActionPerformed
        refreshSettings();
    }//GEN-LAST:event_btnRefreshSetActionPerformed

    private void btnSbsPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSbsPassActionPerformed
        PasswordGUI passGui= new PasswordGUI(this, true);
        passGui.setVisible(true);
        refreshSettings();
    }//GEN-LAST:event_btnSbsPassActionPerformed

    private void checkAutoStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAutoStartActionPerformed
        boolean isActive= setting.getBoolValue("autoStartServer");
        if(isActive) setting.SaveSetting("bool", "autoStartServer", "false");
        else setting.SaveSetting("bool", "autoStartServer", "true");
        refreshSettings();
    }//GEN-LAST:event_checkAutoStartActionPerformed

    private void checkMinimizedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkMinimizedActionPerformed
        boolean min= setting.getBoolValue("serverStartMinimized");
        if(min) setting.SaveSetting("boolean", "serverStartMinimized", "false");
        else setting.SaveSetting("boolean", "serverStartMinimized", "true");
        refreshSettings();
    }//GEN-LAST:event_checkMinimizedActionPerformed

    private void checkAutoOnlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkAutoOnlineActionPerformed
        boolean min= setting.getBoolValue("serverAutoOnline");
        if(min) setting.SaveSetting("boolean", "serverAutoOnline", "false");
        else setting.SaveSetting("boolean", "serverAutoOnline", "true");
        refreshSettings();
    }//GEN-LAST:event_checkAutoOnlineActionPerformed

    private void btnStorageLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStorageLimitActionPerformed
        DaysLimit daysLimit= new DaysLimit(this, true);
        daysLimit.setVisible(true);
        refreshSettings();
    }//GEN-LAST:event_btnStorageLimitActionPerformed

    private void showBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBtnActionPerformed
        String usrId= historyUsrIdField.getText();
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        if(usrId.length()==0){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Enter User ID");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            historyUsrIdField.setText("");
            return;
        }
        try{
            Integer.parseInt(usrId);
        }catch  (NumberFormatException e) {
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("ID must be a number");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            historyUsrIdField.setText("");
            return;
        }
        UpdateHistory upHist= new UpdateHistory(usrId);
        List<String> listLines= upHist.getList();
        int listSize= listLines.size();
        if(listSize>0){
            backupNumberLabel.setForeground(Color.blue);
            backupNumberLabel.setText("User: " + usrId + "   Backups: " + listSize);
        }
        else{
            backupNumberLabel.setForeground(Color.red);
            backupNumberLabel.setText("User: " + usrId + "   No Backup");
            while(model.getRowCount() > 0)
            {
                model.removeRow(0);
            }
            for(int i=0; i<7; i++){
                model.addRow(new Object[]{null, null, null});
            }
        }
        int rowNum= historyTable.getRowCount();
        if(listSize>rowNum){
            model.setRowCount(listSize);
        }
        Iterator<String> itList= listLines.iterator();
        int rowCounter=0;
        while(itList.hasNext()){
            String tmpArray[]= itList.next().split(",");
            historyTable.getModel().setValueAt(tmpArray[0], rowCounter, 0);
            historyTable.getModel().setValueAt(tmpArray[1], rowCounter, 1);
            historyTable.getModel().setValueAt(tmpArray[2], rowCounter, 2);
            rowCounter++;
        }
        historyUsrIdField.setText("");
    }//GEN-LAST:event_showBtnActionPerformed

    private void remotePortSaveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remotePortSaveBtnActionPerformed
        if(srvCon==null){
            if(getRemotePortInput()){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Settings Saved");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));

                setting.SaveSetting("String", "srvRemotePass", remotePass); 
            }
            else{
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Port must be <5000 and != from connPort and filePort");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Stop Server before change settings");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        remotePassField.setText("");
        refreshSettings();
    }//GEN-LAST:event_remotePortSaveBtnActionPerformed

    private void remoteSwitchOnOffMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_remoteSwitchOnOffMouseClicked
        if(getRemotePortSett()){
            if(!setting.getBoolValue("srvRemoteActive")){
                setting.SaveSetting("bool", "srvRemoteActive", "true");
            }
            else{
                setting.SaveSetting("bool", "srvRemoteActive", "false");
            }
        }
        else{
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Please resolve errors first");
            JOptionPane.showMessageDialog(rootPane, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
        refreshSettings();
    }//GEN-LAST:event_remoteSwitchOnOffMouseClicked

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
            java.util.logging.Logger.getLogger(MainServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainServer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainServer().setVisible(true);
            }
        });
    }
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/Server/Icons/error-logo-50x50.png";
    String infoIcon="/Server/Icons/info-icon-50x50.png";
    String checkEngineIcon="/Server/Icons/check_engine_icon-40x29.png";
    String offlineIcon="/Server/Icons/disconnect_icon-40x40.png";
    String onlineIcon="/Server/Icons/globe-online-icon-40x40.png";
    String engineStartIcon="/Server/Icons/engine-start-big-icon-40x40.png";
    String brakeIcon="/Server/Icons/brake-light-red-57x40.png";
    String SwitchOnIcon= "/Server/Icons/switch-on-icon-70x30.png";
    String SwitchOffIcon= "/Server/Icons/switch-off-icon-70x30.png";
    String turnOnIcon= "/Server/Icons/on-btn-black-40x40.png";
    String turnOffIcon= "/Server/Icons/off-btn-black-40x40.png";
    //String sbsServerIcon= "src/Server/Icons/SBS-1.0-server-Logo-50x50.png";  //this icon has "src/" in its path
    String sbsServerIcon= "/Server-Logo-40x40.png";  //debug

    //***************ICONS DATABASE END*****************

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel CustomersPanel;
    private javax.swing.JTabbedPane HistoryPanel;
    private javax.swing.JPanel MainSettingsPanel;
    private javax.swing.JPanel RemotePanel;
    private javax.swing.JPanel ServerSettingsPanel;
    private javax.swing.JLabel activeConnLabel;
    private javax.swing.JLabel activeConnectionLabelTxt;
    private javax.swing.JLabel backupNumberLabel;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnRefreshCmr;
    private javax.swing.JButton btnRefreshSet;
    private javax.swing.JButton btnSbsPass;
    private javax.swing.JButton btnStorageLimit;
    private javax.swing.JButton btnUpUsr;
    private javax.swing.JCheckBox checkAutoOnline;
    private javax.swing.JCheckBox checkAutoStart;
    private javax.swing.JCheckBox checkMinimized;
    private javax.swing.JLabel cmrNumberLabel;
    private javax.swing.JTable cmrTable;
    private javax.swing.JTextField connPortField;
    private javax.swing.JLabel connPortLabel;
    private javax.swing.JLabel connPortLabelTxt;
    private javax.swing.JButton connPortSaveBtn;
    private javax.swing.JTextField filePortField;
    private javax.swing.JLabel filePortLabel;
    private javax.swing.JLabel filePortLabelTxt;
    private javax.swing.JTable historyTable;
    private javax.swing.JTextField historyUsrIdField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JLabel labelDevDayLimTxt;
    private javax.swing.JLabel labelInfo;
    private javax.swing.JLabel labelLimitStorage;
    private javax.swing.JLabel labelSrvStatus;
    private javax.swing.JLabel remoteLabelHome;
    private javax.swing.JLabel remoteOnOffHome;
    private javax.swing.JLabel remoteOnOffLabel;
    private javax.swing.JTextField remotePassField;
    private javax.swing.JLabel remotePassLabel;
    private javax.swing.JLabel remotePassLabelTxt;
    private javax.swing.JButton remotePortSaveBtn;
    private javax.swing.JLabel remoteSetPassLabel;
    private javax.swing.JLabel remoteSettingsLabelTxt;
    private javax.swing.JLabel remoteSwitchOnOff;
    private javax.swing.JLabel serverStatusLabelTxt;
    private javax.swing.JLabel setPortLabel1;
    private javax.swing.JLabel setPortLabel2;
    private javax.swing.JButton showBtn;
    private javax.swing.JLabel srvIpLabel;
    private javax.swing.JLabel srvIpLabelTxt;
    private javax.swing.JLabel srvIpRemoteLabel;
    private javax.swing.JLabel srvIpRemoteLabelTxt;
    private javax.swing.JButton startBtn;
    private javax.swing.JLabel statusInfoLabelTxt;
    private javax.swing.JButton stopBtn;
    private javax.swing.JLabel storageSettingsLabelTxt;
    private javax.swing.JLabel userIdLabel;
    // End of variables declaration//GEN-END:variables
}
