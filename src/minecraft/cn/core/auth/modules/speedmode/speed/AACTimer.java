package cn.core.auth.modules.speedmode.speed;

import cn.distance.api.events.World.*;
import cn.core.auth.modules.speedmode.SpeedModule;
import cn.distance.util.entity.MovementUtils;
import cn.distance.util.time.TimerUtil;


public class AACTimer extends SpeedModule {
    public TimerUtil timer=new TimerUtil();
    @Override
    public void onStep( EventStep e) {

    }

    @Override
    public void onPre( EventPreUpdate e) {

    }

    @Override
    public void onMove(EventMove e) {
        if (MovementUtils.isMoving()) {
            if (stage == 1) {
                mc.timer.timerSpeed = 1.5F;
                if (timer.delay(700)) {
                    timer.reset();
                    stage = stage - 1;
                }
            } else {
                mc.timer.timerSpeed = 0.8F;
                if (timer.delay(400)) {
                    timer.reset();
                    stage = stage + 1;
                }
            }
        }
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
    public void onPacket( EventPacket e ) {

    }

    @Override
    public void onMotion( EventMotionUpdate e) {

    }

    @Override
    public void onPacketSend( EventPacketSend e) {

    }
}
