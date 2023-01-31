
package cn.distance.module.modules.player;

import java.awt.Color;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.entity.PlayerUtil;


public class Strafe
extends Module {

    public Strafe() {
        super("Strafe", new String[]{"Strafe"}, ModuleType.Movement);
        this.setColor(new Color(208, 30, 142).getRGB());
    }

    @EventHandler
    public void onUpdate(EventPreUpdate event) {
        if (PlayerUtil.MovementInput()) {
            PlayerUtil.setSpeed((double)PlayerUtil.getSpeed());
        }
    }
    }


