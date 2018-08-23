/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Remote;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nicholas
 */
public class MainRemote extends javax.swing.JFrame {

    /**
     * Creates new form MainRemote
     */
    
    private File file;
    private PrintWriter pw;
    private BufferedReader br;
    private String selUsr= null;
    private Settings setting= new Settings();
    
    private RemoteConnThread rct;
    
    public MainRemote() {
        initComponents();
        checkPassword();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        SystemTrayMethod();
        setting.SaveSetting("bool", "remoteConnected", "false");
        
        refreshSettings();
        
        //RemoteConnThread rct= new RemoteConnThread(connectBtn, disconnectBtn, shutBtn);
        
        disconnectBtn.setEnabled(false);
        shutBtn.setEnabled(false);
        rebootBtn.setEnabled(false);
        restartBtn.setEnabled(false);
        btnRefreshCmr.setEnabled(false);
        btnAddUser.setEnabled(false);
        btnUpUsr.setEnabled(false);
        showBtn.setEnabled(false);
        cmrNumberLabel.setText("");
        labelLimitStorage.setText("");
        labelDevDayLimTxt.setForeground(Color.gray);
        backupNumberLabel.setText("");
        
        
        //************TEMPORARY START************
        btnUpUsr.setEnabled(false);
        //************TEMPORARY END**************
    }
    
    
    private String encrypt(String toEncrypt){
        if(toEncrypt.length()==0) return "";
        String asciiStr= "";
        for(int i=0; i<toEncrypt.length(); i++){
            asciiStr+= ((int)toEncrypt.charAt(i))+1910+"1910";
        }
        return asciiStr;
    }
    
    private void checkPassword(){
        boolean passOn= setting.getBoolValue("sbsPassActiveRemote");
        if(passOn){
            EnterPasswordGUI enterPass= new EnterPasswordGUI(this, true);
            enterPass.setVisible(true);
            if(!setting.getBoolValue("sbsPassCorrectRemote")) this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
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
    
    Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(sbsRemoteIcon));

    //popupmenu
    PopupMenu trayPopupMenu = new PopupMenu();

    //1t menuitem for popupmenu
    MenuItem action = new MenuItem("Open");
    action.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            checkPassword();
            if(setting.getBoolValue("sbsPassActiveRemote")){
                if(setting.getBoolValue("sbsPassCorrectRemote")) setVisible(true);
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
            if(setting.getBoolValue("sbsPassActiveRemote")){
                if(setting.getBoolValue("sbsPassCorrectRemote")) System.exit(0);
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
                if(setting.getBoolValue("sbsPassActiveRemote")){
                    if(setting.getBoolValue("sbsPassCorrectRemote")) setVisible(true);
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
    
    private void getSettings(){
        String tmpIp= settingsIpField.getText();
        String tmpPort= settingsPortField.getText();
        String tmpPass= srvPassField.getText();
        if(tmpIp.length()<2 || tmpPort.length()<2 || tmpPass.length()<2){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Fill all fields");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon))); 
            settingsIpField.setText("");
            settingsPortField.setText("");
            return;
        }
        try{
            Integer.parseInt(tmpPort);
        }catch  (NumberFormatException e) {
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Port must be an integer");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            settingsIpField.setText("");
            settingsPortField.setText("");
            return;
        }
        setting.SaveSetting("String", "remoteSrvIp", tmpIp);
        setting.SaveSetting("int", "remoteSrvPort", tmpPort);
        setting.SaveSetting("int", "remotePass", tmpPass);
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Info Saved");
        JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon))); 
        settingsIpField.setText("");
        settingsPortField.setText("");
        srvPassField.setText("");
        return;
    }
    
    private void htmlCreator(){
        String tmpIp= ipField.getText();
        String tmpConnPort= connPortField.getText();
        String tmpFilePort= fileField.getText();
        String tmpMaxGb= maxGbField.getText();
        if(tmpIp.length()<2 || tmpConnPort.length()<2 || tmpFilePort.length()<2 || tmpMaxGb.length()<1){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Fill all fields");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon))); 
            ipField.setText("");
            connPortField.setText("");
            fileField.setText("");
            maxGbField.setText("");
            return;
        }
        try{
            Integer.parseInt(tmpConnPort);
            Integer.parseInt(tmpFilePort);
        }catch  (NumberFormatException e) {
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Port must be an integer");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            ipField.setText("");
            connPortField.setText("");
            fileField.setText("");
            maxGbField.setText("");
            return;
        }
        try{
            Integer.parseInt(tmpMaxGb);
        }catch  (NumberFormatException e) {
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Max GB must be an integer");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            ipField.setText("");
            connPortField.setText("");
            fileField.setText("");
            maxGbField.setText("");
            return;
        }
        DestinationFolder destFold= new DestinationFolder(null, true, "htmlFile");
        destFold.setVisible(true);
        if(setting.getStingValue("htmlFileFoldDest")==null || setting.getStingValue("htmlFileFoldDest").length()<2){
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Destination Folder is required");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            ipField.setText("");
            connPortField.setText("");
            fileField.setText("");
            maxGbField.setText("");
            return;
        }
        String encryptStr= tmpIp+"~"+tmpConnPort+"~"+tmpFilePort+"~"+tmpMaxGb;
        encryptStr= encrypt(encryptStr) ;
        WriteFile(encryptStr);
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("File Created");
        JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
        ipField.setText("");
        connPortField.setText("");
        fileField.setText("");
        maxGbField.setText("");
    }
    
    private void WriteFile(String toWrite){
        String destFold= setting.getStingValue("htmlFileFoldDest");
        this.file= new File(destFold+"\\"+"encrypted.html");
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(MainRemote.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            pw= new PrintWriter(new FileOutputStream(file, false));
            br= new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainRemote.class.getName()).log(Level.SEVERE, null, ex);
        }
        pw.write(toWrite);
        CloseFile();
    }
    
    private void CloseFile(){
        try {
            pw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MainRemote.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean checkSettings(){
        String tmpRemotePass= setting.getStingValue("remotePass");
        String tmpIp= setting.getStingValue("remoteSrvIp");
        int tmpPort= setting.getIntValue("remoteSrvPort");
        if(tmpRemotePass==null || tmpRemotePass.length()<2 || tmpIp==null || tmpIp.length()<2 || tmpPort<=0){
            srvIpConnSettLabel.setForeground(Color.red);
            srvIpConnSettLabel.setText("NOT SET");
            connPortConnSettLabel.setForeground(Color.red);
            connPortConnSettLabel.setText("NOT SET");
            return false;
        }
        srvIpConnSettLabel.setForeground(Color.blue);
        srvIpConnSettLabel.setText(tmpIp);
        connPortConnSettLabel.setForeground(Color.blue);
        connPortConnSettLabel.setText(Integer.toString(tmpPort));
        return true;
    }
    
    
    private void refreshSettings(){
        boolean checkFlag= true;
        
        //************INTERNET CHECK************
        if(srvCheckInternetConn()){
            if(!setting.getBoolValue("remoteConnected")){
                activeConnLabel.setIcon(new ImageIcon(getClass().getResource(connIcon)));
                labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(startIcon)));
                noInternetLabel.setText("");
            }
            
            //srvStatusLabel.setForeground(Color.blue);
            //srvStatusLabel.setText("Ready");
        }
        else{
            activeConnLabel.setIcon(new ImageIcon(getClass().getResource(offlineIcon)));
            labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(engineIcon)));
            noInternetLabel.setForeground(Color.red);
            noInternetLabel.setText("No internet Connection");
            srvStatusLabel.setText("");
            checkFlag= false;
            if(setting.getBoolValue("remoteConnected")){
               rct.disconnect();
            }
        }
        
        //************CHECK SETTINGS************
        if(!checkSettings()){
            labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(engineIcon)));
            srvStatusLabel.setForeground(Color.red);
            srvStatusLabel.setText("Empty Values");
            checkFlag=false;
        }
        else if(checkFlag){
            if(!setting.getBoolValue("remoteConnected")){
                srvStatusLabel.setForeground(Color.blue);
                srvStatusLabel.setText("Ready");
            }
        }
        
        
        if(checkFlag && !setting.getBoolValue("remoteConnected")) connectBtn.setEnabled(true);
        else connectBtn.setEnabled(false);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusInfoLabelTxt = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        connectBtn = new javax.swing.JButton();
        disconnectBtn = new javax.swing.JButton();
        serverStatusLabelTxt = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        serverStatusLabelTxt1 = new javax.swing.JLabel();
        activeConnLabel = new javax.swing.JLabel();
        labelSrvStatus = new javax.swing.JLabel();
        srvStatusLabel = new javax.swing.JLabel();
        noInternetLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        shutBtn = new javax.swing.JButton();
        rebootBtn = new javax.swing.JButton();
        restartBtn = new javax.swing.JButton();
        statusInfoLabelTxt1 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        labelDevDayLimTxt = new javax.swing.JLabel();
        labelLimitStorage = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        srvIpConnSett = new javax.swing.JLabel();
        srvIpConnSettLabel = new javax.swing.JLabel();
        storageSettingsLabelTxt = new javax.swing.JLabel();
        connPortConnSett = new javax.swing.JLabel();
        connPortConnSettLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        htmlCreatorLabel = new javax.swing.JLabel();
        srvIpLabelTxt = new javax.swing.JLabel();
        ipField = new javax.swing.JTextField();
        connPortLabelTxt = new javax.swing.JLabel();
        connPortField = new javax.swing.JTextField();
        filePortLabelTxt1 = new javax.swing.JLabel();
        fileField = new javax.swing.JTextField();
        htmlCreateBtn = new javax.swing.JButton();
        maxGbLabelTxt = new javax.swing.JLabel();
        maxGbField = new javax.swing.JTextField();
        htmlTranslatorBtn = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        settingsIpLabelTxt = new javax.swing.JLabel();
        settingsIpField = new javax.swing.JTextField();
        settingsPortLabelTxt = new javax.swing.JLabel();
        settingsPortField = new javax.swing.JTextField();
        htmlCreatorLabel1 = new javax.swing.JLabel();
        btnSbsPass = new javax.swing.JButton();
        settingsSaveBtn = new javax.swing.JButton();
        srvPassLabel = new javax.swing.JLabel();
        srvPassField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        btnRefreshCmr = new javax.swing.JButton();
        cmrNumberLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        cmrTable = new javax.swing.JTable();
        btnAddUser = new javax.swing.JButton();
        btnUpUsr = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        historyTable = new javax.swing.JTable();
        historyUsrIdField = new javax.swing.JTextField();
        showBtn = new javax.swing.JButton();
        userIdLabel = new javax.swing.JLabel();
        backupNumberLabel = new javax.swing.JLabel();
        btnRefreshSet = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(new java.awt.Dimension(584, 455));

        statusInfoLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        statusInfoLabelTxt.setForeground(new java.awt.Color(0, 0, 255));
        statusInfoLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/speed-icon-40x40.png"))); // NOI18N
        statusInfoLabelTxt.setText("Status Info");

        connectBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        connectBtn.setForeground(new java.awt.Color(0, 0, 255));
        connectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/on-power-start-icon-40x40.png"))); // NOI18N
        connectBtn.setText("Connect");
        connectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectBtnActionPerformed(evt);
            }
        });

        disconnectBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        disconnectBtn.setForeground(new java.awt.Color(255, 0, 0));
        disconnectBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/stop-icon-2-40x40.png"))); // NOI18N
        disconnectBtn.setText("Disconnect");
        disconnectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnectBtnActionPerformed(evt);
            }
        });

        serverStatusLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        serverStatusLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/web-internet-world-icon-40x40.png"))); // NOI18N
        serverStatusLabelTxt.setText("Connection");

        serverStatusLabelTxt1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        serverStatusLabelTxt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/Server-Logo-40x40.png"))); // NOI18N
        serverStatusLabelTxt1.setText("Server Status");

        activeConnLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/disconnect_icon-40x40.png"))); // NOI18N

        labelSrvStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/start-icon-green-40x40.png"))); // NOI18N

        srvStatusLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        srvStatusLabel.setText("Ready");

        noInternetLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 20)); // NOI18N
        noInternetLabel.setText("No Internet Connection");

        shutBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        shutBtn.setForeground(new java.awt.Color(255, 0, 0));
        shutBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/shutdown-off-icon-30x30.png"))); // NOI18N
        shutBtn.setText("Shutdown");
        shutBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shutBtnActionPerformed(evt);
            }
        });

        rebootBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        rebootBtn.setForeground(new java.awt.Color(255, 102, 0));
        rebootBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/restart-green-load-icon-30x30.png"))); // NOI18N
        rebootBtn.setText("Reboot PC");
        rebootBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rebootBtnActionPerformed(evt);
            }
        });

        restartBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        restartBtn.setForeground(new java.awt.Color(0, 51, 255));
        restartBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/restart-icon-30x30.png"))); // NOI18N
        restartBtn.setText("Restart SBS");
        restartBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restartBtnActionPerformed(evt);
            }
        });

        statusInfoLabelTxt1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        statusInfoLabelTxt1.setForeground(new java.awt.Color(0, 0, 255));
        statusInfoLabelTxt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/command-Powershell-Icon-50x40.png"))); // NOI18N
        statusInfoLabelTxt1.setText("Actions");

        labelDevDayLimTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelDevDayLimTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/storage-icon-2-40x40.png"))); // NOI18N
        labelDevDayLimTxt.setText("Storage Limit");

        labelLimitStorage.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        labelLimitStorage.setForeground(new java.awt.Color(0, 0, 255));
        labelLimitStorage.setText("100  GB");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(shutBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
                        .addComponent(rebootBtn)
                        .addGap(47, 47, 47)
                        .addComponent(restartBtn))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(statusInfoLabelTxt1)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(labelDevDayLimTxt)
                                .addGap(18, 18, 18)
                                .addComponent(labelLimitStorage)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusInfoLabelTxt1)
                .addGap(11, 11, 11)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shutBtn)
                    .addComponent(rebootBtn)
                    .addComponent(restartBtn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDevDayLimTxt)
                    .addComponent(labelLimitStorage))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Actions", jPanel1);

        srvIpConnSett.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpConnSett.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/ip_icon_40x40.png"))); // NOI18N
        srvIpConnSett.setText("Server IP:");

        srvIpConnSettLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpConnSettLabel.setForeground(new java.awt.Color(255, 0, 0));
        srvIpConnSettLabel.setText("NOT SET");

        storageSettingsLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        storageSettingsLabelTxt.setForeground(new java.awt.Color(0, 0, 255));
        storageSettingsLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/web-internet-world-icon-40x40.png"))); // NOI18N
        storageSettingsLabelTxt.setText("Connection Settings");

        connPortConnSett.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        connPortConnSett.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/port_icon-40x40.png"))); // NOI18N
        connPortConnSett.setText("Conn Port:");

        connPortConnSettLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        connPortConnSettLabel.setForeground(new java.awt.Color(255, 0, 0));
        connPortConnSettLabel.setText("NOT SET");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(srvIpConnSett)
                        .addGap(18, 18, 18)
                        .addComponent(srvIpConnSettLabel))
                    .addComponent(storageSettingsLabelTxt)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(connPortConnSett)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(connPortConnSettLabel)))
                .addContainerGap(348, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(storageSettingsLabelTxt)
                .addGap(22, 22, 22)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvIpConnSett)
                    .addComponent(srvIpConnSettLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(connPortConnSett)
                    .addComponent(connPortConnSettLabel))
                .addContainerGap(38, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Info", jPanel6);

        htmlCreatorLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        htmlCreatorLabel.setForeground(new java.awt.Color(0, 0, 255));
        htmlCreatorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/html-tag-icon-40x40.png"))); // NOI18N
        htmlCreatorLabel.setText("Html Creator");

        srvIpLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvIpLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/ip_icon_40x40.png"))); // NOI18N
        srvIpLabelTxt.setText("IP");

        connPortLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        connPortLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/port_icon-40x40.png"))); // NOI18N
        connPortLabelTxt.setText("Conn Port");

        filePortLabelTxt1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        filePortLabelTxt1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/port_icon-40x40.png"))); // NOI18N
        filePortLabelTxt1.setText("File Port");

        htmlCreateBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        htmlCreateBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/create-edit-icon-30x30.png"))); // NOI18N
        htmlCreateBtn.setText("Create");
        htmlCreateBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                htmlCreateBtnActionPerformed(evt);
            }
        });

        maxGbLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        maxGbLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/storage-icon-2-40x40.png"))); // NOI18N
        maxGbLabelTxt.setText("Max GB");

        htmlTranslatorBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        htmlTranslatorBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/translate-icon-30x30.png"))); // NOI18N
        htmlTranslatorBtn.setText("Html Translator");
        htmlTranslatorBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                htmlTranslatorBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(htmlCreatorLabel)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(connPortLabelTxt)
                            .addComponent(srvIpLabelTxt))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(htmlTranslatorBtn)
                                .addGap(18, 18, 18)
                                .addComponent(htmlCreateBtn))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(connPortField, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(maxGbLabelTxt)
                                        .addGap(15, 15, 15)
                                        .addComponent(maxGbField))
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(filePortLabelTxt1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(fileField, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
                .addGap(16, 16, 16))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(htmlCreatorLabel)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srvIpLabelTxt)
                    .addComponent(ipField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(filePortLabelTxt1)
                    .addComponent(fileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(maxGbLabelTxt)
                        .addComponent(maxGbField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(connPortLabelTxt)
                        .addComponent(connPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(htmlCreateBtn)
                    .addComponent(htmlTranslatorBtn)))
        );

        jTabbedPane1.addTab("Html", jPanel2);

        settingsIpLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        settingsIpLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/ip_icon_40x40.png"))); // NOI18N
        settingsIpLabelTxt.setText("Server IP");

        settingsPortLabelTxt.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        settingsPortLabelTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/port_icon-40x40.png"))); // NOI18N
        settingsPortLabelTxt.setText("Remote Port");

        htmlCreatorLabel1.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        htmlCreatorLabel1.setForeground(new java.awt.Color(0, 0, 255));
        htmlCreatorLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/Settings-icon-40x40.png"))); // NOI18N
        htmlCreatorLabel1.setText("Settings");

        btnSbsPass.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnSbsPass.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/lock-icon-30x30.png"))); // NOI18N
        btnSbsPass.setText("Lock Password");
        btnSbsPass.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnSbsPass.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnSbsPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSbsPassActionPerformed(evt);
            }
        });

        settingsSaveBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        settingsSaveBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/save-floppy-icon-30x30.png"))); // NOI18N
        settingsSaveBtn.setText("Save");
        settingsSaveBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settingsSaveBtnActionPerformed(evt);
            }
        });

        srvPassLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        srvPassLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/password-icon-40x40.png"))); // NOI18N
        srvPassLabel.setText("Server Password");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(htmlCreatorLabel1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(settingsPortLabelTxt)
                            .addComponent(settingsIpLabelTxt)
                            .addComponent(srvPassLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(srvPassField, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                            .addComponent(settingsIpField, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                            .addComponent(settingsPortField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnSbsPass, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(settingsSaveBtn, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(htmlCreatorLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settingsIpLabelTxt)
                    .addComponent(settingsIpField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(settingsPortLabelTxt)
                    .addComponent(settingsPortField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSbsPass, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(settingsSaveBtn))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(srvPassLabel)
                            .addComponent(srvPassField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Settings", jPanel3);

        btnRefreshCmr.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshCmr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/refresh-icon-2-30x30.png"))); // NOI18N
        btnRefreshCmr.setText("Refresh");
        btnRefreshCmr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshCmrActionPerformed(evt);
            }
        });

        cmrNumberLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        cmrNumberLabel.setText("Tot Cmr");

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
                cmrTableselectedRow(evt);
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

        btnAddUser.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/admin-icon-30x30.png"))); // NOI18N
        btnAddUser.setText("Add User");
        btnAddUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddUserActionPerformed(evt);
            }
        });

        btnUpUsr.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnUpUsr.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/user_update_edit_30x30.png"))); // NOI18N
        btnUpUsr.setText("Update User");
        btnUpUsr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpUsrActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnRefreshCmr)
                .addGap(18, 18, 18)
                .addComponent(cmrNumberLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAddUser)
                .addGap(18, 18, 18)
                .addComponent(btnUpUsr)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRefreshCmr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cmrNumberLabel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnUpUsr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnAddUser, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("User", jPanel4);

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

        showBtn.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        showBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/history-folder-30x30.png"))); // NOI18N
        showBtn.setText("Show");
        showBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBtnActionPerformed(evt);
            }
        });

        userIdLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 18)); // NOI18N
        userIdLabel.setForeground(new java.awt.Color(0, 0, 255));
        userIdLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/admin-icon-40x40.png"))); // NOI18N
        userIdLabel.setText("User ID");

        backupNumberLabel.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        backupNumberLabel.setText("label");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(userIdLabel)
                .addGap(42, 42, 42)
                .addComponent(historyUsrIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showBtn)
                .addGap(18, 18, 18)
                .addComponent(backupNumberLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 5, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userIdLabel)
                    .addComponent(historyUsrIdField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(showBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(backupNumberLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("History", jPanel5);

        btnRefreshSet.setFont(new java.awt.Font("Microsoft Tai Le", 1, 14)); // NOI18N
        btnRefreshSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Remote/Icons/refresh-icon-30x30.png"))); // NOI18N
        btnRefreshSet.setText("Refresh");
        btnRefreshSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshSetActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator2)
            .addComponent(jSeparator3)
            .addComponent(jTabbedPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(serverStatusLabelTxt)
                            .addComponent(serverStatusLabelTxt1))
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(activeConnLabel)
                            .addComponent(labelSrvStatus))
                        .addGap(37, 37, 37)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(srvStatusLabel)
                            .addComponent(noInternetLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusInfoLabelTxt)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnRefreshSet)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(connectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(disconnectBtn)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusInfoLabelTxt)
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(serverStatusLabelTxt1)
                        .addGap(18, 18, 18)
                        .addComponent(serverStatusLabelTxt))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(srvStatusLabel)
                            .addComponent(labelSrvStatus))
                        .addGap(18, 18, 18)
                        .addComponent(activeConnLabel))
                    .addComponent(noInternetLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(connectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(disconnectBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnRefreshSet, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void connectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectBtnActionPerformed
        rct= new RemoteConnThread(connectBtn, disconnectBtn, shutBtn, rebootBtn, restartBtn, srvStatusLabel, labelSrvStatus, btnRefreshCmr, btnAddUser, cmrNumberLabel, cmrTable, historyTable, backupNumberLabel, showBtn, labelLimitStorage, labelDevDayLimTxt);
        Thread thrRct= new Thread(rct);
        thrRct.start();
    }//GEN-LAST:event_connectBtnActionPerformed

    private void disconnectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnectBtnActionPerformed
        rct.disconnect();
        refreshSettings();
    }//GEN-LAST:event_disconnectBtnActionPerformed

    private void btnRefreshSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshSetActionPerformed
        refreshSettings();
    }//GEN-LAST:event_btnRefreshSetActionPerformed

    private void htmlCreateBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_htmlCreateBtnActionPerformed
        htmlCreator();
    }//GEN-LAST:event_htmlCreateBtnActionPerformed

    private void btnSbsPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSbsPassActionPerformed
        PasswordGUI passGui= new PasswordGUI(this, true);
        passGui.setVisible(true);
        refreshSettings();
    }//GEN-LAST:event_btnSbsPassActionPerformed

    private void settingsSaveBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsSaveBtnActionPerformed
        getSettings();
        refreshSettings();
    }//GEN-LAST:event_settingsSaveBtnActionPerformed

    private void btnRefreshCmrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshCmrActionPerformed
        rct.refreshUsrTable();
    }//GEN-LAST:event_btnRefreshCmrActionPerformed

    private void cmrTableselectedRow(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmrTableselectedRow
        int selRow= cmrTable.getSelectedRow();
        selUsr= cmrTable.getValueAt(selRow, 0).toString();
        btnUpUsr.setEnabled(true);
    }//GEN-LAST:event_cmrTableselectedRow

    private void btnAddUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddUserActionPerformed
        AddUserGUI addUsr= new AddUserGUI(this,true, rct);
        addUsr.setVisible(true);
    }//GEN-LAST:event_btnAddUserActionPerformed

    private void btnUpUsrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpUsrActionPerformed
        if(selUsr!=null){
            UpdateUserGUI upUsr= new UpdateUserGUI(this, true, selUsr, rct);
            upUsr.setVisible(true);
            cmrTable.clearSelection();
            selUsr= null;
            btnUpUsr.setEnabled(false);
        }
    }//GEN-LAST:event_btnUpUsrActionPerformed

    private void htmlTranslatorBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_htmlTranslatorBtnActionPerformed
        HtmlTranslator htmlTranslator= new HtmlTranslator(this, true);
        htmlTranslator.setVisible(true);
    }//GEN-LAST:event_htmlTranslatorBtnActionPerformed

    private void showBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBtnActionPerformed
        String usrId= historyUsrIdField.getText();
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
        rct.history(usrId);
        historyUsrIdField.setText("");
    }//GEN-LAST:event_showBtnActionPerformed

    private void restartBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restartBtnActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Restart SBS Server?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            rct.restart();
        }
    }//GEN-LAST:event_restartBtnActionPerformed

    private void rebootBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rebootBtnActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Reboot Remote PC?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            rct.reboot();
        }
    }//GEN-LAST:event_rebootBtnActionPerformed

    private void shutBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shutBtnActionPerformed
        JLabel msgLabel= new JLabel();
        msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
        msgLabel.setText("Shutdown Remote PC?");
        int ans= JOptionPane.showConfirmDialog(rootPane, msgLabel, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
        if(ans==JOptionPane.YES_OPTION){
            JLabel msgLabel2= new JLabel();
            msgLabel2.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel2.setText("You will not able to remotely power it back, continue?");
            int ans2= JOptionPane.showConfirmDialog(rootPane, msgLabel2, "", WIDTH, HEIGHT, new ImageIcon(getClass().getResource(warningIcon)));
            if(ans2==JOptionPane.YES_OPTION){
                rct.shutdown();
            }
        }
    }//GEN-LAST:event_shutBtnActionPerformed

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
            java.util.logging.Logger.getLogger(MainRemote.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainRemote.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainRemote.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainRemote.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainRemote().setVisible(true);
            }
        });
    }
    
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/Remote/Icons/error-logo-50x50.png";
    String infoIcon="/Remote/Icons/info-icon-50x50.png";
    String sbsRemoteIcon= "/Remote/Icons/remote-control-icon-40x40.png";
    String connIcon= "/Remote/Icons/connect-icon-40x40.png";
    String offlineIcon= "/Remote/Icons/disconnect_icon-40x40.png";
    String engineIcon= "/Remote/Icons/check_engine_icon-40x29.png";
    String startIcon= "/Remote/Icons/start-icon-green-40x40.png";
    String warningIcon= "/Remote/Icons/warning_logo_50x45.png";
    //***************ICONS DATABASE END*****************

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel activeConnLabel;
    private javax.swing.JLabel backupNumberLabel;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnRefreshCmr;
    private javax.swing.JButton btnRefreshSet;
    private javax.swing.JButton btnSbsPass;
    private javax.swing.JButton btnUpUsr;
    private javax.swing.JLabel cmrNumberLabel;
    private javax.swing.JTable cmrTable;
    private javax.swing.JLabel connPortConnSett;
    private javax.swing.JLabel connPortConnSettLabel;
    private javax.swing.JTextField connPortField;
    private javax.swing.JLabel connPortLabelTxt;
    private javax.swing.JButton connectBtn;
    private javax.swing.JButton disconnectBtn;
    private javax.swing.JTextField fileField;
    private javax.swing.JLabel filePortLabelTxt1;
    private javax.swing.JTable historyTable;
    private javax.swing.JTextField historyUsrIdField;
    private javax.swing.JButton htmlCreateBtn;
    private javax.swing.JLabel htmlCreatorLabel;
    private javax.swing.JLabel htmlCreatorLabel1;
    private javax.swing.JButton htmlTranslatorBtn;
    private javax.swing.JTextField ipField;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel labelDevDayLimTxt;
    private javax.swing.JLabel labelLimitStorage;
    private javax.swing.JLabel labelSrvStatus;
    private javax.swing.JTextField maxGbField;
    private javax.swing.JLabel maxGbLabelTxt;
    private javax.swing.JLabel noInternetLabel;
    private javax.swing.JButton rebootBtn;
    private javax.swing.JButton restartBtn;
    private javax.swing.JLabel serverStatusLabelTxt;
    private javax.swing.JLabel serverStatusLabelTxt1;
    private javax.swing.JTextField settingsIpField;
    private javax.swing.JLabel settingsIpLabelTxt;
    private javax.swing.JTextField settingsPortField;
    private javax.swing.JLabel settingsPortLabelTxt;
    private javax.swing.JButton settingsSaveBtn;
    private javax.swing.JButton showBtn;
    private javax.swing.JButton shutBtn;
    private javax.swing.JLabel srvIpConnSett;
    private javax.swing.JLabel srvIpConnSettLabel;
    private javax.swing.JLabel srvIpLabelTxt;
    private javax.swing.JTextField srvPassField;
    private javax.swing.JLabel srvPassLabel;
    private javax.swing.JLabel srvStatusLabel;
    private javax.swing.JLabel statusInfoLabelTxt;
    private javax.swing.JLabel statusInfoLabelTxt1;
    private javax.swing.JLabel storageSettingsLabelTxt;
    private javax.swing.JLabel userIdLabel;
    // End of variables declaration//GEN-END:variables
}
