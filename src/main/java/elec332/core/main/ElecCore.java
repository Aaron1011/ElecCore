package elec332.core.main;

import elec332.core.api.IElecCoreMod;
import elec332.core.api.data.IExternalSaveHandler;
import elec332.core.api.module.IModuleController;
import elec332.core.api.network.ModNetworkHandler;
import elec332.core.api.registry.ISingleRegister;
import elec332.core.effects.AbilityHandler;
import elec332.core.grid.internal.GridEventHandler;
import elec332.core.grid.internal.GridEventInputHandler;
import elec332.core.handler.ModEventHandler;
import elec332.core.handler.TickHandler;
import elec332.core.network.IElecNetworkHandler;
import elec332.core.network.packets.PacketReRenderBlock;
import elec332.core.network.packets.PacketSyncWidget;
import elec332.core.network.packets.PacketTileDataToServer;
import elec332.core.network.packets.PacketWidgetDataToServer;
import elec332.core.proxies.CommonProxy;
import elec332.core.server.SaveHandler;
import elec332.core.server.ServerHelper;
import elec332.core.util.FileHelper;
import elec332.core.util.LoadTimer;
import elec332.core.util.MCModInfo;
import elec332.core.util.OredictHelper;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by Elec332.
 */
@Mod(modid = ElecCore.MODID, name = ElecCore.MODNAME, dependencies = "required-after:Forge@[12.18.1.2073,);after:forestry;",
acceptedMinecraftVersions = "[1.10,)", version = ElecCore.ElecCoreVersion, useMetadata = true)
public class ElecCore implements IModuleController, IElecCoreMod {

	public static final String ElecCoreVersion = "#ELECCORE_VER#";
	public static final String MODID = "ElecCore";
	public static final String MODNAME = "ElecCore";

	@SidedProxy(clientSide = "elec332.core.proxies.ClientProxy", serverSide = "elec332.core.proxies.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance(MODID)
	public static ElecCore instance;
	@ModNetworkHandler
	public static IElecNetworkHandler networkHandler;
	public static TickHandler tickHandler;
	public static Logger logger;
	protected ElecCoreDiscoverer asmDataProcessor;
	private Configuration config;
	private LoadTimer loadTimer;
	private ModEventHandler modEventHandler;

	public static final boolean developmentEnvironment;
	public static boolean debug = false;
	public static boolean removeJSONErrors = true;

	@EventHandler
	public void construction(FMLConstructionEvent event){
		logger = LogManager.getLogger("ElecCore");
		asmDataProcessor = new ElecCoreDiscoverer();
		asmDataProcessor.identify(event.getASMHarvestedData());
		asmDataProcessor.process(LoaderState.CONSTRUCTING);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ElecModHandler.identifyMods();
		ElecModHandler.initAnnotations(event.getAsmData());
		loadTimer = new LoadTimer(logger, MODNAME);
		loadTimer.startPhase(event);
		this.config = new Configuration(FileHelper.getConfigFileElec(event));
		tickHandler = new TickHandler();
		networkHandler.registerClientPacket(PacketSyncWidget.class);
		networkHandler.registerServerPacket(PacketTileDataToServer.class);
		networkHandler.registerServerPacket(PacketWidgetDataToServer.class);
		networkHandler.registerClientPacket(PacketReRenderBlock.class);

		MinecraftForge.EVENT_BUS.register(tickHandler);
		debug = config.getBoolean("debug", Configuration.CATEGORY_GENERAL, false, "Set to true to print debug info to the log.");
		removeJSONErrors = config.getBoolean("removeJsonExceptions", Configuration.CATEGORY_CLIENT, true, "Set to true to remove all the Json model errors from the log.") && !developmentEnvironment;
		ServerHelper.instance.load();

		MinecraftForge.EVENT_BUS.register(new GridEventHandler());

		proxy.preInitRendering();
		asmDataProcessor.process(LoaderState.PREINITIALIZATION);

		modEventHandler.postEvent(event);

		loadTimer.endPhase(event);
		MCModInfo.createMCModInfoElec(event, "Provides core functionality for Elec's Mods",
				"-", "assets/elec332/logo.png", new String[]{"Elec332"});
	}


	@EventHandler
	@SuppressWarnings("unchecked")
    public void init(FMLInitializationEvent event) {
		loadTimer.startPhase(event);
		config.load();
		if (config.hasChanged()){
			config.save();
		}
		ElecCoreRegistrar.dummyLoad();
		SaveHandler.INSTANCE.dummyLoad();
		AbilityHandler.instance.init();
		ElecModHandler.init();
		asmDataProcessor.process(LoaderState.INITIALIZATION);
		OredictHelper.initLists();
		modEventHandler.postEvent(event);
		loadTimer.endPhase(event);
    }

	@EventHandler
	public void postInit(FMLPostInitializationEvent event){
		loadTimer.startPhase(event);
		asmDataProcessor.process(LoaderState.POSTINITIALIZATION);
		OredictHelper.initLists();
		proxy.postInitRendering();
		modEventHandler.postEvent(event);
		loadTimer.endPhase(event);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event){
		loadTimer.startPhase(event);
		asmDataProcessor.process(LoaderState.AVAILABLE);
		OredictHelper.initLists();
		modEventHandler.postEvent(event);
		loadTimer.endPhase(event);
	}

	@EventHandler
	public void onServerAboutToStart(FMLServerAboutToStartEvent event){
		GridEventInputHandler.INSTANCE.reloadHandlers();
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStarted(FMLServerStartedEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStopping(FMLServerStoppingEvent event){
		modEventHandler.postEvent(event);
	}

	@EventHandler
	public void onServerStopped(FMLServerStoppedEvent event){
		modEventHandler.postEvent(event);
	}

	@Override
	public void registerSaveHandlers(ISingleRegister<IExternalSaveHandler> saveHandlerRegistry) {
		saveHandlerRegistry.register(ServerHelper.instance);
	}

	public static void systemPrintDebug(Object s){
		if (debug) {
			System.out.println(s);
		}
	}

	@Override
	public boolean isModuleEnabled(String moduleName) {
		return true;
	}

	public void setModEventHandler(ModEventHandler handler){
		if (this.modEventHandler != null){
			throw new IllegalStateException();
		}
		this.modEventHandler = handler;
	}

	static {
		developmentEnvironment = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

}
