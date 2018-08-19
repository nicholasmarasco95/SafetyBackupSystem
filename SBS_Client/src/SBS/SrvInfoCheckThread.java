/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.io.BufferedReader;
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
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 *
 * @author nicho
 */
public class SrvInfoCheckThread implements Runnable{
    
    private Settings setting;
    private JLabel labelSrvConn;
    private JLabel labelSrvUserInfo;
    private JLabel labelMainUserInfo;
    private JLabel labelError;
    private JLabel labelMainExpire;
    private boolean running;
    private boolean srvError;
    private int pid;
    
    private MainGUI mainClass;

    @Override
    public void run() {
        if(srvCheckInternetConn() && getServerInfo()) srvInfoCheck();
    }
    
    public SrvInfoCheckThread(JLabel labelSrvConn, JLabel labelSrvUserInfo, JLabel labelMainUserInfo, JLabel labelError, JLabel labelMainExpire, boolean running, boolean srvError, MainGUI mainClass){
        this.setting= new Settings();
        this.labelSrvConn= labelSrvConn;
        this.labelSrvUserInfo= labelSrvUserInfo;
        this.labelMainUserInfo= labelMainUserInfo;
        this.labelError= labelError;
        this.labelMainExpire= labelMainExpire;
        this.running= running;
        this.srvError= srvError;
        
        this.mainClass= mainClass;
        
        this.pid= setting.getIntValue("checkConnSem")+1;
        setting.SaveSetting("int", "checkConnSem", Integer.toString(this.pid));
    }
    
    private void srvInfoCheck(){
        mainClass.threadCheckErr= false;
        String ip= setting.getStingValue("serverIp");
        int port= Integer.parseInt(setting.getStingValue("serverPort"));
        String userId= setting.getStingValue("usrCode");
        String pass= setting.getStingValue("usrPass");
        String mac= getMacAddress();
        String action= "checkUsrOnly";  //checkUsrOnly
            LoadingGUI loadingClass= new LoadingGUI(null, true);
            Thread loadingThread= new Thread(loadingClass);
            loadingThread.start();
            try{
                Socket sock= new Socket(ip, port);
                OutputStream os= sock.getOutputStream();
                BufferedReader br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
                PrintWriter pw= new PrintWriter(os, true);
                boolean sending= true;
                while(sending){
                    labelSrvConn.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
                    pw.println(action+","+userId+","+pass+","+mac);  //info to sent to server (in this order)
                    String msgIn= br.readLine();
                    if(msgIn.equals("usnotreg")){
                        System.out.println("----------->USER NOT REGISTRED");
                        setting.SaveSetting("bool", "sbsLocked", "true");
                        setting.SaveSetting("string", "expireDate", "un");
                        mainClass.mainCheckSettings(true);
                        labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                        labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                        labelError.setText("USER ERROR");
                        labelError.setIcon(new ImageIcon(getClass().getResource(usrErrorIcon)));
                        srvError= true;
                        loadingClass.close();
                        return;
                    }
                    if(msgIn.equals("uspasserr")){
                        System.out.println("----------->USER PASSWORD ERROR");
                        setting.SaveSetting("bool", "sbsLocked", "true");
                        setting.SaveSetting("string", "expireDate", "un");
                        mainClass.mainCheckSettings(true);
                        labelError.setText("WRONG PASSWORD");
                        labelError.setIcon(new ImageIcon(getClass().getResource(usrErrorIcon)));
                        labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                        labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                        srvError= true;
                        loadingClass.close();
                        return;
                    }
                    if(msgIn.equals("usisexp")){
                        System.out.println("----------->USER EXPIRED");
                        setting.SaveSetting("string", "expireDate", "ex");
                        setting.SaveSetting("bool", "sbsLocked", "true");
                        //setting.SaveSetting("bool", "srvError", "true");
                        mainClass.mainCheckSettings(true);
                        labelError.setText("EXPIRED USER");
                        labelError.setIcon(new ImageIcon(getClass().getResource(usrExpiredIcon)));
                        srvError= true;
                        loadingClass.close();
                        return;
                    }
                    if(msgIn.equals("usrmacerr")){
                        System.out.println("----------->MAC ERROR");
                        setting.SaveSetting("bool", "sbsLocked", "true");
                        mainClass.mainCheckSettings(true);
                        //pcErrorIcon
                        labelError.setText("PC LICENSE EXPIRED");
                        labelError.setIcon(new ImageIcon(getClass().getResource(pcErrorIcon)));
                        srvError= true;
                        loadingClass.close();
                        return;
                    }
                    else{
                        //server send expire date to tell to close connection
                        setting.SaveSetting("string", "expireDate", msgIn);
                        setting.SaveSetting("bool", "sbsLocked", "false");
                        labelError.setText("");
                        labelError.setIcon(null);
                        srvError= false;
                        mainClass.mainCheckSettings(true);
                        loadingClass.close();
                        return;
                    }
                }
            } catch (IOException ex) {
                mainClass.threadCheckErr= true;
                mainClass.mainCheckSettings(true);
                Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
                labelSrvConn.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                labelError.setText("SERVER UNREACHABLE");
                labelError.setIcon(new ImageIcon(getClass().getResource(serverErrorIcon)));
                setting.SaveSetting("bool", "sbsLocked", "true");
                
                mainClass.threadCheckErr= false;

                loadingClass.close();
                //close message: 10 secs
                //retry conn: 1 minutes
                RetryConnectionGUI rcg= new RetryConnectionGUI(null, true, "1", "10");
                ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();     
                s.schedule(new Runnable() { public void run() { rcg.setVisible(false); rcg.dispose(); } }, 10, TimeUnit.SECONDS);
                rcg.setVisible(true);
                
                try {
                    Thread.sleep(60000);
                    int maxPid= setting.getIntValue("checkConnSem");
                    if(pid<maxPid){
                        return;
                    }
                    setting.SaveSetting("bool", "sbsLocked", "true");
                    mainClass.mainCheckSettings(false);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(SrvInfoCheckThread.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
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
    
    private boolean getServerInfo(){
        //setting.SaveSetting("string", "serverUrl", "file:///C:/Users/nicho/Desktop/test.html");  //debug only
        String urlStr= setting.getStingValue("serverUrl");
        if(urlStr==null || urlStr.equals("")) return false;
        InputStream is= null;
        BufferedReader br;
        String line;
        String ipStr;
        String portStr;
        String limit;
        String filePortStr;
        try{
            URL url= new URL(urlStr);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            line= br.readLine();
            line= decrypt(line);
            String[] inputLineArray= line.split("~");
            ipStr= inputLineArray[0];
            portStr= inputLineArray[1];
            filePortStr= inputLineArray[2];
            limit= inputLineArray[3];
            setting.SaveSetting("string", "serverIp", ipStr);
            setting.SaveSetting("string", "serverPort", portStr);
            setting.SaveSetting("string", "filePortCl", filePortStr);
            setting.SaveSetting("int", "backupLimit", limit);
        } catch (MalformedURLException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if(ipStr!=null || portStr!=null) return true;
        return false;
    }
    
    private String decrypt(String toDecrypt){
        if(toDecrypt.length()==0) return "";
        String retStr="";
        String[] arrStr= toDecrypt.split("1910");
        for(int i=0; i<arrStr.length; i++){
            int tmpInt= Integer.parseInt(arrStr[i]);
            char tmpChar= (char) (tmpInt-1910);
            retStr+=tmpChar;
        }
        return retStr;
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
    
    
    //***************ICONS DATABASE START***************
    String trafficRedBulb= "/SBS/Icons/Traffic-light-red-logo-30x30.png";
    String trafficGreenBulb= "/SBS/Icons/Traffic-light-green-logo-30x30.png";
    String usrErrorIcon= "/SBS/Icons/user-error-icon-40x40.png";
    String usrExpiredIcon= "/SBS/Icons/expired-icon-40x40.png";
    String pcErrorIcon= "/SBS/Icons/pc-error-icon-40x40.png";
    String trafficYellowBulb= "/SBS/Icons/Traffic-light-yellow-logo-30x30.png";
    String serverErrorIcon= "/SBS/Icons/server-error-icon-40x40.png";
    //***************ICONS DATABASE END*****************
}
