package cn.core.auth.IRC;

import cn.core.auth.Auth;
import cn.core.auth.GuiLogin;
import cn.core.auth.HWIDUtils;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.gui.GuiBaned;
import cn.distance.util.misc.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ChatComponentText;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class IRC extends Module {
    public IRC( ) {
        super( "IRC", new String[]{ "IRC" }, ModuleType.World );
    }

    public BufferedReader reader;
    public static Socket socket;

    @Override
    public void onEnable( ) {
        new IRCThreadClass( ).start( );
        try {
            socket = new Socket( "localhost", 54131 );
        } catch ( IOException e ) {
            Helper.sendMessageWithoutPrefix( "[IRC]Failed To Connect to Server!" );
        }
    }

    public static PrintWriter pw;
    static InputStream in;
    static Minecraft mc = Minecraft.getMinecraft( );
    public static boolean isop = false;

    public static void IRCverify( ) {


        Minecraft.getMinecraft( ).thePlayer.addChatMessage( new ChatComponentText( "[IRC]Try To Connect IRC Server" ) );
        try {
            in=socket.getInputStream();
            pw = new PrintWriter(socket.getOutputStream());
            Minecraft.getMinecraft( ).thePlayer.addChatMessage( new ChatComponentText( "[IRC]Connect Success" ) );

        }catch ( Exception e){

            Helper.sendMessageWithoutPrefix( "[IRC]failed IRC due to exception:"+e.toString());
            e.printStackTrace();
            ModuleManager.getModByClass( IRC.class ).setEnabled( false );
        }
    }

    public static void handleInput( ) {
        byte[] data = new byte[ 1024 ];
        try {
            int len = in.read( data );
            String ircmessage = new String( data, 0, len );
            ircmessage = ircmessage.replaceAll( "\n", "" );
            ircmessage = ircmessage.replaceAll( "\r", "" );
            ircmessage = ircmessage.replaceAll( "\t", "" );
            if ( ircmessage.contains( "/ban" ) && ircmessage.contains( GuiLogin.username.getText( ) ) ) {
                mc.currentScreen = new GuiBaned( );
            }
            if ( ircmessage.contains( "/kick" ) && ircmessage.contains( GuiLogin.username.getText( ) ) ) {
                System.out.println( "You Got Kicked By Admin!" );
                System.exit( 12 );
            }
        } catch ( IOException e ) {
            e.printStackTrace( );
        }
    }

    public static boolean sendIRCMessage( String msg ) {
        if ( !isop && ( msg.contains( "/ban" ) || msg.contains( "/kick" )  ) ) {
            return false;
        }
        if ( msg.contains( "/close" ) ) {
            pw.println( msg );

            ModuleManager.getModuleByClass( IRC.class ).setEnabled( false );
            return true;
        }
        pw.println( msg );
        pw.flush();
        return true;
    }
}
