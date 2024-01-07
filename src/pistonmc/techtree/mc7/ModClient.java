package pistonmc.techtree.mc7;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;
import pistonmc.techtree.ModMain.IModInstance;
import pistonmc.techtree.adapter.INetworkClient;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.TechTree;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgRegistry;
import pistonmc.techtree.item.GuideBook;
import pistonmc.techtree.mc7.data.ConfigHost;
import pistonmc.techtree.mc7.event.ClientObtainItemHandler;

@SideOnly(Side.CLIENT)
public class ModClient implements IModInstance {
    private TechTree tree;
    private ProgressClient progress;
    private Network network;
    private ModServer integratedServer;

    public ModClient() {
    }

    public ProgressClient getProgress() {
        return this.progress;
    }

    public INetworkClient<Msg> getNetwork() {
        return this.network;
    }

    @Override
    public MsgRegistry getMsgRegistry() {
        return this.network.registry;
    }

    @Override
    public ModServer getServer() {
        return this.integratedServer;
    }

    @Override
    public TechTree getTechTree() {
        return this.tree;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        this.network = new Network();
        this.tree = new TechTree(new LocaleLoader(), new ConfigHost());
        this.progress = new ProgressClient(this.tree, this.network);
        this.integratedServer = new ModServer();
        this.integratedServer.setNetwork(this.network);
        this.integratedServer.initTechTreeProgress(tree);
        this.integratedServer.initIntegratedServer();
        FMLCommonHandler.instance().bus().register(new ClientObtainItemHandler(this.progress));

        this.integratedServer.registerItemGuideBook(new GuideBook(false, this.tree).setProgressClient(this.progress));
        this.integratedServer.registerItemGuideBook(new GuideBook(true, this.tree).setProgressClient(this.progress));

        this.integratedServer.initGui(this.progress);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void complete(FMLLoadCompleteEvent event) {
    }
}
