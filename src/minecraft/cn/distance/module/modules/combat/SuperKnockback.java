package cn.distance.module.modules.combat;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventAttack;
import cn.distance.api.value.Mode;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.entity.MovementUtils;
import cn.distance.util.time.MSTimer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class SuperKnockback
extends Module {
    private final Mode mode = new Mode("Mode",Modes.values(),Modes.WTap);
    private final Numbers<Double> hurtTimeValue = new Numbers<>("HurtTime", 10d, 0d, 10d, 1d);
    private final Option onlyMoveValue = new Option("OnlyMove", false);
    private final Option onlyGroundValue = new Option("OnlyGround", false);
    private final Numbers<Double> delay = new Numbers<>("Delay", 0d, 0d, 500d, 1d);

    public SuperKnockback() {
        super("SuperKnockback", new String[]{"wtap"}, ModuleType.Combat);
        addValues(hurtTimeValue, onlyMoveValue, onlyGroundValue ,delay);
    }

    MSTimer timer = new MSTimer();

    @EventHandler
    private void onTick(EventAttack event) {
        if (event.getEntity() instanceof EntityLivingBase) {
            if (((EntityLivingBase) event.getEntity()).hurtTime > hurtTimeValue.get() || !timer.hasTimePassed(delay.get().longValue()) ||
                    (!MovementUtils.isMoving() && onlyMoveValue.get()) || (!mc.thePlayer.onGround && onlyGroundValue.get())) {
                return;
            }
            switch ((Modes) mode.getValue()) {
                case WTap:
                    if (mc.thePlayer.isSprinting()) {
                        mc.thePlayer.setSprinting(false);
                    }
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    mc.thePlayer.serverSprintState = true;
                    break;
                case Packet:
                    if (mc.thePlayer.isSprinting()) {
                        mc.thePlayer.setSprinting(true);
                    }
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    mc.thePlayer.serverSprintState = true;
                    break;
                case ExtraPacket:
                    if (mc.thePlayer.isSprinting()) {
                        mc.thePlayer.setSprinting(true);
                    }
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    mc.thePlayer.serverSprintState = true;
                    break;
            }
        }
    }
    enum Modes{
        ExtraPacket, WTap, Packet
    }
}

