package me.gethertv.afkrewards.data;

import java.util.HashMap;
import java.util.List;

public class CmdRewards {
    private HashMap<String, Double> chance;
    private List<String> cmds;

    public CmdRewards(HashMap<String, Double> chance, List<String> cmds) {
        this.chance = chance;
        this.cmds = cmds;
    }

    public HashMap<String, Double> getChance() {
        return chance;
    }

    public List<String> getCmds() {
        return cmds;
    }
}
