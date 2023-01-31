package cn.core.auth.modules.speedmode.speed;

import cn.distance.api.events.World.*;
import cn.core.auth.modules.Speed;
import cn.core.auth.modules.speedmode.SpeedModule;
import cn.distance.util.entity.MoveUtils;
import cn.distance.util.time.MSTimer;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

import static cn.distance.util.entity.PlayerUtil.setSpeed;

public class AutoJumpSpeed extends SpeedModule {

    public double dmgspeed;
    public boolean shoulddamageboost=false;
    private final MSTimer timer = new MSTimer();

    @Override
    public void onStep( EventStep e) {

    }

    @Override
    public void onPre(EventPreUpdate e) {
        if(mc.thePlayer.isMoving()) {

            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }

            if (shoulddamageboost && !timer.hasTimePassed(500)) {
                setSpeed(Math.max(Speed.dmspeed.getValue().doubleValue() + (double) MoveUtils.getSpeedEffect() * 0.1, this.getBaseMoveSpeed()));
                mc.timer.timerSpeed = 1.0f;
                System.out.println("damageboost");
            } else {
                shoulddamageboost = false;
            }

        }

    }
    public double getBaseMoveSpeed() {
        return this.mc.thePlayer.isSprinting() ? 0.2873 : (double)0.223f;
    }

    @Override
    public void onMove( EventMove e) {

    }

    @Override
    public void onPost( EventPostUpdate e) {

    }

    @Override
    public void onEnabled() {

    }

    @Override
    public void onDisabled() {

    }

    @Override
    public void onMotion( EventMotionUpdate e) {

    }

    @Override
    public void onPacketSend( EventPacketSend e) {

    }
    @Override
    public void onPacket( EventPacket e ) {
        if((e.getPacket() instanceof S12PacketEntityVelocity &&( ( S12PacketEntityVelocity ) e.getPacket() ).getEntityID()==mc.thePlayer.getEntityId()) ){
            shoulddamageboost=true;
            timer.reset();
        }
        if(e.getPacket()instanceof S27PacketExplosion){


            S27PacketExplosion s27PacketExplosion = (S27PacketExplosion)e.getPacket();
            if (s27PacketExplosion.getAffectedBlockPositions().isEmpty()) {
                dmgspeed = Math.hypot(this.mc.thePlayer.motionX + (double)(s27PacketExplosion.func_149149_c() / 8500.0f), this.mc.thePlayer.motionZ + (double)(s27PacketExplosion.func_149147_e() / 8500.0f));
                shoulddamageboost = true;
                timer.reset();
            }
        }
    }
}
