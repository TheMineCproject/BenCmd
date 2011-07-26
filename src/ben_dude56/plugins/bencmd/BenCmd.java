package ben_dude56.plugins.bencmd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ben_dude56.plugins.bencmd.chat.*;
import ben_dude56.plugins.bencmd.chat.channels.*;
import ben_dude56.plugins.bencmd.invisible.*;
import ben_dude56.plugins.bencmd.invtools.*;
import ben_dude56.plugins.bencmd.invtools.kits.*;
import ben_dude56.plugins.bencmd.lots.*;
import ben_dude56.plugins.bencmd.lots.sparea.*;
import ben_dude56.plugins.bencmd.maps.*;
import ben_dude56.plugins.bencmd.money.*;
import ben_dude56.plugins.bencmd.multiworld.*;
import ben_dude56.plugins.bencmd.nofly.*;
import ben_dude56.plugins.bencmd.permissions.*;
import ben_dude56.plugins.bencmd.permissions.MaxPlayers.JoinType;
import ben_dude56.plugins.bencmd.protect.*;
import ben_dude56.plugins.bencmd.reporting.*;
import ben_dude56.plugins.bencmd.warps.*;
import ben_dude56.plugins.bencmd.weather.*;
import ben_dude56.plugins.bencmd.advanced.*;
import ben_dude56.plugins.bencmd.advanced.bank.*;
import ben_dude56.plugins.bencmd.advanced.npc.*;
import ben_dude56.plugins.bencmd.advanced.redstone.*;

/**
 * BenCmd for Bukkit
 * 
 * @author ben_dude56
 * 
 */
public class BenCmd extends JavaPlugin {
	public final static boolean debug = true;
	public final static int cbbuild = 1031;
	private final PermLoginListener permLoginListener = new PermLoginListener(
			this);
	private final InventoryBlockListener invBlockListen = new InventoryBlockListener(
			this);
	private final DeathListener death = new DeathListener(this);
	private final InventoryPlayerListener invPlayerListen = new InventoryPlayerListener(
			this);
	private final ProtectPlayerListener ppListen = new ProtectPlayerListener(
			this);
	private final ProtectBlockListener pbListen = new ProtectBlockListener(this);
	private final EntityPermListen entListen = new EntityPermListen(this);
	public final LotPlayerListener lotListener = new LotPlayerListener(this);
	public final LotBlockListener lotBListener = new LotBlockListener(this);
	public final WeatherPListener wpListen = new WeatherPListener(this);
	public final PortalListener portalListen = new PortalListener(this);
	public final ShelfPListener shelflp = new ShelfPListener(this);
	public final ShelfBListener shelflb = new ShelfBListener(this);
	public final SPAreaPListener spaplisten = new SPAreaPListener(this);
	public final SPAreaEListener spaelisten = new SPAreaEListener(this);
	public final FlyListener flyListen = new FlyListener(this);
	public final NPCListener npcl = new NPCListener(this);
	public final NPCChunkListener npccl = new NPCChunkListener(this);
	public final HashMap<Player, Boolean> godmode = new HashMap<Player, Boolean>();
	public final List<Player> invisible = new ArrayList<Player>();
	public final List<Player> noinvisible = new ArrayList<Player>();
	public final List<Player> allinvisible = new ArrayList<Player>();
	public final List<ActionableUser> offline = new ArrayList<ActionableUser>();
	public final String propDir = "plugins/BenCmd/";
	public final File mainProp = new File(propDir + "main.properties");
	public final File itemAlias = new File(propDir + "items.txt");
	public final File unlDisp = new File(propDir + "disp.db");
	public final File dispChests = new File(propDir + "chest.db");
	public final File lotFile = new File(propDir + "lots.db");
	public final File kitFile = new File(propDir + "kits.db");
	public final File proFile = new File(propDir + "protection.db");
	public final File chatFile = new File(propDir + "channels.db");
	public final File ticketFile = new File(propDir + "tickets.db");
	public final File pricesFile = new File(propDir + "prices.db");
	public final File portalFile = new File(propDir + "portals.db");
	public final File shelfFile = new File(propDir + "shelves.db");
	public final File actionFile = new File(propDir + "action.db");
	public final File spareaFile = new File(propDir + "sparea.db");
	public final File bankFile = new File(propDir + "bank.db");
	public final File NPCFile = new File(propDir + "npc.db");
	public final File RFile = new File(propDir + "lever.db");
	public PluginProperties mainProperties;
	public PluginProperties itemAliases;
	public LotFile lots;
	public boolean timeRunning = true;
	public long lastTime = 0;
	public long timeFrozenAt = 0;
	public boolean heroActive = false;
	public MainPermissions perm;
	public WarpList warps;
	public HomeWarps homes;
	public PreWarp checkpoints;
	public ChatPlayerListener chatListen;
	public BlockChecker blockCheck;
	public Jail jail;
	public UnlimitedDisp dispensers;
	public DispChest chests;
	public KitList kits;
	public CreeperListener creeperListen;
	public Invisibility inv;
	public ProtectFile protectFile;
	public MaxPlayers maxPlayers;
	public ChatChannelController channels;
	public ReportFile reports;
	public FlyDetect flyDetect;
	public WeatherBinding strikeBind;
	public PriceFile prices;
	public KickList kicked;
	public PortalFile portals;
	public ShelfFile shelff;
	public ActionFile actions;
	public SPAreaFile spafile;
	public BankFile banks;
	public NPCFile npcs;
	public RedstoneFile levers;
	public List<Location> canSpread = new ArrayList<Location>();
	public List<Grave> graves = new ArrayList<Grave>();
	public HashMap<Player, List<ItemStack>> returns = new HashMap<Player, List<ItemStack>>();
	public Logger bLog = Logger.getLogger("Minecraft.BenCmd");
	public FileHandler fh;
	public Logger log = Logger.getLogger("Minecraft");
	public Calendar clog;

	public boolean checkID(int id) {
		for (Material item : Material.values()) {
			if (item.getId() == id) {
				return true;
			}
		}
		return false;
	}

	public boolean canIgnite(Location loc) {
		for (Location loc2 : canSpread) {
			if (loc.getBlockX() < loc2.getBlockX() + 3
					&& loc.getBlockX() > loc2.getBlockX() - 3) {
				if (loc.getBlockY() < loc2.getBlockY() + 3
						&& loc.getBlockY() > loc2.getBlockY() - 3) {
					if (loc.getBlockZ() < loc2.getBlockZ() + 3
							&& loc.getBlockZ() > loc2.getBlockZ() - 3) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	/**
	 * Disables the plugin
	 */
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		for (Grave g : graves) {
			g.delete();
		}
		graves.clear();
		for (SPArea a : spafile.listAreas()) {
			a.delete();
		}
		banks.saveAll();
		for (NPC npc : npcs.allNPCs()) {
			npc.despawn();
		}
		PluginDescriptionFile pdfFile = this.getDescription();
		log.info(pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " has been disabled!");
	}
	
	public void rotateLogFile() {
		log.info("BenCmd log file is rotating...");
		try {
			String logName = "";
			Calendar c = Calendar.getInstance();
			clog = c;
			logName += c.get(Calendar.MONTH);
			logName += "-" + c.get(Calendar.DAY_OF_MONTH);
			logName += "-" + c.get(Calendar.YEAR);
			bLog.setLevel(Level.INFO);
			bLog.setUseParentHandlers(false);
			fh = new FileHandler(propDir + logName + ".log", true);
			fh.setFormatter(new LogFormatter());
			bLog.addHandler(fh);
		} catch (IOException e) {
			log.severe("Unable to create/load log files. Some logging functions may not work properly!");
		}
	}

	@Override
	/**
	 * Initializes the plugin for general use.
	 */
	public void onEnable() {
		try {
			String logName = "";
			Calendar c = Calendar.getInstance();
			clog = c;
			logName += c.get(Calendar.MONTH);
			logName += "-" + c.get(Calendar.DAY_OF_MONTH);
			logName += "-" + c.get(Calendar.YEAR);
			bLog.setLevel(Level.INFO);
			bLog.setUseParentHandlers(false);
			fh = new FileHandler(propDir + logName + ".log", true);
			fh.setFormatter(new LogFormatter());
			bLog.addHandler(fh);
		} catch (IOException e) {
			log.severe("Unable to create/load log files. Some logging functions may not work properly!");
		}
		bLog.info("BenCmd log ready! Running BenCmd v" + getDescription().getVersion());
		// Check for missing files and add them if necessary
		bLog.info("Checking for missing database files...");
		new File(propDir).mkdirs();
		if (!mainProp.exists()) {
			bLog.info("\"main.properties\" missing... Attempting to create...");
			try {
				mainProp.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"main.properties\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!itemAlias.exists()) {
			bLog.info("\"items.txt\" missing... Attempting to create...");
			try {
				itemAlias.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"items.txt\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!unlDisp.exists()) {
			bLog.info("\"disp.db\" missing... Attempting to create...");
			try {
				unlDisp.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"disp.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!dispChests.exists()) {
			bLog.info("\"chest.db\" missing... Attempting to create...");
			try {
				dispChests.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"chest.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!lotFile.exists()) {
			bLog.info("\"lots.db\" missing... Attempting to create...");
			try {
				lotFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"lots.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!kitFile.exists()) {
			bLog.info("\"kits.db\" missing... Attempting to create...");
			try {
				kitFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"kits.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!proFile.exists()) {
			bLog.info("\"protection.db\" missing... Attempting to create...");
			try {
				proFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"protection.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!chatFile.exists()) {
			bLog.info("\"channels.db\" missing... Attempting to create...");
			try {
				chatFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"channels.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!ticketFile.exists()) {
			bLog.info("\"tickets.db\" missing... Attempting to create...");
			try {
				ticketFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"tickets.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!pricesFile.exists()) {
			bLog.info("\"prices.db\" missing... Attempting to create...");
			try {
				pricesFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"prices.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!portalFile.exists()) {
			bLog.info("\"portals.db\" missing... Attempting to create...");
			try {
				portalFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"portals.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!shelfFile.exists()) {
			bLog.info("\"shelves.db\" missing... Attempting to create...");
			try {
				shelfFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"shelves.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!actionFile.exists()) {
			bLog.info("\"action.db\" missing... Attempting to create...");
			try {
				actionFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"action.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!spareaFile.exists()) {
			bLog.info("\"sparea.db\" missing... Attempting to create...");
			try {
				spareaFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"sparea.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!bankFile.exists()) {
			bLog.info("\"bank.db\" missing... Attempting to create...");
			try {
				bankFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"bank.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!NPCFile.exists()) {
			bLog.info("\"npc.db\" missing... Attempting to create...");
			try {
				NPCFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"npc.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		if (!RFile.exists()) {
			bLog.info("\"lever.db\" missing... Attempting to create...");
			try {
				RFile.createNewFile();
			} catch (IOException e) {
				bLog.log(Level.SEVERE, "Error creating \"lever.db\":", e);
				System.out.println("BenCmd had a problem:");
				e.printStackTrace();
			}
		}
		// Get some static methods ready
		User.finalizeAll();
		// Start loading classes
		bLog.info("Loading databases...");
		perm = new MainPermissions(this);
		warps = new WarpList(this);
		homes = new HomeWarps(this);
		checkpoints = new PreWarp(this);
		blockCheck = new BlockChecker(this);
		mainProperties = new PluginProperties(propDir + "main.properties");
		mainProperties.loadFile();
		itemAliases = new PluginProperties(propDir + "items.txt");
		itemAliases.loadFile();
		chatListen = new ChatPlayerListener(this);
		jail = new Jail(this);
		dispensers = new UnlimitedDisp(propDir + "disp.db");
		chests = new DispChest(propDir + "chest.db");
		lots = new LotFile(this);
		kits = new KitList(this);
		creeperListen = new CreeperListener(this);
		inv = new Invisibility(this);
		protectFile = new ProtectFile(this, propDir + "protection.db");
		channels = new ChatChannelController(propDir + "channels.db", this);
		maxPlayers = new MaxPlayers(this, mainProperties.getInteger(
				"maxPlayers", 10), mainProperties.getInteger("maxReserve", 4),
				mainProperties.getBoolean("reserveActive", true),
				mainProperties.getBoolean("indefActive", true));
		reports = new ReportFile(this);
		flyDetect = new FlyDetect(this);
		strikeBind = new WeatherBinding(this);
		prices = new PriceFile(this, propDir + "prices.db");
		kicked = new KickList(this);
		portals = new PortalFile(this, propDir + "portals.db");
		shelff = new ShelfFile(this, propDir + "shelves.db");
		actions = new ActionFile(this);
		spafile = new SPAreaFile(this, propDir + "sparea.db");
		banks = new BankFile(this, propDir + "bank.db");
		npcs = new NPCFile(this, propDir + "npc.db");
		levers = new RedstoneFile(this, propDir + "lever.db");
		bLog.info("Performing configuration check...");
		// SANITY CHECK
		if (!sanityCheck()) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		// Check for existing players (on reload) and add them to the maxPlayers
		// class
		for (Player player : this.getServer().getOnlinePlayers()) {
			User user;
			JoinType jt = maxPlayers.join(user = User.getUser(this, player));
			if (jt == JoinType.NO_SLOT_NORMAL
					|| jt == JoinType.NO_SLOT_RESERVED) {
				user.Kick("The server ran out of player slots when reloading... :(");
			}
		}
		bLog.info("Registering events...");
		// Register all necessary events
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.chatListen,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_LOGIN, this.permLoginListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.chatListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockCheck,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockCheck,
				Event.Priority.High, this);
		pm.registerEvent(Event.Type.BLOCK_BURN, this.blockCheck,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_IGNITE, this.blockCheck,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockCheck,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.invPlayerListen,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.invBlockListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.death,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.death,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.lotListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.REDSTONE_CHANGE, this.invBlockListen,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.lotBListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.lotBListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, this.permLoginListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, this.permLoginListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, this.creeperListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.pbListen,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.ppListen,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this.lotListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this.lotListener,
				Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.chatListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.chatListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_KICK, this.chatListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.EXPLOSION_PRIME, this.entListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.wpListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_PORTAL, this.portalListen,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.shelflp,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.shelflb,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this.spaplisten,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.spaplisten,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.spaelisten,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.spaelisten,
				Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_TELEPORT, this.flyListen,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_PORTAL, this.flyListen,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, this.npcl,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.CHUNK_LOAD, this.npccl,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, this.npccl,
				Event.Priority.Monitor, this);
		pm.registerEvent(Event.Type.WORLD_SAVE, this.npccl,
				Event.Priority.Monitor, this);
		PluginDescriptionFile pdfFile = this.getDescription();
		// Prepare the time lock timer
		bLog.info("Preparing time task...");
		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new TimeFreeze(), 100, 100);
		bLog.info("BenCmd enabled successfully!");
		log.info(pdfFile.getName() + " v" + pdfFile.getVersion()
				+ " has been enabled!");
		for (World w : getServer().getWorlds()) {
			w.setPVP(true);
		}
		if (mainProperties.getBoolean("channelsEnabled", false)) {
			log.warning("BenCmd Chat Channels (Experimental) are active...");
		}
	}

	/**
	 * Used to add or remove a player's god status
	 * 
	 * @param player
	 *            The player who's status should be edited
	 * @param value
	 *            Whether the player would be godded
	 */
	public void setGod(final Player player, final boolean value) {
		if (value) {
			godmode.put(player, value);
			player.setHealth(20);
		} else {
			godmode.remove(player);
		}
	}

	/**
	 * Checks if a player has god status
	 * 
	 * @param player
	 *            The player to check the status of
	 * @return Returns whether or not the player has god status
	 */
	public boolean isGod(final Player player) {
		if (godmode.containsKey(player)) {
			return godmode.get(player);
		} else {
			return false;
		}
	}

	public String[] fatalconflicts = new String[] { "Essentials", "KeepChest",
			"OPImmunity", "Humiliation", "Wrath", "CenZor", "xGive", "getID",
			"ItemDrop", "Reporter", "Spyer", "WeatherGod", "TweakedCycle",
			"DefaultCommands", "Prefixer", "RegexFilter", "iChat", "nChat",
			"ColorMe", "SimpleCensor", "Silence", "Chat Color",
			"SimpleWhisper", "Colors", "On Request", "iOP", "OffLine", "mGold",
			"StockCraft" };
	public String[] warningconflicts = new String[] { "WorldGuard", "Jail",
			"PlgSetspawn", "GiveTo", "SpawnCreature", "CreatureSpawner",
			"FullChest", "SpawnMob", "SimpleSpawn", "AdminCmd", "StruckDown",
			"Requests", "EasyShout", "DeathTpPlus", "iConomy", "RepairChest",
			"BukkitCompat"};
	public String[] minorconflicts = new String[] { "MessageChanger",
			"NoMoreRain", "kcpxBukkit", "Regios", "ClothCommand", "ChatCensor",
			"Permissions", "DeathSigns" };

	public boolean sanityCheck() {
		if (debug) {
			log.warning("You are running a version of BenCmd marked for DEBUGGING ONLY! Use of this version may cause world/database corruption. Use at your own risk!");
		}
		Integer v = null;
		try {
			v = Integer.parseInt(getServer().getVersion().split("-")[5]
					.split(" ")[0].replace("b", "").replace("jnks", ""));
		} catch (IndexOutOfBoundsException e) {
			log.severe("Cannot determine CraftBukkit build... Version check will be skipped...");
		} catch (NumberFormatException e) {
			log.severe("Cannot determine CraftBukkit build... Version check will be skipped...");
		}
		if (v == null) {
			// Do nothing
		} else if (v < cbbuild) {
			log.warning("You are using a version of CraftBukkit that is earlier than this version of BenCmd was built against. This may cause unexpected problems... Run AT YOUR OWN RISK!");
		} else if (v > cbbuild) {
			log.warning("You are using a version of CraftBukkit that is newer than this version of BenCmd was built against. This may cause unexpected problems... Run AT YOUR OWN RISK!");
		}
		PluginManager pm = getServer().getPluginManager();
		int result = -1;
		for (String plugin : fatalconflicts) {
			if (pm.getPlugin(plugin) != null) {
				log.severe("BenCmd Plugin Check: " + plugin
						+ " can cause major problems with BenCmd.");
				result = 2;
			}
		}
		for (String plugin : warningconflicts) {
			if (pm.getPlugin(plugin) != null) {
				log.warning("BenCmd Plugin Check: " + plugin
						+ " can cause some command conflicts with BenCmd.");
				if (result == -1) {
					result = 1;
				}
			}
		}
		for (String plugin : minorconflicts) {
			if (pm.getPlugin(plugin) != null) {
				log.info("BenCmd Plugin Check: " + plugin
						+ " may cause minor conflicts with BenCmd.");
				if (result == -1) {
					result = 0;
				}
			}
		}
		if (result == 2) {
			if (mainProperties.getInteger("pluginCheckFailLevel", 1) <= 2) {
				log.severe("BenCmd Plugin Conflicts have caused BenCmd to automatically shut down.");
				log.severe("You can override this by changing pluginCheckFailLevel in main.properties to 3 or higher...");
				return false;
			}
		} else if (result == 1) {
			if (mainProperties.getInteger("pluginCheckFailLevel", 1) <= 1) {
				log.severe("BenCmd Plugin Conflicts have caused BenCmd to automatically shut down.");
				log.severe("You can override this by changing pluginCheckFailLevel in main.properties to 2 or higher...");
				return false;
			}
		} else if (result == 0) {
			if (mainProperties.getInteger("pluginCheckFailLevel", 1) <= 0) {
				log.severe("BenCmd Plugin Conflicts have caused BenCmd to automatically shut down.");
				log.severe("You can override this by changing pluginCheckFailLevel in main.properties to 1 or higher...");
				return false;
			}
		}
		return true;
	}

	public boolean onCommand(CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (new BasicCommands(this).onCommand(sender, command, commandLabel,
				args)) {
			return true;
		} else if (new ChatCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new PermissionCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new WarpCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new InventoryCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new LotCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new InvisibleCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new ProtectedCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new ChatChannelCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new ReportCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new WeatherCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new MoneyCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new MapCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new PortalCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new AdvancedCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new BankCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new NPCCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else if (new RedstoneCommands(this).onCommand(sender, command,
				commandLabel, args)) {
			return true;
		} else {
			User user;
			try {
				user = User.getUser(this, (Player) sender);
			} catch (ClassCastException e) {
				user = User.getUser(this);
			}
			user.sendMessage(ChatColor.RED
					+ "You don't have permission to do that!");
			return true;
		}
	}

	public class TimeFreeze implements Runnable {
		@Override
		public void run() {
			if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) != clog.get(Calendar.DAY_OF_MONTH)) {
				rotateLogFile();
			}
			levers.timeTick();
			if (!timeRunning) {
				for (World world : getServer().getWorlds()) {
					world.setTime(timeFrozenAt);
				}
			} else {
				if (lastTime == 0) {
					lastTime = getServer().getWorlds().get(0).getFullTime();
					return;
				}
				for (World world : getServer().getWorlds()) {
					if (world.getTime() >= 0 && world.getTime() < 12000) {
						world.setFullTime(lastTime
								+ mainProperties.getInteger("daySpeed", 100));
					} else {
						world.setFullTime(lastTime
								+ mainProperties.getInteger("nightSpeed", 100));
					}
				}
				lastTime = getServer().getWorlds().get(0).getFullTime();
			}
		}
	}

}
