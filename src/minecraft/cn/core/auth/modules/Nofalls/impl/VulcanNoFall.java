package cn.core.auth.modules.Nofalls.impl;

import cn.core.auth.modules.Nofalls.NofallModule;
import cn.distance.api.events.World.EventMotionUpdate;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.util.entity.MovementUtils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VulcanNoFall implements NofallModule {
    private boolean nextSpoof = false;
    private boolean doSpoof = false;
    @Override
    public void onEnable() {

    }

    @Override
    public void onUpdate(EventPreUpdate e) {

    }

    @Override
    public void onPacketSend(EventPacketSend e) {
        if (e.packet instanceof C03PacketPlayer) {
            C03PacketPlayer packet = (C03PacketPlayer) e.packet;
            if (doSpoof) {
                packet.onGround = true;
                doSpoof = false;
                packet.y = Math.round(mc.thePlayer.posY * 2) / 2d;
                mc.thePlayer.setPosition(mc.thePlayer.posX, packet.y, mc.thePlayer.posZ);
            }
        }
    }

    @Override
    public void onUpdateMotion(EventMotionUpdate e) {
        if(nextSpoof) {
            mc.thePlayer.motionY = -0.1;
            MovementUtils.strafe(0.343f);
            nextSpoof = false;
        }
        if(mc.thePlayer.fallDistance > 3.65) {
            mc.thePlayer.fallDistance = 0.0f;
            doSpoof = true;
            nextSpoof = true;
        }
    }
}
