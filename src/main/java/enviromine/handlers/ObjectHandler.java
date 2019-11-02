package enviromine.handlers;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.Level;

import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import enviromine.EntityPhysicsBlock;
import enviromine.blocks.BlockBurningCoal;
import enviromine.blocks.BlockDavyLamp;
import enviromine.blocks.BlockElevator;
import enviromine.blocks.BlockEsky;
import enviromine.blocks.BlockFireTorch;
import enviromine.blocks.BlockFlammableCoal;
import enviromine.blocks.BlockFreezer;
import enviromine.blocks.BlockGas;
import enviromine.blocks.BlockNoPhysics;
import enviromine.blocks.materials.MaterialElevator;
import enviromine.blocks.materials.MaterialGas;
import enviromine.blocks.tiles.TileEntityBurningCoal;
import enviromine.blocks.tiles.TileEntityDavyLamp;
import enviromine.blocks.tiles.TileEntityElevator;
import enviromine.blocks.tiles.TileEntityEsky;
import enviromine.blocks.tiles.TileEntityFreezer;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.core.EnviroMine;
import enviromine.items.EnviroArmor;
import enviromine.items.EnviroItemBadWaterBottle;
import enviromine.items.EnviroItemColdWaterBottle;
import enviromine.items.EnviroItemSaltWaterBottle;
import enviromine.items.ItemDavyLamp;
import enviromine.items.ItemElevator;
import enviromine.items.ItemSpoiledMilk;
import enviromine.items.RottenFood;

public class ObjectHandler
{
	public static HashMap<Block, ArrayList<Integer>> igniteList = new HashMap<Block, ArrayList<Integer>>();
	public static ArmorMaterial camelPackMaterial;
	
	public static Item badWaterBottle;
	public static Item saltWaterBottle;
	public static Item coldWaterBottle;
	
	public static Item airFilter;
	public static Item davyLamp;
	public static Item gasMeter;
	public static Item rottenFood;
	public static Item spoiledMilk;
	
	public static ItemArmor camelPack;
	public static ItemArmor gasMask;
	public static ItemArmor hardHat;
	
	public static Block davyLampBlock;
	public static Block elevator;
	public static Block gasBlock;
	public static Block fireGasBlock;
	
	public static Block flammableCoal;
	public static Block burningCoal;
	public static Block fireTorch;
	public static Block offTorch;
	
	public static Block esky;
	public static Block freezer;
	
	public static Block noPhysBlock;
	
	public static int renderGasID;
	public static int renderSpecialID;
	
	public static Material gasMat;
	public static Material elevatorMat;
	
	public static void initItems()
	{
		badWaterBottle = new EnviroItemBadWaterBottle().setMaxStackSize(1).setUnlocalizedName("enviromine.badwater").setCreativeTab(EnviroMine.enviroTab);
		saltWaterBottle = new EnviroItemSaltWaterBottle().setMaxStackSize(1).setUnlocalizedName("enviromine.saltwater").setCreativeTab(EnviroMine.enviroTab);
		coldWaterBottle = new EnviroItemColdWaterBottle().setMaxStackSize(1).setUnlocalizedName("enviromine.coldwater").setCreativeTab(EnviroMine.enviroTab);
		airFilter = new Item().setMaxStackSize(16).setUnlocalizedName("enviromine.airfilter").setCreativeTab(EnviroMine.enviroTab).setTextureName("enviromine:air_filter");
		rottenFood = new RottenFood(1).setMaxStackSize(64).setUnlocalizedName("enviromine.rottenfood").setCreativeTab(EnviroMine.enviroTab).setTextureName("enviromine:rot");
		spoiledMilk = new ItemSpoiledMilk().setUnlocalizedName("enviromine.spoiledmilk").setCreativeTab(EnviroMine.enviroTab).setTextureName("bucket_milk");
		
		camelPackMaterial = EnumHelper.addArmorMaterial("camelPack", 100, new int[]{2, 2, 0, 0}, 0);
		
		camelPack = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 1).setTextureName("camel_pack").setUnlocalizedName("enviromine.camelpack").setCreativeTab(null);
		
		gasMask = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("gas_mask").setUnlocalizedName("enviromine.gasmask").setCreativeTab(null);
		hardHat = (ItemArmor)new EnviroArmor(camelPackMaterial, 4, 0).setTextureName("hard_hat").setUnlocalizedName("enviromine.hardhat").setCreativeTab(EnviroMine.enviroTab);
	}
	
	public static void registerItems()
	{
		GameRegistry.registerItem(badWaterBottle, "badWaterBottle");
		GameRegistry.registerItem(saltWaterBottle, "saltWaterBottle");
		GameRegistry.registerItem(coldWaterBottle, "coldWaterBottle");
		GameRegistry.registerItem(airFilter, "airFilter");
		GameRegistry.registerItem(rottenFood, "rottenFood");
		GameRegistry.registerItem(spoiledMilk, "spoiledMilk");
		GameRegistry.registerItem(camelPack, "camelPack");
		GameRegistry.registerItem(gasMask, "gasMask");
		GameRegistry.registerItem(hardHat, "hardHat");

		// Empty Pack
		ItemStack camelStack1 = new ItemStack(camelPack);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("camelPackFill", 0);
		tag.setInteger("camelPackMax", 100);
		tag.setBoolean("isCamelPack", true);
		tag.setString("camelPath", Item.REGISTRY.getNameForObject(camelPack));
		camelStack1.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(camelStack1);
		
		// Full Pack
		ItemStack camelStack2 = new ItemStack(camelPack);
		tag = new NBTTagCompound();
		tag.setInteger("camelPackFill", 100);
		tag.setInteger("camelPackMax", 100);
		tag.setBoolean("isCamelPack", true);
		tag.setString("camelPath", Item.REGISTRY.getNameForObject(camelPack));
		camelStack2.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(camelStack2);
		
		// Empty Mask
		ItemStack mask = new ItemStack(gasMask);
		tag = new NBTTagCompound();
		tag.setInteger("gasMaskFill", 0);
		tag.setInteger("gasMaskMax", 1000);
		mask.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(mask);
		
		// Full Mask
		mask = new ItemStack(gasMask);
		tag = new NBTTagCompound();
		tag.setInteger("gasMaskFill", 1000);
		tag.setInteger("gasMaskMax", 1000);
		mask.setTagCompound(tag);
		EnviroMine.enviroTab.addRawStack(mask);
	}
	
	public static void initBlocks()
	{
		gasMat = new MaterialGas(MapColor.airColor);
		gasBlock = new BlockGas(gasMat).setBlockName("enviromine.gas").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("enviromine:gas_block");
		fireGasBlock = new BlockGas(gasMat).setBlockName("enviromine.firegas").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("enviromine:gas_block").setLightLevel(1.0F);

		elevatorMat = new MaterialElevator(MapColor.ironColor);
		elevator = new BlockElevator(elevatorMat).setBlockName("enviromine.elevator").setCreativeTab(EnviroMine.enviroTab).setBlockTextureName("iron_block");
		
		davyLampBlock = new BlockDavyLamp(Material.REDSTONE_LIGHT).setLightLevel(1.0F).setBlockName("enviromine.davy_lamp").setCreativeTab(EnviroMine.enviroTab);
		davyLamp = new ItemDavyLamp(davyLampBlock).setUnlocalizedName("enviromine.davylamp").setCreativeTab(EnviroMine.enviroTab);
		
		flammableCoal = new BlockFlammableCoal();
		burningCoal = new BlockBurningCoal(Material.ROCK).setBlockName("enviromine.burningcoal").setCreativeTab(EnviroMine.enviroTab);
		fireTorch = new BlockFireTorch(true).setTickRandomly(true).setBlockName("torch").setBlockTextureName("torch_on").setLightLevel(0.9375F).setCreativeTab(EnviroMine.enviroTab);
		offTorch = new BlockFireTorch(false).setTickRandomly(false).setBlockName("torch").setBlockTextureName("torch_on").setLightLevel(0F).setCreativeTab(EnviroMine.enviroTab);
		esky = new BlockEsky(Material.IRON).setBlockName("enviromine.esky").setCreativeTab(EnviroMine.enviroTab);
		freezer = new BlockFreezer(Material.IRON).setBlockName("enviromine.freezer").setCreativeTab(EnviroMine.enviroTab);
		
		noPhysBlock = new BlockNoPhysics();
		
		Blocks.REDSTONE_TORCH.setLightLevel(0.9375F);
	}
	
	public static void registerBlocks()
	{
		GameRegistry.registerBlock(gasBlock, "gas");
		GameRegistry.registerBlock(fireGasBlock, "firegas");
		GameRegistry.registerBlock(elevator, ItemElevator.class, "elevator");
		GameRegistry.registerBlock(davyLampBlock, ItemDavyLamp.class, "davy_lamp");
		GameRegistry.registerBlock(fireTorch, "firetorch");
		GameRegistry.registerBlock(offTorch, "offtorch");
		GameRegistry.registerBlock(burningCoal, "burningcoal");
		GameRegistry.registerBlock(flammableCoal, "flammablecoal");
		GameRegistry.registerBlock(esky, "esky");
		GameRegistry.registerBlock(freezer, "freezer");
		GameRegistry.registerBlock(noPhysBlock, "no_phys_block");
		
		// Must be done after registration
		Blocks.FIRE.setFireInfo(flammableCoal, 60, 100);
		
		// Ore Dictionary Stuffs
		OreDictionary.registerOre("oreCoal", flammableCoal);
	}
	
	public static void registerGases()
	{
	}
	
	public static void registerEntities()
	{
		int physID = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityPhysicsBlock.class, "EnviroPhysicsBlock", physID);
		EntityRegistry.registerModEntity(EntityPhysicsBlock.class, "EnviroPhysicsEntity", physID, EnviroMine.instance, 64, 1, true);
		GameRegistry.registerTileEntity(TileEntityGas.class, "enviromine.tile.gas");
		GameRegistry.registerTileEntity(TileEntityBurningCoal.class, "enviromine.tile.burningcoal");
		GameRegistry.registerTileEntity(TileEntityEsky.class, "enviromine.tile.esky");
		GameRegistry.registerTileEntity(TileEntityFreezer.class, "enviromine.tile.freezer");
		
		GameRegistry.registerTileEntity(TileEntityElevator.class, "enviromine.tile.elevator");
		

		GameRegistry.registerTileEntity(TileEntityDavyLamp.class, "enviromine.tile.davy_lamp");
	}
	
	public static void registerRecipes()
	{
		GameRegistry.addSmelting(badWaterBottle, new ItemStack(Items.POTIONITEM, 1, 0), 0.0F);
		GameRegistry.addSmelting(saltWaterBottle, new ItemStack(Items.POTIONITEM, 1, 0), 0.0F);
		GameRegistry.addSmelting(coldWaterBottle, new ItemStack(Items.POTIONITEM, 1, 0), 0.0F);
		GameRegistry.addShapelessRecipe(new ItemStack(coldWaterBottle, 1, 0), new ItemStack(Items.POTIONITEM, 1, 0), new ItemStack(Items.SNOWBALL, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(badWaterBottle, 1, 0), new ItemStack(Items.POTIONITEM, 1, 0), new ItemStack(Blocks.DIRT, 1));
		GameRegistry.addShapelessRecipe(new ItemStack(saltWaterBottle, 1, 0), new ItemStack(Items.POTIONITEM, 1, 0), new ItemStack(Blocks.SAND, 1));
		
		GameRegistry.addRecipe(new ItemStack(Items.SLIME_BALL, 4, 0), " r ", "rwr", " r ", 'w', new ItemStack(spoiledMilk, 1, 0), 'r', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.MYCELIUM), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.GRASS), 'x', new ItemStack(Blocks.BROWN_MUSHROOM), 'y', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.MYCELIUM), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.GRASS), 'y', new ItemStack(Blocks.BROWN_MUSHROOM), 'x', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.MYCELIUM), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.GRASS), 'x', new ItemStack(Blocks.RED_MUSHROOM), 'y', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.MYCELIUM), "xyx", "yzy", "xyx", 'z', new ItemStack(Blocks.GRASS), 'y', new ItemStack(Blocks.RED_MUSHROOM), 'x', new ItemStack(rottenFood, 1));
		GameRegistry.addRecipe(new ItemStack(Blocks.DIRT, 1), "xxx", "xxx", "xxx", 'x', new ItemStack(rottenFood));
		
		
		GameRegistry.addRecipe(new ItemStack(gasMask, 1), "xxx", "xzx", "yxy", 'x', new ItemStack(Items.IRON_INGOT), 'y', new ItemStack(airFilter), 'z', new ItemStack(Blocks.GLASS_PANE));
		GameRegistry.addRecipe(new ItemStack(hardHat, 1), "xyx", "xzx", 'x', new ItemStack(Items.DYE, 1, 11), 'y', new ItemStack(Blocks.REDSTONE_LAMP), 'z', new ItemStack(Items.IRON_HELMET, 1, 0));

		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xyx", 'x', new ItemStack(Items.IRON_INGOT), 'y', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE), 'z', new ItemStack(Items.COAL, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xyx", 'x', new ItemStack(Items.IRON_INGOT), 'y', new ItemStack(Items.PAPER), 'z', new ItemStack(Items.COAL, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xyx", "xzx", "xpx", 'x', new ItemStack(Items.IRON_INGOT), 'y', new ItemStack(Items.PAPER), 'p', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),'z', new ItemStack(Items.COAL, 1, 1));
		GameRegistry.addRecipe(new ItemStack(airFilter, 4), "xpx", "xzx", "xyx", 'x', new ItemStack(Items.IRON_INGOT), 'y', new ItemStack(Items.PAPER), 'p', new ItemStack(Blocks.WOOL, 1, OreDictionary.WILDCARD_VALUE),'z', new ItemStack(Items.COAL, 1, 1));
		
		GameRegistry.addRecipe(new ItemStack(elevator, 1, 0), "xyx", "z z", "z z", 'x', new ItemStack(Blocks.IRON_BLOCK), 'y', new ItemStack(Blocks.REDSTONE_LAMP), 'z', new ItemStack(Blocks.IRON_BARS));
		GameRegistry.addRecipe(new ItemStack(elevator, 1, 1), "z z", "xyx", "www", 'x', new ItemStack(Blocks.IRON_BLOCK), 'y', new ItemStack(Blocks.FURNACE), 'z', new ItemStack(Blocks.IRON_BARS), 'w', new ItemStack(Items.DIAMOND_PICKAXE));
		
		GameRegistry.addRecipe(new ItemStack(davyLampBlock), " x ", "zyz", "xxx", 'x', new ItemStack(Items.GOLD_INGOT), 'y', new ItemStack(Blocks.TORCH), 'z', new ItemStack(Blocks.GLASS_PANE));
		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(Items.FLINT_AND_STEEL, 1, OreDictionary.WILDCARD_VALUE));
		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(Blocks.TORCH));
		GameRegistry.addShapelessRecipe(new ItemStack(davyLampBlock, 1, 1), new ItemStack(davyLampBlock, 1, 0), new ItemStack(fireTorch));
		
		GameRegistry.addRecipe(new ItemStack(esky), "xxx", "yzy", "yyy", 'x', new ItemStack(Blocks.SNOW), 'y', new ItemStack(Items.DYE, 1, 4), 'z', new ItemStack(Blocks.CHEST));
		GameRegistry.addRecipe(new ItemStack(freezer), "xyx", "yzy", "xyx", 'x', new ItemStack(Blocks.IRON_BLOCK), 'y', new ItemStack(Blocks.ICE), 'z', new ItemStack(esky));
		GameRegistry.addRecipe(new ItemStack(freezer), "xyx", "yzy", "xyx", 'x', new ItemStack(Blocks.IRON_BLOCK), 'y', new ItemStack(Blocks.PACKED_ICE), 'z', new ItemStack(esky));
		
		ItemStack camelStack = new ItemStack(camelPack);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("camelPackFill", 0);
		tag.setInteger("camelPackMax", 100);
		tag.setBoolean("isCamelPack", true);
		tag.setString("camelPath", Item.REGISTRY.getNameForObject(camelPack));
		camelStack.setTagCompound(tag);
		GameRegistry.addRecipe(camelStack, "xxx", "xyx", "xxx", 'x', new ItemStack(Items.LEATHER), 'y', new ItemStack(Items.GLASS_BOTTLE));
		
		ItemStack camelStack2 = camelStack.copy();
		camelStack2.getTagCompound().setInteger("camelPackFill", 25);
		GameRegistry.addRecipe(camelStack2, "xxx", "xyx", "xxx", 'x', new ItemStack(Items.LEATHER), 'y', new ItemStack(Items.POTIONITEM, 1, 0));
	}

	public static String[] DefaultIgnitionSources()
	{
		String[] list = new String[]{Block.REGISTRY.getNameForObject(Blocks.FLOWING_LAVA), 
			Block.REGISTRY.getNameForObject(Blocks.LAVA),
			Block.REGISTRY.getNameForObject(Blocks.TORCH),
			Block.REGISTRY.getNameForObject(Blocks.LIT_FURNACE),
			Block.REGISTRY.getNameForObject(Blocks.FIRE),
			Block.REGISTRY.getNameForObject(ObjectHandler.fireGasBlock),
			Block.REGISTRY.getNameForObject(ObjectHandler.fireTorch),
			Block.REGISTRY.getNameForObject(ObjectHandler.burningCoal)};
		
		return list;
	}	
	
	public static void LoadIgnitionSources(String[] listIn)
	{
		for(String source : listIn)
		{
			try
			{
				Block sBlock = Block.getBlockFromName(source);
				igniteList.put(sBlock, new ArrayList<Integer>());
				EnviroMine.logger.log(Level.INFO, "Registered "+ sBlock.getLocalizedName() +"("+source+") as an ignition source.");
			}catch(NullPointerException e)
			{
				EnviroMine.logger.log(Level.ERROR, "Could not find "+ source);
			}
			
		}
	}
}
