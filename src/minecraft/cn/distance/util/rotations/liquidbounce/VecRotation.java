package cn.distance.util.rotations.liquidbounce;


import cn.distance.util.math.Rotation;

public class VecRotation {
    net.minecraft.util.Vec3 vec;
    Rotation rotation;

    public VecRotation(net.minecraft.util.Vec3 vec, Rotation rotation) {
        this.vec = vec;
        this.rotation = rotation;
    }

    public Rotation getRotation() {
        return this.rotation;
    }
}
