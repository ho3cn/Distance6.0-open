package cn.distance.module.modules.move;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.api.value.Numbers;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.module.modules.player.Blink;
import cn.distance.util.time.TimerUtil;

public class HytJump extends Module {
    public static Numbers<Double> motionY = new Numbers<>("MotionY", 2d, 1d, 8d, 1d);
    public static Numbers<Double> DisableDelay = new Numbers<>("DisableDelay", 300d, 0d, 1000d, 50d);
    public static TimerUtil timer = new TimerUtil();
    public HytJump(){
        super("HytJump",new String[]{"hytjump"}, ModuleType.Movement);
        addValues(motionY,DisableDelay);
    }
    @Override
    public void onEnable(){
        timer.reset();
        if (!ModuleManager.getModuleByClass( Blink.class).isEnabled())ModuleManager.getModuleByClass(Blink.class).setEnabled(true);
        mc.timer.timerSpeed = 2.0f;
        mc.thePlayer.motionY = motionY.getValue();
    }
    @EventHandler
    public void onUpdate( EventPreUpdate e){
        if(timer.hasReached(DisableDelay.getValue())){
            setEnabled(false);
        }
    }
    @Override
    public void onDisable(){
        mc.timer.timerSpeed = 1.0f;
        ModuleManager.getModuleByClass(Blink.class).setEnabled(false);
    }

}
