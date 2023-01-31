package cn.core.auth.modules.Nofalls.impl;

import cn.core.auth.modules.Nofalls.NofallModule;
import cn.distance.api.events.World.EventMotionUpdate;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventPreUpdate;

public class SpoofGroundNoFall implements NofallModule {
    @Override
    public void onEnable() {

    }

    @Override
    public void onUpdate(EventPreUpdate e) {
        if (mc.thePlayer.fallDistance > 2.5) {
            e.setOnground(true);
        }
    }

    @Override
    public void onPacketSend(EventPacketSend e) {

    }

    @Override
    public void onUpdateMotion(EventMotionUpdate e) {

    }
}
