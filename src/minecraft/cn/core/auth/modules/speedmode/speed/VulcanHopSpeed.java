package cn.core.auth.modules.speedmode.speed;

import cn.distance.api.events.World.*;
import cn.core.auth.modules.KillAura;
import cn.core.auth.modules.speedmode.SpeedModule;

import cn.distance.util.entity.MovementUtils;

public class VulcanHopSpeed extends SpeedModule {
    @Override
    public void onStep( EventStep e) {

    }

    @Override
    public void onPre( EventPreUpdate e) {

    }

    @Override
    public void onMove( EventMove e) {

    }

    @Override
    public void onPost(EventPostUpdate e) {

    }

    @Override
    public void onEnabled() {

    }

    @Override
    public void onDisabled() {

    }

    @Override
    public void onPacket( EventPacket e ) {

    }

    @Override
    public void onMotion( EventMotionUpdate e) {
        if ( KillAura.currentTarget != null && KillAura.currentTarget.hurtTime > 0) {
            if (!MovementUtils.isMoving() || mc.thePlayer.movementInput.jump) {
                return;
            }
            if (mc.thePlayer.onGround) {
                MovementUtils.strafe(0.9f);
                mc.thePlayer.motionY = 0.2;
            }
        }
    }

    @Override
    public void onPacketSend( EventPacketSend e) {

    }
}
