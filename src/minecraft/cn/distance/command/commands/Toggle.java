package cn.distance.command.commands;

import cn.distance.Client;
import cn.distance.command.Command;
import cn.distance.module.Module;
import cn.distance.util.misc.Helper;
import net.minecraft.util.EnumChatFormatting;

public class Toggle extends Command {
	public Toggle() {
		super("t", new String[] { "toggle", "togl", "turnon", "enable" }, "", "切换指定模块的开关");
	}

	@Override
	public String execute(String[] args) {
		if (args.length == 0) {
			Helper.sendMessage("Correct usage .t <module>");
			return null;
		}
		for(String s : args)
		{
		boolean found = false;
		Module m = Client.instance.getModuleManager().getAlias(s);
		if (m != null) {
			if (!m.isEnabled()) {
				m.setEnabled(true);
			} else {
				m.setEnabled(false);
			}
			found = true;
			if (m.isEnabled()) {
				Helper.sendMessage(m.getName() + (Object) ((Object) EnumChatFormatting.GRAY) + " was"
						+ (Object) ((Object) EnumChatFormatting.GREEN) + " enabled");
			} else {
				Helper.sendMessage(m.getName() + (Object) ((Object) EnumChatFormatting.GRAY) + " was"
						+ (Object) ((Object) EnumChatFormatting.RED) + " disabled");
			}
		}
		if (!found) {
			Helper.sendMessage("Module name " + (Object) ((Object) EnumChatFormatting.RED) + s
					+ (Object) ((Object) EnumChatFormatting.GRAY) + " is invalid");
		}
		
	}
		return null;
	}
}
