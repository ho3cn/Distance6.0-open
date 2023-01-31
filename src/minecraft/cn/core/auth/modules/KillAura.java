package cn.core.auth.modules;


import cn.distance.Client;
import cn.distance.api.EventHandler;
import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.events.Render.EventRender3D;
import cn.distance.api.events.World.*;
import cn.distance.api.value.Mode;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.manager.FriendManager;
import cn.distance.manager.ModuleManager;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.module.modules.combat.AntiBot;
import cn.distance.module.modules.world.Teams;
import cn.distance.ui.notifications.user.Notifications;
import cn.distance.util.RayTraceUtil2;
import cn.distance.util.math.RotationUtil;
import cn.distance.util.misc.Helper;
import cn.distance.util.render.RenderUtil;
import cn.distance.util.rotations.*;
import cn.distance.util.time.TimeHelper;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class KillAura extends Module {
    public static Option autoBlock = new Option("AutoBlock", true);
    public Mode priority = new Mode("Priority", Prioritymode.values(), Prioritymode.Range);
    public Mode rotMode = new Mode("RotationMode", Rotationmode.values(), Rotationmode.Hypixel);
    public Numbers<Double> hurttime = new Numbers<Double>("HurtTime", 10.0, 1.0, 10.0, 1.0);
    public Numbers<Double> mistake = new Numbers<Double>("Mistakes", 0.0, 0.0, 100.0, 1d);
    public Mode blockMode = new Mode("BlockMode", BlockMode.values(), BlockMode.Hypixel);
    public static Numbers<Double> reach = new Numbers<Double>("Range", 4.2, 3.0, 10.0, 0.1);
    public Numbers<Double> blockReach = new Numbers<Double>("BlockRange", 0.5, 0.0, 3.0, 0.1);
    public Numbers<Double> cpsMax = new Numbers<Double>("CPSMax", 10.0, 1.0, 20.0, 1.0);
    public Numbers<Double> cpsMin = new Numbers<Double>("CPSMin", 8.0, 1.0, 20.0, 1.0);
    public Numbers<Double> turn = new Numbers<Double>("TurnHeadSpeed", 15.0, 5.0, 120.0, 1.0);
    public static Numbers<Double> switchsize = new Numbers<Double>("MaxTargets", 1.0, 1.0, 5.0, 1.0);
    public Numbers<Double> switchDelay = new Numbers<Double>("SwitchDelay", 50d, 0d, 2000d, 10d);
    public Numbers<Double> yawDiff = new Numbers<Double>("YawDifference", 15.0, 5.0, 90.0, 1.0);
    public Option throughblock = new Option("ThroughBlock", true);
    public Option rotations = new Option("HeadRotations", true);
    public static Option attackPlayers = new Option("Players", true);
    public static Option attackAnimals = new Option("Animals", false);
    public static Option attackMobs = new Option("Mobs", false);
    public static Option invisible = new Option("Invisibles", false);
    public final Option toggleWhenDeadValue = new Option("DisableOnDeath", true);
    public Option esp = new Option("ESP", true);
    public Option RayCast = new Option("RayCast", true);

    public KillAura() {
        super("KillAura", new String[]{"ka"}, ModuleType.Combat);
        addValues(blockMode, priority, rotMode, cpsMax, cpsMin, reach, blockReach, mistake, hurttime, turn, yawDiff, switchsize, switchDelay, rotations, autoBlock, attackPlayers, attackAnimals, attackMobs
                , invisible,toggleWhenDeadValue, RayCast, esp, throughblock);
        attacked = new CopyOnWriteArrayList<EntityLivingBase>();

    }

    public static boolean blockingStatus = false;
    public static boolean fakeblockstatus = false;
    private boolean isAttacking;

    // Utils
    public static CopyOnWriteArrayList<EntityLivingBase> targets = new CopyOnWriteArrayList();
    public Random random = new Random();
    public static CopyOnWriteArrayList<EntityLivingBase> attacked = new CopyOnWriteArrayList();
    public static EntityLivingBase curBot = null;
    public static EntityLivingBase currentTarget;

    public boolean needBlock = false;
    public boolean needUnBlock = false;
    public int index;

    public static EntityLivingBase target = null;
    public static EntityLivingBase needHitBot = null;

    // TimeHelper
    public TimeHelper switchTimer = new TimeHelper();
    public TimeHelper attacktimer = new TimeHelper();
    public TimeHelper TickexistCharge = new TimeHelper();

    // תͷ
    AxisAlignedBB axisAlignedBB;
    float shouldAddYaw;
    float[] lastRotation = new float[]{0f, 0f};
    private float rotationYawHead;

    private float[] lastRotations;

    boolean Crit = false;

    float curHealthX = 0f;
    float curAbsorptionAmountX = 0f;
    float curY = new ScaledResolution(mc).getScaledHeight();


    private float yaw;
    private float pitch;


    @EventHandler
    public void targetHud( EventRender2D event) {
        String str;
        if (switchsize.getValue() <= 1) {
            str = "Single";
        } else {
            str = "Switch";
        }
        setSuffix(str);
    }
    @EventHandler
    public void Event( EventWorldChanged e){
        if (toggleWhenDeadValue.getValue()) {
            Notifications.getManager().post("KillAura", "检测到世界变更！已自动关闭KillAura");
            this.setEnabled(false);

        }
    }
    public boolean getBlockingStatus(){
        return blockingStatus;
    }
    public boolean getfakeblockstatus(){
        return fakeblockstatus;
    }
    @EventHandler
    public void onRender( EventRender3D render) { // Copy
        if (target == null || !esp.getValue()) {
            return;
        }
        final Color color2;
        if(KillAura.currentTarget==null)return;
        final Color color = color2 = ((KillAura.currentTarget.hurtTime > 0) ? new Color(-1618884) : new Color(-13330213));
        RenderManager renders=KillAura.mc.getRenderManager();
        double x = KillAura.currentTarget.lastTickPosX + (KillAura.currentTarget.posX - KillAura.currentTarget.lastTickPosX) * KillAura.mc.timer.renderPartialTicks - renders.renderPosX;
        renders=KillAura.mc.getRenderManager();
        double y = KillAura.currentTarget.lastTickPosY + (KillAura.currentTarget.posY - KillAura.currentTarget.lastTickPosY) * KillAura.mc.timer.renderPartialTicks - renders.renderPosY;
        renders=KillAura.mc.getRenderManager();
        double z = KillAura.currentTarget.lastTickPosZ + (KillAura.currentTarget.posZ - KillAura.currentTarget.lastTickPosZ) * KillAura.mc.timer.renderPartialTicks - renders.renderPosZ;
        if (KillAura.currentTarget instanceof EntityPlayer) {
            x -= 0.5;
            z -= 0.5;
            y += KillAura.currentTarget.getEyeHeight() + 0.35 - (KillAura.target.isSneaking() ? 0.25 : 0.0);
            final double mid = 0.5;
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            final double rotAdd = -0.25 * (Math.abs(KillAura.currentTarget.rotationPitch) / 90.0f);
            GL11.glTranslated(x + 0.5, y + 0.5, z + 0.5);
            GL11.glRotated((double) (-KillAura.currentTarget.rotationYaw % 360.0f), 0.0, 1.0, 0.0);
            GL11.glTranslated(-(x + 0.5), -(y + 0.5), -(z + 0.5));
            GL11.glDisable(3553);
            GL11.glEnable(2848);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 1.0f);
            GL11.glLineWidth(2.0f);
            RenderUtil.drawOutlinedBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 0.05, z + 1.0));
            GL11.glColor4f(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, 0.5f);
            RenderUtil.drawBoundingBox(new AxisAlignedBB(x, y, z, x + 1.0, y + 0.05, z + 1.0));
            GL11.glDisable(2848);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }
    }

    @EventHandler
    public void onPre( EventPreUpdate event) {

        if (target == null) {
            Client.Pitch = 0;
        }
        RayTraceUtil2 rayCastUtil;
        rotationYawHead = mc.thePlayer.rotationYawHead;
        needHitBot = null;

        if (!targets.isEmpty() && index >= targets.size())
            index = 0; // ����Switch����

        for (EntityLivingBase ent : targets) {
            if (isValidEntity(ent))
                continue;
            targets.remove(ent);
        }
        // Switch����

        getTarget(event); // ��ʵ��

        if (targets.size() == 0) { // ʵ������Ϊ0ֹͣ����
            target = null;
            attackSpeed = 0;
        } else {
            try{
                target = targets.get(index);// ���ù�����Target
                axisAlignedBB = null;
                if (mc.thePlayer.getDistanceToEntity(target) > reach.getValue()) {
                    target = targets.get(0);
                }
            }catch ( Exception e ){
                ;
            }

        }
        if ( ModuleManager.getModuleByName("Scaffold").isEnabled() ) {
            target = null;
            return;
        }
        if (this.RayCast.getValue() && target != null && (rayCastUtil = new RayTraceUtil2(target)).getEntity() != target) {
            curBot = rayCastUtil.getEntity();
        }
        if (target != null) {
            // Switch��ʼ
            if (target.hurtTime == 10 && switchTimer.isDelayComplete(switchDelay.getValue().longValue())
                    && targets.size() > 1) {
                switchTimer.reset();
                ++index;
            }
            float diff = Math.abs(Math.abs(MathHelper.wrapAngleTo180_float(rotationYawHead))
                    - Math.abs(MathHelper.wrapAngleTo180_float( RotationUtil.getRotations(target)[0])));

            if (rotations.getValue()) { // Ťͷ
                Random rand = new Random();
                switch ((Rotationmode) rotMode.getValue()) {
                    case Hypixel: {
                        float[] rotation = getEntityRotations(target, lastRotations, false,
                                turn.getValue().intValue());
                        lastRotations = new float[]{rotation[0], rotation[1]};

                        event.setYaw(rotation[0]);
                        mc.thePlayer.rotationYawHead = event.getYaw();


                        event.setPitch(rotation[1]);
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(rotation[0], rotation[1]);
                        break;
                    }
                    case Viro: {
                        float[] rotations = getRotations(target);
                        event.setYaw(rotations[0]);
                        event.setPitch(rotations[1]);
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(rotations[0], rotations[1]);
                        break;
                    }
                    case Smooth: {
                        double comparison = Math.abs(target.posY - mc.thePlayer.posY) > 1.8
                                ? Math.abs(target.posY - mc.thePlayer.posY) / Math.abs(target.posY - mc.thePlayer.posY) / 2
                                : Math.abs(target.posY - mc.thePlayer.posY);

                        Vector3<Double> enemyCoords = new Vector3<>(
                                target.getEntityBoundingBox().minX
                                        + (target.getEntityBoundingBox().maxX - target.getEntityBoundingBox().minX) / 2,
                                (target instanceof EntityPig || target instanceof EntitySpider
                                        ? target.getEntityBoundingBox().minY - target.getEyeHeight() * 1.2
                                        : target.posY) - comparison,
                                target.getEntityBoundingBox().minZ
                                        + (target.getEntityBoundingBox().maxZ - target.getEntityBoundingBox().minZ) / 2);

                        Vector3<Double> myCoords = new Vector3<>(mc.thePlayer.getEntityBoundingBox().minX
                                + (mc.thePlayer.getEntityBoundingBox().maxX - mc.thePlayer.getEntityBoundingBox().minX) / 2,
                                mc.thePlayer.posY,
                                mc.thePlayer.getEntityBoundingBox().minZ + (mc.thePlayer.getEntityBoundingBox().maxZ
                                        - mc.thePlayer.getEntityBoundingBox().minZ) / 2);

                        Angle srcAngle = new Angle(lastRotation[0], lastRotation[1]);

                        Angle dstAngle = AngleUtility.calculateAngle(enemyCoords, myCoords);
                        Angle smoothedAngle = AngleUtility.smoothAngle(dstAngle, srcAngle,
                                turn.getValue().floatValue() * 8, turn.getValue().floatValue() * 7.5f);
                        event.setYaw(smoothedAngle.getYaw() + randomNumber(-2, 2));
                        event.setPitch(smoothedAngle.getPitch() + randomNumber(-3, 3));
                        lastRotation[0] = event.getYaw();
                        mc.thePlayer.rotationYawHead = event.getYaw();
                        lastRotation[1] = event.getPitch();
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(event.getYaw(), event.getPitch());
                        break;
                    }
                    case FootClick: {
                        float[] rotations = getLoserRotation(target);
                        event.setYaw(rotations[0]);
                        event.setPitch(rotations[1]);
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(rotations[0], rotations[1]);
                        break;
                    }
                    case HypixelGood: {
                        float[] rotations = RotationLib(target);
                        event.setYaw(rotations[0]);
                        event.setPitch(rotations[1]);
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(rotations[0], rotations[1]);
                        break;
                    }
                    case HypixelRandom: {
                        float[] rot = RotationUtil.getRotations(target);
                        event.setYaw(rot[0] + rand.nextInt(10) - 5);
                        event.setPitch(rot[1] + rand.nextInt(3) - 2);
                        rotationYawHead = event.getYaw();
                        Client.RenderRotate(event.getYaw(), event.getPitch());
                        break;
                    }
                }

            }

            if (mc.thePlayer.isBlocking()
                    || mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                    && autoBlock.getValue() && blockingStatus && blockMode.getValue().equals(BlockMode.Hypixel)) { // ��
                unBlock(!mc.thePlayer.isBlocking()
                        && !autoBlock.getValue() && mc.thePlayer.getItemInUseCount() > 0);
            }

        } else { // ûʵ��
            lastRotation[0] = mc.thePlayer.rotationYaw;
            targets.clear();
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                    && autoBlock.getValue() && (blockingStatus||fakeblockstatus)) {
                unBlock(true);
            }
        }
    }
    boolean cantickblock=false;
    private void doBlock(boolean setItemUseInCount) {

        if(!blockMode.getValue().equals( BlockMode.Fake )){

            if(!blockMode.getValue().equals( BlockMode.Tick )&&!blockMode.getValue().equals( BlockMode.Packet )) {
                if ( setItemUseInCount )
                    ( mc.thePlayer ).itemInUseCount = ( mc.thePlayer.getHeldItem( ).getMaxItemUseDuration( ) );
                mc.thePlayer.sendQueue.getNetworkManager( )
                        .sendPacket( new C08PacketPlayerBlockPlacement(
                                BlockPos.ORIGIN, 255,
                                mc.thePlayer.getHeldItem( ), 0.0f, 0.0f, 0.0f ) );
                blockingStatus = true;
            }else{
                if(cantickblock&&currentTarget!=null){
                    blockingStatus=true;
                    mc.thePlayer.sendQueue.addToSendQueue( new C08PacketPlayerBlockPlacement( new BlockPos(-1,-1,-1),255,mc.thePlayer.getHeldItem(),0,0,0 ) );
                    cantickblock=false;
                }
            }

        }
        if(blockMode.getValue()==BlockMode.Packet){
            blockingStatus=true;
            mc.getNetHandler().addToSendQueue( new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN) );
        }

        // needUnBlock = true;
        if(blockMode.getValue()== BlockMode.Fake ){
            fakeblockstatus=true;
        }

    }

    private void unBlock(boolean setItemUseInCount) {
        if(!blockMode.getValue().equals( BlockMode.Fake ))
        {
            if (setItemUseInCount){
                (mc.thePlayer).itemInUseCount = (0);
                mc.thePlayer.sendQueue.getNetworkManager()
                        .sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
                                ,BlockPos.ORIGIN,
                                EnumFacing.DOWN));
                blockingStatus = false;
                cantickblock=false;
            }

        }

        // needUnBlock = false;
        if(blockMode.getValue()==BlockMode.Fake){
            fakeblockstatus=false;
        }
    }

    public static float[] getEntityRotations(EntityLivingBase target, float[] lastrotation, boolean aac, int smooth) {
        myAngleUtility angleUtility = new myAngleUtility(aac, smooth);
        Vector3d enemyCoords = new Vector3d(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        Vector3d myCoords = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(),
                mc.thePlayer.posZ);
        myAngle dstAngle = angleUtility.calculateAngle(enemyCoords, myCoords);
        myAngle srcAngle = new myAngle(lastrotation[0], lastrotation[1]);
        myAngle smoothedAngle = angleUtility.smoothAngle(dstAngle, srcAngle);
        float yaw = smoothedAngle.getYaw();
        float pitch = smoothedAngle.getPitch();
        float yaw2 = MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
        yaw = mc.thePlayer.rotationYaw + yaw2;
        return new float[]{yaw, pitch};
    }

    public static float[] getLoserRotation(Entity target) {
        double xDiff = target.posX - mc.thePlayer.posX;
        double yDiff = target.posY - mc.thePlayer.posY - 0.4;
        double zDiff = target.posZ - mc.thePlayer.posZ;


        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) ((-Math.atan2(yDiff, dist)) * 180.0 / 3.141592653589793);
        float[] array = new float[2];
        int n = 0;
        float rotationYaw = mc.thePlayer.rotationYaw;
        float n2 = yaw;
        array[n] = rotationYaw + MathHelper.wrapAngleTo180_float(n2 - mc.thePlayer.rotationYaw);
        int n3 = 1;
        float rotationPitch = mc.thePlayer.rotationPitch;
        float n4 = pitch;
        array[n3] = rotationPitch + MathHelper.wrapAngleTo180_float(n4 - mc.thePlayer.rotationPitch);
        return array;
    }

    private int randomNumber(int max, int min) {
        return (int) (Math.random() * (double) (max - min)) + min;
    }

    private void doAttack() {
        int aps = randomNumber(cpsMax.getValue().intValue(), cpsMin.getValue().intValue());
        int delayValue = 1000 / aps;

        if (attacktimer.isDelayComplete(delayValue)) { // ����Timer
            boolean miss = false;
            boolean isInRange = mc.thePlayer.getDistanceToEntity(target) <= reach.getValue();

            if (isInRange) {
                attacktimer.reset();
                if (target.hurtTime > hurttime.getValue() || // Hurttime
                        random.nextInt(100) < mistake.getValue().intValue() // ���Mistakes
                )
                    miss = true;

                float diff = Math.abs(Math.abs(MathHelper.wrapAngleTo180_float(rotationYawHead))
                        - Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(target)[0])));

                if (diff > yawDiff.getValue() && !ModuleManager.getModuleByName("Scaffold").isEnabled()) {
                    miss = true;
                }
            }

            if (mc.thePlayer.isBlocking() || mc.thePlayer.getHeldItem() != null
                    && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && autoBlock.getValue()) { // ��
                unBlock(!mc.thePlayer.isBlocking() && !autoBlock.getValue()
                        && mc.thePlayer.getItemInUseCount() > 0);

            }

            if (isInRange) {
                attack(miss); // ��������miss
            }
            // needBlattackock = true;
        }
    }

    @EventHandler
    public void onPost( EventPostUpdate event) {
        if (target != null)
            doAttack();

        if (target != null
                && (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                && autoBlock.getValue() || mc.thePlayer.isBlocking())
                && /* needBlock */!blockingStatus) { // ��
            doBlock(true);
        }
    }

    int attackSpeed;

    public float[] RotationLib(EntityLivingBase target) {
        cn.distance.util.rotations.AngleUtilitys angleUtility = new cn.distance.util.rotations.AngleUtilitys(110, 120, 30, 40);
        cn.distance.util.rotations.Vector3<Double> enemyCoords = new cn.distance.util.rotations.Vector3<>(target.posX, target.posY, target.posZ);
        cn.distance.util.rotations.Vector3<Double> myCoords = new cn.distance.util.rotations.Vector3<>(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        cn.distance.util.rotations.Angle dstAngle = angleUtility.calculateAngle(enemyCoords, myCoords);
        cn.distance.util.rotations.Angle smoothedAngle1 = angleUtility.smoothAngle(dstAngle, dstAngle);
        return new float[]{smoothedAngle1.getYaw(), smoothedAngle1.getPitch()};
    }

    private void attack(boolean mistake) {
        this.Crit = false;
        currentTarget = ((KillAura.curBot != null) ? KillAura.curBot : KillAura.target);
        if (!mistake) {
            isAttacking = true;
            needBlock = true; // ȷ����
            CopyOnWriteArrayList<EntityLivingBase> list = new CopyOnWriteArrayList<>();
            for (Entity entity : mc.theWorld.loadedEntityList) {
                float diff = Math.abs(Math.abs(MathHelper.wrapAngleTo180_float(rotationYawHead))
                        - Math.abs(MathHelper.wrapAngleTo180_float(RotationUtil.getRotations(entity)[0])));

                if (entity instanceof EntityZombie && entity.isInvisible()
                        && (diff < yawDiff.getValue() || mc.thePlayer.getDistanceToEntity(target) < 1)
                        && mc.thePlayer.getDistanceToEntity(entity) < reach.getValue() && entity != mc.thePlayer) {

                    list.add((EntityLivingBase) entity);
                }
            }
            if (list.size() == 0)
                list.add(target);
            needHitBot = list.get(random.nextInt(list.size()));
            Criticals Crit = (Criticals) ModuleManager.getModuleByClass(Criticals.class);
            if (Crit.autoCrit(target)) {
                this.Crit = true;
                attackSpeed = 0;
            }

            attackSpeed++;
            mc.thePlayer.swingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity((KillAura.curBot != null) ? KillAura.curBot : KillAura.target, C02PacketUseEntity.Action.ATTACK));

            if (!attacked.contains(target) && target instanceof EntityPlayer) {
                attacked.add(target);
            }
            isAttacking = false;
            needHitBot = null;
            curBot = null;
        } else {
            mc.thePlayer.swingItem();
        }
    }

    private void getTarget(EventPreUpdate event) {
        int maxSize = switchsize.getValue().intValue();
        for (Entity o3 : mc.theWorld.loadedEntityList) {
            EntityLivingBase curEnt;

            if (o3 instanceof EntityLivingBase && isValidEntity(curEnt = (EntityLivingBase) o3)
                    && !targets.contains(curEnt))
                targets.add(curEnt);

            if (targets.size() >= maxSize)
                break;
        }

        if (priority.getValue().equals(Prioritymode.Range))
            targets.sort(
                    (o1, o2) -> (int) (o1.getDistanceToEntity(mc.thePlayer) - o2.getDistanceToEntity(mc.thePlayer)));

        if (priority.getValue().equals(Prioritymode.Fov))
            targets.sort(Comparator.comparingDouble(o -> RotationUtil
                    .getDistanceBetweenAngles(mc.thePlayer.rotationPitch, RotationUtil.getRotations(o)[0])));

        if (priority.getValue().equals(Prioritymode.Angle)) {
            targets.sort((o1, o2) -> {
                float[] rot1 = RotationUtil.getRotations(o1);
                float[] rot2 = RotationUtil.getRotations(o2);
                return (int) (mc.thePlayer.rotationYaw - rot1[0] - (mc.thePlayer.rotationYaw - rot2[0]));
            });
        }

    }

    @EventHandler
    private void onPacket( EventPacket e) { // No Rotate
        // if (e.getPacket() instanceof S08PacketPlayerPosLook) {
        //       S08PacketPlayerPosLook look = (S08PacketPlayerPosLook) e.getPacket();
        //    look.yaw = (mc.thePlayer.rotationYaw);
        //    look.pitch = (mc.thePlayer.rotationPitch);
        //}
        if(e.getPacket() instanceof S12PacketEntityVelocity && ( ( S12PacketEntityVelocity ) e.getPacket() ).getEntityID()==mc.thePlayer.getEntityId()){
          cantickblock=true;
        }

    }

    private boolean isValidEntity(Entity entity) {
        if (entity instanceof EntityLivingBase) {
            if (entity.isDead || ((EntityLivingBase) entity).getHealth() <= 0f) {
                return false;
            }
            if ( FriendManager.isFriend(entity.getName())) {
                return false;
            }
            if (mc.thePlayer.getDistanceToEntity(entity) < (reach.getValue() + blockReach.getValue())) {
                if (entity != mc.thePlayer && !mc.thePlayer.isDead
                        && !(entity instanceof EntityArmorStand || entity instanceof EntitySnowman)) {

                    if (entity instanceof EntityPlayer && attackPlayers.getValue()) {
                        if (entity.ticksExisted < 30)
                            return false;

                        if (!mc.thePlayer.canEntityBeSeen(entity) && !throughblock.getValue())
                            return false;

                        if (entity.isInvisible() && !invisible.getValue())
                            return false;

                        return !isBot2(entity) && !Teams.isOnSameTeam(entity);
                    }

                    if (entity instanceof EntityMob && attackMobs.getValue()) {
                        return !isBot2(entity);
                    }

                    if ((entity instanceof EntityAnimal || entity instanceof EntityVillager)
                            && attackAnimals.getValue()) {
                        return !isBot2(entity);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEnable() {
        cantickblock=false;
        curY = new ScaledResolution(mc).getScaledHeight();
        this.Crit = false;
        shouldAddYaw = 0;
        attacked = new CopyOnWriteArrayList<EntityLivingBase>();
        axisAlignedBB = null;
        if (mc.thePlayer != null) {
            lastRotation[0] = mc.thePlayer.rotationYaw;
            lastRotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        }
        index = 0; // Switch Targetָ��
        super.onEnable();
    }

    @Override
    public void onDisable() {
//        if(mc.thePlayer.isBlocking() && (Boolean) autoBlock.getValue() && blockMode.getValue().equals(BlockMode.Legit)) {
//            this.stopBlocking();
//        }
        cantickblock=false;
        curY = new ScaledResolution(mc).getScaledHeight();
        this.Crit = false;
        axisAlignedBB = null;
        if (mc.thePlayer != null) {
            lastRotation[0] = mc.thePlayer.rotationYaw;
        }

        targets.clear();
        target = null; // ���Ŀ��? (AutoBlock�����޸�)

        unBlock(true);

        super.onDisable();
    }

    public static float[] getRotations(EntityLivingBase currentTarget2) {
        if (currentTarget2 == null) {
            return null;
        }
        double diffX = currentTarget2.posX - mc.thePlayer.posX;
        double diffZ = currentTarget2.posZ - mc.thePlayer.posZ;
        double diffY = currentTarget2.posY - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / 3.141592653589793) - 90.0f;
        float pitch = (float) ((-Math.atan2(diffY, dist)) * 180.0 / 3.141592653589793);
        return new float[]{yaw, pitch};
    }

    public static double isInFov(float var0, float var1, double var2, double var4, double var6) {
        Vec3 var8 = new Vec3((double) var0, (double) var1, 0.0D);
        float[] var9 = getAngleBetweenVecs(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ),
                new Vec3(var2, var4, var6));
        double var10 = MathHelper.wrapAngleTo180_double(var8.xCoord - (double) var9[0]);
        return Math.abs(var10) * 2.0D;
    }

    public static float[] getAngleBetweenVecs(Vec3 var0, Vec3 var1) {
        double var2 = var1.xCoord - var0.xCoord;
        double var4 = var1.yCoord - var0.yCoord;
        double var6 = var1.zCoord - var0.zCoord;
        double var8 = Math.sqrt(var2 * var2 + var6 * var6);
        float var10 = (float) (Math.atan2(var6, var2) * 180.0D / 3.141592653589793D) - 90.0F;
        float var11 = (float) (-(Math.atan2(var4, var8) * 180.0D / 3.141592653589793D));
        return new float[]{var10, var11};


    }

    public static float[] getAnglesIgnoringNull(Entity var0, float var1, float var2) {
        float[] var3 = getAngles(var0);
        if (var3 == null) {
            return new float[]{0.0F, 0.0F};
        } else {
            float var4 = var3[0];
            float var5 = var3[1];
            return new float[]{var1 + MathHelper.wrapAngleTo180_float(var4 - var1),
                    var2 + MathHelper.wrapAngleTo180_float(var5 - var2) + 5.0F};
        }
    }

    public static float[] getAngles(Entity entity) {
        if (entity == null) {
            return null;
        } else {
            double var1 = entity.posX - mc.thePlayer.posX;
            double var3 = entity.posZ - mc.thePlayer.posZ;
            double var5;
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase var7 = (EntityLivingBase) entity;
                var5 = var7.posY + ((double) var7.getEyeHeight() - 0.4D)
                        - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            } else {
                var5 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0D
                        - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            }

            double var11 = (double) MathHelper.sqrt_double(var1 * var1 + var3 * var3);
            float var9 = (float) (Math.atan2(var3, var1) * 180.0D / 3.141592653589793D) - 90.0F;
            float var10 = (float) (-(Math.atan2(var5, var11) * 180.0D / 3.141592653589793D));
            return new float[]{var9, var10};
        }
    }

    public static boolean isValidToRotate(double var0, double var2) {
        if (mc.thePlayer != null && mc.theWorld != null && mc.thePlayer.getEntityWorld() != null) {
            Iterator var4 = mc.thePlayer.getEntityWorld().loadedEntityList.iterator();

            Entity var5;
            do {
                if (!var4.hasNext()) {
                    return false;
                }

                var5 = (Entity) var4.next();
            } while (!(var5 instanceof EntityPlayer) || var5 == mc.thePlayer
                    || (double) mc.thePlayer.getDistanceToEntity(var5) >= var0 || isInFov(mc.thePlayer.rotationYaw,
                    mc.thePlayer.rotationPitch, var5.posX, var5.posY, var5.posZ) >= var2);

            return true;
        } else {
            return false;
        }
    }

    public static double normalizeAngle(double var0, double var2) {
        double var4 = Math.abs(var0 % 360.0D - var2 % 360.0D);
        var4 = Math.min(360.0D - var4, var4);
        return Math.abs(var4);
    }

    private boolean isBot2(Entity e) {
        if ( AntiBot.isServerBot(e)) {
            return true;
        } else if (AntiBot.isServerBot(e)) {
            return true;
        }
        return false;
    }

    private boolean canBlock() {
        return mc.thePlayer.getCurrentEquippedItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword;
    }

    enum Rotationmode {
        Hypixel,
        HypixelGood,
        HypixelRandom,
        Viro,
        FootClick,
        Smooth
    }

    enum Prioritymode {
        Range,
        Angle,
        Fov;
    }

    enum BlockMode {
        Hypixel,
        Fake,
        Vanilla,
        Tick,
        Packet;
    }
}