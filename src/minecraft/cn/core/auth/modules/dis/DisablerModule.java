package cn.core.auth.modules.dis;

import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.events.Render.EventRender3D;
import cn.distance.api.events.World.*;

import net.minecraft.client.Minecraft;

public interface DisablerModule {
    Minecraft mc = Minecraft.getMinecraft();
    void onDisable();
    void onEnabled();

    void onPacket( EventPacketSend event);

    void onPacket( EventPacketReceive event);

    void onPacket( EventPacket event);

    void onUpdate( EventPreUpdate event);

    void onWorldChange( EventWorldChanged event);
    void onRender2d(EventRender2D event);
    void onRender3d( EventRender3D event );
    void onMotionUpdate(EventMotionUpdate event);
}
