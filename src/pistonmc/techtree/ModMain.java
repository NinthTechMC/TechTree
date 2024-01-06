package pistonmc.techtree;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import pistonmc.techtree.data.TechTree;
import pistonmc.techtree.event.MsgRegistry;
import pistonmc.techtree.mc7.ModClient;
import pistonmc.techtree.mc7.ModServer;

@Mod(modid = ModInfo.ID, version = ModInfo.VERSION)
public class ModMain
{
	public static Logger log = LogManager.getLogger("TechTree");
    public static void error(Throwable e) {
        log.error(e);
        e.printStackTrace();
    }
    public static interface IModInstance {
        public MsgRegistry getMsgRegistry();
        public ModServer getServer();
        public TechTree getTechTree();
        public void preInit(FMLPreInitializationEvent event);
        public void init(FMLInitializationEvent event);
        public void postInit(FMLPostInitializationEvent event);
        public void complete(FMLLoadCompleteEvent event);
    }


    @SidedProxy(clientSide = ModInfo.GROUP + ".mc7.ModClient", serverSide = ModInfo.GROUP + ".mc7.ModServer")
    public static IModInstance instance;

    @SideOnly(Side.CLIENT)
    public static ModClient getClient() {
        return (ModClient) instance;
    }

    public static ModServer getServer() {
        return instance.getServer();
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance.preInit(event);
    }
    @EventHandler
    public void init(FMLInitializationEvent event) {
        instance.init(event);
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        instance.postInit(event);
    }
    @EventHandler
    public void complete(FMLLoadCompleteEvent event) {
        instance.complete(event);
    }
}
