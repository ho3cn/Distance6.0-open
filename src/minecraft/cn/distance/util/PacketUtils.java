package cn.distance.util;


import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

public class PacketUtils {
	private static final Minecraft mc = Minecraft.getMinecraft();

	public static void sendPacket2(Packet packet) {
		if (mc.thePlayer != null) {
			mc.thePlayer.sendQueue.addToSendQueue(packet);
		}
	}

	public static void sendPacket(Packet<?> packet) {
		if (Minecraft.getMinecraft().thePlayer != null) {
			(( NetworkManager ) Minecraft.getMinecraft().thePlayer.sendQueue.getNetworkManager()).sendPacketNoEvent(packet);
		}
	}

	public static void sendPacketNoEvent(Packet packet) {
		sendPacket(packet);
	}

	public static void sendBlocking(boolean callEvent, boolean placement) {
		if (mc.thePlayer == null)
			return;

		if (placement) {
			C08PacketPlayerBlockPlacement packet = new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255,
					mc.thePlayer.getHeldItem(), 0, 0, 0);
			if (callEvent) {
				sendPacket2(packet);
			} else {
				sendPacketNoEvent(packet);
			}
		} else {
			C08PacketPlayerBlockPlacement packet = new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem());
			if (callEvent) {
				sendPacket2(packet);
			} else {
				sendPacketNoEvent(packet);
			}
		}
	}
}
