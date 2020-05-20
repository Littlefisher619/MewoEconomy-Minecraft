package com.mewo.economy;


import com.mysql.fabric.xmlrpc.base.Array;
import net.steepout.meoweco.Overseer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;

/**
 * Created by admin on 2017/7/28.
 */
public class MarketGUI extends JavaPlugin implements Listener {
    //public static FileConfiguration mcfg;
    //private Map<String,Integer> cnt= new HashMap<String, Integer>();
    public static MarketGUI instance;
    private LogWatcher logWatcher;
    private Balancer balancer;
    private ConfigManager cfg;
    private DataManager datas;

    private java.text.DecimalFormat df =new java.text.DecimalFormat("0.00");
    private static Economy econ = null;

    private ItemStack
            mmqhbtn=new ItemStack(Material.DIODE,1),
            itemicon=new ItemStack(Material.BARRIER,1),
            jyxqbtn=new ItemStack(Material.EMPTY_MAP,1),
            cancelbtn=new ItemStack(Material.REDSTONE,1),
            okbtn=new ItemStack(Material.SLIME_BALL,1) ;
    private HashMap<String,ItemStack> resbtn=new HashMap<>();
    private ItemStack glass[]= new ItemStack[9];

    private String MoneyUnit,Prefix,GUITitle;

    private final List<String> infolore = new ArrayList<>(Arrays.asList(
            "§b> §e左键单击数量§a  +1",
            "§b> §e右键单击数量§a  +10",
            "§b> §eShift+左键数量§c -1",
            "§b> §eShift+右键数量§c -10 ","§7§o商品标识: []"));


    @Override
    public void onEnable(){
        if (!setupEconomy() ) {
            getLogger().warning(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.cfg=new ConfigManager(this);

        this.balancer = new Balancer(this.getLogger(),this);

        this.datas=new DataManager(this);

        this.logWatcher = new LogWatcher(this, new File(this.getDataFolder(), "market.log"));
        logWatcher.task = Bukkit.getScheduler().runTaskTimerAsynchronously(this, this.logWatcher, 150, 150);



        instance=this;

        MoneyUnit=cfg.getMoneyUnit();
        Prefix=cfg.getPrefix();
        GUITitle=cfg.getGUITitle();
        //AntiBotDelay=cfg.getAntiBotDelay();

        initbtn();

        log("[DailyTask] DailyTask Finished!");
        log("[onEnable] MewoEconomy Enabled!");


        //Listeners
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new Overseer(), this);

        //Timers
        getServer().getScheduler().runTaskTimer(this,new floatTimer(this),1200L,12000L);
        getServer().getScheduler().runTaskTimer(this,new dailyTaskTimer(this),1200L,72000L);

        this.getLogger().info("MewoEconomy Enabled Successfully!");
    }
    public class dailyTaskTimer extends BukkitRunnable {

        private final JavaPlugin plugin;

        public dailyTaskTimer(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            SimpleDateFormat datef = new SimpleDateFormat("yyyyMMdd");
            String today=datef.format(new Date());
            if(!cfg.getDate().equals(today)) {
                log("[DailyTask] A new day coming! Doing daily-tasks!");

                cfg.setDate(today,plugin);

                for(String res:cfg.getResSet())balancer.dodailytask(res);
                plugin.saveConfig();

                log("[DailtTask] Floats:");
                for(String res:cfg.getResSet()){
                    String s=String.format("%S has float to %.2f",res,cfg.getPriceNow(res));
                    log(s);
                    getLogger().info(s);
                }
            }else{
                log("[DailyTask] There is nothing to do.");
                getLogger().info("[DailyTask] There is nothing to do.");
            }
            log("[DailyTask] DailyTask Finished!");
        }

    }
    public class floatTimer extends BukkitRunnable {

        private final JavaPlugin plugin;

        public floatTimer(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            // 你需要在运行的时候执行的内容放这
            //log("run!");
            for (String res : cfg.getResSet()) {
                int LimitPer = 0;
                boolean flag = false;
                //buy
                    LimitPer = cfg.getBuyLimitPer(res);
                    while (datas.getBuyCnt(res) >= LimitPer) {
                        flag = true;
                        datas.setBuyCnt(res, datas.getBuyCnt(res) - LimitPer);
                        balancer.dopricefloat(res, "BUY");
                    }

                //sell
                    LimitPer = cfg.getSellLimitPer(res);
                    while (datas.getSellCnt(res) >= LimitPer) {
                        flag = true;
                        datas.setSellCnt(res, datas.getSellCnt(res) - LimitPer);
                        balancer.dopricefloat(res, "SELL");
                    }


                if (flag) {
                    log(String.format("[PriceFloat] %S has float to %.2f", res, cfg.getPriceNow(res)));
                    cfg.savePriceNow(res, plugin);
                }
            }
        }

    }
    @Override
    public void onDisable(){
        this.saveConfig();

        if (logWatcher != null) {
            logWatcher.task.cancel();
            logWatcher.close(); // Closes the file
        }
        getLogger().info("Mewo Economy Unload Successfully!");
    }
    private final void initresbtn(){
        resbtn.clear();
        for(String i: cfg.getResSet())
        {
            ItemStack ims=new ItemStack(cfg.getResBtnId(i),1);
            ItemMeta mta=ims.getItemMeta();

            List<String> lores = new ArrayList<>();
            lores.add("§b成交量 §6>> ");
            lores.add("  §a今日买入： " + datas.getStatsBuy(i) + " 笔");
            lores.add("  §a今日出售： " + datas.getStatsSell(i) + " 笔");
            lores.add("§b价格 §6>>");
            lores.add("  §a起始价格： " + df.format(cfg.getPriceToday(i)) + " " + MoneyUnit);
            lores.add("  §a当前价格： " + df.format(cfg.getPriceNow(i)) + " " + MoneyUnit);
            lores.add(" ");
            lores.add("§b>§e 点击选定此商品 §b<");
            mta.setLore(lores);
            mta.setDisplayName(cfg.getResBtnTitle(i));
            ims.setItemMeta(mta);
            resbtn.put(i,ims);
        }
    }

    private final void initbtn(){

        initresbtn();
        ItemMeta meta;

        meta = mmqhbtn.getItemMeta();
        meta.setDisplayName("§a买入物品");
        meta.setLore(Collections.singletonList("§7§o点击切换为§b§o[卖]"));
        mmqhbtn.setItemMeta(meta);

        meta = jyxqbtn.getItemMeta();
        meta.setLore(Collections.singletonList("§c您当前未选中任何商品！"));
        meta.setDisplayName("§e交易详情");
        jyxqbtn.setItemMeta(meta);

        meta = cancelbtn.getItemMeta();
        meta.setDisplayName("§c取消交易");
        cancelbtn.setItemMeta(meta);

        meta = okbtn.getItemMeta();
        meta.setDisplayName("§a提交交易(右键)");
        meta.setLore(Collections.singletonList("§7§o购买时请在背包中留一个空位！"));
        okbtn.setItemMeta(meta);

        meta = itemicon.getItemMeta();
        meta.setLore(Collections.singletonList("§7§o请在上方选择商品。"));
        meta.setDisplayName("§c未知交易对象");
        itemicon.setItemMeta(meta);

        for(int i=0;i<=8;i++) {
            glass[i] = new ItemStack(Material.STAINED_GLASS_PANE,1);
            meta=glass[i].getItemMeta();
            meta.setDisplayName(" ");
            glass[i].setItemMeta(meta);
        }
        glass[0].setDurability((short)14);glass[5].setDurability((short)5);
        glass[1].setDurability((short)1);glass[6].setDurability((short)4);
        glass[2].setDurability((short)4);glass[7].setDurability((short)1);
        glass[3].setDurability((short)5);glass[8].setDurability((short)14);
        glass[4].setDurability((short)3);
    }

    private void initinv(Inventory inv) {
        inv.clear();
        for(String i: cfg.getResSet())
            inv.setItem(cfg.getResBtnSlot(i) ,resbtn.get(i));
        for(int i=0;i<=8;i++) inv.setItem(27+i,glass[i]);
        inv.setItem(40,mmqhbtn);
        inv.setItem(41,jyxqbtn);
        inv.setItem(42,itemicon);
        inv.setItem(43,cancelbtn);
        inv.setItem(44,okbtn);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args )
    {
        if(cmd.getName().equalsIgnoreCase("oremarket") )
        {

            if(args.length ==0)
            {
                sender.sendMessage(Prefix + " §a>> §c未知命令!");
            }
            if(args.length ==1)
            {
                if (sender instanceof Player)
                {
                    Player p = (Player)sender;

                    switch (args[0].toLowerCase()){
                        case "open":
                            Inventory minv=Bukkit.createInventory(null,45 , "§1交易市场 §c>§6>§e>");
                            initresbtn();
                            initinv(minv);
                            p.closeInventory();

                            p.openInventory(minv);
                            break;
                        default:
                            sender.sendMessage(Prefix + " §a>> §c未知命令!");
                            break;
                    }
                } else{ sender.sendMessage(Prefix + " §a>> §c只有玩家才可使用该命令!"); }
            }
        }
        return true;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
    public static Economy getEcononomy() {
        return econ;
    }

    private String getResByLore(List<String> lore)
    {
        String s =lore.get(lore.size()-1);
        s=s.substring(s.indexOf("[")+1,s.indexOf("]"));
        //System.out.println(s);
        return s.toUpperCase();

    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!(event.getWhoClicked() instanceof Player)) {return;}
        Player p = (Player)event.getWhoClicked();
        Inventory inv=event.getInventory();

        if (inv.getTitle().equalsIgnoreCase(GUITitle) )
        {
            event.setCancelled(true);

            int Slot=event.getRawSlot();
            if(Slot==-999 || event.getCurrentItem().getType()==Material.AIR) return;

            /*
            p.sendMessage("Action " + event.getAction().toString());
            p.sendMessage("RawSlot " + event.getRawSlot());
            p.sendMessage("CLICK:"+event.getClick() );*/

            ItemStack target=new ItemStack(Material.AIR,1);
            ItemMeta meta;
            if(cfg.getRESNAMEById(event.getRawSlot())!=null) {
                String res=cfg.getRESNAMEById(Slot);
                //p.sendMessage("ClickOn ResBtn!");
                target.setType(event.getCurrentItem().getType());
                meta=target.getItemMeta();
                meta.setDisplayName(cfg.getResBtnName(res) + " *1");

                List<String> lores=infolore;
                lores.set(lores.size()-1,"§7§o商品标识： [" + res.toUpperCase() + "]");
                meta.setLore(lores);
                target.setItemMeta(meta);
                inv.setItem(42,target);
            }else switch (event.getRawSlot()) {
                case 40:
                    target=event.getCurrentItem();
                    meta=target.getItemMeta();
                    if(!meta.getDisplayName().contains("买")) {
                        meta.setDisplayName("§a买入物品");
                        meta.setLore(Collections.singletonList("§7§o点击切换为§b§o[卖]"));
                    } else {
                        meta.setDisplayName("§a卖出物品");
                        meta.setLore(Collections.singletonList("§7§o点击切换为§b§o[买]"));
                    }
                    target.setItemMeta(meta);
                    inv.setItem(40,target);
                    break;
                case 42:
                    if(inv.getItem(42).getType()==Material.BARRIER) return;
                    target=event.getCurrentItem();
                    meta=target.getItemMeta();
                    String name=meta.getDisplayName();
                    Integer amount=Integer.parseInt(name.substring(name.indexOf("*")+1));
                    switch (event.getClick()) {
                        case LEFT:
                            amount+=1;
                            break;
                        case RIGHT:
                            amount+=10;
                            break;
                        case SHIFT_LEFT:
                            amount-=1;
                            break;
                        case SHIFT_RIGHT:
                            amount-=10;
                            break;
                    }
                    if(amount<1) amount=1;
                    if(amount>=30) amount=30;
                    name=name.substring(0,name.indexOf("*")+1)+amount.toString();
                    meta.setDisplayName(name);
                    target.setItemMeta(meta);
                    inv.setItem(42,target);
                    break;
                case 43:
                    p.closeInventory();
                    p.sendMessage(Prefix + " §a>> §c交易已取消，欢迎下次惠顾！");
                    break;
                case 44:
                    EconomyResponse r;
                    if(event.getClick()!=ClickType.RIGHT) p.sendMessage(Prefix + " §a>> §c必须右键点击才能确认交易!");
                    else if(inv.getItem(42).getType()==Material.BARRIER) p.sendMessage(Prefix + " §a>> §c当前未选定任何物品！");
                    else{
                        String tmp=inv.getItem(42).getItemMeta().getDisplayName();
                        Integer am=Integer.parseInt(tmp.substring(tmp.indexOf("*")+1));

                        String material= getResByLore(inv.getItem(42).getItemMeta().getLore());
                        Double total=(double)am*cfg.getPriceNow(material);
                        if(!inv.getItem(40).getItemMeta().getDisplayName().contains("买")) {
                            //卖
                            if(ItemOperate.hasItem(p,inv.getItem(42).getType(),am)){
                                ItemOperate.takeItem(p,inv.getItem(42).getType(),am);
                                r=econ.depositPlayer(p, total);
                                if(r.transactionSuccess()) {
                                    p.sendMessage("");
                                    p.sendMessage(Prefix + " §a>> §a交易成功，共 " + df.format(total) + " " + MoneyUnit + "已打入您的帐户，祝您游戏愉快！");
                                    logtrade(p,material,"sell",am,total);
                                } else p.sendMessage(Prefix + " §a>> §c交易时发生未知错误，请联系管理员！\n"+ r.errorMessage);
                                p.closeInventory();
                            }else p.sendMessage(Prefix + " §a>> §c所需物品不足！");
                        } else {//买
                            if(econ.has(p,total)){
                                r=econ.withdrawPlayer(p,total);
                                if(r.transactionSuccess()) {
                                    p.sendMessage(Prefix + " §a>> §a交易成功，共 " + df.format(total) + " " + MoneyUnit + "已扣款，祝您游戏愉快！");
                                    ItemOperate.sendItem(p,inv.getItem(42).getType(),am);
                                    logtrade(p,material,"buy",am,total);
                                } else p.sendMessage(Prefix + " §a>> §c交易时发生未知错误，请联系管理员！\n"+ r.errorMessage);
                                p.closeInventory();
                            }else p.sendMessage(Prefix + " §a>> §c余额不足！");
                        }

                    }
                default:
                    return;
            }
            refreshinfo(inv,p);
        }
    }
    private void refreshinfo(Inventory inv,Player p)
    {
        if(inv.getItem(42).getType()==Material.BARRIER) return;
        ItemStack action=inv.getItem(40),
                  info=inv.getItem(41),
                  icon=inv.getItem(42);
        Double money= econ.getBalance(p);
        ItemMeta maction=action.getItemMeta(),
                 micon = icon.getItemMeta(),
                 minfo = info.getItemMeta();
        String name=micon.getDisplayName();
        Integer amount=Integer.parseInt(name.substring(name.indexOf("*")+1));

        String material=getResByLore(inv.getItem(42).getItemMeta().getLore());
        Double total=(double)amount*cfg.getPriceNow(material);
        List<String> xq = new ArrayList<>();

        xq.add("§a商品: §e" + micon.getDisplayName());
        xq.add("§a操作: §e" + maction.getDisplayName());
        xq.add("§a总价: §e" + df.format(total) + " §a" + MoneyUnit);
        //xq.add(" ");
        xq.add("§a现金: §e" + df.format(money) + " §a" + MoneyUnit);
        if(!maction.getDisplayName().contains("买")) {
            //卖
            xq.add("§a(+)§b交易后: §e" );
            xq.add("    §e" + df.format(money+total) +  MoneyUnit + " ");
            if(!ItemOperate.hasItem(p,icon.getType(),amount)) xq.add("§c§o物品不足！");
        } else {//买
            xq.add("§c(-)§b交易后: §e");
            xq.add("    §e" + df.format(money-total) + MoneyUnit + " ");
            if(money-total<0) xq.add("§c§o余额不足！");
        }
        minfo.setLore(xq);
        info.setItemMeta(minfo);
        inv.setItem(41,info);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onInventoryClose(InventoryCloseEvent event)
    {
        if(event.getInventory().getTitle().equalsIgnoreCase(GUITitle))
            event.getInventory().clear();
    }



    private void logtrade(Player player,String res, String action, Integer amount,  Double total)
    {

        if(action.equalsIgnoreCase("buy")){
            datas.newTradeBuy(res,amount);
        }
        if(action.equalsIgnoreCase("sell")){
            datas.newTradeSell(res,amount);
        }
        String s=String.format("[#%04d][P:%s,Action=%S] Info=%S*%d ,Total=%.2f ,MoneyNow=%.2f",
                datas.TradeCnt,player.getName(),action,res,amount,total,econ.getBalance(player));
        log(s);

    }
    private void log(String s)
    {
        if (this.logWatcher == null) { return; }
        final Date date = Calendar.getInstance().getTime();
        final Timestamp time = new Timestamp(date.getTime());
        this.logWatcher.add("[" + time.toString() + "] " + s);
    }
    public ConfigManager getConfigManager(){return this.cfg;}
    public DataManager getDataManager(){return this.datas;}
}
