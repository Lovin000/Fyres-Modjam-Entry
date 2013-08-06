package assets.fyresmodjam;

import java.util.ArrayList;
import java.util.EnumSet;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class CommonTickHandler implements ITickHandler {
	public static FyresWorldData worldData = null;
	
	public static ArrayList<Entity> addLater = new ArrayList<Entity>();

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if(type.equals(EnumSet.of(TickType.WORLDLOAD))){
			for(int i = 0; i < tickData.length; i++) {
				if(tickData[i] instanceof World && ((World) tickData[i]).provider.dimensionId == 0) {
					worldData = FyresWorldData.forWorld((World) tickData[i]);
					worldData.markDirty();
				}
			}
		} else if(type.equals(EnumSet.of(TickType.SERVER))) {
			serverTick();
		}
		
	}
	
	public void serverTick() {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		
		for(int i = 0; i < server.worldServers.length; i++) {
			WorldServer s = FMLCommonHandler.instance().getMinecraftServerInstance().worldServers[i];
			
			if(s == null) {continue;}
			
			for(Object o : s.playerEntities) {
				if(o == null || !(o instanceof EntityPlayer)) {continue;}
				
				EntityPlayer player = (EntityPlayer) o;
				
				if(player.getEntityData().hasKey("Blessing")) {
					if(player.isSneaking() && player.getEntityData().getString("Blessing").equals("Ninja")) {
						PotionEffect e = player.getActivePotionEffect(Potion.invisibility);
						if(e == null || player.getActivePotionEffect(Potion.invisibility).getDuration() < 10) {player.addPotionEffect(new PotionEffect(Potion.invisibility.id, 10, 1, false));}
					}/* else if(player.getEntityData().getString("Blessing").equals("Healer")) {
						if(player.worldObj.getGameRules().getGameRuleBooleanValue("naturalRegeneration") && ((player.worldObj.difficultySetting == 0 && player.func_110143_aJ() < player.func_110138_aP() && player.ticksExisted % 20 * 12 == 0) || (player.getFoodStats().getFoodLevel() >= 18 && player.getEntityData().getInteger("foodTickTimer") >= 80 && player.shouldHeal()))) {
							player.heal(1.0F);
							System.out.println(true);
				        }
					}*/
				}
			}
		}
		
		for(Entity e : addLater) {
			WorldServer world = null;
			for(WorldServer s : MinecraftServer.getServer().worldServers) {if(s.provider.dimensionId == e.dimension) {world = s; break;}}
			if(world != null) {world.spawnEntityInWorld(e);}
		}
		addLater.clear();
		
		if(worldData != null) {
			if(worldData.currentDisadvantage.equals("Neverending Rain")) {
				if(!MinecraftServer.getServer().worldServers[0].getWorldInfo().isRaining()) {MinecraftServer.getServer().worldServers[0].getWorldInfo().setRaining(true);}
				if(!MinecraftServer.getServer().worldServers[0].getWorldInfo().isThundering()) {MinecraftServer.getServer().worldServers[0].getWorldInfo().setThundering(true);}
			} else if(worldData.currentDisadvantage.equals("Neverending Night")) {
				MinecraftServer.getServer().worldServers[0].getWorldInfo().setWorldTime(18000);
			}
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet ticks() {
        return EnumSet.of(TickType.SERVER, TickType.WORLDLOAD);
    }

	@Override
	public String getLabel() {
		return "FyresModJamCommonTicker";
	}
}