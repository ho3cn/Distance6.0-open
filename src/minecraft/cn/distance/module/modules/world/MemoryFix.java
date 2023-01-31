package cn.distance.module.modules.world;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventTick;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.time.TimerUtil;

public class MemoryFix extends Module {
    private final TimerUtil mftimer = new TimerUtil();

    public MemoryFix() {
        super("MemoryFix", new String[]{"memoryfix"}, ModuleType.World);
    }

    @Override
    public void onEnable() {
        Runtime.getRuntime().gc();
        mftimer.reset();
    }

    @EventHandler
    public void onTick(EventTick e) {
        double mflimit = 10.0;
        if(mftimer.hasReached(120000) && mflimit <= ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * 100f / Runtime.getRuntime().maxMemory())) {
            Runtime.getRuntime().gc();
            mftimer.reset();
        }
    }
}
