package enviromine.handlers.keybinds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//TODO I18n translates server side and while this is okay for testing since nothing is noticeable if both server and client are the same language, this will have to be changed with local translation before release

import org.lwjgl.input.Keyboard;

import enviromine.client.gui.menu.EM_Gui_Menu;

@SideOnly(Side.CLIENT)
public class EnviroKeybinds
{
	public static KeyBinding reloadConfig;
	public static KeyBinding addRemove;
	public static KeyBinding menu;
	
	public static void Init()
	{
		reloadConfig = new KeyBinding(I18n.translateToLocal("keybinds.enviromine.reload"), Keyboard.KEY_K, "EnviroMine");
		addRemove = new KeyBinding(I18n.translateToLocal("keybinds.enviromine.addremove"), Keyboard.KEY_J, "EnviroMine");
		menu = new KeyBinding(I18n.translateToLocal("options.enviromine.menu.title"), Keyboard.KEY_M, "EnviroMine");
		
		ClientRegistry.registerKeyBinding(reloadConfig);
		ClientRegistry.registerKeyBinding(addRemove);
		ClientRegistry.registerKeyBinding(menu);
	}
	
	@SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
	{
		if(reloadConfig.isPressed())
		{
			ReloadCustomObjects.doReloadConfig();
		}
		
		if(addRemove.isPressed())
		{
			AddRemoveCustom.doAddRemove();
		}
		
		if(menu.isPressed())
		{
			Minecraft.getMinecraft().displayGuiScreen(new EM_Gui_Menu());
		}
		
	}
}