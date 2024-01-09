package pistonmc.techtree.mc7;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.ModMain.IModInstance;
import pistonmc.techtree.adapter.INetworkServer;
import pistonmc.techtree.data.ProgressClient;
import pistonmc.techtree.data.ProgressServer;
import pistonmc.techtree.data.TechTree;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgPostNewPages;
import pistonmc.techtree.event.MsgPostObtainItem;
import pistonmc.techtree.event.MsgPostReadPage;
import pistonmc.techtree.event.MsgRegistry;
import pistonmc.techtree.event.MsgSyncInit;
import pistonmc.techtree.event.MsgSyncObtainItem;
import pistonmc.techtree.item.GuideBook;
import pistonmc.techtree.mc7.data.ConfigHost;
import pistonmc.techtree.mc7.event.Command;
import pistonmc.techtree.mc7.event.InitProgressHandler;
import pistonmc.techtree.mc7.event.ObtainItemHandler;
import pistonmc.techtree.mc7.event.PlayerSaveHandler;
import pistonmc.techtree.mc7.event.TutorialHandler;
import pistonmc.techtree.mc7.gui.GuiHandler;

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



    @Override
    public void preInit(FMLPreInitializationEvent event) {
        this.network = new Network();
        this.initTechTreeProgress(new TechTree(new LocaleLoaderServer(), new ConfigHost()));

        ItemGuideBook guideBookItem = this.registerItemGuideBook(new GuideBook(false, this.tree));
        this.registerItemGuideBook(new GuideBook(true, this.tree));
        this.initIntegratedServer(guideBookItem);

        this.initGui(null, null);
    }

    public void initTechTreeProgress(TechTree tree) {
        this.tree = tree;
        tree.reload();
        this.progress = new ProgressServer(tree, this.network);
    }

    public void initIntegratedServer(ItemGuideBook guideBookItem) {
        MsgRegistry registry = this.getMsgRegistry();
        registry.register(i -> MsgPostNewPages.id = i, MsgPostNewPages::new);
        registry.register(i -> MsgPostObtainItem.id = i, MsgPostObtainItem::new);
        registry.register(i -> MsgPostReadPage.id = i, MsgPostReadPage::new);
        registry.register(i -> MsgSyncInit.id = i, MsgSyncInit::new);
        registry.register(i -> MsgSyncObtainItem.id = i, MsgSyncObtainItem::new);

        MinecraftForge.EVENT_BUS.register(new PlayerSaveHandler(this));
        MinecraftForge.EVENT_BUS.register(new Command(this));
        {
            ObtainItemHandler handler = new ObtainItemHandler(this);
            MinecraftForge.EVENT_BUS.register(handler);
            FMLCommonHandler.instance().bus().register(handler);
        }
        {
            TutorialHandler handler = new TutorialHandler(guideBookItem);
            MinecraftForge.EVENT_BUS.register(handler);
            FMLCommonHandler.instance().bus().register(handler);
        }
        FMLCommonHandler.instance().bus().register(new InitProgressHandler(this));
    }

    public ItemGuideBook registerItemGuideBook(GuideBook book) {
        ItemGuideBook item = new ItemGuideBook(book);
        GameRegistry.registerItem(item, item.getRegistryName());
        return item;
    }

    public void initGui(ProgressClient progress, Item debugItem) {
        NetworkRegistry.INSTANCE.registerGuiHandler(ModInfo.ID, new GuiHandler(progress, debugItem));
    }

    @Override
    public void init(FMLInitializationEvent event) {
    }



    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Override
    public void complete(FMLLoadCompleteEvent event) {
    }

}
