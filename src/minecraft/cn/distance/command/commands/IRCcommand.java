package cn.distance.command.commands;
import cn.core.auth.GuiLogin;
import cn.distance.Client;
import cn.distance.command.Command;
import cn.distance.manager.ModuleManager;
import cn.core.auth.IRC.IRC;
import cn.distance.util.misc.Helper;


import net.minecraft.client.main.Main;
import net.minecraft.util.EnumChatFormatting;

import java.util.Objects;

public class IRCcommand
        extends Command {
    public IRCcommand() {
        super("IRC", new String[]{"c"}, "", "发送一个IRC消息");
    }

    @Override
    public String execute(String[] args) {
        String msg;
        if (args.length == 0) {
            Helper.sendMessageWithoutPrefix(EnumChatFormatting.GRAY +
                    "[" + EnumChatFormatting.GOLD + "IRC" +
                    EnumChatFormatting.GRAY +
                    "]" + EnumChatFormatting.WHITE + " .irc <text>");
            return null;
        } else {
            if ( Main.LastIRCMessage + 6000 <= System.currentTimeMillis() || Main.LastIRCMessage == 0) {
                String IIiI = null;
                boolean a = false;
                for (String s : args) {
                    if (!a) {
                        IIiI = s;
                        a = true;
                        continue;
                    }
                    IIiI = ( Object ) IIiI + " " + s;

                }
                msg = IIiI;
                if ( Objects.requireNonNull( ModuleManager.getModuleByClass( IRC.class)).isEnabled()) {
                    Main.LastIRCMessage = System.currentTimeMillis();
                    if(msg.contains( "@" )){
                        Helper.sendMessageWithoutPrefix( "[IRC]Cannot Send '@' In IRC!" );
                        return null;
                    }
                    if( IRC.sendIRCMessage( "MSG@"+ GuiLogin.username.getText() + "@" + EnumChatFormatting.WHITE + Client.name + "@" + msg)){
                        Helper.sendMessageWithoutPrefix( "[IRC]Send Success" );
                    }

                }
            } else {
                Helper.sendMessageWithoutPrefix(EnumChatFormatting.GRAY +
                        "[" + EnumChatFormatting.GOLD + "IRC" +
                        EnumChatFormatting.GRAY +
                        "]" + EnumChatFormatting.RED + " You need to wait " + ((Main.LastIRCMessage + 6000) - System.currentTimeMillis()) / 1000 + "s to send the next message");
                return null;
            }
        }

        return null;
    }
}





