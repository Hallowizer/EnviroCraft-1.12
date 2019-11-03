package enviromine.handlers;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.client.gui.SaveController;
import enviromine.client.gui.hud.HUDRegistry;
import enviromine.core.EM_Settings;
import enviromine.gases.GasBuffer;
import enviromine.world.Earthquake;

public class EM_ServerScheduledTickHandler
{
	@SubscribeEvent
	public void tickEnd(TickEvent.WorldTickEvent tick)
	{
		if(tick.side.isServer() && tick.phase == TickEvent.Phase.END)
		{
			GasBuffer.update();
			
			if(EM_Settings.enablePhysics)
			{
				EM_PhysManager.updateSchedule();
			}
			
			Earthquake.updateEarthquakes();
			
			if(EM_Settings.enableQuakes && MathHelper.floor(tick.world.getTotalWorldTime()/24000L) != Earthquake.lastTickDay && tick.world.playerEntities.size() > 0)
			{
				Earthquake.lastTickDay = MathHelper.floor(tick.world.getTotalWorldTime()/24000L);
				Earthquake.TickDay(tick.world);
			}
		}
	}
	
	// Used for to load up SaveContoler for clients side GUI settings
    //private boolean ticked = false;
    private boolean firstload = true;

    @SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void RenderTickEvent(RenderTickEvent event) 
    {
        if ((event.type == Type.RENDER || event.type == Type.CLIENT) && event.phase == Phase.END) 
        {
            Minecraft mc = Minecraft.getMinecraft();
            if (firstload && mc != null) 
            {
                if (!SaveController.loadConfig(SaveController.UISettingsData))
                {
                    HUDRegistry.checkForResize();
                    HUDRegistry.resetAllDefaults();
                    SaveController.saveConfig(SaveController.UISettingsData);
                }
                firstload = false;
            }
        }
    }
}
