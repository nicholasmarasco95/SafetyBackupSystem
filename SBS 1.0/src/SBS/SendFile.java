/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
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
public class SendFile {
    
    private FileInputStream fis;
    private BufferedInputStream bis;
    private OutputStream os;
    private Socket sock;
    private Settings setting= new Settings();
    private String userId;
    private String pass;
    private String mac;
    String ip= setting.getStingValue("serverIp");
    private int connPort= Integer.parseInt(setting.getStingValue("serverPort"));
    private int filePort= Integer.parseInt(setting.getStingValue("filePortCl"));
    private String action= "";  //if empty send file
    private String fileName;
    private int fileSize;
    private PrintWriter pw;
    private JProgressBar progressBarTotal;
    private JProgressBar tmpProgressBar;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentageTotal;
    private JLabel tmpLabelPercentage;
    private BufferedReader br;
    
    private int addPerc;
    private int totPerc;
    
    public SendFile(String fileName, int fileSize, JProgressBar progressBarTotal, JProgressBar tmpProgressBar, JLabel tmpLabelStatus, JLabel tmpLabelPercentageTotal, JLabel tmpLabelPercentage, int totPerc, int addPerc){
        setting= new Settings();
        userId= setting.getStingValue("usrCode");
        pass= setting.getStingValue("usrPass");
        mac= setting.getStingValue("macAddr");
        ip= setting.getStingValue("serverIp");
        connPort= Integer.parseInt(setting.getStingValue("serverPort"));
        this.fileName= fileName;
        this.fileSize= fileSize;
        this.tmpProgressBar= tmpProgressBar;
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.tmpLabelPercentageTotal= tmpLabelPercentageTotal;
        this.progressBarTotal= progressBarTotal;
        this.addPerc= addPerc;
        this.totPerc= totPerc;
    }
    
    private boolean contactServerCheckError(){
        try{
            sock= new Socket(ip, connPort);
            os= sock.getOutputStream();
            pw= new PrintWriter(os, true);
            //BufferedReader br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
            br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
            boolean sending= true;
            while(sending){
                pw.println(action+","+userId+","+pass+","+mac);  //info to sent to server (in this order)
                String msgIn= br.readLine();
                if(msgIn.equals("usnotreg")){
                    System.out.println("----------->USER NOT REGISTRED");
                    return true;
                }
                if(msgIn.equals("uspasserr")){
                    System.out.println("----------->USER PASSWORD ERROR");
                    return true;
                }
                if(msgIn.equals("usisexp")){
                    System.out.println("----------->USER EXPIRED");
                    return true;
                }
                if(msgIn.equals("usrmacerr")){
                    System.out.println("----------->MAC ERROR");
                    return true;
                }
                else{
                    //server send expire date to tell to close connection
                    return false;
                }
            }
        } catch (IOException ex) {
            return true;
        }
        return false;
    }
    
    public long upload(String fileToSend){
        long uploadTimerStart = System.currentTimeMillis();  //start timer
        if(contactServerCheckError()) return -1;
        try{
            pw.println(fileSize);
            pw.println(fileName);
            Socket sendSock= new Socket(ip, filePort);   //connecting to sending file port
            DataOutputStream dos= new DataOutputStream(sendSock.getOutputStream());
            File file= new File(fileToSend);
            int arraySize= (int)file.length();  //used for println only
            byte[] array= new byte[1024];  //array is 1024 to use progress bar
            fis= new FileInputStream(file);
            bis= new BufferedInputStream(fis);
            int len;
            int tmpBytes=0;
            while((len= bis.read(array))>0){
                //System.out.println("SendFile " + tmpBytes + " bytes " + "of " + arraySize);  //debug
                dos.write(array, 0, len);
                dos.flush();
                tmpBytes+=len;
                updateProgressBars(tmpBytes);
                updateLabelsPercentage(tmpBytes);
            }
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        finally{
            try{
            if(bis!=null) bis.close();
            if(os!=null) os.close();
            //if(sock!=null) sock.close();
            } catch (IOException ex) {
                Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "ERROR " + ex);
                return -1;
            }
        }
        
        long uploadTimerEnd = System.currentTimeMillis();  //end timer
        long uploadTimerDelta= uploadTimerEnd - uploadTimerStart;
        return uploadTimerDelta;
    }
    
    
    private void updateProgressBars(long tmpBytes){
        if(fileSize>0){
            tmpProgressBar.setValue((int) (tmpBytes*100/fileSize));
            progressBarTotal.setValue((int) (tmpBytes*totPerc/fileSize) + addPerc);
        }
    }
    
    private void updateLabelsPercentage(long tmpBytes){
      if(fileSize>0){
      int percentage= (int) (tmpBytes*100/fileSize);
      int percentageTot= (int) (tmpBytes*totPerc/fileSize) + addPerc;
      String strPerc= Integer.toString(percentage) + "%";
      String strPerc25= Integer.toString(percentageTot) + "%";
      tmpLabelPercentage.setText(strPerc);
      tmpLabelPercentageTotal.setText(strPerc25);
      }  
    }
    
}
