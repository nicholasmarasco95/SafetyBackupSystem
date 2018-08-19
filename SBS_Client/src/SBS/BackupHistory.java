/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 *
 * @author nicho
 */
public class BackupHistory {
    
    private String backType;
    private File file;
    DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss:SSS");
    
    
    public BackupHistory(String backType, long copyTmpTimer, long zipTimer, long copyDestTimer, long totalTimerEnd){
        file= new File(backType + "BackupHistory.csv");
        
    }
    
}
