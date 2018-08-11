/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 *
 * @author nicho
 */
public class DownloadStatusThread implements Runnable{
    
    JProgressBar progressBar;
    JLabel labelPerc;
    private String fileName;
    private Settings setting= new Settings();
    String ip;
    int port;
    String userId;
    String pass;
    String mac;
    
    public DownloadStatusThread(JProgressBar progressBar, String fileName, JLabel labelPerc){
        this.progressBar= progressBar;
        this.fileName= fileName;
        this.labelPerc= labelPerc;
    }

    @Override
    public void run() {
        ip= setting.getStingValue("serverIp");
        port= Integer.parseInt(setting.getStingValue("serverPort"));
        userId= setting.getStingValue("usrCode");
        pass= setting.getStingValue("usrPass");
        mac= getMacAddress();
        contactServer();
    }
    
    private String contactServer(){
        String fileList= "";
        String msgIn= "";
        String action= "download";  //user wants download
        try{
            Socket sock= new Socket(ip, port);
            OutputStream os= sock.getOutputStream();
            BufferedReader br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter pw= new PrintWriter(os, true);
            pw.println(action+","+userId+","+pass+","+mac);  //info to sent to server (in this order)
            //br.readLine(); //throw expire date message
            msgIn= br.readLine();
            if(msgIn.equals("usnotreg")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User Not Registed");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
            if(msgIn.equals("uspasserr")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Wrong Password");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
            if(msgIn.equals("usisexp")){
                //do nothing, user can download his files
            }
            if(msgIn.equals("usrmacerr")){
                //do nothing, user can download his files
            }
            else{
                pw.println(fileName);  //send file name
                String fileSize= br.readLine();  //receive file size
                ReceiveFile(Integer.parseInt(fileSize.replaceAll("\\s","")), sock, fileName);  //receive file
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

    public void ReceiveFile(int fileSize, Socket sock, String fileName){
        String destFold= setting.getStingValue("downloadFoldDest");
        FileOutputStream fos= null;
        BufferedOutputStream bos= null;
        try {
            int ret;
            int bytesRead=0;
            fos= new FileOutputStream(destFold+"/"+fileName);  //receive file to User Dedicated folder
            bos= new BufferedOutputStream(fos);
            InputStream input= sock.getInputStream();
            byte[] bytesArray= new byte[fileSize];
            
            //ret= input.read(bytesArray, 0, bytesArray.length);
            //bytesRead= ret;
            while(bytesRead<fileSize){
                ret= input.read(bytesArray, bytesRead, (bytesArray.length-bytesRead));
                if(ret>=0){
                    bytesRead+=ret;
                    updateProgressBarLabel(bytesRead, fileSize);
                }
            }
            labelPerc.setText("Writing file, please wait");
            bos.write(bytesArray, 0, bytesRead);
            bos.flush();
            labelPerc.setText("Completed");
            progressBar.setValue(0);
        } catch (IOException ex) {
            Logger.getLogger(DownloadStatusGUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            try {
                fos.close();
                bos.close();
            } catch (IOException ex) {
                Logger.getLogger(DownloadStatusGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void updateProgressBarLabel(long bytesRead, int fileSize){
        if(bytesRead>0){
            int percentage= (int) (bytesRead*100/fileSize);
            percentage-=1;
            String strPerc= Integer.toString(percentage) + "%";
            if(percentage>0){  //set to 99 to write file
                progressBar.setValue(percentage);
                labelPerc.setText(strPerc);
            }
        }
    }
    
    
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/SBS/Icons/error-logo-50x50.png";
    
        //new ImageIcon(getClass().getResource(ICON))
    //***************ICONS DATABASE END*****************
    
}
