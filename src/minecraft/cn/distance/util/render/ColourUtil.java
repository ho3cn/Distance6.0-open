package cn.distance.util.render;




import cn.distance.module.modules.render.HUD;

import java.awt.*;

public final class ColourUtil {

    private ColourUtil() {
    }

    private static final int[] HEALTH_COLOURS = {
            0xFF00FF59, // Green
            0xFFFFFF00, // Yellow
            0xFFFF8000, // Orange
            0xFFFF0000, // Red
            0xFF800000 // Dark-red
    };

    private static final int[] RAINBOW_COLOURS = {
            0xfffc6a8c, 0xfffc6ad5, 0xffda6afc, 0xff916afc, 0xff6a8cfc, 0xff6ad5fc, 0xffda6afc, 0xfffc6a8c,
    };

    private static final int[] CZECHIA_COLOURS = {
            0xFF11457E, 0xFF11457E, 0xFFD7141A, 0xFFD7141A, 0xFFFFFFFF, 0xFF11457E,
    };

    private static final int[] GERMAN_COLOURS = {
            0xFF000000, 0xFFFE0000, 0xFFFFCF00, 0xFF000000,
    };

    public static int blendHealthColours(final double progress) {
        return blendColours(HEALTH_COLOURS, progress);
    }

    public static int blendRainbowColours(final double progress) {
        return blendColours(RAINBOW_COLOURS, progress);
    }

    public static int blendRainbowColours(final long offset) {
        return blendRainbowColours(getFadingFromSysTime(offset));
    }

    public static int blendCzechiaColours(final double progress) {
        return blendColours(CZECHIA_COLOURS, progress);
    }

    public static int blendCzechiaColours(final long offset) {
        return blendCzechiaColours(getFadingFromSysTime(offset));
    }

    public static int blendGermanColours(final double progress) {
        return blendColours(GERMAN_COLOURS, progress);
    }

    public static int blendGermanColours(final long offset) {
        return blendGermanColours(getFadingFromSysTime(offset));
    }

    public static int blendSpecialRainbow(final long offset) {
        final float fading = (float) getFadingFromSysTime(offset);
        return Color.HSBtoRGB(1.f - fading, 0.8f, 1.f);
    }

    public static double getFadingFromSysTime(final long offset) {
        return ((System.currentTimeMillis() + offset) % 2000L) / 2000.0;
    }

    public static float getBreathingProgress() {
        final float progress = ((System.currentTimeMillis()) % 2000L) / 1000.0F;
        return progress > 1.0F ? 1.0F - progress % 1.0F : progress;
    }

    public static int blendRainbowColours() {
        return blendRainbowColours(0);
    }

    public static int clientColour = Colors.getColor(0);


    public static int getClientColour() {
        return clientColour;
    }

    public static void setClientColour(final int colour) {
        clientColour = colour;
    }

    public static int secondaryColour = new Color(HUD.r.getValue().intValue(),HUD.g.getValue().intValue(),HUD.b.getValue().intValue()).getRGB();;

    public static int getSecondaryColour() {
        return secondaryColour;
    }

    public static void setSecondaryColour(final int secondColour) {
        secondaryColour = secondColour;
    }


    public static int darker(final int colour, final double factor) {
        final int r = (int) ((colour >> 16 & 0xFF) * factor);
        final int g = (int) ((colour >> 8 & 0xFF) * factor);
        final int b = (int) ((colour & 0xFF) * factor);
        final int a = colour >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }

    public static float calculateAverageChannel(final int rgb) {
        final int red = rgb >> 16 & 0xFF;
        final int green = rgb >> 8 & 0xFF;
        final int blue = rgb & 0xFF;
        return Math.max(red, Math.max(green, blue)) / 255.f;
    }

    public static int removeAlphaComponent(final int colour) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;

        return ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                (blue & 0xFF);
    }

    public static int overwriteAlphaComponent(final int colour, final int alphaComponent) {
        final int red = colour >> 16 & 0xFF;
        final int green = colour >> 8 & 0xFF;
        final int blue = colour & 0xFF;

        return ((alphaComponent & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                (blue & 0xFF);
    }

    public static Color getGradientOffset(Color color1, Color color2, double offset) {
        if (offset > 1) {
            double left = offset % 1;
            int off = (int) offset;
            offset = off % 2 == 0 ? left : 1 - left;
        }
        double inverse_percent = 1 - offset;
        int redPart = (int) (color1.getRed() * inverse_percent + color2.getRed() * offset);
        int greenPart = (int) (color1.getGreen() * inverse_percent + color2.getGreen() * offset);
        int bluePart = (int) (color1.getBlue() * inverse_percent + color2.getBlue() * offset);
        return new Color(redPart, greenPart, bluePart);
    }

    public static int blendOpacityRainbowColours(final long offset, final int alphaComponent) {
        return overwriteAlphaComponent(blendRainbowColours(getFadingFromSysTime(offset)), alphaComponent);
    }

    public static int darker(int color) {
        return darker(color, 0.6);
    }

    public static int blendColours(final int[] colours, final double progress) {
        final int size = colours.length;
        if (progress == 1.f) return colours[0];
        else if (progress == 0.f) return colours[size - 1];
        final double mulProgress = Math.max(0, (1 - progress) * (size - 1));
        final int index = (int) mulProgress;
        return fadeBetween(colours[index], colours[index + 1], mulProgress - index);
    }

    public static int fadeBetween(int startColour, int endColour, double progress) {
        if (progress > 1) progress = 1 - progress % 1;
        return fadeTo(startColour, endColour, progress);
    }

    public static int fadeBetween(int startColour, int endColour, long offset) {
        return fadeBetween(startColour, endColour, ((System.currentTimeMillis() + offset) % 2000L) / 1000.0);
    }

    public static int fadeBetween(int startColour, int endColour) {
        return fadeBetween(startColour, endColour, 0L);
    }

    public static int fadeTo(int startColour, int endColour, double progress) {
        double invert = 1.0 - progress;
        int r = (int) ((startColour >> 16 & 0xFF) * invert + (endColour >> 16 & 0xFF) * progress);
        int g = (int) ((startColour >> 8 & 0xFF) * invert + (endColour >> 8 & 0xFF) * progress);
        int b = (int) ((startColour & 0xFF) * invert + (endColour & 0xFF) * progress);
        int a = (int) ((startColour >> 24 & 0xFF) * invert + (endColour >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }
}
