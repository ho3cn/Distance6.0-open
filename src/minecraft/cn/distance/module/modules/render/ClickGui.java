package cn.distance.module.modules.render;

import cn.distance.api.value.Mode;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.gui.clikguis.ClickUi.ClickUi;
import cn.distance.util.sound.SoundFxPlayer;
import cn.distance.ui.gui.clikguis.clickgui3.ClientClickGui;

public class ClickGui extends Module {
	private static final Mode mode = new Mode("Mode", "mode", ClickGui.modes.values(), modes.Distance);
	public static final Option CustomColor = new Option("CustomColor",false);
	public ClickGui() {
		super("ClickGui", new String[] { "clickui" }, ModuleType.Render);
		this.addValues(mode,CustomColor);
	}

	@Override
	public void onEnable() {

        new SoundFxPlayer().playSound(SoundFxPlayer.SoundType.ClickGuiOpen,-4);
	    switch ((modes)mode.getValue()){
            case Nov:{
                mc.displayGuiScreen(new ClickUi());
                break;
            }
            case Azlips:{
                mc.displayGuiScreen(new cn.distance.ui.gui.clikguis.clickgui4.ClickGui());
                break;
            }
            case Distance:{
                mc.displayGuiScreen(new ClientClickGui());
                break;
            }
        }
		this.setEnabled(false);
	}
    enum modes{
    	Nov,
        Distance,
		Azlips
    }
}
