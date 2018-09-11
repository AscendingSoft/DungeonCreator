package com.ascending.games.id1.edit.board.action.content

import com.ascending.games.id1.edit.board.BoardDomain
import com.ascending.games.id1.edit.board.action.content.IRoomContentAction
import com.ascending.games.id1.model.board.ARoomContent

interface IRoomContentActionProvider {
    fun getNextActions(board : BoardDomain) : List<IRoomContentAction>
}