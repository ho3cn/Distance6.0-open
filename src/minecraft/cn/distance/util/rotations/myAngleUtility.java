package cn.distance.util.rotations;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import javax.vecmath.Vector3d;
import java.util.Iterator;
import java.util.Random;

public class myAngleUtility {

    private boolean aac;
    private float smooth;
    private Random random;

    public myAngleUtility(boolean aac, float smooth) {
        this.aac = aac;
        this.smooth = smooth;
        this.random = new Random();
    }

    public myAngle calculateAngle(Vector3d destination, Vector3d source) {
        myAngle angles = new myAngle();
        destination.x += (aac ? randomFloat(-0.75F, 0.75F) : 0.0F) - source.x;
        destination.y += (aac ? randomFloat(-0.25F, 0.5F) : 0.0F) - source.y;
        destination.z += (aac ? randomFloat(-0.75F, 0.75F) : 0.0F) - source.z;
        double hypotenuse = Math.hypot(destination.x, destination.z);
        angles.setYaw((float) (Math.atan2(destination.z, destination.x) * 57.29577951308232D) - 90.0F);
        angles.setPitch(-(float) ((Math.atan2(destination.y, hypotenuse) * 57.29577951308232D)));
        return angles.constrantAngle();
    }

    public myAngle smoothAngle(myAngle destination, myAngle source) {
        myAngle angles = (new myAngle(source.getYaw() - destination.getYaw(),
                source.getPitch() - destination.getPitch())).constrantAngle();
        angles.setYaw(source.getYaw() - angles.getYaw() / 100.0F * smooth);
        angles.setPitch(source.getPitch() - angles.getPitch() / 100.0F * smooth);
        return angles.constrantAngle();
    }

    public float randomFloat(float min, float max) {
        return min + (this.random.nextFloat() * (max - min));
    }

    private static Minecraft mc = Minecraft.getMinecraft();

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

    private double getAngleYaw(EntityLivingBase var1) {
        return (double) getAnglesIgnoringNull(var1, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)[0];
    }

}
