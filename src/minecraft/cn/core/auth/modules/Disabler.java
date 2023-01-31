package cn.core.auth.modules;

import cn.distance.api.EventHandler;
import cn.distance.api.events.Render.EventRender2D;
import cn.distance.api.events.Render.EventRender3D;
import cn.distance.api.events.World.*;
import cn.distance.api.value.Mode;
import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.api.value.Value;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.core.auth.modules.dis.DisablerModule;
import cn.core.auth.modules.dis.disablers.*;
import cn.distance.module.modules.render.MotionBlur;
import cn.distance.util.time.MSTimer;


import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class Disabler extends Module {
    private final Mode mode = new Mode("Mode", Modes.values(), Modes.NewSpoof);
    public static Option slowc03value=new Option( "Test-SlowC03",false );
    public static Option lobbycheckvalue=new Option( "LobbyCheck",true );
    public static Option lowerTimer = new Option("Lower timer on Lag",false);
    public static Option TimerA= new Option( "TimerA",false );
    public static Option TimerB=new Option( "TimerB",false );
    public static Option noC03s=new Option( "NoC03s",false );
    public static Option blinkvalue =new Option( "TestBlink",false );
    public static Option pingspoofvalue=new Option( "PingSpoof",true );
    public static Numbers<Number>pingspoofdelay=new Numbers<Number>( "PingSpoodDelay",400,10,600,1 );
    public static Option invclickbypass=new Option( "InvClickBypass",true );

    private final MSTimer lagTimer = new MSTimer();
    //    public static final Numbers<Double> delay = new Numbers<>("Delay",500d, 300d, 2000d, 100d);

    public Disabler() {
        super("Disabler", new String[]{"Bypass", "Patcher"}, ModuleType.World);
        addValues(mode,lowerTimer,slowc03value,lobbycheckvalue,TimerA,TimerB,noC03s,blinkvalue,pingspoofvalue,pingspoofdelay,invclickbypass);
        setValueDisplayable( new Value< ? >[]{slowc03value,TimerA,TimerB,noC03s,blinkvalue,pingspoofvalue,invclickbypass },mode, Modes.Hypxiel );
    }

    @Override
    public void onEnable() {
        ((Modes) mode.getValue()).get().onEnabled();
    }

    @Override
    public void onDisable() {
        ((Modes) mode.getValue()).get().onDisable();
    }

    @EventHandler
    public void onMotionUpdate( EventMotionUpdate e){
        ((Modes) mode.getValue()).get().onMotionUpdate(e);
    }

    @EventHandler
    public void onRender2d( EventRender2D e){
        ((Modes) mode.getValue()).get().onRender2d(e);

    }


    @EventHandler
    public void onPre( EventPreUpdate e) {
        setSuffix(mode.getValue());

        ((Modes) mode.getValue()).get().onUpdate(e);

        if (lowerTimer.getValue()) {
            if (!lagTimer.hasTimePassed(1000)) {
                mc.timer.timerSpeed = 0.7f;
            } else {
                mc.timer.timerSpeed = 1f;
            }
        }
    }
    @EventHandler
    public void onRender3d( EventRender3D e){
        ((Modes)mode.getValue()).get().onRender3d( e );
    }
    @EventHandler
    public void onPacket( EventPacket e){
        ((Modes) mode.getValue()).get().onPacket(e);
        if (e.packet instanceof S08PacketPlayerPosLook) {
            lagTimer.reset();
        }
    }

    @EventHandler
    public void onPacket( EventPacketSend event) {
        ((Modes) mode.getValue()).get().onPacket(event);
    }

    @EventHandler
    public void onPacketRE( EventPacketReceive e) {
        ((Modes) mode.getValue()).get().onPacket(e);
    }

    @EventHandler
    public void onRespawn(EventWorldChanged e) {
        ((Modes) mode.getValue()).get().onWorldChange(e);
    }


    enum Modes {
        Hypxiel(new DisablerHypixelDisabler()),
        NewSpoof(new NewSpoofDisabler()),
        AAC4LessFlag(new AAC4LessFlagDisabler()),
        AAC5Test(new AAC5TestDisabler()),
        VulcanCombat(new VulcanCombatDisabler());

        final DisablerModule disablerModule;

        Modes(DisablerModule disabler) {
            disablerModule = disabler;
        }

        public DisablerModule get() {
            return disablerModule;
        }
    }
}
