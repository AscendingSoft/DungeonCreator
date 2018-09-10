package com.ascending.games.id1.model.board

import com.badlogic.gdx.math.Vector2

class Hero() : ARoomContent() {
    var position = Vector2()
    var spawned = false

    fun spawn(board : Board) {
        spawned = true

        val room = board.rooms[0]
        roomElement = room.roomElements.filter { roomElement -> room.roomElements.all { roomElement.position.y <= it.position.y } }.first()
    }
}