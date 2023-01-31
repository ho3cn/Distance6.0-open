package cn.distance.module.modules.world;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPacketReceive;
import cn.distance.api.events.World.EventTick;
import cn.distance.api.value.Numbers;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import net.minecraft.network.play.server.S03PacketTimeUpdate;

import java.awt.*;

public class WorldTime
extends Module {
	private Numbers<Double> Time = new Numbers<>("Time", "Time", 18000.0, 0.0, 24000.0, 1.0);
    public WorldTime() {
        super("WorldTime", new String[]{"WorldTime", "WorldTime"}, ModuleType.World);
        this.setColor(new Color(198, 253, 191).getRGB());
        super.addValues(Time);
        
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }
	@EventHandler
	public void EventPacketSend(EventPacketReceive e) {
		if (e.getPacket() instanceof S03PacketTimeUpdate) {
			e.setCancelled(true);
		}
	}
    @EventHandler
    public void onTick(EventTick event) {
    	mc.theWorld.setWorldTime(Time.getValue().longValue());
    }



}

