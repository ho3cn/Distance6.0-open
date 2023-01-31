
package cn.distance.module.modules.world;

import java.awt.Color;

import cn.distance.module.Module;
import cn.distance.module.ModuleType;


public class NoCommand
extends Module {
    public NoCommand() {
        super("NoCommand", new String[]{"No Command", "Commnand"}, ModuleType.World);
        this.setColor(new Color(223, 233, 233).getRGB());
    }
}
