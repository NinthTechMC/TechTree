package pistonmc.techtree.adapter;

public interface INetworkClient<TMsg> {
    public void sendToServer(TMsg msg);
}
