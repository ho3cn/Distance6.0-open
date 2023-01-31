package cn.distance.module.modules.world;

import cn.distance.api.value.Numbers;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;
import cn.distance.ui.cloudmusic.ui.GuiCloudMusic;

public class MusicPlayer extends Module {
    public static final Numbers<Double> musicPosYlyr = new Numbers<>("MusicPlayerLyricY", 120d, 0d, 200d, 1d);
    public MusicPlayer() {
        super("MusicPlayer",new String[]{"neteasemusicplayer","music"}, ModuleType.World);
        addValues(musicPosYlyr);
    }

    @Override
    public void onEnable(){
        mc.displayGuiScreen(new GuiCloudMusic());
        this.setEnabled(false);
    }
}
