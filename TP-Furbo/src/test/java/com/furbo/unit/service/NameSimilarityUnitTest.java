package com.furbo.unit.service;

import com.furbo.service.matching.NameSimilarity;
import com.furbo.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class NameSimilarityUnitTest {

    @ParameterizedTest
    @CsvSource({
        "Álvaro Valles, Álvaro Vallés Rosa, CONTAINED",
        "Antonio Sivera, Sivera, CONTAINED",
        "Odysseas Vlachodimos, Odisseas Vlachodimos, TYPO",
        "Manu Sánchez, Manuel Sánchez, ABBREVIATION",
        "Pablo Ibáñez, Pablo Ibáñez Tébar, CONTAINED",
        "Javi Guerra, Javier Guerra, ABBREVIATION",
        "Matvey Safonov, Matvei Safonov, TYPO",
        "Enrico Delprato, Enrico Del Prato, TYPO",
        "Alejandro Catena, Catena, CONTAINED",
        "Jorge Salinas, Salinas, CONTAINED",
        "Jon Ander Olasagasti, Jon Olasagasti, CONTAINED",
        "Chupe, Chupete, ABBREVIATION",
        "Andrés Castrín, Castrin, CONTAINED",
        "Mikael Ellertsson, Mikael Egill Ellertsson, CONTAINED",
        "Romano Floriani Mussolini, Romano Floriani, CONTAINED",
        "Nico González, Nicolás González, ABBREVIATION",
        "Mathew Ryan, Maty Ryan, SURNAME_ONLY",
        "Junior Diaz, Michel Diaz, SURNAME_ONLY",
        "Pedro Gonzalez, Pablo Gonzalez, SURNAME_ONLY"
    })
    void testNameSimilarityRules(String wsName, String playerName, String expectedRule) {
        Player p = new Player();
        p.setName(playerName);
        NameSimilarity.Result res = NameSimilarity.evaluate(wsName, null, null, null, p);
        assertThat(res).isNotNull();
        assertThat(res.rule()).isEqualTo(expectedRule);
    }

    @Test
    void testNoMatchAndPositionBonus() {
        Player p = new Player();
        p.setName("Carlos Soler");
        p.setPosition("Midfield");
        NameSimilarity.Result res = NameSimilarity.evaluate("Carlos Martin", null, null, null, p);
        assertThat(res).isNull();

        Player pRafita = new Player();
        pRafita.setName("Rafa Garrido");
        pRafita.setPosition("Midfield");
        NameSimilarity.Result resRafita = NameSimilarity.evaluate("Rafita", null, null, null, pRafita);
        assertThat(resRafita).isNull();

        Player p2 = new Player();
        p2.setName("Álvaro Vallés Rosa");
        p2.setPosition("Goalkeeper");
        NameSimilarity.Result res2 = NameSimilarity.evaluate("Álvaro Valles", null, null, "Goalkeeper", p2);
        assertThat(res2).isNotNull();
        assertThat(res2.score()).isEqualTo(Math.min(1.0, 0.93 + 0.03)); // compatible sums 0.03

        Player pIncompat = new Player();
        pIncompat.setName("Álvaro Vallés Rosa");
        pIncompat.setPosition("Forward");
        NameSimilarity.Result resIncompat = NameSimilarity.evaluate("Álvaro Valles", null, null, "Goalkeeper", pIncompat);
        assertThat(resIncompat).isNotNull();
        assertThat(resIncompat.score()).isEqualTo(0.93); // incompatible does not sum

        Player pNone = new Player();
        pNone.setName("Álvaro Vallés Rosa");
        pNone.setPosition(null);
        NameSimilarity.Result resNone = NameSimilarity.evaluate("Álvaro Valles", null, null, null, pNone);
        assertThat(resNone).isNotNull();
        assertThat(resNone.score()).isEqualTo(0.93); // no position does not sum
    }
}
