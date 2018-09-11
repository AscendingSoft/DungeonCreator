package com.ascending.games.id1.model.board

import com.ascending.games.lib.model.geometry.Coord2
import com.ascending.games.lib.model.geometry.Direction4

class Board(val width : Int, val height : Int) {
    var rooms = emptyList<Room>()

    fun haveRoomsFallen() : Boolean {
        return rooms.all { hasRoomFallen(it) }
    }

    fun hasRoomFallen(room : Room) : Boolean {
        for (roomElement in room.roomElements) {
            if (roomElement.getBoardY() <= 0) {
                return true
            } else {
                val roomCoord = roomElement.getBoardCoord()
                val roomBelow = getRoomAt(Coord2(roomCoord.x, roomCoord.y - 1))
                if (roomBelow != null && roomBelow != room && roomBelow.position.y.toDouble() == Math.ceil(roomBelow.position.y.toDouble())) return true
            }
        }

        return false
    }

    fun getRoomElementsAt(position : Coord2) : List<RoomElement> {
        return rooms.flatMap { it.roomElements.filter { it.getBoardCoord() == position } }
    }

    fun getRoomElementAt(position : Coord2) : RoomElement? {
        return getRoomElementsAt(position).getOrNull(0)
    }

    fun getRoomAt(position : Coord2) : Room? {
        val roomElement = getRoomElementAt(position)
        roomElement ?: return null

        return roomElement.room
    }

    fun isRoomOverlapping(room : Room) : Boolean {
        return !room.roomElements.none { getRoomElementsAt(it.getBoardCoord()).size > 1 }
    }

    fun isRoomInBounds(room : Room) : Boolean {
        return room.roomElements.none { it.getBoardX() < 0 || it.getBoardY() < 0 || it.getBoardX() >= width || it.getBoardY() >= height }
    }

    private fun getClearedElements(row : Int) : List<RoomElement> {
        var clearedElements = emptyList<RoomElement>()

        for (x in 0 until width) {
            val roomElement = getRoomElementAt(Coord2(x, row))
            roomElement ?: return emptyList()

            if (!hasRoomFallen(roomElement.room)) {
                return emptyList()
            }

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
                rooms -= room
            }
        }

        return !clearedElements.isEmpty()
    }

    fun openWallsNeighbouringDoors(room: Room) {
        for (roomElement in room.roomElements) {
            val coord = roomElement.getBoardCoord()
            val wallsToOpen = mutableListOf<Wall>()
            for (wall in roomElement.walls) {
                val coordOther = coord.add(wall.direction.toOffset())
                val roomElementOther = getRoomElementAt(coordOther)
                if (roomElementOther != null) {
                    val wallOther = roomElementOther.walls.find { it.direction == wall.direction.opposite() }
                    if (wallOther != null) {
                        if (wall.wallState == WallState.DOOR || wallOther.wallState == WallState.DOOR) {
                            wallsToOpen.add(wall)
                            wallsToOpen.add(wallOther)
                        }
                    }
                }
            }

            for (wall in wallsToOpen) {
                wall.roomElement.walls -= wall
            }
        }
    }

    fun getNeighbouringRooms(room : Room) : Set<Room> {
        val neighbouringRooms = mutableSetOf<Room>()
        for (roomElement in room.roomElements) {
            for (direction in Direction4.values()) {
                if (roomElement.isOpen(direction)) {
                    val offset = direction.toOffset()
                    val coord = roomElement.getBoardCoord().add(offset)

                    val roomElementOther = getRoomElementAt(coord)
                    if (roomElementOther != null && roomElementOther.isOpen(direction.opposite()) && roomElementOther.room != room) {
                        neighbouringRooms.add(roomElementOther.room)
                    }
                }
            }
        }
        return neighbouringRooms
    }
}