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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
public class srvCheckInternetThread implements Runnable{
    
    private Settings setting;
    private JLabel labelError;
    private JLabel expireDateLabel;
    private JLabel labelSrvInternet;
    private JLabel labelMainInternet;
    private JLabel labelSrvInfo;
    private JLabel labelMainUserInfo;
    private JLabel labelSrvUserInfo;
    private JLabel labelMainExpire;
    private JLabel labelSrvCheckInfo;
    private int pid;
    
    private MainGUI mainClass;

    @Override
    public void run() {
        checkConnServStatus();
    }
    
    
    public srvCheckInternetThread(JLabel labelError, JLabel expireDateLabel, JLabel labelSrvInternet, JLabel labelMainInternet, JLabel labelSrvInfo, JLabel labelMainUserInfo, JLabel labelSrvUserInfo, JLabel labelMainExpire, JLabel labelSrvCheckInfo, MainGUI mainClass){
        this.setting= new Settings();
        this.labelError= labelError;
        this.expireDateLabel= expireDateLabel;
        this.labelSrvInternet=labelSrvInternet;
        this.labelMainInternet=labelMainInternet;
        this.labelSrvInfo=labelSrvInfo;
        this.labelMainUserInfo=labelMainUserInfo;
        this.labelSrvUserInfo=labelSrvUserInfo;
        this.labelMainExpire=labelMainExpire;
        this.labelSrvCheckInfo=labelSrvCheckInfo;
        this.mainClass=mainClass;
        
        this.pid= setting.getIntValue("checkInternetSem")+1;
        setting.SaveSetting("int", "checkInternetSem", Integer.toString(this.pid));
        
        //srvCheckInternetThread(labelError, expireDateLabel, labelSrvInternet, labelMainInternet, labelSrvInfo, labelMainUserInfo, labelSrvUserInfo, labelMainExpire, labelSrvCheckInfo, this);
    }
    
    private boolean checkConnServStatus(){
        setting.SaveSetting("boolean", "intChecking", "true");
        labelError.setText("");
        labelError.setIcon(null);
        boolean error= false;
        expireDateLabel.setText("");
        setting.SaveSetting("bool", "srvError", "false");
        //***************Check internet connection***************
        boolean internetConn= srvCheckInternetConn();
        if(!internetConn){
            error=true;
            labelSrvInternet.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelMainInternet.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelSrvInfo.setIcon(new ImageIcon(getClass().getResource(offlineIcon)));
            labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
            labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
            labelError.setText("No Internet Connection");
            labelError.setIcon(new ImageIcon(getClass().getResource(internetError)));
        }
        else{
            labelSrvInternet.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            labelMainInternet.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
            labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        
        //***************Get Server Info***************
        /*
        if(getServerInfo()){
            labelSrvCheckInfo.setIcon(new ImageIcon(getClass().getResource(trafficGreenBulb)));
        }
        else{
            if(!internetConn) labelSrvCheckInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
            else{
                labelSrvCheckInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
                labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                labelMainExpire.setIcon(new ImageIcon(getClass().getResource(trafficYellowBulb)));
                if(labelError.getText().length()<2){
                    labelError.setText("Enter Server Info");
                    labelError.setIcon(new ImageIcon(getClass().getResource(serverInfoErrorIcon)));
                }
            }
            error=true;
        }
        */
        //***************User Info Input***************
        String usrId= setting.getStingValue("usrCode");
        String usrPort= setting.getStingValue("usrPass");
        if(usrId==null || usrPort==null || usrId.length()<2 || usrPort.length()<2){
            labelSrvUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelMainUserInfo.setIcon(new ImageIcon(getClass().getResource(trafficRedBulb)));
            labelError.setIcon(new ImageIcon(getClass().getResource(usrErrorIcon)));
            if(labelError.getText().length()<2){
                labelError.setText("Enter User Info");
                error= true;
            }
        }
        
        if(error){
            setting.SaveSetting("bool", "sbsLocked", "true");
            //setting.SaveSetting("string", "expireDate", "un");
            RetryInternetGUI rcg= new RetryInternetGUI(null, true, "1", "10");
                ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();     
                s.schedule(new Runnable() { public void run() { rcg.setVisible(false); rcg.dispose(); } }, 10, TimeUnit.SECONDS);
                rcg.setVisible(true);
                
                setting.SaveSetting("bool", "srvError", "true");
                setting.SaveSetting("boolean", "intChecking", "false");
                mainClass.deactivateServer();
                
                try {
                    Thread.sleep(60000);
                    int maxPid= setting.getIntValue("checkInternetSem");
                    if(pid<maxPid){
                        return error;
                    }
                    mainClass.mainCheckSettings(false);
                } catch (InterruptedException ex1) {
                    Logger.getLogger(SrvInfoCheckThread.class.getName()).log(Level.SEVERE, null, ex1);
                }
        }
        
        setting.SaveSetting("boolean", "intChecking", "false");
        return error;
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
    /*
    private boolean getServerInfo(){
        //setting.SaveSetting("string", "serverUrl", "file:///C:/Users/nicho/Desktop/test.html");  //debug only
        String urlStr= setting.getStingValue("serverUrl");
        if(urlStr==null || urlStr.equals("")) return false;
        InputStream is= null;
        BufferedReader br;
        String line;
        String ipStr;
        String connPortStr;
        String filePortStr;
        String limit;
        try{
            URL url= new URL(urlStr);
            is = url.openStream();
            br = new BufferedReader(new InputStreamReader(is));
            line= br.readLine();
            String[] inputLineArray= line.split("~");
            ipStr= inputLineArray[0];
            connPortStr= inputLineArray[1];
            filePortStr= inputLineArray[2];
            limit= inputLineArray[3];
            setting.SaveSetting("string", "serverIp", ipStr);
            setting.SaveSetting("string", "serverPort", connPortStr);
            setting.SaveSetting("string", "filePortCl", filePortStr);
            setting.SaveSetting("int", "backupLimit", limit);
        } catch (MalformedURLException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(MainGUI.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        if(ipStr!=null || connPortStr!=null || filePortStr!=null) return true;
        return false;
    }
    */
    
    
    //***************ICONS DATABASE START***************
    String trafficRedBulb= "/SBS/Icons/Traffic-light-red-logo-30x30.png";
    String trafficGreenBulb= "/SBS/Icons/Traffic-light-green-logo-30x30.png";
    String usrErrorIcon= "/SBS/Icons/user-error-icon-40x40.png";
    String usrExpiredIcon= "/SBS/Icons/expired-icon-40x40.png";
    String pcErrorIcon= "/SBS/Icons/pc-error-icon-40x40.png";
    String trafficYellowBulb= "/SBS/Icons/Traffic-light-yellow-logo-30x30.png";
    String serverErrorIcon= "/SBS/Icons/server-error-icon-40x40.png";
    String offlineIcon= "/SBS/Icons/disconnect_icon-40x40.png";
    String internetError= "/SBS/Icons/no-error-internet-40x40.png";
    String serverInfoErrorIcon= "/SBS/Icons/server-info-error-40x40.png";
    //***************ICONS DATABASE END*****************
    
}
