package com.ascending.games.id1.model.mechanics

import com.ascending.games.lib.model.game.IStats

class StatService {

    companion object {
        const val BASED_GOLD_PER_ROOM_ELEMENT = 1
    }

    fun isDead(stats : IStats) : Boolean {
        return (stats.stats.get(StatType.CURRENT_HP.name) ?: 0f) <= 0f
    }

    fun applyDamage(attacker : IStats, defender : IStats) {
        val attack = attacker.stats[StatType.ATTACK.name] ?: 0f
        val defense = defender.stats[StatType.DEFENSE.name] ?: 0f
        val damage = Math.max(0f, attack - defense)
        defender.stats[StatType.CURRENT_HP.name] = Math.max(0f, (defender.stats[StatType.CURRENT_HP.name] ?: 0f) - damage)
    }

    fun reward(killer : IStats, killed : IStats) {
        killer.stats[StatType.EXP.name] = (killer.stats[StatType.EXP.name] ?: 0f) + (killed.stats[StatType.LEVEL.name] ?: 1f)

        if (hasLevelUp(killer)) {
            levelUp(killer)
        }
    }

    fun rewardRoomElementClear(stats : IStats) {
        stats.stats[StatType.GOLD.name] = (stats.stats[StatType.GOLD.name] ?: 0f) + BASED_GOLD_PER_ROOM_ELEMENT
    }

    fun levelUp(stats : IStats) {
        val level = (stats.stats[StatType.LEVEL.name] ?: 1f).toInt()
        if (level % 10 == 0) {
            stats.stats[StatType.DEFENSE.name] = (stats.stats[StatType.DEFENSE.name] ?: 0f) + 1f
        } else if (level % 5 == 0) {
            stats.stats[StatType.ATTACK.name] = (stats.stats[StatType.ATTACK.name] ?: 0f) + 1f
        } else {
            stats.stats[StatType.MAX_HP.name] = (stats.stats[StatType.MAX_HP.name] ?: 0f) + 1f
        }

        stats.stats[StatType.EXP.name] = (stats.stats[StatType.EXP.name] ?: 0f) - getNextExp(stats.stats)
        stats.stats[StatType.LEVEL.name] = (stats.stats[StatType.LEVEL.name] ?: 1f) + 1
    }

    fun hasLevelUp(stats : IStats) : Boolean {
        return stats.stats[StatType.EXP.name] ?: 0f >= getNextExp(stats.stats)
    }

    fun getNextExp(stats : Map<String, Float>) : Float {
        return ((stats[StatType.LEVEL.name] ?: 1f) + 1).let { it * it }
    }
}