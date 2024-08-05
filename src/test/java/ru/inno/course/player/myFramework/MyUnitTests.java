package ru.inno.course.player.myFramework;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.inno.course.player.model.Player;
import ru.inno.course.player.service.PlayerService;
import ru.inno.course.player.service.PlayerServiceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.NoSuchElementException;


import static org.junit.jupiter.api.Assertions.*;

public class MyUnitTests {
    private PlayerService service;
    private static final String NICKNAME = "Nikita";


    @BeforeEach
    public void setUp() {
        service = new PlayerServiceImpl();
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(Path.of("./data.json"));
    }

    // Позитивные тесты
    @Test
    @DisplayName("Добавить игрока и проверить его наличие в списке")
    public void testAddPlayerAndCheckInList() {
        int playerId = service.createPlayer(NICKNAME);
        Collection<Player> players = service.getPlayers();
        assertTrue(players.stream().anyMatch(player -> player.getId() == playerId));
        assertEquals(1, players.size());
    }
    @Test
    @DisplayName("Добавить и удалить игрока, проверить отсутствие в списке")
    public void testAddAndRemovePlayer() {
        int playerId = service.createPlayer(NICKNAME);
        service.deletePlayer(playerId);
        Collection<Player> players = service.getPlayers();
        assertFalse(players.stream().anyMatch(player -> player.getId() == playerId));
    }


    @Test
    @DisplayName("Добавить игрока, когда JSON-файл существует")
    public void testAddPlayerWhenJsonFileExists() {
        int existingPlayerId = service.createPlayer("ExistingPlayer");
        int newPlayerId = service.createPlayer(NICKNAME);
        Player player = service.getPlayerById(newPlayerId);
        assertNotNull(player);
    }

    @Test
    @DisplayName("Добавить очков игроку")
    public void testAddPointsToExistingPlayer() {
        int playerId = service.createPlayer(NICKNAME);
        service.addPoints(playerId, 50);
        Player player = service.getPlayerById(playerId);
        assertEquals(50, player.getPoints());
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 100, -50, 0, 100, -5000000})
    @DisplayName("Добавление очков игроку (параметризованный)")
    public void testAddPointsParametrized(int points) {
        int playerId = service.createPlayer(NICKNAME);
        service.addPoints(playerId, points);
        Player playerById = service.getPlayerById(playerId);
        assertEquals(points, playerById.getPoints());
    }

    @Test
    @DisplayName("Добавить очков поверх существующих")
    public void testAddPointsToExistingPoints() {
        int playerId = service.createPlayer(NICKNAME);
        service.addPoints(playerId, 50);
        service.addPoints(playerId, 35);
        Player player = service.getPlayerById(playerId);
        assertEquals(85, player.getPoints());
    }

    @Test
    @DisplayName("Добавить игрока и получить его по ID")
    public void testAddPlayerAndGetById() {
        int playerId = service.createPlayer(NICKNAME);
        Player player = service.getPlayerById(playerId);
        assertEquals(playerId, player.getId());
        assertEquals(NICKNAME, player.getNick());
    }

    @Test
    @DisplayName("Проверить уникальность ID игроков")
    public void testUniquePlayerIds() {
        int id1 = service.createPlayer("Player1");
        int id2 = service.createPlayer("Player2");
        int id3 = service.createPlayer("Player3");
        service.deletePlayer(id3);
        int id4 = service.createPlayer("Player4");
        assertNotEquals(id3, id4);
    }

    @Test
    @DisplayName("Добавить игрока, когда JSON-файла нет")
    public void testAddPlayerWhenJsonFileDoesNotExist() throws IOException {
        tearDown();
        setUp();
        int playerId = service.createPlayer(NICKNAME);
        Player player = service.getPlayerById(playerId);
        assertNotNull(player);
    }


    @Test
    @DisplayName("Нет json-файла запросить список игроков")
    public void testPlayerListWithNoJson() {
        Collection<Player> players = service.getPlayers();
        assertEquals(0, players.size());
    }


    @Test
    @DisplayName("Проверить создание игрока с 15 символами")
    public void testCreatePlayerWith15Chars() {
        String longNickname = "abcdefghijklmno";
        int playerId = service.createPlayer(longNickname);
        Player player = service.getPlayerById(playerId);
        assertEquals(longNickname, player.getNick());
    }

    // Негативные тесты


    @Test
    @DisplayName("Удалить игрока, которого нет")
    public void testDeleteNonExistentPlayer() {
        assertThrows(NoSuchElementException.class, () -> service.deletePlayer(10));
    }

    @Test
    @DisplayName("Создать дубликат игрока")
    public void testCreateDuplicatePlayer() {
        service.createPlayer(NICKNAME);
        assertThrows(IllegalArgumentException.class, () -> service.createPlayer(NICKNAME));
    }

    @Test
    @DisplayName("Получить игрока по несуществующему ID")
    public void testGetPlayerByNonExistentId() {
        assertThrows(NoSuchElementException.class, () -> service.getPlayerById(9999));
    }



    // Тест провален, игрок с пустым ником создается
    @Test
    @DisplayName("Сохранить игрока с пустым ником")
    public void testSavePlayerWithEmptyNick() {
        assertThrows(IllegalArgumentException.class, () -> service.createPlayer(""));
    }

    @Test
    @DisplayName("Ввести невалидный ID (String)")
    public void testInvalidIdFormat() {
        assertThrows(NumberFormatException.class, () -> service.getPlayerById(Integer.parseInt("invalid")));
    }

    // Тест провален, отрицательное количество очков начисляется
    @Test
    @DisplayName("Начислить отрицательное число очков")
    public void testAddNegativePoints() {
        int playerId = service.createPlayer(NICKNAME);
        assertThrows(IllegalArgumentException.class, () -> service.addPoints(playerId, -50));
    }

    @Test
    @DisplayName("Начислить очки игроку, которого нет")
    public void testAddPointsToNonExistentPlayer() {
        assertThrows(NoSuchElementException.class, () -> service.addPoints(9999, 50));
    }

    @Test
    @DisplayName("Начислить 1.5 балла игроку")
    public void testAddFractionalPoints() {
        int playerId = service.createPlayer(NICKNAME);
        service.addPoints(playerId, (int)1.5);
        Player player = service.getPlayerById(playerId);
        assertNotEquals(1.5, player.getPoints());
    }
    // Тест провален, игрок с ником из 16 символов создается
    @Test
    @DisplayName("Проверить создание игрока с 16 символами")
    public void testCreatePlayerWith16Chars() {
        String longNickname = "qwertyqwertyqwer";
        assertThrows(IllegalArgumentException.class, () -> service.createPlayer(longNickname));
    }

}
