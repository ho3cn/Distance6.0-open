package cn.core.auth.modules.dis.disablers;


import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.events.Render.EventRender3D;
import cn.distance.api.events.World.*;
import cn.core.auth.modules.Disabler;
import cn.core.auth.modules.dis.DisablerModule;
import cn.distance.ui.notifications.user.Notifications;
import cn.distance.util.PacketUtils;
import cn.distance.util.math.RandomUtil;
import cn.distance.util.time.MSTimer;
import cn.distance.util.time.TimeHelper;
import cn.distance.util.time.TimerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.optifine.util.MathUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


public class DisablerHypixelDisabler implements DisablerModule {
    byte[] uuid = UUID.randomUUID().toString().getBytes();
    public boolean isCraftingItem = false;
    private final Queue<TimestampedPacket> queue = new ConcurrentLinkedDeque<>();
    MSTimer timedOutTimer = new MSTimer();
    CopyOnWriteArrayList<C0EPacketClickWindow> clickWindowPackets = new CopyOnWriteArrayList<>();
    private final ArrayList<C0FPacketConfirmTransaction> movePackets = new ArrayList<>();
    private final LinkedList<Packet<?>> list = new LinkedList<>();
    private List<Packet> packetList = new CopyOnWriteArrayList<Packet>();
    private TimerUtil timer = new TimerUtil();

    private int bypassValue = 0;
    private long lastTransaction = 0L;

    private int lastUid;
    private boolean checkReset;
    private boolean active;

    private final TimeHelper tick = new TimeHelper();
    private final TimeHelper collecttimer = new TimeHelper();
    private final LinkedBlockingQueue<Packet> packets = new LinkedBlockingQueue<>();
    private MSTimer timerCancelDelay = new MSTimer();
    private MSTimer timerCancelTimer = new MSTimer();
    private boolean timerShouldCancel = true;
    private boolean canBlink = true;

    @Override
    public void onEnabled() {
        if(mc.thePlayer==null)return;
//        synchronized(positions) {
//            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + (mc.thePlayer.getEyeHeight() / 2), mc.thePlayer.posZ});
//            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
//        }
//
//        pulseTimer.reset();
        uuid = UUID.randomUUID().toString().getBytes();
        packetList.clear();
        list.clear();
        isCraftingItem=false;
        clickWindowPackets.clear();
        movePackets.clear();
        timedOutTimer.reset();
        sended=false;
        timerCancelDelay.reset();
        timerCancelTimer.reset();
        packets.clear();
        timer.reset();
    }

    @Override
    public void onPacket(EventPacketSend event) {

//
//        if (mc.thePlayer == null || disableLogger)
//            return;

//        if (packet instanceof C03PacketPlayer) // Cancel all movement stuff
//            event.setCancelled(true);
//
//        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook ) {
//            event.setCancelled(true);
//
//            packets.add(packet);
//     }



    }
    public boolean isPlayerInGame() {
        return isHypixelLobby()&&(Minecraft.getMinecraft().thePlayer != null) && (Minecraft.getMinecraft().theWorld != null);
    }
    public boolean isOnHypixel() {
        if (!isPlayerInGame())
            return false;
        try {
            return !mc.isSingleplayer() && (mc.getCurrentServerData().serverIP.toLowerCase().contains("hypixel.net")
                    || mc.getCurrentServerData().serverIP.toLowerCase().contains("localhost"));
        } catch (Exception welpBruh) {
            welpBruh.printStackTrace();
            return false;
        }
    }


    @Override
    public void onPacket( EventPacketReceive event) {

    }
    @Override
    public void onRender3d(EventRender3D event){
//        final Breadcrumbs breadcrumbs = (Breadcrumbs) ModuleManager.getModuleByClass(Breadcrumbs.class);
//        final Color color = breadcrumbs.colorRainbow.getValue() ? HUD.RainbowColor : new Color(breadcrumbs.colorRedValue.getValue().intValue(), breadcrumbs.colorGreenValue.getValue().intValue(), breadcrumbs.colorBlueValue.getValue().intValue());
//
//        synchronized(positions) {
//            glPushMatrix();
//
//            glDisable(GL_TEXTURE_2D);
//            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//            glEnable(GL_LINE_SMOOTH);
//            glEnable(GL_BLEND);
//            glDisable(GL_DEPTH_TEST);
//            mc.entityRenderer.disableLightmap();
//            glBegin(GL_LINE_STRIP);
//            GLUtils.glColor(color.getRGB());
//            final double renderPosX = mc.getRenderManager().viewerPosX;
//            final double renderPosY = mc.getRenderManager().viewerPosY;
//            final double renderPosZ = mc.getRenderManager().viewerPosZ;
//
//            for(final double[] pos : positions)
//                glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ);
//
//            glColor4d(1, 1, 1, 1);
//            glEnd();
//            glEnable(GL_DEPTH_TEST);
//            glDisable(GL_LINE_SMOOTH);
//            glDisable(GL_BLEND);
//            glEnable(GL_TEXTURE_2D);
//            glPopMatrix();
//        }
    }
    @Override
    public void onPacket( EventPacket event) {
        base(event);
    }

    @Override
    public void onUpdate( EventPreUpdate event) {
        if (mc.thePlayer.ticksExisted < 1) {
            timerCancelTimer.reset();
            timerCancelDelay.reset();
            packets.clear();
        }

        if (Disabler.TimerB.getValue()) {
            if (timerCancelDelay.hasTimePassed(10000)) {
                timerShouldCancel = true;
                timerCancelTimer.reset();
                timerCancelDelay.reset();
            }
        }
        try {
            synchronized(packetsMap) {
                for(final Iterator<Map.Entry<Packet<?>, Long>> iterator = packetsMap.entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<Packet<?>, Long> entry = iterator.next();

                    if(entry.getValue() < System.currentTimeMillis()) {
                        mc.getNetHandler().addToSendQueue(entry.getKey());
                        iterator.remove();
                    }
                }
            }
        }catch(final Throwable t) {
            t.printStackTrace();
        }
//        synchronized(positions) {
//            positions.add(new double[] {mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ});
//        }
//
//        if(Disabler.Test_blinkvalue.getValue()&& pulseTimer.hasTimePassed(420L)) {
//            blink();
//            pulseTimer.reset();
//        }
    }

    @Override
    public void onDisable() {
        if(mc.thePlayer == null)
            return;

    }

    @Override
    public void onWorldChange( EventWorldChanged event) {
        sended=false;
    }

    @Override
    public void onRender2d(EventRender2D event) {

    }

    @Override
    public void onMotionUpdate( EventMotionUpdate event) {

    }

    public void doInvMove(EventPacket e) {
        if (e.getTypes() == EventPacket.Type.RECEIVE) return;
        if (e.getPacket() instanceof C16PacketClientStatus ) {
            C16PacketClientStatus clientStatus = ((C16PacketClientStatus) e.getPacket());
            if (clientStatus.getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) {
                e.setCancelled(true);
            }
        }

        if (e.getPacket() instanceof C0DPacketCloseWindow ) {
            C0DPacketCloseWindow closeWindow = ((C0DPacketCloseWindow) e.getPacket());
            if (closeWindow.windowId == 0) {
                if (isCraftingItem) {
                    isCraftingItem = false;
                }
                e.setCancelled(true);
            }
        }

        if (e.getPacket() instanceof C0EPacketClickWindow ) {
            C0EPacketClickWindow clickWindow = ((C0EPacketClickWindow) e.getPacket());
            if (clickWindow.getWindowId() == 0) {
                if (!isCraftingItem && clickWindow.getSlotId() >= 1 && clickWindow.getSlotId() <= 4) {
                    isCraftingItem = true;
                }

                if (isCraftingItem && clickWindow.getSlotId() == 0 && clickWindow.getClickedItem() != null) {
                    isCraftingItem = false;
                }

                timedOutTimer.reset();
                e.setCancelled(true);
                clickWindowPackets.add(clickWindow);
            }
        }

        boolean isDraggingItem = false;

        if ( Minecraft.getMinecraft().currentScreen instanceof GuiInventory ) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getItemStack() != null) {
                isDraggingItem = true;
            }
        }

        if (mc.thePlayer.ticksExisted % 5 == 0 && !clickWindowPackets.isEmpty() && !isDraggingItem && !isCraftingItem) {
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            for (C0EPacketClickWindow clickWindowPacket : clickWindowPackets) {
                Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEvent(clickWindowPacket);
            }
            Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEvent(new C0DPacketCloseWindow(0));
            clickWindowPackets.clear();
            timedOutTimer.reset();
        }
    }
    private void checkUidVaild(EventPacket event) {

        if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
            final C0FPacketConfirmTransaction C0F = (C0FPacketConfirmTransaction) event.getPacket();
            final int windowId = C0F.getWindowId();
            final int uid = C0F.getUid();
            if (windowId == 0 && uid < 0) {
                final int predictedUid = lastUid - 1;
                if (!checkReset) {
                    if (uid == predictedUid) {
                        if (!active) {
                            active = true;
                        }
                    } else {
                        active = false;
                    }
                } else {
                    if (uid != predictedUid) {
                        active = false;
                    }
                    checkReset = false;
                }
                lastUid = uid;
            }
        }
    }


    private final HashMap<Packet<?>, Long> packetsMap = new HashMap<>();
    boolean sended=false;
    public void base(EventPacket event) {
        if ( mc.isSingleplayer( ) ) {
            if ( !sended ) {
                Notifications.getManager( ).post( "Disabler", "检测到您在单人游戏，disabler不会生效！" );
                sended = true;
            }

            return;
        }

        if ( isHypixelLobby( ) && Disabler.lobbycheckvalue.get( ) ) {
            if ( !sended ) {
                Notifications.getManager( ).post( "Disabler", "检测到你在大厅，disabler不会生效" );
                sended = true;
            }

            return;
        }
        if ( event != null ) {
//            if (event.getPacket() instanceof C03PacketPlayer && !(event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || event.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook|| event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook)) {
//                if (mc.thePlayer.ticksExisted < 70) {
//                    event.setCancelled( true );
//                }
//            }
            if(Disabler.invclickbypass.get()){
                doInvMove( event );
            }


//            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
//                S08PacketPlayerPosLook serverSidePosition = (S08PacketPlayerPosLook) event.getPacket();
//
//                if (mc.currentScreen instanceof GuiDownloadTerrain)
//                    mc.currentScreen = null;
//
//                final float serverPitch = serverSidePosition.getPitch();
//                final float serverYaw = serverSidePosition.getYaw();
//
//                if (serverPitch == 0 && serverYaw == 0)
//                    event.setCancelled(true);
//            }
            //ping test bypass
//            if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
//                lastTransaction = System.currentTimeMillis();
//                checkUidVaild(event);
//                if (active){
//                    event.setCancelled(true);
//                    queue.add(new TimestampedPacket(event.getPacket(), System.currentTimeMillis()));
//                }
//            }
            //ping test bypass
            Packet<?>packet=event.getPacket();
            if(Disabler.TimerA.get()){
                canBlink = true;


                if (packet instanceof C02PacketUseEntity || packet instanceof C03PacketPlayer || packet instanceof C07PacketPlayerDigging || packet instanceof C08PacketPlayerBlockPlacement ||
                        packet instanceof C0APacketAnimation || packet instanceof C0BPacketEntityAction && mc.thePlayer.ticksExisted > 70) {

                    if ( timerShouldCancel ) {
                        if ( !timerCancelTimer.hasTimePassed( 450 ) ) {
                            packets.add( ( Packet< INetHandlerPlayServer > ) packet );
                            event.setCancelled( true );
                            canBlink = false;
                        } else {
                            timerShouldCancel = false;
                            while ( !packets.isEmpty( ) ) {
                                try {
                                    PacketUtils.sendPacketNoEvent( packets.take( ) );
                                } catch ( InterruptedException e ) {
                                    e.printStackTrace( );
                                }
                            }
                        }
                    }
                }

            }
            if (packet instanceof C03PacketPlayer && !(packet instanceof C03PacketPlayer.C05PacketPlayerLook
                    || packet instanceof C03PacketPlayer.C06PacketPlayerPosLook
                    || packet instanceof C03PacketPlayer.C04PacketPlayerPosition) && Disabler.noC03s.getValue()) {
                event.setCancelled(true);
                canBlink = false;
            }

            if (isOnHypixel()) {
                if (Disabler.blinkvalue.getValue()) {
                    if (packet instanceof C02PacketUseEntity || packet instanceof C03PacketPlayer
                            || packet instanceof C07PacketPlayerDigging || packet instanceof C08PacketPlayerBlockPlacement
                            || packet instanceof C0APacketAnimation || packet instanceof C0BPacketEntityAction) {

                        while (!packets.isEmpty()) {
                            try {
                                PacketUtils.sendPacketNoEvent(packets.take());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        if (canBlink) {
                            packets.add((Packet<INetHandlerPlayServer>) packet);
                            event.setCancelled( true );
                        }
                    }
                }
            }
            if (Disabler.pingspoofvalue.get()) {
                if ((packet instanceof C00PacketKeepAlive || packet instanceof C16PacketClientStatus) && !(mc.thePlayer.isDead || mc.thePlayer.getHealth() <= 0) && !packetsMap.containsKey(packet)) {
                    event.setCancelled(true);

                    synchronized(packetsMap) {
                        packetsMap.put(packet, System.currentTimeMillis() + Disabler.pingspoofdelay.getValue().longValue());
                    }
                }
            }

        }
    }
     public static boolean isHypixelLobby() {
        String[] strings = new String[] {"CLICK TO PLAY", "点击开始游戏"};
        try {
            for ( Entity entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if(entity!=null){
                    if (entity.getName().startsWith("§e§l")) {
                        for (String string : strings) {
                            if (entity.getName().equals("§e§l" + string)) {
                                return true;
                            }
                        }
                    }
                }

            }
            return false;
        }catch ( NullPointerException e ){
            return false;
        }

    }



    private static class TimestampedPacket {
        private final Packet<?> packet;
        private final long timestamp;

        public TimestampedPacket(final Packet<?> packet, final long timestamp) {
            this.packet = packet;
            this.timestamp = timestamp;
        }
    }
}
