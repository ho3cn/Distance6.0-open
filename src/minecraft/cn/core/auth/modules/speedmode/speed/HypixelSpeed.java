package cn.core.auth.modules.speedmode.speed;

import cn.distance.api.events.World.*;
import cn.core.auth.modules.speedmode.SpeedModule;

import cn.core.auth.modules.Speed;
import cn.distance.util.entity.MoveUtils;
import cn.distance.util.time.MSTimer;

import static cn.distance.util.entity.PlayerUtil.setSpeed;
import static net.minecraft.util.MathHelper.abs;

public class HypixelSpeed extends SpeedModule {
    private boolean stage = false;
    private final MSTimer timer = new MSTimer();
    public boolean shoulddamageboost=false;

    public double dmgspeed;

    @Override
    public void onMotion( EventMotionUpdate e) {
        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isInWeb()) return;
        if(mc.thePlayer.isMoving()){
            if (mc.thePlayer.onGround) {

//              Speed

//              MovementUtils.strafe(0.32f);

//              Jump
                mc.thePlayer.jump();

                if(!Speed.fullstrafe.get()) {
                    setSpeed(Speed.basespeed.get().floatValue() + MoveUtils.getSpeedEffect() * 0.1);
                }

            }

            if(shoulddamageboost&&!timer.hasTimePassed( Speed.dmgboostmstimer.get().longValue())&&!mc.thePlayer.isBurning()){
                setSpeed(Math.max(Speed.dmspeed.getValue().doubleValue() + (double)MoveUtils.getSpeedEffect() * 0.1, this.getBaseMoveSpeed() ));
                mc.timer.timerSpeed = 1.0f;
            }else {
                if (Speed.fullstrafe.get()) {
                    setSpeed(Speed.basespeed.get().floatValue() + MoveUtils.getSpeedEffect() * 0.1);
                }
                shoulddamageboost = false;
            }
        }
    }
    public double getBaseMoveSpeed() {
        return this.mc.thePlayer.isSprinting() ? 0.2873 : (double)0.223f;
    }


    @Override
    public void onPacketSend( EventPacketSend e) {
    }

    @Override
    public void onStep( EventStep e) {
    }

    @Override
    public void onPre(EventPreUpdate e) {
        if(mc.thePlayer.hurtTime>3){
            shoulddamageboost=true;
            timer.reset();
        }
    }



    double y;
    @Override
    public void onMove( EventMove event) {

    }

    @Override
    public void onPost( EventPostUpdate e) {
    }

    @Override
    public void onEnabled() {
        timer.reset();
    }



    @Override
    public void onDisabled() {
        timer.reset();
        mc.timer.timerSpeed = 1F;
    }

    @Override
    public void onPacket( EventPacket e ) {



    }
}
