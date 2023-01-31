package cn.core.auth.modules.Nofalls.impl;

import cn.distance.api.events.World.EventMotionUpdate;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventPreUpdate;
import cn.core.auth.modules.Nofalls.NofallModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;

public class HypixelNoFall implements NofallModule {
    Minecraft mc=Minecraft.getMinecraft();

    @Override
    public void onEnable( ) {

    }

    @Override
    public void onUpdate( EventPreUpdate e){

        if(mc.thePlayer.onGround){
            mc.thePlayer.fallDistance=0.5f;
        }
        if(mc.thePlayer.fallDistance > 2) {
            mc.thePlayer.onGround = false;
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
        }
    }

    @Override
    public void onPacketSend( EventPacketSend e ) {
        if(e.getPacket() instanceof C03PacketPlayer){
            C03PacketPlayer playerPacket = (C03PacketPlayer ) e.packet;
            if (mc.thePlayer != null && mc.thePlayer.fallDistance > 1.5)
                playerPacket.onGround = mc.thePlayer.ticksExisted % 2 == 0;
        }
    }

    @Override
    public void onUpdateMotion( EventMotionUpdate e ) {

    }

}
