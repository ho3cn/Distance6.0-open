package cn.distance.module.modules.render;


import cn.distance.api.value.Numbers;
import cn.distance.api.value.Option;
import cn.distance.module.Module;
import cn.distance.module.ModuleType;

public class SetScoreboard extends Module {

    public static Option hideboard = new Option("hideboard", "hideboard", false);
    public static Option fastbord = new Option("fastbord", "fastbord", false);
    public static Option Norednumber = new Option("Norednumber", "Norednumber", false);
    public static Option noServername = new Option("noServername", "noServername", false);
    public static Numbers<Double> X = new Numbers<>("X", "X", 4.5, 0.0, 1000.0, 1.0);
    public static Numbers<Double> Y = new Numbers<>("Y", "Y", 4.5, -300.0, 300.0, 1.0);
    public static Option noanyfont = new Option("noanyfont", "noanyfont", false);
    public SetScoreboard() {
        super("Scoreboard", new String[] {"SetScoreboard"}, ModuleType.Render);
        this.addValues(X, Y, hideboard , fastbord, Norednumber, noServername);
    }
}
