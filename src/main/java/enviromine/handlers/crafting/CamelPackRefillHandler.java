package enviromine.handlers.crafting;

import java.util.HashMap;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import enviromine.core.EM_Settings;
import enviromine.trackers.properties.ItemProperties;

public class CamelPackRefillHandler implements IRecipe
{
	public int totalFill;
	public HashMap<ItemStack,ItemStack> fullItems = new HashMap<ItemStack,ItemStack>();
	public ItemStack emptyItem = null;
	public ItemStack pack;
	
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		if (!inv.getName().equals("container.crafting"))
		{
			return false;
		}
		
		this.totalFill = 0;
		this.pack = null;
		this.fullItems.clear();
		this.emptyItem = null;
		
		for (int i = inv.getSizeInventory() - 1; i >= 0; i--)
		{
			ItemStack item = inv.getStackInSlot(i);
			
			if (item == null)
			{
				continue;
			}
			
			ItemProperties itemProps  = EM_Settings.itemProperties.get(Item.REGISTRY.getNameForObject(item.getItem()) + "," + item.getItemDamage());
			itemProps = itemProps != null? itemProps : EM_Settings.itemProperties.get(Item.REGISTRY.getNameForObject(item.getItem()));
			
			if (item.hasTagCompound() && item.getTagCompound().hasKey("camelPackFill"))
			{
				if (pack != null)
				{
					return false;
				} else
				{
					pack = item.copy();
					//packFillCur = item.getTagCompound().getInteger("camelPackFill");
					//packFillMax = item.getTagCompound().getInteger("camelPackMax");
				}
			} else if(itemProps != null && itemProps.camelFill != 0)
			{
				Item outItem = (Item)Item.REGISTRY.getObject(itemProps.fillReturnItem);
				ItemStack outStack = null;
				if(outItem != null)
				{
					outStack = new ItemStack(outItem, 1, itemProps.fillReturnMeta < 0? item.getItemDamage() : itemProps.fillReturnMeta);
				}
				
				if(totalFill < 0 && itemProps.camelFill > 0)
				{
					return false;
				} else if(totalFill != 0 && itemProps.camelFill < 0)
				{
					return false;
				} else
				{
					totalFill += itemProps.camelFill;
					
					if(itemProps.camelFill > 0)
					{
						fullItems.put(item, outStack);
					} else
					{
						emptyItem = outStack;
					}
				}
			} else
			{
				return false;
			}
		}
		
		return pack != null && totalFill != 0;
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		this.matches(inv, null);
		
		if (totalFill < 0)
		{
			int attemptedFill = pack.getTagCompound().getInteger("camelPackFill") + totalFill;
			
			if (attemptedFill <= pack.getTagCompound().getInteger("camelPackMax") && attemptedFill >= 0)
			{
				return emptyItem;
			} else
			{
				return null;
			}
		} else
		{
			int attemptedFill = pack.getTagCompound().getInteger("camelPackFill") + totalFill;
			
			if (attemptedFill <= pack.getTagCompound().getInteger("camelPackMax") && attemptedFill >= 0)
			{
				pack.getTagCompound().setInteger("camelPackFill", attemptedFill);
				return pack;
			} else
			{
				return null;
			}
		}
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
	
	@SubscribeEvent
	public void onCrafting(PlayerEvent.ItemCraftedEvent event)
	{
		IInventory craftMatrix = event.craftMatrix;
		if (!(craftMatrix instanceof InventoryCrafting))
		{
			return;
		}
		
		if (this.matches((InventoryCrafting)craftMatrix, event.player.world))
		{
			if (!craftMatrix.getName().equals("container.crafting"))
			{
				return;
			} else
			{
				for (int i = craftMatrix.getSizeInventory() - 1; i >= 0; i--)
				{
					ItemStack slot = craftMatrix.getStackInSlot(i);
					
					if (slot == null)
					{
						continue;
					} else if (slot.hasTagCompound() && slot.getTagCompound().hasKey("camelPackFill") && totalFill < 0)
					{
						slot.grow(1);
						slot.getTagCompound().setInteger("camelPackFill", slot.getTagCompound().getInteger("camelPackFill") + totalFill);
					} else if(slot.getItem() == Items.POTIONITEM && totalFill >= 0)
					{
						if(!event.player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE)))
						{
							event.player.dropPlayerItemWithRandomChoice(new ItemStack(Items.GLASS_BOTTLE), false);
						}
					}
				}
			}
		}
	}
}