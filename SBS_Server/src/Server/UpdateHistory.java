/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nicholas
 */
public class UpdateHistory {
    
    private File file;
    private String fileName= "SBS-History.csv";
    private String histFoldName= "BackupHistory";
    private File histFold= new File(histFoldName);
    private PrintWriter pw;
    private BufferedReader br;
    private String backupName;
    private String cmrId;
    private String fileSize;
    DateTimeFormatter dateForm = DateTimeFormatter.ofPattern("MMM-dd-yyyy");
    LocalDate date;
    private long sizeLong;
    private boolean readOnly= true;
    private boolean notExists= false;
    
    
    public UpdateHistory(String backupName, long sizeLong, String cmrId){
        checkFolder();
        this.file= new File(histFoldName + "\\" + cmrId + "-" +fileName);
        this.backupName= backupName;
        this.date= date;
        this.sizeLong=sizeLong;
        this.cmrId= cmrId;
        this.fileSize= humanReadableByteCount(sizeLong, false);
        this.date= LocalDate.now();
        this.readOnly= false;
    }
    
    public UpdateHistory(String cmrId){
        //to update GUI table
        this.readOnly= true;
        checkFolder();
        this.file= new File(histFoldName + "\\" + cmrId + "-" +fileName);
        this.cmrId= cmrId;
    }
    
    
    public void update(){
        OpenFile();
        String strToWrite= null;
        
        strToWrite= dateForm.format(date) + "," + backupName + "," + fileSize + "\n";
        
        pw.append(strToWrite);
        CloseFile();
    }
    
    
    private void checkFolder(){
        if(!histFold.exists()){
            histFold.mkdir();
        }
    }
    
    private void OpenFile(){
        if(!file.exists()){
            if(!readOnly){
                try {
                    file.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    pw= new PrintWriter(new FileOutputStream(file, true));
                    br= new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
                }
                pw.write("Date, File Name, Size\n");
            }
            else{
                notExists= true;
                return;
            }
        }
        else{
            try {
                pw= new PrintWriter(new FileOutputStream(file, true));
                br= new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void CloseFile(){
        try {
            pw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public List<String> getList(){
        //return reversed list of lines (except first) in file
        String line;
        List<String> list= new LinkedList<String>();
        OpenFile();
        if(notExists) return list;
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                if(line.length()>2){
                    list.add(line);
                }
            }
        } catch (IOException ex) {
            if(readOnly) return list;
            Logger.getLogger(UpdateHistory.class.getName()).log(Level.SEVERE, null, ex);
        }
        CloseFile();
        Collections.reverse(list);
        return list;
    }
    
    private static String humanReadableByteCount(long bytes, boolean si) {
        //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
