package pistonmc.techtree.adapter;

public interface INetworkServer<TMsg> {
    public void sendToAll(TMsg msg);
    public void sendToPlayer(TMsg msg, IPlayerServerSide player);
    public void sendToDimension(TMsg msg, int dimensionId);
}
