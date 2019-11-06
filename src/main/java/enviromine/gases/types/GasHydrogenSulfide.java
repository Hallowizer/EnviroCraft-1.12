package enviromine.gases.types;

import java.awt.Color;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import enviromine.gases.EnviroGas;
import enviromine.handlers.ObjectHandler;

public class GasHydrogenSulfide extends EnviroGas
{
	public GasHydrogenSulfide(String name, int ID)
	{
		super(name, ID);
		this.setColor(new Color(255, 255, 0, 64));
		this.setDensity(1F);
		this.setVolitility(100F, 0F, 1F);
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
