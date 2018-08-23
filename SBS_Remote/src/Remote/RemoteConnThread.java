/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Remote;

import java.awt.Color;
import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Nicholas
 */
public class RemoteConnThread implements Runnable{
    
    private String srvIp;
    private int srvPort;
    private Socket sock;
    private Settings setting= new Settings();
    private JButton connBtn;
    private JButton disBtn;
    private JButton shutBtn;
    private JButton rebootBtn;
    private JButton restartBtn;
    private JLabel srvStatusLabel;
    private JLabel labelSrvStatus;
    private JLabel cmrNumberLabel;
    private PrintWriter pw;
    private BufferedReader br; 
    private JButton btnRefreshCmr;
    private JButton btnAddUser;
    private JTable cmrTable;
    private JTable historyTable;
    private JLabel backupNumberLabel;
    private JButton showBtn;
    private JLabel labelLimitStorage;
    private JLabel labelDevDayLimTxt;
    
    
    
    public RemoteConnThread(JButton connBtn, JButton disBtn, JButton shutBtn, JButton rebootBtn, JButton restartBtn, JLabel srvStatusLabel, JLabel labelSrvStatus, JButton btnRefreshCmr, JButton btnAddUser, JLabel cmrNumberLabel, JTable cmrTable, JTable historyTable, JLabel backupNumberLabel, JButton showBtn, JLabel labelLimitStorage, JLabel labelDevDayLimTxt){
        this.connBtn=connBtn;
        this.disBtn= disBtn;
        this.shutBtn= shutBtn;
        this.rebootBtn= rebootBtn;
        this.restartBtn= restartBtn;
        this.srvStatusLabel= srvStatusLabel;
        this.labelSrvStatus= labelSrvStatus;
        this.btnRefreshCmr= btnRefreshCmr;
        this.btnAddUser= btnAddUser;
        this.cmrNumberLabel= cmrNumberLabel;
        this.cmrTable= cmrTable;
        this.historyTable= historyTable;
        this.backupNumberLabel= backupNumberLabel;
        this.showBtn= showBtn;
        this.labelLimitStorage= labelLimitStorage;
        this.labelDevDayLimTxt= labelDevDayLimTxt;
    }
    
    public void run(){
        connect();
    }
    
    public void connect(){
        srvIp= setting.getStingValue("remoteSrvIp");
        srvPort= setting.getIntValue("remoteSrvPort");
        LoadingGUI loadingClass = new LoadingGUI(null, true);
        try {
            Thread loadingThread= new Thread(loadingClass);
            loadingThread.start();
            sock= new Socket(srvIp, srvPort);
            br= new BufferedReader(new InputStreamReader(sock.getInputStream()));
            pw= new PrintWriter(sock.getOutputStream(), true);
            pw.println("remote");
            String input= br.readLine();
            if(input.equals("remoteoff")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Remote Connection Disabled");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
                loadingClass.close();
                return;
            }
            pw.println(setting.getStingValue("remotePass"));
            input= br.readLine();
            if(input.equals("remPassErr")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Wrong Password");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
                loadingClass.close();
                return;
            }
            String limit= br.readLine();
            if(limit.equals("OFF")){
                labelLimitStorage.setForeground(Color.orange);
                labelLimitStorage.setText("OFF");
            }
            else{
                labelLimitStorage.setForeground(Color.BLUE);
                labelLimitStorage.setText(limit + " GB");
            }
            
            loadingClass.close();
            srvStatusLabel.setText("Connected");
            labelSrvStatus.setIcon(new ImageIcon(getClass().getResource(connIcon)));
            setting.SaveSetting("bool", "remoteConnected", "true");
            connBtn.setEnabled(false);
            disBtn.setEnabled(true);
            shutBtn.setEnabled(true);
            rebootBtn.setEnabled(true);
            restartBtn.setEnabled(true);
            btnRefreshCmr.setEnabled(true);
            btnAddUser.setEnabled(true);
            showBtn.setEnabled(true);
            labelDevDayLimTxt.setForeground(Color.black);
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
            loadingClass.close();
            disBtn.setEnabled(false);
            
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Server Unreachable");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
        }
    }
    
    public void shutdown(){
        try {
            pw.println("shutdown");
            String input= br.readLine();
            if(input.equals("ackShut")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Remote server will shutdown");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                disconnected();
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void reboot(){
        try {
            pw.println("reboot");
            String input= br.readLine();
            if(input.equals("ackReboot")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Remote server will reboot");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                disconnected();
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void restart(){
        try {
            pw.println("restart");
            String input= br.readLine();
            if(input.equals("ackRestart")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("SBS Server will Restart");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                disconnected();
            }
            else if(input.equals("errRestart")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Restart Error");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void disconnect(){
        pw.println("disconnect");
        setting.SaveSetting("bool", "remoteConnected", "false");
        disconnected();
    }
    
    private void disconnected(){
        srvStatusLabel.setText("Ready");
        connBtn.setEnabled(true);
        disBtn.setEnabled(false);
        shutBtn.setEnabled(false);
        rebootBtn.setEnabled(false);
        restartBtn.setEnabled(false);
        btnRefreshCmr.setEnabled(false);
        btnAddUser.setEnabled(false);
        showBtn.setEnabled(false);
        labelDevDayLimTxt.setForeground(Color.gray);
        labelLimitStorage.setText("");
    }
    
    public void refreshUsrTable(){
        try {
            pw.println("usrlist");
            String ans= br.readLine();
            if(ans.equals("nousr")){
                cmrNumberLabel.setForeground(Color.red);
                cmrNumberLabel.setText("No Customers");
                return;
            }
            List<String> inputLines= new LinkedList<String>();
            int listSize= Integer.parseInt(ans);
            String lineInput= br.readLine();
            while(!lineInput.equals("end")){
                inputLines.add(lineInput);
                lineInput= br.readLine();
            }
            int rowNum= cmrTable.getRowCount();
            if(listSize>rowNum){
                DefaultTableModel model = (DefaultTableModel) cmrTable.getModel();
                model.setRowCount(listSize);
            }
            Iterator<String> itList= inputLines.iterator();
            int rowCounter=0;
            while(itList.hasNext()){
                String tmpArray[]= itList.next().split(",");
                if(!tmpArray[0].equals("000")){
                    cmrTable.getModel().setValueAt(tmpArray[0], rowCounter, 0);
                    cmrTable.getModel().setValueAt(tmpArray[1], rowCounter, 1);
                    cmrTable.getModel().setValueAt(tmpArray[2], rowCounter, 2);
                    cmrTable.getModel().setValueAt(tmpArray[3], rowCounter, 3);
                    cmrTable.getModel().setValueAt(tmpArray[5], rowCounter, 4);
                    cmrTable.getModel().setValueAt(tmpArray[8], rowCounter, 5);
                    cmrTable.getModel().setValueAt(tmpArray[4], rowCounter, 6);
                    rowCounter++;
                }
                else{
                    listSize-=1;
                }
            }
            if(listSize>0){
                cmrNumberLabel.setForeground(Color.blue);
                cmrNumberLabel.setText("Total Customers: " + listSize);
            }
            else{
                cmrNumberLabel.setForeground(Color.red);
                cmrNumberLabel.setText("No Customers");
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addUser(String name, String last, String expireDateStr, String email, String pass, String todayDateStr){
        try {
            pw.println("addusr");
            pw.println(name);
            pw.println(last);
            pw.println(expireDateStr);
            pw.println(email);
            pw.println(pass);
            pw.println(todayDateStr);
            String ans= br.readLine();
            if(!ans.equals("usrerror")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User Created: " + ans);
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            }
            else{
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User creation Error");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void editUsrExpire(String userId, String expireDateStr){
        try {
            pw.println("editusr");
            pw.println(userId);
            pw.println(expireDateStr);
            String ans= br.readLine();
            if(ans.equals("ackex")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User expire updated");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void editUsrMac(String usrId){
        try {
            pw.println("upmac");
            pw.println(usrId);
            String ans=br.readLine();
            if(ans.equals("ackmac")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("User MAC updated");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void history(String usrId){
        try {
            DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
            pw.println("history");
            pw.println(usrId);
            String listSizeStr= br.readLine();
            int listSize=0;
            if(listSizeStr.equals("nohist")){
                listSize=0;
            }
            else listSize= Integer.parseInt(listSizeStr);
            if(listSize>0){
                backupNumberLabel.setForeground(Color.blue);
                backupNumberLabel.setText("User: " + usrId + "   Backups: " + listSize);
            }
            else{
                backupNumberLabel.setForeground(Color.red);
                backupNumberLabel.setText("User: " + usrId + "   No Backup");
                while(model.getRowCount() > 0)
                {
                    model.removeRow(0);
                }
                for(int i=0; i<7; i++){
                    model.addRow(new Object[]{null, null, null});
                }
                return;
            }
            int rowNum= historyTable.getRowCount();
            if(listSize>rowNum){
                model.setRowCount(listSize);
            }
            List<String> listLines= new LinkedList<String>();
            String input= br.readLine();
            while(!input.equals("end")){
                listLines.add(input);
                input= br.readLine();
            }
            Iterator<String> itList= listLines.iterator();
            int rowCounter=0;
            while(itList.hasNext()){
                String tmpArray[]= itList.next().split(",");
                historyTable.getModel().setValueAt(tmpArray[0], rowCounter, 0);
                historyTable.getModel().setValueAt(tmpArray[1], rowCounter, 1);
                historyTable.getModel().setValueAt(tmpArray[2], rowCounter, 2);
                rowCounter++;
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void downloadUsers(){
        try {
            pw.println("usrlist");
            String ans= br.readLine();
            if(ans.equals("nousr")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("No Customers");
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                return;
            }
            List<String> inputLines= new LinkedList<String>();
            String lineInput= br.readLine();
            while(!lineInput.equals("end")){
                inputLines.add(lineInput);
                lineInput= br.readLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(RemoteConnThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/Remote/Icons/error-logo-50x50.png";
    String infoIcon="/Remote/Icons/info-icon-50x50.png";
    String connIcon= "/Remote/Icons/connect-icon-40x40.png";
    //***************ICONS DATABASE END*****************
}
