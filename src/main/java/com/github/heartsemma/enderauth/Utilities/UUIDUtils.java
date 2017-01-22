package com.github.heartsemma.enderauth.Utilities;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.api.entity.living.player.Player;

/**
 * <B>UUIDUtils</b> is a class with mostly static methods that perform operations related to UUID's. 
 *
 */
public class UUIDUtils {
	
	/**
	 * @param player The player to get the UUID from.
	 * @return The UUID as a byte array 
	 * 
	 * <br><br> This static function returns the UUID of the given player as a byte array with a length of 16 (equalling 128 bits of data).
	 */
	public static byte[] getUUID(Player player){
		
		UUID uuid = player.getUniqueId();
		
		long frontLong = uuid.getMostSignificantBits();
		long backLong = uuid.getLeastSignificantBits();
		
		byte[] frontOfID = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(frontLong).array();
		byte[] backOfID = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(backLong).array();
		
		byte[] uuidBytes = ArrayUtils.addAll(frontOfID,backOfID);
		
		return uuidBytes;
	}
	
	/**
	 * @param uuid (The UUID to turn into a byte array).
	 * @return The UUID as a byte array 
	 * 
	 * <br><br> This static function converts a UUID into the form of a byte array with a length of 16 (equalling 128 bits of data).
	 */
	public static byte[] getBytes(UUID uuid){
		
		long frontLong = uuid.getMostSignificantBits();
		long backLong = uuid.getLeastSignificantBits();
		
		byte[] frontOfID = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(frontLong).array();
		byte[] backOfID = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(backLong).array();
		
		byte[] uuidBytes = ArrayUtils.addAll(frontOfID,backOfID);
		
		return uuidBytes;
	}
}
