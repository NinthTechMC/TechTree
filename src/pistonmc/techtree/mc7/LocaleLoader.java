package pistonmc.techtree.mc7;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import pistonmc.techtree.adapter.ILocaleLoader;

@SideOnly(Side.CLIENT)
public class LocaleLoader implements ILocaleLoader {
	@Override
	public String getCurrentLocale() {
		return Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
	}
}
