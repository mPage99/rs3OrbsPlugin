# RS3-Inspired XP Orb Prototype

## Goal

Build a local RuneLite plugin that displays one RS3-inspired skill progress orb and temporary XP drops.

## Behavior

- The orb is fixed near the top center of the game viewport and hidden until new XP is gained.
- It shows the icon for the skill that most recently gained XP, surrounded by a ring showing progress through the current level. The ring tracks virtual levels through 126 and stays full at the cap.
- A new skill XP gain replaces the orb's skill and resets its 20-second idle timer.
- Once a skill's XP baseline is established, every positive XP gain creates a separate drop beneath the orb with the skill icon and `+XP` amount. Keep each drop for 2 seconds, then fade it; stack all active drops.
- Hide the orb after 20 seconds without XP, then fade it.
- Treat the first available XP observation for each skill during login as its baseline, not as a drop. Show subsequent positive deltas, including while other skills are still initializing.
- Keep timing, placement, and appearance fixed in v1.

## Verification

Build the plugin. The user will perform live RuneLite checks.

## Out of scope

- Plugin Hub submission and distribution.
- Configurable placement, timing, or appearance.
- Multiple persistent skill orbs.
