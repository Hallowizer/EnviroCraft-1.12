package enviromine.handlers.keybinds;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.client.gui.menu.config.EM_ConfigMenu;
import enviromine.client.gui.menu.config.GuiAddCustom;
import enviromine.core.EM_ConfigHandler;
import enviromine.core.EnviroMine;
import enviromine.trackers.properties.BlockProperties;
import enviromine.trackers.properties.EntityProperties;
import enviromine.utils.EnviroUtils;

@SideOnly(Side.CLIENT)
public class AddRemoveCustom
{	
	public boolean keydown = true;
	
	

	public static void doAddRemove()
	{
		Minecraft mc = Minecraft.getMinecraft();
		
		if(mc.currentScreen != null)
		{
			return;
		}
		
		if((!(Minecraft.getMinecraft().isSingleplayer()) || !EnviroMine.proxy.isClient()) && Minecraft.getMinecraft().player != null)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				mc.player.addChatMessage(new ChatComponentText("Single player only function."));
			}
			return;
		}
		// prevents key press firing while gui screen or chat open, if that's what you want
		// if you want your key to be able to close the gui screen, handle it outside this if statement
		if(mc.currentScreen == null)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				try
				{
					String returnValue = "";
					if(mc.player.getHeldItem() != null)
					{
						Item item = mc.player.getHeldItem().getItem();
						int itemMeta = mc.player.getHeldItem().getItemDamage();
						String idName = Item.REGISTRY.getNameForObject(item);
						String name = mc.player.getHeldItem().getDisplayName();
						
						if(item instanceof ItemArmor)
						{
							Minecraft.getMinecraft().displayGuiScreen(new GuiAddCustom(item));

						} else if(item instanceof Item)
						{
							if(Block.getBlockFromItem(item) != Blocks.AIR)
							{
								Minecraft.getMinecraft().displayGuiScreen(new GuiAddCustom(item));
							}
							else 
							{
								returnValue = EM_ConfigHandler.SaveMyCustom(item);
								mc.player.addChatMessage(new ChatComponentText(name + " " + returnValue));								
							}
						}
						
						return;
					}
					
					MovingObjectType type = Minecraft.getMinecraft().objectMouseOver.typeOfHit;
					if(type.name() == "ENTITY")
					{
						Entity lookingAt = Minecraft.getMinecraft().objectMouseOver.entityHit;
						int id = 0;
						
						if(EntityList.getEntityID(lookingAt) > 0)
						{
							id = EntityList.getEntityID(lookingAt);
						} else if(EntityRegistry.instance().lookupModSpawn(lookingAt.getClass(), false) != null)
						{
							id = EntityRegistry.instance().lookupModSpawn(lookingAt.getClass(), false).getModEntityId() + 128;
						} else
						{
							mc.player.addChatMessage(new ChatComponentText("Failed to add config entry. " + lookingAt.getCommandSenderName() + " has no ID!"));
							EnviroMine.logger.log(Level.WARN, "Failed to add config entry. " + lookingAt.getCommandSenderName() + " has no ID!");
						}
						
						if(EM_ConfigMenu.ElementExist(lookingAt, EntityProperties.base))
						{
							Configuration config;
							List<IConfigElement> configElements;
							
							config = EM_ConfigHandler.getConfigFromObj(lookingAt);
							configElements = EM_ConfigMenu.GetElement(config,lookingAt, EntityProperties.base);
							mc.displayGuiScreen(new EM_ConfigMenu(configElements, config));
						}
						else
						{
							returnValue = EM_ConfigHandler.SaveMyCustom(lookingAt);
							mc.player.addChatMessage(new ChatComponentText(lookingAt.getCommandSenderName() + " (" + id + ") " + returnValue));
						}
					} else if(type.name() == "BLOCK")
					{
						
						int blockX = Minecraft.getMinecraft().objectMouseOver.blockX;
						int blockY = Minecraft.getMinecraft().objectMouseOver.blockY;
						int blockZ = Minecraft.getMinecraft().objectMouseOver.blockZ;
						
						Block block = Minecraft.getMinecraft().player.world.getBlock(blockX, blockY, blockZ);
						int blockMeta = Minecraft.getMinecraft().player.world.getBlockMetadata(blockX, blockY, blockZ);
						String blockULName = Block.REGISTRY.getNameForObject(block);
						String blockName = block.getLocalizedName();
					
						blockName = EnviroUtils.replaceULN(blockName);
											
						returnValue = EM_ConfigHandler.SaveMyCustom(block);
						mc.player.addChatMessage(new ChatComponentText(blockName + "(" + Block.REGISTRY.getNameForObject(block) + ":" + blockMeta + ") " + returnValue));
					}
				}
				catch(NullPointerException e)
				{
					EnviroMine.logger.log(Level.WARN, "A NullPointerException occured while adding config entry!", e);
				}
			}
			else
			{
				
				mc.player.addChatMessage(new ChatComponentText("Must hold left shift to add objects"));
			}
		}
	}
}
