package cn.distance.module.modules.world;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.Color;

import org.lwjgl.input.Mouse;

import cn.distance.manager.FriendManager;

public class MCF extends Module {
	private boolean down;

	public MCF() {
		super("MCF", new String[] { "middleclickfriends", "middleclick" }, ModuleType.World);
		this.setColor(new Color(241, 175, 67).getRGB());
	}

	@EventHandler
	private void onClick(EventPreUpdate e) {
		if (Mouse.isButtonDown((int) 2) && !this.down) {
			if (this.mc.objectMouseOver.entityHit != null && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
				EntityPlayer player = (EntityPlayer) this.mc.objectMouseOver.entityHit;
				String playername = player.getName();
				if (!FriendManager.isFriend(playername)) {
					this.mc.thePlayer.sendChatMessage(".f add " + playername);
				} else {
					this.mc.thePlayer.sendChatMessage(".f del " + playername);
				}
			}
			this.down = true;
		}
		if (!Mouse.isButtonDown((int) 2)) {
			this.down = false;
		}
	}
}
