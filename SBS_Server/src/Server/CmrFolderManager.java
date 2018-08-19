/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author nicho
 */
public class CmrFolderManager {
    
    private String cmrId;
    private File folder;
    private DaysLimit daysLimit;
    private UpdateHistory upHist;
    
    public CmrFolderManager(){
        daysLimit= new DaysLimit(null, true);
    }
    
    private void folderCheck(String cmrId){
       File folder= new File(cmrId);
        if(!folder.exists()){
            folder.mkdir();
        } 
    }
    
    public void ReceiveFile(int fileSize, Socket sock, String fileName, String cmrId, PrintWriter pw){
        folderCheck(cmrId);
        FileOutputStream fos= null;
        BufferedOutputStream bos= null;
        try {
            int ret;
            int bytesRead=0;
            fos= new FileOutputStream(cmrId+"/"+fileName);  //receive file to User Dedicated folder
            //bos= new BufferedOutputStream(fos);
            InputStream input= sock.getInputStream();
            //DataInputStream dis= new DataInputStream(sock.getInputStream());
            byte[] bytesArray= new byte[1024];
            
            //System.out.println("CmrFoldMan -- Received " + bytesRead + " of " + fileSize);  //debug
            while((bytesRead= input.read(bytesArray))!=-1){
                fos.write(bytesArray, 0, bytesRead);
                //System.out.println("CmrFoldMan -- Received " + bytesRead + " of " + fileSize);  //debug
            }
            fos.flush();   //check if needed
            
            upHist= new UpdateHistory(fileName, fileSize, cmrId);
            upHist.update();
            
            daysLimit.deleteFilesLimit(fileSize, cmrId);  //delete files that exceed memory limit
            
            
        } catch (IOException ex) {
            Logger.getLogger(CmrFolderManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
            try {
                fos.close();
                bos.close();
            } catch (IOException ex) {
                Logger.getLogger(CmrFolderManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
