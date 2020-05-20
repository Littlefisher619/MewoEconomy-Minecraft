package com.mewo.economy;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Set;

public class DataManager {
    public int TradeCnt=0;
    private HashMap<String, Integer> statssell=new HashMap<>();
    private HashMap<String, Integer> statsbuy=new HashMap<>();

    private HashMap<String, Integer> buycnt=new HashMap<>();
    private HashMap<String, Integer> sellcnt=new HashMap<>();
    private void init(HashMap<String, Integer> map,Set<String> resset){
        for(String i: resset) {
            map.put(i, 0);
        }
    };
    public DataManager(final MarketGUI plugin){
        TradeCnt=0;
        init(statsbuy,plugin.getConfigManager().getResSet());
        init(statssell,plugin.getConfigManager().getResSet());

        init(buycnt,plugin.getConfigManager().getResSet());
        init(sellcnt,plugin.getConfigManager().getResSet());
    }
    public void universalItemAppend(Material m, int x)
    {

        String res=m.name().toUpperCase();
        int val=getBuyCnt(res);
        if(val != -1)
            setBuyCnt(res,val + x);
    }
    public void universalItemRemove(Material m, int x)
    {
        String res=m.name().toUpperCase();
        int val=getSellCnt(res);
        if(val != -1)
            setBuyCnt(res,val + x);
    }
    public int getStatsSell(final String res) {return statssell.get(res);}
    public int getStatsBuy(final String res) {return statsbuy.get(res);}
    public int getBuyCnt(final String res) { return buycnt.getOrDefault(res,-1); }
    public int getSellCnt(final String res) { return sellcnt.getOrDefault(res,-1); }
    public void setBuyCnt(final String res,int cnt) { buycnt.put(res,cnt);}
    public void setSellCnt(final String res,int cnt) {buycnt.put(res,cnt);}
    public void newTradeSell(final String res,final int amount){
        if(res=="TOTAL") return;
        sellcnt.put(res,sellcnt.get(res)+amount);
        statssell.put(res,statssell.get(res)+1);
        TradeCnt++;
    }
    public void newTradeBuy(final String res,final int amount){
        if(res=="TOTAL") return;
        buycnt.put(res,buycnt.get(res)+amount);
        statsbuy.put(res,statsbuy.get(res)+1);
        TradeCnt++;
    }
}
