package enviromine.core;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import enviromine.handlers.ObjectHandler;

public class EnviroTab extends CreativeTabs
{
	ArrayList<ItemStack> rawStacks = new ArrayList<ItemStack>();
	
	public EnviroTab(String par2Str) {
		super(par2Str);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getTabIconItem() {
		return new ItemStack(ObjectHandler.camelPack);
	}
	
	public void addRawStack(ItemStack stack)
	{
		rawStacks.add(stack);
	}

    /**
     * only shows items which have tabToDisplayOn == this
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
	@SideOnly(Side.CLIENT)
    public void displayAllReleventItems(NonNullList<ItemStack> list)
    {
        super.displayAllRelevantItems(list);
        
        list.addAll(rawStacks);
    }
}