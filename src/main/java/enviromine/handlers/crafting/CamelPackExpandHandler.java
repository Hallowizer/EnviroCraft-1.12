package enviromine.handlers.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import enviromine.handlers.ObjectHandler;

public class CamelPackExpandHandler implements IRecipe
{
	public ItemStack pack1;
	public ItemStack pack2;
	
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		if (!inv.getName().equals("container.crafting"))
		{
			return false;
		}
		
		this.pack1 = null;
		this.pack2 = null;
		
		for (int i = inv.getSizeInventory() - 1; i >= 0; i--)
		{
			ItemStack item = inv.getStackInSlot(i);
			if (item == null)
			{
				continue;
			} else if (item.hasTagCompound() && item.getTagCompound().hasKey("isCamelPack"))
			{
				if (item.getTagCompound().getInteger("camelPackMax") > 100) {
					return false; //Temp thing to disable more than double sized packs
				}
				
				if (pack1 != null) {
					if (pack2 != null) {
						return false;
					} else {
						pack2 = item.copy();
					}
				} else {
					pack1 = item.copy();
				}
			} else
			{
				return false;
			}
		}
		
		return (pack1 != null && pack2 != null);
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		this.matches(inv, null);
		
		ItemStack pack = new ItemStack(ObjectHandler.camelPack);
		
		pack.setTagCompound(new NBTTagCompound());
		pack.getTagCompound().setInteger("camelPackFill", pack1.getTagCompound().getInteger("camelPackFill") + pack2.getTagCompound().getInteger("camelPackFill"));
		pack.getTagCompound().setInteger("camelPackMax", pack1.getTagCompound().getInteger("camelPackMax") + pack2.getTagCompound().getInteger("camelPackMax"));
		pack.getTagCompound().setBoolean("isCamelPack", true);
		
		return pack;
	}
	
	@Override
	public boolean canFit(int a, int b)
	{
		if(a >= 2 && b >= 2)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@Override
	public ItemStack getRecipeOutput()
	{
		return null;
	}
	
	/*
	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event)
	{
		IInventory craftMatrix = event.craftMatrix;
		if (!(craftMatrix instanceof InventoryCrafting) || !craftMatrix.getInventoryName().equals("container.crafting")) {
			return;
		}
		
		this.matches((InventoryCrafting)craftMatrix, event.player.worldObj);
	}
	*/
	
}