package elec332.core.effects.defaultabilities;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import elec332.core.effects.api.ability.Ability;
import elec332.core.effects.api.ability.WrappedAbility;
import elec332.core.effects.api.util.AbilityHelper;
import elec332.core.util.PlayerHelper;
import elec332.core.util.EventHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by Elec332 on 27-9-2015.
 */
public class Flight extends Ability {

    public Flight() {
        super("flight");
        EventHelper.registerHandlerFML(this);
    }

    @Override
    public void onEffectAddedToEntity(EntityLivingBase entity, WrappedAbility activeEffect) {
        if (entity instanceof EntityPlayer)
            PlayerHelper.activateFlight((EntityPlayer) entity);
    }

    @Override
    public void updateEffectOnEntity(EntityLivingBase entity, WrappedAbility activeEffect) {
        if (entity instanceof EntityPlayer && !((EntityPlayer)entity).capabilities.allowFlying)
            PlayerHelper.activateFlight((EntityPlayer) entity);
    }

    @Override
    public void onEffectRemovedFromEntity(EntityLivingBase entity, WrappedAbility activeEffect) {
        if (entity instanceof EntityPlayer)
            PlayerHelper.deactivateFlight((EntityPlayer) entity);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        if (AbilityHelper.isEffectActive(event.player, this)){
            event.player.capabilities.isFlying = true;
            event.player.sendPlayerAbilities();
        }
    }
}
