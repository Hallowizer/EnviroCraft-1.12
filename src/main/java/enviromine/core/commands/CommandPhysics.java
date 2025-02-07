package enviromine.core.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.translation.I18n;
import enviromine.core.EM_Settings;

//TODO: I18n can cause server side translation, change uses of this to client side variants before release

public class CommandPhysics extends CommandBase
{

	private String on = I18n.translateToLocal("options.enviromine.on");
	private String off = I18n.translateToLocal("options.enviromine.off");
	private String toggle = I18n.translateToLocal("commands.enviromine.envirophysic.toggle");
	private String status = I18n.translateToLocal("commands.enviromine.envirophysic.status");

	@Override
	public String getName() 
	{
		return "envirophysic";
	}
	
	@Override
	public int getRequiredPermissionLevel()
	{
		return 2;
	}
	
	@Override
	public String getUsage(ICommandSender icommandsender) 
	{
		return "/envirophysic <"+on+", "+off+", "+toggle+", "+status+">";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] astring) 
	{
		
		if(astring.length != 1)
		{
			this.ShowUsage(sender);
			return;
		}
		try
		{
			
			if(astring[0].equalsIgnoreCase(toggle))
			{
				this.togglePhy(sender);
			}
			else if(astring[0].equalsIgnoreCase(on))
			{
				doPhy(true , sender);
			}
			else if(astring[0].equalsIgnoreCase(off))
			{
				doPhy(false , sender);
			}
			else if(astring[0].equalsIgnoreCase(status))
			{
				String Status = "";
				if(EM_Settings.enablePhysics)
				{
					Status = on;
				}
				else
				{
					Status = off;
				}
				sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirophysic.statusText", "Enviromine", Status));
			}
			else
			{
				this.ShowUsage(sender);
				return;
			}
			
		} catch(Exception e)
		{
			this.ShowUsage(sender);
			return;
		}
	}
	
	
	public void ShowUsage(ICommandSender sender)
	{
		sender.sendMessage(new TextComponentString(getUsage(sender)));
	}
	
	public void togglePhy( ICommandSender sender)
	{
		
		EM_Settings.enablePhysics = !EM_Settings.enablePhysics;
		
		if(EM_Settings.enablePhysics)
		{
			sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirophysic.physics", "Enviromine", "On"));
		}
		else
		{
			sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirophysic.physics", "Enviromine", "Off"));
		}
	}
	
	public void doPhy(boolean what, ICommandSender sender)
	{
		EM_Settings.enablePhysics = what;
		
		if(what)
		{
			sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirophysic.physics","Enviromine", "On"));
		}
		else
		{
			sender.sendMessage(new TextComponentTranslation("commands.enviromine.envirophysic.physics","Enviromine", "Off"));
		}
	}
}
