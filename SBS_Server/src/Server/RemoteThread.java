/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.awt.Color;
import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nicholas
 */
public class RemoteThread implements Runnable{
    
    private Socket connSock;
    private PrintWriter pw;
    private BufferedReader br; 
    private JLabel activeConnLabel;
    private Settings setting= new Settings();
    
    
    public RemoteThread(Socket connSock, BufferedReader br, PrintWriter pw, JLabel activeConnLabel){
        this.connSock=connSock;
        this.br= br;
        this.pw= pw;
        this.activeConnLabel= activeConnLabel;
    }
    
    
    public void run(){
        try {
            //br= new BufferedReader(new InputStreamReader(connSock.getInputStream()));
            //pw= new PrintWriter(connSock.getOutputStream(), true);
            
            String pass= br.readLine();
            
            if(!pass.equals(setting.getStingValue("srvRemotePass"))){
                pw.println("remPassErr");
                connSock.close();
                return;
            }
            pw.println("ackPass");
            int limit= setting.getIntValue("srvGbLim");
            if(limit>0){
                pw.println(limit);
            }
            else pw.println("OFF");
            
            while(true){
                String info= br.readLine();
                
                if(info.equals("shutdown")){
                    shutdownServerPc();
                    pw.println("ackShut");
                }
                
                if(info.equals("reboot")){
                    rebootServerPc();
                    pw.println("ackReboot");
                }
                if(info.equals("restart")){
                    restartApp();
                }
                if(info.equals("usrlist")){
                    updateCmrTable();
                }
                if(info.equals("addusr")){
                    addUsr();
                }
                if(info.equals("editusr")){
                    editUsr();
                }
                if(info.equals("upmac")){
                    editUsrMac();
                }
                if(info.equals("history")){
                    history();
                }
                if(info.equals("disconnect")){
                    disconnect();
                }
                
            }
            
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void restartApp(){
        String jarName= "";
        String currentDir= System.getProperty("user.dir");
        File folder= new File(currentDir);
        File[] listOfFiles= folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
              String tmpFile= listOfFiles[i].getName();
              if(tmpFile.contains(".jar")){
                  jarName= tmpFile;
              }
            }
        }
        if(jarName.length()<2){
            pw.println("errRestart");
            return;
        }
        String execStr= "java -jar " + jarName;
        try {
            pw.println("ackRestart");  //Send acknowledge  to Remote Software
            setting.SaveSetting("bool", "srvRestartCheck", "true");
            Runtime.getRuntime().exec(execStr);
            System.exit(0);
        } catch (IOException ex) {
            Logger.getLogger(null).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void shutdownServerPc(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("shutdown -s");
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void rebootServerPc(){
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("shutdown -r");
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void disconnect(){
        try {
            connSock.close();
            decrActiveConnLabel();
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void decrActiveConnLabel(){
        String tmpStr= activeConnLabel.getText();
        int activeConn= Integer.parseInt(tmpStr);
        activeConn-=1;
        if(activeConn>=0){
            activeConnLabel.setText(Integer.toString(activeConn));
        }
        else activeConnLabel.setText("0");
    }
    
    private void updateCmrTable(){
        CmrFileManager cmrFileMan= new CmrFileManager();
        List<String> listLines= cmrFileMan.getList();
        int listSize= listLines.size();
        if(listSize<=0){
            pw.println("nousr");
            return;
        }
        pw.println(listSize);
        Iterator<String> itList= listLines.iterator();
        while(itList.hasNext()){
            pw.println(itList.next());
        }
        pw.println("end");
    }
    
    private void addUsr(){
        try {
            String name= br.readLine();
            String last= br.readLine();
            String expireDateStr= br.readLine();
            String email= br.readLine();
            String pass= br.readLine();
            String todayDateStr= br.readLine();
            CmrFileManager cmrFileMan= new CmrFileManager();
            int userRec= cmrFileMan.AddNewUser(name, last, expireDateStr, email, pass, todayDateStr);
            pw.println(userRec);
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void editUsr(){
        try {
            String userId= br.readLine();
            String expire= br.readLine();
            CmrFileManager cmrFileMan= new CmrFileManager();
            cmrFileMan.updateUserExpire(userId, expire);
            pw.println("ackex");
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void editUsrMac(){
        try {
            String usrId= br.readLine();
            CmrFileManager cmrFileMan= new CmrFileManager();
            cmrFileMan.resetUserMacCounter(usrId);
            pw.println("ackmac");
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void history(){
        try {
            String usrId= br.readLine();
            UpdateHistory upHist= new UpdateHistory(usrId);
            List<String> listLines= upHist.getList();
            int listSize= listLines.size();
            if(listSize<=0){
                pw.println("nohist");
                return;
            }
            pw.println(listSize);
            Iterator<String> itList= listLines.iterator();
            while(itList.hasNext()){
                pw.println(itList.next());
            }
            pw.println("end");
        } catch (IOException ex) {
            Logger.getLogger(RemoteThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/Server/Icons/error-logo-50x50.png";
    //***************ICONS DATABASE END*****************
    
}
