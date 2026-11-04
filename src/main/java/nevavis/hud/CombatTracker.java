package nevavis.hud;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;

/** Запоминает последнюю цель удара, комбо и дистанцию (reach) последнего удара. */
public final class CombatTracker {
	private static LivingEntity target;
	private static long lastHit;
	private static int combo;
	private static double reach;
	private static int kills, hits, crits, swings, serial;
	private static boolean lastCrit, lastSwing;
	private static boolean counted;

	private CombatTracker() {
	}

	public static void init() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
			if (world.isClient && mc.player != null && player == mc.player && entity instanceof LivingEntity le) {
				long now = System.currentTimeMillis();
				combo = (le == target && now - lastHit < 3000) ? combo + 1 : 1;
				if (le != target) {
					counted = false;
				}
				target = le;
				lastHit = now;
				reach = player.getEyePos().distanceTo(le.getBoundingBox().getCenter());
				hits++;
				serial++;
				lastCrit = player.fallDistance > 0f && !player.isOnGround() && !player.isClimbing()
						&& !player.isTouchingWater() && !player.hasVehicle()
						&& !player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS);
				if (lastCrit) {
					crits++;
				}
			}
			return ActionResult.PASS;
		});
	}

	public static void tick(MinecraftClient mc) {
		if (mc.player != null) {
			boolean sw = mc.player.handSwinging;
			if (sw && !lastSwing) {
				swings++;
			}
			lastSwing = sw;
		}
		if (mc.player != null && mc.player.hurtTime == mc.player.maxHurtTime - 1 && mc.player.hurtTime > 0) {
			combo = 0;
		}
		if (target != null && !counted && target.getHealth() <= 0f) {
			kills++;
			counted = true;
		}
		if (target != null && (target.isRemoved() || System.currentTimeMillis() - lastHit > 6000)) {
			target = null;
		}
	}

	public static LivingEntity target() {
		return target;
	}

	public static int hits() {
		return hits;
	}

	public static int crits() {
		return crits;
	}

	public static int accuracy() {
		return swings <= 0 ? 0 : Math.min(100, Math.round(100f * hits / swings));
	}

	/** Растёт с каждым ударом по существу. */
	public static int serial() {
		return serial;
	}

	public static boolean lastCrit() {
		return lastCrit;
	}

	public static long lastHitAt() {
		return lastHit;
	}

	public static int kills() {
		return kills;
	}

	public static int combo() {
		return System.currentTimeMillis() - lastHit < 3000 ? combo : 0;
	}

	public static double reach() {
		return reach;
	}
}
