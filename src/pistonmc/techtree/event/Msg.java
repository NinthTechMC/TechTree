package pistonmc.techtree.event;

import libpiston.adapter.IDeserializer;
import libpiston.adapter.ISerializer;
import pistonmc.techtree.adapter.IPlayerServerSide;

/**
 * A message that can be sent between client and server
 */
public abstract class Msg {
    public abstract byte getId();
    public abstract void writeTo(ISerializer serializer);
    public abstract void readFrom(IDeserializer deserializer);

    public void handleAtClient() {
        throw new UnsupportedOperationException("This message cannot be received on the client side");
    };
    public Msg handleAtServer(IPlayerServerSide sender) {
        throw new UnsupportedOperationException("This message cannot be received on the server side");
    };
}
