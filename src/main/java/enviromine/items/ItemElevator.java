package enviromine.items;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ItemElevator extends ItemBlock
{
	public ItemElevator(Block block)
	{
		super(block);
		this.setHasSubtypes(true);
		this.setUnlocalizedName("enviromine.elevator");
	}
	
	@Override
	public int getMetadata(int damageValue)
	{
		return damageValue%4;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return this.getUnlocalizedName() + (stack.getItemDamage()%2 == 0? "_top" : "_bottom");
	}

    /**
     * Gets an icon index based on an item's damage value
     */
	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage)
    {
        return this.field_150939_a.getIcon(1, damage);
    }
}
