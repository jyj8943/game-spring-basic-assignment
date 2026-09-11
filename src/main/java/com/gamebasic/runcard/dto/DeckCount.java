package com.gamebasic.runcard.dto;

import lombok.Getter;

@Getter
public class DeckCount {
    private Long gameId;
    private Long deckSize;

    public DeckCount(Long gameId, Long deckSize) {
        this.gameId = gameId;
        this.deckSize = deckSize;
    }
}
