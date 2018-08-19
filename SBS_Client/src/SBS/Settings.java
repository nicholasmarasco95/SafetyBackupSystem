/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SBS;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.prefs.*;

/**
 *
 * @author nicho
 */
public class Settings {
    
    Preferences setting= Preferences.userRoot();
    
    public void SaveSetting(String type, String key, String value){
        if(type.toLowerCase().equals("string")){
            setting.put(key, value);
        }
        if(type.toLowerCase().equals("bool") || type.toLowerCase().equals("boolean")){
            boolean bool = Boolean.parseBoolean(value.toLowerCase());
            setting.putBoolean(key, bool);
        }
        if(type.toLowerCase().equals("int") || type.toLowerCase().equals("integer")){
            if(value!=null){
                Integer intValue= Integer.parseInt(value);
                setting.putInt(key, intValue);
            }
        }
        if(type.toLowerCase().equals("long")){
            if(value!=null){
                Long longValue= Long.parseLong(value);
                setting.putLong(key, longValue);
            }
        }
    }
    
    public Class getClassType(){
        return setting.getClass();
    }
    
    public boolean getBoolValue(String key){
        return setting.getBoolean(key, false);
    }
    
    public String getStingValue(String key){
        return setting.get(key, null);
    }
    
    public Integer getIntValue(String key){
        return setting.getInt(key, -1);
    }
    
    public long getLongValue(String key){
        return setting.getLong(key, -1);
    }
    
}
