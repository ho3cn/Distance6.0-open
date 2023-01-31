package cn.distance.util.misc.scaffold;

import cn.distance.util.math.Rotation;
import net.minecraft.util.Vec3;

public class VecRotation {
	Vec3 vec;
	Rotation rotation;

	public VecRotation(Vec3 vec, Rotation rotation) {
		this.vec = vec;
		this.rotation = rotation;
	}
	public Rotation getRotation()
	{
		return this.rotation;
	}
}
