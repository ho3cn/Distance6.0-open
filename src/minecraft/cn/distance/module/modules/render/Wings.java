package cn.distance.module.modules.render;

import java.awt.Color;

import cn.distance.api.EventHandler;
import cn.distance.api.events.Render.EventRender3D;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.render.RenderWings;

public class Wings
extends Module {
	//public static Option<Boolean> Rainbow = new Option<Boolean>("Rainbow", "Rainbow", false);
    public Wings() {
        super("Wings", new String[]{"Wings"}, ModuleType.Render);
        this.setColor(new Color(208, 30, 142).getRGB());
        //super.addValues(Rainbow);
    }
    @EventHandler
    public void onRenderPlayer(EventRender3D event) {
        RenderWings renderWings = new RenderWings();
        renderWings.renderWings(event.getPartialTicks());
}

    }


