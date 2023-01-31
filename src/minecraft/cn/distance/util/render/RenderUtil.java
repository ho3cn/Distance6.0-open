package cn.distance.util.render;

import cn.distance.module.modules.render.HUD;
import cn.distance.module.modules.render.TargetHUD;
import cn.distance.util.Vec3;
import cn.distance.util.math.Vec2f;
import cn.distance.util.math.Vec3f;
import cn.distance.util.misc.Helper;
import cn.distance.libraries.tessellate.Tessellation;
import cn.distance.util.render.gl.GLClientState;
import cn.distance.util.render.gl.ScaleUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import static org.lwjgl.opengl.GL11.*;
import static net.minecraft.client.renderer.GlStateManager.disableBlend;
import static net.minecraft.client.renderer.GlStateManager.enableTexture2D;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RenderUtil {
	public static float delta;
	public static float guiDelta;
	public static float render2dDelta;
	private static final Minecraft mc = Minecraft.getMinecraft();
	public static final Tessellation tessellator;
	private static final List<Integer> csBuffer;
	private static final Consumer<Integer> ENABLE_CLIENT_STATE;
	private static final Consumer<Integer> DISABLE_CLIENT_STATE;
	private static final Frustum frustrum = new Frustum();
	static {
		tessellator = Tessellation.createExpanding(4, 1.0f, 2.0f);
		csBuffer = new ArrayList<>();
		ENABLE_CLIENT_STATE = GL11::glEnableClientState;
		DISABLE_CLIENT_STATE = GL11::glEnableClientState;
	}
	public static void color(int color) {
		float f = (float) (color >> 24 & 255) / 255.0f;
		float f1 = (float) (color >> 16 & 255) / 255.0f;
		float f2 = (float) (color >> 8 & 255) / 255.0f;
		float f3 = (float) (color & 255) / 255.0f;
		GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
	}
	private static double lastP, diffP;
	public static void renderTHUD(EntityPlayer e) {
		GL11.glPushMatrix();
		Minecraft mc = Minecraft.getMinecraft();
		ScaleUtils.scale(mc);
		float hp = e.getHealth() + e.getAbsorptionAmount();
		final float maxHP = e.getMaxHealth() + e.getAbsorptionAmount() - 0.05f;
		int i = 0;

		for (int b = 0; b < e.inventory.armorInventory.length; b++) {
			final ItemStack armor = e.inventory.armorInventory[b];

			if (armor != null) {
				i++;
			}
		}

		if (e.getCurrentEquippedItem() != null) {
			i++;
		}

		float rectLength = 35 + mc.fontRendererObj.getStringWidth(e.getName()) + 40, health = (float) (Math.round(hp * 100.0) / 100.0);

		if (health > maxHP) {
			health *= maxHP / health;
		}

		float amplifier = 100 / maxHP, percent = health * amplifier, space = (rectLength - 50) / 100; //
		ScaledResolution sr = new ScaledResolution(mc);
		int i2 = (int) ((double) (ScaledResolution.getScaledWidth() / 2) + TargetHUD.hudx.getValue());
		int i1 = (int) ((double) ScaledResolution.getScaledHeight() - TargetHUD.hudY.getValue());

		if (percent < lastP) {
			diffP = lastP - percent;
		}
		lastP = percent;
		if (diffP > 0) {
			diffP = diffP + (0 - diffP) * 0.05f;
		}

		diffP = MathHelper.clamp_double(diffP, 0, 100 - percent);

		mc.getTextureManager().bindTexture(((AbstractClientPlayer) e).getLocationSkin());
		DrawUtil.drawBorderedRect(i2, i1 - 1.5f, i2 + rectLength - 6, i1 + 37.5, (float) 1, new Color(0, 0, 0, 50).getRGB(), new Color(29, 29, 29, 130).getRGB());
		Gui.drawRect(i2 + 1, i1, i2 + rectLength - 7, i1 + 36, new Color(40, 40, 40, 130).getRGB());
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);
		final int l6 = 8;
		final int i6 = 8;
		GL11.glScaled(4.4, 4.4, 4.4);
		Gui.drawScaledCustomSizeModalRect((float) ((i2 + 1.5) / 4.4), (float) ((i1 + 0.2) / 4.4), 8.0f, (float) l6, 8, i6, 8, 8, 64.0f, 64.0f);
		GL11.glPopMatrix();
		int hudColor = new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue()).getRGB();
		Gui.drawRect(i2 + 40, i1 + 16.5, i2 + 40 + 100 * space, i1 + 27.3, new Color(0, 0, 0, 50).getRGB());
		Gui.drawRect(i2 + 40, i1 + 16.5, i2 + 40 + percent * space, i1 + 27.3, hudColor);
		Gui.drawRect(i2 + 40 + percent * space, i1 + 16.5, i2 + 40 + percent * space + diffP * space, i1 + 27.3, new Color(hudColor).darker().getRGB());
		String text = String.format("%.1f", percent) + "%";
		mc.fontRendererObj.drawString(text, i2 + 40 + 50 * space - mc.fontRendererObj.getStringWidth(text) / 2,
				i1 + 18f, 0xffffffff, true);
		mc.fontRendererObj.drawString(e.getName(), i2 + 40, i1 + 4, 0xffffffff, true);
		GL11.glPopMatrix();
	}

	public static void drawNormalFace(EntityLivingBase e, float x, float y, float scale) {
		if (e instanceof EntityPlayer) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			List var5 = GuiPlayerTabOverlay.field_175252_a.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
			for (Object aVar5 : var5) {
				NetworkPlayerInfo var6 = (NetworkPlayerInfo) aVar5;
				if (mc.theWorld.getPlayerEntityByUUID(var6.getGameProfile().getId()) != e) continue;
				mc.getTextureManager().bindTexture(var6.getLocationSkin());
				Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 8.0f, 8.0f, 8, 8, (int) scale, (int) scale, 64.0f, 64.0f);
				if (((EntityPlayer) e).isWearing(EnumPlayerModelParts.HAT)) {
					Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 40.0f, 8.0f, 8, 8, (int) scale, (int) scale, 64.0f, 64.0f);
				}
				GlStateManager.bindTexture(0);
				break;
			}
		}
	}

	public static void renderItemStack(ItemStack stack, int x, int y) {
		GlStateManager.pushMatrix();
		GlStateManager.depthMask(true);
		GlStateManager.clear(256);
		RenderHelper.enableStandardItemLighting();
		Minecraft.getMinecraft().getRenderItem().zLevel = -150.0f;
		GlStateManager.disableDepth();
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		renderItem.renderItemOverlays(mc.fontRendererObj, stack, x, y);
		Minecraft.getMinecraft().getRenderItem().zLevel = 0.0f;
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableCull();
		GlStateManager.enableAlpha();
		disableBlend();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}
	public static void drawEntityOnScreen(int posX, int posY, int scale, EntityLivingBase ent) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, 50.0f);
		GlStateManager.scale(-scale, scale, scale);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		float f = ent.renderYawOffset;
		float f1 = ent.rotationYaw;
		float f2 = ent.rotationPitch;
		float f3 = ent.prevRotationYawHead;
		float f4 = ent.rotationYawHead;
		GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(0.0f, 1.0f, 0.0f, 0.0f);
		ent.renderYawOffset = 10 * ent.ticksExisted % 360;
		ent.rotationYaw = 10 * ent.ticksExisted % 360;
		ent.rotationPitch = 0.0f;
		ent.rotationYawHead = ent.rotationYaw;
		ent.prevRotationYawHead = ent.rotationYaw;
		GlStateManager.translate(0.0f, 0.0f, 0.0f);
		RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
		rendermanager.setPlayerViewY(180.0f);
		rendermanager.setRenderShadow(false);
		rendermanager.renderEntityWithPosYaw(ent, 0.0, 0.0, 0.0, 0.0f, 1.0f);
		rendermanager.setRenderShadow(true);
		ent.renderYawOffset = f;
		ent.rotationYaw = f1;
		ent.rotationPitch = f2;
		ent.prevRotationYawHead = f3;
		ent.rotationYawHead = f4;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}
	public static void drawEntityOnScreen(int p_147046_0_, int p_147046_1_, int p_147046_2_, float p_147046_3_, float p_147046_4_, EntityLivingBase p_147046_5_) {
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate(p_147046_0_, p_147046_1_, 40.0F);
		GlStateManager.scale((-p_147046_2_), p_147046_2_, p_147046_2_);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		float var6 = p_147046_5_.renderYawOffset;
		float var7 = p_147046_5_.rotationYaw;
		float var8 = p_147046_5_.rotationPitch;
		float var9 = p_147046_5_.prevRotationYawHead;
		float var10 = p_147046_5_.rotationYawHead;
		GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-((float) Math.atan(p_147046_4_ / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);
		p_147046_5_.renderYawOffset = (float) Math.atan(p_147046_3_ / 40.0F) * -14.0F;
		p_147046_5_.rotationYaw = (float) Math.atan(p_147046_3_ / 40.0F) * -14.0F;
		p_147046_5_.rotationPitch = -((float) Math.atan(p_147046_4_ / 40.0F)) * 15.0F;
		p_147046_5_.rotationYawHead = p_147046_5_.rotationYaw;
		p_147046_5_.prevRotationYawHead = p_147046_5_.rotationYaw;
		GlStateManager.translate(0.0F, 0.0F, 0.0F);
		RenderManager var11 = Minecraft.getMinecraft().getRenderManager();
		var11.setPlayerViewY(180.0F);
		var11.setRenderShadow(false);
		var11.renderEntityWithPosYaw(p_147046_5_, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
		var11.setRenderShadow(true);
		p_147046_5_.renderYawOffset = var6;
		p_147046_5_.rotationYaw = var7;
		p_147046_5_.rotationPitch = var8;
		p_147046_5_.prevRotationYawHead = var9;
		p_147046_5_.rotationYawHead = var10;
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public static void MdrawRect(double d, double e, double g, double h, int color) {
		if (d < g) {
			int i = (int) d;
			d = g;
			g = i;
		}

		if (e < h) {
			int j = (int) e;
			e = h;
			h = j;
		}

		float f3 = (color >> 24 & 255) / 255.0F;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(d, h, 0.0D).endVertex();
		worldrenderer.pos(g, h, 0.0D).endVertex();
		worldrenderer.pos(g, e, 0.0D).endVertex();
		worldrenderer.pos(d, e, 0.0D).endVertex();
		tessellator.draw();
		enableTexture2D();
		disableBlend();
	}

	public static void drawScaledCustomSizeModalRect(double d, double e, float u, float v, int uWidth, int vHeight, float size, float size2, float tileWidth, float tileHeight) {
		float f = 1.0f / tileWidth;
		float f1 = 1.0f / tileHeight;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(d, e + size2, 0.0).tex(u * f, (v + vHeight) * f1).endVertex();
		worldrenderer.pos(d + size, e + size2, 0.0).tex((u + uWidth) * f, (v + vHeight) * f1).endVertex();
		worldrenderer.pos(d + size, e, 0.0).tex((u + uWidth) * f, v * f1).endVertex();
		worldrenderer.pos(d, e, 0.0).tex(u * f, v * f1).endVertex();
		tessellator.draw();
	}

	public static void drawFace(double x, double y, double width, double height, AbstractClientPlayer target) {
		ResourceLocation skin = target.getLocationSkin();
		mc.getTextureManager().bindTexture(skin);
		glEnable(GL_BLEND);
		glColor4f(255, 255, 255, 1);

		final float hurtTimePercentage = (target.hurtTime - Minecraft.getMinecraft().timer.renderPartialTicks) / target.maxHurtTime;

		if (hurtTimePercentage > 0) {
			x += 1 * hurtTimePercentage;
			y += 1 * hurtTimePercentage;
			height -= 2 * hurtTimePercentage;
			width -= 2 * hurtTimePercentage;
		}

		drawScaledCustomSizeModalRect(x, y, 8F, 8F, 8, 8, (float) width, (float) height, 64, 64);

		if (hurtTimePercentage > 0.0) {
			glTranslated(x, y, 0);
			glDisable(GL_TEXTURE_2D);
			final boolean restore = DrawUtil.glEnableBlend();
			glShadeModel(GL_SMOOTH);
			glDisable(GL_ALPHA_TEST);

			final float lineWidth = 10.f;
			glLineWidth(lineWidth);

			final int fadeOutColour = ColourUtil.fadeTo(0x00000000, ColourUtil.blendHealthColours(target.getHealth() / target.getMaxHealth()), hurtTimePercentage);

			glBegin(GL_QUADS);
			{
				// Left
				DrawUtil.glColour(fadeOutColour);
				glVertex2d(0, 0);
				glVertex2d(0, height);
				DrawUtil.glColour(0x00FF0000);
				glVertex2d(lineWidth, height - lineWidth);
				glVertex2d(lineWidth, lineWidth);

				// Right
				DrawUtil.glColour(0x00FF0000);
				glVertex2d(width - lineWidth, lineWidth);
				glVertex2d(width - lineWidth, height - lineWidth);
				DrawUtil.glColour(fadeOutColour);
				glVertex2d(width, height);
				glVertex2d(width, 0);

				// Top
				DrawUtil.glColour(fadeOutColour);
				glVertex2d(0, 0);
				DrawUtil.glColour(0x00FF0000);
				glVertex2d(lineWidth, lineWidth);
				glVertex2d(width - lineWidth, lineWidth);
				DrawUtil.glColour(fadeOutColour);
				glVertex2d(width, 0);

				// Bottom
				DrawUtil.glColour(0x00FF0000);
				glVertex2d(lineWidth, height - lineWidth);
				DrawUtil.glColour(fadeOutColour);
				glVertex2d(0, height);
				glVertex2d(width, height);
				DrawUtil.glColour(0x00FF0000);
				glVertex2d(width - lineWidth, height - lineWidth);
			}
			glEnd();

			glEnable(GL_ALPHA_TEST);
			glShadeModel(GL_FLAT);
			DrawUtil.glRestoreBlend(restore);
			glEnable(GL_TEXTURE_2D);
			glTranslated(-x, -y, 0);
		}
	}
	public static double getAnimationState(double animation, double finalState, double speed) {
		float add = (float) (0.01 * speed);
		if (animation < finalState) {
			if (animation + add < finalState)
				animation += add;
			else
				animation = finalState;
		} else {
			if (animation - add > finalState)
				animation -= add;
			else
				animation = finalState;
		}
		return animation;
	}

	public static int reAlpha(int color, float alpha) {
		Color c = new Color(color);
		float r = 0.003921569F * (float) c.getRed();
		float g = 0.003921569F * (float) c.getGreen();
		float b = 0.003921569F * (float) c.getBlue();
		return (new Color(r, g, b, alpha)).getRGB();
	}
	public static void drawArc(float x1, float y1, double r, int color, int startPoint, double arc, int linewidth) {
		r *= 2.0D;
		x1 *= 2;
		y1 *= 2;
		float f = (color >> 24 & 0xFF) / 255.0F;
		float f1 = (color >> 16 & 0xFF) / 255.0F;
		float f2 = (color >> 8 & 0xFF) / 255.0F;
		float f3 = (color & 0xFF) / 255.0F;
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glDisable(3553);
		GL11.glBlendFunc(770, 771);
		GL11.glDepthMask(true);
		GL11.glEnable(2848);
		GL11.glHint(3154, 4354);
		GL11.glHint(3155, 4354);
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		GL11.glLineWidth(linewidth);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glColor4f(f1, f2, f3, f);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		for (int i = startPoint; i <= arc; i += 1) {
			double x = Math.sin(i * 3.141592653589793D / 180.0D) * r;
			double y = Math.cos(i * 3.141592653589793D / 180.0D) * r;
			GL11.glVertex2d(x1 + x, y1 + y);
		}
		GL11.glEnd();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		GL11.glEnable(3553);
		GL11.glDisable(3042);
		GL11.glEnable(2929);
		GL11.glDisable(2848);
		GL11.glHint(3154, 4352);
		GL11.glHint(3155, 4352);
	}

	public static void drawCircle2(double x, double y, double radius, int c) {
		float f2 = (float) (c >> 24 & 255) / 255.0f;
		float f22 = (float) (c >> 16 & 255) / 255.0f;
		float f3 = (float) (c >> 8 & 255) / 255.0f;
		float f4 = (float) (c & 255) / 255.0f;
		GlStateManager.alphaFunc(516, 0.001f);
		GlStateManager.color(f22, f3, f4, f2);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		Tessellator tes = Tessellator.getInstance();
		double i = 0.0;
		while (i < 360.0) {
			double f5 = Math.sin(i * 3.141592653589793 / 180.0) * radius;
			double f6 = Math.cos(i * 3.141592653589793 / 180.0) * radius;
			GL11.glVertex2d((double) ((double) f3 + x), (double) ((double) f4 + y));
			i += 1.0;
		}
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.enableTexture2D();
		GlStateManager.alphaFunc(516, 0.1f);
	}

	public static void drawFullCircle(int cx, int cy, double r, int segments, float lineWidth, int part, int c) {
		GL11.glScalef(0.5f, 0.5f, 0.5f);
		r *= 2.0;
		cx *= 2;
		cy *= 2;
		float f2 = (float) (c >> 24 & 255) / 255.0f;
		float f22 = (float) (c >> 16 & 255) / 255.0f;
		float f3 = (float) (c >> 8 & 255) / 255.0f;
		float f4 = (float) (c & 255) / 255.0f;
		GL11.glEnable(3042);
		GL11.glLineWidth(lineWidth);
		GL11.glDisable(3553);
		GL11.glEnable(2848);
		GL11.glBlendFunc(770, 771);
		GL11.glColor4f(f22, f3, f4, f2);
		GL11.glBegin(3);
		int i = segments - part;
		while (i <= segments) {
			double x = Math.sin((double) i * 3.141592653589793 / 180.0) * r;
			double y = Math.cos((double) i * 3.141592653589793 / 180.0) * r;
			GL11.glVertex2d((double) cx + x, (double) cy + y);
			++i;
		}
		GL11.glEnd();
		GL11.glDisable(2848);
		GL11.glEnable(3553);
		GL11.glDisable(3042);
		GL11.glScalef(2.0f, 2.0f, 2.0f);
	}
	public static void drawFilledCircle(double x, double y, double r, int c, int id) {
		float f = (float) (c >> 24 & 0xff) / 255F;
		float f1 = (float) (c >> 16 & 0xff) / 255F;
		float f2 = (float) (c >> 8 & 0xff) / 255F;
		float f3 = (float) (c & 0xff) / 255F;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glColor4f(f1, f2, f3, f);
		GL11.glBegin(GL11.GL_POLYGON);
		if (id == 1) {
			GL11.glVertex2d(x, y);
			for (int i = 0; i <= 90; i++) {
				double x2 = Math.sin((i * 3.141526D / 180)) * r;
				double y2 = Math.cos((i * 3.141526D / 180)) * r;
				GL11.glVertex2d(x - x2, y - y2);
			}
		} else if (id == 2) {
			GL11.glVertex2d(x, y);
			for (int i = 90; i <= 180; i++) {
				double x2 = Math.sin((i * 3.141526D / 180)) * r;
				double y2 = Math.cos((i * 3.141526D / 180)) * r;
				GL11.glVertex2d(x - x2, y - y2);
			}
		} else if (id == 3) {
			GL11.glVertex2d(x, y);
			for (int i = 270; i <= 360; i++) {
				double x2 = Math.sin((i * 3.141526D / 180)) * r;
				double y2 = Math.cos((i * 3.141526D / 180)) * r;
				GL11.glVertex2d(x - x2, y - y2);
			}
		} else if (id == 4) {
			GL11.glVertex2d(x, y);
			for (int i = 180; i <= 270; i++) {
				double x2 = Math.sin((i * 3.141526D / 180)) * r;
				double y2 = Math.cos((i * 3.141526D / 180)) * r;
				GL11.glVertex2d(x - x2, y - y2);
			}
		} else {
			for (int i = 0; i <= 360; i++) {
				double x2 = Math.sin((i * 3.141526D / 180)) * r;
				double y2 = Math.cos((i * 3.141526D / 180)) * r;
				GL11.glVertex2f((float) (x - x2), (float) (y - y2));
			}
		}
		GL11.glEnd();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	public static int createShader(String shaderCode, int shaderType) throws Exception {
		int shader;
		shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);
			if (shader != 0)
				return 0;
		} catch (Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
		ARBShaderObjects.glShaderSourceARB(shader, shaderCode);
		ARBShaderObjects.glCompileShaderARB(shader);
		if (ARBShaderObjects.glGetObjectParameteriARB(shader, 35713) == 0) {
			throw new RuntimeException("Error creating shader:");
		}
		return shader;
	}

	public static String getShaderCode(InputStreamReader file) {
		StringBuilder shaderSource = new StringBuilder();
		try {
			String line;
			BufferedReader reader = new BufferedReader(file);
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return shaderSource.toString();
	}

	public static boolean isHovering(int mouseX, int mouseY, float xLeft, float yUp, float xRight, float yBottom) {
		return mouseX > xLeft && mouseX < xRight && mouseY > yUp && mouseY < yBottom;
	}


	public static void Gamesense(double x, double y, double x1, double y1, double size, float color2, float color3, int badd) {
		RenderUtil.rectangleBordered(x, y, x1 + size, y1 + size, 0.5d, Colors2.getColor(90), Colors2.getColor(badd));
		RenderUtil.rectangleBordered(x + 1.0f, y + 1.0f, (x1 + size - 1.0f), y1 + size - 1.0f, 1.0, Colors2.getColor(90+badd),
				Colors2.getColor(61));
		RenderUtil.rectangleBordered(x + 2.5, y + 2.5, (x1 + size) - 2.5, (y1 + size) - 2.5, 0.5, Colors2.getColor(61+badd),
				Colors2.getColor(0));
		RenderUtil.drawGradientSideways(x + size * 3, (y + 3.0f), x1 -  size * 2, y + 4, (int) color2, (int) color3);
	}
	public static void triangle(float x1, float y1, float x2, float y2, float x3, float y3, int fill) {
		RenderUtil.enableGL2D();
		GlStateManager.color(0, 0, 0);
		GL11.glColor4f(0, 0, 0, 0);

		float var11 = (float)(fill >> 24 & 255) / 255.0F;
		float var6 = (float)(fill >> 16 & 255) / 255.0F;
		float var7 = (float)(fill >> 8 & 255) / 255.0F;
		float var8 = (float)(fill & 255) / 255.0F;

		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(var6, var7, var8, var11);

		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x3, y3);
		GL11.glVertex2f(x2, y2);
		GL11.glVertex2f(x1, y1);
		GL11.glEnd();

		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		RenderUtil.disableGL2D();
	}
	public static void prepareScissorBox(float x, float y, float x2, float y2) {
		ScaledResolution scale = new ScaledResolution(mc);
		int factor = scale.getScaleFactor();
		GL11.glScissor((int)(x * (float)factor), (int)(((float)scale.getScaledHeight() - y2) * (float)factor), (int)((x2 - x) * (float)factor), (int)((y2 - y) * (float)factor));
	}

	public static Color effect(long offset, int speed) {
		float hue = (float) (System.nanoTime() + (offset * speed)) / 0.7E10F % 1.0F;
		Color c = Color.getHSBColor(hue, 0.4f, 0.8f);
		return new Color(c.getRed()/255.0F, c.getGreen()/255.0F, 1f, c.getAlpha()/255.0F);
	}

	public static void renderStringWave(String s, float f, float yCount) {
		int updateX = (int) f;
		for(int i = 0; i < s.length(); i++) {
			String str = s.charAt(i) + "";
			Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(str, updateX, yCount, effect((i) * 5000000L, 100).getRGB());
			updateX += Minecraft.getMinecraft().fontRendererObj.getStringWidth(String.valueOf(s.charAt(i)));
		}
	}

	public static void drawRectWorldRenderer(double left, double top, double right, double bottom, int color) {
		if (left < right) {
			double i = left;
			left = right;
			right = i;
		}

		if (top < bottom) {
			double j = top;
			top = bottom;
			bottom = j;
		}

		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(left, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, bottom, 0.0D).endVertex();
		worldrenderer.pos(right, top, 0.0D).endVertex();
		worldrenderer.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	public static void drawPath( Vec3 vec) {
		double x = vec.getX() - mc.getRenderManager().renderPosX;
		double y = vec.getY() - mc.getRenderManager().renderPosY;
		double z = vec.getZ() - mc.getRenderManager().renderPosZ;
		double width = 0.3;
		double height = mc.thePlayer.getEyeHeight();
		RenderingUtil.pre3D();
		GL11.glLoadIdentity();
		mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2);
		int[] colors = {Colors.getColor(Color.WHITE), Colors.getColor(Color.white)};
		RenderingUtil.glColor(colors[1]);
		GL11.glLineWidth(3 - 2);
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3d(x - width, y, z - width);
		GL11.glVertex3d(x - width, y, z - width);
		GL11.glVertex3d(x - width, y + height, z - width);
		GL11.glVertex3d(x + width, y + height, z - width);
		GL11.glVertex3d(x + width, y, z - width);
		GL11.glVertex3d(x - width, y, z - width);
		GL11.glVertex3d(x - width, y, z + width);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3d(x + width, y, z + width);
		GL11.glVertex3d(x + width, y + height, z + width);
		GL11.glVertex3d(x - width, y + height, z + width);
		GL11.glVertex3d(x - width, y, z + width);
		GL11.glVertex3d(x + width, y, z + width);
		GL11.glVertex3d(x + width, y, z - width);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3d(x + width, y + height, z + width);
		GL11.glVertex3d(x + width, y + height, z - width);
		GL11.glEnd();
		GL11.glBegin(GL11.GL_LINE_STRIP);
		GL11.glVertex3d(x - width, y + height, z + width);
		GL11.glVertex3d(x - width, y + height, z - width);
		GL11.glEnd();

		RenderingUtil.post3D();
	}
	public static void drawFullCircle(int cx, double cy, double r, int segments, float lineWidth, int part, int c) {
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		r *= 2.0D;
		cx *= 2;
		cy *= 2;
		float f = (c >> 24 & 0xFF) / 255.0F;
		float f2 = (c >> 16 & 0xFF) / 255.0F;
		float f3 = (c >> 8 & 0xFF) / 255.0F;
		float f4 = (c & 0xFF) / 255.0F;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glLineWidth(lineWidth);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(f2, f3, f4, f);
		GL11.glBegin(GL11.GL_LINE_STRIP);

		for (int i = segments - part; i <= segments; i++) {
			double x = MathHelper.sin(i * Math.PI / 180.0D) * r;
			double y = Math.cos((float) (i * Math.PI / 180.0D)) * r;
			GL11.glVertex2d(cx + x, cy + y);
		}

		GL11.glEnd();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glScalef(2.0F, 2.0F, 2.0F);
	}
	public static void drawIcon(float x, float y, int sizex, int sizey, ResourceLocation resourceLocation) {
		GL11.glPushMatrix();
		Minecraft.getMinecraft().getTextureManager().bindTexture(resourceLocation);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(2848);
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1f);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		GL11.glTranslatef(x, y, 0.0f);
		RenderUtil.drawScaledRect(0, 0, 0.0f, 0.0f, sizex, sizey, sizex, sizey, sizex, sizey);
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.disableRescaleNormal();
		GL11.glDisable(2848);
		GlStateManager.disableBlend();
		GL11.glPopMatrix();
	}
	public static int rainbow(int delay) {
		double rainbowState = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 20.0D);
		rainbowState %= 360.0D;
		return Color.getHSBColor((float) (rainbowState / 360.0D), 0.8F, 0.7F).brighter().getRGB();
	}
	public static Color rainbow(int delay,boolean b) {
		double rainbowState = Math.ceil((double) (System.currentTimeMillis() + (long) delay) / 20.0D);
		rainbowState %= 360.0D;
		return Color.getHSBColor((float) (rainbowState / 360.0D), 0.8F, 0.7F).brighter();
	}
	public static void doGlScissor(int x, int y, int width, int height) {
		Minecraft mc = Minecraft.getMinecraft();
		int scaleFactor = 1;
		int k = mc.gameSettings.guiScale;
		if (k == 0) {
			k = 1000;
		}
		while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
			++scaleFactor;
		}
		GL11.glScissor(x * scaleFactor, mc.displayHeight - (y + height) * scaleFactor, width * scaleFactor, height * scaleFactor);
	}
	public static void drawScaledRect(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, float tileWidth, float tileHeight) {
		Gui.drawScaledCustomSizeModalRect(x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
	}

	public static void drawRoundedRect(float x, float y, float x2, float y2, final float round, final int color) {
		x += (float)(round / 2.0f + 0.5);
		y += (float)(round / 2.0f + 0.5);
		x2 -= (float)(round / 2.0f + 0.5);
		y2 -= (float)(round / 2.0f + 0.5);
		drawRect(x, y, x2, y2, color);
		enableGL2D();
		circle(x2 - round / 2.0f, y + round / 2.0f, round, color);
		circle(x + round / 2.0f, y2 - round / 2.0f, round, color);
		circle(x + round / 2.0f, y + round / 2.0f, round, color);
		circle(x2 - round / 2.0f, y2 - round / 2.0f, round, color);
		disableGL2D();

		drawRect((x - round / 2.0f - 0.5f), (y + round / 2.0f), x2, (y2 - round / 2.0f), color);
		drawRect(x, (y + round / 2.0f), (x2 + round / 2.0f + 0.5f), (y2 - round / 2.0f), color);
		drawRect((x + round / 2.0f), (y - round / 2.0f - 0.5f), (x2 - round / 2.0f), (y2 - round / 2.0f), color);
		drawRect((x + round / 2.0f), y, (x2 - round / 2.0f), (y2 + round / 2.0f + 0.5f), color);
	}
	public static void circle(final float x, final float y, final float radius, final Color fill) {
		arc(x, y, 0.0f, 360.0f, radius, fill);
	}
	public static void circle(final float x, final float y, final float radius, final int fill) {
		arc(x, y, 0.0f, 360.0f, radius, fill);
	}
	public static void arc(final float x, final float y, final float start, final float end, final float radius, final int color) {
		arcEllipse(x, y, start, end, radius, radius, color);
	}

	public static void arc(final float x, final float y, final float start, final float end, final float radius, final Color color) {
		arcEllipse(x, y, start, end, radius, radius, color);
	}

	public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final int color) {
		GlStateManager.color(0.0f, 0.0f, 0.0f);
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
		float temp;
		if (start > end) {
			temp = end;
			end = start;
			start = temp;
		}
		final float var11 = (color >> 24 & 0xFF) / 255.0f;
		final float var12 = (color >> 16 & 0xFF) / 255.0f;
		final float var13 = (color >> 8 & 0xFF) / 255.0f;
		final float var14 = (color & 0xFF) / 255.0f;
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(var12, var13, var14, var11);
		if (var11 > 0.5f) {
			GL11.glEnable(2848);
			GL11.glLineWidth(2.0f);
			GL11.glBegin(3);
			for (float i = end; i >= start; i -= 4.0f) {
				final float ldx = (float) (Math.cos((float) (i * 3.141592653589793 / 180.0)) * w * 1.001f);
				final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
				GL11.glVertex2f(x + ldx, y + ldy);
			}
			GL11.glEnd();
			GL11.glDisable(2848);
		}
		GL11.glBegin(6);
		for (float i = end; i >= start; i -= 4.0f) {
			final float ldx = (float) (Math.cos((float) (i * 3.141592653589793 / 180.0)) * w);
			final float ldy = (float) Math.sin(i * 3.141592653589793 / 180.0) * h;
			GL11.glVertex2f(x + ldx, y + ldy);
		}
		GL11.glEnd();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	public static void arcEllipse(final float x, final float y, float start, float end, final float w, final float h, final Color color) {
		GlStateManager.color(0.0f, 0.0f, 0.0f);
		GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
		float temp;
		if (start > end) {
			temp = end;
			end = start;
			start = temp;
		}
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, color.getAlpha() / 255.0f);
		if (color.getAlpha() > 0.5f) {
			GL11.glEnable(2848);
			GL11.glLineWidth(2.0f);
			GL11.glBegin(3);
			for (float i = end; i >= start; i -= 4.0f) {
				final float ldx = (float) (Math.cos((float) (i * 3.141592653589793 / 180.0)) * w * 1.001f);
				final float ldy = MathHelper.sin(i * 3.141592653589793 / 180.0) * h * 1.001f;
				GL11.glVertex2f(x + ldx, y + ldy);
			}
			GL11.glEnd();
			GL11.glDisable(2848);
		}
		GL11.glBegin(6);
		for (float i = end; i >= start; i -= 4.0f) {
			final float ldx = (float) (Math.cos((float) (i * 3.141592653589793 / 180.0)) * w);
			final float ldy = MathHelper.sin(i * 3.141592653589793 / 180.0) * h;
			GL11.glVertex2f(x + ldx, y + ldy);
		}
		GL11.glEnd();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	public RenderUtil() {
		super();
	}

	public static void enableGL2D() {
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glDisable(3553);
		GL11.glBlendFunc(770, 771);
		GL11.glDepthMask(true);
		GL11.glEnable(2848);
		GL11.glHint(3154, 4354);
		GL11.glHint(3155, 4354);
	}
	public static void drawFastRoundedRect(final float x0, final float y0, final float x1, final float y1, final float radius, final int color) {
		final int Semicircle = 360;
		final float f = 90.0f / Semicircle;
		final float f2 = (color >> 24 & 0xFF) / 255.0f;
		final float f3 = (color >> 16 & 0xFF) / 255.0f;
		final float f4 = (color >> 8 & 0xFF) / 255.0f;
		final float f5 = (color & 0xFF) / 255.0f;
		GL11.glDisable(2884);
		GL11.glDisable(3553);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(f3, f4, f5, f2);
		GL11.glBegin(5);
		GL11.glVertex2f(x0 + radius, y0);
		GL11.glVertex2f(x0 + radius, y1);
		GL11.glVertex2f(x1 - radius, y0);
		GL11.glVertex2f(x1 - radius, y1);
		GL11.glEnd();
		GL11.glBegin(5);
		GL11.glVertex2f(x0, y0 + radius);
		GL11.glVertex2f(x0 + radius, y0 + radius);
		GL11.glVertex2f(x0, y1 - radius);
		GL11.glVertex2f(x0 + radius, y1 - radius);
		GL11.glEnd();
		GL11.glBegin(5);
		GL11.glVertex2f(x1, y0 + radius);
		GL11.glVertex2f(x1 - radius, y0 + radius);
		GL11.glVertex2f(x1, y1 - radius);
		GL11.glVertex2f(x1 - radius, y1 - radius);
		GL11.glEnd();
		GL11.glBegin(6);
		float f6 = x1 - radius;
		float f7 = y0 + radius;
		GL11.glVertex2f(f6, f7);
		int j;
		//GL11.glEnable(2848);
		for (j = 0; j <= Semicircle; ++j) {
			final float f8 = j * f;
			GL11.glVertex2f((float) (f6 + radius * Math.cos((float) Math.toRadians(f8))), f7 - radius * MathHelper.sin(Math.toRadians(f8)));
		}
		//GL11.glDisable(2848);
		GL11.glEnd();
		GL11.glBegin(6);
		f6 = x0 + radius;
		f7 = y0 + radius;
		GL11.glVertex2f(f6, f7);
		for (j = 0; j <= Semicircle; ++j) {
			final float f9 = j * f;
			GL11.glVertex2f((float) (f6 - radius * Math.cos((float) Math.toRadians(f9))), f7 - radius * MathHelper.sin(Math.toRadians(f9)));
		}
		GL11.glEnd();
		GL11.glBegin(6);
		f6 = x0 + radius;
		f7 = y1 - radius;
		GL11.glVertex2f(f6, f7);
		for (j = 0; j <= Semicircle; ++j) {
			final float f10 = j * f;
			GL11.glVertex2f((float) (f6 - radius * Math.cos((float) Math.toRadians(f10))), f7 + radius * MathHelper.sin(Math.toRadians(f10)));
		}
		GL11.glEnd();
		GL11.glBegin(6);
		f6 = x1 - radius;
		f7 = y1 - radius;
		GL11.glVertex2f(f6, f7);
		for (j = 0; j <= Semicircle; ++j) {
			final float f11 = j * f;
			GL11.glVertex2f((float) (f6 + radius * Math.cos((float) Math.toRadians(f11))), f7 + radius * MathHelper.sin(Math.toRadians(f11)));
		}
		GL11.glEnd();
		GL11.glEnable(3553);
		GL11.glEnable(2884);
		GL11.glDisable(3042);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}
	public static void disableGL2D() {
		GL11.glEnable(3553);
		GL11.glDisable(3042);
		GL11.glEnable(2929);
		GL11.glDisable(2848);
		GL11.glHint(3154, 4352);
		GL11.glHint(3155, 4352);
	}
	public static void drawCircle(float cx, float cy, float r, int num_segments, int c) {
		GL11.glPushMatrix();
		cx *= 2.0F;
		cy *= 2.0F;
		float f = (c >> 24 & 0xFF) / 255.0F;
		float f1 = (c >> 16 & 0xFF) / 255.0F;
		float f2 = (c >> 8 & 0xFF) / 255.0F;
		float f3 = (c & 0xFF) / 255.0F;
		float theta = (float) (6.2831852D / num_segments);
		float p = (float) Math.cos(theta);
		float s = MathHelper.sin(theta);
		float x = r * 2.0F;
		float y = 0.0F;
		enableGL2D();
		GL11.glScalef(0.5F, 0.5F, 0.5F);
		GL11.glColor4f(f1, f2, f3, f);
		GL11.glBegin(2);
		int ii = 0;
		while (ii < num_segments) {
			GL11.glVertex2f(x + cx, y + cy);
			float t = x;
			x = p * x - s * y;
			y = s * t + p * y;
			ii++;
		}
		GL11.glEnd();
		GL11.glScalef(2.0F, 2.0F, 2.0F);
		disableGL2D();
		GlStateManager.color(1, 1, 1, 1);
		GL11.glPopMatrix();
	}
	public static void drawGradientSideways(double left, double top, double right, double bottom, int col1, int col2) {
		float f = (col1 >> 24 & 0xFF) / 255.0F;
		float f1 = (col1 >> 16 & 0xFF) / 255.0F;
		float f2 = (col1 >> 8 & 0xFF) / 255.0F;
		float f3 = (col1 & 0xFF) / 255.0F;

		float f4 = (col2 >> 24 & 0xFF) / 255.0F;
		float f5 = (col2 >> 16 & 0xFF) / 255.0F;
		float f6 = (col2 >> 8 & 0xFF) / 255.0F;
		float f7 = (col2 & 0xFF) / 255.0F;
		GL11.glEnable(3042);
		GL11.glDisable(3553);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(2848);
		GL11.glShadeModel(7425);

		GL11.glPushMatrix();
		GL11.glBegin(7);
		GL11.glColor4f(f1, f2, f3, f);
		GL11.glVertex2d(left, top);
		GL11.glVertex2d(left, bottom);

		GL11.glColor4f(f5, f6, f7, f4);
		GL11.glVertex2d(right, bottom);
		GL11.glVertex2d(right, top);
		GL11.glEnd();
		GL11.glPopMatrix();

		GL11.glEnable(3553);
		GL11.glDisable(3042);
		GL11.glDisable(2848);
		GL11.glShadeModel(7424);
		GL11.glColor4d(255, 255, 255, 255);
	}
	public static void drawMyTexturedModalRect(final float x, final float y, final int textureX, final int textureY,
											   final float width, final float height, final float factor) {
		final float f = 1.0f / factor;
		final Tessellator tessellator = Tessellator.getInstance();
		final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(x, y + height, 0.0D)
				.tex(textureX * f, (textureY + height) * f).endVertex();
		worldrenderer.pos(x + width, y + height, 0.0D)
				.tex((textureX + width) * f, (textureY + height) * f).endVertex();
		worldrenderer.pos(x + width, y, 0.0D)
				.tex((textureX + width) * f, textureY * f).endVertex();
		worldrenderer.pos(x, y, 0.0D).tex(textureX * f, textureY * f)
				.endVertex();
		tessellator.draw();
	}
	public static void drawHorizontalLine(float x, float y, float x1, float thickness, int color) {
		drawRect2(x, y, x1, y + thickness, color);
	}

	public static void drawVerticalLine(float x, float y, float y1, float thickness, int color) {
		drawRect2(x, y, x + thickness, y1, color);
	}
	public static void drawRect2(double x, double y, double x2, double y2, int color) {
		Gui.drawRect((int)x, (int)y, (int)x2, (int)y2, color);
	}
	public static void drawHollowBox(float x, float y, float x1, float y1, float thickness, int color) {
		/* Top */
		drawHorizontalLine(x, y, x1, thickness, color);
		/* Bottom */
		drawHorizontalLine(x, y1, x1, thickness, color);
		/* Left */
		drawVerticalLine(x, y, y1, thickness, color);
		/* Right */
		drawVerticalLine(x1 - thickness, y, y1, thickness, color);
	}
	public static boolean isInViewFrustrum(Entity entity) {
		return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
	}

	public static boolean isInViewFrustrum(AxisAlignedBB bb) {
		Entity current = Minecraft.getMinecraft().getRenderViewEntity();
		frustrum.setPosition(current.posX, current.posY, current.posZ);
		return frustrum.isBoundingBoxInFrustum(bb);
	}
	public static int getHealthColor(EntityLivingBase player) {
		float f = player.getHealth();
		float f1 = player.getMaxHealth();
		float f2 = Math.max(0.0F, Math.min(f, f1) / f1);
		return Color.HSBtoRGB(f2 / 3.0F, 1.0F, 1.0F) | 0xFF000000;
	}
	public static float[] getRGBAs(int rgb) {
		return new float[] { ((rgb >> 16) & 255) / 255F, ((rgb >> 8) & 255) / 255F, (rgb & 255) / 255F,
				((rgb >> 24) & 255) / 255F };
	}

	public static int getRainbow(int speed, int offset) {
		float hue = (System.currentTimeMillis() + offset) % speed;
		hue /= speed;
		return Color.getHSBColor(hue, 0.75f, 1f).getRGB();

	}

	public static void rectangle(double left, double top, double right, double bottom, int color) {
		double var5;
		if (left < right) {
			var5 = left;
			left = right;
			right = var5;
		}
		if (top < bottom) {
			var5 = top;
			top = bottom;
			bottom = var5;
		}
		float var11 = (float) (color >> 24 & 255) / 255.0f;
		float var6 = (float) (color >> 16 & 255) / 255.0f;
		float var7 = (float) (color >> 8 & 255) / 255.0f;
		float var8 = (float) (color & 255) / 255.0f;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(var6, var7, var8, var11);
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(left, bottom, 0.0).endVertex();
		worldRenderer.pos(right, bottom, 0.0).endVertex();
		worldRenderer.pos(right, top, 0.0).endVertex();
		worldRenderer.pos(left, top, 0.0).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor,
										 int borderColor) {
		RenderUtil.rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderUtil.rectangle(x + width, y, x1 - width, y + width, borderColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderUtil.rectangle(x, y, x + width, y1, borderColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderUtil.rectangle(x1 - width, y, x1, y1, borderColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		RenderUtil.rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}
	public static double[] convertTo2D(double x, double y, double z) {
		double[] arrd;
		FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
		FloatBuffer projection = BufferUtils.createFloatBuffer(16);
		GL11.glGetFloat(2982, modelView);
		GL11.glGetFloat(2983, projection);
		GL11.glGetInteger(2978, viewport);
		boolean result = GLU.gluProject((float) x, (float) y,
				(float) z, modelView, projection,
				viewport, screenCoords);
		if (result) {
			double[] arrd2 = new double[3];
			arrd2[0] = screenCoords.get(0);
			arrd2[1] = (float) Display.getHeight() - screenCoords.get(1);
			arrd = arrd2;
			arrd2[2] = screenCoords.get(2);
		} else {
			arrd = null;
		}
		return arrd;
	}

	public static void drawblock(double a, double a2, double a3, int a4, int a5, float a6) {
		float a7 = (float) (a4 >> 24 & 255) / 255.0f;
		float a8 = (float) (a4 >> 16 & 255) / 255.0f;
		float a9 = (float) (a4 >> 8 & 255) / 255.0f;
		float a10 = (float) (a4 & 255) / 255.0f;
		float a11 = (float) (a5 >> 24 & 255) / 255.0f;
		float a12 = (float) (a5 >> 16 & 255) / 255.0f;
		float a13 = (float) (a5 >> 8 & 255) / 255.0f;
		float a14 = (float) (a5 & 255) / 255.0f;
		GL11.glPushMatrix();
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3553);
		GL11.glEnable(2848);
		GL11.glDisable(2929);
		GL11.glDepthMask(false);
		GL11.glColor4f(a8, a9, a10, a7);
		drawOutlinedBoundingBox(new AxisAlignedBB(a, a2, a3, a + 1.0, a2 + 1.0, a3 + 1.0));
		GL11.glLineWidth(a6);
		GL11.glColor4f(a12, a13, a14, a11);
		drawOutlinedBoundingBox(new AxisAlignedBB(a, a2, a3, a + 1.0, a2 + 1.0, a3 + 1.0));
		GL11.glDisable(2848);
		GL11.glEnable(3553);
		GL11.glEnable(2929);
		GL11.glDepthMask(true);
		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}
	public static void pre3D() {
		GL11.glPushMatrix();
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
	}

	public static int width() {
		return new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
	}

	public static int height() {
		return new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight();
	}

	public static double interpolation(final double newPos, final double oldPos) {
		return oldPos + (newPos - oldPos) * Helper.mc.timer.renderPartialTicks;
	}

	public static void draw2DCorner(double posX, double posY, double posZ, int color) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(posX, posY, posZ);
		GL11.glNormal3f(0.0f, 0.0f, 0.0f);
		GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
		GlStateManager.scale(-0.1, -0.1, 0.1);
		GL11.glDisable(2896);
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GlStateManager.depthMask(true);
		drawRect(7.0, -20.0, 7.300000190734863, -17.5, color);
		drawRect(-7.300000190734863, -20.0, -7.0, -17.5, color);
		drawRect(4.0, -20.299999237060547, 7.300000190734863, -20.0, color);
		drawRect(-7.300000190734863, -20.299999237060547, -4.0, -20.0, color);
		drawRect(-7.0, 3.0, -4.0, 3.299999952316284, color);
		drawRect(4.0, 3.0, 7.0, 3.299999952316284, color);
		drawRect(-7.300000190734863, 0.8, -7.0, 3.299999952316284, color);
		drawRect(7.0, 0.5, 7.300000190734863, 3.299999952316284, color);
		drawRect(7.0, -20.0, 7.300000190734863, -17.5, color);
		drawRect(-7.300000190734863, -20.0, -7.0, -17.5, color);
		drawRect(4.0, -20.299999237060547, 7.300000190734863, -20.0, color);
		drawRect(-7.300000190734863, -20.299999237060547, -4.0, -20.0, color);
		drawRect(-7.0, 3.0, -4.0, 3.299999952316284, color);
		drawRect(4.0, 3.0, 7.0, 3.299999952316284, color);
		drawRect(-7.300000190734863, 0.8, -7.0, 3.299999952316284, color);
		drawRect(7.0, 0.5, 7.300000190734863, 3.299999952316284, color);
		GL11.glDisable(3042);
		GL11.glEnable(2929);
		GlStateManager.popMatrix();
	}
	public static void drawRect(double x2, double y2, double x1, double y1, int color) {
		enableGL2D();
		glColor(color);
		drawRect(x2, y2, x1, y1);
		disableGL2D();
	}

	private static void drawRect(double x2, double y2, double x1, double y1) {
		GL11.glBegin(7);
		GL11.glVertex2d(x2, y1);
		GL11.glVertex2d(x1, y1);
		GL11.glVertex2d(x1, y2);
		GL11.glVertex2d(x2, y2);
		GL11.glEnd();
	}

	public static void drawRect(float x, float y, float x1, float y1, int color) {
		enableGL2D();
		glColor(color);
		drawRect(x, y, x1, y1);
		disableGL2D();
	}
	public static void drawRect3(final float x, final float y, final float x1, final float y1) {
		glBegin(7);
		glVertex2f(x, y1);
		glVertex2f(x1, y1);
		glVertex2f(x1, y);
		glVertex2f(x, y);
		glEnd();
	}
	public static void drawRect3(float left, float top, float right, float bottom, int color) {
		float f3;
		if (left < right) {
			f3 = left;
			left = right;
			right = f3;
		}

		if (top < bottom) {
			f3 = top;
			top = bottom;
			bottom = f3;
		}

		f3 = (color >> 24 & 255) / 255.0F;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer WorldRenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f3);
		WorldRenderer.begin(7, DefaultVertexFormats.POSITION);
		WorldRenderer.pos(left, bottom, 0.0D).endVertex();
		WorldRenderer.pos(right, bottom, 0.0D).endVertex();
		WorldRenderer.pos(right, top, 0.0D).endVertex();
		WorldRenderer.pos(left, top, 0.0D).endVertex();
		tessellator.draw();
		enableTexture2D();
		disableBlend();
	}
	public static void drawRect3(double d, double e, double g, double h, int color) {
		int f3;
		if (d < g) {
			f3 = (int) d;
			d = g;
			g = f3;
		}

		if (e < h) {
			f3 = (int) e;
			e = h;
			h = f3;
		}

		float f31 = (color >> 24 & 255) / 255.0F;
		float f = (color >> 16 & 255) / 255.0F;
		float f1 = (color >> 8 & 255) / 255.0F;
		float f2 = (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.color(f, f1, f2, f31);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION);
		worldrenderer.pos(d, h, 0.0D).endVertex();
		worldrenderer.pos(g, h, 0.0D).endVertex();
		worldrenderer.pos(g, e, 0.0D).endVertex();
		worldrenderer.pos(d, e, 0.0D).endVertex();
		tessellator.draw();
		enableTexture2D();
		disableBlend();
	}
	public static void drawBorderedRect(float x, float y, float x1, float y1, float width, int borderColor) {
		enableGL2D();
		glColor(borderColor);
		drawRect(x + width, y, x1 - width, y + width);
		drawRect(x, y, x + width, y1);
		drawRect(x1 - width, y, x1, y1);
		drawRect(x + width, y1 - width, x1 - width, y1);
		disableGL2D();
	}

	public static void drawGradientRect(float x, float y, float x1, float y1, int topColor, int bottomColor) {
		enableGL2D();
		GL11.glShadeModel(7425);
		GL11.glBegin(7);
		glColor(topColor);
		GL11.glVertex2f(x, y1);
		GL11.glVertex2f(x1, y1);
		glColor(bottomColor);
		GL11.glVertex2f(x1, y);
		GL11.glVertex2f(x, y);
		GL11.glEnd();
		GL11.glShadeModel(7424);
		disableGL2D();
	}

	public static void drawHLine(float x, float y, float x1, int y1) {
		if (y < x) {
			float var5 = x;
			x = y;
			y = var5;
		}
		drawRect(x, x1, y + 1.0f, x1 + 1.0f, y1);
	}

	public static void drawVLine(float x, float y, float x1, int y1) {
		if (x1 < y) {
			float var5 = y;
			y = x1;
			x1 = var5;
		}
		drawRect(x, y + 1.0f, x + 1.0f, x1, y1);
	}

	public static void drawHLine(float x, float y, float x1, int y1, int y2) {
		if (y < x) {
			float var5 = x;
			x = y;
			y = var5;
		}
		drawGradientRect(x, x1, y + 1.0f, x1 + 1.0f, y1, y2);
	}

	public static void drawRect(float x, float y, float x1, float y1) {
		GL11.glBegin(7);
		GL11.glVertex2f(x, y1);
		GL11.glVertex2f(x1, y1);
		GL11.glVertex2f(x1, y);
		GL11.glVertex2f(x, y);
		GL11.glEnd();
	}

	public static int getHexRGB(final int hex) {
		return 0xFF000000 | hex;
	}

	public static void drawCustomImage(int x, int y, int width, int height, ResourceLocation image) {
		ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(image);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height,
				(float) width, (float) height);
		GL11.glDepthMask(true);
		GL11.glDisable(3042);
		GL11.glEnable(2929);
		Gui.drawRect(0, 0, 0, 0, 0);
	}

	public static void drawBorderedRect(final float x, final float y, final float x2, final float y2, final float l1,
										final int col1, final int col2) {
		Gui.drawRect((int)x, (int)y, (int)x2, (int)y2, col2);
		final float f = (col1 >> 24 & 0xFF) / 255.0f;
		final float f2 = (col1 >> 16 & 0xFF) / 255.0f;
		final float f3 = (col1 >> 8 & 0xFF) / 255.0f;
		final float f4 = (col1 & 0xFF) / 255.0f;
		GL11.glEnable(3042);
		GL11.glDisable(3553);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(2848);
		GL11.glPushMatrix();
		GL11.glColor4f(f2, f3, f4, f);
		GL11.glLineWidth(l1);
		GL11.glBegin(1);
		GL11.glVertex2d(x, y);
		GL11.glVertex2d(x, y2);
		GL11.glVertex2d(x2, y2);
		GL11.glVertex2d(x2, y);
		GL11.glVertex2d(x, y);
		GL11.glVertex2d(x2, y);
		GL11.glVertex2d(x, y2);
		GL11.glVertex2d(x2, y2);
		GL11.glEnd();
		GL11.glPopMatrix();
		GL11.glEnable(3553);
		GL11.glDisable(3042);
		GL11.glDisable(2848);
	}

	public static void pre() {
		GL11.glDisable(2929);
		GL11.glDisable(3553);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
	}

	public static void post() {
		GL11.glDisable(3042);
		GL11.glEnable(3553);
		GL11.glEnable(2929);
		GL11.glColor3d(1.0, 1.0, 1.0);
	}

	public static void startDrawing() {
		GL11.glEnable(3042);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glEnable(2848);
		GL11.glDisable(3553);
		GL11.glDisable(2929);
		Helper.mc.entityRenderer.setupCameraTransform(Helper.mc.timer.renderPartialTicks, 0);
	}

	public static void stopDrawing() {
		GL11.glDisable(3042);
		GL11.glEnable(3553);
		GL11.glDisable(2848);
		GL11.glDisable(3042);
		GL11.glEnable(2929);
	}

	public static Color blend(final Color color1, final Color color2, final double ratio) {
		final float r = (float) ratio;
		final float ir = 1.0f - r;
		final float[] rgb1 = new float[3];
		final float[] rgb2 = new float[3];
		color1.getColorComponents(rgb1);
		color2.getColorComponents(rgb2);
		final Color color3 = new Color(rgb1[0] * r + rgb2[0] * ir, rgb1[1] * r + rgb2[1] * ir,
				rgb1[2] * r + rgb2[2] * ir);
		return color3;
	}

	public static void drawLine( final Vec2f start, final Vec2f end, final float width) {
		drawLine(start.getX(), start.getY(), end.getX(), end.getY(), width);
	}

	public static void drawLine( final Vec3f start, final Vec3f end, final float width) {
		drawLine((float) start.getX(), (float) start.getY(), (float) start.getZ(), (float) end.getX(),
				(float) end.getY(), (float) end.getZ(), width);
	}

	public static void drawLine(final float x, final float y, final float x1, final float y1, final float width) {
		drawLine(x, y, 0.0f, x1, y1, 0.0f, width);
	}

	public static void drawLine(final float x, final float y, final float z, final float x1, final float y1,
								final float z1, final float width) {
		GL11.glLineWidth(width);
		setupRender(true);
		setupClientState(GLClientState.VERTEX, true);
		RenderUtil.tessellator.addVertex(x, y, z).addVertex(x1, y1, z1).draw(3);
		setupClientState(GLClientState.VERTEX, false);
		setupRender(false);
	}

	public static void setupClientState(final GLClientState state, final boolean enabled) {
		RenderUtil.csBuffer.clear();
		if (state.ordinal() > 0) {
			RenderUtil.csBuffer.add(state.getCap());
		}
		RenderUtil.csBuffer.add(32884);
		RenderUtil.csBuffer.forEach(enabled ? RenderUtil.ENABLE_CLIENT_STATE : RenderUtil.DISABLE_CLIENT_STATE);
	}

	public static void setupRender(final boolean start) {
		if (start) {
			GlStateManager.enableBlend();
			GL11.glEnable(2848);
			GlStateManager.disableDepth();
			GlStateManager.disableTexture2D();
			GlStateManager.blendFunc(770, 771);
			GL11.glHint(3154, 4354);
		} else {
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
			GL11.glDisable(2848);
			GlStateManager.enableDepth();
		}
		GlStateManager.depthMask(!start);
	}

	public static void drawImage(ResourceLocation image, float x, float y, int width, int height) {
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GL11.glEnable(3042);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		Minecraft.getMinecraft().getTextureManager().bindTexture(image);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
		GL11.glDepthMask(true);
		GL11.glEnable(2929);
	}

	public static void drawImage(ResourceLocation image, float x, float y, int width, int height,float alpha) {
		GlStateManager.disableAlpha();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GL11.glDisable(2929);
		GL11.glEnable(3042);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, alpha);
		Minecraft.getMinecraft().getTextureManager().bindTexture(image);
		Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0f, 0.0f, width, height, width, height);
		GL11.glDepthMask(true);
		GL11.glEnable(2929);
	}


	public static void layeredRect(double x1, double y1, double x2, double y2, int outline, int inline,
								   int background) {
		drawRect(x1, y1, x2, y2, outline);
		drawRect(x1 + 1, y1 + 1, x2 - 1, y2 - 1, inline);
		drawRect(x1 + 2, y1 + 2, x2 - 2, y2 - 2, background);
	}

	public static void drawEntityESP(double x, double y, double z, double width, double height, float red, float green,
									 float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWdith) {
		GL11.glPushMatrix();
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3553);
		GL11.glEnable(2848);
		GL11.glDisable(2929);
		GL11.glDepthMask(false);
		GL11.glColor4f(red, green, blue, alpha);
		RenderUtil.drawBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
		GL11.glLineWidth(lineWdith);
		GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
		RenderUtil
				.drawOutlinedBoundingBox(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
		GL11.glDisable(2848);
		GL11.glEnable(3553);
		GL11.glEnable(2929);
		GL11.glDepthMask(true);
		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}
	public static void drawEntityESPs(double x, double y, double z, double width, double height, float red, float green, float blue, float alpha, float lineRed, float lineGreen, float lineBlue, float lineAlpha, float lineWdith) {
		GL11.glPushMatrix();
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, 771);
		GL11.glDisable(3553);
		GL11.glEnable(2848);
		GL11.glDisable(2929);
		GL11.glDepthMask(false);
		GL11.glColor4f(red, green, blue, alpha);
		RenderUtil.drawBoundingBox2(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
		GL11.glLineWidth(lineWdith);
		GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha);
		RenderUtil.drawOutlinedBoundingBox2(new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width));
		GL11.glDisable(2848);
		GL11.glEnable(3553);
		GL11.glEnable(2929);
		GL11.glDepthMask(true);
		GL11.glDisable(3042);
		GL11.glPopMatrix();
	}
	public static void drawOutlinedBoundingBox2(AxisAlignedBB aa) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(3, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(3, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(1, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		tessellator.draw();
	}

	public static void drawBoundingBox2(AxisAlignedBB aa) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		tessellator.draw();
	}


	public static void drawBoundingBox(AxisAlignedBB aa) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(7, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		tessellator.draw();
	}

	public static void drawOutlinedBoundingBox(AxisAlignedBB aa) {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		worldRenderer.begin(3, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(3, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		tessellator.draw();
		worldRenderer.begin(1, DefaultVertexFormats.POSITION);
		worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex();
		worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex();
		tessellator.draw();
	}
	public static void glColor(Color color) {
		GL11.glColor4f((float) color.getRed() / 255.0f, (float) color.getGreen() / 255.0f,
				(float) color.getBlue() / 255.0f, (float) color.getAlpha() / 255.0f);
	}
	public static void glColor(float alpha, int redRGB, int greenRGB, int blueRGB) {
		float red = 0.003921569F * redRGB;
		float green = 0.003921569F * greenRGB;
		float blue = 0.003921569F * blueRGB;
		GL11.glColor4f(red, green, blue, alpha);
	}

	public static void glColor(int hex) {
		float alpha = (hex >> 24 & 0xFF) / 255.0F;
		float red = (hex >> 16 & 0xFF) / 255.0F;
		float green = (hex >> 8 & 0xFF) / 255.0F;
		float blue = (hex & 0xFF) / 255.0F;
		GL11.glColor4f(red, green, blue, alpha);
	}

	public static void post3D() {
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
		GL11.glColor4f(1, 1, 1, 1);
	}
}
