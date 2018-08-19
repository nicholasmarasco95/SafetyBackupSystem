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
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author nicho
 */
public class CopyTmp {
    
    private String backType;
    private Path tmpPath;
    private long backupSize;
    Set<Path> pathList;
    
    private JProgressBar progressBarTotal;
    private JProgressBar tmpProgressBar;
    private long tmpBytes= 0;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentage;
    private JLabel tmpLabelPercentageTotal;
    
    private int addPerc;
    private int totPerc;
    
    public CopyTmp(String backType, JProgressBar progressBarTotal, JProgressBar tmpProgressBar, JLabel tmpLabelStatus, JLabel tmpLabelPercentageTotal, JLabel tmpLabelPercentage, int totPerc, int addPerc, long backupSize, Set<Path> pathList){
        this.backType= backType;
        this.tmpProgressBar= tmpProgressBar;
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.tmpLabelPercentageTotal= tmpLabelPercentageTotal;
        this.progressBarTotal= progressBarTotal;
        this.addPerc= addPerc;
        this.totPerc= totPerc;
        this.backupSize= backupSize;
        this.pathList= pathList;
    }
    
    public long Copy(){
        
        long copyTimerStart = System.currentTimeMillis();  //start copying timer
        Iterator<Path> itPath= pathList.iterator();
        
        File tmpDest= new File(backType + "TmpCopy");
        if(!tmpDest.exists()){
            tmpDest.mkdir();
        }
        else{
            //delete the folder and create new one
            try {
                FileUtils.deleteDirectory(tmpDest);
                tmpDest.mkdir();
            } catch (IOException ex) {
                Logger.getLogger(CopyTmp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while(itPath.hasNext()){
            tmpPath= itPath.next();
            streamCopy(tmpPath.toFile(), tmpDest);
        }
        
        long copyTimerEnd = System.currentTimeMillis();  //end copying timer
        long copyTimerDelta= copyTimerEnd - copyTimerStart;
        return copyTimerDelta;
    }
    
    
    
    private void streamCopy(File tmpSrc, File dest){
        
        if(tmpSrc.isDirectory()){
            if(!dest.exists()){
                dest.mkdir();
            }
            updateLabelFolder(tmpSrc); //update jlabel
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
        if(backupSize>0){
            tmpProgressBar.setValue((int) (tmpBytes*100/backupSize));
            progressBarTotal.setValue((int) (tmpBytes*totPerc/backupSize) + addPerc);
        }
    }
    
    private void updateLabelsPercentage(long tmpBytes){
      int percentage= (int) (tmpBytes*100/backupSize);
      int percentageTot= (int) (tmpBytes*totPerc/backupSize) + addPerc;
      String strPerc= Integer.toString(percentage) + "%";
      String strPerc25= Integer.toString(percentageTot) + "%";
      if(backupSize>0){
          tmpLabelPercentage.setText(strPerc);
          tmpLabelPercentageTotal.setText(strPerc25);
      }  
    }
    
    private void updateLabelFolder(File tmpSrc){
        String tmpSrcStr= tmpSrc.toString();
        int len= tmpSrcStr.length();
        if(len>=70){
            if(len<89){
                String startStr= tmpSrcStr.substring(0, 20);
                String endStr= tmpSrcStr.substring(60, tmpSrcStr.length()-1);
                tmpLabelStatus.setText(startStr + "........" + endStr);
            }
            else{
                String startStr= tmpSrcStr.substring(0, 20);
                String endStr= tmpSrcStr.substring(60, 89);
                tmpLabelStatus.setText(startStr + "........" + endStr);
            }
            return;
        }
        tmpLabelStatus.setText(tmpSrcStr);
        return;
    }
    
}
