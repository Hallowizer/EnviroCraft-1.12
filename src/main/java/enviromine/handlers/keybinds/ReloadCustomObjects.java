package enviromine.handlers.keybinds;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import enviromine.core.EM_ConfigHandler;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;

import org.lwjgl.input.Keyboard;

public class ReloadCustomObjects
{
	public static void doReloadConfig()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if((!(Minecraft.getMinecraft().isSingleplayer()) || !EnviroMine.proxy.isClient()) && Minecraft.getMinecraft().player != null)
		{
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				mc.player.sendMessage(new TextComponentString("Single player only function."));
			}
			return;
		}
		// prevents key press firing while gui screen or chat open, if that's what you want
		// if you want your key to be able to close the gui screen, handle it outside this if statement
		if(mc.currentScreen == null)
		{
			
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
			{
				
				mc.player.sendMessage(new TextComponentString("Reloading Configs..."));
			
				if(EM_ConfigHandler.ReloadConfig())
				{
					mc.player.sendMessage(new TextComponentString("Loaded objects and " + EM_Settings.stabilityTypes.size() + " stability types"));
				}else
				{
					mc.player.sendMessage(new TextComponentString("Failed to Load Custom Objects Files."));
				}
			}
			else
			{
				
				mc.player.sendMessage(new TextComponentString("Must hold left shift to reload Custom Objects"));
			}
		}
	}
}
