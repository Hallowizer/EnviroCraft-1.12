package enviromine.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
//import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileEntityBurningCoal extends TileEntity
{
	public int fuel = 1000;
	
	public TileEntityBurningCoal()
	{
		this.fuel = 1000;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tags)
    {
		super.readFromNBT(tags);
		
		tags.setInteger("Fuel", fuel);
    }
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tags)
    {
		NBTTagCompound returnedNBT;
		returnedNBT = super.writeToNBT(tags);
		
		if(tags.hasKey("Fuel"))
		{
			fuel = tags.getInteger("Fuel");
		}
		return returnedNBT;
    }

    /**
     * Overridden in a sign to provide the text.
     */
	@Override
    public Packet getDescriptionPacket()
    {
    	NBTTagCompound tags = new NBTTagCompound();
    	this.writeToNBT(tags);
        return new SPacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tags);
    }
	
	@Override
	public void onDataPacket(NetworkManager netManager, SPacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.func_148857_g());
	}
}
