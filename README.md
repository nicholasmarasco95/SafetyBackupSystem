# Safety Backup System

**Safety Backup System** is a project complete of Client, Server and Remote Access software, developed to improve security of users. It's extremely useful to prevent data loss in case of cyberattack or hardware malfunction. SBS can backup computer files and send them to a remote server or store them in an external device.


# Client
This software runs in all Java-supported platforms, however it has some features that work in Windows environment only. The Client will copy selected files on a temporary folder, it'll zip them and, depending on what user had choosen, SBS will send the file to Server or Store it to another destination.


## Client Instructions
![GitHub Logo](pictures/home.png)
### First run
When a user run for the first time SBS, he must check if  **Server Info** is correctly set. It is a URL where the software will take the crypted information to parse (IP and ports).
![GitHub Logo](pictures/server_info.png)
**Login**
Once "Server Info" is set, the user must login to be recognized by server. He must click on **User Info** and then enter User Code and Password (User must be registered into server database).
![GitHub Logo](pictures/user_info.png)
### Device Backup
In Device Panel the user had a summary of backup status, and he can manage settings for this backup type.
**Backup Folder**
The user can choose the folders to backup.
