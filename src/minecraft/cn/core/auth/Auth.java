package cn.core.auth;

import cn.core.auth.IRC.IRC;
import cn.distance.manager.ModuleManager;
import cn.distance.util.misc.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.Sys;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

/**
 * Code by MiLiBlue, At 2022/11/12
 **/

public class Auth {
    public static Auth getInstance() {
        return new Auth();
    }
    public static String[] ops={ "TIQS","BestLaoLiu"};

    public Socket socket;
    DesUtils desUtils= new DesUtils( "TIQSGOD" );
    {
        try {
            socket = new Socket("153.36.240.12", 54130);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * Login your account.
     *
     * @param uname
     * @param upasswd
     */

    public boolean loginAccount(String uname, String upasswd, String hwid) throws IOException {
        boolean help = false;

        try {
            // 要发送给服务器的信息
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write( desUtils.encrypt( "Check|" + uname + ":" + upasswd + ":" + hwid ));
            pw.flush();

            socket.shutdownOutput();

            // 从服务器接收的信息
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;
            String info1 = null;
            while((info1 = br.readLine())!=null){
                info = desUtils.decrypt( info1 );
            }
            if(!Objects.requireNonNull( info ).contains("Login Done.")){
                help = false;
            }else if(info.contains("Login Done.")){
                help = true;
            }

            br.close();
            is.close();
            os.close();
            pw.close();
            socket.close();
        } catch (Exception e) {
            System.out.println( "Failed To Login Because Of Errors!Please Contact The Admin!" );
        }
        return help;
    }


    /**
     * Register a account by key.
     * @param uname
     * @param upasswd
     * @param key
     * @param hwid
     * @return
     */

    public boolean registerAccount(String uname, String upasswd, String key, String hwid){
        boolean help = false;
        try {
            // 要发送给服务器的信息
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write(desUtils.decrypt(  "Reg|"+uname+":"+upasswd+":"+hwid +":"+key));
            pw.flush();

            socket.shutdownOutput();

            // 从服务器接收的信息
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String info = null;
            String info1 = "rat";
            while((info1 = br.readLine())!=null){
                info = desUtils.decrypt( info1 );
            }
            if(info.contains("Reg Done.")){
                help = true;
            }else {
                help = false;
            }

            br.close();
            is.close();
            os.close();
            pw.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf(String.valueOf(help));
        return help;
    }

}
