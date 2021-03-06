package com.untamedears.humbug;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Splitter;
import com.untamedears.humbug.annotations.BahHumbug;
import com.untamedears.humbug.annotations.BahHumbugs;
import com.untamedears.humbug.annotations.ConfigOption;

public class Config {
  private static Config global_instance_ = null;

  // ================================================
  // Configuration defaults
  private static final boolean debug_log_ = false;
  private static final int quartz_gravel_percentage_ = 0;
  private static final int cobble_from_lava_scan_radius_ = 0;
  private static final int projectile_slow_ticks_ = 100;
  private static final int loot_multiplier_ = 1;
  private static final String book_name_ = "Welcome to Devoted";
  private static final String book_author_ = "Bonkill";
  private static final String book_text_ =
      "    {|oWhat is Devoted?\n"
      + "\"A new type of Minecraft server built towards providing a competitive, high stakes, immersive experience controlled by the players.\"{|r\n"
      + "-ShadedJon, server owner\n}|"
      + "    {|oWhat is Devoted?{|r\n"
      + "{|lNOT just survival{|r\nAdmins are hands-off and only handle glitches and hackers\n"
      + "{|lNOT just chaos{|r\nMods allow players and towns to enforce their own rules\n"
      + "{|lNOT just roleplay{|r\nNobody pretends -- conflict is genuine and heated\n}|"
      + "    {|oBasic Mechanics{|r\n"
      + "-The world is a series of islands\n"
      + "-They're seperated by ocean.\n"
      + "-Limited chat range\n"
      + "-Respawn in random area unless you sleep\n"
      + "-Food grows slowly\n"
      + "-Mobs spawn sparsely\n"
      + "-Vanilla Nether\n"
      + "-Ore veins and vanilla ore spawning.\n}|"
      + "   {|oDevoted Mods{|r\n"
      + "   {|lCitadel{|r:\n Reinforces things so it takes numerous breaks to destroy them. Locks some things too.\n"
      + "{|lMore Info{|r:\n {|oWiki Page:{|r tinyurl.com/dv-citadel\n"
      + "{|oType \"/help citadel\" in chat to get commands}|"
      + "   {|lPrison Pearl{|r:\n Ender Pearls trap players in the end. Others can steal back your pearl and free you -- you always know where it is so they cannot hide it\n"
      + "{|lMore Info{|r:\n {|oWiki Page:{|r tinyurl.com/dv-prison}|"
      + "   {|lJuke Alert{|r:\n Creates 'Juke' blocks that record player activity in radius. If you steal, grief, or trespass -- people will know about it & put a bounty for you to be pearled.\n"
      + "{|lMore Info{|r:\n {|oWiki Page:{|r tinyurl.com/dv-snitch}|"
      + "   {|lMythicMobs{|r:\n Some custom mobs to increase the risk and thrill on specific areas throughout Devoted.\n"
      + "{|lMore Info:{|r\n {|oBukkit Page:{|r dev.bukkit.org/bukkit-plugins/mythicmobs\n}|"
      + "   {|lItem Exchange{|r:\n A minecraft shop mod that enables different chests (and other inventory blocks) to perform an exchange of items with a player.\n"
      + "{|lMore Info:{|r\n {|oWiki Page:{|r tinyurl.com/itemxchng\n}|"
      + "   {|lHumbug{|r:\n Disables some features of minecraft -- see wiki for short list. Please read it to ensure you don't waste resources on a useless block.\n"
      + "{|lMore Info:{|r {|oWiki Page:{|r tinyurl.com/humbugwiki\n}|"
      + "  {|lRealistic Biomes{|r:\n Biomes are large, crops grow different in different biomes. Hit ground with seed to see growth rate. Farms need sunlight. Crops grow with nobody around.\n"
      + "{|lMore Info:{|r {|oWiki Page:{|r tinyurl.com/realbiome\n}|"
      + "  {|lArthropod Egg{|r:\n Allows you to capture passive mobs into spawn eggs using Bane of Arthropod enchantment.\n"
      + "{|lMore Info:{|r {|oGithub Page:{|r tinyurl.com/arthrop\n}|"
      + "  {|lBrewery{|r:\n Custom brewing of booze and more.\n"
      + "{|lMore Info:{|r {|oBukkit Page:{|r tinyurl.com/bukkitbrewp\n}|"
      + "   {|oFurther Info{|r\n"
      + "Visit our subreddit at: {|oreddit.com/r/Devoted{|r\n}|"
      + "We extend our appreciation to the devs, admins, and players at: {|oreddit.com/r/Civcraft{|r\n";
  private static final Iterable<String> compiled_book_text_ =
      Splitter.on("}|").split(book_text_.replaceAll("\\{\\|", "\u00A7"));

  private static final String holiday_book_name_ = "Happy Holidays";
  private static final String holiday_book_author_ = "the CivCraft Admins";
  private static final String holiday_book_text_ =
      "    {|2H{|4a{|2p{|4p{|2y {|4H{|2o{|4l{|2i{|4d{|2a{|4y{|2s{|4!{|0\n"
      + "Thank each and every one of you for making the server what it is. "
      + "Our best wishes go out to you and yours. May your New Year "
      + "be full of griefing, drama, and mayhem.\n\n"
      + "-Santa Ttk2 and the Admin Elves";
  private static final Iterable<String> compiled_holiday_book_text_ =
      Splitter.on("}|").split(holiday_book_text_.replaceAll("\\{\\|", "\u00A7"));

  private static FileConfiguration config_ = null;

  public static Config initialize(Humbug plugin) {
    if (global_instance_ == null) {
      plugin.reloadConfig();
      config_ = plugin.getConfig();
      config_.options().copyDefaults(true);
      global_instance_ = new Config(plugin);
      global_instance_.load();
    }
    return global_instance_;
  }

  public static ConfigurationSection getStorage() {
    return config_;
  }

  private Humbug plugin_ = null;
  private Set<Integer> remove_item_drops_ = null;

  public Config(Humbug plugin) {
    plugin_ = plugin;
    scanAnnotations();
  }

  private Map<String, ConfigOption> dynamicOptions_ = new TreeMap<String, ConfigOption>();

  private void addToConfig(BahHumbug bug) {
    if (dynamicOptions_.containsKey(bug.opt())) {
      Humbug.info("Duplicate configuration option detected: " + bug.opt());
      return;
    }
    dynamicOptions_.put(bug.opt(), new ConfigOption(bug));
    
    Humbug.info(" - " + bug.opt());
  }

  private void scanAnnotations() {
    try {
      Humbug.info("Options:");
    	
      for (Method method : Humbug.class.getMethods()) {
        BahHumbug bug = method.getAnnotation(BahHumbug.class);
        if (bug != null) {
          addToConfig(bug);
          continue;
        }
        BahHumbugs bugs = method.getAnnotation(BahHumbugs.class);
        if (bugs != null) {
          for (BahHumbug drone : bugs.value()) {
            addToConfig(drone);
          }
          continue;
        }
      }
    } catch(Exception ex) {
      Humbug.info(ex.toString());
    }
  }

  public void load() {
    // Setting specific initialization
    loadRemoveItemDrops();
  }

  public void reload() {
    plugin_.reloadConfig();
  }

  public void save() {
    plugin_.saveConfig();
  }

  public ConfigOption get(String optionName) {
    return dynamicOptions_.get(optionName);
  }

  public boolean set(String optionName, String value) {
    ConfigOption opt = dynamicOptions_.get(optionName);
    if (opt != null) {
      opt.setString(value);
      return true;
    }
    return false;
  }

  public boolean getDebug() {
    return config_.getBoolean("debug", debug_log_);
  }

  public void setDebug(boolean value) {
    config_.set("debug", value);
  }

  public String getTitle(){
    return config_.getString("noobbook.name", book_name_);
  }

  public String getAuthor(){
    return config_.getString("noobbook.author", book_author_);
  }

  public List<String> getPages(){
    List<String> book_pages = new LinkedList<String>();
    for(final String text: compiled_book_text_){
      book_pages.add(text);
    }
    return book_pages;
  }

  public String getHolidayTitle(){
    return holiday_book_name_;
  }

  public String getHolidayAuthor(){
    return holiday_book_author_;
  }

  public List<String> getHolidayPages(){
    List<String> book_pages = new LinkedList<String>();
    for(final String text: compiled_holiday_book_text_){
      book_pages.add(text);
    }
    return book_pages;
  }

  public int getLootMultiplier(String entity_type){
    return config_.getInt("loot_multiplier." + entity_type.toLowerCase(), loot_multiplier_);
  }

  public void setLootMultiplier(String entity_type, int value){
    config_.set("loot_multiplier." + entity_type.toLowerCase(), value);
  }


  public int getQuartzGravelPercentage() {
    return config_.getInt("quartz_gravel_percentage", quartz_gravel_percentage_);
  }

  public void setQuartzGravelPercentage(int value) {
    if (value < 0) {
      value = 0;
      Humbug.warning("quartz_gravel_percentage adjusted to 0");
    } else if (value > 100) {
      value = 100;
      Humbug.warning("quartz_gravel_percentage adjusted to 100");
    }
    config_.set("quartz_gravel_percentage", value);
  }


  public int getCobbleFromLavaScanRadius() {
    return config_.getInt("cobble_from_lava_scan_radius", cobble_from_lava_scan_radius_);
  }

  public void setCobbleFromLavaScanRadius(int value) {
    if (value < 0) {
      value = 0;
      Humbug.warning("cobble_from_lava_scan_radius adjusted to 0");
    } else if (value > 20) {  // 8000 blocks to scan at 20
      value = 20;
      Humbug.warning("cobble_from_lava_scan_radius adjusted to 20");
    }
    config_.set("cobble_from_lava_scan_radius", value);
  }


  public int getProjectileSlowTicks() {
    int ticks = config_.getInt("projectile_slow_ticks", projectile_slow_ticks_);
    if (ticks <= 0 || ticks > 600) {
      ticks = 100;
    }
    return ticks;
  }
  

  private void loadRemoveItemDrops() {
    if (!config_.isSet("remove_mob_drops")) {
      remove_item_drops_ = new HashSet<Integer>(4);
      return;
    }
    remove_item_drops_ = new HashSet<Integer>();
    if (!config_.isList("remove_mob_drops")) {
      Integer val = config_.getInt("remove_mob_drops");
      if (val == null) {
        config_.set("remove_mob_drops", new LinkedList<Integer>());
        Humbug.info("remove_mob_drops was invalid, reset");
        return;
      }
      remove_item_drops_.add(val);
      List<Integer> list = new LinkedList<Integer>();
      list.add(val);
      config_.set("remove_mob_drops", val);
      Humbug.info("remove_mob_drops was not an Integer list, converted");
      return;
    }
    remove_item_drops_.addAll(config_.getIntegerList("remove_mob_drops"));
  }

  public boolean doRemoveItemDrops() {
    return !remove_item_drops_.isEmpty();
  }

  public Set<Integer> getRemoveItemDrops() {
    return Collections.unmodifiableSet(remove_item_drops_);
  }

  public void addRemoveItemDrop(int item_id) {
    if (item_id < 0) {
      return;
    }
    remove_item_drops_.add(item_id);
    List<Integer> list;
    if (!config_.isSet("remove_mob_drops")) {
      list = new LinkedList<Integer>();
    } else {
      list = config_.getIntegerList("remove_mob_drops");
    }
    list.add(item_id);
    config_.set("remove_mob_drops", list);
  }

  public void removeRemoveItemDrop(int item_id) {
    if (item_id < 0) {
      return;
    }
    if (!remove_item_drops_.remove(item_id)) {
      return;
    }
    List<Integer> list = config_.getIntegerList("remove_mob_drops");
    list.remove((Object)item_id);
    config_.set("remove_mob_drops", list);
  }

  public void setRemoveItemDrops(Set<Integer> item_ids) {
    remove_item_drops_ = new HashSet<Integer>();
    remove_item_drops_.addAll(item_ids);
    List<Integer> list = new LinkedList<Integer>();
    list.addAll(item_ids);
    config_.set("remove_mob_drops", list);
  }

  public String toDisplayRemoveItemDrops() {
    StringBuilder sb = new StringBuilder();
    for (Integer item_id : remove_item_drops_) {
      Material mat = Material.getMaterial(item_id);
      if (mat == null) {
        sb.append(item_id);
      } else {
        sb.append(mat.toString());
      }
      sb.append(",");
    }
    return sb.toString();
  }
  
  public void tag_on_join(boolean value){
	  config_.set("tag_on_join", value);
  }

  public List<ItemStack> getStartingKit() {
    List<?> bsk = config_.getList("newbie_kit");
	if (bsk != null) {
      return (List<ItemStack>) bsk;
	}
	return null;
  }

  public void setDefaultStartingKit() {
    List<ItemStack> kit = new LinkedList<ItemStack>();
	ItemStack def = new ItemStack(Material.getMaterial("COOKIE"), 32);
	ItemMeta meta = def.getItemMeta();
	meta.setDisplayName("Manna");
	List<String> lore = new LinkedList<String>();
	lore.add("Gift from the Admins as you");
	lore.add("begin your journey on Devoted");
	meta.setLore(lore);
	def.setItemMeta(meta);
	kit.add(def);
	setStartingKit(kit);
  }

  public void setStartingKit(List<ItemStack> kit) {
	config_.set("newbie_kit", kit);
  }
}
