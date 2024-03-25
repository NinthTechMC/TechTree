package pistonmc.techtree.mc7;

import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import libpiston.impl.NBTTagCompoundWrapper;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;
import pistonmc.techtree.item.GuideBook;

public class ItemGuideBook extends Item {
    public static final String NAME = "itemTTGuideBook";
    public static final String NEW_PAGE_TAG = "TTNewPage";
    public static final String TUTORIAL_TAG = "TTTutorial";
    public static final String COMPLETE_TAG = "TTComplete";
    @SideOnly(Side.CLIENT)
    private IIcon iconOverlayNew;
    @SideOnly(Side.CLIENT)
    private IIcon iconOverlay;
    @SideOnly(Side.CLIENT)
    private IIcon iconUsed;
    @SideOnly(Side.CLIENT)
    private IIcon iconCompleted;
    private GuideBook inner;


    public ItemGuideBook(GuideBook inner) {
        this.inner = inner;
        this.setCreativeTab(CreativeTabs.tabTools);
        this.setUnlocalizedName(NAME);
        this.setTextureName(ModInfo.ID + ":" + this.getRegistryName());
        this.setMaxStackSize(1);
    }

    public String getRegistryName() {
        return this.inner.isDebug ? NAME + "Debug" : NAME;
    }

    public boolean hasBookTag(ItemStack stack, String name) {
        if (stack == null) {
            return false;
        }
        Item item = stack.getItem();
        if (!(item instanceof ItemGuideBook)) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return false;
        }
        return tag.getBoolean(name);
    }

    public void setBookTag(ItemStack stack, String name, boolean value) {
        if (stack == null) {
            return;
        }
        Item item = stack.getItem();
        if (!(item instanceof ItemGuideBook)) {
            return;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setBoolean(name, value);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity player, int slot, boolean isCurrent) {
        boolean completed = this.hasBookTag(stack, COMPLETE_TAG);
        boolean actuallyCompleted = this.inner.isCompleted();
        if (completed != actuallyCompleted) {
            this.setBookTag(stack, COMPLETE_TAG, actuallyCompleted);
        }

        boolean tutorial = this.hasBookTag(stack, TUTORIAL_TAG);
        boolean actuallyTutorial = this.inner.hasUnfinishedTutorial();
        if (tutorial != actuallyTutorial) {
            this.setBookTag(stack, TUTORIAL_TAG, actuallyTutorial);
        }

        boolean newPages = this.hasBookTag(stack, NEW_PAGE_TAG);
        boolean actuallyNewPages = this.inner.hasNewPages();
        if (newPages != actuallyNewPages) {
            this.setBookTag(stack, NEW_PAGE_TAG, actuallyNewPages);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        iconOverlay = register.registerIcon(ModInfo.ID + ":" + NAME + "Overlay");
        iconOverlayNew = register.registerIcon(ModInfo.ID + ":" + NAME + "OverlayNew");
        iconUsed = register.registerIcon(ModInfo.ID + ":" + NAME + "Used");
        iconCompleted = register.registerIcon(ModInfo.ID + ":" + NAME + "Completed");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }

    @Override
    public IIcon getIconIndex(ItemStack stack) {
        return this.getBookIcon(stack);
    }
    
    // only called when multiple render pass is enabled
    @Override
    public IIcon getIcon(ItemStack stack, int pass) {
        if (pass > 0) {
            if (this.hasBookTag(stack, NEW_PAGE_TAG)) {
                return iconOverlayNew;
            }
            return iconOverlay;
        }
        return this.getBookIcon(stack);
    }

    private IIcon getBookIcon(ItemStack stack)
    {
        if (this.inner.isDebug) {
            return this.itemIcon;
        }
        if (this.hasBookTag(stack, TUTORIAL_TAG)) {
            return this.itemIcon;
        }
        if (this.hasBookTag(stack, COMPLETE_TAG)) {
            return iconCompleted;
        }
        return iconUsed;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean extra) {
        List<String> subtitles = this.inner.getBookSubtitles();
        for (String subtitle : subtitles) {
            tooltip.add(subtitle);
        }
        if (stack.hasTagCompound()) {
            GuiState state = GuiTechTree.readState(new NBTTagCompoundWrapper(stack.getTagCompound()), false);
            if (!state.isOnCategoryList()) {
                String page = state.getCurrentPage();
                String pageName = this.inner.getPageName(page);
                if (pageName != null) {
                    tooltip.add(StatCollector.translateToLocalFormatted("techtree.guidebook.bookmark", pageName));
                }
            }
        }
        if (this.inner.hasNewPages()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("techtree.guidebook.new_pages"));
        }
        if (this.inner.isDebug) {
            tooltip.add(EnumChatFormatting.LIGHT_PURPLE + StatCollector.translateToLocal("techtree.guidebook.debug_mode"));
        }
        if (this.inner.hasUnfinishedTutorial()) {
            tooltip.add(EnumChatFormatting.YELLOW + StatCollector.translateToLocal("techtree.guidebook.tutorial"));
        }
        if (this.inner.isCompleted()) {
            tooltip.add(EnumChatFormatting.GREEN + StatCollector.translateToLocal("techtree.guidebook.complete"));
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Server side does nothing
            return stack;
        }
        if (this.inner.isDebug) {
            boolean success = this.inner.reloadClient();
            if (success) {
                player.addChatMessage(new ChatComponentTranslation("techtree.load_success"));
            } else {
                player.addChatMessage(new ChatComponentTranslation("techtree.load_error"));
            }
        }
        player.openGui(ModInfo.ID, 0, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return this.inner.getBookTitle();
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        if (this.inner.isDebug) {
            super.getSubItems(item, tab, list);
            return;
        }
        list.add(new ItemStack(item));
        ItemStack tutorial = new ItemStack(item);
        this.setBookTag(tutorial, TUTORIAL_TAG, true);
        list.add(tutorial);
        ItemStack complete = new ItemStack(item);
        this.setBookTag(complete, COMPLETE_TAG, true);
        list.add(complete);
    }
}
