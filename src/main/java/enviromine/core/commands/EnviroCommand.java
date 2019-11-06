package enviromine.core.commands;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.FMLCommonHandler;
import enviromine.handlers.EM_StatusManager;
import enviromine.trackers.EnviroDataTracker;

//TODO: I18n can cause server side translation, change uses of this to client side variants before release

public class EnviroCommand extends CommandBase
{
	
	private String add = I18n.translateToLocal("commands.enviromine.envirostat.add");
	private String set = I18n.translateToLocal("commands.enviromine.envirostat.set");
	private String temp = I18n.translateToLocal("commands.enviromine.envirostat.temp");
	private String sanity = I18n.translateToLocal("commands.enviromine.envirostat.sanity");
	private String water = I18n.translateToLocal("commands.enviromine.envirostat.water");
	private String air = I18n.translateToLocal("commands.enviromine.envirostat.air");
	
	@Override
	public String getName()
	{
		return "envirostat";
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/envirostat <playername> <"+add+", "+set+"> <"+temp+", "+sanity+", "+water+", "+air+"> <float>";
	}
	
	@Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] astring)
	{

		if(astring.length != 4)
		{
			this.ShowUsage(sender);
			return;
		}
		
		EntityPlayerMP player;
		try 
		{
			player = getPlayer(sender.getServer(), sender, astring[0]);
			
			String target = player.getName();
			
			EnviroDataTracker tracker = EM_StatusManager.lookupTrackerFromUsername(target);
			
			if(tracker == null)
			{
				this.ShowNoTracker(sender);
				return;
			}

			
			try
			{
				float value = Float.parseFloat(astring[3]);
				
				if(astring[1].equalsIgnoreCase(add))
				{
					if(astring[2].equalsIgnoreCase(temp))
					{
						tracker.bodyTemp += value;
					} else if(astring[2].equalsIgnoreCase(sanity))
					{
						tracker.sanity += value;
					} else if(astring[2].equalsIgnoreCase(water))
					{
						tracker.hydration += value;
					} else if(astring[2].equalsIgnoreCase(air))
					{
						tracker.airQuality += value;
					} else
					{
						this.ShowUsage(sender);
						return;
					}
				} else if(astring[1].equalsIgnoreCase(set))
				{
					if(astring[2].equalsIgnoreCase(temp))
					{
						tracker.bodyTemp = value;
					} else if(astring[2].equalsIgnoreCase(sanity))
					{
						tracker.sanity = value;
					} else if(astring[2].equalsIgnoreCase(water))
					{
						tracker.hydration = value;
					} else if(astring[2].equalsIgnoreCase("air"))
					{
						tracker.airQuality = value;
					} else
					{
						this.ShowUsage(sender);
						return;
					}
				} else
				{
					this.ShowUsage(sender);
					return;
				}
				
				tracker.fixFloatinfPointErrors();
				return;
			} catch(Exception e)
			{
				this.ShowUsage(sender);
				return;
			}
		} catch(PlayerNotFoundException exc)
		{
			System.err.println("PlayerNotFoundException caught in EnviroCommand execute.");
			this.ShowUsage(sender);
		} catch(CommandException exc)
		{
			System.err.println("CommandException caught in EnviroCommand execute.");
			this.ShowUsage(sender);
		}
	}
	
	public void ShowUsage(ICommandSender sender)
	{
		sender.sendMessage(new TextComponentString(getUsage(sender)));
	}
	
	public void ShowNoTracker(ICommandSender sender)
	{
		sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirostat.noTracker"));
	}

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
	@SuppressWarnings("unchecked")
	@Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] strings, @Nullable BlockPos targetPo)
    {
        if(strings.length == 1)
        {
        	return getListOfStringsMatchingLastWord(strings, FMLCommonHandler.instance().getMinecraftServerInstance().getOnlinePlayerNames());
        } else if(strings.length == 2)
        {
        	return getListOfStringsMatchingLastWord(strings, new String[]{add, set});
        } else if(strings.length == 3)
        {
        	return getListOfStringsMatchingLastWord(strings, new String[]{temp, sanity, water, air});
        } else
        {
        	return new ArrayList<String>();
        }
    }
}
