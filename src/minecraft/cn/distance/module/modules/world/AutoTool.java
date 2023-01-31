package cn.distance.module.modules.world;


import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventTick;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.world.BlockUtils;
import net.minecraft.util.BlockPos;

public class AutoTool extends Module {
	public AutoTool() {
		super("AutoTool", new String[] {"AutoTool"}, ModuleType.Player);
    }
	public Class type() {
        return EventPacketSend.class;
    }

	@EventHandler
	    public void onEvent( EventTick event) {
	        if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
	            return;
	        }
	        if (mc.objectMouseOver == null) {
	            return;
	        }
	        BlockPos pos = mc.objectMouseOver.getBlockPos();
	        if (pos == null) {
	            return;
	        }
	        BlockUtils.updateTool(pos);
	    }
	}
