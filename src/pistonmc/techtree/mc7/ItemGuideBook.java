package pistonmc.techtree.mc7;

import java.util.List;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.data.DataEntry;
import pistonmc.techtree.gui.GuiState;
import pistonmc.techtree.gui.GuiTechTree;
import pistonmc.techtree.item.GuideBook;
import pistonmc.techtree.mc7.data.NBTTagCompoundWrapper;

public class ItemGuideBook extends Item {
    public static final String NAME = "itemTTGuideBook";
    private GuideBook inner;

    @SideOnly(Side.CLIENT)
    private IIcon iconNew;

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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        super.registerIcons(register);
        if (!this.inner.isDebug) {
            this.iconNew = register.registerIcon(ModInfo.ID + ":" + NAME + "New");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage)
    {
        if (!this.inner.isDebug && this.inner.hasNewPages()) {
            return this.iconNew;
        }
        return super.getIconFromDamage(damage);
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
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            // Server side does nothing
            return stack;
        }
        if (this.inner.isDebug) {
            this.inner.reloadClient();
        }
        player.openGui(ModInfo.ID, 0, world, (int) player.posX, (int) player.posY, (int) player.posZ);
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return this.inner.getBookTitle();
    }
}
