package enviromine;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.client.gui.EM_GuiFakeDeath;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.handlers.EM_StatusManager;
import enviromine.handlers.EnviroAchievements;
import enviromine.trackers.EnviroDataTracker;
import enviromine.utils.RenderAssist;

//TODO remove potion ids from the config (EM_Settings.*)

public class EnviroPotion extends Potion
{
	public static EnviroPotion hypothermia;
	public static EnviroPotion heatstroke;
	public static EnviroPotion frostbite;
	public static EnviroPotion dehydration;
	public static EnviroPotion insanity;
	
	public static ResourceLocation textureResource = new ResourceLocation("enviromine", "textures/gui/status_Gui.png");
	
	public EnviroPotion(boolean par2, int par3)
	{
		super(par2, par3);
	}
	
	public static void RegisterPotions()
	{
		EnviroPotion.frostbite = ((EnviroPotion)new EnviroPotion(true, 8171462).setPotionName("potion.enviromine.frostbite")).setIconIndex(0, 0);
		EnviroPotion.dehydration = ((EnviroPotion)new EnviroPotion(true, 3035801).setPotionName("potion.enviromine.dehydration")).setIconIndex(1, 0);
		EnviroPotion.insanity = ((EnviroPotion)new EnviroPotion(true, 5578058).setPotionName("potion.enviromine.insanity")).setIconIndex(2, 0);
		EnviroPotion.heatstroke = ((EnviroPotion)new EnviroPotion(true, RenderAssist.getColorFromRGBA(255, 0, 0, 255)).setPotionName("potion.enviromine.heatstroke")).setIconIndex(3, 0);
		EnviroPotion.hypothermia = ((EnviroPotion)new EnviroPotion(true, 8171462).setPotionName("potion.enviromine.hypothermia")).setIconIndex(4, 0);
	}
	
	public static void checkAndApplyEffects(EntityLivingBase entityLiving)
	{
		if(entityLiving.world.isRemote)
		{
			return;
		}
		
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(entityLiving);
		
		if(entityLiving.isPotionActive(heatstroke))
		{
			if(entityLiving.getActivePotionEffect(heatstroke).getDuration() == 0)
			{
				entityLiving.removePotionEffect(heatstroke);
			}
			
			PotionEffect effect = entityLiving.getActivePotionEffect(heatstroke);
			
			if(effect.getAmplifier() >= 2 && entityLiving.getRNG().nextInt(2) == 0)
			{
				entityLiving.attackEntityFrom(EnviroDamageSource.heatstroke, 4.0F);
			}
			
			if(effect.getAmplifier() >= 1)
			{
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200, 0));
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 200, 0));
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200, 0));
				
				if(entityLiving.getRNG().nextInt(10) == 0)
				{
					if(EM_Settings.noNausea)
					{
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200, 0));
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 200, 0));
					} else
					{
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
					}
				}
			}
		}
		
		if(entityLiving.isPotionActive(hypothermia))
		{
			PotionEffect effect = entityLiving.getActivePotionEffect(hypothermia);
			
			if(effect.getDuration() == 0)
			{
				entityLiving.removePotionEffect(hypothermia);
			}
			
			if(effect.getAmplifier() >= 2 && entityLiving.getRNG().nextInt(2) == 0)
			{
				entityLiving.attackEntityFrom(EnviroDamageSource.organfailure, 4.0F);
			}
			
			if(effect.getAmplifier() >= 1)
			{
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200, 0));
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 200, 0));
			}
		}
		if(entityLiving.isPotionActive(frostbite))
		{
			if(entityLiving.getActivePotionEffect(frostbite).getDuration() == 0)
			{
				entityLiving.removePotionEffect(frostbite);
			}
			
			if(entityLiving.getRNG().nextInt(2) == 0 && entityLiving.getActivePotionEffect(frostbite).getAmplifier() >= 2)
			{
				entityLiving.attackEntityFrom(EnviroDamageSource.frostbite, 4.0F);
			}
			
			if(entityLiving.getHeldItem() != null)
			{
				if(entityLiving.getActivePotionEffect(EnviroPotion.frostbite) != null)
				{
					if(entityLiving.getRNG().nextInt(20) == 0)
					{
						EntityItem item = entityLiving.entityDropItem(entityLiving.getHeldItem(), 0.0F);
						item.setPickupDelay(40);
						entityLiving.setCurrentItemOrArmor(0, null);
					
						//Sound should play to all that can hear
						entityLiving.world.playSound(entityLiving, entityLiving.getPosition(), new SoundEvent(new ResourceLocation("enviromine:shiver")), SoundCategory.AMBIENT, 1f, 1f);
						
						if(entityLiving instanceof EntityPlayer)
						{
							((EntityPlayer)entityLiving).addStat(EnviroAchievements.iNeededThat, 1);
						}
					}
				}
			}
		}
		if(entityLiving.isPotionActive(dehydration))
		{
			if(entityLiving.getActivePotionEffect(dehydration).getDuration() == 0)
			{
				entityLiving.removePotionEffect(dehydration);
			}
			
			if(tracker != null)
			{
				tracker.dehydrate(1F + (entityLiving.getActivePotionEffect(dehydration).getAmplifier() * 1F));
			}
		}
		if(entityLiving.isPotionActive(insanity))
		{
			PotionEffect effect = entityLiving.getActivePotionEffect(insanity);
			if(effect.getDuration() == 0)
			{
				entityLiving.removePotionEffect(insanity);
			}
			
			int chance = 50 / (effect.getAmplifier() + 1);
			
			chance = chance > 0? chance : 1;
			
			if(entityLiving.getRNG().nextInt(chance) == 0)
			{
				if(effect.getAmplifier() >= 1)
				{
					if(EM_Settings.noNausea)
					{
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200));
					} else
					{
						entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200));
					}
				}
			}
			
			if(effect.getAmplifier() >= 2 && entityLiving.getRNG().nextInt(1000) == 0 && EnviroMine.proxy.isClient())
			{
				displayFakeDeath();
			}
			
			SoundEvent soundEve = null;
			SoundCategory soundCat = null;
			if(entityLiving.getRNG().nextInt(chance) == 0 && entityLiving instanceof EntityPlayer)
			{
				switch(entityLiving.getRNG().nextInt(16))
				{
					case 0:
					{
						soundEve = new SoundEvent(new ResourceLocation("ambient.cave.cave"));
						soundCat = SoundCategory.AMBIENT;
						break;
					}
					case 1:
					{
						soundEve = new SoundEvent(new ResourceLocation("random.explode"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 2:
					{
						soundEve = new SoundEvent(new ResourceLocation("creeper.primed"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 3:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.zombie.say"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 4:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.endermen.idle"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 5:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.skeleton.say"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 6:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.wither.idle"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 7:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.spider.say"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 8:
					{
						soundEve = new SoundEvent(new ResourceLocation("ambient.weather.thunder"));
						soundCat = SoundCategory.WEATHER;
						break;
					}
					case 9:
					{
						soundEve = new SoundEvent(new ResourceLocation("liquid.lava"));
						soundCat = SoundCategory.BLOCKS;
						break;
					}
					case 10:
					{
						soundEve = new SoundEvent(new ResourceLocation("liquid.water"));
						soundCat = SoundCategory.BLOCKS;
						break;
					}
					case 11:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.ghast.moan"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 12:
					{
						soundEve = new SoundEvent(new ResourceLocation("random.bowhit"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 13:
					{
						soundEve = new SoundEvent(new ResourceLocation("game.player.hurt"));
						soundCat = SoundCategory.PLAYERS;
						break;
					}
					case 14:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.enderdragon.growl"));
						soundCat = SoundCategory.HOSTILE;
						break;
					}
					case 15:
					{
						soundEve = new SoundEvent(new ResourceLocation("mob.endermen.portal"));
						soundCat = SoundCategory.AMBIENT;
						break;
					}
				}
				
				EntityPlayer player = ((EntityPlayer)entityLiving);
				
				float rndX = (player.getRNG().nextInt(6) - 3) * player.getRNG().nextFloat();
				float rndY = (player.getRNG().nextInt(6) - 3) * player.getRNG().nextFloat();
				float rndZ = (player.getRNG().nextInt(6) - 3) * player.getRNG().nextFloat();
				
				if(soundEve != null)
				{
					SPacketSoundEffect packet = new SPacketSoundEffect(soundEve, soundCat, entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ, 1.0F, player.getRNG().nextBoolean()? 0.2F : (player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.2F + 1.0F);
					
					//sound should only play to the player with insanity
					if(!EnviroMine.proxy.isClient() && player instanceof EntityPlayerMP)
					{
						((EntityPlayerMP)player).connection.sendPacket(packet);
					} else if(EnviroMine.proxy.isClient() && !player.world.isRemote)
					{
						player.world.playSound(new BlockPos(entityLiving.posX + rndX, entityLiving.posY + rndY, entityLiving.posZ + rndZ), soundEve, soundCat, 1.0F, (player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.2F + 1.0F);
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	private static void displayFakeDeath()
	{
		if(Minecraft.getMinecraft().currentScreen == null)
		{
			Minecraft.getMinecraft().displayGuiScreen(new EM_GuiFakeDeath());
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * Returns true if the potion has a associated status icon to display in then inventory when active.
	 */
	public boolean hasStatusIcon()
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(textureResource);
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	/**
	 * This method returns true if the potion effect is bad - negative - for the entity.
	 */
	public boolean isBadEffect()
	{
		return true;
	}

    /**
     * Sets the index for the icon displayed in the player's inventory when the status is active.
     */
	@Override
    public EnviroPotion setIconIndex(int p_76399_1_, int p_76399_2_)
    {
        return (EnviroPotion)super.setIconIndex(p_76399_1_, p_76399_2_);
    }
}
