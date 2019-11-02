package enviromine.blocks;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import enviromine.core.EnviroMine;
import enviromine.handlers.EM_PhysManager;

public class BlockNoPhysics extends Block
{
	public BlockNoPhysics()
	{
		super(Material.IRON);
		this.setBlockUnbreakable();
		this.setUnlocalizedName("enviromine.nophysblock");
		//below needs to be done with 1.8's new model json thing
		this.setBlockTextureName("enviromine:no_phys_block");
		this.setCreativeTab(EnviroMine.enviroTab);
		this.setTickRandomly(true);
	}
	
	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		EM_PhysManager.chunkDelay.put(world.provider.getDimension() + "" + (pos.getX() >> 4) + "," + (pos.getZ() >> 4), Long.MAX_VALUE);
	}
	
	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state)
	{
		EM_PhysManager.chunkDelay.put(world.provider.getDimension() + "" + (pos.getX() >> 4) + "," + (pos.getZ() >> 4), Long.MAX_VALUE);
	}
}
