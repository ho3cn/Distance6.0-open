package cn.distance.ui;

import java.awt.Color;


import cn.distance.Client;
import cn.distance.fastuni.FastUniFontRenderer;
import cn.distance.module.modules.render.HUD;
import cn.distance.util.SuperLib;
import cn.distance.util.anim.AnimationUtils;
import cn.distance.util.render.RenderUtil;
import cn.distance.util.time.StopWatchs;
import cn.distance.util.time.TimeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

import net.minecraft.util.ResourceLocation;

import static net.minecraft.util.MathHelper.abs;

public class ClientNotification {
	private final String message;
	private final TimeHelper timer;
	private final StopWatchs timer2;
	private double lastY;
	private double posY;
	private final double width;
	private final double height;
	private double animationX;
	private final int color;
	private final int imageWidth;
	private boolean forceFinished = false;
	private final ResourceLocation image;
	private long stayTime;
	Minecraft mc = Minecraft.getMinecraft();

	public ClientNotification(final String message, final Type type) {
		this.message = message;
		timer = new TimeHelper();
		timer2 = new StopWatchs();
		timer.reset();
		FastUniFontRenderer font = Client.FontLoaders.Chinese14;
		this.width = font.getStringWidth(message) + 60;
		this.height = 20.0;
		this.animationX = this.width;
		long staytimes = (long) (abs(width * 2));
		this.stayTime = 2000L + staytimes;
		this.imageWidth = 20;
		this.posY = -1.0;
		this.image = new ResourceLocation("Distance/notification/" + type.name() + ".png");
		this.color = HUD.logomode.getValue().equals(HUD.logomodeE.Dark_Distance) ? new Color(15, 15, 15).getRGB() : new Color(234, 234, 234).getRGB();

	}

	public void draw(final double getY, final double lastY) {
		this.lastY = lastY;
		this.animationX = SuperLib.getAnimationState(this.animationX, this.isFinished() ? this.width : 0.0, (Math.max(this.isFinished() ? 200 : 30, Math.abs(this.animationX - (this.isFinished() ? this.width : 0.0)) * 20.0) * 50) / Minecraft.getDebugFPS());
		if (this.posY == -1.0) {
			this.posY = getY;
		} else {
			this.posY = AnimationUtils.animate(getY, this.posY, 14f / Minecraft.getDebugFPS());
		}
		final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
		final int x1 = (int) (res.getScaledWidth() - this.width + this.animationX);
		final int x2 = (int) (res.getScaledWidth() + this.animationX);
		final int y1 = (int) this.posY;
		final int y2 = (int) (y1 + this.height);
		Gui.drawRect(x1 - 3, y1, x2, y2, this.color);
		SuperLib.drawImage(this.image, (int) (x1 - 1 + (this.height - this.imageWidth) / 2.0), y1 - 1 + (int) ((this.height - this.imageWidth) / 2.0), this.imageWidth, this.imageWidth);
		FastUniFontRenderer font = Client.FontLoaders.Chinese18;
		font.drawCenteredString(this.message, (float) (x1 + this.width / 2.0) - 4.0f, (float) (y1 + this.height / 2f) - 2.5f, HUD.logomode.getValue().equals(HUD.logomodeE.Dark_Distance) ? new Color(255, 255, 255).getRGB() : new Color(60, 60, 60).getRGB());
		if(stayTime==0)return;
		RenderUtil.drawGradientSideways(x1 - 3, y2 - 1,
				x1 + Math.min((x2 - x1) * (System.currentTimeMillis() - timer.getLastMs()) / this.stayTime, x2 - x1), y2,
				new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(),HUD.a.getValue().intValue()).getRGB(),
				new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(),HUD.a.getValue().intValue()).getRGB());

	}

	public boolean shouldDelete() {
		return this.isFinished() && this.animationX >= this.width;
	}

	public void setFinished() {
		forceFinished = true;
		stayTime = 0;
	}

	public boolean isFinished() {
		return (this.timer.isDelayComplete(this.stayTime) && (this.posY >= this.lastY - 20 || forceFinished));
	}

	public double getHeight() {
		return this.height;
	}

	public enum Type {
		success("success", 0),
		info("info", 1),
		warning("warning", 2),
		error("error", 3),
		gg("info", 4);

		Type(final String s, final int n) {
		}
	}
}

