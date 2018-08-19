/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author nicho
 */
public class CopyToDestination {
    
    private String backType;
    private File dest;
    private File zipFolder;
    private Settings setting= new Settings();
    private String ZipFolderName= "TmpZip";
    private JProgressBar progressBarTotal;
    private JProgressBar tmpProgressBar;
    private long tmpBytes= 0;
    private long sizeOfZip= 0;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentage;
    private JLabel tmpLabelPercentageTotal;
    
    private int addPerc;
    private int totPerc;
    
    
    public CopyToDestination(String backType, JProgressBar progressBarTotal, JProgressBar tmpProgressBar, JLabel tmpLabelStatus, JLabel tmpLabelPercentageTotal, JLabel tmpLabelPercentage, int totPerc, int addPerc){
        this.backType= backType;
        dest= new File(setting.getStingValue(backType + "FoldDest"));
        zipFolder= new File(ZipFolderName);
        this.tmpProgressBar= tmpProgressBar;
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.tmpLabelPercentageTotal= tmpLabelPercentageTotal;
        this.progressBarTotal= progressBarTotal;
        this.addPerc= addPerc;
        this.totPerc= totPerc;
    }
    
    public long Copy(){
        
        long copyTimerStart = System.currentTimeMillis();  //start copying timer
        
        sizeOfZip= FileUtils.sizeOfDirectory(zipFolder);
        
        streamCopy(zipFolder, dest);
        
        long copyTimerEnd = System.currentTimeMillis();  //end copying timer
        long copyTimerDelta= copyTimerEnd - copyTimerStart;
        return copyTimerDelta;
    }
    
    private void streamCopy(File tmpSrc, File dest){
        
        if(tmpSrc.isDirectory()){
            if(!dest.exists()){
                dest.mkdir();
            }
            String listFile[]= tmpSrc.list();  //list all files/folder
            for(String file: listFile){
                //construct the src and dest file structure
                File tmpSrcFile= new File(tmpSrc, file);
                File destFile= new File(dest, file);
                streamCopy(tmpSrcFile, destFile); //recursive copy
            }
        }
        else{
            try {
                //if file, copy it
                InputStream in= new FileInputStream(tmpSrc);
                OutputStream out= new FileOutputStream(dest);
                
                byte[] buffer= new byte[1024];
                
                int len;
                while((len=in.read(buffer))>0){
                    out.write(buffer,0, len);
                    tmpBytes+=len; //to update tmpProgressBar
                    updateProgressBars(tmpBytes);
                    updateLabelsPercentage(tmpBytes); //uodate Percentage Label
                }
                in.close();
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CopyTmp.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CopyTmp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
       
    }
    
    private void updateProgressBars(long tmpBytes){
        if(sizeOfZip>0){
            tmpProgressBar.setValue((int) (tmpBytes*100/sizeOfZip));
            progressBarTotal.setValue((int) (tmpBytes*totPerc/sizeOfZip) + addPerc);
        }
    }
    
    private void updateLabelsPercentage(long tmpBytes){
      if(sizeOfZip>0){
      int percentage= (int) (tmpBytes*100/sizeOfZip);
      int percentageTot= (int) (tmpBytes*totPerc/sizeOfZip) + addPerc;
      String strPerc= Integer.toString(percentage) + "%";
      String strPerc25= Integer.toString(percentageTot) + "%";
      tmpLabelPercentage.setText(strPerc);
      tmpLabelPercentageTotal.setText(strPerc25);
      }  
    }
    
}
