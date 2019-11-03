package enviromine.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.blocks.tiles.TileEntityGas;
import enviromine.core.EM_Settings;
import enviromine.gases.EnviroGas;
import enviromine.gases.EnviroGasDictionary;
import enviromine.gases.GasBuffer;
import enviromine.handlers.ObjectHandler;
import enviromine.utils.EnviroUtils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

public class BlockGas extends Block implements ITileEntityProvider
{
	public IIcon gasIcon;
	public IIcon gasFireIcon;
	
	public BlockGas(Material par2Material)
	{
		super(par2Material);
		this.setTickRandomly(true);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		super.onBlockAdded(world, pos, state);
		
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile == null)
		{
			tile = new TileEntityGas(world);
			world.setTileEntity(pos, tile);
		}
		
		if(this == ObjectHandler.fireGasBlock)
		{
			for(int dir = 0; dir < ForgeDirection.VALID_DIRECTIONS.length; dir++)
			{
				int xOff = ForgeDirection.VALID_DIRECTIONS[dir].offsetX + pos.getX();
				int yOff = ForgeDirection.VALID_DIRECTIONS[dir].offsetY + pos.getY();
				int zOff = ForgeDirection.VALID_DIRECTIONS[dir].offsetZ + pos.getZ();
				
				Block sBlock = world.getBlock(xOff, yOff, zOff);
				
				if((sBlock.isFlammable(world, xOff, yOff, zOff, ForgeDirection.VALID_DIRECTIONS[dir].getOpposite()) && !(sBlock instanceof BlockGas)) || sBlock == Blocks.AIR)
				{
					world.setBlock(xOff, yOff, zOff, Blocks.FIRE, 0, 3);
				}
			}
		}
		
		GasBuffer.scheduleUpdate(world, pos, this);
	}
	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLiving, ItemStack itemStack)
	{
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			
			//EnviroGasDictionary.gasFire.setDecayRates(1, 1, 100).setDensity(-1F);
			//EnviroGasDictionary.methane.setVolitility(10F);
			//EnviroGasDictionary.hydrogenSulfide.setVolitility(100F);
			//EnviroGasDictionary.carbonDioxide.setDecayRates(1, 1, 100, 5);
			//EnviroGasDictionary.carbonMonoxide.setDecayRates(1, 0, 100, 1);
			
			//gasTile.addGas(1, 10);
			//gasTile.addGas(3, 50);
			//gasTile.addGas(4, 100); // METHANE
			//gasTile.addGas(0, 2000); // FIRE
			gasTile.addGas(7, 100); // NUKE
			gasTile.updateRender();
		}
	}
	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getTileEntity(new BlockPos(i, j, k));
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return gasTile.color.getRGB();
		} else
		{
			return Color.WHITE.getRGB();
		}
	}
	
	public float getOpacity(IBlockAccess blockAccess, BlockPos pos)
	{
		if(EM_Settings.renderGases)
		{
			return 0.75F;
		} else
		{
			TileEntity tile = blockAccess.getTileEntity(pos);
			
			if(tile != null && tile instanceof TileEntityGas)
			{
				float maxOpacity = ((TileEntityGas)tile).opacity;
				return maxOpacity;
			} else
			{
				return 0F;
			}
		}
	}
	
	public void swtichIgnitionState(World world, BlockPos pos)
	{
        TileEntity tile = world.getTileEntity(pos);
        Block newBlock = this;
        
        if(this == ObjectHandler.gasBlock)
        {
            world.setBlock(pos, ObjectHandler.fireGasBlock);
            newBlock = ObjectHandler.fireGasBlock;
        } else
        {
            world.setBlock(pos, ObjectHandler.gasBlock);
            newBlock = ObjectHandler.gasBlock;
        }
        
        if (tile != null)
        {
            tile.validate();
            world.setTileEntity(pos, tile);
            tile.blockType = newBlock;
            
            if(tile instanceof TileEntityGas)
            {
            	((TileEntityGas)tile).updateRender();
            }
        }
	}
	
	@Override
	public int tickRate(World world)
	{
		if(this  == ObjectHandler.fireGasBlock)
		{
			return EM_Settings.gasTickRate/4;
		} else
		{
			return EM_Settings.gasTickRate;
		}
	}
	
	@Override
	public int getRenderColor(int meta)
	{
		return 16777215;
	}
	
	@Override
	public int getRenderType()
	{
		return ObjectHandler.renderGasID;
	}
	
	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	@Override
	public boolean canCollideCheck(int par1, boolean par2)
	{
		return false;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}
	
	public boolean isBlockReplaceable(World world, int x, int y, int z)
	{
		return true;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int i, int j, int k, int side)
	{
		double yMax = this.getMaxY(blockAccess, i, j, k);
		double yMin = this.getMinY(blockAccess, i, j, k);
		float opacity = this.getOpacity(blockAccess, new BlockPos(i, j, k));
		
		if(opacity <= 0.1F)
		{
			return false;
		}
		
		int[] sideCoord = EnviroUtils.getAdjacentBlockCoordsFromSide(i, j, k, side);
		if(blockAccess.getBlockState(new BlockPos(sideCoord[0], sideCoord[1], sideCoord[2])).getBlock() == ObjectHandler.gasBlock || blockAccess.getBlockState(new BlockPos(sideCoord[0], sideCoord[1], sideCoord[2])).getBlock() == ObjectHandler.fireGasBlock)
		{
			double sideYMax = this.getMaxY(blockAccess, sideCoord[0], sideCoord[1], sideCoord[2]);
			double sideYMin = this.getMinY(blockAccess, sideCoord[0], sideCoord[1], sideCoord[2]);
			
			if(this.getOpacity(blockAccess, new BlockPos(sideCoord[0], sideCoord[1], sideCoord[2])) <= 0.1F)
			{
				return true;
			} else if(side > 1) // Sides
			{
				
				if(sideYMin > yMin || sideYMax < yMax)
				{
					return true;
				} else
				{
					return false;
				}
			} else if(side == 0) // Bottom
			{
				if(sideYMax != 1.0F || yMin != 0.0F)
				{
					return true;
				} else
				{
					return false;
				}
			} else if(side == 1) // Top
			{
				if(yMax != 1.0F || sideYMin != 0.0F)
				{
					return true;
				} else
				{
					return false;
				}
			} else
			{
				return true;
			}
		} else
		{
			if(side == 0 && yMin != 0.0F)
			{
				return true;
			} else if(side == 1 && yMax != 1.0F)
			{
				return true;
			} else
			{
				return !blockAccess.getBlockState(new BlockPos(sideCoord[0], sideCoord[1], sideCoord[2])).isOpaqueCube();
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor()
	{
		return 0;
	}
	
	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		if(world.isRemote)
		{
			return;
		} else if(EM_Settings.noGases)
		{
			world.setBlockToAir(pos);
			return;
		}
		
		boolean scheduleTick = false;
		
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile == null || !(tile instanceof TileEntityGas))
		{
			world.setBlockToAir(pos);
			return;
		} else
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			
			/*if(gasTile.amount >= 10000)
			{
				EnviroMine.logger.log(Level.ERROR, "Too many gases inside one block! (" + gasTile.amount + " / 10000)");
				world.removeTileEntity(x, y, z);
				world.setBlockToAir(x, y, z);
				return;
			}*/
			
			int fireNum = gasTile.getGasQuantity(0);
			
			if(isTouchingIgnition(world, pos) && this == ObjectHandler.gasBlock)
			{
				if(gasTile.burnGases())
				{
					//this.swtichIgnitionState(world, x, y, z);
		            world.playSoundEffect(x, y, z, "enviromine:gas_ignite", 1.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
					return;
				}
			} else if(fireNum >= 1 && this == ObjectHandler.gasBlock)
			{
				gasTile.burnGases();
				//this.swtichIgnitionState(world, x, y, z);
				return;
			} else if(fireNum <= 0 && this == ObjectHandler.fireGasBlock)
			{
				this.swtichIgnitionState(world, pos);
				return;
			}
			
			if(gasTile.gases.size() <= 0 || gasTile.amount <= 0)
			{
				world.setBlockToAir(pos);
				return;
			} else if(gasTile.spreadGas())
			{
				world.notifyBlocksOfNeighborChange(pos, this);
			} else if(gasTile.amount > 10 || this == ObjectHandler.fireGasBlock)
			{
				scheduleTick = true;
			}
			
			fireNum = gasTile.getGasQuantity(0);
			
			if(fireNum >= 1 && this == ObjectHandler.gasBlock)
			{
				gasTile.burnGases();
				this.swtichIgnitionState(world, pos);
				return;
			} else if(fireNum <= 0 && this == ObjectHandler.fireGasBlock)
			{
				this.swtichIgnitionState(world, pos);
				return;
			}
			
			if(fireNum > 20)
			{
				if(gasTile.firePressure >= 10)
				{
					scheduleTick = false;
					world.setBlockToAir(pos);
					if(fireNum > 80)
					{
						world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), 16F, true, true);
					} else
					{
						world.newExplosion(null, pos.getX(), pos.getY(), pos.getZ(), fireNum/5F, true, true);
					}
					return;
				} else
				{
					gasTile.firePressure += 1;
				}
			} else
			{
				gasTile.firePressure = 0;
			}
			
			if(gasTile.gases.size() <= 0 || gasTile.amount <= 0)
			{
				world.setBlockToAir(pos);
				scheduleTick = false;
				return;
			} else
			{
				gasTile.updateRender();
			}
		}
		
		if(scheduleTick)
		{
			GasBuffer.scheduleUpdate(world, pos, this);
		}
	}
	
	public boolean isTouchingIgnition(World world, int x, int y, int z)
	{
		ArrayList<int[]> dir = new ArrayList<int[]>();
		
		dir.add(new int[]{-1,0,0});
		dir.add(new int[]{1,0,0});
		dir.add(new int[]{0,-1,0});
		dir.add(new int[]{0,1,0});
		dir.add(new int[]{0,0,-1});
		dir.add(new int[]{0,0,1});
		
		for(int i = 0; i < dir.size(); i++)
		{
			int[] pos = dir.get(i);
			Block block = world.getBlockState(new BlockPos(x + pos[0], y + pos[1], z + pos[2])).getBlock();
			int meta = world.getBlockMetadata(x + pos[0], y + pos[1], z + pos[2]);
			
			if(ObjectHandler.igniteList.containsKey(block) && (ObjectHandler.igniteList.get(block).isEmpty() || ObjectHandler.igniteList.get(block).contains(meta)))
			{
				return true;
			} else
			{
				TileEntity tile = world.getTileEntity(new BlockPos(x + pos[0], y + pos[1], z + pos[2]));
				
				if(tile != null && tile instanceof TileEntityGas)
				{
					TileEntityGas gasTile = (TileEntityGas)tile;
					
					if(gasTile.getGasQuantity(EnviroGasDictionary.gasFire.gasID) > 0)
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, Block block)
	{
		GasBuffer.scheduleUpdate(world, pos, this);
		
		if(world.isRemote && (block == ObjectHandler.gasBlock || block == ObjectHandler.fireGasBlock))
		{
			TileEntity tile = world.getTileEntity(pos);
			
			if(tile != null && tile instanceof TileEntityGas)
			{
				TileEntityGas gasTile = (TileEntityGas)tile;
				
				gasTile.updateOpacity();
				gasTile.updateColor();
				gasTile.updateSize();
				gasTile.updateRender();
			}
		}
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity)
	{
		
		TileEntity tile = world.getTileEntity(pos);
		
		if(tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			
			if(entity.isBurning() && this == ObjectHandler.gasBlock)
			{
				if(gasTile.burnGases())
				{
					//this.swtichIgnitionState(world, x, y, z);
					return;
				}
			}
			
			if(!(entity instanceof EntityLivingBase))
			{
				return;
			}
			
			EntityLivingBase entityLiving = (EntityLivingBase)entity;
			gasTile.doAllEffects(entityLiving);
			
			if(entityLiving instanceof EntityPlayer)
			{
				EntityPlayer player = (EntityPlayer)entityLiving;
				int state = 1;
				
				for(int jj = 0; jj < gasTile.gases.size(); jj++)
				{
					EnviroGas gasInfo = EnviroGasDictionary.gasList[gasTile.gases.get(jj)[0]];
					if(gasInfo.volitility > 0)
					{
						state = 2;
						break;
					} else if(gasInfo.suffocation > 0)
					{
						state = 0;
					}
				}
				
				if(state == 1)
				{
					return;
				}
				
				for(int i = 0; i < player.inventory.getSizeInventory(); i++)
				{
					ItemStack stack = player.inventory.getStackInSlot(i);
					if(stack != null && stack.getItem() == Item.getItemFromBlock(ObjectHandler.davyLampBlock) && stack.getItemDamage() > 0)
					{
						stack.setItemDamage(state);
					}
				}
			}
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int i)
	{
		TileEntityGas tile = new TileEntityGas();
		return tile;
	}
	
	@Override
	public IIcon getIcon(int par1, int par2)
	{
		if(this == ObjectHandler.fireGasBlock)
		{
			return gasFireIcon;
		} else
		{
			return gasIcon;
		}
	}
	
	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		this.gasIcon = register.registerIcon("enviromine:block_gas");
		this.gasFireIcon = register.registerIcon("enviromine:block_gas_fire");
		//this.blockIcon = register.registerIcon("enviromine:block_gas");
	}
	
	/**
	 * Return whether this block can drop from an explosion.
	 */
	@Override
	public boolean canDropFromExplosion(Explosion par1Explosion)
	{
		return false;
	}

	@Override
    public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
    {
        return null;
    }
    
    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }
    
    @Override
    public void onBlockDestroyedByExplosion(World world, BlockPos pos, Explosion explosion)
    {
    	if(world.isBlockNormalCubeDefault(pos.down(), false) && this == ObjectHandler.fireGasBlock)
    	{
    		world.setBlockState(pos, Blocks.FIRE.getDefaultState());
    	}
    	

		ArrayList<int[]> dir = new ArrayList<int[]>();
		
		dir.add(new int[]{-1,0,0});
		dir.add(new int[]{1,0,0});
		dir.add(new int[]{0,-1,0});
		dir.add(new int[]{0,1,0});
		dir.add(new int[]{0,0,-1});
		dir.add(new int[]{0,0,1});
		
		for(int i = 0; i < dir.size(); i++)
		{
			int[] ipos = dir.get(i);
			
			TileEntity tile = world.getTileEntity(new BlockPos(pos.getX() + ipos[0], pos.getY() + ipos[1], pos.getZ() + ipos[2]));
			
			if(tile != null && tile instanceof TileEntityGas)
			{
				TileEntityGas gasTile = (TileEntityGas)tile;
				
				if(gasTile.burnGases())
				{
					if(gasTile.getBlockType() == ObjectHandler.fireGasBlock)
					{
						((BlockGas)gasTile.getBlockType()).swtichIgnitionState(world, new BlockPos(pos.getX() + ipos[0], pos.getY() + ipos[1], pos.getZ() + ipos[2]));
			            world.playSoundEffect(pos, "enviromine:gas_ignite", 1.0F, (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
					}
				}
			}
		}
    }
    
    public ArrayList<int[]> getGasInfo(World world, int i, int j, int k)
    {
    	TileEntity tile = world.getTileEntity(new BlockPos(i, j, k));
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return gasTile.gases;
		} else
		{
			return new ArrayList<int[]>();
		}
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int i, int j, int k, Random rand)
    {
    	if(this == ObjectHandler.fireGasBlock)
    	{
            if (rand.nextInt(24) == 0)
            {
                world.playSound((double)((float)i + 0.5F), (double)((float)j + 0.5F), (double)((float)k + 0.5F), "fire.fire", 1.0F + rand.nextFloat(), rand.nextFloat() * 0.7F + 0.3F, false);
            }
            
	        double d0 = (double)((float)i + 0.5F);
	        double d1 = (double)((float)j + 0.5F);
	        double d2 = (double)((float)k + 0.5F);
	        
	        double d3 = rand.nextDouble() - 0.5D;
	        double d4 = rand.nextDouble() - 0.5D;
	        
	        world.spawnParticle("largesmoke", d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
	        world.spawnParticle("flame", d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
    	}
    }
	
	public double getMinY(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getTileEntity(new BlockPos(i, j, k));
		
		if(tile != null && tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return (double)gasTile.yMin;
		} else
		{
			return 0D;
		}
	}
	
	public double getMaxY(IBlockAccess blockAccess, int i, int j, int k)
	{
		TileEntity tile = blockAccess.getTileEntity(new BlockPos(i, j, k));
		
		if(tile instanceof TileEntityGas)
		{
			TileEntityGas gasTile = (TileEntityGas)tile;
			return (double)gasTile.yMax;
		} else
		{
			return 1D;
		}
	
	}
}
