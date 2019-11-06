package enviromine.gases.types;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import enviromine.gases.EnviroGas;
import enviromine.handlers.ObjectHandler;
import java.awt.Color;

public class GasSulfurDioxide extends EnviroGas
{
	public GasSulfurDioxide(String name, int ID)
	{
		super(name, ID);
		this.setColor(new Color(192, 192, 0, 0));
		this.setDensity(3F);
		this.setSuffocation(0.01F);
	}
	
	@Override
	public void applyEffects(EntityLivingBase entityLiving, int amplifier)
	{
		super.applyEffects(entityLiving, amplifier);
		
		if(entityLiving.world.isRemote || entityLiving.isEntityUndead() || (entityLiving.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null && entityLiving.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == ObjectHandler.gasMask))
		{
			return;
		}
		
		if(amplifier >= 5 && entityLiving.getRNG().nextInt(100) == 0)
		{
			if(amplifier >= 10)
			{
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 600));
			} else
			{
				entityLiving.addPotionEffect(new PotionEffect(MobEffects.POISON, 200));
			}
		}
	}
}
