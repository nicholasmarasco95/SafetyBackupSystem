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

![GitHub Logo](pictures/device_backup.png)

In "Device" user has a check panel, where he can manage settings for this backup type.

**Backup Folder**

The user can choose folders to backup. **Sync Device/Server Backup** is a feature that allows the software to use same file previously zipped to be managed by Server and Device backup.

![GitHub Logo](pictures/folder_backup.png)

**Folder Destination**
The user must choose folder destination. It could be in a external device or into PC hard drive.

![GitHub Logo](pictures/folder_destination.png)

**Storage Limit**
This feature prevents to overload device memory, by deleting oldests backups. User can set a fixed storage limit or **Autofree** feature, which will automatically calculate the available space to store max number of backup.

![GitHub Logo](pictures/storage_limit.png)
