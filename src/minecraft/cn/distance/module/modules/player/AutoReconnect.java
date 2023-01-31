package cn.distance.module.modules.player;

import java.awt.Color;

import cn.distance.api.EventHandler;
import cn.distance.api.events.Misc.EventChat;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;

public class AutoReconnect
extends Module {
    private float old;

    public AutoReconnect() {
        super("AutoReconnect", new String[]{"AutoReconnect", "AutoReconnect", "AutoReconnect"}, ModuleType.Player);
        this.setColor(new Color(244, 255, 149).getRGB());
    }

    @EventHandler
    private void onChat(EventChat e) {
        if(e.getMessage().contains("Flying or related."))mc.thePlayer.sendChatMessage("/back");
    }

}

