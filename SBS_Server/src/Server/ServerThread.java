/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 *
 * @author nicho
 */
public class ServerThread implements Runnable{
    
    private CmrFileManager cmrFileMan;
    private Socket connSock;
    private ServerSocket fileSrvSock;
    private CmrFolderManager cmrFolderMan= new CmrFolderManager();
    private int fileSize;
    private String fileName;
    private PrintWriter pw;
    private BufferedReader br; 
    private boolean running;
    private JLabel activeConnLabel;
    private FileInputStream fis;
    private BufferedInputStream bis;
    private OutputStream os;
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
    private Settings settings= new Settings();
    
    public ServerThread(Socket connSock, ServerSocket fileSrvSock, JLabel activeConnLabel){
        running=true;
        cmrFileMan= new CmrFileManager();
        this.connSock= connSock;
        this.fileSrvSock= fileSrvSock;
        this.activeConnLabel= activeConnLabel;
    }
    
    public void run(){
        try{
            br= new BufferedReader(new InputStreamReader(connSock.getInputStream()));
            pw= new PrintWriter(connSock.getOutputStream(), true);
            String connSockIp= connSock.getRemoteSocketAddress().toString();
            connSockIp= extraxtIp(connSockIp);
            //System.out.println("ServerThread -- connSockIp: " + connSockIp);  //debug
            while(running){
                //check user registration
                String inputLine= br.readLine();
                if(inputLine.equals("remote")){
                    if(settings.getBoolValue("srvRemoteActive")){
                        pw.println("aknConn");
                        RemoteThread rmt= new RemoteThread(connSock, br, pw, activeConnLabel);
                        Thread thrRmt= new Thread(rmt);
                        thrRmt.start();
                    }
                    else{
                        pw.println("remoteoff");
                    }
                    return;
                }
                String[] inputLineArray= inputLine.split(",");
                String info= inputLineArray[0];  //check client intentions
                String user= inputLineArray[1];
                String pass= inputLineArray[2];
                String mac= inputLineArray[3];
                
                boolean checkUsr= checkUser(user, pass, mac);
                
                if(info.equals("checkUsrOnly")){
                    //check done in "checkUsr"
                    //server send expire date to tell to close connection
                    running=false;
                    break;
                }
                
                if(info.equals("sendList")){
                    String list= getClientFilesList(user);
                    pw.println(list);
                    break;
                }
                
                if(info.equals("download")){
                    //clients wants file
                    String downFileName= br.readLine();     //client send file name
                    int fileSize= getFileSize(user, downFileName);  //send file size
                    pw.println(fileSize); //send file
                    sendFile(user, downFileName);
                }
                
                if(!checkUsr){
                    running=false;
                    break;
                }
                
                //pw.println("readyRec");  //client can send file;
                fileSize= Integer.parseInt(br.readLine());
                fileName= br.readLine();
                Socket fileSock= fileSrvSock.accept();
                String fileSockIp= fileSock.getRemoteSocketAddress().toString();
                fileSockIp= extraxtIp(fileSockIp);
                //System.out.println("ServerThread -- fileSock IP: " + fileSockIp);         //debug
                if(!fileSockIp.equals(connSockIp)) return;          //if Ip of connSock not equals to fileSockIp close connectino
                cmrFolderMan.ReceiveFile(fileSize, fileSock, fileName, user, pw);
                running=false;
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            decrActiveConnLabel();
        }
        decrActiveConnLabel();
        try {
            pw.close();
            br.close();
            connSock.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String extraxtIp(String ip){
        int ind= ip.indexOf(":");
        String retIp= ip.substring(0, ind);
        return retIp;
    }
    
    private int getFileSize(String user, String fileName){
        File folder= new File(user);
        File[] listOfFiles= folder.listFiles();
        int size= 0;
        for(int i=0; i<listOfFiles.length; i++){
            if(listOfFiles[i].getName().equals(fileName)) size= (int) listOfFiles[i].length();
        }
        return size;
    }
    
    private String getClientFilesList(String user){
        File folder= new File(user);
        File[] listOfFiles= folder.listFiles();
        String list= "";
        for(int i=0; i<listOfFiles.length; i++){
            list+= listOfFiles[i].getName() + ", " + listOfFiles[i].lastModified() + ", " + listOfFiles[i].length() + "~";
        }
        return list;
    }
    
    private void sendFile(String user, String fileName){
        File fileToSend= new File(user+"/"+fileName);
        try{
            byte[] array= new byte[1024];
            os= connSock.getOutputStream();
            fis= new FileInputStream(fileToSend);
            bis= new BufferedInputStream(fis);
            int len;
            while((len=bis.read(array))>0){
                //System.out.println("ServerThread -- SendFile " + len + " bytes ");  //debug
                os.write(array, 0, len);
                os.flush();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            try{
                if(bis!=null) bis.close();
                if(os!=null) os.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private boolean checkUser(String user, String pass, String usrMac){
        if(!cmrFileMan.CheckUserRegistration(user)){
            pw.println("usnotreg");
            running=false;
            return false;
        }
        //check user password
        if(!cmrFileMan.CheckUserPassword(user, pass)){
            pw.println("uspasserr");
            running=false;
            return false;
        }
        //check user expire
        String todayDate= dtf.format(LocalDateTime.now());
        String expireCheck= cmrFileMan.CheckUserExpire(user, todayDate);
        if(expireCheck.equals("ex")){
            pw.println("usisexp");
            running= false;
            return false;
        }
        //check user Mac
        if(!cmrFileMan.CheckUserMac(user, usrMac)){
            pw.println("usrmacerr");
            running= false;
            return false;
        }
        pw.println(expireCheck);  //send expire date to cmr
        return true;
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
    
}
