package cn.core.auth.IRC;

import cn.core.auth.Auth;
import cn.core.auth.GuiLogin;
import cn.distance.manager.ModuleManager;
import cn.distance.util.misc.Helper;

import javax.swing.*;


public class IRCThreadClass extends Thread{

    @Override
    public void run(){
        if(!GuiLogin.logined){
            while(true){
                System.out.println( "破解你老妈子?" );
                javax.swing.JOptionPane.showMessageDialog( null,"破解你老妈子?","操你妈你个傻逼", JOptionPane.ERROR_MESSAGE );
            }
        }
        if(!GuiLogin.Passed){
            System.out.println( "破解你老妈子?" );
            while(true){
                javax.swing.JOptionPane.showMessageDialog( null,"破解你老妈子?","操你妈你个傻逼", JOptionPane.ERROR_MESSAGE );
            }
        }
        IRC.IRCverify();
        while(true){
            IRC.handleInput();
            if(!ModuleManager.getModByClass(IRC.class).isEnabled()){
                Helper.sendMessageWithoutPrefix("[IRC]Disconnect From IRC Server Due To IRC Disabled");
                break;
            }
        }
    }
}
