package pistonmc.techtree.mc7;

import java.util.Arrays;
import java.util.List;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOps;
import net.minecraftforge.event.world.WorldEvent;

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
        return "/ttree reload";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List getCommandAliases() {
        return this.aliases;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 1 && args[0].equals("reload")) {
            server.getTechTree().reload();
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
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_) {
        return Arrays.asList("reload");
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
