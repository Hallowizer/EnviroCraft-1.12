package enviromine.blocks;

import java.util.Random;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import enviromine.blocks.tiles.TileEntityFreezer;
import enviromine.handlers.ObjectHandler;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockFreezer extends BlockContainer implements ITileEntityProvider
{
    private final Random field_149955_b = new Random();
    
	public BlockFreezer(Material material)
	{
		super(material);
		this.setHardness(3.0F);
	}

    /**
     * Called upon block activation (right click on the block.)
     */
	@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (world.isRemote)
        {
            return true;
        }
        else
        {
            IInventory iinventory = (TileEntityFreezer)world.getTileEntity(pos);

            if (iinventory != null)
            {
                player.displayGUIChest(iinventory);
            }

            return true;
        }
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
	@Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
	@Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    /**
     * The type of render function that is called for this block
     */
	@Override
    public int getRenderType()
    {
        return ObjectHandler.renderSpecialID;
    }

    /**
     * Called when the block is placed in the world.
     */
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack)
    {
        Block block = world.getBlockState(pos.north()).getBlock();
        Block block1 = world.getBlockState(pos.south()).getBlock();
        Block block2 = world.getBlockState(pos.west()).getBlock();
        Block block3 = world.getBlockState(pos.east()).getBlock();
        byte b0 = 0;
        int l = MathHelper.floor((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        if (l == 0)
        {
            b0 = 2;
        }

        if (l == 1)
        {
            b0 = 5;
        }

        if (l == 2)
        {
            b0 = 3;
        }

        if (l == 3)
        {
            b0 = 4;
        }

        if (block != this && block1 != this && block2 != this && block3 != this)
        {
            world.setBlockMetadataWithNotify(pos, b0, 3);
        }
        else
        {
            if ((block == this || block1 == this) && (b0 == 4 || b0 == 5))
            {
                if (block == this)
                {
                    world.setBlockMetadataWithNotify(pos.north(), b0, 3);
                }
                else
                {
                    world.setBlockMetadataWithNotify(pos.south(), b0, 3);
                }

                world.setBlockMetadataWithNotify(pos, b0, 3);
            }

            if ((block2 == this || block3 == this) && (b0 == 2 || b0 == 3))
            {
                if (block2 == this)
                {
                    world.setBlockMetadataWithNotify(pos.west(), b0, 3);
                }
                else
                {
                    world.setBlockMetadataWithNotify(pos.east(), b0, 3);
                }

                world.setBlockMetadataWithNotify(pos, b0, 3);
            }
        }
    }

	@Override
    public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_)
    {
        TileEntityFreezer tileentitychest = (TileEntityFreezer)p_149749_1_.getTileEntity(new BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

        if (tileentitychest != null)
        {
            for (int i1 = 0; i1 < tileentitychest.getSizeInventory(); ++i1)
            {
                ItemStack itemstack = tileentitychest.getStackInSlot(i1);

                if (itemstack != null)
                {
                    float f = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = this.field_149955_b.nextFloat() * 0.8F + 0.1F; itemstack.getCount() > 0; p_149749_1_.spawnEntity(entityitem))
                    {
                        int j1 = this.field_149955_b.nextInt(21) + 10;

                        if (j1 > itemstack.getCount())
                        {
                            j1 = itemstack.getCount();
                        }

                        itemstack.shrink(j1);
                        entityitem = new EntityItem(p_149749_1_, (double)((float)p_149749_2_ + f), (double)((float)p_149749_3_ + f1), (double)((float)p_149749_4_ + f2), new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)this.field_149955_b.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)this.field_149955_b.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)this.field_149955_b.nextGaussian() * f3);

                        if (itemstack.hasTagCompound())
                        {
                            entityitem.getItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }
                    }
                }
            }

            p_149749_1_.func_147453_f(p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_);
        }

        super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
    }

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return new TileEntityFreezer();
	}

	@Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister p_149651_1_)
    {
        this.blockIcon = p_149651_1_.registerIcon("iron_block");
    }
}