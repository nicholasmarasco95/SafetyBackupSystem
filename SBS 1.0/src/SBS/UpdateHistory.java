/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nicho
 */
public class UpdateHistory{
    
    private boolean deviceOn;
    private boolean serverOn;
    private File file= new File("SBS-History.csv");
    private PrintWriter pw;
    private BufferedReader br;
    private String computerName;
    private String fileName;
    private String date;
    private long sizeLong;
    private String size;
    private Settings setting;
    private String backType;
    private long totTimer;
    private long uploadTimer;
    
    
    public UpdateHistory(long totTimer, long uploadTimer, boolean deviceOn, boolean serverOn, String fileName, String date, long sizeLong){
        this.totTimer= totTimer;
        this.uploadTimer= uploadTimer;
        this.deviceOn= deviceOn;
        this.serverOn= serverOn;
        computerName= getComputerName();
        this.fileName= fileName;
        this.date= date;
        this.sizeLong= sizeLong;
        size= humanReadableByteCount(sizeLong, false);
        setting= new Settings();
    }
    
    public UpdateHistory(){
        //Constructor use to update History Table
    }
    
    
    public void updateDeviceHistory(){
        OpenFile();
        String uploadTimerStr, totTimerStr;
        totTimerStr= convertTime(totTimer);
        if(uploadTimer<0) uploadTimerStr= "---";
        else uploadTimerStr= convertTime(uploadTimer);
        String strToWrite= null;
        if(deviceOn && !serverOn){
            backType= "DEV";
            strToWrite= date + ", " + fileName + ", " + size + ", " +
                backType + ", " + totTimerStr + ", " + computerName+ ", " + uploadTimerStr + "\n";
        }
        if(deviceOn && serverOn){
            backType= "DEV & SRV";
            strToWrite= date + ", " + fileName + ", " + size + ", " +
                backType + ", " + totTimerStr + ", " + computerName+ ", " + uploadTimerStr + "\n";
        }
        if(serverOn && !deviceOn){
            backType= "SRV";
            strToWrite= date + ", " + fileName + ", " + size + ", " +
                backType + ", " + totTimerStr + ", " + computerName+ ", " + uploadTimerStr + "\n";
        }
        setting.SaveSetting("string", "lastBackup", date +"     "+ size);  //update last backup setting
        
        pw.append(strToWrite);
        CloseFile();
    }
    
    
    private static String humanReadableByteCount(long bytes, boolean si) {
        //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
    
    
    private String convertTime(long timeMillis){
        String time= String.format("%02d HH %02d MM",TimeUnit.MILLISECONDS.toHours(timeMillis), TimeUnit.MILLISECONDS.toMinutes(timeMillis));
        return time;
    }
    
    
    private void OpenFile(){
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                pw= new PrintWriter(new FileOutputStream(file, true));
                br= new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
            }
            pw.write("Date, File Name, Size, Backup Type, Total Timer, Computer Name, Upload Timer\n");
        }
        else{
            try {
                pw= new PrintWriter(new FileOutputStream(file, true));
                br= new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    private void CloseFile(){
        try {
            pw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private String getComputerName(){
        String hostname= "Unknown";
        InetAddress addr;
        try {
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hostname;
    }
    
    
    public List<String> getList(){
        //return reversed list of lines (except first) in file
        String line;
        List<String> list= new LinkedList<String>();
        OpenFile();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                if(line.length()>2){
                    list.add(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        CloseFile();
        Collections.reverse(list);
        return list;
    }
    
    
}
