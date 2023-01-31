
package cn.distance.module.modules.combat;

import java.awt.Color;

import cn.distance.api.value.Numbers;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;


public class HitBox
extends Module {
	public static Numbers<Double> Size = new Numbers<>("Size", "Size", 0.0, 0.0, 5.0, 0.1);
    public HitBox() {
        super("HitBox", new String[]{"HitBox"}, ModuleType.Combat);
        this.setColor(new Color(208, 30, 142).getRGB());
        super.addValues(Size);
    }
}


