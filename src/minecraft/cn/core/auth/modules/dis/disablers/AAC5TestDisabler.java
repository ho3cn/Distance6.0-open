package cn.core.auth.modules.dis.disablers;


import cn.distance.api.events.Render.EventRender3D;
import cn.distance.api.events.World.*;
import cn.core.auth.modules.dis.DisablerModule;
import cn.distance.api.events.Render.EventRender2D;
import io.netty.buffer.Unpooled;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;


public class AAC5TestDisabler implements DisablerModule {

    @Override
    public void onEnabled() {

    }

    @Override
    public void onPacket( EventPacketSend event) {
    }

    @Override
    public void onPacket( EventPacketReceive event) {

    }

    @Override
    public void onPacket( EventPacket event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C03PacketPlayer && mc.thePlayer.ticksExisted % 15 == 0){
            try {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream _out = new DataOutputStream(b);
                _out.writeUTF(mc.thePlayer.getGameProfile().getName());
                PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
                buf.writeBytes(b.toByteArray());
                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("aac5:resetFlags", buf));
            } catch (IOException ignored){
            }
        }
    }

    @Override
    public void onUpdate( EventPreUpdate event) {
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onWorldChange( EventWorldChanged event) {

    }

    @Override
    public void onRender2d(EventRender2D event) {

    }

    @Override
    public void onMotionUpdate(EventMotionUpdate event) {

    }

    public void onRender3d( EventRender3D event ){

    }
}
