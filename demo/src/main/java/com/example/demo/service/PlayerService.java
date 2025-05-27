package com.example.demo.service;

import com.example.demo.dao.MatchDAO;
import com.example.demo.dao.PlayerDAO;
import com.example.demo.dao.PlayerNodeDAO;
import com.example.demo.dao.TournamentDAO;
import com.example.demo.model.*;
import lombok.Setter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);
    private final PlayerDAO playerDAO;
    private final PlayerNodeDAO playerNodeDAO;
    private final MatchDAO matchDAO;
    private final TournamentDAO tournamentDAO;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final static int EloConstant = 30;

    public PlayerService(PlayerDAO playerDAO, PlayerNodeDAO playerNodeDAO, MatchDAO matchDAO,
            TournamentDAO tournamentDAO) {
        this.playerDAO = playerDAO;
        this.playerNodeDAO = playerNodeDAO;
        this.matchDAO = matchDAO;
        this.tournamentDAO = tournamentDAO;
    }

    public void banPlayer(String playerUsername) {
        try {
            playerDAO.banPlayer(playerUsername);
        } catch (RuntimeException e) {
            logger.error("Errore durante il ban del giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nel bannare il giocatore: " + playerUsername);
        }
    }

    public void unBanPlayer(String playerUsername) {
        try {
            playerDAO.unBanPlayer(playerUsername);
        } catch (Exception e) {
            logger.error("Errore durante lo sblocco del giocatore: {}", playerUsername, e);
            throw new RuntimeException("Errore nel sbloccare il giocatore: " + playerUsername);
        }
    }

    @Transactional
    public void deletePlayer(String playerUsername) {
        // backup in caso di rollback manuale su Neo4j
        Player mongoPlayerBackup = playerDAO.getPlayer(playerUsername);

        // se playerDAO.deletePlayer lancia RuntimeException, Spring fa rollback
        // automatico
        playerDAO.deletePlayer(playerUsername);

        try {
            playerNodeDAO.deletePlayer(playerUsername);
        } catch (Exception neo4jEx) {
            // qui vogliamo comunque cancellare la rollback su Mongo
            if (mongoPlayerBackup != null) {
                playerDAO.rollbackPlayer(mongoPlayerBackup);
            }
            throw new RuntimeException("Errore cancellando da Neo4j, rollback su Mongo eseguito!", neo4jEx);
        }
    }

    public void updatePlayerPassword(String oldPassword, String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Player player = playerDAO.getPlayer(currentUsername);

            if (!encoder.matches(oldPassword, player.getPassword())) {
                throw new IllegalArgumentException("La vecchia password non è corretta.");
            }

            String hashedNewPassword = encoder.encode(newPassword);

            playerDAO.updatePlayerPassword(currentUsername, hashedNewPassword);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento della password per il giocatore: {}", e.getMessage(), e);
            throw new RuntimeException("Errore nell'aggiornare la password per il giocatore.");
        }
    }

    public void updatePlayerUsername(String newUsername) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        try {
            playerDAO.updatePlayerUsername(currentUsername, newUsername);
            matchDAO.updatePlayerUsernameInMatches(currentUsername, newUsername);
            tournamentDAO.updatePlayerUsernameInTournaments(currentUsername, newUsername);
            playerNodeDAO.updatePlayerUsername(currentUsername, newUsername);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiornamento del nome utente da {} a {}", currentUsername, newUsername,
                    e);
            throw new RuntimeException(
                    "Errore nell'aggiornare il nome utente da " + currentUsername + " a " + newUsername);
        }
    }

    // posso vedere le statistiche di tutti i giocatori
    public PlayerNode getStats(String playerId) {
        return playerNodeDAO.getStats(playerId);
    }

    public List<PlayerMatch> getEloTrend(String playerId) {
        try {
            return playerDAO.getEloTrend(playerId);
        } catch (Exception e) {
            logger.error("Errore durante il recupero del trend Elo per il giocatore {}", playerId, e);
            throw new RuntimeException("Errore nel recupero del trend Elo per il giocatore " + playerId);
        }
    }

    public void addFriend(String friendId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();

            if(playerNodeDAO.getPlayerById(friendId)==null)
                throw new RuntimeException("Player not found"+ friendId);

            playerNodeDAO.addFriend(playerId, friendId);
        } catch (Exception e) {
            logger.error("Errore durante l'aggiunta dell'amico {} per il giocatore {}", friendId, e.getMessage(), e);
            throw new RuntimeException("Errore nell'aggiungere l'amico " + friendId);
        }
    }

    public List<PlayerNode> getFriends() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();
            return playerNodeDAO.getFriends(playerId);
        } catch (Exception e) {
            logger.error("Errore durante il recupero della lista degli amici", e.getMessage(), e);
            throw new RuntimeException("Errore durante il recupero della lista degli amici");
        }
    }

    public void removeFriend(String friendId) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String playerId = authentication.getName();

            int removed = playerNodeDAO.removeFriend(playerId, friendId);

            if (removed == 0) {
                throw new IllegalArgumentException("Amico non trovato o relazione inesistente");
            }
        } catch (Exception e) {
            logger.error("Errore durante la rimozione dell'amico {} per il giocatore {}", friendId, e.getMessage(), e);
            throw new RuntimeException("Errore nella rimozione dell'amico " + friendId);
        }
    }

    @Transactional // rollback automatico su Mongo in caso di eccezioni
    public String createPlayer(String username, String password, int elo) {
        // se getPlayer o createPlayer lanciassero eccezione, Spring rollbacka
        // automaticamente
        if (playerDAO.getPlayer(username) == null) {
            playerDAO.createPlayer(username, password);
            try {
                playerNodeDAO.createPlayer(username, elo);
                return "Giocatore creato con successo";
            } catch (Exception neo4jEx) {
                // rollback esplicito su Mongo in caso di fallimento Neo4j
                playerDAO.deletePlayer(username);
                throw new RuntimeException("Errore creando in Neo4j, rollback Mongo eseguito!", neo4jEx);
            }
        } else {
            return "Giocatore già esistente";
        }
    }

    // calculated with this formula: https://it.wikipedia.org/wiki/Elo_(scacchi)
    public static List<Integer> calculateNewElo(Match match) {
        int newWhiteElo, newBlackElo;
        double whiteExpectedScore = expectedScore(match.getWhiteElo(), match.getBlackElo());
        double blackExpectedScore = expectedScore(match.getBlackElo(), match.getWhiteElo());
        double whiteScore = match.getResult().equals("1-0") ? 1 : match.getResult().equals("0-1") ? 0 : 0.5;
        double blackScore = 1 - whiteScore;

        newWhiteElo = (int) (match.getWhiteElo() + EloConstant * (whiteScore - whiteExpectedScore));
        newBlackElo = (int) (match.getBlackElo() + EloConstant * (blackScore - blackExpectedScore));

        return List.of(newWhiteElo, newBlackElo);
    }

    private static double expectedScore(int myElo, int opponentElo) {
        return 1 / (1 + Math.pow(10, (double) (opponentElo - myElo) / 400));
    }

    public List<PlayerTournament> getMyTournaments(String playerId) {
        return playerDAO.getMyTournaments(playerId);
    }
}
