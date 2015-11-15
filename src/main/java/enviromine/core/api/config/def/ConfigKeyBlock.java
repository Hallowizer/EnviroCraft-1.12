package enviromine.core.api.config.def;

import net.minecraft.block.Block;
import enviromine.core.api.config.ConfigKey;

public class ConfigKeyBlock extends ConfigKey
{
	public Block block;
	public int[] metadata;
	
	public ConfigKeyBlock(Block block, int[] metadata)
	{
		this.block = block;
		this.metadata = metadata;
	}
	
	@Override
	public boolean SameKey(ConfigKey key) // Note: This will return true even if this key has more meta values than requested, as long as all requested values are present here.
	{
		if(!(key instanceof ConfigKeyBlock))
		{
			return false;
		}
		
		ConfigKeyBlock bKey = (ConfigKeyBlock)key;
		
		if(block != bKey.block)
		{
			return false;
		}
		
		if(bKey.isWildcard()) // Request is a wildcard, metadata is ignored
		{
			return true;
		} else if(this.isWildcard()) // This is a wildcard, request is for specific only
		{
			return false;
		}
		
		toploop:
		for(int meta1 : bKey.metadata)
		{
			for(int meta2 : metadata)
			{
				if(meta1 == meta2)
				{
					continue toploop;
				}
			}
			
			return false; // This key is missing one of the required metadata values requested
		}
		
		return true;
	}
	
	@Override
	public boolean isWildcard()
	{
		return metadata.length <= 0;
	}
	
	@Override
	public void setWildcard()
	{
		metadata = new int[]{};
	}
	
	@Override
	public ConfigKey copy()
	{
		return new ConfigKeyBlock(block, metadata);
	}
}