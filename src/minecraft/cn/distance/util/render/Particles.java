package cn.distance.util.render;

import cn.distance.util.misc.Location;

import java.awt.*;

public class Particles {
	public int ticks;
	public Location location;
	public String text;
	public double x, y, deltaX, deltaY, size, opacity;
	public Color color;
	public void render2D() {
		DrawUtil.circle(x, y, size, new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) opacity));
	}

	public void updatePosition() {
		x += deltaX * 2;
		y += deltaY * 2;
		deltaY *= 0.95;
		deltaX *= 0.95;
		opacity -= 2f;
		if (opacity < 1) opacity = 1;
	}
	public void init(final double x, final double y, final double deltaX, final double deltaY, final double size, final Color color) {
		this.x = x;
		this.y = y;
		this.deltaX = deltaX;
		this.deltaY = deltaY;
		this.size = size;
		this.opacity = 254;
		this.color = color;
	}
	public Particles(final Location location, final String text) {
		this.location = location;
		this.text = text;
		this.ticks = 0;
	}
}
