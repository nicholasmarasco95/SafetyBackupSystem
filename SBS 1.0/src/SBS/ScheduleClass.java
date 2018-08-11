/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;

import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 *
 * @author nicho
 */
public class ScheduleClass extends TimerTask{
    
    private JProgressBar progressBarTotal;
    private JProgressBar progressBarTmp;
    private JLabel tmpLabelStatus;
    private JLabel tmpLabelPercentageTotal;
    private JLabel tmpLabelPercentage;
    private JLabel labelTmpStatusName;
    private boolean serverBackup;
    private JLabel labelLastUpdate;
    Settings setting= new Settings();
    
    
    public ScheduleClass(JProgressBar progressBarTotal, JProgressBar progressBarTmp, JLabel tmpLabelStatus, JLabel tmpLabelPercentageTotal, JLabel tmpLabelPercentage, JLabel labelTmpStatusName, JLabel labelLastUpdate){
        this.progressBarTotal= progressBarTotal;
        this.progressBarTmp= progressBarTmp;
        this.tmpLabelStatus= tmpLabelStatus;
        this.tmpLabelPercentageTotal= tmpLabelPercentageTotal;
        this.tmpLabelPercentage= tmpLabelPercentage;
        this.labelTmpStatusName= labelTmpStatusName;
        this.serverBackup= serverBackup;
        this.labelLastUpdate= labelLastUpdate;
    }

    @Override
    public void run() {
        //check if Dev backup is enabled
        boolean serverStartCheck= setting.getBoolValue("serverBackupActive");
        boolean deviceStartCheck= setting.getBoolValue("deviceBackupActive");
        StartDevice startDev= new StartDevice(progressBarTotal, progressBarTmp, tmpLabelStatus, tmpLabelPercentageTotal, tmpLabelPercentage, labelTmpStatusName, serverStartCheck, labelLastUpdate);
        Thread thStartDev= new Thread(startDev);
        StartServer startSrv= new StartServer(progressBarTotal, progressBarTmp, tmpLabelStatus, tmpLabelPercentageTotal, tmpLabelPercentage, labelTmpStatusName, deviceStartCheck, labelLastUpdate);
        Thread thStartSrv= new Thread(startSrv);
        //check if both acrivate. If true start both backup (join server after device)
        if(deviceStartCheck && serverStartCheck){
            thStartDev.start();
            thStartSrv.start();
        }
        if(deviceStartCheck && !serverStartCheck){
            thStartDev.start();
        }
        if(!deviceStartCheck && serverStartCheck){
            thStartSrv.start();
        }
    }
    
}
