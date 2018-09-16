package com.ascending.games.id1.edit.board

import com.ascending.games.id1.edit.board.action.content.HeroActionProvider
import com.ascending.games.lib.edit.action.ITimedAction
import com.ascending.games.id1.edit.board.action.room.DropAction
import com.ascending.games.id1.edit.board.action.room.IBoardAction
import com.ascending.games.id1.model.board.*
import com.ascending.games.id1.model.mechanics.StatService
import com.ascending.games.id1.model.world.Player
import com.ascending.games.lib.edit.action.ITimedActionProvider
import com.ascending.games.lib.model.geometry.Coord2
import com.badlogic.gdx.math.Vector2
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class BoardDomain(val board: Board, player : Player, private val roomFactory : IRoomFactory) {

    val onProjectedRoomChanged = HashSet<() -> Unit>()
    val onBoardFinished = HashSet<(Boolean) -> Unit>()

    companion object {
        const val COUNT_WAITING_ROOMS = 3
    }

    var waitingRooms = List(COUNT_WAITING_ROOMS) { roomFactory.createRoom() }
    var currentRoom by Delegates.notNull<Room>()
    var projectedRoom by Delegates.notNull<Room>()

    private var time = 0f
    private val heroActionProvider = HeroActionProvider(board)
    private val mapRoomContentToActionList = mutableMapOf<ARoomContent, ITimedAction>()
    private val statService = StatService()

    init {
        player.stats.forEach { statType, value -> board.hero.stats.put(statType, value) }
        nextRoom()
    }

    fun update(time : Float) {
        updateFallingRooms(time)

        for (row in 0 until board.height) {
            clearRowIfFull(row)
        }

        updateRoomContentActions(time)

        if (board.hero.spawned) {
            if (statService.isDead(board.hero)) {
                onBoardFinished.forEach { it.invoke(false) }
            } else if (board.hero.roomElement.roomContents.filter { it is StairsDown }.isNotEmpty()) {
                onBoardFinished.forEach { it.invoke(true) }
            }
        }
    }

    private fun updateFallingRooms(time : Float) {
        var positionChanged = false
        for (room in board.rooms) {
            if (!board.hasRoomFallen(room)) {
                room.position.y -= time
                positionChanged = true
            } else {
                room.position.y = room.position.y.roundToInt().toFloat()
                board.openWallsNeighbouringDoors(room)
            }
        }

        if (!positionChanged) {
            if (!board.hero.spawned) {
                board.hero.spawn(board)
                heroActionProvider.lastRoom = board.hero.roomElement.room
            }

            nextRoom()
        }
    }

    private fun updateRoomContentActions(time : Float)  {
        val heroAction = getAction(board.hero, heroActionProvider)

        if (heroAction != null && (!heroAction.canExecute || heroAction.execute(time))) {
            mapRoomContentToActionList.remove(board.hero)
        }
    }

    private fun getAction(aRoomContent: ARoomContent, timedActionProvider : ITimedActionProvider) : ITimedAction? {
        val existingAction = mapRoomContentToActionList[aRoomContent]
        if (existingAction != null)  return existingAction

        val newAction = timedActionProvider.getNextActions()
        if (newAction != null) mapRoomContentToActionList.put(board.hero, newAction)
        return newAction
    }

    fun execute(action : IBoardAction) {
        action.execute(currentRoom,board)
        updateProjectedRoom()
    }

    private fun getClearedElements(row : Int) : List<RoomElement> {
        var clearedElements = emptyList<RoomElement>()

        for (x in 0 until board.width) {
            val roomElement = board.getRoomElementAt(Coord2(x, row))
            roomElement ?: return emptyList()

            if (!roomElement.room.isCleared) return emptyList()
            if (board.hero.spawned && board.hero.roomElement == roomElement) return emptyList()
            if (!board.hasRoomFallen(roomElement.room)) return emptyList()

            clearedElements += roomElement
        }

        return clearedElements
    }

    fun clearRowIfFull(row : Int) : Boolean {
        val clearedElements = getClearedElements(row)
        for (roomElement in clearedElements) {
            val room = roomElement.room
            room.roomElements -= roomElement
            if (room.roomElements.isEmpty()) {
                board.rooms.remove(room)
            }
        }

        return !clearedElements.isEmpty()
    }

    fun nextRoom() {
        currentRoom = waitingRooms[0]
        currentRoom.position = Vector2(((board.width - 1) / 2).toFloat(), board.height.toFloat() - 1)
        waitingRooms = waitingRooms.drop(1) + roomFactory.createRoom()
        board.rooms.add(currentRoom)
        updateProjectedRoom()

        if (board.isRoomOverlapping(currentRoom)) {
            onBoardFinished.forEach { it.invoke(false) }
        }
    }

    private fun updateProjectedRoom() {
        val positionY = currentRoom.position.y
        DropAction().execute(currentRoom, board)
        projectedRoom = Room(currentRoom.roomElements.map { it -> it.copy() }.toMutableList(), currentRoom.position.cpy())
        currentRoom.position.y = positionY
        onProjectedRoomChanged.forEach { it.invoke() }
    }
}