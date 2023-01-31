package cn.core.auth.modules.speedmode.speed;

import cn.distance.api.events.World.*;
import cn.core.auth.modules.speedmode.SpeedModule;
import cn.distance.util.entity.MovementUtils;
import net.minecraft.potion.Potion;

public class HypixelLowHopSpeed extends SpeedModule {

    @Override
    public void onStep( EventStep e ) {

    }
    private double speed;
    @Override
    public void onPre( EventPreUpdate e ) {
        if (mc.thePlayer.onGround) {
            if (mc.thePlayer.isMoving()) {
                mc.thePlayer.motionY = 0.319f;
                speed = 1.10f;
                mc.timer.timerSpeed = 1.08f;
            } else {
                speed = 0f;
            }
        }
        MovementUtils.strafe((float) (getBaseMoveSpeed() * speed));
    }


    public double getBaseMoveSpeed() {
        double baseSpeed = 0.2603;
        if (mc.thePlayer.isPotionActive( Potion.moveSpeed))
            baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1);
        return baseSpeed;
    }

    @Override
    public void onMove( EventMove e ) {

    }

    @Override
    public void onPost( EventPostUpdate e ) {

    }

    @Override
    public void onEnabled( ) {

    }

    @Override
    public void onDisabled( ) {
        mc.timer.timerSpeed= 1.0F;
    }

    @Override
    public void onPacket( EventPacket e ) {

    }

    @Override
    public void onMotion( EventMotionUpdate e ) {

    }

    @Override
    public void onPacketSend( EventPacketSend e ) {

    }
}
