package cn.core.auth.modules;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPacketSend;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.api.value.Mode;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.core.auth.modules.Nofalls.NofallModule;
import cn.core.auth.modules.Nofalls.impl.AAC5NoFall;
import cn.core.auth.modules.Nofalls.impl.HypixelNoFall;
import cn.core.auth.modules.Nofalls.impl.SpoofGroundNoFall;
import cn.core.auth.modules.Nofalls.impl.VulcanNoFall;
import cn.distance.module.modules.render.Freecam;

public class NoFall extends Module {
	public Mode mode = new Mode("Mode", "Mode", NoFallMode.values(), NoFallMode.SpoofGround);


	public NoFall() {
		super("NoFall", new String[] { "Nofalldamage" }, ModuleType.Player);
		super.addValues(mode);
	}

	@Override
	public void onEnable(){
		((NoFallMode)mode.getValue()).get().onEnable();
	}

	@EventHandler
	private void onUpdate(EventPreUpdate e) {
		super.setSuffix(mode.getValue());
		if (mc.thePlayer.capabilities.isFlying || mc.thePlayer.capabilities.disableDamage
				|| mc.thePlayer.motionY >= 0.0d)
			return;
		if ( Criticals.mode.getValue().equals(Criticals.CritMode.NoGround) && ModuleManager.getModuleByClass(Criticals.class).isEnabled()) {
			return;
		}
		if(ModuleManager.getModByClass( Freecam.class ).isEnabled()){return;}
		((NoFallMode)mode.getValue()).get().onUpdate(e);

	}

	@EventHandler
	public void onPacket(EventPacketSend e) {
		((NoFallMode)mode.getValue()).get().onPacketSend(e);
	}

	public enum NoFallMode {
		SpoofGround(new SpoofGroundNoFall()),
		AAC5(new AAC5NoFall()),
		Vulcan(new VulcanNoFall()),
		Hypixel(new HypixelNoFall());
		final NofallModule nofallModule;
		NoFallMode(NofallModule nofallModuleIn){
			nofallModule = nofallModuleIn;
		}

		public NofallModule get() {
			return nofallModule;
		}
	}
}
