package enviromine.world.features;

import java.util.ArrayList;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.core.EM_Settings;
import enviromine.gases.EnviroGasDictionary;
import enviromine.handlers.ObjectHandler;
import enviromine.trackers.properties.DimensionProperties;
import enviromine.world.features.mineshaft.MineshaftBuilder;

public class WorldFeatureGenerator implements IWorldGenerator
{
	public static ArrayList<int[]> pendingMines = new ArrayList<int[]>();
	public static boolean disableMineScan = false;
	
	public WorldFeatureGenerator()
	{
	}

	@Override
	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		if(world.isRemote)
		{
			return;
		}
		
		DimensionProperties dimensionProp = null;
		boolean allowMines = true;
		
		if(EM_Settings.dimensionProperties.containsKey(world.provider.getDimension()))
		{ 
			dimensionProp = EM_Settings.dimensionProperties.get(world.provider.getDimension());
			allowMines = dimensionProp.mineshaftGen;
		}
		
		if(allowMines && EM_Settings.oldMineGen)
		{
			if(!disableMineScan)
			{
				MineshaftBuilder.scanGrids(world, chunkX, chunkZ, random);
			}
			
			for(int i = MineshaftBuilder.pendingBuilders.size() - 1; i >= 0; i--)
			{
				MineshaftBuilder builder = MineshaftBuilder.pendingBuilders.get(i);
				
				if(builder.checkAndBuildSegments(chunkX, chunkZ))
				{
					MineshaftBuilder.pendingBuilders.remove(i);
					if(MineshaftBuilder.scannedGrids.containsKey("" + (chunkX/64) + "," + (chunkZ/64) + "," + world.provider.getDimension()))
					{
						int remBuilders = MineshaftBuilder.scannedGrids.get("" + (chunkX/64) + "," + (chunkZ/64) + "," + world.provider.getDimension());
						MineshaftBuilder.scannedGrids.put("" + (chunkX/64) + "," + (chunkZ/64) + "," + world.provider.getDimension(), remBuilders - 1);
					}
				}
			}
		}
		
		ReplaceCoal(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
		
		if(EM_Settings.gasGen && !EM_Settings.noGases)
		{
			for(int i = 4; i >= 0; i--)
			{
				GenGasPocket(random, chunkX, chunkZ, world, chunkGenerator, chunkProvider);
			}
		}
	}
	
	public void ReplaceCoal(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		DimensionProperties dProps = EM_Settings.dimensionProperties.get(world.provider.getDimension());
		int xOff = chunkX * 16;
		int zOff = chunkZ * 16;
		
		for(int i = 0; i < 16; i++)
		{
			for(int j = 0; j < (dProps != null? (float)dProps.sealevel : 64F) * 0.75F; j++)
			{
				for(int k = 0; k < 16; k++)
				{
					Item item = Item.getItemFromBlock(world.getBlockState(new BlockPos(i + xOff, j, k + zOff)).getBlock());
					
					if(world.getBlockState(new BlockPos(i + xOff, j, k + zOff)).getBlock() == Blocks.COAL_ORE || (item != null && SameOre(new ItemStack(item), "oreCoal")))
					{
						world.setBlockState(new BlockPos(i + xOff, j, k + zOff), ObjectHandler.flammableCoal.getDefaultState());
					}
				}
			}
		}
	}
	
	public boolean SameOre(ItemStack item, String oreName)
	{
		int[] oreIds = OreDictionary.getOreIDs(item);
		int findId = OreDictionary.getOreID(oreName);
		
		for(int id : oreIds)
		{
			if(id == findId)
			{
				return true;
			} else
			{
				continue;
			}
		}
		
		return false;
	}
	
	public void GenGasPocket(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider)
	{
		int rX = (chunkX * 16) + random.nextInt(16);
		int rY = 1 + random.nextInt(32);
		int rZ = (chunkZ * 16) + random.nextInt(16);
		
		while(world.getBlockState(new BlockPos(rX, rY - 1, rZ)).getBlock() == Blocks.AIR && rY > 1)
		{
			rY -= 1;
		}
		
		if(world.getSavedLightValue(EnumSkyBlock.SKY, rX, rY, rZ) > 1)
		{
			return;
		}
		
		if(world.getBlockState(new BlockPos(rX, rY, rZ)).getBlock() == Blocks.AIR)
		{
			Block bBlock = world.getBlockState(new BlockPos(rX, rY - 1, rZ)).getBlock();
			if((rY < 16 && rY > 0) || world.provider.isNether())
			{
				if(bBlock.getMaterial() == Material.WATER)
				{
					world.setBlockState(rX, rY, rZ, ObjectHandler.gasBlock, 0, 2);
					TileEntity tile = world.getTileEntity(new BlockPos(rX, rY, rZ));
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.hydrogenSulfide.gasID, 10);
						//EnviroMine.logger.log(Level.INFO, "Generating hydrogen sulfide at (" + rX + "," + rY + "," + rZ + ")");
					}
				} else if(bBlock.getMaterial() == Material.LAVA || bBlock.getMaterial() == Material.FIRE)
				{
					world.setBlockState(rX, rY, rZ, ObjectHandler.gasBlock, 0, 2);
					TileEntity tile = world.getTileEntity(new BlockPos(rX, rY, rZ));
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.carbonMonoxide.gasID, 25);
						gasTile.addGas(EnviroGasDictionary.sulfurDioxide.gasID, 25);
						//EnviroMine.logger.log(Level.INFO, "Generating carbon monoxide at (" + rX + "," + rY + "," + rZ + ")");
					}
				} else
				{
					world.setBlockState(rX, rY, rZ, ObjectHandler.gasBlock, 0, 2);
					TileEntity tile = world.getTileEntity(new BlockPos(rX, rY, rZ));
					
					if(tile instanceof TileEntityGas)
					{
						TileEntityGas gasTile = (TileEntityGas)tile;
						gasTile.addGas(EnviroGasDictionary.sulfurDioxide.gasID, 20);
						gasTile.addGas(EnviroGasDictionary.carbonDioxide.gasID, 30);
						//EnviroMine.logger.log(Level.INFO, "Generating sulfur dioxide at (" + rX + "," + rY + "," + rZ + ")");
					}
				}
			} else
			{
				world.setBlockState(rX, rY, rZ, ObjectHandler.gasBlock, 0, 2);
				TileEntity tile = world.getTileEntity(new BlockPos(rX, rY, rZ));
				
				if(tile instanceof TileEntityGas)
				{
					TileEntityGas gasTile = (TileEntityGas)tile;
					gasTile.addGas(EnviroGasDictionary.carbonDioxide.gasID, 50);
					//EnviroMine.logger.log(Level.INFO, "Generating carbon dioxide at (" + rX + "," + rY + "," + rZ + ")");
				}
			}
		}
	}
	
	public void SavePendingMines()
	{
	}
	
	public void LoadPendingMines()
	{
	}
}
