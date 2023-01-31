package cn.distance.module.modules.render;

import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.gui.GuiBaned;
import net.minecraft.client.multiplayer.WorldClient;

public class Baned extends Module {
    public Baned(){
        super("DistanceBan",new String[]{"ban"}, ModuleType.World);
    }
    @Override
    public void onEnable(){
        if (mc.theWorld != null){
            mc.theWorld.sendQuittingDisconnectingPacket();
            mc.loadWorld((WorldClient)null);
        }
        mc.displayGuiScreen(new GuiBaned());
    }
}
