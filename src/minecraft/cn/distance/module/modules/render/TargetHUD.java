//Code by SB
package cn.distance.module.modules.render;

import cn.core.auth.modules.KillAura;
import cn.distance.util.math.TimeUtil;
import cn.distance.util.render.*;
import cn.distance.util.time.MSTimer;
import com.ibm.icu.text.NumberFormat;
import cn.distance.Client;
import cn.distance.api.EventHandler;
import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.value.Mode;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.fastuni.FastUniFontRenderer;
import cn.distance.fastuni.FontLoader;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.font.CFontRenderer;
import cn.distance.ui.font.FontLoaders;
import cn.distance.util.anim.AnimationUtil;
import cn.distance.util.anim.AnimationUtils;
import cn.distance.util.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.MathUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.distance.util.render.RenderUtil.drawFace;

public class TargetHUD
        extends Module {
    private double healthBarWidth;
    private double healthBarWidth2;
    private double introAnim;
    public static double animation = 0;
    private final FastUniFontRenderer font1 = Client.FontLoaders.Chinese16;
    boolean startAnim, stopAnim;
    double r2;
    public EntityLivingBase lastEnt;
    public EntityPlayer ent;
    float anim2 = 0f;
    double rect;
    int animAlpha = 0;
    private static final TimerUtil timerUtils = new TimerUtil();
    private final TimeUtil timeUtil = new TimeUtil();
    public static int authListPos;

    public DecimalFormat format;
    public static DecimalFormat format0 = new DecimalFormat("0.0");
    public static DecimalFormat format00 = new DecimalFormat("0");
    public static Mode mode = new Mode("Mode", Modes.values(), Modes.Distance);
    public static Mode animMode = new Mode("Animation Mode", AnimMode.values(), AnimMode.Scale);

    public static Numbers<Double> hudx = new Numbers<>("X", 70.0d, -400d, 300d, 1d);
    public static Numbers<Double> hudY = new Numbers<>("Y", 80.0d, 0d, 400d, 1d);

    private static final Option black = new Option("Black", true);
    private static final Option Pvp = new Option("PVP", false);
    private final List<Particle> particles = new ArrayList<>();
    private static double animHealth = 0;
    private float displayHealth;
    private double scale = 1;
    private int ticks;
    float powerxHealthAnim;
    private int animWidth;
    private boolean sentParticles;
    private final TimeUtil timer = new TimeUtil();
    private static final DecimalFormat df = new DecimalFormat("00.0");
    //remix
    private float health;

    public TargetHUD() {
        super("TargetHUD", new String[]{"TargetInfo"}, ModuleType.Render);
        addValues(hudx, hudY, mode, black, animMode, Pvp);
    }

    @Override
    public void onEnable() {
        animation = 0;
    }

    float width = 0;
    float height = 0;

    boolean introOverCharge = false;

    @EventHandler
    public void onRender(EventRender2D event) {
        if (mc.ingameGUI.getChatGUI().getChatOpen()) {
            nulltarget = false;
            target = mc.thePlayer;
        } else {
            if ( KillAura.target == null) {
                if (Pvp.getValue()) {
                    if (mc.pointedEntity != null) {
                        nulltarget = false;
                        if (mc.pointedEntity instanceof EntityLivingBase) {
                            target = (EntityLivingBase) mc.pointedEntity;
                        } else {
                            nulltarget = true;
                        }
                    } else {
                        nulltarget = true;
                    }
                } else {
                    nulltarget = true;
                }
            } else {
                nulltarget = false;
                target = KillAura.target;
            }
        }

        GlStateManager.pushMatrix();
        sr = new ScaledResolution(mc);
        if (nulltarget) {
            if (timerUtils.hasReached(1000L)) {
                introOverCharge = false;
                this.introAnim = AnimationUtils.animate(animMode.get().equals(AnimMode.Scale) ? 0f : (sr.getScaledWidth() / 2.0f) - (hudx.getValue().floatValue()), this.introAnim, 14f / Minecraft.getDebugFPS());
            } else {
                if (!introOverCharge && introAnim > 110){
                    introOverCharge = true;
                }
                this.introAnim = AnimationUtils.animate(animMode.get().equals(AnimMode.Scale) ? introOverCharge ? 100f : 140f : 0f, this.introAnim, 14f / Minecraft.getDebugFPS());
            }
        } else {
            timerUtils.reset();
            if (!introOverCharge && introAnim > 110){
                introOverCharge = true;
            }
            this.introAnim = AnimationUtils.animate(animMode.get().equals(AnimMode.Scale) ? introOverCharge ? 100f : 140f : 0f, this.introAnim, 14f / Minecraft.getDebugFPS());
        }
        x = sr.getScaledWidth() / 2.0f + hudx.getValue().floatValue();
        y = sr.getScaledHeight() / 2.0f + hudY.getValue().floatValue();

        if (animMode.get().equals(AnimMode.Scale)) {
            float value = (float) Math.max(0, introAnim / 100f);
            GlStateManager.translate((x + width / 2f) * (1 - value), (y + height / 2f) * (1 - value), 0f);
            GlStateManager.scale(value, value, 0);
        } else {
            GlStateManager.translate(introAnim, 0, 0);
        }

        if (animMode.get().equals(AnimMode.Slide) && introAnim == sr.getScaledWidth() / 2.0f - hudx.getValue().floatValue()) {
            if (mode.get().equals(Modes.Distance)) {
                this.healthBarWidth2 = 92;
                this.healthBarWidth = 92;
            }
        } else if (animMode.get().equals(AnimMode.Scale) && introAnim < 30) {
            if (mode.get().equals(Modes.Distance)) {
                this.healthBarWidth2 = 92;
                this.healthBarWidth = 92;
            }
        } else {
            switch ((Modes) mode.getValue()) {
                case Simple: {
                    simple();
                    break;
                }
                case Distance: {
                    Flat();
                    break;
                }
                case Astolfo: {
                    SB();
                    break;
                }
                case Exhibition: {
                    ES();
                    break;
                }
                case OldExhibition: {
                    NaoCan();
                    break;
                }
                case Rosalba: {
                    Fuck();
                    break;
                }
                case Remix: {
                    NM();
                    break;
                }
                case Rise: {
                    NMSL();
                    break;
                }
                case Classic: {
                    CSB();
                    break;
                }
                case Vanilla: {
                    CNM();
                    break;
                }
                case Novoline: {
                    FW();
                    break;
                }
                case Lune: {
                    Bitch();
                    break;
                }
                case Other: {
                    OMG();
                    break;
                }
                case OldPowerX: {
                    NC();
                    break;
                }
                case NewPowerX: {
                    SM();
                    break;
                }
                case Flux: {
                    FLux();
                    break;
                }
            }
        }
        GlStateManager.popMatrix();
    }

    EntityLivingBase lastTarget;
    EntityLivingBase target;
    ScaledResolution sr = new ScaledResolution(mc);

    boolean nulltarget = false;
    double healthLocationani;
    float x = sr.getScaledWidth() / 2.0f + hudx.getValue().floatValue();
    float y = sr.getScaledHeight() / 2.0f + hudY.getValue().floatValue();

    EntityLivingBase lasttarget=null;
    boolean keeptarget=false;
    MSTimer targettimer = new MSTimer();
    private void OMG() {
        if (target == null || !(target instanceof EntityPlayer) || mc.theWorld.getEntityByID(target.getEntityId()) == null || mc.theWorld.getEntityByID(target.getEntityId()).getDistanceSqToEntity(mc.thePlayer) > 100) {
            return;
        }
        final EntityPlayer enti = (EntityPlayer) target;
        final double distan = mc.thePlayer.getDistanceToEntity(target);

        this.health = (float) MathUtil.lerp(this.health, enti.getHealth(), 0.1);
        final String distance = String.valueOf(distan).split("\\.")[0] + "." + String.valueOf(distan).split("\\.")[1].charAt(0);
        final int tx = (int) (ScaledResolution.getScaledWidth()  + hudx.getValue().floatValue());
        final int ty = (int) (ScaledResolution.getScaledHeight()  + hudY.getValue().floatValue());

        DrawUtil.color(new Color(255, 255, 255, 255));

        if (target instanceof EntityLivingBase)
            GuiInventory.drawEntityOnScreen((int) (tx / 2F) + 54 + 16, (int) (ty / 2F) + 54 + 65, 32, 0, 0, (EntityLivingBase) target);

        Gui.drawRect(tx / 2F + 50, ty / 2F + 50, tx / 2F + 50 + 160, ty / 2F + 50 + 80, 0xcc000000);

        for (int i = 0; i < (this.health / ((EntityPlayer) target).getMaxHealth()) * 160; i++) {
            DrawUtil.rect(tx / 2F + 50 + i, ty / 2F + 50 + 78.5, 1, 1.5, new Color(ColorUtils.getStaticColor(i / 8f, 0.7f, 1)));
        }

        FontLoaders.SF24.drawString(((EntityPlayer) target).getName(), (tx / 2F) + 54 + 35, ty / 2F + 50 + 6, Color.WHITE.getRGB());

        if (enti.getHealth() / enti.getMaxHealth() <= mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth())
            FontLoaders.SF24.drawString("Winning", (tx / 2F) + 54 + 35, ty / 2F + 50 + 45 + FontLoaders.SF24.getHeight(), Color.WHITE.getRGB());
        else
            FontLoaders.SF24.drawString("Losing", (tx / 2F) + 54 + 35, ty / 2F + 50 + 45 + FontLoaders.SF24.getHeight(), Color.WHITE.getRGB());

        FontLoaders.SF18.drawString("Dist: " + distance, (float) ((tx / 2F) + 54 + 35.5), ty / 2F + 50 + FontLoaders.SF24.getHeight() + 6, Color.WHITE.getRGB());
        FontLoaders.SF18.drawString("Hurt Resistant Time: " + enti.hurtResistantTime, (float) ((tx / 2F) + 54 + 35.5), ty / 2F + 50 + FontLoaders.SF24.getHeight() + 6 + FontLoaders.SF18.getHeight() + 1, Color.WHITE.getRGB());
    }
    private void NM() {
        if (target != null) {
            GlStateManager.pushMatrix();
            // Width and height
            final float width = mc.displayWidth / (float) (mc.gameSettings.guiScale * 2) + 680+ hudx.getValue().floatValue();
            final float height = mc.displayHeight / (float) (mc.gameSettings.guiScale * 2) + 280+ hudY.getValue().floatValue();
            GlStateManager.translate(width - 660, height - 160.0f - 90.0f, 0.0f);
            // Border rect.
            RenderUtil.rectangle(2, -6, 156.0, 47.0, new Color(25, 25, 25).getRGB());
            // Main rect.
            RenderUtil.rectangle(4, -4, 154.0, 45.0, new Color(45, 45, 45).getRGB());
            // Draws name.
            mc.fontRendererObj.drawStringWithShadow(((EntityPlayer) target).getName(), 46f, 0.3f, -1);
            // Gets health.
            final float health = ((EntityPlayer) target).getHealth();
            // Color stuff for the healthBar.
            final float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
            final Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
            // Max health.
            final float progress = health / ((EntityPlayer) target).getMaxHealth();
            // Color.
            final Color healthColor = health >= 0.0f ? ColorUtils.blendColors(fractions, colors, progress).brighter() : Color.RED;
            // $$ draws the 4 fucking boxes killing my self btw. $$
            DrawUtil.rect(45, 11, 20, 20, new Color(25, 25, 25));
            DrawUtil.rect(46, 12, 18, 18, new Color(95, 95, 95));
            DrawUtil.rect(67, 11, 20, 20, new Color(25, 25, 25));
            DrawUtil.rect(68, 12, 18, 18, new Color(95, 95, 95));
            DrawUtil.rect(89, 11, 20, 20, new Color(25, 25, 25));
            DrawUtil.rect(90, 12, 18, 18, new Color(95, 95, 95));
            DrawUtil.rect(111, 11, 20, 20, new Color(25, 25, 25));
            DrawUtil.rect(112, 12, 18, 18, new Color(95, 95, 95));
            // Draws the current ping/ms.
            NetworkPlayerInfo networkPlayerInfo = mc.getNetHandler().getPlayerInfo(target.getUniqueID());
            final String ping = (Objects.isNull(networkPlayerInfo) ? "0ms" : networkPlayerInfo.getResponseTime() + "ms");
            GlStateManager.pushMatrix();
            GlStateManager.scale(0.6, 0.6, 0.6);
            mc.fontRendererObj.drawCenteredStringWithShadow(ping, 240, 40, Color.WHITE.getRGB());
            GlStateManager.popMatrix();
            // Draws the ping thingy from tab. :sunglasses:
            if (target != null && networkPlayerInfo != null) GuiPlayerTabOverlay.drawPing(103, 50, 14, networkPlayerInfo);
            // Round.
            double cockWidth = 0.0;
            cockWidth = MathUtil.round(cockWidth, (int) 5.0);
            if (cockWidth < 50.0) {
                cockWidth = 50.0;
            }
            // Bar behind healthbar.
            RenderUtil.rectangle(6.5, 37.3, 151, 43, Color.RED.darker().darker().getRGB());
            final double healthBarPos = cockWidth * (double) progress;
            // health bar.
            DrawUtil.rect(6f, 37.3f, (healthBarPos * 2.9), 6f, healthColor);
            // Gets the armor thingy for the bar.
            float armorValue = ((EntityPlayer) target).getTotalArmorValue();
            double armorWidth = armorValue / 20D;
            // Bar behind armor bar.
            DrawUtil.rect(45.5f, 32.3f, 105, 2.5f, new Color(0, 0, 255));
            // Armor bar.
            DrawUtil.rect(45.5f, 32.3f, (105 * armorWidth), 2.5f, new Color(0, 45, 255));
            // White rect around head.
            DrawUtil.rect(6, -2, 37, 37, new Color(205, 205, 205));
            // Draws head.
            renderPlayerModelTexture(7, -1, 3, 3, 3, 3, 35, 35, 24, 24, (AbstractClientPlayer) target);
            // Draws armor.
            GlStateManager.scale(1.1, 1.1, 1.1);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            // Draw targets armor the worst way possible.
            if (target != null) drawHelmet(24, 11); drawChest(44, 11); drawLegs(64, 11); drawBoots(84, 11);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
    private void NMSL() {
        final float nameWidth = 38;
        final float posX = x  - nameWidth - 45 + 80;
        if (target == null || !(target instanceof EntityPlayer)) {
            particles.clear();
            return;
        }
        if (scale == 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate((posX + 38 + 2 + 129 / 2f) * (1 - scale), (y - 34 + 48 / 2f) * (1 - scale), 0);
        GlStateManager.scale(scale, scale, 0);
        final EntityPlayer en = (EntityPlayer) target;
        final double dist = mc.thePlayer.getDistanceToEntity(target);

        final String name = ((EntityPlayer) target).getName();
        //Background
        if (black.getValue()) DrawUtil.roundedRect(posX + 38 + 2, y - 34, 129, 48, 8, new Color(0, 0, 0, 110));

        GlStateManager.popMatrix();

        final int scaleOffset = (int) (((EntityPlayer) target).hurtTime * 0.35f);

        for (final Particle p : particles) {
            if (p.opacity > 4) p.render2D();
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate((posX + 38 + 2 + 129 / 2f) * (1 - scale), (y - 34 + 48 / 2f) * (1 - scale), 0);
        GlStateManager.scale(scale, scale, 0);

        //Renders face
        if (target instanceof AbstractClientPlayer) {
            //offset other colors aside from red, so the face turns red
            final double offset = -(((AbstractClientPlayer) target).hurtTime * 23);
            //sets color to red
            DrawUtil.color(new Color(255, (int) (255 + offset), (int) (255 + offset)));
            //renders face
            renderPlayerModelTexture(posX + 38 + 6 + scaleOffset / 2f, y - 34 + 5 + scaleOffset / 2f, 3, 3, 3, 3, 30 - scaleOffset, 30 - scaleOffset, 24, 24.5f, (AbstractClientPlayer) en);
            //renders top layer of face
            renderPlayerModelTexture(posX + 38 + 6 + scaleOffset / 2f, y - 34 + 5 + scaleOffset / 2f, 15, 3, 3, 3, 30 - scaleOffset, 30 - scaleOffset, 24, 24.5f, (AbstractClientPlayer) en);
            //resets color to white
            DrawUtil.color(Color.WHITE);
        }

        final double fontHeight = FontLoaders.SF18.getHeight();

        FontLoaders.SF18.drawString("Distance: " + MathUtil.round(dist, 1), posX + 38 + 6 + 30 + 3, y - 34 + 5 + 15 + 2, Color.WHITE.hashCode());

        GlStateManager.pushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        DrawUtil.scissor(posX + 38 + 6 + 30 + 3, y - 34 + 5 + 15 - fontHeight, 91, 30);

        FontLoaders.SF18.drawString("Name: " + name, posX + 38 + 6 + 30 + 3, (float) (y - 34 + 5 + 15 - fontHeight), Color.WHITE.hashCode());

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        if (!String.valueOf(((EntityPlayer) target).getHealth()).equals("NaN"))
            health = Math.min(20, ((EntityPlayer) target).getHealth());

        if (String.valueOf(displayHealth).equals("NaN")) {
            displayHealth = (float) (Math.random() * 20);
        }

        final int speed = 6;
        if (timer.hasReached(1000 / 60)) {
            displayHealth = (displayHealth * (speed - 1) + health) / speed;

            ticks += 1;

            for (final Particle p : particles) {
                p.updatePosition();

                if (p.opacity < 1) particles.remove(p);
            }

            timer.reset();
        }

        float offset = 6;
        final float drawBarPosX = posX + nameWidth;

        if (displayHealth > 0.1)
            for (int i = 0; i < displayHealth * 4; i++) {
                    int color = new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue()).getRGB();;
                    final Color color1 = new Color(78, 161, 253, 100);
                    final Color color2 = new Color(78, 253, 154, 100);
                    color = ColorUtils.mixColors(color1, color2, (Math.sin(DrawUtil.ticks + posX * 0.4f + i * 0.6f / 14f) + 1) * 0.5f).hashCode();
                Gui.drawRect(drawBarPosX + offset, y + 5, drawBarPosX + 1 + offset * 1.25, y + 10, color);
                offset += 1;
            }
        if ((((EntityPlayer) target).hurtTime == 9 && !sentParticles) || (lastTarget != null && ((EntityPlayer) lastTarget).hurtTime == 9 && !sentParticles)) {
            final Color color21 = new Color(190, 0, 255, 100);
            final Color color11 = new Color(0, 190, 255, 100);
            for (int i = 0; i <= 15; i++) {
                final Particle p = new Particle();
                final Color c;
                c = ColorUtils.mixColors(color11, color21, (Math.sin(DrawUtil.ticks + posX * 0.4f + i) + 1) * 0.5f);
                p.init(posX + 55, y - 15, ((Math.random() - 0.5) * 2) * 1.4, ((Math.random() - 0.5) * 2) * 1.4, Math.random() * 4, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (((EntityPlayer) target).hurtTime == 8) sentParticles = false;

        if (lastTarget != target) {
            lastTarget = target;
        }

        final ArrayList<Particle> removeList = new ArrayList<>();
        for (final Particle p : particles) {
            if (p.opacity <= 1) {
                removeList.add(p);
            }
        }

        for (final Particle p : removeList) {
            particles.remove(p);
        }
        GlStateManager.popMatrix();
        timeUtil.reset();
    }

    private void CSB() {
        if (target == null || !(target instanceof EntityPlayer) || mc.theWorld.getEntityByID(target.getEntityId()) == null || mc.theWorld.getEntityByID(target.getEntityId()).getDistanceSqToEntity(mc.thePlayer) > 100) {
            particles.clear();
            return;
        }
        final int tx = (int) (ScaledResolution.getScaledWidth()  + hudx.getValue().floatValue());
        final int ty = (int) (ScaledResolution.getScaledHeight()  + hudY.getValue().floatValue());

        final EntityPlayer ent = (EntityPlayer) target;
        final double dista = mc.thePlayer.getDistanceToEntity(target);

        final float clampedHealthValue = MathHelper.clamp_float(ent.getHealth(), 0, ent.getMaxHealth());
        final double enHeartsValue = (clampedHealthValue / ent.getMaxHealth()) * 20.0;
        final float normalizedenHealthValue = clampedHealthValue / ent.getMaxHealth();
        final String enHearts = String.valueOf(enHeartsValue).split("\\.")[0] + "." + String.valueOf(enHeartsValue).split("\\.")[1].charAt(0);
        final String enDistance = String.valueOf(dista).split("\\.")[0] + "." + String.valueOf(dista).split("\\.")[1].charAt(0);

        Gui.drawRect(((tx - 150)) / 2F, (ty / 2F + 205) - 60, ((tx + 150)) / 2F, (ty / 2F + 165) - 60, 0xBB000000);

        FontLoaders.SF18.drawString(ent.getName(), ((tx - 70)) / 2F, (ty / 2F + 169) - 60, -1);
        FontLoaders.SF18.drawString("Dist: " + enDistance, ((tx - 70)) / 2F, (ty / 2F + 179) - 60, -1);
        FontLoaders.SF18.drawString(enHearts, ((tx + 149)) / 2F - FontLoaders.SF18.getStringWidth(enHearts), (ty / 2F + 184) - 60, -1);

        Gui.drawRect(((tx - 70)) / 2F, (ty / 2F + 202) - 60, ((tx + 146)) / 2F, (ty / 2F + 199) - 60, 0xFF353535);
        Gui.drawRect(((tx - 70)) / 2F, (ty / 2F + 197) - 60, ((tx + 146)) / 2F, (ty / 2F + 194) - 60, 0xFF353535);

        DrawUtil.renderGradientRectLeftRight(((tx - 70)) / 2, (int) (ty / 2F + 194) - 60, (int) ((tx - 70 + (216 * normalizedenHealthValue))) / 2, (int) (ty / 2F + 197) - 60, new Color(0xFF078301).darker().getRGB(), 0xFF00FF50);
        DrawUtil.renderGradientRectLeftRight(((tx - 70)) / 2, (int) (ty / 2F + 199) - 60, (int) ((tx - 70 + (216 * (ent.getTotalArmorValue() / 20f)))) / 2, (int) (ty / 2F + 202) - 60, 0xFF0050FF, 0xFF00FFFF);

        //offset other colors aside from red, so the face turns red
        final double offset = -(ent.hurtTime * 23);
        //sets color to red
        DrawUtil.color(new Color(255, (int) (255 + offset), (int) (255 + offset)));

        //Renders face
        if (target instanceof AbstractClientPlayer) {
            //renders face
            renderPlayerModelTexture(((tx - 146)) / 2f, (ty / 2F + 167) - 60, 3, 3, 3, 3, 36, 36, 24, 24, (AbstractClientPlayer) ent);
            //renders top layer of face
            renderPlayerModelTexture(((tx - 146)) / 2f, (ty / 2F + 167) - 60, 15, 3, 3, 3, 36, 36, 24, 24, (AbstractClientPlayer) ent);
        } else {
            // renders face
            renderSteveModelTexture(((tx - 146)) / 2f, (ty / 2F + 167) - 60, 3, 3, 3, 3, 36, 36, 24, 24);
            // renders top layer of face
            renderSteveModelTexture(((tx - 146)) / 2f, (ty / 2F + 167) - 60, 15, 3, 3, 3, 36, 36, 24, 24);
        }

        //resets color to white
        DrawUtil.color(Color.WHITE);
    }
    private void NaoCan() {
        if (target == null || !(target instanceof EntityPlayer) || mc.theWorld.getEntityByID(target.getEntityId()) == null || mc.theWorld.getEntityByID(target.getEntityId()).getDistanceSqToEntity(mc.thePlayer) > 100) {
            return;
        }
        GlStateManager.pushMatrix();
        // Width and height
        final float width = mc.displayWidth / (float) (mc.gameSettings.guiScale * 2) + 680;
        final float height = mc.displayHeight / (float) (mc.gameSettings.guiScale * 2) + 280;
        GlStateManager.translate(width - 660, height - 160.0f - 90.0f, 0.0f);
        // Draws the skeet rectangles.
        RenderUtil.rectangle(4, -2, mc.fontRendererObj.getStringWidth(((EntityPlayer) target).getName()) > 70.0f ? (124.0D + mc.fontRendererObj.getStringWidth(((EntityPlayer) target).getName()) - 70.0f) : 124.0, 37.0, new Color(0, 0, 0, 160).getRGB());
        // Draws name.
        mc.fontRendererObj.drawStringWithShadow(((EntityPlayer) target).getName(), 42.3f, 0.3f, -1);
        // Gets health.
        final float health = ((EntityPlayer) target).getHealth();
        // Gets health and absorption
        final float healthWithAbsorption = ((EntityPlayer) target).getHealth() + ((EntityPlayer) target).getAbsorptionAmount();
        // Color stuff for the healthBar.
        final float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
        final Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        // Max health.
        final float progress = health / ((EntityPlayer) target).getMaxHealth();
        // Color.
        final Color healthColor = health >= 0.0f ? ColorUtils.blendColors(fractions, colors, progress).brighter() : Color.RED;
        // Round.
        double cockWidth = 0.0;
        cockWidth = MathUtil.round(cockWidth, (int) 5.0);
        if (cockWidth < 50.0) {
            cockWidth = 50.0;
        }
        // Healthbar + absorption
        final double healthBarPos = cockWidth * (double) progress;
        RenderUtil.rectangle(42.5, 10.3, 53.0 + healthBarPos + 0.5, 13.5, healthColor.getRGB());
        if (((EntityPlayer) target).getAbsorptionAmount() > 0.0f) {
            RenderUtil.rectangle(97.5 - (double) ((EntityPlayer) target).getAbsorptionAmount(), 10.3, 103.5, 13.5, new Color(137, 112, 9).getRGB());
        }
        // Draws rect around health bar.
        RenderUtil.rectangleBordered(42.0, 9.8f, 54.0 + cockWidth, 14.0, 0.5, 0, Color.BLACK.getRGB());
        // Draws the lines between the healthbar to make it look like boxes.
        for (int dist = 1; dist < 10; ++dist) {
            final double cock = cockWidth / 8.5 * (double) dist;
            RenderUtil.rectangle(43.5 + cock, 9.8, 43.5 + cock + 0.5, 14.0, Color.BLACK.getRGB());
        }
        // Draw targets hp number and distance number.
        GlStateManager.scale(0.5, 0.5, 0.5);
        final int distance = (int) mc.thePlayer.getDistanceToEntity(target);
        final String nice = "HP: " + (int) healthWithAbsorption + " | Dist: " + distance;
        mc.fontRendererObj.drawString(nice, 85.3f, 32.3f, -1, true);
        GlStateManager.scale(2.0, 2.0, 2.0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // Draw targets armor and tools and weapons and shows the enchants.
        if (target != null) drawEquippedShit(28, 20);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        // Draws targets model.
        GlStateManager.scale(0.31, 0.31, 0.31);
        GlStateManager.translate(73.0f, 102.0f, 40.0f);
        DrawUtil.drawModel(target.rotationYaw, target.rotationPitch, (EntityLivingBase) target);
        GlStateManager.popMatrix();
    }
    private void ES() {
        if (target != null) {
            GlStateManager.pushMatrix();
            // Width and height
            final float width = x  + 680;
            final float height = y  + 280;
            GlStateManager.translate(width - 660, height - 160.0f - 90.0f, 0.0f);
            // Draws the skeet rectangles.
            DrawUtil.skeetRect(0, -2.0, FontLoaders.SF18.getStringWidth(((EntityPlayer) target).getName()) > 70.0f ? (double) (124.0f + FontLoaders.SF18.getStringWidth(((EntityPlayer) target).getName()) - 70.0f) : 124.0, 38.0, 1.0);
            DrawUtil.skeetRectSmall(0.0f, -2.0f, 124.0f, 38.0f, 1.0);
            // Draws name.
            FontLoaders.SF18.drawStringWithShadow(((EntityPlayer) target).getName(), 42.3f, 0.3f, -1);
            // Gets health.
            final float health = ((EntityPlayer) target).getHealth();
            // Gets health and absorption
            final float healthWithAbsorption = ((EntityPlayer) target).getHealth() + ((EntityPlayer) target).getAbsorptionAmount();
            // Color stuff for the healthBar.
            final float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
            final Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
            // Max health.
            final float progress = health / ((EntityPlayer) target).getMaxHealth();
            // Color.
            final Color healthColor = health >= 0.0f ? ColorUtils.blendColors(fractions, colors, progress).brighter() : Color.RED;
            // Round.
            double cockWidth = 0.0;
            cockWidth = MathUtil.round(cockWidth, (int) 5.0);
            if (cockWidth < 50.0) {
                cockWidth = 50.0;
            }
            // Healthbar + absorption
            final double healthBarPos = cockWidth * (double) progress;
            RenderUtil.rectangle(42.5, 10.3, 103, 13.5, healthColor.darker().darker().darker().darker().getRGB());
            RenderUtil.rectangle(42.5, 10.3, 53.0 + healthBarPos + 0.5, 13.5, healthColor.getRGB());
            if (((EntityPlayer) target).getAbsorptionAmount() > 0.0f) {
                RenderUtil.rectangle(97.5 - (double) ((EntityPlayer) target).getAbsorptionAmount(), 10.3, 103.5, 13.5, new Color(137, 112, 9).getRGB());
            }
            // Draws rect around health bar.
            RenderUtil.rectangleBordered(42.0, 9.8f, 54.0 + cockWidth, 14.0, 0.5, 0, Color.BLACK.getRGB());
            // Draws the lines between the healthbar to make it look like boxes.
            for (int dist = 1; dist < 10; ++dist) {
                final double cock = cockWidth / 8.5 * (double) dist;
                RenderUtil.rectangle(43.5 + cock, 9.8, 43.5 + cock + 0.5, 14.0, Color.BLACK.getRGB());
            }
            // Draw targets hp number and distance number.
            GlStateManager.scale(0.5, 0.5, 0.5);
            final int distance = (int) mc.thePlayer.getDistanceToEntity(target);
            final String nice = "HP: " + (int) healthWithAbsorption + " | Dist: " + distance;
            mc.fontRendererObj.drawString2(nice, 85.3f, 32.3f, -1, true);
            GlStateManager.scale(2.0, 2.0, 2.0);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            // Draw targets armor and tools and weapons and shows the enchants.
            if (target != null) drawEquippedShit(28, 20);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();
            // Draws targets model.
            GlStateManager.scale(0.31, 0.31, 0.31);
            GlStateManager.translate(73.0f, 102.0f, 40.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            DrawUtil.drawModel(target.rotationYaw, target.rotationPitch, (EntityLivingBase) target);
            GlStateManager.popMatrix();
        }
    }

        private void Fuck() {
        double hpClamped = target.getHealth() / target.getMaxHealth();
        hpClamped = MathHelper.clamp_double(hpClamped, 0.0, 1.0);
        final double hpWidth = 80 * hpClamped;
        healthBarWidth = DrawUtil.animateProgress(healthBarWidth, hpWidth, 75.f);
        double healthAnimatedPercent = 20.0 * (healthBarWidth / 80.0) * 5.0;
        //color hud
        final int startColour = ColourUtil.fadeBetween(ColourUtil.getClientColour(), new Color(HUD.r.getValue().intValue(),HUD.g.getValue().intValue(),HUD.b.getValue().intValue()).getRGB(), 0);
        final int endColour = ColourUtil.fadeBetween(new Color(HUD.r.getValue().intValue(),HUD.g.getValue().intValue(),HUD.b.getValue().intValue()).getRGB(), ColourUtil.getClientColour(), 250);
        //x y
        double x = (int) ((double) (ScaledResolution.getScaledWidth() / 2) + hudx.getValue());
        double y = (int) ((double) ScaledResolution.getScaledHeight() - hudY.getValue());
        double margin = 2.0f;
        double width = 85 + margin * 2.0f;
        String text = "Distance: " + format00.format(target.getDistanceToEntity(mc.thePlayer)) + " | Hurt: " + format0.format(target.hurtTime);
        //String text = "";
        final CFontRenderer fontRenderer = FontLoaders.GoogleSans18;
        // Draw TargetHud
        {
            // Top Gradient Bar
            //DrawUtil.glDrawSidewaysGradientRect(x + 39, y + 30, (float) healthBarWidth, 6.75f, startColour, endColour);
            // Top Gradient Bar
            DrawUtil.glDrawSidewaysGradientRect(x, y, width + 70, 1, startColour, endColour);
            // Background Rounded Rect - 60% Opacity
            DrawUtil.glDrawFilledQuad(x, y + 1, (float) width + 70,  42, 0x60 << 24);
            // Draw Face
            if (target instanceof EntityPlayer) {
                drawFace(x + 3, y + 5, 33, 33, (AbstractClientPlayer) target);
            }
            // Target Name
            mc.fontRendererObj.drawStringWithShadow(target.getName(), (float) (x + 39f), (float) (y + 6f), 0xFFFFFFFF);
            // Target Distance
            fontRenderer.drawStringWithShadow(text, x + 39f, y + 18f,  0xFF808080);
            // Health Bar
            DrawUtil.glDrawSidewaysGradientRect(x + 39, y + 30, (float) healthBarWidth, 6.75f, startColour, endColour);
            // Draw Health Value
            fontRenderer.drawStringWithShadow(format0.format(healthAnimatedPercent) + "%", x + healthBarWidth + hpClamped + 41, y + 31,  0xFFFFFFFF);
        }
    }
    //Bitch fuck me Omg!
    private void Bitch() {
        int targetx = (int) ((double) (ScaledResolution.getScaledWidth() / 2) + hudx.getValue());
        int targety = (int) ((double) ScaledResolution.getScaledHeight() - hudY.getValue());
        float anim = 150;
        if (target != null) {
            RenderUtil.drawRect(targetx + 35, targety + 40, targetx + 195, targety + 72, new Color(33, 36, 41, 255).getRGB());

            FontLoaders.Comfortaa18.drawCenteredStringWithShadow(target.getName(), targetx + 40 + 75, targety + 45, -1);

            if (anim < 150 * (target.getHealth() / target.getMaxHealth())) {
                anim = (int) (150 * (target.getHealth() / target.getMaxHealth()));
            } else if (anim > 150 * (target.getHealth() / target.getMaxHealth())) {
                anim -= 120f / mc.debugFPS;
            }

            RenderUtil.drawRect(targetx + 40, targety + 60, targetx + 40 + 150, targety + 65, new Color(target.hurtTime * 20, 0, 0).getRGB());
            RenderUtil.drawRect(targetx + 40 + 150 * (target.getHealth() / target.getMaxHealth()), targety + 60, targetx + 40 + anim, targety + 65, new Color(255, 100, 152).getRGB());
            RenderUtil.drawGradientSideways(targetx + 40, targety + 60, targetx + 40 + 150 * (target.getHealth() / target.getMaxHealth()), targety + 65, new Color(73, 148, 248).getRGB(), new Color(73, 200, 248).getRGB());
        }
    }
    private void CNM() {
        ScaledResolution sr = new ScaledResolution(mc);
        final FontRenderer font2 = mc.fontRendererObj;
        if (KillAura.currentTarget != null && Client.instance.getModuleManager().getModuleByClass(TargetHUD.class).isEnabled()
                & Client.instance.getModuleManager().getModuleByClass(KillAura.class).isEnabled()) {
            final String name = KillAura.currentTarget.getName()+ " ";
            font2.drawStringWithShadow(name, (float) (sr.getScaledWidth() / 2) - (font2.getStringWidth(name) / 2),
                    (float) (sr.getScaledHeight() / 2 - 30), -1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/gui/icons.png"));
            int i = 0;
            while ((float) i < KillAura.currentTarget.getMaxHealth() / 2.0f) {
                Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float) (sr.getScaledWidth() / 2)
                                - KillAura.currentTarget.getMaxHealth() / 2.0f * 10.0f / 2.0f + (float) (i * 10),
                        (float) (sr.getScaledHeight() / 2 - 16), 16, 0, 9, 9);
                ++i;
            }
            i = 0;
            while ((float) i < KillAura.currentTarget.getHealth() / 2.0f) {
                Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float) (sr.getScaledWidth() / 2)
                                - KillAura.currentTarget.getMaxHealth() / 2.0f * 10.0f / 2.0f + (float) (i * 10),
                        (float) (sr.getScaledHeight() / 2 - 16), 52, 0, 9, 9);
                ++i;
            }
        }
    }
    private void SM() {
        int x = (int) ((double) (ScaledResolution.getScaledWidth() / 2) + hudx.getValue());
        int y = (int) ((double) ScaledResolution.getScaledHeight() - hudY.getValue());
        double armorlecotion;
        if (!(target instanceof EntityPlayer)) {
            animWidth = 0;
            r2 = 0.0;
            return;
        }
        int modelWidth = 22;
        int height = 30;
        int width = 2 + Math.max(mc.fontRendererObj.getStringWidth(target.getName()) + 2, 98);
        String healthStr = String.valueOf(((int) target.getHealth()) / 2.0f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        RenderUtil.drawRect(0.0, 0.0, modelWidth + width, height, Colors.getColor(0, 155));
        GlStateManager.color(1.0f, 1.0f, 1.0f);
        String name = target.getDisplayName().getFormattedText();
        FontLoaders.Comfortaa18.drawStringWithShadow(name, modelWidth + 11, 5.0, -1);
        FontLoaders.Comfortaa12.drawStringWithShadow(healthStr, modelWidth + 81, 23.6, new Color(150, 150, 150).getRGB());
        FontLoaders.ICON10.drawStringWithShadow("s", modelWidth + 74, 25.0, new Color(87, 160, 250).getRGB());
        FontLoaders.ICON10.drawStringWithShadow("s", modelWidth + 11, 18.5, new Color(255, 85, 85).getRGB());
        FontLoaders.ICON10.drawStringWithShadow("r", modelWidth + 11, 25.5, new Color(153, 153, 153).getRGB());
        double healthLocation = (modelWidth + width - 2) * target.getHealth() / target.getMaxHealth();
        if (animWidth > healthLocation) {
            animWidth = MathUtils.getNextPostion(animWidth, (int) healthLocation, 2.0);
        }
        if (animWidth < healthLocation) {
            animWidth = MathUtils.getNextPostion(animWidth, (int) healthLocation, 2.0);
        }
        if (r2 > (armorlecotion = (modelWidth + width - 2) * target.getTotalArmorValue())) {
            r2 = MathUtils.getNextPostion((int) r2, (int) armorlecotion, 2.0);
        }
        if (r2 < armorlecotion) {
            r2 = MathUtils.getNextPostion((int) r2, (int) armorlecotion, 2.0);
        }
        RenderUtil.drawRect(modelWidth + 18, 17.0, (modelWidth + 18) + (modelWidth + width - 2) / 1.6, 19.0, new Color(0, 0, 0, 180).getRGB());
        RenderUtil.drawRect(modelWidth + 18, 17.0, (modelWidth + 18) + animWidth / 1.6, 19.0, ColorUtils.getTargetHudColor(target.getHealth(), target.getMaxHealth()));
        RenderUtil.drawRect(modelWidth + 18, 24.0, (modelWidth + 18) + (modelWidth + width - 2) / 2.4, 26.0, new Color(0, 0, 0, 180).getRGB());
        RenderUtil.drawRect(modelWidth + 18, 24.0, (modelWidth + 18) + r2 / 48.0, 26.0, new Color(87, 160, 250).getRGB());
        RenderUtil.drawNormalFace(target, 1.0f, 1.0f, 28.0f);
        GlStateManager.popMatrix();
    }
    private void FW() {
        if (target != null) {
            RenderUtil.renderTHUD((EntityPlayer) target);
        }
    }
    private void NC() {
        int x = (int) ((double) (ScaledResolution.getScaledWidth() / 2) + hudx.getValue());
        int y = (int) ((double) ScaledResolution.getScaledHeight() - hudY.getValue());
        EntityLivingBase player = target;
        if (player != null) {
            float f = 0;
            GlStateManager.pushMatrix();
            float xLeng = 144.0f;
            float yLeng = 52.0f;
            RenderUtil.rectangleBordered(x, y, x + xLeng, y + yLeng, 0.5, Colors.getColor(90), Colors.getColor(0));
            RenderUtil.rectangleBordered(x + 1.0f, y + 1.0f, x + xLeng - 1.0f, y + yLeng - 1.0f, 1.0, Colors.getColor(90), Colors.getColor(61));
            RenderUtil.rectangleBordered(x + 2.5, y + 2.5, (x + xLeng) - 2.5, (y + yLeng) - 2.5, 0.5, Colors.getColor(61), Colors.getColor(0));
            RenderUtil.rectangleBordered(x + 3.0f, y + 3.0f, x + xLeng - 3.0f, y + yLeng - 3.0f, 0.5, Colors.getColor(27), Colors.getColor(61));
            mc.fontRendererObj.drawStringWithShadow(player.getName(), x + 40, y + 6, -1);
            if (player instanceof EntityPlayer) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                GlStateManager.enableAlpha();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                List<NetworkPlayerInfo> var5 = GuiPlayerTabOverlay.field_175252_a.sortedCopy(mc.thePlayer.sendQueue.getPlayerInfoMap());
                for (NetworkPlayerInfo aVar5 : var5) {
                    if (mc.theWorld.getPlayerEntityByUUID(aVar5.getGameProfile().getId()) != player)
                        continue;
                    float size = 30.0f;
                    mc.getTextureManager().bindTexture(aVar5.getLocationSkin());
                    RenderUtil.drawScaledCustomSizeModalRect(x + 6, y + 6, 8.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
                    if (((EntityPlayer) player).isWearing(EnumPlayerModelParts.HAT)) {
                        RenderUtil.drawScaledCustomSizeModalRect(x + 6, y + 6, 40.0f, 8.0f, 8, 8, size, size, 64.0f, 64.0f);
                    }
                    GlStateManager.bindTexture(0);
                    break;
                }
                GlStateManager.popMatrix();
            }
            if (player instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) target;
                GlStateManager.pushMatrix();
                ArrayList<ItemStack> stuff = new ArrayList<>();
                int split = x + 19;
                int armorY = y + 16;
                int index = 3;
                while (index >= 0) {
                    ItemStack armer = entityPlayer.inventory.armorInventory[index];
                    if (armer != null) {
                        stuff.add(armer);
                    }
                    --index;
                }
                if (entityPlayer.getCurrentEquippedItem() != null) {
                    stuff.add(entityPlayer.getCurrentEquippedItem());
                }
                for (ItemStack errything : stuff) {
                    if (mc.theWorld != null) {
                        RenderHelper.enableGUIStandardItemLighting();
                        split += 20;
                    }
                    RenderUtil.rectangleBordered(split, armorY, split + 18.0, armorY + 18.0, 1.0, new Color(52, 52, 52, 150).getRGB(), Colors.BLACK.c);
                    GlStateManager.disableAlpha();
                    GlStateManager.clear(256);
                    mc.getRenderItem().zLevel = -150.0f;
                    mc.getRenderItem().renderItemAndEffectIntoGUI(errything, split + 1, armorY + 1);
                    mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, errything, split + 1, armorY + 1);
                    mc.getRenderItem().zLevel = 0.0f;
                    GlStateManager.disableBlend();
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    GlStateManager.disableDepth();
                    GlStateManager.disableLighting();
                    GlStateManager.enableDepth();
                    GlStateManager.scale(2.0f, 2.0f, 2.0f);
                    GlStateManager.enableAlpha();
                }
                GlStateManager.popMatrix();
            }
            BigDecimal bigDecimal = new BigDecimal(player.getHealth());
            float health = bigDecimal.setScale(1, 4).floatValue();
            float[] fractions = {0.0f, 0.2f, 0.7f};
            Color[] colors = {Color.RED, Color.YELLOW, Color.GREEN};
            float progress = health / player.getMaxHealth();
            Color customColor = health >= 0.0f ? blendColors(fractions, colors, progress).brighter() : Color.RED;
            double wdnmd = 98.0;
            float health2 = player.getHealth() / player.getMaxHealth();
            if (powerxHealthAnim < wdnmd * f) {
                if (wdnmd * health2 - powerxHealthAnim < 1.0) {
                    powerxHealthAnim = (float) (wdnmd * health2);
                }
                powerxHealthAnim = (float) (powerxHealthAnim + 2.0);
            }
            if (wdnmd * health2 - powerxHealthAnim > 1.0) {
                powerxHealthAnim = (float) (wdnmd * health2);
            }
            powerxHealthAnim = (float) (powerxHealthAnim - 2.0);
            if (powerxHealthAnim < 0.0f) {
                powerxHealthAnim = 0.0f;
            }
            RenderUtil.rectangleBordered(x + 39, y + mc.fontRendererObj.FONT_HEIGHT + 26, x + 137, y + mc.fontRendererObj.FONT_HEIGHT + 38, 1.0, 0, Colors.BLACK.c);
            Gui.drawRect(x + 40, y + mc.fontRendererObj.FONT_HEIGHT + 27, (x + 40) + powerxHealthAnim, y + mc.fontRendererObj.FONT_HEIGHT + 37, customColor.darker().getRGB());
            mc.fontRendererObj.drawStringWithShadow(EnumChatFormatting.RED + "\u2764" + EnumChatFormatting.RESET + health, x + 7.5f, y + 52.0f - mc.fontRendererObj.FONT_HEIGHT - 5.0f, customColor.darker().getRGB());
            GlStateManager.popMatrix();
        }
    }
    private void SB() {
        //SB code by Fw
        if (target != null) {
            int colors = new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(), 255).getRGB();
            int colors1 = new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(), 150).getRGB();
            int colors2 = new Color(HUD.r.getValue().intValue(), HUD.g.getValue().intValue(), HUD.b.getValue().intValue(), 50).getRGB();

            final float health = getHealthes(target);
            final float n18 = 125.0f * (health / target.getMaxHealth());
            animation = AnimationUtil.getAnimationState((float) animation, n18, 135.0f);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (ScaledResolution.getScaledWidth() / 2 + hudx.get() + 15), (float) (ScaledResolution.getScaledHeight() - hudY.get() - 55), 0.0F);
            GlStateManager.color(1, 1, 1);
            if (target instanceof EntityPlayer) {
                GuiInventory.drawEntityOnScreen(-18, 47, 30, -180, 0, target);
            } else {
                GuiInventory.drawEntityOnScreen(-20, 50, 30, -180, 0, target);
            }
            RenderUtil.MdrawRect(-38.0, -14.0, 133, 52.0, Colors.getColor(0, 0, 0, 180));
            mc.fontRendererObj.drawStringWithShadow(target.getName(), 0.0F, -8.0F, new Color(255, 255, 255).getRGB());
            RenderUtil.MdrawRect(0.0f, 8.0f + 40, 130f, 40f, colors2);
            if ((target.getHealth() / 2.0f + target.getAbsorptionAmount() / 2.0f) > 1.0) {
                RenderUtil.MdrawRect(0.0f, 8.0f + 40, animation + 5f, 40f, colors1);
            }
            RenderUtil.MdrawRect(0.0f, 8.0f + 40, animation, 40f, colors);
            GlStateManager.scale(3f, 3f, 3f);
            mc.fontRendererObj.drawStringWithShadow(getHealthes(target) + " \u2764", 0.0F, 2.5F, colors);
            GlStateManager.popMatrix();
        }
    }

    private void Flat() {

        //Distance TH code by TIQS
        int blackcolor = black.getValue() ? new Color(0, 0, 0, 180).getRGB() : new Color(200, 200, 200, 180).getRGB();
        int blackcolor2 = !black.getValue() ? new Color(0, 0, 0).getRGB() : new Color(200, 200, 200).getRGB();

        float health;
        double hpPercentage;
        Color hurt;
        int healthColor;
        if (nulltarget) {
            health = 0;
            hpPercentage = health / 20;
            hurt = Color.getHSBColor(300f / 360f, ((float) 0 / 10f) * 0.37f, 1f);
            healthColor = ColorUtils.getHealthColor(0, 20).getRGB();
        } else {
            health = target.getHealth();
            hpPercentage = health / target.getMaxHealth();
            hurt = Color.getHSBColor(310f / 360f, ((float) target.hurtTime / 10f), 1f);
            healthColor = ColorUtils.getHealthColor(target.getHealth(), target.getMaxHealth()).getRGB();
        }
        hpPercentage = MathHelper.clamp_double(hpPercentage, 0.0, 1.0);
        double hpWidth = 92.0 * hpPercentage;

        if (nulltarget) {
            this.healthBarWidth2 = AnimationUtil.moveUD((float) this.healthBarWidth2, 0, 7f / Minecraft.getDebugFPS(), 5f / Minecraft.getDebugFPS());
            this.healthBarWidth = AnimationUtils.animate(0, this.healthBarWidth, 14f / Minecraft.getDebugFPS());
        } else {
            this.healthBarWidth2 = AnimationUtil.moveUD((float) this.healthBarWidth2, (float) hpWidth, 7f / Minecraft.getDebugFPS(), 5f / Minecraft.getDebugFPS());
            this.healthBarWidth = AnimationUtils.animate(hpWidth, this.healthBarWidth, 14f / Minecraft.getDebugFPS());
        }
        width = 190.0f;

        height =55.0f;

        Pattern p=Pattern.compile( "[\u4e00-\u9fa5]" );
        Matcher m=p.matcher( target.getName() );



        RenderUtil.drawFastRoundedRect( x,y,x+width-40 ,y+height,10f,new Color(255,255,255,180).getRGB());
        if(m.find()){
            FontLoader.msFont18.drawString( target.getName(),x+50,y+10,new Color( 0,0,0 ).getRGB() );
        }else{

            FontLoaders.GoogleSans18.drawString( target.getName()!=null ?target.getName( ):"No Target", x+50,y+10,new Color( 0,0,0).getRGB());
            keeptarget=true;
        }
        if(target instanceof EntityPlayer&!nulltarget) {
            FontLoaders.GoogleSans14.drawString( "1",1,1,-1);
            mc.getTextureManager( ).bindTexture( ( ( AbstractClientPlayer ) target ).getLocationSkin( ) );
            Gui.drawScaledCustomSizeModalRect( ( int ) x + 6, ( int ) y + 6, 8.0F, 8.0F, 8, 8, 40, 40, 64, 64 );
        }

        int color;

        if (health > 20) {
            health = 20;
        }
        float[] fractions = new float[]{0f, 0.5f, 1f};
        Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        float progress = (health * 5) * 0.01f;
        Color customColor = ColorUtils.blendColors(fractions, colors, progress).brighter();
        color = customColor.getRGB();

//        RenderUtil.drawRect(width+200, height +100, width+200 + healthBarWidth2,  + 15, ColorUtils.getDarker(new Color(color), 60, 255).getRGB());
//        RenderUtil.drawRect(width+200, height  +100, width+200+ healthBarWidth2, height +80, color);
        int K=0;
        if(!(target.getHealth()==0)){
            RenderUtil.drawRect( x+50,y+22, ( float ) (x+50+healthBarWidth2),y+26,  Color.RED.getRGB() );
            RenderUtil.drawRect( x+50,y+22, ( float ) (x+50+healthBarWidth),y+26,  new Color( 30,178,79 ).getRGB() );
            // RenderUtil.drawFastRoundedRect( x+50, y+22, ( float ) ( x+50+healthBarWidth2),y+26,1,ColorUtils.getDarker( new Color( color ),60,255 ).getRGB());
        }

        float r7=target!=null?target.getTotalArmorValue():0;
        if(r7!=0){
            if(!m.find()){
                RenderUtil.drawFastRoundedRect((float) (x + 50),
                        (float) (y + 35),
                        (float) (x + 7+50 + r7*4.25),
                        y + 39,1,  new Color(87, 130, 189).getRGB());
            }
        }

    }

    //  }

    public void simple() {
        float w = x;
        float h = y;
        String NAME = nulltarget ? "(无目标)" : target.getName();
        String HEALTH = "H:" + (nulltarget ? "0" : df.format(target.getHealth()));
        int width = Math.max(70, FontLoader.msFont16.getStringWidth(NAME) + (FontLoader.msFont16.getStringWidth(HEALTH) / 2));
//        Blur.blurAreaBoarderXY(w, h + 15, w + width + 1, h + 30);
        this.width = width + 1;
        height = 15;

        RenderUtil.drawRect(w, h, w + width + 1, h + 15, new Color(15, 15, 15, 140).getRGB());
        FontLoader.msFont16.drawString(NAME, w + 3, h + 5, -1);
        float healthWidth = (float) ((width + 1) * MathHelper.clamp_double(nulltarget ? 0 : target.getHealth() / (nulltarget ? 20 : target.getMaxHealth()), 0, 1));
        if (lastTarget != target && !nulltarget) {
            lastTarget = target;
            healthBarWidth2 = healthWidth;
        }
        this.healthBarWidth2 = AnimationUtil.moveUD((float) this.healthBarWidth2, healthWidth, 10f / Minecraft.getDebugFPS(), 5f / Minecraft.getDebugFPS());

        int color;
        float health = nulltarget ? 0 : target.getHealth();
        if (health > 20) {
            health = 20;
        }
        float[] fractions = new float[]{0f, 0.5f, 1f};
        Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        float progress = (health * 5) * 0.01f;
        Color customColor = ColorUtils.blendColors(fractions, colors, progress).brighter();
        color = customColor.getRGB();
        RenderUtil.drawRect(w, h + 14, w + healthBarWidth2, h + 15, ColorUtils.getDarker(new Color(color), 60, 255).getRGB());
        RenderUtil.drawRect(w, h + 14, w + healthWidth, h + 15, color);
    }

    //Flux
    public void FLux() {
        ScaledResolution res = new ScaledResolution(mc);
        int x = res.getScaledWidth() / 2 + 30;
        int y = res.getScaledHeight() / 2 - 5;
        if (KillAura.target != null) {
            this.FluxBackground(target);
            this.FluxonName(target);
            this.FluxonHead();
            RenderUtil.drawRect(x - 0.3, y - 10, x + 20 + 0.3, y - 9.3, new Color(149, 255, 147).getRGB());

            RenderUtil.drawRect(x - 0.3, y - 10, x, y + 10, new Color(149, 255, 147).getRGB());

            RenderUtil.drawRect(x - 0.3, y + 10, x + 20 + 0.3, y + 10.3, new Color(149, 255, 147).getRGB());

            RenderUtil.drawRect(x + 20, y - 10, x + 20 + 0.6, y + 10, new Color(149, 255, 147).getRGB());
//            RenderUtil.drawRect(x - 0.5, y - 10, x + 20 + 0.5, y - 9.5, new Color(149,255,147).getRGB());
//            RenderUtil.drawRect(x - 0.5, y - 10, x + 20 + 0.5, y - 9.5, new Color(149,255,147).getRGB());
            if (ModuleManager.getModuleByClass(KillAura.class).isEnabled()) {

                EntityLivingBase target1 = KillAura.target;
                if (target1 != this.lastEnt && target1 != null) {
                    this.lastEnt = target1;
                }
                if (startAnim) {
                    stopAnim = false;
                }
                if (animAlpha == 255 && KillAura.target == null) {
                    stopAnim = true;
                }
                startAnim = KillAura.target != null;
                if (startAnim) {
                    if (animAlpha < 255) {
                        animAlpha += 15;
                    }
                }
                if (stopAnim) {
                    if (animAlpha > 0) {
                        animAlpha -= 15;
                    }
                }
                if (KillAura.target == null && animAlpha < 255) {
                    stopAnim = true;
                }
                EntityLivingBase player = null;
                if (lastEnt != null) {
                    player = lastEnt;
                }
                int c;
                if (player != null && animAlpha >= 135) {
                    double Width = getWidth(KillAura.target);
                    if (Width < 50.0) {
                        Width = 50.0;
                    }
                    final double healthLocation;


                    if (KillAura.target.getHealth() > 20)
                        healthLocation = 16 + rect;
                    else
                        healthLocation = ((16 + rect) / 20) * (int) KillAura.target.getHealth();

                    anim2 = AnimationUtil.moveUD(anim2, (float) healthLocation, 18f / Minecraft.getDebugFPS(), 5f / Minecraft.getDebugFPS());
                    int color = KillAura.target.getHealth() > 10.0f ? RenderUtil.blend(new Color(-16711936), new Color(-256), 1.0f / KillAura.target.getHealth() / 2.0f * (KillAura.target.getHealth() - 10.0f)).getRGB() : RenderUtil.blend(new Color(-256), new Color(-65536), 0.1f * KillAura.target.getHealth()).getRGB();

                    r2 = ((16 + rect) / 20) * KillAura.target.getTotalArmorValue();
                    //health


                    Gui.drawRect(x + 7,
                            y + 13,
                            x + 23 + rect,
                            y + 15, new Color(60, 60, 60).getRGB());

                    if (!((x + 7) == (x + 7 + anim2))) {
                        RenderUtil.drawFastRoundedRect(x + 7,
                                y + 13,
                                x + 7 + anim2,
                                y + 15, 1, new Color(255, 213, 0, 201).getRGB());
                    }
                    if (!((x + 7) == (x + 7 + healthLocation))) {
                        RenderUtil.drawFastRoundedRect((float) (x + 7),
                                (float) (y + 13),
                                (float) (x + 7 + healthLocation),
                                y + 15, 1, new Color(47, 190, 130).getRGB());
                    }


                    RenderUtil.drawFastRoundedRect((float) (x + 7),
                            (float) (y + 18),
                            (float) (x + 23 + rect),
                            y + 20,1, new Color(60, 60, 60).getRGB());


                    if (!((x + 7) == (x + 7 + r2))) {
                        RenderUtil.drawFastRoundedRect((float) (x + 7),
                                (float) (y + 18),
                                (float) (x + 7 + r2),
                                y + 20, 1, new Color(87, 130, 189).getRGB());
                    }

                }
            }
        }
    }

    private int getWidth(EntityLivingBase target) {
        return 38 + FontLoaders.GoogleSans18.getStringWidth(target.getName());
    }

    private void FluxBackground(EntityLivingBase target) {
        ScaledResolution res = new ScaledResolution(mc);
        int x = res.getScaledWidth() / 2 + 30;
        int y = res.getScaledHeight() / 2 - 5;

        double hea = target.getHealth();
        double f1 = new com.ibm.icu.math.BigDecimal(hea).setScale(1, com.ibm.icu.math.BigDecimal.ROUND_HALF_UP).doubleValue();

        if (FontLoaders.GoogleSans18.getStringWidth(target.getName()) > FontLoaders.GoogleSans14.getStringWidth("Health:" + f1 + ""))
            rect = FontLoaders.GoogleSans18.getStringWidth(target.getName());
        if (FontLoaders.GoogleSans18.getStringWidth(target.getName()) == FontLoaders.GoogleSans14.getStringWidth("Health:" + f1 + ""))
            rect = FontLoaders.GoogleSans18.getStringWidth(target.getName());
        if (FontLoaders.GoogleSans18.getStringWidth(target.getName()) < FontLoaders.GoogleSans14.getStringWidth("Health:" + f1 + ""))
            rect = FontLoaders.GoogleSans14.getStringWidth("Health:" + f1 + "");

        RenderUtil.drawFastRoundedRect(x - 3,
                y - 13,
                (int) (x + 25 + rect) + 1,
                y - 20 + 28 + 16, 2, new Color(40, 40, 40, 200).getRGB());
    }

    private void FluxonHead() {
        if (!(KillAura.target instanceof EntityPlayer)) {
            return;
        }
        ScaledResolution res = new ScaledResolution(mc);
        int x = res.getScaledWidth() / 2 + 30;
        int y = res.getScaledHeight() / 2 - 5;
        mc.getTextureManager().bindTexture(((AbstractClientPlayer) KillAura.target).getLocationSkin());

        Gui.drawScaledCustomSizeModalRect(x, y - 10, 8.0F, 8.0F, 8, 8, 20, 20, 64, 64);


    }

    private void FluxonName(EntityLivingBase target) {
        ScaledResolution res = new ScaledResolution(mc);
        int x = res.getScaledWidth() / 2 + 30;
        int y = res.getScaledHeight() / 2 + 30;
        final CFontRenderer font2 = FontLoaders.GoogleSans14;

        final CFontRenderer font3 = FontLoaders.ICON10;
        Client.FontLoaders.Chinese18.drawStringWithShadow(target.getName(), x + 20 + 3, y - 45 + 3, -1);

        double hea = target.getHealth();

        String str1 = String.format("%.1f", hea);

        double f1 = new com.ibm.icu.math.BigDecimal(hea).setScale(1, com.ibm.icu.math.BigDecimal.ROUND_HALF_UP).doubleValue();

        font2.drawStringWithShadowNew("", x - 20 + 50 + 2 + 2 + font2.getStringWidth("Health: " + f1 + ""), y - 25 + 20 + 6, -1);
        font2.drawStringWithShadowNew("Health:" + f1 + "", x + 20 + 2, y - 40 + font2.getStringHeight("A") + 2, -1);

        font3.drawString("s", x, y - 30 + FontLoaders.GoogleSans18.getStringHeight("A") + 1, Color.getHSBColor(1f, KillAura.target.hurtTime / 10f, 0.9f).getRGB());

        font3.drawString("r", x, y - 30 + FontLoaders.GoogleSans18.getStringHeight("A") + 6.5f, -1);
    }

    public static int[] getFractionIndicies(float[] fractions, float progress) {
        int[] range = new int[2];

        int startPoint;
        for (startPoint = 0; startPoint < fractions.length && fractions[startPoint] <= progress; ++startPoint) {

        }

        if (startPoint >= fractions.length) {
            startPoint = fractions.length - 1;
        }

        range[0] = startPoint - 1;
        range[1] = startPoint;
        return range;
    }
    public static void renderSteveModelTexture(final double x, final double y, final float u, final float v, final int uWidth, final int vHeight, final int width, final int height, final float tileWidth, final float tileHeight) {
        final ResourceLocation skin = new ResourceLocation("textures/entity/steve.png");
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        GL11.glEnable(GL11.GL_BLEND);
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glDisable(GL11.GL_BLEND);
    }
    public static void renderPlayerModelTexture(final double x, final double y, final float u, final float v, final int uWidth, final int vHeight, final int width, final int height, final float tileWidth, final float tileHeight, final AbstractClientPlayer target) {
        final ResourceLocation skin = target.getLocationSkin();
        Minecraft.getMinecraft().getTextureManager().bindTexture(skin);
        GL11.glEnable(GL11.GL_BLEND);
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glDisable(GL11.GL_BLEND);
    }
    // It's a retarded way to do, but I couldn't figure how to space them proper. (I'll improve this some other time can't be asked rn)
    private void drawHelmet(final int x, final int y) {
        if (target == null || !(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        final ItemStack helmet = ((EntityPlayer) target).getCurrentArmor(3);
        if (helmet != null) {
            stuff.add(helmet);
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 20;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        GL11.glPopMatrix();
    }

    private void drawChest(final int x, final int y) {
        if (target == null || !(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        final ItemStack chest = ((EntityPlayer) target).getCurrentArmor(2);
        if (chest != null) {
            stuff.add(chest);
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 20;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        GL11.glPopMatrix();
    }
    private void drawEquippedShit(final int x, final int y) {
        if (target == null || !(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        for (int geraltOfNigeria = 3; geraltOfNigeria >= 0; --geraltOfNigeria) {
            final ItemStack armor = ((EntityPlayer) target).getCurrentArmor(geraltOfNigeria);
            if (armor != null) {
                stuff.add(armor);
            }
        }
        if (((EntityPlayer) target).getHeldItem() != null) {
            stuff.add(((EntityPlayer) target).getHeldItem());
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, yes, cock + x, y);
            DrawUtil.renderEnchantText(yes, cock + x, (y + 0.5f));
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            yes.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
    }

    private void drawLegs(final int x, final int y) {
        if (target == null || !(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        final ItemStack legs = ((EntityPlayer) target).getCurrentArmor(1);
        if (legs != null) {
            stuff.add(legs);
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 20;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        GL11.glPopMatrix();
    }

    private void drawBoots(final int x, final int y) {
        if (target == null || !(target instanceof EntityPlayer)) return;
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        final ItemStack boots = ((EntityPlayer) target).getCurrentArmor(0);
        if (boots != null) {
            stuff.add(boots);
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 20;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + x, y);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        GL11.glPopMatrix();
    }
    public static Color blendColors(float[] fractions, Color[] colors, float progress) {
        Color color = null;
        if (fractions == null) {
            throw new IllegalArgumentException("Fractions can\'t be null");
        } else if (colors == null) {
            throw new IllegalArgumentException("Colours can\'t be null");
        } else if (fractions.length == colors.length) {
            int[] indicies = getFractionIndicies(fractions, progress);
            float[] range = new float[]{fractions[indicies[0]], fractions[indicies[1]]};
            Color[] colorRange = new Color[]{colors[indicies[0]], colors[indicies[1]]};
            float max = range[1] - range[0];
            float value = progress - range[0];
            float weight = value / max;
            color = blend(colorRange[0], colorRange[1], (double) (1.0F - weight));
            return color;
        } else {
            throw new IllegalArgumentException("Fractions and colours must have equal number of elements");
        }
    }

    public static Color blend(Color color1, Color color2, double ratio) {
        float r = (float) ratio;
        float ir = 1.0F - r;
        float[] rgb1 = new float[3];
        float[] rgb2 = new float[3];
        color1.getColorComponents(rgb1);
        color2.getColorComponents(rgb2);
        float red = rgb1[0] * r + rgb2[0] * ir;
        float green = rgb1[1] * r + rgb2[1] * ir;
        float blue = rgb1[2] * r + rgb2[2] * ir;
        if (red < 0.0F) {
            red = 0.0F;
        } else if (red > 255.0F) {
            red = 255.0F;
        }

        if (green < 0.0F) {
            green = 0.0F;
        } else if (green > 255.0F) {
            green = 255.0F;
        }

        if (blue < 0.0F) {
            blue = 0.0F;
        } else if (blue > 255.0F) {
            blue = 255.0F;
        }

        Color color3 = null;

        try {
            color3 = new Color(red, green, blue);
        } catch (IllegalArgumentException var14) {
            NumberFormat nf = NumberFormat.getNumberInstance();
            System.out.println(nf.format((double) red) + "; " + nf.format((double) green) + "; " + nf.format((double) blue));
            var14.printStackTrace();
        }

        return color3;
    }

    private double getIncremental(double val, double inc) {
        double one = 1.0D / inc;
        return (double) Math.round(val * one) / one;
    }

    private static float getHealthes(EntityLivingBase entityLivingBase) {
        return (int) (((int) Math.ceil(entityLivingBase.getHealth())) + 0.5f);
    }
    enum AnimMode{
        Slide,
        Scale
    }

    enum Modes {
        Distance,
        Astolfo,
        Novoline,
        NewPowerX,
        OldPowerX,
        Flux,
        Lune,
        Other,
        Exhibition,
        OldExhibition,
        Rosalba,
        Remix,
        Classic,
        Rise,
        Vanilla,
        Simple
    }
}


