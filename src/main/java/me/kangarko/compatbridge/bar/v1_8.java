package me.kangarko.compatbridge.bar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;

import me.kangarko.compatbridge.utils.ReflectionUtil;

/**
 * Represents a fake dragon entity for Minecraft 1.8.x
 */
class v1_8 extends BarDragonEntity {
	private Object dragon;
	private int id;

	public v1_8(String name, Location loc) {
		super(name, loc);
	}

	@Override
	public Object getSpawnPacket() {
		final Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
		final Class<?> EntityLiving = ReflectionUtil.getNMSClass("EntityLiving");
		final Class<?> EntityEnderDragon = ReflectionUtil.getNMSClass("EntityEnderDragon");
		Object packet = null;

		try {
			dragon = EntityEnderDragon.getConstructor(ReflectionUtil.getNMSClass("World")).newInstance(getWorld());

			final Method setLocation = ReflectionUtil.getMethod(EntityEnderDragon, "setLocation", new Class<?>[] { double.class, double.class, double.class, float.class, float.class });
			setLocation.invoke(dragon, getX(), getY(), getZ(), getPitch(), getYaw());

			final Method setInvisible = ReflectionUtil.getMethod(EntityEnderDragon, "setInvisible", new Class<?>[] { boolean.class });
			setInvisible.invoke(dragon, true);

			final Method setCustomName = ReflectionUtil.getMethod(EntityEnderDragon, "setCustomName", new Class<?>[] { String.class });
			setCustomName.invoke(dragon, name);

			final Method setHealth = ReflectionUtil.getMethod(EntityEnderDragon, "setHealth", new Class<?>[] { float.class });
			setHealth.invoke(dragon, health);

			final Field motX = ReflectionUtil.getDeclaredField(Entity, "motX");
			motX.set(dragon, getXvel());

			final Field motY = ReflectionUtil.getDeclaredField(Entity, "motY");
			motY.set(dragon, getYvel());

			final Field motZ = ReflectionUtil.getDeclaredField(Entity, "motZ");
			motZ.set(dragon, getZvel());

			final Method getId = ReflectionUtil.getMethod(EntityEnderDragon, "getId", new Class<?>[] {});
			this.id = (Integer) getId.invoke(dragon);

			final Class<?> PacketPlayOutSpawnEntityLiving = ReflectionUtil.getNMSClass("PacketPlayOutSpawnEntityLiving");

			packet = PacketPlayOutSpawnEntityLiving.getConstructor(new Class<?>[] { EntityLiving }).newInstance(dragon);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getDestroyPacket() {
		final Class<?> PacketPlayOutEntityDestroy = ReflectionUtil.getNMSClass("PacketPlayOutEntityDestroy");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityDestroy.newInstance();
			final Field a = PacketPlayOutEntityDestroy.getDeclaredField("a");
			a.setAccessible(true);
			a.set(packet, new int[] { id });
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getMetaPacket(Object watcher) {
		final Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");

		final Class<?> PacketPlayOutEntityMetadata = ReflectionUtil.getNMSClass("PacketPlayOutEntityMetadata");

		Object packet = null;
		try {
			packet = PacketPlayOutEntityMetadata.getConstructor(new Class<?>[] { int.class, DataWatcher, boolean.class }).newInstance(id, watcher, true);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getTeleportPacket(Location loc) {
		final Class<?> PacketPlayOutEntityTeleport = ReflectionUtil.getNMSClass("PacketPlayOutEntityTeleport");
		Object packet = null;

		try {
			packet = PacketPlayOutEntityTeleport.getConstructor(new Class<?>[] { int.class, int.class, int.class, int.class, byte.class, byte.class, boolean.class }).newInstance(this.id, loc.getBlockX() * 32, loc.getBlockY() * 32, loc.getBlockZ() * 32, (byte) ((int) loc.getYaw() * 256 / 360), (byte) ((int) loc.getPitch() * 256 / 360), false);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return packet;
	}

	@Override
	public Object getWatcher() {
		final Class<?> Entity = ReflectionUtil.getNMSClass("Entity");
		final Class<?> DataWatcher = ReflectionUtil.getNMSClass("DataWatcher");

		Object watcher = null;
		try {
			watcher = DataWatcher.getConstructor(new Class<?>[] { Entity }).newInstance(dragon);
			final Method a = ReflectionUtil.getMethod(DataWatcher, "a", new Class<?>[] { int.class, Object.class });

			a.invoke(watcher, 5, isVisible() ? (byte) 0 : (byte) 0x20);
			a.invoke(watcher, 6, health);
			a.invoke(watcher, 7, 0);
			a.invoke(watcher, 8, (byte) 0);
			a.invoke(watcher, 10, name);
			a.invoke(watcher, 11, (byte) 1);
		} catch (final ReflectiveOperationException e) {
			e.printStackTrace();
		}

		return watcher;
	}
}