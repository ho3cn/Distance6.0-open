/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package cn.distance.module.modules.move;


import cn.distance.api.EventHandler;
import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.events.World.*;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.font.FontLoaders;
import cn.distance.ui.notifications.user.Notifications;
import cn.distance.util.PacketUtil;
import cn.distance.util.entity.MoveUtils;
import cn.distance.util.entity.MovementUtils;
import cn.distance.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.awt.*;

public class BowJump extends Module {

    private final Numbers<Double> boostValue = new Numbers<Double>("Boost", 4.25, 0.0, 10.0, 0.01);
    private final Numbers<Double> heightValue = new Numbers<Double>("Height", 0.42, 0.0, 10.0, 0.1);
    private final Numbers<Double> timerValue = new Numbers<Double>("Timer", 1.0, 0.1, 10.0, 0.1);
    private final Numbers<Double> delayBeforeLaunch = new Numbers<Double>("DelayBeforeArrowLaunch", 1.0, 1.0, 20.0, 1.0);

    private final Option autoDisable = new Option("AutoDisable", true);
    private final Option renderValue = new Option("RenderStatus", true);

    public BowJump() {
        super("BowJump", new String[]{"Use Bow to longjump"}, ModuleType.Movement);
        this.addValues(boostValue, heightValue, timerValue, delayBeforeLaunch, autoDisable, renderValue);
    }

    private int bowState = 0;
    private long lastPlayerTick = 0;

    private int lastSlot = -1;

    public void onEnable() {
        if (mc.thePlayer == null) return;
        bowState = 0;
        lastPlayerTick = -1;
        lastSlot = mc.thePlayer.inventory.currentItem;

        MoveUtils.strafe( 0 );
    }

    @EventHandler
    public void onMove( EventMove event) {
        if (mc.thePlayer.onGround && bowState < 3){
            MovementUtils.setSpeed(event,0  );

        }

    }

    @EventHandler
    public void onPacket( EventPacket event) {
        if (event.getPacket() instanceof C09PacketHeldItemChange) {
            C09PacketHeldItemChange c09 = (C09PacketHeldItemChange) event.getPacket();
            lastSlot = c09.getSlotId();
            event.setCancelled(true);
        }

        if (event.getPacket() instanceof C03PacketPlayer) {
            C03PacketPlayer c03 = (C03PacketPlayer) event.getPacket();
            if (bowState < 3) c03.setMoving(false);
        }
    }

    @EventHandler
    public void onUpdate( EventMotionUpdate event) {
        mc.timer.timerSpeed = 1F;

        boolean forceDisable = false;
        switch (bowState) {
            case 0:
                int slot = getBowSlot();
                if (slot < 0 || !mc.thePlayer.inventory.hasItem(Items.arrow)) {
                    Notifications.getManager().post("BowJump", "No arrows or bow found in your inventory!");
                    forceDisable = true;
                    bowState = 5;
                    break; // nothing to shoot
                } else if (lastPlayerTick == -1) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack();

                    if (lastSlot != slot)
                        PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255, mc.thePlayer.inventoryContainer.getSlot(slot + 36).getStack(), 0, 0, 0));

                    lastPlayerTick = mc.thePlayer.ticksExisted;
                    bowState = 1;
                }
                break;
            case 1:
                int reSlot = getBowSlot();
                if (mc.thePlayer.ticksExisted - lastPlayerTick > delayBeforeLaunch.get()) {
                    PacketUtil.sendPacketNoEvent(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, -90, mc.thePlayer.onGround));
                    PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));

                    if (lastSlot != reSlot) PacketUtil.sendPacketNoEvent(new C09PacketHeldItemChange(lastSlot));
                    bowState = 2;
                }
                break;
            case 2:
                if (mc.thePlayer.hurtTime > 0)
                    bowState = 3;
                break;
            case 3:
                MovementUtils.strafe(boostValue.get().floatValue());
                mc.thePlayer.motionY = heightValue.get();
                bowState = 4;
                lastPlayerTick = mc.thePlayer.ticksExisted;
                break;
            case 4:
                mc.timer.timerSpeed = timerValue.get().floatValue();
                if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted - lastPlayerTick >= 1)
                    bowState = 5;
                break;
        }

        if (bowState < 3) {
            mc.thePlayer.movementInput.moveForward = 0F;
            mc.thePlayer.movementInput.moveStrafe = 0F;
        }

        if (bowState == 5 && (autoDisable.get() || forceDisable))
            this.setEnabled(false);
    }

    @EventHandler
    public void onWorld( EventWorldChanged event) {
        this.setEnabled(false); //prevent weird things
    }

    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;
        mc.thePlayer.speedInAir = 0.02F;
    }

    private int getBowSlot() {
        for (int i = 36; i < 45; ++i) {
            ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null && stack.getItem() instanceof ItemBow) {
                return i - 36;
            }
        }
        return -1;
    }

    @EventHandler
    public void onRender2D(final EventRender2D event) {
        if (!renderValue.get()) return;
        ScaledResolution scaledRes = new ScaledResolution(mc);

        float width = (float) bowState / 5F * 60F;

        FontLoaders.GoogleSans20.drawCenteredString(getBowStatus(), scaledRes.getScaledWidth() / 2F, scaledRes.getScaledHeight() / 2F + 14F, -1);
        RenderUtil.drawRect(scaledRes.getScaledWidth() / 2F - 31F, scaledRes.getScaledHeight() / 2F + 25F, scaledRes.getScaledWidth() / 2F + 31F, scaledRes.getScaledHeight() / 2F + 29F, 0xA0000000);
        RenderUtil.drawRect(scaledRes.getScaledWidth() / 2F - 30F, scaledRes.getScaledHeight() / 2F + 26F, scaledRes.getScaledWidth() / 2F - 30F + width, scaledRes.getScaledHeight() / 2F + 28F, getStatusColor().getRGB());

    }

    public String getBowStatus() {
        switch (bowState) {
            case 0:
                return "Idle...";
            case 1:
                return "Preparing...";
            case 2:
                return "Waiting for damage...";
            case 3:
            case 4:
                return "Boost!";
            default:
                return "Task completed.";
        }
    }

    public Color getStatusColor() {
        switch (bowState) {
            case 0:
                return new Color(21, 21, 21);
            case 1:
                return new Color(48, 48, 48);
            case 2:
                return Color.yellow;
            case 3:
            case 4:
                return Color.green;
            default:
                return new Color(0, 111, 255);
        }
    }
}
