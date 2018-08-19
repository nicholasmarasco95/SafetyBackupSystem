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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author nicho
 */
public class BackupPath {
    
    private Set<Path> pathSet;
    private PrintWriter pw;
    private BufferedReader br;
    private String deviceFileName= "DevicePathBackup.txt";
    private String serverFileName= "ServerPathBackup.txt";
    private File file;
    private Settings setting= new Settings();
    private String backupType;
    
    public BackupPath(String backupType){
        pathSet= new HashSet<Path>();
        this.backupType= backupType;
        if(backupType.equals("device")){
            file= new File(deviceFileName);
        }
        if(backupType.equals("server")){
            file= new File(serverFileName);
        }
    }
    
    public void CheckCreateFiles(){
        File devFile= new File(deviceFileName);
        File srvFile= new File(serverFileName);
        if(!devFile.exists()){
            try {
                devFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if(!srvFile.exists()){
            try {
                srvFile.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void OpenFile(){
       if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            pw= new PrintWriter(new FileOutputStream(file, true));
            br= new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
        }
        LoadSet(); 
    }
    
    private void refreshSetting(){
        String strFoldNum= (Integer.toString(GetNumPath()));
        setting.SaveSetting("int", backupType+"FoldNum", strFoldNum);
    }
    
    private void LoadSet(){
        String tmpLine= null;
        pathSet= new HashSet<Path>();
        try {
            while((tmpLine=br.readLine())!=null){
                pathSet.add(Paths.get(tmpLine));
            }
        } catch (IOException ex) {
            Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean IsInSet(Path p){
        OpenFile();
        Iterator<Path> itPath= pathSet.iterator();
        while(itPath.hasNext()){
            if(itPath.next().equals(p)){
                CloseFile();
                return true;
            }
        }
        CloseFile();
        return false;
    }
    
    public void removeDeletedFolder(){
        OpenFile();
        Iterator itPath= pathSet.iterator();
        Path tmpPath;
        while(itPath.hasNext()){
            tmpPath= (Path) itPath.next();
            if(!Files.exists(tmpPath)){
                RemovePath(tmpPath);
            }
        }
        CloseFile();
    }
    
    public void AddPath(Path newPath){
        OpenFile();
        pathSet.add(newPath);
        RefreshFile();
        CloseFile();
    }
    
    private void EraseFile(){
        PrintWriter pwErase= null;
        try {
            pwErase = new PrintWriter(new FileOutputStream(file, false));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
        }
        pwErase.print("");
        pwErase.flush();
        refreshSetting();
    }
    
    public void EraseSet(){
        OpenFile();
        pathSet= new HashSet<Path>();
        EraseFile();
        CloseFile();
    }
    
    private void RefreshFile(){
        EraseFile();
        Iterator<Path> itPath= pathSet.iterator();
        while(itPath.hasNext()){
            pw.println(itPath.next());
            pw.flush();
        }
        refreshSetting();
    }
    
    public void RemovePath(Path pR){
        OpenFile();
        pathSet.remove(pR);
        RefreshFile();
        CloseFile();
    }
    
    public Set<Path> GetPathSet(){
        OpenFile();
        CloseFile();
        return pathSet;
    }
    
    private void CloseFile(){
        try {
            pw.close();
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(BackupPath.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private int GetNumPath(){
        return pathSet.size();
        /*
        This is used by refreshSetting() method.
        With refreshSetting() method every times that there is a change in set
        the number of Path is saved in settings, the key is backupType+"FoldNum".
        */
    }
    
    public boolean isEmpty(){
        OpenFile();
        CloseFile();
        return pathSet.isEmpty();
    }
    
    public boolean existsPath(){
        OpenFile();
        Iterator<Path> itPath= pathSet.iterator();
        while(itPath.hasNext()){
            Path tmpPath= itPath.next();
            if(!Files.exists(tmpPath)){
                CloseFile();
                return false;
            }
        }
        CloseFile();
        return true;
    }
    
    public long BackupSize(){
        OpenFile();
        long size=0;
        Iterator<Path> itPath= pathSet.iterator();
        while(itPath.hasNext()){
            size+=FolderSize(itPath.next());
        }
        CloseFile();
        return size;
    }
    
    private static long FolderSize(Path path) {
    //from: http://stackoverflow.com/questions/2149785/get-size-of-folder-or-file/19877372#19877372
    final AtomicLong size = new AtomicLong(0);
    try {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {

                //System.out.println("skipped: " + file + " (" + exc + ")");
                // Skip folders that can't be traversed
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {

                if (exc != null)
                    System.out.println("had trouble traversing: " + dir + " (" + exc + ")");
                // Ignore errors traversing a folder
                return FileVisitResult.CONTINUE;
            }
        });
    } catch (IOException e) {
        throw new AssertionError("walkFileTree will not throw IOException if the FileVisitor does not");
    }
    return size.get();
    }
}
