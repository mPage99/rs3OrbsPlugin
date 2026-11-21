package com.rs3orbs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class Rs3OrbsOverlay extends Overlay
{
	private static final int ORB_SIZE = 54;
	private static final int ORB_ICON_SIZE = 34;
	private static final int DROP_ICON_SIZE = 18;
	private static final int DROP_ROW_HEIGHT = 24;
	private static final int DROP_HORIZONTAL_PADDING = 7;
	private static final int TOP_PADDING = 10;
	private static final long ORB_IDLE_NANOS = 20_000_000_000L;
	private static final long DROP_VISIBLE_NANOS = 2_000_000_000L;
	private static final long FADE_NANOS = 500_000_000L;
	private static final Color RING_COLOR = new Color(255, 190, 60);
	private static final Color RING_BACKGROUND = new Color(30, 30, 34, 220);
	private static final Color ORB_BACKGROUND = new Color(16, 16, 20, 220);
	private static final Color DROP_BACKGROUND = new Color(16, 16, 20, 185);

	private final SkillIconManager skillIconManager;
	private final List<XpDrop> drops = new ArrayList<>();

	private Skill currentSkill;
	private BufferedImage currentSkillIcon;
	private int currentXp;
	private long lastXpAtNanos;

	@Inject
	public Rs3OrbsOverlay(SkillIconManager skillIconManager)
	{
		this.skillIconManager = skillIconManager;
		setPosition(OverlayPosition.TOP_CENTER);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(false);
		setSnappable(false);
		setResettable(false);
	}

	void recordXpGain(Skill skill, int gainedXp, int xp)
	{
		long now = System.nanoTime();
		currentSkill = skill;
		currentSkillIcon = skillIconManager.getSkillImage(skill);
		currentXp = xp;
		lastXpAtNanos = now;

		BufferedImage smallIcon = skillIconManager.getSkillImage(skill, true);
		if (smallIcon == null)
		{
			smallIcon = currentSkillIcon;
		}
		drops.add(new XpDrop(smallIcon, formatXp(gainedXp), now));
	}

	void clearState()
	{
		currentSkill = null;
		currentSkillIcon = null;
		currentXp = 0;
		lastXpAtNanos = 0;
		drops.clear();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long now = System.nanoTime();
		pruneExpiredDrops(now);

		boolean drawOrb = currentSkill != null && now - lastXpAtNanos < ORB_IDLE_NANOS + FADE_NANOS;
		if (!drawOrb && drops.isEmpty())
		{
			return null;
		}

		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fontMetrics = graphics.getFontMetrics();
		int width = ORB_SIZE;
		for (XpDrop drop : drops)
		{
			width = Math.max(width, DROP_ICON_SIZE + DROP_HORIZONTAL_PADDING * 2 + 4 + fontMetrics.stringWidth(drop.amount));
		}

		int y = TOP_PADDING;
		int height = TOP_PADDING;
		if (drawOrb)
		{
			float alpha = fadeAlpha(now - lastXpAtNanos, ORB_IDLE_NANOS);
			drawOrb(graphics, (width - ORB_SIZE) / 2, y, alpha);
			y += ORB_SIZE + 6;
			height += ORB_SIZE + 6;
		}

		for (int i = drops.size() - 1; i >= 0; i--)
		{
			XpDrop drop = drops.get(i);
			float alpha = fadeAlpha(now - drop.createdAtNanos, DROP_VISIBLE_NANOS);
			drawDrop(graphics, drop, (width - DROP_ICON_SIZE - DROP_HORIZONTAL_PADDING * 2 - 4 - fontMetrics.stringWidth(drop.amount)) / 2,
				y, fontMetrics, alpha);
			y += DROP_ROW_HEIGHT;
			height += DROP_ROW_HEIGHT;
		}

		return new Dimension(width, height);
	}

	private void drawOrb(Graphics2D graphics, int x, int y, float alpha)
	{
		Composite originalComposite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		graphics.setColor(ORB_BACKGROUND);
		graphics.fillOval(x + 4, y + 4, ORB_SIZE - 8, ORB_SIZE - 8);

		graphics.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.setColor(RING_BACKGROUND);
		graphics.drawArc(x + 2, y + 2, ORB_SIZE - 4, ORB_SIZE - 4, 90, -360);
		double progress = getProgress();
		if (progress > 0.0)
		{
			graphics.setColor(RING_COLOR);
			graphics.drawArc(x + 2, y + 2, ORB_SIZE - 4, ORB_SIZE - 4, 90, -(int) Math.round(progress * 360));
		}

		if (currentSkillIcon != null)
		{
			int iconX = x + (ORB_SIZE - ORB_ICON_SIZE) / 2;
			int iconY = y + (ORB_SIZE - ORB_ICON_SIZE) / 2;
			graphics.drawImage(currentSkillIcon, iconX, iconY, ORB_ICON_SIZE, ORB_ICON_SIZE, null);
		}

		graphics.setComposite(originalComposite);
	}

	private void drawDrop(Graphics2D graphics, XpDrop drop, int x, int y, FontMetrics fontMetrics, float alpha)
	{
		Composite originalComposite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

		int textWidth = fontMetrics.stringWidth(drop.amount);
		int width = DROP_ICON_SIZE + DROP_HORIZONTAL_PADDING * 2 + 4 + textWidth;
		graphics.setColor(DROP_BACKGROUND);
		graphics.fillRoundRect(x, y, width, DROP_ROW_HEIGHT - 2, 9, 9);

		if (drop.icon != null)
		{
			graphics.drawImage(drop.icon, x + DROP_HORIZONTAL_PADDING, y + 2, DROP_ICON_SIZE, DROP_ICON_SIZE, null);
		}

		int textX = x + DROP_HORIZONTAL_PADDING + DROP_ICON_SIZE + 4;
		int textY = y + (DROP_ROW_HEIGHT - 2 - fontMetrics.getHeight()) / 2 + fontMetrics.getAscent();
		graphics.setColor(new Color(20, 16, 8));
		graphics.drawString(drop.amount, textX + 1, textY + 1);
		graphics.setColor(new Color(255, 232, 163));
		graphics.drawString(drop.amount, textX, textY);

		graphics.setComposite(originalComposite);
	}

	private double getProgress()
	{
		if (currentXp >= Experience.MAX_SKILL_XP)
		{
			return 1.0;
		}

		int level = Experience.getLevelForXp(currentXp);
		if (level >= Experience.MAX_VIRT_LEVEL)
		{
			return 1.0;
		}

		int xpForCurrentLevel = Experience.getXpForLevel(level);
		int xpForNextLevel = Experience.getXpForLevel(level + 1);
		return Math.max(0.0, Math.min(1.0,
			(double) (currentXp - xpForCurrentLevel) / (xpForNextLevel - xpForCurrentLevel)));
	}

	private void pruneExpiredDrops(long now)
	{
		Iterator<XpDrop> iterator = drops.iterator();
		while (iterator.hasNext())
		{
			if (now - iterator.next().createdAtNanos >= DROP_VISIBLE_NANOS + FADE_NANOS)
			{
				iterator.remove();
			}
		}
	}

	private static float fadeAlpha(long elapsedNanos, long visibleNanos)
	{
		if (elapsedNanos <= visibleNanos)
		{
			return 1.0f;
		}
		return Math.max(0.0f, 1.0f - (float) (elapsedNanos - visibleNanos) / FADE_NANOS);
	}

	private static String formatXp(int xp)
	{
		return "+" + NumberFormat.getIntegerInstance(Locale.US).format(xp);
	}

	private static final class XpDrop
	{
		private final BufferedImage icon;
		private final String amount;
		private final long createdAtNanos;

		private XpDrop(BufferedImage icon, String amount, long createdAtNanos)
		{
			this.icon = icon;
			this.amount = amount;
			this.createdAtNanos = createdAtNanos;
		}
	}
}
