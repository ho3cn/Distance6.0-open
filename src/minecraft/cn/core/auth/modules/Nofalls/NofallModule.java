package cn.core.auth.modules.Nofalls;

import cn.distance.api.events.World.EventMotionUpdate;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventPreUpdate;
import net.minecraft.client.Minecraft;

public interface NofallModule {
    Minecraft mc = Minecraft.getMinecraft();

    void onEnable();
    void onUpdate(EventPreUpdate e);
    void onPacketSend(EventPacketSend e);
    void onUpdateMotion(EventMotionUpdate e);
}
