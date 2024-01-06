package pistonmc.techtree.mc7;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.MinecraftForge;
import pistonmc.techtree.ModMain.IModInstance;
import pistonmc.techtree.adapter.INetworkServer;
import pistonmc.techtree.data.ProgressServer;
import pistonmc.techtree.data.TechTree;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgGetProgress;
import pistonmc.techtree.event.MsgPostObtainItem;
import pistonmc.techtree.event.MsgPostReadPage;
import pistonmc.techtree.event.MsgRegistry;
import pistonmc.techtree.event.MsgSyncInit;
import pistonmc.techtree.event.MsgSyncInitPages;
import pistonmc.techtree.event.MsgSyncNewPage;
import pistonmc.techtree.event.MsgSyncObtainItem;

public class ModServer implements IModInstance {
    private TechTree tree;
    private ProgressServer progress;
    private Network network;

    public ModServer() {
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public INetworkServer<Msg> getNetwork() {
        return this.network;
    }

    public ProgressServer getProgress() {
        return this.progress;
    }


    @Override
    public void preInit(FMLPreInitializationEvent event) {
        this.network = new Network();
        this.initTechTreeProgress(new TechTree(new ServerLocaleLoader(), new ConfigHost()));
        this.initIntegratedServer();
    }

    public void initTechTreeProgress(TechTree tree) {
        this.tree = tree;
        tree.reload();
        this.progress = new ProgressServer(tree, this.network);
    }

    public void initIntegratedServer() {
        MsgRegistry registry = this.getMsgRegistry();
        registry.register(i -> MsgGetProgress.id = i, MsgGetProgress::new);
        registry.register(i -> MsgPostObtainItem.id = i, MsgPostObtainItem::new);
        registry.register(i -> MsgPostReadPage.id = i, MsgPostReadPage::new);
        registry.register(i -> MsgSyncInit.id = i, MsgSyncInit::new);
        registry.register(i -> MsgSyncInitPages.id = i, MsgSyncInitPages::new);
        registry.register(i -> MsgSyncNewPage.id = i, MsgSyncNewPage::new);
        registry.register(i -> MsgSyncObtainItem.id = i, MsgSyncObtainItem::new);

        MinecraftForge.EVENT_BUS.register(new PlayerSaveHandler(this));
        MinecraftForge.EVENT_BUS.register(new Command(this));
        FMLCommonHandler.instance().bus().register(new ObtainItemHandler(this));
        FMLCommonHandler.instance().bus().register(new InitProgressHandler(this));
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public MsgRegistry getMsgRegistry() {
        return this.network.registry;
    }

    @Override
    public ModServer getServer() {
        return this;
    }

    @Override
    public TechTree getTechTree() {
        return this.tree;
    }

}
