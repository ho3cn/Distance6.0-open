package cn.distance.module.modules.move;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.EventPreUpdate;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.util.entity.MovementUtils;
import cn.distance.util.math.Rotation;
import cn.distance.util.math.RotationUtil;
import net.minecraft.potion.Potion;

import java.awt.*;

public class Sprint extends Module {
	public final Option allDirectionsValue = new Option("AllDirections", true);
	public final Option blindnessValue = new Option("Blindness", true);
	public final Option foodValue = new Option("Food", true);
	public final Option checkServerSide = new Option("CheckServerSide", false);
	public final Option checkServerSideGround = new Option("CheckServerSideOnlyGround", false);
	public Sprint() {
		super("Sprint", new String[] { "run" }, ModuleType.Movement);
		this.setColor(new Color(158, 205, 125).getRGB());
		this.addValues(allDirectionsValue,blindnessValue,foodValue,checkServerSide,checkServerSideGround);
	}
	@EventHandler
	private void onUpdate( EventPreUpdate event) {
		if (!MovementUtils.isMoving() || mc.thePlayer.isSneaking() ||
				(blindnessValue.getValue() && mc.thePlayer.isPotionActive(Potion.blindness)) ||
				(foodValue.getValue() && !(mc.thePlayer.getFoodStats().getFoodLevel() > 6.0F || mc.thePlayer.capabilities.allowFlying))
				|| (checkServerSide.getValue() && (mc.thePlayer.onGround || !checkServerSideGround.getValue())
				&& !allDirectionsValue.getValue() && RotationUtil.targetRotation != null &&
				RotationUtil.getRotationDifference(new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) > 30)) {
			mc.thePlayer.setSprinting(false);
			return;
		}
		if (allDirectionsValue.getValue() || mc.thePlayer.movementInput.moveForward >= 0.8F)
			mc.thePlayer.setSprinting(true);
	}
}
