package com.gamebasic.runcard.repository;

import com.gamebasic.game.entity.Game;
import com.gamebasic.runcard.dto.DeckCount;
import com.gamebasic.runcard.entity.RunCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RunCardRepository extends JpaRepository<RunCard, Long> {
    List<RunCard> findAllByGameOrderByIdAsc(Game game);

    void deleteAllByGame(Game game);

    // TODO (Lv 11): @Query 작성
    @Query("""
        select new com.gamebasic.runcard.dto.DeckCount(
            r.game.id,
            count(r)
        )
        from RunCard r
        where r.game in :games
        group by r.game.id
        """)
    List<DeckCount> countByGames(List<Game> games);
}
