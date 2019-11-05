package enviromine.handlers;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox.TileEntityJukebox;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.SoundCategory;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent17;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;

import org.apache.logging.log4j.Level;
import org.lwjgl.opengl.GL11;

import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.EntityPhysicsBlock;
import enviromine.EnviroDamageSource;
import enviromine.EnviroPotion;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.client.gui.menu.config.EM_ConfigMenu;
import enviromine.core.EM_ConfigHandler;
import enviromine.core.EM_Settings;
import enviromine.core.EnviroMine;
import enviromine.gases.GasBuffer;
import enviromine.network.packet.PacketEnviroMine;
import enviromine.trackers.EnviroDataTracker;
import enviromine.trackers.Hallucination;
import enviromine.trackers.properties.BiomeProperties;
import enviromine.trackers.properties.CaveSpawnProperties;
import enviromine.trackers.properties.DimensionProperties;
import enviromine.trackers.properties.EntityProperties;
import enviromine.trackers.properties.ItemProperties;
import enviromine.trackers.properties.RotProperties;
import enviromine.utils.EnviroUtils;
import enviromine.world.EM_WorldData;
import enviromine.world.Earthquake;
import enviromine.world.features.mineshaft.MineshaftBuilder;

public class EM_EventManager
{
	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event)
	{
		boolean chunkPhys = true;
		
		DimensionProperties dProps = EM_Settings.dimensionProperties.get(event.getWorld().provider.getDimension());
		
		if(!event.getWorld().isRemote)
		{
			if(EM_PhysManager.chunkDelay.containsKey(event.getWorld().provider.getDimension() + "" + (MathHelper.floor(event.getEntity().posX) >> 4) + "," + (MathHelper.floor(event.getEntity().posZ) >> 4)))
			{
				chunkPhys = (EM_PhysManager.chunkDelay.get(event.getWorld().provider.getDimension() + "" + (MathHelper.floor(event.getEntity().posX) >> 4) + "," + (MathHelper.floor(event.getEntity().posZ) >> 4)) < event.getWorld().getTotalWorldTime());
			}
		}
		
		if((dProps != null && !dProps.physics) || !EM_Settings.enablePhysics)
		{
			chunkPhys = false;
		}
		
		if(EM_Settings.foodSpoiling)
		{
			if(event.getEntity() instanceof EntityItem)
			{
				EntityItem item = (EntityItem)event.getEntity();
				ItemStack rotStack = RotHandler.doRot(event.getWorld(), item.getItem());
				
				if(item.getItem() != rotStack)
				{
					item.setItem(rotStack);
				}
			} else if(event.getEntity() instanceof EntityPlayer)
			{
				IInventory invo = ((EntityPlayer)event.getEntity()).inventory;
				RotHandler.rotInvo(event.getWorld(), invo);
			} else if(event.getEntity() instanceof IInventory)
			{
				IInventory invo = (IInventory)event.getEntity();
				RotHandler.rotInvo(event.getWorld(), invo);
			}
		}
		
		if(event.getEntity() instanceof EntityLivingBase)
		{
			// Ensure that only one set of trackers are made per Minecraft instance.
			boolean allowTracker = !(event.getWorld().isRemote && EnviroMine.proxy.isClient() && Minecraft.getMinecraft().isIntegratedServerRunning());
			
			if(EnviroDataTracker.isLegalType((EntityLivingBase)event.getEntity()) && (event.getEntity() instanceof EntityPlayer || EM_Settings.trackNonPlayer) && allowTracker)
			{
				EnviroDataTracker tracker = EM_StatusManager.lookupTracker((EntityLivingBase)event.getEntity());
				boolean hasOld = tracker != null && !tracker.isDisabled;
				
				if(!hasOld)
				{
					EnviroDataTracker emTrack = new EnviroDataTracker((EntityLivingBase)event.getEntity());
					EM_StatusManager.addToManager(emTrack);
					emTrack.loadNBTTags();
					if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
					{
						EM_StatusManager.syncMultiplayerTracker(emTrack);
					}
				} else
				{
					tracker.trackedEntity = (EntityLivingBase)event.getEntity();
				}
				
				//TODO this is for updating clients gui
				if (event.getEntity() instanceof EntityPlayerMP && !event.getWorld().isRemote) 
				{
					NBTTagCompound pData = new NBTTagCompound();
					pData.setInteger("id", 4);
					pData.setString("player", event.getEntity().getCommandSenderName());
					pData.setBoolean("enableAirQ", EM_Settings.enableAirQ);
					pData.setBoolean("enableBodyTemp", EM_Settings.enableBodyTemp);
					pData.setBoolean("enableHydrate", EM_Settings.enableHydrate);
					pData.setBoolean("enableSanity", EM_Settings.enableSanity);
					
					EnviroMine.instance.network.sendTo(new PacketEnviroMine(pData), (EntityPlayerMP) event.getEntity());
			
				}
			}
		} else if(event.getEntity() instanceof EntityFallingBlock && !(event.getEntity() instanceof EntityPhysicsBlock) && !event.getWorld().isRemote && event.getWorld().getTotalWorldTime() > EM_PhysManager.worldStartTime + EM_Settings.worldDelay && chunkPhys)
		{
			EntityFallingBlock oldSand = (EntityFallingBlock)event.getEntity();
			
			if(oldSand.func_145805_f() != Blocks.AIR)
			{
				NBTTagCompound oldTags = new NBTTagCompound();
				oldSand.writeToNBT(oldTags);
				
				EntityPhysicsBlock newSand = new EntityPhysicsBlock(oldSand.world, oldSand.prevPosX, oldSand.prevPosY, oldSand.prevPosZ, oldSand.func_145805_f(), oldSand.field_145814_a, true);
				newSand.readFromNBT(oldTags);
				event.getWorld().spawnEntity(newSand);
				event.setCanceled(true);
				event.getEntity().setDead();
				return;
			}
		}
	}
	
	@SubscribeEvent
	public void onLivingSpawn(LivingSpawnEvent.CheckSpawn event)
	{
		if(EM_Settings.enforceWeights)
		{
			if(EnviroMine.caves.totalSpawnWeight > 0 && event.getWorld().provider.getDimension() == EM_Settings.caveDimID && EM_Settings.caveSpawnProperties.containsKey(EntityList.getEntityID(event.getEntity())))
			{
				CaveSpawnProperties props = EM_Settings.caveSpawnProperties.get(EntityList.getEntityID(event.getEntity()));
				
				if(event.getWorld().rand.nextInt(EnviroMine.caves.totalSpawnWeight) > props.weight)
				{
					event.setResult(Result.DENY);
					return;
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerClone(PlayerEvent.Clone event)
	{
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(event.getOriginal());
		
		if(tracker != null && !tracker.isDisabled)
		{
			tracker.trackedEntity = event.getEntityPlayer();
			
			if(event.isWasDeath() && !EM_Settings.keepStatus)
			{
				tracker.resetData();
				EM_StatusManager.saveTracker(tracker);
			} else if(event.isWasDeath())
			{
				tracker.ClampSafeRange();
				EM_StatusManager.saveTracker(tracker);
			}
			
			tracker.loadNBTTags();
		}
		
		if(event.isWasDeath())
		{
			doDeath(event.getEntityPlayer());
			doDeath(event.getOriginal());
		}
	}
	
	@SubscribeEvent
	public void onLivingDeath(LivingDeathEvent event)
	{
		doDeath(event.getEntityLiving());
		
		if(event.getEntityLiving() instanceof EntityMob && event.getSource().getTrueSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
			EnviroDataTracker tracker = EM_StatusManager.lookupTracker(player);

			
			if(player.isPotionActive(EnviroPotion.insanity) && player.getActivePotionEffect(EnviroPotion.insanity).getAmplifier() >= 2)
			{
				int val = player.getEntityData().getInteger("EM_MIND_MAT") + 1;
				player.getEntityData().setInteger("EM_MIND_MAT", val);
				
				if(val >= 5)
				{
					player.addStat(EnviroAchievements.mindOverMatter, 1);
				}
			}

			// If player kill mob give some sanity back
			if(tracker != null && tracker.sanity < 100 && !(event.getEntityLiving() instanceof EntityAnimal))
			{
				tracker.sanity += event.getEntityLiving().world.rand.nextInt(5);
			}
			

		}
	}
	
	public static void doDeath(EntityLivingBase entityLiving)
	{
		if(entityLiving instanceof EntityPlayer)
		{
			if(entityLiving.getEntityData().hasKey("EM_MINE_TIME"))
			{
				entityLiving.getEntityData().removeTag("EM_MINE_TIME");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_WINTER"))
			{
				entityLiving.getEntityData().removeTag("EM_WINTER");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_CAVE_DIST"))
			{
				entityLiving.getEntityData().removeTag("EM_CAVE_DIST");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_SAFETY"))
			{
				entityLiving.getEntityData().removeTag("EM_SAFETY");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_MIND_MAT"))
			{
				entityLiving.getEntityData().removeTag("EM_MIND_MAT");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_THAT"))
			{
				entityLiving.getEntityData().removeTag("EM_THAT");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_BOILED"))
			{
				entityLiving.getEntityData().removeTag("EM_BOILED");
			}
			
			if(entityLiving.getEntityData().hasKey("EM_PITCH"))
			{
				entityLiving.getEntityData().removeTag("EM_PITCH");
			}
		}
	}
	
	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event)
	{
		if(event.getEntityLiving().world.isRemote)
		{
			return;
		}
		
		Entity attacker = event.getSource().getTrueSource();
		
		if((event.getSource() == DamageSource.FALLING_BLOCK || event.getSource() == DamageSource.ANVIL || event.getSource() == EnviroDamageSource.landslide || event.getSource() == EnviroDamageSource.avalanche) && event.getEntityLiving().getEquipmentInSlot(4) != null && event.getEntityLiving().getEquipmentInSlot(4).getItem() == ObjectHandler.hardHat)
		{
			ItemStack hardHat = event.getEntityLiving().getEquipmentInSlot(4);
			int dur = (hardHat.getMaxDamage() + 1) - hardHat.getItemDamage();
			int dam = MathHelper.ceiling_float_int(event.ammount);
			event.setCanceled(true);
			hardHat.damageItem(dam, event.getEntityLiving());
			
			if(dur >= dam)
			{
				event.getEntityLiving().world.playSoundAtEntity(event.getEntityLiving(), "dig.stone", 1.0F, 1.0F);
				return;
			} else
			{
				event.getEntityLiving().attackEntityFrom(event.getSource(), dam - dur);
				return;
			}
		}
		
		if(event.getSource() == DamageSource.FALLING_BLOCK && event.getEntityLiving() instanceof EntityPlayer)
		{
			event.getEntityLiving().getEntityData().setLong("EM_SAFETY", event.getEntityLiving().world.getTotalWorldTime());
		}
		
		if(event.getSource() == EnviroDamageSource.gasfire && event.getEntityLiving() instanceof EntityPlayer)
		{
			event.getEntityLiving().getEntityData().setLong("EM_THAT", event.getEntityLiving().world.getTotalWorldTime());
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().getEntityData().hasKey("EM_MIND_MAT"))
		{
			event.getEntityLiving().getEntityData().removeTag("EM_MIND_MAT");
		}
		
		if(attacker != null)
		{
			EnviroDataTracker tracker = EM_StatusManager.lookupTracker(event.getEntityLiving());
			
			if(event.getEntityLiving() instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)event.getEntityLiving();
				
				if(player.capabilities.disableDamage || player.capabilities.isCreativeMode)
				{
					return;
				}
			}
			
			if(tracker != null)
			{
				EntityProperties livingProps = null;
				
				if(EntityList.getEntityString(attacker) != null)
				{
					if(EntityProperties.base.hasProperty(attacker))
					{
						livingProps = EntityProperties.base.getProperty(attacker);
					}
				}
				
				if(livingProps != null)
				{
					tracker.sanity += livingProps.hitSanity;
					tracker.airQuality += livingProps.hitAir;
					tracker.hydration += livingProps.hitHydration;
					
					if(!livingProps.bodyTemp)
					{
						tracker.bodyTemp += livingProps.hitTemp;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		ItemStack item = event.getEntityPlayer().getCurrentEquippedItem();
		
		if(event.action == Action.RIGHT_CLICK_BLOCK && EM_Settings.foodSpoiling)
		{
			TileEntity tile = event.getEntityPlayer().world.getTileEntity(event.getPos());
			
			if(tile != null & tile instanceof IInventory)
			{
				RotHandler.rotInvo(event.getEntityPlayer().world, (IInventory)tile);
			}
		}
		
		if(event.getResult() != Result.DENY && event.action == Action.RIGHT_CLICK_BLOCK && item != null)
		{
			
			if(item.getItem() instanceof ItemBlock && !event.getEntityPlayer().world.isRemote)
			{
				int adjCoords[] = EnviroUtils.getAdjacentBlockCoordsFromSide(event.x, event.y, event.z, event.face);
				
				if(item.getItem() == Item.getItemFromBlock(Blocks.TORCH) && (EM_Settings.torchesBurn || EM_Settings.torchesGoOut)) // Redirect torch placement to our own
				{
					Vec3 lookVec = event.getEntityPlayer().getLookVec();
					
		            Block block = event.getWorld().getBlock(event.x, event.y, event.z);
					if(block.onBlockActivated(event.getWorld(), event.x, event.y, event.y, event.getEntityPlayer(), event.face, 0F, 0F, 0F))
					{
						event.useItem = Result.DENY;
						event.useBlock = Result.ALLOW;
					}else
					{
//						event.useItem = Result.DENY;
						ItemBlock torchItem = (ItemBlock)Item.getItemFromBlock(ObjectHandler.fireTorch);
						torchItem.onItemUse(item, event.getEntityPlayer(), event.getWorld(), event.x, event.y, event.z, event.face, (float)lookVec.xCoord, (float)lookVec.yCoord, (float)lookVec.zCoord);
						event.setCanceled(true);
					}
					return;

				}
				
				EM_PhysManager.schedulePhysUpdate(event.getEntityPlayer().world, adjCoords[0], adjCoords[1], adjCoords[2], true, "Normal");
			} else if(item.getItem() == Items.GLASS_BOTTLE && !event.getEntityPlayer().world.isRemote)
			{
				if(event.getEntityPlayer().world.getBlock(event.x, event.y, event.z) == Blocks.CAULDRON && event.getEntityPlayer().world.getBlockMetadata(event.x, event.y, event.z) > 0)
				{
					fillBottle(event.entityPlayer.worldObj, event.entityPlayer, event.x, event.y, event.z, item, event);
				}
			} else if(item.getItem() == Items.record_11)
			{
				RecordEasterEgg(event.entityPlayer, event.x, event.y, event.z);
			}
		} else if(event.getResult() != Result.DENY && event.action == Action.RIGHT_CLICK_BLOCK && item == null)
		{
			if(!event.entityPlayer.worldObj.isRemote)
			{
				drinkWater(event.entityPlayer, event);
			}
		} else if(event.getResult() != Result.DENY && event.action == Action.LEFT_CLICK_BLOCK)
		{
			EM_PhysManager.schedulePhysUpdate(event.entityPlayer.worldObj, event.x, event.y, event.z, true, "Normal");
		} else if(event.getResult() != Result.DENY && event.action == Action.RIGHT_CLICK_AIR && item != null)
		{
			if(item.getItem() instanceof ItemGlassBottle && !event.entityPlayer.worldObj.isRemote)
			{
				if(!(event.entityPlayer.worldObj.getBlock(event.x, event.y, event.z) == Blocks.cauldron && event.entityPlayer.worldObj.getBlockMetadata(event.x, event.y, event.z) > 0))
				{
					fillBottle(event.entityPlayer.worldObj, event.entityPlayer, event.x, event.y, event.z, item, event);
				}
			}
		} else if(event.getResult() != Result.DENY && event.action == Action.RIGHT_CLICK_AIR && item == null)
		{
			NBTTagCompound pData = new NBTTagCompound();
			pData.setInteger("id", 1);
			pData.setString("player", event.entityPlayer.getCommandSenderName());
			EnviroMine.instance.network.sendToServer(new PacketEnviroMine(pData));
		}
	}
	
	@SubscribeEvent
	public void onEntityInteract(EntityInteractEvent event)
	{
		if(event.isCanceled() || event.entityPlayer.worldObj.isRemote)
		{
			return;
		}
		
		if(event.target instanceof EntityIronGolem && event.entityPlayer.getEquipmentInSlot(0) != null)
		{
			ItemStack stack = event.entityLiving.getEquipmentInSlot(0);
			
			if(stack.getItem() == Items.name_tag && stack.getDisplayName().toLowerCase().equals("siyliss"))
			{
				event.entityPlayer.addStat(EnviroAchievements.ironArmy, 1);
			}
		}
		
		if(!EM_Settings.foodSpoiling)
		{
			return;
		}
		
		if(event.target != null && event.target instanceof IInventory && EM_Settings.foodSpoiling)
		{
			IInventory chest = (IInventory)event.target;
			
			RotHandler.rotInvo(event.entityPlayer.worldObj, chest);
		}
	}
	
	public void RecordEasterEgg(EntityPlayer player, int x, int y, int z)
	{
		if(player.world.isRemote)
		{
			return;
		}
		
		MovingObjectPosition movingobjectposition = getMovingObjectPositionFromPlayer(player.worldObj, player, true);
		
		if(movingobjectposition == null)
		{
			return;
		} else
		{
			if(movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				int i = movingobjectposition.blockX;
				int j = movingobjectposition.blockY;
				int k = movingobjectposition.blockZ;
				
				if(player.worldObj.getBlock(i, j, k) == Blocks.jukebox)
				{
					TileEntityJukebox recordplayer = (TileEntityJukebox)player.worldObj.getTileEntity(i, j, k);
					
					if (recordplayer != null)
					{
						if(recordplayer.func_145856_a() == null)
						{
							EnviroDataTracker tracker = EM_StatusManager.lookupTracker(player);
							
							if(tracker != null)
							{
								if(tracker.sanity >= 75F)
								{
									tracker.sanity -= 50F;
								}
								
								player.addChatMessage(new ChatComponentText("An eerie shiver travels down your spine"));
								player.addStat(EnviroAchievements.ohGodWhy, 1);
							}
						}
					}
				}
			}
		}
	}
	
	public static void fillBottle(World world, EntityPlayer player, int x, int y, int z, ItemStack item, PlayerInteractEvent event)
	{
		
		MovingObjectPosition movingobjectposition = getMovingObjectPositionFromPlayer(world, player, true);
		
		if(movingobjectposition == null)
		{
			return;
		} else
		{
			if(movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				int i = movingobjectposition.blockX;
				int j = movingobjectposition.blockY;
				int k = movingobjectposition.blockZ;
				
				boolean isValidCauldron = (player.world.getBlock(i, j, k) == Blocks.CAULDRON && player.world.getBlockMetadata(i, j, k) > 0);
				
				if(!world.canMineBlock(player, i, j, k))
				{
					return;
				}
				
				if(!player.canPlayerEdit(i, j, k, movingobjectposition.sideHit, item))
				{
					return;
				}
				
				boolean isWater;
				
				
				if(world.getBlock(i, j, k) == Blocks.WATER || world.getBlock(i, j, k) == Blocks.FLOWING_WATER)
				{
					isWater = true;
					
					// if finite is on.. make sure player cant drink from a infinite flowing water source
					if(world.getBlockMetadata(i, j, k) > .2f && EM_Settings.finiteWater)
					{
						isWater = false;
					}

				} else
				{
					isWater = false;
				}
				
				if(isWater || isValidCauldron)
				{
					Item newItem = Items.POTIONITEM;
					switch(getWaterType(world, i, j, k))
					{
						case 0:
						{
							newItem = Items.POTIONITEM;
							break;
						}
						case 1:
						{
							newItem = ObjectHandler.badWaterBottle;
							break;
						}
						case 2:
						{
							newItem = ObjectHandler.saltWaterBottle;
							break;
						}
						case 3:
						{
							newItem = ObjectHandler.coldWaterBottle;
							break;
						}
					}
					
					if(isValidCauldron && (world.getBlock(i, j - 1, k) == Blocks.FIRE || world.getBlock(i, j - 1, k) == Blocks.FLOWING_LAVA || world.getBlock(i, j - 1, k) == Blocks.LAVA))
					{
						newItem = Items.POTIONITEM;
					}
					
					if(isValidCauldron)
					{
						player.world.setBlockMetadataWithNotify(i, j, k, player.world.getBlockMetadata(i, j, k) - 1, 2);
					} else if(EM_Settings.finiteWater)
					{
						player.world.setBlock(i, j, k, Blocks.FLOWING_WATER, player.world.getBlockMetadata(i, j, k) + 1, 2);
					}
					
					item.shrink(1);
					
					if(item.getCount() <= 0)
					{
						item = new ItemStack(newItem);
						item.setCount(1);
						item.setItemDamage(0);
						player.setCurrentItemOrArmor(0, item);
					} else if(!player.inventory.addItemStackToInventory(new ItemStack(newItem,1,0)))
					{
        					 player.dropPlayerItemWithRandomChoice(new ItemStack(newItem, 1, 0), false);
					}

    				//NEEDED TO RESYNC THE PLAYER CONTAINER
    				player.inventoryContainer.detectAndSendChanges();
					event.setCanceled(true);
				}
			}
			
			return;
		}
	}
	
	public static void drinkWater(EntityPlayer entityPlayer, PlayerInteractEvent event)
	{
		if(entityPlayer.isInsideOfMaterial(Material.WATER))
		{
			return;
		}
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(entityPlayer);
		MovingObjectPosition mop = getMovingObjectPositionFromPlayer(entityPlayer.world, entityPlayer, true);
		
		if(mop != null)
		{
			if(mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
			{
				int i = mop.blockX;
				int j = mop.blockY;
				int k = mop.blockZ;
				
				int[] hitBlock = EnviroUtils.getAdjacentBlockCoordsFromSide(i, j, k, mop.sideHit);
				
				int x = hitBlock[0];
				int y = hitBlock[1];
				int z = hitBlock[2];
				
				if(entityPlayer.world.getBlock(i, j, k).getMaterial() != Material.WATER && entityPlayer.world.getBlock(x, y, z).getMaterial() == Material.WATER)
				{
					i = x;
					j = y;
					k = z;
				}
				
				boolean isWater;
				
				if(entityPlayer.world.getBlock(i, j, k) == Blocks.FLOWING_WATER  || entityPlayer.world.getBlock(i, j, k) == Blocks.WATER)
				{
					isWater = true;
					
					// if finite is on.. make sure player cant drink from a infinite flowing water source
					if(entityPlayer.world.getBlockMetadata(i, j, k) > .2f && EM_Settings.finiteWater)
					{
						isWater = false;
					}
				} else
				{
					isWater = false;
				}
				
				boolean isValidCauldron = (entityPlayer.world.getBlock(i, j, k) == Blocks.CAULDRON && entityPlayer.world.getBlockMetadata(i, j, k) > 0);

				if(isWater || isValidCauldron)
				{
					if(tracker != null && tracker.hydration < 100F)
					{
						int type = 0;
						
						if(isValidCauldron && (entityPlayer.world.getBlock(i, j - 1, k) == Blocks.FIRE || entityPlayer.world.getBlock(i, j - 1, k) == Blocks.FLOWING_LAVA || entityPlayer.world.getBlock(i, j - 1, k) == Blocks.LAVA))
						{
							type = 0;
						} else
						{
							type = getWaterType(entityPlayer.world, i, j, k);
						}
						
						if(type == 0)
						{
							if(tracker.bodyTemp >= 37.05F)
							{
								tracker.bodyTemp -= 0.05;
							}
							tracker.hydrate(10F);
						} else if(type == 1)
						{
							if(entityPlayer.getRNG().nextInt(2) == 0)
							{
								entityPlayer.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 200));
							}
							if(entityPlayer.getRNG().nextInt(4) == 0)
							{
								entityPlayer.addPotionEffect(new PotionEffect(MobEffects.POISON, 200));
							}
							if(tracker.bodyTemp >= 37.05)
							{
								tracker.bodyTemp -= 0.05;
							}
							tracker.hydrate(10F);
						} else if(type == 2)
						{
							if(entityPlayer.getRNG().nextInt(1) == 0)
							{
								if(entityPlayer.getActivePotionEffect(EnviroPotion.dehydration) != null && entityPlayer.getRNG().nextInt(5) == 0)
								{
									int amp = entityPlayer.getActivePotionEffect(EnviroPotion.dehydration).getAmplifier();
									entityPlayer.addPotionEffect(new PotionEffect(EnviroPotion.dehydration, 600, amp + 1));
								} else
								{
									entityPlayer.addPotionEffect(new PotionEffect(EnviroPotion.dehydration, 600));
								}
							}
							if(tracker.bodyTemp >= 37.05)
							{
								tracker.bodyTemp -= 0.05;
							}
							tracker.hydrate(5F);
						} else if(type == 3)
						{
							if(tracker.bodyTemp >= 30.1)
							{
								tracker.bodyTemp -= 0.1;
							}
							tracker.hydrate(10F);
						}
						
						if(isValidCauldron)
						{
							entityPlayer.world.setBlockMetadataWithNotify(i, j, k, entityPlayer.world.getBlockMetadata(i, j, k) - 1, 2);
						} else if(EM_Settings.finiteWater)
						{
							entityPlayer.world.setBlock(i, j, k, Blocks.FLOWING_WATER, entityPlayer.world.getBlockMetadata(i, j, k) + 1, 2);
						}
						
						entityPlayer.world.playSoundAtEntity(entityPlayer, "random.drink", 1.0F, 1.0F);
						
						if(event != null)
						{
							event.setCanceled(true);
						}
					}
				}
			}
		}
	}
	
	public static int getWaterType(World world, int x, int y, int z)
	{
		BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
		DimensionProperties dProps = EM_Settings.dimensionProperties.get(world.provider.dimensionId);
		int seaLvl = dProps != null? dProps.sealevel : 64;
		
		if(biome == null)
		{
			return 0;
		}
		
		BiomeProperties bProps = EM_Settings.biomeProperties.get(biome.biomeID);
		
		if(bProps != null && bProps.getWaterQualityId() != -1)
		{
			return bProps.getWaterQualityId();
		}
		
		int waterColour = biome.getWaterColorMultiplier();
		boolean looksBad = false;
		
		if(waterColour != 16777215)
		{
			Color bColor = new Color(waterColour);
			
			if(bColor.getRed() < 200 || bColor.getGreen() < 200 || bColor.getBlue() < 200)
			{
				looksBad = true;
			}
		}
		
		ArrayList<Type> typeList = new ArrayList<Type>();
		Type[] typeArray = BiomeDictionary.getTypesForBiome(biome);
		for(int i = 0; i < typeArray.length; i++)
		{
			typeList.add(typeArray[i]);
		}
		
		
		if(typeList.contains(Type.SWAMP) || typeList.contains(Type.JUNGLE) || typeList.contains(Type.DEAD) || typeList.contains(Type.WASTELAND) || y < (float)seaLvl/0.75F || looksBad)
		{
			return 1;
		} else if(typeList.contains(Type.OCEAN) || typeList.contains(Type.BEACH))
		{
			return 2;
		} else if(typeList.contains(Type.SNOWY) || typeList.contains(Type.CONIFEROUS) || biome.getFloatTemperature(x, y, z) < 0F || y > seaLvl * 2)
		{
			return 3;
		} else
		{
			return 0;
		}
	}
	
	@SubscribeEvent
	public void onBreakBlock(HarvestDropsEvent event)
	{
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		if(event.getHarvester() != null)
		{
			if(event.getResult() != Result.DENY && !event.getHarvester().capabilities.isCreativeMode)
			{
				EM_PhysManager.schedulePhysUpdate(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), true, "Normal");
			}
		} else
		{
			if(event.getResult() != Result.DENY)
			{
				EM_PhysManager.schedulePhysUpdate(event.getWorld(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), true, "Normal");
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerUseItem(PlayerUseItemEvent.Finish event)
	{
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(event.entityPlayer);
		
		if(tracker == null || event.item == null)
		{
			return;
		}
		
		ItemStack item = event.item;
		
		if(EM_Settings.itemProperties.containsKey(Item.itemRegistry.getNameForObject(item.getItem())) || EM_Settings.itemProperties.containsKey(Item.itemRegistry.getNameForObject(item.getItem()) + "," + item.getItemDamage()))
		{
			ItemProperties itemProps;
			if(EM_Settings.itemProperties.containsKey(Item.itemRegistry.getNameForObject(item.getItem()) + "," + item.getItemDamage()))
			{
				itemProps = EM_Settings.itemProperties.get(Item.itemRegistry.getNameForObject(item.getItem()) + "," + item.getItemDamage());
			} else
			{
				itemProps = EM_Settings.itemProperties.get(Item.itemRegistry.getNameForObject(item.getItem()));
			}
			
			if(itemProps.effTemp > 0F)
			{
				if(tracker.bodyTemp + itemProps.effTemp > itemProps.effTempCap)
				{
					if(tracker.bodyTemp <= itemProps.effTempCap)
					{
						tracker.bodyTemp = itemProps.effTempCap;
					}
				} else
				{
					tracker.bodyTemp += itemProps.effTemp;
				}
			} else
			{
				if(tracker.bodyTemp + itemProps.effTemp < itemProps.effTempCap)
				{
					if(tracker.bodyTemp >= itemProps.effTempCap)
					{
						tracker.bodyTemp = itemProps.effTempCap;
					}
				} else
				{
					tracker.bodyTemp += itemProps.effTemp;
				}
			}
			
			if(tracker.sanity + itemProps.effSanity >= 100F)
			{
				tracker.sanity = 100F;
			} else if(tracker.sanity + itemProps.effSanity <= 0F)
			{
				tracker.sanity = 0F;
			} else
			{
				tracker.sanity += itemProps.effSanity;
			}
			
			if(itemProps.effHydration > 0F)
			{
				tracker.hydrate(itemProps.effHydration);
			} else if(itemProps.effHydration < 0F)
			{
				tracker.dehydrate(Math.abs(itemProps.effHydration));
			}
			
			if(tracker.airQuality + itemProps.effAir >= 100F)
			{
				tracker.airQuality = 100F;
			} else if(tracker.airQuality + itemProps.effAir <= 0F)
			{
				tracker.airQuality = 0F;
			} else
			{
				tracker.airQuality += itemProps.effAir;
			}
		}
		
		if(item.getItem() == Items.golden_apple)
		{
			if(item.isItemDamaged())
			{
				tracker.hydration = 100F;
				tracker.sanity = 100F;
				tracker.airQuality = 100F;
				tracker.bodyTemp = 37F;
				if(!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN())
				{
					EM_StatusManager.syncMultiplayerTracker(tracker);
				}
			} else
			{
				tracker.sanity = 100F;
				tracker.hydrate(10F);
			}
			
			tracker.trackedEntity.removePotionEffect(EnviroPotion.frostbite.id);
			tracker.frostbiteLevel = 0;
		}
	}
	
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event)
	{
		if(event.getEntityLiving().isDead)
		{
			if(!event.getEntityLiving().isEntityAlive())
			{
				doDeath(event.getEntityLiving());
			}
			return;
		}
		
		if(event.getEntityLiving().world.isRemote)
		{
			if(event.getEntityLiving().getRNG().nextInt(5) == 0)
			{
				EM_StatusManager.createFX(event.getEntityLiving());
			}
			
			if(event.getEntityLiving() instanceof EntityPlayer && event.getEntityLiving().world.isRemote)
			{
				if(Minecraft.getMinecraft().player.isPotionActive(EnviroPotion.insanity))
				{
					int chance = 100 / (Minecraft.getMinecraft().player.getActivePotionEffect(EnviroPotion.insanity).getAmplifier() + 1);
					
					chance = chance > 0? chance : 1;
					
					if(event.getEntityLiving().getRNG().nextInt(chance) == 0)
					{
						new Hallucination(event.getEntityLiving());
					}
				}
				
				Hallucination.update();
			}
			return;
		}
		
		if(event.getEntityLiving() instanceof EntityPlayer)
		{
			InventoryPlayer invo = (InventoryPlayer)((EntityPlayer)event.getEntityLiving()).inventory;
			AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(event.getEntityLiving().posX - 0.5D, event.getEntityLiving().posY - 0.5D, event.getEntityLiving().posZ - 0.5D, event.getEntityLiving().posX + 0.5D, event.getEntityLiving().posY + 0.5D, event.getEntityLiving().posZ + 0.5D).expand(2D, 2D, 2D);
			if(event.entityLiving.worldObj.getEntitiesWithinAABB(TileEntityGas.class, boundingBox).size() <= 0)
			{
				ReplaceInvoItems(invo, Item.getItemFromBlock(ObjectHandler.davyLampBlock), 2, Item.getItemFromBlock(ObjectHandler.davyLampBlock), 1);
			}
			
			if(EM_Settings.foodSpoiling)
			{
				RotHandler.rotInvo(event.entityLiving.worldObj, invo);
			}
			
			if(event.entityLiving.getEntityData().hasKey("EM_SAFETY"))
			{
				if(event.entityLiving.worldObj.getTotalWorldTime() - event.entityLiving.getEntityData().getLong("EM_SAFETY") >= 1000L)
				{
					((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.funwaysFault, 1);
				}
			}
			
			if(event.entityLiving.getEntityData().hasKey("EM_THAT"))
			{
				if(event.entityLiving.worldObj.getTotalWorldTime() - event.entityLiving.getEntityData().getLong("EM_THAT") >= 1000L)
				{
					((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.thatJustHappened, 1);
				}
			}
			
			if(event.entityLiving.worldObj.provider.dimensionId == EM_Settings.caveDimID && event.entityLiving.getEntityData().hasKey("EM_CAVE_DIST"))
			{
				int[] prePos = event.entityLiving.getEntityData().getIntArray("EM_CAVE_DIST");
				int distance = MathHelper.floor_double(event.entityLiving.getDistance(prePos[0], prePos[1], prePos[2]));
				
				if(distance > prePos[3])
				{
					prePos[3] = distance;
					event.entityLiving.getEntityData().setIntArray("EM_CAVE_DIST", prePos);
				}
			}
			
			if(!event.entityLiving.isPotionActive(EnviroPotion.hypothermia) && !event.entityLiving.isPotionActive(EnviroPotion.frostbite) && event.entityLiving.worldObj.getBiomeGenForCoords(MathHelper.floor_double(event.entityLiving.posX), MathHelper.floor_double(event.entityLiving.posZ)).getEnableSnow())
			{
				if(event.entityLiving.getEntityData().hasKey("EM_WINTER"))
				{
					if(event.entityLiving.worldObj.getTotalWorldTime() - event.entityLiving.getEntityData().getLong("EM_WINTER") > 24000L * 7)
					{
						((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.winterIsComing, 1);
						event.entityLiving.getEntityData().removeTag("EM_WINTER");
					}
				} else
				{
					event.entityLiving.getEntityData().setLong("EM_WINTER", event.entityLiving.worldObj.getTotalWorldTime());
				}
			} else if(event.entityLiving.getEntityData().hasKey("EM_WINTER"))
			{
				event.entityLiving.getEntityData().removeTag("EM_WINTER");
			}
			
			if(event.entityLiving.isPotionActive(EnviroPotion.heatstroke) && event.entityLiving.getActivePotionEffect(EnviroPotion.heatstroke).getAmplifier() >= 2)
			{
				event.entityLiving.getEntityData().setBoolean("EM_BOILED", true);
			} else if(event.entityLiving.getEntityData().getBoolean("EM_BOILED") && !event.entityLiving.isPotionActive(EnviroPotion.heatstroke))
			{
				((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.hardBoiled, 1);
				event.entityLiving.getEntityData().removeTag("EM_BOILED");
			} else if(event.entityLiving.getEntityData().hasKey("EM_BOILED"))
			{
				event.entityLiving.getEntityData().removeTag("EM_BOILED");
			}
			
			if(event.entityLiving.worldObj.provider.dimensionId == EM_Settings.caveDimID && event.entityLiving.worldObj.getBlockLightValue(MathHelper.floor_double(event.entityLiving.posX), MathHelper.floor_double(event.entityLiving.posY), MathHelper.floor_double(event.entityLiving.posZ)) < 1)
			{
				int x = MathHelper.floor_double(event.entityLiving.posX);
				int y = MathHelper.floor_double(event.entityLiving.posY);
				int z = MathHelper.floor_double(event.entityLiving.posZ);
				
				if(!event.entityLiving.getEntityData().hasKey("EM_PITCH"))
				{
					event.entityLiving.getEntityData().setIntArray("EM_PITCH", new int[]{x, y, z});
				}
				
				if(event.entityLiving.getDistance(x, y, z) >= 250)
				{
					((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.itsPitchBlack, 1);
				}
			} else if(event.entityLiving.getEntityData().hasKey("EM_PITCH"))
			{
				event.entityLiving.getEntityData().removeTag("EM_PITCH");
			}
			
			if(EM_Settings.enableAirQ && EM_Settings.enableBodyTemp && EM_Settings.enableHydrate && EM_Settings.enableSanity && EM_Settings.enableLandslide && EM_Settings.enablePhysics && EM_Settings.enableQuakes)
			{
				int seaLvl = 48;
				
				if(EM_Settings.dimensionProperties.containsKey(event.entityLiving.worldObj.provider.dimensionId))
				{
					seaLvl = MathHelper.ceiling_double_int(EM_Settings.dimensionProperties.get(event.entityLiving.worldObj.provider.dimensionId).sealevel * 0.75F);
				} else if(event.entityLiving.worldObj.provider.dimensionId == EM_Settings.caveDimID)
				{
					seaLvl = 256;
				}
				
				if(event.entityLiving.posY < seaLvl)
				{
					long time = event.entityLiving.getEntityData().getLong("EM_MINE_TIME");
					long date = event.entityLiving.getEntityData().getLong("EM_MINE_DATE");
					time += event.entityLiving.worldObj.getTotalWorldTime() - date;
					event.entityLiving.getEntityData().setLong("EM_MINE_DATE", event.entityLiving.worldObj.getTotalWorldTime());
					event.entityLiving.getEntityData().setLong("EM_MINE_TIME", time);
					
					if(time > 24000L * 3L)
					{
						((EntityPlayer)event.entityLiving).addStat(EnviroAchievements.proMiner, 1);
					}
				} else
				{
					event.entityLiving.getEntityData().setLong("EM_MINE_DATE", event.entityLiving.worldObj.getTotalWorldTime());
				}
			} else
			{
				if(event.entityLiving.getEntityData().hasKey("EM_MINE_TIME"))
				{
					event.entityLiving.getEntityData().removeTag("EM_MINE_TIME");
				}
				if(event.entityLiving.getEntityData().hasKey("EM_MINE_DATE"))
				{
					event.entityLiving.getEntityData().removeTag("EM_MINE_DATE");
				}
			}
		}
		
		EnviroDataTracker tracker = EM_StatusManager.lookupTracker(event.entityLiving);
		
		if(tracker == null || tracker.isDisabled)
		{
			if((!EnviroMine.proxy.isClient() || EnviroMine.proxy.isOpenToLAN()) && (EM_Settings.enableAirQ || EM_Settings.enableBodyTemp || EM_Settings.enableHydrate || EM_Settings.enableSanity))
			{
				if(event.entityLiving instanceof EntityPlayer || (EM_Settings.trackNonPlayer && EnviroDataTracker.isLegalType(event.entityLiving)))
				{
					EnviroMine.logger.log(Level.WARN, "Server lost track of player! Attempting to re-sync...");
					EnviroDataTracker emTrack = new EnviroDataTracker((EntityLivingBase)event.entity);
					EM_StatusManager.addToManager(emTrack);
					emTrack.loadNBTTags();
					EM_StatusManager.syncMultiplayerTracker(emTrack);
					tracker = emTrack;
				} else
				{
					return;
				}
			} else
			{
				return;
			}
		}
		
		EM_StatusManager.updateTracker(tracker);
		
		UUID EM_DEHY1_ID = EM_Settings.DEHY1_UUID;
		
		if(tracker.hydration < 10F)
		{
			event.entityLiving.addPotionEffect(new PotionEffect(Potion.weakness.id, 200, 0));
			event.entityLiving.addPotionEffect(new PotionEffect(Potion.digSlowdown.id, 200, 0));
			
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			AttributeModifier mod = new AttributeModifier(EM_DEHY1_ID, "EM_Dehydrated", -0.25D, 2);
			
			if(mod != null && attribute.getModifier(mod.getID()) == null)
			{
				attribute.applyModifier(mod);
			}
		} else
		{
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			
			if(attribute.getModifier(EM_DEHY1_ID) != null)
			{
				attribute.removeModifier(attribute.getModifier(EM_DEHY1_ID));
			}
		}
		
		UUID EM_FROST1_ID = EM_Settings.FROST1_UUID;
		UUID EM_FROST2_ID = EM_Settings.FROST2_UUID;
		UUID EM_FROST3_ID = EM_Settings.FROST3_UUID;
		UUID EM_HEAT1_ID = EM_Settings.HEAT1_UUID;
		
		if(event.entityLiving.isPotionActive(EnviroPotion.heatstroke))
		{
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			AttributeModifier mod = new AttributeModifier(EM_HEAT1_ID, "EM_Heat", -0.25D, 2);
			
			if(mod != null && attribute.getModifier(mod.getID()) == null)
			{
				attribute.applyModifier(mod);
			}
		} else
		{
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			
			if(attribute.getModifier(EM_HEAT1_ID) != null)
			{
				attribute.removeModifier(attribute.getModifier(EM_HEAT1_ID));
			}
		}
		
		if(event.entityLiving.isPotionActive(EnviroPotion.hypothermia) || event.entityLiving.isPotionActive(EnviroPotion.frostbite))
		{
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			AttributeModifier mod = new AttributeModifier(EM_FROST1_ID, "EM_Frost_Cold", -0.25D, 2);
			String msg = "";
			
			if(event.entityLiving.isPotionActive(EnviroPotion.frostbite))
			{
				if(event.entityLiving.getActivePotionEffect(EnviroPotion.frostbite).getAmplifier() > 0)
				{
					mod = new AttributeModifier(EM_FROST3_ID, "EM_Frost_NOLEGS", -0.99D, 2);
					
					if(event.entityLiving instanceof EntityPlayer)
					{
						msg = "Your legs stiffen as they succumb to frostbite";
					}
				} else
				{
					mod = new AttributeModifier(EM_FROST2_ID, "EM_Frost_NOHANDS", -0.5D, 2);
					
					if(event.entityLiving instanceof EntityPlayer)
					{
						msg = "Your fingers start to feel numb and unresponsive";
					}
				}
			}
			if(mod != null && attribute.getModifier(mod.getID()) == null)
			{
				attribute.applyModifier(mod);
				
				if(event.entityLiving instanceof EntityPlayer && mod.getID() != EM_FROST1_ID)
				{
					((EntityPlayer)event.entityLiving).addChatMessage(new ChatComponentText(msg));
				}
			}
		} else
		{
			IAttributeInstance attribute = event.entityLiving.getEntityAttribute(SharedMonsterAttributes.movementSpeed);
			
			if(attribute.getModifier(EM_FROST1_ID) != null)
			{
				attribute.removeModifier(attribute.getModifier(EM_FROST1_ID));
			}
			if(attribute.getModifier(EM_FROST2_ID) != null && tracker.frostbiteLevel < 1)
			{
				attribute.removeModifier(attribute.getModifier(EM_FROST2_ID));
			}
			if(attribute.getModifier(EM_FROST3_ID) != null && tracker.frostbiteLevel < 2)
			{
				attribute.removeModifier(attribute.getModifier(EM_FROST3_ID));
			}
		}
		
		if(event.entityLiving instanceof EntityPlayer)
		{
			HandlingTheThing.stalkPlayer((EntityPlayer)event.entityLiving);
			if(event.entityLiving.isDead)
			{
				return;
			}
			
			if(((EntityPlayer)event.entityLiving).isPlayerSleeping() && tracker != null && !event.entityLiving.worldObj.isDaytime())
			{
				tracker.sleepState = "Asleep";
				tracker.lastSleepTime = (int)event.entityLiving.worldObj.getWorldInfo().getWorldTime() % 24000;
			} else if(tracker != null && event.entityLiving.worldObj.isDaytime())
			{
				int relitiveTime = (int)event.entityLiving.worldObj.getWorldInfo().getWorldTime() % 24000;
				
				if(tracker.sleepState.equals("Asleep") && tracker.lastSleepTime - relitiveTime > 100)
				{
					int timeSlept = MathHelper.floor_float(100*(12000 - (tracker.lastSleepTime - 12000))/12000);
					
					if(tracker.sanity + timeSlept > 100F)
					{
						tracker.sanity = 100;
					} else if(timeSlept >= 0)
					{
						tracker.sanity += timeSlept;
					} else
					{
						EnviroMine.logger.log(Level.ERROR, "Something went wrong while calculating sleep sanity gain! Result: " + timeSlept);
						tracker.sanity = 100;
						if(tracker.trackedEntity instanceof EntityPlayer)
						{
							((EntityPlayer)tracker.trackedEntity).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[ENVIROMINE] Sleep state failed to detect sleep time properly!"));
							((EntityPlayer)tracker.trackedEntity).addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "[ENVIROMINE] Defaulting to 100%"));
						}
					}
				}
				tracker.sleepState = "Awake";
			}
		}
	}
	
	public void ReplaceInvoItems(IInventory invo, Item fItem, int fDamage, Item rItem, int rDamage)
	{
		for(int i = 0; i < invo.getSizeInventory(); i++)
		{
			ItemStack stack = invo.getStackInSlot(i);
			
			if(stack != null)
			{
				if(stack.getItem() == fItem && (stack.getItemDamage() == fDamage || fDamage <= -1))
				{
					invo.setInventorySlotContents(i, new ItemStack(rItem, stack.getCount(), fDamage <= -1? stack.getItemDamage() : rDamage));
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onJump(LivingJumpEvent event)
	{
		if(event.getEntityLiving().isPotionActive(EnviroPotion.frostbite))
		{
			if(event.getEntityLiving().getActivePotionEffect(EnviroPotion.frostbite).getAmplifier() > 0)
			{
				event.getEntityLiving().motionY = 0;
			}
		}
	}
	
	@SubscribeEvent
	public void onLand(LivingFallEvent event)
	{
		if(event.getEntityLiving().getRNG().nextInt(5) == 0)
		{
			EM_PhysManager.schedulePhysUpdate(event.getEntityLiving().world, MathHelper.floor(event.getEntityLiving().posX), MathHelper.floor(event.getEntityLiving().posY - 1), MathHelper.floor(event.getEntityLiving().posZ), true, "Jump");
		}
	}
	
	private static boolean firstload = false;
	@SubscribeEvent
	public void onWorldLoad(Load event)
	{
		
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		
		
		//Load Custom Configs
		if (!firstload) 
		{
			//EnviroMine.theWorldEM = EM_WorldData.get(event.world);
			EM_ConfigHandler.initProfile(); 
			firstload = true;
		}
		
		if(EM_PhysManager.worldStartTime < 0)
		{
			EM_PhysManager.worldStartTime = event.getWorld().getTotalWorldTime();
		}
		
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		if(EM_Settings.worldDir == null && server.isServerRunning())
		{
			if(EnviroMine.proxy.isClient())
			{
				EM_Settings.worldDir = FMLCommonHandler.instance().getMinecraftServerInstance().getFile("saves/" + server.getFolderName());
			} else
			{
				EM_Settings.worldDir = server.getFile(server.getFolderName());
			}
			
			MineshaftBuilder.loadBuilders(new File(EM_Settings.worldDir.getAbsolutePath(), "data/EnviroMineshafts"));
			Earthquake.loadQuakes(new File(EM_Settings.worldDir.getAbsolutePath(), "data/EnviroEarthquakes"));
		}
	}
	
	@SubscribeEvent
	public void onWorldUnload(Unload event)
	{
		EM_StatusManager.saveAndDeleteWorldTrackers(event.getWorld());
		
		if(!event.getWorld().isRemote)
		{
			if(!FMLCommonHandler.instance().getMinecraftServerInstance().isServerRunning())
			{
				EM_PhysManager.physSchedule.clear();
				EM_PhysManager.excluded.clear();
				EM_PhysManager.usedSlidePositions.clear();
				EM_PhysManager.worldStartTime = -1;
				EM_PhysManager.chunkDelay.clear();
				
				if(EM_Settings.worldDir != null)
				{
					MineshaftBuilder.saveBuilders(new File(EM_Settings.worldDir.getAbsolutePath(), "data/EnviroMineshafts"));
					Earthquake.saveQuakes(new File(EM_Settings.worldDir.getAbsolutePath(), "data/EnviroEarthquakes"));
				}
				Earthquake.Reset();;
				MineshaftBuilder.clearBuilders();
				GasBuffer.reset();
				
				EM_Settings.worldDir = null;
			}
		}
	}
	
	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event)
	{
		if(event.getWorld().isRemote)
		{
			return;
		}
		
		if(!EM_PhysManager.chunkDelay.containsKey(event.getWorld().provider.getDimension() + "" + event.getChunk().x + "," + event.getChunk().z))
		{
			EM_PhysManager.chunkDelay.put(event.getWorld().provider.getDimension() + "" + event.getChunk().x + "," + event.getChunk().z, event.getWorld().getTotalWorldTime() + EM_Settings.chunkDelay);
		}
	}
	
	@SubscribeEvent
	public void onWorldSave(Save event)
	{		
		EM_StatusManager.saveAllWorldTrackers(event.getWorld());
		if(EM_Settings.worldDir != null && event.getWorld().provider.getDimension() == 0)
		{
			MineshaftBuilder.saveBuilders(new File(EM_Settings.worldDir.getAbsolutePath(), "data/EnviroMineshafts"));
		}
	}
	
	protected static MovingObjectPosition getMovingObjectPositionFromPlayer(World par1World, EntityPlayer par2EntityPlayer, boolean par3)
	{
		float f = 1.0F;
		float f1 = par2EntityPlayer.prevRotationPitch + (par2EntityPlayer.rotationPitch - par2EntityPlayer.prevRotationPitch) * f;
		float f2 = par2EntityPlayer.prevRotationYaw + (par2EntityPlayer.rotationYaw - par2EntityPlayer.prevRotationYaw) * f;
		double d0 = par2EntityPlayer.prevPosX + (par2EntityPlayer.posX - par2EntityPlayer.prevPosX) * (double)f;
		double d1 = par2EntityPlayer.prevPosY + (par2EntityPlayer.posY - par2EntityPlayer.prevPosY) * (double)f + (double)(par1World.isRemote ? par2EntityPlayer.getEyeHeight() - par2EntityPlayer.getDefaultEyeHeight() : par2EntityPlayer.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
		double d2 = par2EntityPlayer.prevPosZ + (par2EntityPlayer.posZ - par2EntityPlayer.prevPosZ) * (double)f;
		Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
		float f3 = MathHelper.cos(-f2 * 0.017453292F - (float)Math.PI);
		float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
		float f5 = -MathHelper.cos(-f1 * 0.017453292F);
		float f6 = MathHelper.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		double d3 = 5.0D;
		if(par2EntityPlayer instanceof EntityPlayerMP)
		{
			d3 = ((EntityPlayerMP)par2EntityPlayer).theItemInWorldManager.getBlockReachDistance();
		}
		Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
		return par1World.func_147447_a(vec3, vec31, par3, !par3, false);
	}
	
	/* Client only events */
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onEntitySoundPlay(PlaySoundAtEntityEvent event)
	{
		if(event.getEntity().getEntityData().getBoolean("EM_Hallucination"))
		{
			ResourceLocation resLoc = event.getSound().getSoundName();
			if(new File(resLoc.getResourcePath()).exists())
			{
				Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(event.getSound(), SoundCategory.NEUTRAL, 1.0F, (event.getEntity().world.rand.nextFloat() - event.getEntity().world.rand.nextFloat()) * 0.2F + 1.0F, (float)event.getEntity().posX, (float)event.getEntity().posY, (float)event.getEntity().posZ));
			}
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onMusicPlay(PlaySoundEvent17 event)
	{
		if(Minecraft.getMinecraft().thePlayer != null && event.category == SoundCategory.MUSIC && Minecraft.getMinecraft().thePlayer.dimension == EM_Settings.caveDimID)
		{
			// Replaces background music with cave ambience in the cave dimension
			event.result = PositionedSoundRecord.func_147673_a(new ResourceLocation("enviromine", "cave_ambience"));
		}
	}
	
	HashMap<String, EntityLivingBase> playerMob = new HashMap<String, EntityLivingBase>();
	
	@SuppressWarnings("unchecked")
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRender(RenderPlayerEvent.Pre event)
	{
		if(Minecraft.getMinecraft().player.isPotionActive(EnviroPotion.insanity) && Minecraft.getMinecraft().player.getActivePotionEffect(EnviroPotion.insanity).getAmplifier() >= 2)
		{
			event.setCanceled(true);
			
			EntityLivingBase entity = playerMob.get(event.getEntityPlayer().getCommandSenderName());
			if(entity == null || entity.world != event.getEntityPlayer().world)
			{
				BiomeGenBase biome = event.entityPlayer.worldObj.getBiomeGenForCoords(MathHelper.floor_double(event.entityPlayer.posX), MathHelper.floor_double(event.entityPlayer.posZ));
				ArrayList<SpawnListEntry> spawnList = (ArrayList<SpawnListEntry>)biome.getSpawnableList(EnumCreatureType.monster);
				
				if(spawnList.size() <= 0)
				{
					entity = new EntityZombie(event.entityPlayer.worldObj);
				} else
				{
					int spawnIndex = event.entityPlayer.getRNG().nextInt(spawnList.size());
					try
					{
						entity = (EntityLiving)spawnList.get(spawnIndex).entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {event.entityPlayer.worldObj});
					} catch(Exception e)
					{
						entity = new EntityZombie(event.entityPlayer.worldObj);
					}
				}
				
				playerMob.put(event.entityPlayer.getCommandSenderName(), entity);
			}
			entity.renderYawOffset = event.entityPlayer.renderYawOffset;
			entity.prevRenderYawOffset = event.entityPlayer.prevRenderYawOffset;
			entity.cameraPitch = event.entityPlayer.cameraPitch;
			entity.posX = event.entityPlayer.posX;
			entity.posY = event.entityPlayer.posY - event.entityPlayer.yOffset;
			entity.posZ = event.entityPlayer.posZ;
			entity.prevPosX = event.entityPlayer.prevPosX;
			entity.prevPosY = event.entityPlayer.prevPosY - event.entityPlayer.yOffset;
			entity.prevPosZ = event.entityPlayer.prevPosZ;
			entity.lastTickPosX = event.entityPlayer.lastTickPosX;
			entity.lastTickPosY = event.entityPlayer.lastTickPosY - event.entityPlayer.yOffset;
			entity.lastTickPosZ = event.entityPlayer.lastTickPosZ;
			entity.rotationPitch = event.entityPlayer.rotationPitch;
			entity.prevRotationPitch = event.entityPlayer.prevRotationPitch;
			entity.rotationYaw = event.entityPlayer.rotationYaw;
			entity.prevRotationYaw = event.entityPlayer.prevRotationYaw;
			entity.rotationYawHead = event.entityPlayer.rotationYawHead;
			entity.prevRotationYawHead = event.entityPlayer.prevRotationYawHead;
			entity.limbSwingAmount = event.entityPlayer.limbSwingAmount;
			entity.prevLimbSwingAmount = event.entityPlayer.prevLimbSwingAmount;
			entity.limbSwing = event.entityPlayer.limbSwing;
			entity.prevSwingProgress = event.entityPlayer.prevSwingProgress;
			entity.swingProgress = event.entityPlayer.swingProgress;
			entity.swingProgressInt = event.entityPlayer.swingProgressInt;
			ItemStack[] equipped = event.entityPlayer.getLastActiveItems();
			entity.setCurrentItemOrArmor(0, event.entityPlayer.getHeldItem());
			entity.setCurrentItemOrArmor(1, equipped[0]);
			entity.setCurrentItemOrArmor(2, equipped[1]);
			entity.setCurrentItemOrArmor(3, equipped[2]);
			entity.setCurrentItemOrArmor(4, equipped[3]);
			entity.motionX = event.entityPlayer.motionX;
			entity.motionY = event.entityPlayer.motionY;
			entity.motionZ = event.entityPlayer.motionZ;
			entity.ticksExisted = event.entityPlayer.ticksExisted;
			GL11.glPushMatrix();
			//GL11.glRotatef(180F, 0F, 1F, 0F);
			//GL11.glRotatef(180F - (event.entityPlayer.renderYawOffset + (event.entityPlayer.renderYawOffset - event.entityPlayer.prevRenderYawOffset) * partialTicks), 0F, 1F, 0F);
			RenderManager.instance.renderEntitySimple(entity, partialTicks);
			GL11.glPopMatrix();
		} else
		{
			playerMob.clear();
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRender(RenderLivingEvent.Specials.Pre event)
	{ 
		
		
		/*
		ItemStack plate = event.entity.getEquipmentInSlot(3);
		EntityPlayer thePlayer = Minecraft.getMinecraft().thePlayer;
		
		if(event.entity == thePlayer && Minecraft.getMinecraft().currentScreen != null)
		{
			// Prevents the pack from rendering weirdly in the inventory screen
			return;
		}
		
		GL11.glPushMatrix();
		
		if (plate != null && (event.renderer instanceof RenderBiped || event.renderer instanceof RenderPlayer))
		{
			if (plate.getItem() == ObjectHandler.camelPack && !(plate.hasTagCompound() && !plate.getTagCompound().hasKey("camelPackFill"))) {
				plate.getItem().onUpdate(plate, event.entity.worldObj, event.entity, 3, false);
			}
			
			if (plate.hasTagCompound() && plate.getTagCompound().hasKey("camelPackFill"))
			{
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				double diffX = (event.entity.lastTickPosX + (event.entity.posX - event.entity.lastTickPosX) * partialTicks) - (player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks);
				double diffY = (event.entity.lastTickPosY + (event.entity.posY - event.entity.lastTickPosY) * partialTicks) - (player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks) + (event.entity == thePlayer? -0.1D : event.entity.getEyeHeight() + (event.entity instanceof EntityPlayer? -0.1D : (0.1D * (event.entity.width/0.6D))));
				double diffZ = (event.entity.lastTickPosZ + (event.entity.posZ - event.entity.lastTickPosZ) * partialTicks) - (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks);
				GL11.glTranslated(diffX, diffY, diffZ);
				GL11.glRotatef(180F, 0F, 0F, 1F);
				GL11.glRotatef(180F + (event.entity.renderYawOffset + ((event.entity == player && (player.openContainer != player.inventoryContainer)) ? ((event.entity.renderYawOffset - event.entity.prevRenderYawOffset) * partialTicks) : 0)), 0F, 1F, 0F);
				GL11.glScaled(event.entity.width/0.6D, event.entity.width/0.6D, event.entity.width/0.6D);
				if(event.entity.isSneaking())
				{
					GL11.glRotatef(30F, 1F, 0F, 0F);
				}
				ModelCamelPack.RenderPack(event.entity, 0, 0, 0, 0, 0, .06325f);
			}
		}
		GL11.glPopMatrix();
		
		*/
	}
	
	float partialTicks = 1F;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void RenderTickEvent(TickEvent.RenderTickEvent event)
	{
		partialTicks = event.renderTickTime;
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onItemTooltip(ItemTooltipEvent event)
	{
		if(event.getItemStack() != null && event.getItemStack().hasTagCompound())
		{
			if (event.getItemStack().getTagCompound().hasKey("camelPackFill")) {
				int fill = event.getItemStack().getTagCompound().getInteger("camelPackFill");
				int max = event.getItemStack().getTagCompound().getInteger("camelPackMax");
				if (fill > max) {
					fill = max;
					event.getItemStack().getTagCompound().setInteger("camelPackFill", fill);
				}
				
				int disp = (fill <= 0 ? 0 : fill > max ? 100 : (int)(((float)fill/(float)max)*100));
				event.getToolTip().add(new TextComponentTranslation("misc.enviromine.tooltip.water", disp + "%",  fill, max).getUnformattedText());
				//event.toolTip.add("Water: " + disp + "% ("+fill+"/"+max+")");
			}
			
			if(event.getItemStack().getTagCompound().getLong("EM_ROT_DATE") > 0 && EM_Settings.foodSpoiling)
			{
				double rotDate = event.getItemStack().getTagCompound().getLong("EM_ROT_DATE");
				double rotTime = event.getItemStack().getTagCompound().getLong("EM_ROT_TIME");
				double curTime = event.getEntity().world.getTotalWorldTime();
				
				if(curTime - rotDate <= 0)
				{
					event.toolTip.add(new TextComponentTranslation("misc.enviromine.tooltip.rot", "0%" , MathHelper.floor((curTime - rotDate)/24000L) , MathHelper.floor(rotTime/24000L)).getUnformattedText());
					//event.toolTip.add("Rotten: 0% (Day " + MathHelper.floor_double((curTime - rotDate)/24000L) + "/" + MathHelper.floor_double(rotTime/24000L) + ")");
					//event.toolTip.add("Use-By: Day " + MathHelper.floor_double((rotDate + rotTime)/24000L));
				} else
				{
					event.toolTip.add(new TextComponentTranslation("misc.enviromine.tooltip.rot", MathHelper.floor((curTime - rotDate)/rotTime * 100D) + "%", MathHelper.floor((curTime - rotDate)/24000L), MathHelper.floor(rotTime/24000L)).getUnformattedText());
					//event.toolTip.add("Use-By: Day " + MathHelper.floor_double((rotDate + rotTime)/24000L));
				}
			}
			
			if(event.itemStack.getTagCompound().hasKey("gasMaskFill"))
			{
				int i = event.getItemStack().getTagCompound().getInteger("gasMaskFill");
				int max = event.getItemStack().getTagCompound().getInteger("gasMaskMax");
				int disp = (i <= 0 ? 0 : i > max ? 100 : (int)(i/(max/100F)));
				event.toolTip.add(new TextComponentTranslation("misc.enviromine.tooltip.filter", disp + "%", i, max).getUnformattedText());
			}
		}
	}
	
	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
	{
		if(event.getModID().equals(EM_Settings.ModID))
		{
			for(Configuration config : EM_ConfigMenu.tempConfigs)
			{
				config.save();
			}
			
			EM_ConfigHandler.ReloadConfig();
		}
	}
	
	@SubscribeEvent
	public void onCrafted(ItemCraftedEvent event) // Prevents exploit of making foods with almost rotten food to prolong total life of food supplies
	{
		if(event.player.world.isRemote || event.crafting == null || event.crafting.getItem() == null)
		{
			return;
		}
		
		RotProperties rotProps = null;
		long rotTime = (long)(EM_Settings.foodRotTime * 24000L);
		
		if(EM_Settings.rotProperties.containsKey("" + Item.REGISTRY.getNameForObject(event.crafting.getItem())))
		{
			rotProps = EM_Settings.rotProperties.get("" + Item.REGISTRY.getNameForObject(event.crafting.getItem()));
			rotTime = (long)(rotProps.days * 24000L);
		} else if(EM_Settings.rotProperties.containsKey("" + Item.REGISTRY.getNameForObject(event.crafting.getItem()) + "," + event.crafting.getItemDamage()))
		{
			rotProps = EM_Settings.rotProperties.get("" + Item.REGISTRY.getNameForObject(event.crafting.getItem()) + "," + event.crafting.getItemDamage());
			rotTime = (long)(rotProps.days * 24000L);
		}
		
		if(rotProps == null)
		{
			return; // Crafted item is not a rotting food
		}
		
		long lowestDate = -1L;
		
		for(int i = 0; i < event.craftMatrix.getSizeInventory(); i++)
		{
			ItemStack stack = event.craftMatrix.getStackInSlot(i);
			
			if(stack == null || stack.getItem() == null || stack.getTagCompound() == null)
			{
				continue;
			}
			
			if(stack.getTagCompound().hasKey("EM_ROT_DATE") && (lowestDate < 0 || stack.getTagCompound().getLong("EM_ROT_DATE") < lowestDate))
			{
				lowestDate = stack.getTagCompound().getLong("EM_ROT_DATE");
			}
		}
		
		if(lowestDate >= 0)
		{
			if(event.crafting.getTagCompound() == null)
			{
				event.crafting.setTagCompound(new NBTTagCompound());
			}
			
			event.crafting.getTagCompound().setLong("EM_ROT_DATE", lowestDate);
			event.crafting.getTagCompound().setLong("EM_ROT_TIME", rotTime);
		}
	}
}
