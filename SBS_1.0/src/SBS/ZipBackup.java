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
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author nicho
 */

//site: http://www.avajava.com/tutorials/lessons/how-do-i-zip-a-directory-and-all-its-contents.html

public class ZipBackup {
    
    private long backupSize;
    private String backType;
    private JProgressBar progressBarTotal;
    private JProgressBar tmpProgressBar;
    private long tmpBytes= 0;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentage;
    private JLabel tmpLabelPercentageTotal;
    private String zipNameFile;
    private File toZipFolder;
    private String ZipFolderName= "TmpZip";
    
    private int addPerc;
    private int totPerc;

    
    public ZipBackup(String backType, JProgressBar progressBarTotal, JProgressBar tmpProgressBar, JLabel tmpLabelStatus, JLabel tmpLabelPercentageTotal, JLabel tmpLabelPercentage, int totPerc, int addPerc, String zipNameFile, long backupSize){
        this.backType= backType;
        this.backupSize= backupSize;
        createFolder();
        this.addPerc= addPerc;
        this.totPerc= totPerc;
        
        this.tmpProgressBar= tmpProgressBar;
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.tmpLabelPercentageTotal= tmpLabelPercentageTotal;
        this.progressBarTotal= progressBarTotal;
        this.zipNameFile= zipNameFile;
        toZipFolder= new File(backType + "TmpCopy");
    }
    
    public String GetFolder(){
        return ZipFolderName + "/" + zipNameFile + ".zip";
    }
    
    private void createFolder(){
        File tmpDest= new File(ZipFolderName);
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
    }
    
    public long Zip(){
        
        long zipTimerStart = System.currentTimeMillis();  //start zip timer
        
        List<File> fileListZip= new ArrayList<File>();
        getAllFiles(toZipFolder, fileListZip);  
        writeZipFile(toZipFolder, fileListZip); 
        
        long zipTimerEnd = System.currentTimeMillis();  //end copying timer
        long zipTimerDelta= zipTimerEnd - zipTimerStart;
        return zipTimerDelta;
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
      String strPercTot= Integer.toString(percentageTot) + "%";
      if(backupSize>0){
          tmpLabelPercentage.setText(strPerc);
          tmpLabelPercentageTotal.setText(strPercTot);
      }  
    }
    
    private void getAllFiles(File dir, List<File> fileList) {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, fileList);
            } else {
                //System.out.println("     file:" + file.getCanonicalPath());
            }
        }
    }
    
    private void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException, IOException {

		FileInputStream fis = new FileInputStream(file);

		// we want the zipEntry's path to be a relative path that is relative
		// to the directory being zipped, so chop off the rest of the path
		String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
				file.getCanonicalPath().length());
		ZipEntry zipEntry = new ZipEntry(zipFilePath);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
                        tmpBytes+=length;
                        updateProgressBars(tmpBytes);
                        updateLabelsPercentage(tmpBytes);
		}

		zos.closeEntry();
		fis.close();
	}
    
    private void writeZipFile(File directoryToZip, List<File> fileList) {

		try {
                        File testFile= new File(ZipFolderName + "/" + zipNameFile + ".zip");
                        FileOutputStream fos = new FileOutputStream(testFile);  //file name                        
                        
//			FileOutputStream fos = new FileOutputStream(fileName + ".zip");  //file name                        
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (File file : fileList) {
				if (!file.isDirectory()) { // we only zip files, not directories
					addToZip(directoryToZip, file, zos);
				}
			}

			zos.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
