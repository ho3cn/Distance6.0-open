
package cn.distance.module.modules.combat;

import cn.core.auth.modules.KillAura;
import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.entity.PlayerUtil;
import cn.distance.util.time.TimeHelper;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Random;


public class AutoClicker extends Module {

    public TimeHelper time = new TimeHelper();
    public static Numbers<Double> cpsmin = new Numbers<>("CPSMin", "CPSMin", 8.0, 2.0, 20.0, 1.0);
    public static Numbers<Double> cpsmax = new Numbers<>("CPSMax", "CPSMax", 8.0, 2.0, 20.0, 1.0);
    protected Random r = new Random();
    public Option ab = new Option("BlockHit", "BlockHit", true);
    public Option BreakBlock = new Option("BreakBlock", "BreakBlock", true);
    public Option InvClicker = new Option("Inventory", "Inventory", false);
    public static int Click;
    public static boolean Clicked;
    private double delay = 0;
    private final TimeHelper time2 = new TimeHelper();
    private final TimeHelper time3 = new TimeHelper();

    public AutoClicker() {
        super("AutoClicker", new String[]{"AutoClicker"}, ModuleType.Combat);
        this.setColor(new Color(208, 30, 142).getRGB());
        super.addValues(cpsmin, cpsmax, ab, InvClicker, BreakBlock);
    }

    private void delay() {
        float minCps = cpsmin.getValue().floatValue();
        float maxCps = cpsmax.getValue().floatValue();
        float minDelay = 10.0f / minCps;
        float maxDelay = 10.0f / maxCps;
        this.delay = (double) maxDelay + this.r.nextDouble() * (double) (minDelay - maxDelay);
    }


    @EventHandler
    private void onUpdate(EventPreUpdate event) {
        BlockPos bp = mc.thePlayer.rayTrace(6.0, 0.0f).getBlockPos();
        boolean isblock = isblock = mc.theWorld.getBlockState(bp).getBlock() != Blocks.air && mc.objectMouseOver.typeOfHit != MovingObjectType.ENTITY;
        if (!BreakBlock.getValue()) isblock = false;
        if (this.time2.delay(this.delay) && !this.time2.delay(this.delay + this.delay / 2)) Clicked = true;
        if (this.time2.delay(this.delay + this.delay - 1.0)) {
            Clicked = false;
            time2.reset();
        }

        if (!ModuleManager.getModuleByClass( KillAura.class).isEnabled() && Mouse.isButtonDown(0) && this.time.delay(this.delay) && mc.currentScreen == null && !isblock) {
            PlayerUtil.blockHit(mc.objectMouseOver.entityHit, this.ab.getValue());
            mc.leftClickCounter = 0;
            mc.clickMouse();
            this.delay();
            this.time.reset();
        }
    }

    @EventHandler
    private void invClicks(EventPreUpdate event) {
        if (!Keyboard.isKeyDown(42)) {
            return;
        }
        if (mc.currentScreen instanceof GuiContainer && this.InvClicker.getValue()) {
            float invClickDelay = 1000.0f / cpsmax.getValue().floatValue() + (float) this.r.nextInt(50);
            if (Mouse.isButtonDown(0) && this.time3.delay(invClickDelay)) {
                try {
                    mc.currentScreen.InventoryClicks();
                    this.time3.reset();
                } catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }
}


