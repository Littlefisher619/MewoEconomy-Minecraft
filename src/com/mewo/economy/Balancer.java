package com.mewo.economy;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

public class Balancer {

    private Random r = new Random();
    private Logger console;
    private MarketGUI plugin;
    private final DecimalFormat df =new DecimalFormat("0.00");
    public Balancer(Logger bukkitlogger,MarketGUI plugin)
    {
        console=bukkitlogger;
        this.plugin=plugin;
    }
    public void dodailytask(String target)
    {
        ConfigManager cfg=plugin.getConfigManager();

        target=target.toUpperCase();
        int RATEMAX = (int) (cfg.getDailyFloatMAX(target) * 100), RATEMIN = (int) (cfg.getDailyFloatMIN(target) * 100);
        double RNDRATE=rand(RATEMAX,RATEMIN);
        int ISNEGATIVE=rand(1,0);

        double  price = cfg.getPriceNow(target),
                value = cfg.getValue(target),
                pmax = cfg.getPriceLimit(target,"MAX"),
                pmin = cfg.getPriceLimit(target,"MIN"),
                floatrate = ((double)RNDRATE)/100,
                restorerate = cfg.getDailyRestore(target);
        if(ISNEGATIVE==1)floatrate=-floatrate;
        //console.info(String.format("[%s Config]price=%.2f value=%.2f pmax=%.2f pmin=%.2f floatrate=%.2f restorerate=%.2f",target,price,value,pmax,pmin,floatrate,restorerate));
        price += (value - price) * restorerate;
        price *= (1 + floatrate);
        if(price<=pmin) price=pmin;
        if(price>=pmax) price=pmax;

        cfg.setPriceNow(target,price);
        cfg.setPriceToday(target,price);
        cfg.savePriceNow(target,plugin);
        cfg.savePriceToday(target,plugin);
        //console.info(String.format("[%s]%.2f Float_Rate=%.2f ( RNDRATE=%d ISNEGATIVE=%d )",target,cfg.getPriceNow(target),floatrate,RNDRATE,ISNEGATIVE));
        //console.info(df.format( cfg.getPriceNow(target)));
    }

    public void dopricefloat(String target,String action)
    {
        ConfigManager cfg=plugin.getConfigManager();
        target=target.toUpperCase();
        action=action.toUpperCase();
        double  price = cfg.getPriceNow(target),
                pmax= cfg.getPriceLimit(target,"MAX"),
                pmin= cfg.getPriceLimit(target,"MIN"),
                rate;
        if(action=="SELL")
        {
            rate=cfg.getSellLimitFellRate(target);
            price=price*(1-rate);
        }
        if(action=="BUY")
        {
            rate=cfg.getBuyLimitRaiseRate(target);
            price=price*(1+rate);
        }
        if(price<=pmin) price=pmin;
        if(price>=pmax) price=pmax;
        cfg.setPriceNow(target,price);

    }
    private int rand(int MAX,int MIN)
    {
        return r.nextInt(MAX - MIN + 1) + MIN;
    }
}
