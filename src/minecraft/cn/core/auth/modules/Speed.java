package cn.core.auth.modules;

import cn.distance.api.EventHandler;
import cn.distance.api.events.World.*;
import cn.distance.api.value.Mode;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.core.auth.modules.speedmode.SpeedModule;
import cn.core.auth.modules.speedmode.speed.*;
import cn.distance.ui.notifications.user.Notifications;

import cn.distance.util.entity.MovementUtils;
import cn.distance.util.entity.PlayerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import org.lwjgl.input.Keyboard;


public class Speed extends Module {
    public static Mode mode = new Mode("Mode", SpeedMode.values(), SpeedMode.Hypixel);

    public static Option lagcheck = new Option("LagBackCheck", true);
    public static Option groundspoof=new Option( "GroundSpoof",false );
    public static Option fastfall=new Option( "FastFall",false );

    public static Option sprint=new Option( "Sprint",true );
    public static Option aireagle=new Option( "Aireagle",false );
    public static Option jumpnobob=new Option( "JumpNoBob",false );

    public static Option fullstrafe=new Option( "FullStrafe",false );

    public static Numbers<Number> dmspeed=new Numbers<Number>("DamageBoostSpeed",0.2,0.0,1.0,0.05);
    public static Numbers<Number> dmgboostmstimer=new Numbers<>( "DamageBoostMstimer",500,100,5000,10 );

    public static Numbers<Number> timer=new Numbers<Number>("Timer",1.0,0.5,3.0,0.1);
    public static Numbers<Number> basespeed=new Numbers<Number>("BaseSpeed",0.42,0.00,0.80,0.01);

    public static Numbers<Number> fastfallticks=new Numbers<Number>("Fastfallticks",4.0,1.0,20.0,1.0);
    public static Numbers<Number> fastfallmotion=new Numbers<Number>( "FastfallMotionY",0.1,0.01,2.00,0.01 );


    public Speed() {
        super("Speed", new String[]{"zoom"}, ModuleType.Movement);

        addValues(mode,lagcheck,sprint,aireagle,fullstrafe,groundspoof,jumpnobob,fastfall ,fastfallticks,fastfallmotion,timer,basespeed,dmspeed,dmgboostmstimer);

    }
    public void onEnable() {
        ((SpeedMode)mode.getValue()).getModule().onEnabled();
    }

    public void onDisable() {

        mc.timer.timerSpeed = 1.0F;
        ((SpeedMode)mode.getValue()).getModule().onDisabled();

        if (aireagle.getValue()) {
            mc.gameSettings.keyBindSneak.pressed = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
        }

    }

    @EventHandler
    public void onPacketReceive( EventPacketReceive e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof S08PacketPlayerPosLook && lagcheck.getValue()) {
            Notifications.getManager().post("Speed", "Speed拉回!已自动关闭Speed");
            this.setEnabled(false);
        }
    }
    @EventHandler
    public void onPacket(EventPacket e){
        ((SpeedMode)mode.getValue()).getModule().onPacket( e );
    }
    @EventHandler
    public void onSteps( EventStep e){
        ((SpeedMode)mode.getValue()).getModule().onStep(e);
    }

    @EventHandler
    public void onPost( EventPostUpdate e) {
        ((SpeedMode)mode.getValue()).getModule().onPost(e);
    }

    @EventHandler
    public void onMotion( EventMotionUpdate e){
        ((SpeedMode)mode.getValue()).getModule().onMotion(e);
    }

    @EventHandler
    public void onPacket( EventPacketSend e){
        if(groundspoof.get()){
            if(e.getPacket() instanceof C03PacketPlayer ){
                ( ( C03PacketPlayer ) e.getPacket() ).onGround=false;
            }
        }
        ((SpeedMode)mode.getValue()).getModule().onPacketSend(e);
    }

    @EventHandler
    private void onMove(EventMove event) {
        ((SpeedMode)mode.getValue()).getModule().onMove(event);
    }
    @EventHandler
    public void onStrafe(EventStrafe e){
        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isInWeb()) return;
        if (mode.getValue()==SpeedMode.Hypixel&&!mc.gameSettings.keyBindJump.isKeyDown()) {
            double d = Math.sqrt( mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ);

            double d5 = mc.thePlayer.hurtTime > 1 && mc.thePlayer.fallDistance < 3.0f && !mc.thePlayer.isPotionActive( Potion.poison) && !mc.thePlayer.isBurning() ? 1.0 : 0.0;
            double d6 = Math.hypot( mc.thePlayer.motionX, mc.thePlayer.motionZ) * d5;
            double d7 = mc.thePlayer.motionX * (1.0 - d5);
            double d8 = mc.thePlayer.motionZ * (1.0 - d5);
            mc.thePlayer.motionX = d6 * -Math.sin(MovementUtils.getDirection()) + d7;
            mc.thePlayer.motionZ = d6 * Math.cos(MovementUtils.getDirection()) + d8;

        }
    }

    int airticks=0;
    @EventHandler
    public void OnTick(EventMotionUpdate e){

        mc.timer.timerSpeed = Speed.timer.get().floatValue();

//      跳跃时无视角摇晃
        if(jumpnobob.getValue()){
            this.mc.thePlayer.cameraYaw = -0;
        }

//      疾跑控制
        if(!sprint.getValue()) {
            mc.thePlayer.setSprinting( false );
        }

//      空中蹲
        if (aireagle.getValue() && !mc.thePlayer.onGround && mc.thePlayer.isMoving()) {
            mc.gameSettings.keyBindSneak.pressed = PlayerUtil.isAirUnder(mc.thePlayer);
    }

        if(e.isPre()){
            if(mc.thePlayer.onGround&& fastfall.get( )){
                airticks=0;
            }
            if(!mc.thePlayer.onGround&& fastfall.get( )){
                airticks++;
                if(airticks==fastfallticks.getValue().intValue()){
                    mc.thePlayer.motionY=-fastfallmotion.get().floatValue();
                }
            }
        }

    }
    @EventHandler
    private void onPreUpdate(EventPreUpdate e) {


        ((SpeedMode)mode.getValue()).getModule().onPre(e);
        this.setSuffix(mode.getValue());
    }

    public enum SpeedMode {
        Hypixel(new HypixelSpeed()),
        AutoJump(new AutoJumpSpeed()),
        HypixelLowHop(new HypixelLowHopSpeed()),
        Hive(new HiveSpeed()),
        AAC440(new AAC440Speed()),
        Bhop(new BhopSpeed()),
        GudHop(new GudHopSpeed()),
        HuaYuTingA(new HytBhopSpeed()),
        HuaYuTingB(new HytTypeBSpeed()),
        OnGround(new OnGroundSpeed()),
        AACTimer(new AACTimer()),
        VulcanHop(new VulcanHopSpeed()),
        VulcanFastHop(new VulcanFastHopSpeed()),
        VulcanLowHop(new VulcanLowHopSpeed());


        final SpeedModule module;
        SpeedMode(SpeedModule speedModule){
            this.module = speedModule;
        }
        public SpeedModule getModule(){
            return module;
        }
    }
}
