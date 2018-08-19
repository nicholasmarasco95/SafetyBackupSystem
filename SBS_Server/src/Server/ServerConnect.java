/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.awt.Font;
import static java.awt.image.ImageObserver.HEIGHT;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author nicho
 */
public class ServerConnect implements Runnable{
    
    private ServerSocket connServSock;
    private ServerSocket fileSrvSock;
    private ServerSocket remoteSrvSock;
    private int connPort;
    private int filePort;
    private boolean auto;  //set if server is started automatically and not by GUI
    private Socket connSock;
    private ServerThread srvThr;
    private JLabel activeConnLabel;
    private int activeConnCount=0;
    private Settings setting= new Settings();
    
    public ServerConnect(int connPort, int filePort, boolean auto, JLabel activeConnLabel){
        this.connPort= connPort;
        this.filePort= filePort;
        this.auto= auto;
        this.activeConnLabel= activeConnLabel;
    }
    
    public void run(){
        try {
            connServSock= new ServerSocket(connPort);
            fileSrvSock= new ServerSocket(filePort);
        } catch (IOException ex) {
            if(!setting.getBoolValue("serverAutoOnline")){
                JLabel msgLabel= new JLabel();
                msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                msgLabel.setText("Cannot start Server: " + ex);
                JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
                System.exit(1);
            }
        }
        if(!auto){
            if(!setting.getBoolValue("srvRestartCheck")){
                if(!setting.getBoolValue("autoStartServer")){
                    JLabel msgLabel= new JLabel();
                    msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
                    msgLabel.setText("Server Online");
                    JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(infoIcon)));
                }
            }
        }
        
        while(true){
            try {
                connSock= connServSock.accept();
                addActiveConnLabel();
            } catch (IOException ex) {
                break;
            }
            srvThr= new ServerThread(connSock, fileSrvSock, activeConnLabel);
            Thread thr= new Thread(srvThr);
            thr.start();
        } 
    }
    
    public void closeScoket(){
        try {
            connSock.close();
            connServSock.close();
            fileSrvSock.close();
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Server Offline");
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(offlineIcon)));
        } catch (IOException ex) {
            JLabel msgLabel= new JLabel();
            msgLabel.setFont(new Font ("Microsoft tai le", Font.BOLD, 16));
            msgLabel.setText("Cannot Disconnect Server: " + ex);
            JOptionPane.showMessageDialog(null, msgLabel, null, HEIGHT, new ImageIcon(getClass().getResource(errorIcon)));
            System.exit(1);
        }
    }
    
    private void addActiveConnLabel(){
        String tmpStr= activeConnLabel.getText();
        int activeConn= Integer.parseInt(tmpStr);
        activeConn+=1;
        activeConnLabel.setText(Integer.toString(activeConn));
    }
    
    
    //***************ICONS DATABASE START***************
    String errorIcon= "/Server/Icons/error-logo-50x50.png";
    String infoIcon="/Server/Icons/info-icon-50x50.png";
    String offlineIcon="/Server/Icons/disconnect_icon-40x40.png";
    //***************ICONS DATABASE END*****************
    
}
