package pistonmc.techtree.mc7;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import pistonmc.techtree.ModInfo;
import pistonmc.techtree.ModMain;
import pistonmc.techtree.adapter.INetworkClient;
import pistonmc.techtree.adapter.INetworkServer;
import pistonmc.techtree.adapter.IPlayerServerSide;
import pistonmc.techtree.event.Msg;
import pistonmc.techtree.event.MsgRegistry;

public class Network implements INetworkClient<Msg>, INetworkServer<Msg> {
    public boolean initialized = false;
    private SimpleNetworkWrapper wrapper;
    public final MsgRegistry registry;

    public Network() {
        this.registry = new MsgRegistry();
        this.wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModInfo.ID);
        this.wrapper.registerMessage(new MessageHandlerServer(), MessageServer.class, 0, Side.SERVER);
        this.wrapper.registerMessage(new MessageHandlerClient(), MessageClient.class, 1, Side.CLIENT);
    }

    public static class MessageServer implements IMessage {
        public Msg msg;
        public MessageServer() {}
        public MessageServer(Msg msg) {
            this.msg = msg;
        }
        @Override
        public void fromBytes(ByteBuf buf) {
            this.msg = ModMain.instance.getMsgRegistry().deserialize(new ByteBufWrapper(buf));
            
        }
        @Override
        public void toBytes(ByteBuf buf) {
            ModMain.instance.getMsgRegistry().serialize(this.msg, new ByteBufWrapper(buf));
        }
    }

    // need this so it's a different message class
    public static class MessageClient extends MessageServer {
        public MessageClient() {}
        public MessageClient(Msg msg) {
            super(msg);
        }
    }

    static class MessageHandlerServer implements IMessageHandler<MessageServer, IMessage> {

        @Override
        public IMessage onMessage(MessageServer message, MessageContext ctx) {
            Msg response = message.msg.handleAtServer(new PlayerServerSide(ctx.getServerHandler().playerEntity));
            if (response == null) {
                return null;
            }
            return new MessageClient(response);
        }
    }

    static class MessageHandlerClient implements IMessageHandler<MessageServer, IMessage> {
        @Override
        public IMessage onMessage(MessageServer message, MessageContext ctx) {
            message.msg.handleAtClient();
            return null;
        }
    }

    @Override
    public void sendToServer(Msg msg) {
        this.wrapper.sendToServer(new MessageServer(msg));
    }

    @Override
    public void sendToAll(Msg msg) {
        this.wrapper.sendToAll(new MessageClient(msg));
    }

    @Override
    public void sendToPlayer(Msg msg, IPlayerServerSide player) {
        PlayerServerSide p = (PlayerServerSide) player;
        if (!(p.entity instanceof EntityPlayerMP)) {
            ModMain.log.error("Cannot send message to non-player entity! Type is: " + p.entity.getClass().getCanonicalName());
            return;
        }
        this.wrapper.sendTo(new MessageClient(msg), (EntityPlayerMP) p.entity);
    }

    @Override
    public void sendToDimension(Msg msg, int dimensionId) {
        this.wrapper.sendToDimension(new MessageClient(msg), dimensionId);
    }

}
