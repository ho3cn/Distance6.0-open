package cn.distance.module.modules.world;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventTick;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;

import java.awt.Color;

public class FastPlace extends Module {
	public FastPlace() {
		super("FastPlace", new String[] { "fplace", "fc" }, ModuleType.World);
		this.setColor(new Color(226, 197, 78).getRGB());
	}

	@EventHandler
	private void onTick( EventTick e) {
		this.mc.rightClickDelayTimer = 0;
	}
}
