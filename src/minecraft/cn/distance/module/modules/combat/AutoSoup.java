package cn.distance.module.modules.combat;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.time.Timer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

/**
 * @author cool1
 */
public class AutoSoup extends Module {

    Timer timer = new Timer();
    public static Numbers<Double> DELAY = new Numbers<>("Delay", 350d, 100d, 1000d, 50d);
    public static Numbers<Double> HEALTH = new Numbers<>("Health", 3d, 20d, 1d, 1d);
    public static Option DROP = new Option("Drop", true);

    public AutoSoup() {
        super("AutoSoup",new String[]{"autosoup"}, ModuleType.Combat);
        addValues(DELAY,HEALTH,DROP);
    }

    @EventHandler
    public void onEvent(EventPreUpdate event) {
        int soupSlot = getSoupFromInventory();
        if (soupSlot != -1 && mc.thePlayer.getHealth() < (HEALTH.getValue().floatValue())
                        && timer.delay(DELAY.getValue().floatValue())) {
            swap(getSoupFromInventory(), 6);
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(6));
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
            mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    protected void swap(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }
    public static int getSoupFromInventory() {
        Minecraft mc = Minecraft.getMinecraft();
        int soup = -1;
        for (int i = 1; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();
                if (Item.getIdFromItem(item) == 282) {
                    soup = i;
                }
            }
        }
        return soup;
    }
}
