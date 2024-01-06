package pistonmc.techtree.adapter;

public interface IPlayerServerSide {
    /**
     * Get the name of the player who sent the message
     */
    public String getClientPlayerName();

    public void markDirty();
}
