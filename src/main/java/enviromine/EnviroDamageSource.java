package enviromine;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

public class EnviroDamageSource extends DamageSource
{
	public static EnviroDamageSource heatstroke = (EnviroDamageSource)(new EnviroDamageSource("heatstroke")).setDamageBypassesArmor();
	public static EnviroDamageSource organfailure = (EnviroDamageSource)(new EnviroDamageSource("organfailure")).setDamageBypassesArmor();
	public static EnviroDamageSource bleedout = (EnviroDamageSource)(new EnviroDamageSource("bleedout")).setDamageBypassesArmor();
	public static EnviroDamageSource suffocate = (EnviroDamageSource)(new EnviroDamageSource("suffocate")).setDamageBypassesArmor();
	public static EnviroDamageSource frostbite = (EnviroDamageSource)(new EnviroDamageSource("frostbite")).setDamageBypassesArmor();
	public static EnviroDamageSource dehydrate = (EnviroDamageSource)(new EnviroDamageSource("dehydrate")).setDamageBypassesArmor();
	public static EnviroDamageSource landslide = (EnviroDamageSource)(new EnviroDamageSource("landslide"));
	public static EnviroDamageSource avalanche = (EnviroDamageSource)(new EnviroDamageSource("avalanche"));
	public static EnviroDamageSource gasfire = (EnviroDamageSource)(new EnviroDamageSource("gasfire")).setDamageBypassesArmor();
	public static EnviroDamageSource thething = (EnviroDamageSource)(new EnviroDamageSource("thething")).setDamageBypassesArmor();
	
	protected EnviroDamageSource(String par1Str)
	{
		super(par1Str);
	}
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase par1EntityLivingBase)
	{
		if (!this.damageType.equals("thething"))
		{
			return new TextComponentTranslation("deathmessage.enviromine."+this.damageType, par1EntityLivingBase.getName());
		}
		return new TextComponentString("");
	}
}
