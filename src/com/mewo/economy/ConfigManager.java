package com.mewo.economy;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ConfigManager {
    private Set<String> Resources;
    private HashMap<Integer,String> SlotsTable=new HashMap<>();
    private HashMap<String,Double> Value=new HashMap<>(),
            PriceLimitMAX=new HashMap<>(), PriceLimitMIN=new HashMap<>(),PriceNow=new HashMap<>(),PriceToday=new HashMap<>();
    private HashMap<String,Integer> SellLimitPer=new HashMap<>(),BuyLimitPer=new HashMap<>();
    private HashMap<String,Double> SellLimitFellRate=new HashMap<>(),BuyLimitRaiseRate=new HashMap<>();
    private HashMap<String,Double> DailyFloatMAX=new HashMap<>(),DailyFloatMIN=new HashMap<>(),DailyRestore=new HashMap<>();
    private HashMap<String,String> ResBtnTitle=new HashMap<>(),ResBtnName=new HashMap<>();
    private HashMap<String,Integer> ResBtnSlot=new HashMap<>(),ResBtnId=new HashMap<>();
    private String LastUpdateDate,MoneyUnit,Prefix,GUITitle;
    private int AntiBotDelay;
    private final DecimalFormat df =new DecimalFormat("0.00");

    private void init(String res,final FileConfiguration config){

        Value.put(res,config.getDouble(res + ".value"));

        PriceLimitMAX.put(res,config.getDouble(res + ".limit.price.max"));
        PriceLimitMIN.put(res,config.getDouble(res + ".limit.price.min"));
        PriceNow.put(res,config.getDouble(res + ".price.now"));
        PriceToday.put(res,config.getDouble(res + ".price.today"));

        SellLimitPer.put(res,config.getInt(res + ".limit.sell.per"));
        BuyLimitPer.put(res,config.getInt(res + ".limit.buy.per"));
        SellLimitFellRate.put(res,config.getDouble(res + ".limit.sell.fell"));
        BuyLimitRaiseRate.put(res,config.getDouble(res + ".limit.buy.raise"));

        DailyFloatMAX.put(res,config.getDouble(res + ".daily_float.to"));
        DailyFloatMIN.put(res,config.getDouble(res + ".daily_float.from"));
        DailyRestore.put(res,config.getDouble(res + ".daily_restore"));

        ResBtnTitle.put(res,config.getString(res+".gui.title"));
        ResBtnSlot.put(res,config.getInt(res+".gui.slot"));
        ResBtnId.put(res,config.getInt(res+".gui.iconid"));
        ResBtnName.put(res,config.getString(res+".gui.name"));
        SlotsTable.put(config.getInt(res+".gui.slot"),res);

        LastUpdateDate=config.getString("Settings.LastUpdateDate");
        MoneyUnit=config.getString("Settings.MoneyUnit");
        Prefix=config.getString("Settings.Prefix");
        GUITitle=config.getString("Settings.GUITitle");
        AntiBotDelay=config.getInt("Settings.AntiBotDelay");
    }
    public ConfigManager(final MarketGUI plugin)
    {

        plugin.getConfig().options().copyDefaults(true);
        File configFile = new File(plugin.getDataFolder(),"config.yml");
        if(!configFile.exists()){plugin.saveDefaultConfig();plugin.getLogger().info("Configure File Not Found! Regen...");}
        Resources=plugin.getConfig().getKeys(false);
        Iterator<String> it = Resources.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (str.equalsIgnoreCase("Settings")) it.remove();
            else init(str,plugin.getConfig());
        }
    }
    public double getValue(final String res) {return Value.get(res);}
    public double getPriceLimit(final String res,final String maxormin) {
        if(maxormin.toUpperCase().equalsIgnoreCase("MAX")) return PriceLimitMAX.get(res);
        else return PriceLimitMIN.get(res);
    }
    public double getPriceNow(final String res) {return PriceNow.get(res);}
    public double getPriceToday(final String res) {return PriceToday.get(res);}
    public int getSellLimitPer(final String res) {return SellLimitPer.get(res);}
    public double getSellLimitFellRate(final String res) {return SellLimitFellRate.get(res);}
    public int getBuyLimitPer(final String res) {return BuyLimitPer.get(res);}
    public double getBuyLimitRaiseRate(final String res) {return BuyLimitRaiseRate.get(res);}
    public double getDailyFloatMAX(final String res) {return DailyFloatMAX.get(res);}
    public double getDailyFloatMIN(final String res) {return DailyFloatMIN.get(res);}
    public double getDailyRestore(final String res) {return DailyRestore.get(res);}

    public String getDate() {return LastUpdateDate;}
    public void setDate(String date, final JavaPlugin plugin) {
        LastUpdateDate=date;
        plugin.getConfig().set("Settings.LastUpdateDate",date);
        plugin.saveConfig();
    }

    public String getResBtnTitle(final String res){return ResBtnTitle.get(res);}
    public String getResBtnName(final String res){return ResBtnName.get(res);}
    public int getResBtnSlot(final String res){return ResBtnSlot.get(res);}
    public int getResBtnId(final String res){return ResBtnId.get(res);}
    public String getRESNAMEById(final int Id){
        return SlotsTable.getOrDefault(Id,null);
    }
    public Set<String> getResSet(){return this.Resources;}

    public String getMoneyUnit(){return this.MoneyUnit;}
    public String getPrefix(){return this.Prefix;}
    public String getGUITitle(){return this.GUITitle;}
    public int getAntiBotDelay(){return this.AntiBotDelay;}

    public void setPriceNow(final String res,final double price){ PriceNow.put(res,price); }
    public void setPriceToday(final String res,final double price){ PriceToday.put(res,price); }
    public void savePriceNow(final String res, final JavaPlugin plugin){ plugin.getConfig().set(res + ".price.now",df.format(getPriceNow(res)));plugin.saveConfig(); }
    public void savePriceToday(final String res, final JavaPlugin plugin){ plugin.getConfig().set(res + ".price.today",df.format(getPriceToday(res)));plugin.saveConfig(); }
}
