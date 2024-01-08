package pistonmc.techtree.mc7.event;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOps;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.event.world.WorldEvent;
import pistonmc.techtree.mc7.ModServer;

public class Command implements ICommand {

    private ModServer server;
    private List<String> aliases = Arrays.asList("techtree", "ttree");

    public Command(ModServer server) {
        this.server = server;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ICommand) {
            return this.getCommandName().compareTo(((ICommand) o).getCommandName());
        }
        return -1;
    }

    @Override
    public String getCommandName() {
        return "techtree";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/t[ech]tree reload|hand";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            boolean success = server.getTechTree().reload();
            if (success) {
                sender.addChatMessage(new ChatComponentTranslation("techtree.load_success"));
            } else {
                sender.addChatMessage(new ChatComponentTranslation("techtree.load_error"));
            }
            return;
        }
        if (args.length == 1 && args[0].equals("hand")) {
            if (!(sender instanceof EntityPlayer)) {
                return;
            }
            EntityPlayer player = (EntityPlayer) sender;
            ItemStack s = player.getCurrentEquippedItem();
            if (s == null) {
                return;
            }
            UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(s.getItem());
            if (id == null) {
                return;
            }
            sender.addChatMessage(new ChatComponentText(id.modId+":"+id.name+":"+s.getItemDamage()));
            return;
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        MinecraftServer server = MinecraftServer.getServer();
        if (!server.isDedicatedServer()) {
            return true;
        }
        UserListOps ops = server.getConfigurationManager().func_152603_m();
        if (ops == null) {
            return true;
        }
        return ops.func_152690_d() || ops.func_152700_a(sender.getCommandSenderName()) != null;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            if (args[0].startsWith("r")) {
                return Arrays.asList("reload");
            }
            if (args[0].startsWith("h")) {
                return Arrays.asList("hand");
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i) {
        return false;
    }

    @SubscribeEvent
    public void register(WorldEvent.Load event) {
        if (event.world.isRemote) {
            return;
        }
        CommandHandler handler = (CommandHandler) MinecraftServer.getServer().getCommandManager();
		handler.registerCommand(this);
    }
}
