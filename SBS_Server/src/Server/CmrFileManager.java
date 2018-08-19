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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nicho
 */
public class CmrFileManager {
    
    private String fileName= "SBS-Customers-List.csv";
    private File file= new File(fileName);
    private PrintWriter pw;
    private BufferedReader br;
    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy");
    private Settings setting= new Settings();
    
    public CmrFileManager(){
        CreateFile(); //check and create file if doesn't exist
    }
    
    private void SemCheck(){
        while(setting.getBoolValue("writeFileSem")){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void SemLock(){
        while(setting.getBoolValue("writeFileSem")){
            //wait
        }
        setting.SaveSetting("bool", "writeFileSem", "true");
    }
    
    private void SemUnlock(){
        setting.SaveSetting("bool", "writeFileSem", "false");
    }
    
    private void CreateFile(){
        //check and create file if doesn't exist
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException ex) {
                System.out.println("File creation error\n");
            }
            try {
                pw= new PrintWriter(new FileOutputStream(file, true));
                br= new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                System.out.println("PrintWriter and/or BufferedReader creation error");
            }
            pw.write("User, Name, Last, Expiry date, Email, Password, First registration, Mac Address, Mac Count\n");
            try {
                br.close();
                pw.close();
            } catch (IOException ex) {
                System.out.println("Cannot close PrintWriter and/or BufferedReader");
            }
        }
    }
    
    private void OpenReadOnly(){
        try {
            br= new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            System.out.println("Cannot open BufferedReader");
        }
    }
    
    private void OpenWriteOnly(){
        try {
            pw= new PrintWriter(new FileOutputStream(file, true));
        } catch (FileNotFoundException ex) {
            System.out.println("Cannot open PrintWriter");
        }
    }
    
    private void OpenReadAndWrite(){
        try {
                pw= new PrintWriter(new FileOutputStream(file, true));
                br= new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException ex) {
                System.out.println("PrintWriter and/or BufferedReader creation error");
            }
    }
    
    private void CloseFileReadAndWrite(){
        try {
            pw.close();
            br.close();
        } catch (IOException ex) {
            System.out.println("Cannot close PrintWriter and/or BufferedReader");
        }
    }
    
    private void CloseFileRead(){
        try {
            br.close();
        } catch (IOException ex) {
            System.out.println("Cannot close BufferedReader");
        }
    }
    
    private void CloseFileWrite(){
        pw.close();
    }
    
    public String GetUserInfo(String id){
        SemCheck();
        String line;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                String lineToRet= line;
                int indx= line.indexOf(",");
                if(line.subSequence(0, indx).toString().equals(id)){
                    CloseFileRead();
                    return lineToRet;
                }
            }
        } catch (IOException ex) {
            System.out.println("Checking User Registration error");
        }
        CloseFileRead();
        return null;
    }
    
    public boolean CheckUserRegistration(String id){
        SemCheck();
        String line;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                int indx= line.indexOf(",");
                if(line.subSequence(0, indx).toString().equals(id)){
                    CloseFileRead();
                    return true;
                }
            }
        } catch (IOException ex) {
            System.out.println("Checking User Registration error");
        }
        CloseFileRead();
        return false;
    }
    
    public String GetExpireDate(String id){
        SemCheck();
        String line;
        String expireDateStr= null;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                int indx= line.indexOf(",");
                String[] lineArray= line.split(",");
                if(lineArray[0].equals(id)){
                    expireDateStr= lineArray[3].replaceAll("\\s", "");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return expireDateStr;
    }
    
    public String CheckUserExpire(String id, String todayDate){
        SemCheck();
        //if true NOT expired
        String line;
        String expireDateStr= null;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                int indx= line.indexOf(",");
                String[] lineArray= line.split(",");
                if(lineArray[0].equals(id)){
                    expireDateStr= lineArray[3].replaceAll("\\s", "");
                    try {
                        Date cmrDate= sdf.parse(todayDate);
                        Date expireDate= sdf.parse(expireDateStr);
                        if (cmrDate.before(expireDate) || cmrDate.equals(expireDate)){
                            CloseFileRead();
                            return expireDateStr;
                        }
                        if(cmrDate.after(expireDate)){
                            CloseFileRead();
                            return "ex";
                        }
                    } catch (ParseException ex) {
                        System.out.println("Cannot parse date: " + ex);
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            System.out.println("Checking User Registration error");
        }
        CloseFileRead();
        return "ex";
    }
    
    public boolean CheckUserPassword(String id, String pass){
        SemCheck();
        //if true NOT expired
        String line;
        String filePass= null;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                int indx= line.indexOf(",");
                String[] lineArray= line.split(",");
                if(lineArray[0].equals(id)){
                    filePass= lineArray[5].replaceAll("\\s", "");
                    if(filePass.equals(pass)){
                        CloseFileRead();
                        return true;
                    }
                    CloseFileRead();
                    return false;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        CloseFileRead();
        return false;
    }
    
    public boolean CheckUserMac(String id, String usrMac){
        SemCheck();
        String line;
        String fileMac= null;
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                int indx= line.indexOf(",");
                String[] lineArray= line.split(",");
                if(lineArray[0].equals(id)){
                    fileMac= lineArray[7].replaceAll("\\s", "");
                    if(fileMac.equals(usrMac)){
                        CloseFileRead();
                        return true;
                    }
                    int macCount= Integer.parseInt(lineArray[8].replaceAll("\\s", ""));
                    if(macCount<3){
                        CloseFileRead();
                        updateUserMac(id, usrMac);
                        return true;
                    }
                    CloseFileRead();
                    return false;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        CloseFileRead();
        return false;
    }
    
    private void updateUserMac(String userId, String usrMac){
        SemLock();
        try{
            BufferedReader bufRead= new BufferedReader(new FileReader(fileName));
                String bufLine;
                StringBuffer inputBuffer= new StringBuffer();
                while((bufLine=bufRead.readLine())!=null){
                    if(bufLine.subSequence(0, bufLine.indexOf(",")).toString().equals(userId)){
                        String newLine= bufLine;
                        String[] lineArray= newLine.split(",");
                        String oldMac= lineArray[7].replaceAll("\\s", "");
                        int oldCount= Integer.parseInt(lineArray[8].replaceAll("\\s", ""));
                        int newCount=oldCount+1;
                        newLine= newLine.replace(oldMac, usrMac);
                        newLine= newLine.replace(" " + Integer.toString(oldCount), " " + Integer.toString(newCount));
                        inputBuffer.append(newLine);
                        inputBuffer.append("\n");
                    }
                    else{
                        inputBuffer.append(bufLine);
                        inputBuffer.append("\n");
                    }
                }
                String inputBufferStr= inputBuffer.toString();
                bufRead.close();
                FileOutputStream fileOut= new FileOutputStream(fileName);
                fileOut.write(inputBufferStr.getBytes());
                fileOut.close();
        } catch (FileNotFoundException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        SemUnlock();
    }
    
    public int AddNewUser(String name, String last, String ExpiryDate, String Email, String Password, String FirstRegistration){
        SemLock();
        OpenReadAndWrite();
        String line;
        String lastIndxStr= "000";
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                if(line.length()>2){
                    lastIndxStr= line.subSequence(0, line.indexOf(",")).toString();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        int lastIndx= Integer.parseInt(lastIndxStr);
        int newIndex= lastIndx+1;
        String newIndexStr= String.format("%03d", newIndex);
        line= newIndexStr + ", " + name + ", " + last + ", " + ExpiryDate + ", " + Email + ", " + Password + ", " + FirstRegistration + ", " + "MAC" + ", " + "0" + "\n";
        pw.append(line);
        CloseFileReadAndWrite();
        SemUnlock();
        return newIndex;
    }
    
    public void resetUserMacCounter(String userId){
        SemLock();
        try{
            BufferedReader bufRead= new BufferedReader(new FileReader(fileName));
                String bufLine;
                StringBuffer inputBuffer= new StringBuffer();
                while((bufLine=bufRead.readLine())!=null){
                    if(bufLine.subSequence(0, bufLine.indexOf(",")).toString().equals(userId)){
                        String newLine= bufLine;
                        String[] lineArray= newLine.split(",");
                        String oldMac= lineArray[7].replaceAll("\\s", "");
                        newLine= newLine.replace(oldMac, "MAC");
                        newLine= newLine.replace(lineArray[8], " 0");
                        inputBuffer.append(newLine);
                        inputBuffer.append("\n");
                    }
                    else{
                        inputBuffer.append(bufLine);
                        inputBuffer.append("\n");
                    }
                }
                String inputBufferStr= inputBuffer.toString();
                bufRead.close();
                FileOutputStream fileOut= new FileOutputStream(fileName);
                fileOut.write(inputBufferStr.getBytes());
                fileOut.close();
        } catch (FileNotFoundException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        SemUnlock();
    }
    
    public void updateUserExpire(String userId, String newExpireDate){
        if(!CheckUserRegistration(userId)) return;
        SemLock();
        try{
            BufferedReader bufRead= new BufferedReader(new FileReader(fileName));
                String bufLine;
                StringBuffer inputBuffer= new StringBuffer();
                while((bufLine=bufRead.readLine())!=null){
                    if(bufLine.subSequence(0, bufLine.indexOf(",")).toString().equals(userId)){
                        String newLine= bufLine;
                        String[] lineArray= newLine.split(",");
                        String oldExpire= lineArray[3];
                        newLine= newLine.replace(oldExpire, " " + newExpireDate);
                        inputBuffer.append(newLine);
                        inputBuffer.append("\n");
                    }
                    else{
                        inputBuffer.append(bufLine);
                        inputBuffer.append("\n");
                    }
                }
                String inputBufferStr= inputBuffer.toString();
                bufRead.close();
                FileOutputStream fileOut= new FileOutputStream(fileName);
                fileOut.write(inputBufferStr.getBytes());
                fileOut.close();
        } catch (FileNotFoundException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        SemUnlock();
    }
    
    public void updateUserPass(String userId, String newPass){
        SemLock();
        if(!CheckUserRegistration(userId)) return;
        try{
            BufferedReader bufRead= new BufferedReader(new FileReader(fileName));
                String bufLine;
                StringBuffer inputBuffer= new StringBuffer();
                while((bufLine=bufRead.readLine())!=null){
                    if(bufLine.subSequence(0, bufLine.indexOf(",")).toString().equals(userId)){
                        String newLine= bufLine;
                        String[] lineArray= newLine.split(",");
                        String oldPass= lineArray[5];
                        newLine= newLine.replace(oldPass, " " + newPass);
                        inputBuffer.append(newLine);
                        inputBuffer.append("\n");
                    }
                    else{
                        inputBuffer.append(bufLine);
                        inputBuffer.append("\n");
                    }
                }
                String inputBufferStr= inputBuffer.toString();
                bufRead.close();
                FileOutputStream fileOut= new FileOutputStream(fileName);
                fileOut.write(inputBufferStr.getBytes());
                fileOut.close();
        } catch (FileNotFoundException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        SemUnlock();
    }
    
    public void updateUserEmail(String userId, String newEmail){
        SemLock();
        if(!CheckUserRegistration(userId)) return;
        try{
            BufferedReader bufRead= new BufferedReader(new FileReader(fileName));
                String bufLine;
                StringBuffer inputBuffer= new StringBuffer();
                while((bufLine=bufRead.readLine())!=null){
                    if(bufLine.subSequence(0, bufLine.indexOf(",")).toString().equals(userId)){
                        String newLine= bufLine;
                        String[] lineArray= newLine.split(",");
                        String oldEmail= lineArray[4];
                        newLine= newLine.replace(oldEmail, " " + newEmail);
                        inputBuffer.append(newLine);
                        inputBuffer.append("\n");
                    }
                    else{
                        inputBuffer.append(bufLine);
                        inputBuffer.append("\n");
                    }
                }
                String inputBufferStr= inputBuffer.toString();
                bufRead.close();
                FileOutputStream fileOut= new FileOutputStream(fileName);
                fileOut.write(inputBufferStr.getBytes());
                fileOut.close();
        } catch (FileNotFoundException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
                Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        SemUnlock();
    }
    
    public List<String> getList(){
        SemCheck();
        //return reversed list of lines (except first) in file
        String line;
        List<String> list= new LinkedList<String>();
        OpenReadOnly();
        try{
            br.readLine();  //throw header line
            while((line=br.readLine())!=null){
                if(line.length()>2){
                    list.add(line);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CmrFileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        CloseFileRead();
        return list;
    }
    
}
