package net.p3pp3rf1y.sophisticatedcore.util;

import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.fluid.FluidUtil;

public class XpHelper {
	private XpHelper() {}

	private static final long RATIO = 20 * FluidUtil.BUCKET_VOLUME_IN_MILLIBUCKETS;

	/** Upper bound for the level lookup; the quadratic still fits a long there. */
	private static final int MAX_SEARCHED_LEVEL = 50_000_000;

	public static float liquidToExperience(long liquid) {
		return (float) liquid / RATIO;
	}

	/** Whole experience points for a liquid amount without detouring through float, which loses precision above 2^24. */
	public static long liquidToExperiencePoints(long liquid) {
		return liquid / RATIO;
	}

	public static long experienceToLiquid(float xp) {
		return (long) (xp * RATIO);
	}

	public static long experienceToLiquid(long xp) {
		return xp * RATIO;
	}

	/** Experience needed to reach a level. In long math, because as int this wraps from level 21846 on. */
	public static long getExperienceForLevel(int level) {
		if (level <= 0) {
			return 0;
		}
		if (level < 16) {
			return level * (12L + level * 2L) / 2L;
		}
		if (level < 31) {
			long l = level - 15L;
			return l * (69L + l * 5L) / 2L + 315L;
		}
		long l = level - 30L;
		return l * (215L + l * 9L) / 2L + 1395L;
	}

	public static long getExperienceLimitOnLevel(int level) {
		if (level >= 30) {
			return 112L + (level - 30L) * 9L;
		} else if (level >= 15) {
			return 37L + (level - 15L) * 5L;
		} else if (level > 0) {
			return 7L + level * 2L;
		}
		return 7L;
	}

	/** Binary search instead of stepping level by level, which never returns once the target is unreachable. */
	public static int getLevelForExperience(long experience) {
		if (experience <= 0) {
			return 0;
		}
		if (experience >= getExperienceForLevel(MAX_SEARCHED_LEVEL)) {
			return MAX_SEARCHED_LEVEL;
		}
		int low = 0;
		int high = MAX_SEARCHED_LEVEL;
		while (low < high) {
			int mid = (low + high + 1) >>> 1;
			if (getExperienceForLevel(mid) <= experience) {
				low = mid;
			} else {
				high = mid - 1;
			}
		}
		return low;
	}

	public static double getLevelsForExperience(long experience) {
		int baseLevel = getLevelForExperience(experience);
		long remainingExperience = experience - getExperienceForLevel(baseLevel);
		return baseLevel + ((double) remainingExperience / getExperienceLimitOnLevel(baseLevel));
	}

	public static long getPlayerTotalExperience(Player player) {
		long currentLevelPoints = getExperienceForLevel(player.experienceLevel);
		long partialLevelPoints = (long) (player.experienceProgress * player.getXpNeededForNextLevel());
		return currentLevelPoints + partialLevelPoints;
	}
}
