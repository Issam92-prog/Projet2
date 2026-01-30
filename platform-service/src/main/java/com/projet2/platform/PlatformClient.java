package com.projet2.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PlatformClient {

    // --- CONFIGURATION DES PORTS (CORRIGÉE) ---
    private static final String PLATFORM_URL = "http://localhost:8080/api";
    private static final String EDITOR_URL = "http://localhost:8081/api";
    private static final String USER_URL = "http://localhost:8082/api";
    private static final String AUTH_URL = "http://localhost:8082/api/auth";

    // Mot de passe pour le mode plateforme
    private static final String PLATFORM_PASSWORD = "platform";

    // Outils
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Scanner scanner = new Scanner(System.in);
    private static final ObjectMapper mapper = new ObjectMapper();

    // Codes ANSI pour les couleurs
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String BOLD = "\u001B[1m";

    public static void main(String[] args) {
        clearScreen();
        printBanner();

        while (true) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> userModeMenu();
                case "2" -> editorModeMenu();
                case "3" -> platformModeMenu();
                case "4" -> {
                    printSuccess("👋 Au revoir !");
                    return;
                }
                default -> printError("Choix invalide.");
            }
        }
    }

    // =========================================================================
    // MENU PRINCIPAL
    // =========================================================================

    private static void printBanner() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║                                                          ║");
        System.out.println("║     🎮  PLATEFORME DE JEUX - INTERFACE COMPLÈTE  🎮     ║");
        System.out.println("║                                                          ║");
        System.out.println("║            Projet JVM2 - Flux d'Événements              ║");
        System.out.println("║                                                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println(RESET);
    }

    private static void printMainMenu() {
        System.out.println("\n" + BOLD + "═══ MENU PRINCIPAL ═══" + RESET);
        System.out.println("1. 👤 Mode Utilisateur (Joueur)");
        System.out.println("2. 📝 Mode Éditeur");
        System.out.println("3. 🎮 Mode Plateforme");
        System.out.println("4. ❌ Quitter");
        System.out.print("\n" + BOLD + "Votre choix : " + RESET);
    }

    // =========================================================================
    // MODE UTILISATEUR
    // =========================================================================

    private static void userModeMenu() {
        clearScreen();
        printHeader("MODE UTILISATEUR");

        System.out.println("1. 🆕 Créer un compte");
        System.out.println("2. 🔑 Se connecter");
        System.out.println("3. ⬅️  Retour");
        System.out.print("\n" + BOLD + "Votre choix : " + RESET);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> createUserAccount();
            case "2" -> loginUser();
            case "3" -> {}
            default -> printError("Choix invalide.");
        }
    }

    private static void createUserAccount() {
        clearScreen();
        printHeader("INSCRIPTION JOUEUR");

        System.out.print("Pseudo (unique) : ");
        String pseudo = scanner.nextLine();
        System.out.print("Prénom : ");
        String firstName = scanner.nextLine();
        System.out.print("Nom : ");
        String lastName = scanner.nextLine();
        System.out.print("Email : ");
        String email = scanner.nextLine();
        System.out.print("Date de naissance (YYYY-MM-DD) : ");
        String birthDate = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        String json = String.format(
                "{\"pseudo\":\"%s\", \"firstName\":\"%s\", \"lastName\":\"%s\", \"email\":\"%s\", \"birthDate\":\"%s\", \"password\":\"%s\"}",
                pseudo, firstName, lastName, email, birthDate, password
        );

        String response = postRequest(AUTH_URL + "/register", json);
        System.out.println(response);

        if (response.contains("✅")) {
            printInfo("Vous pouvez maintenant vous connecter.");
            pause();
        } else {
            pause();
        }
    }

    private static void loginUser() {
        clearScreen();
        printHeader("CONNEXION JOUEUR");

        System.out.print("Pseudo : ");
        String pseudo = scanner.nextLine();
        System.out.print("Mot de passe : ");
        String password = scanner.nextLine();

        String json = String.format(
                "{\"pseudo\":\"%s\", \"password\":\"%s\"}",
                pseudo, password
        );

        String response = postRequest(AUTH_URL + "/login", json);

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode user = mapper.readTree(response.substring(response.indexOf("{")));
            String userId = user.get("id").asText();
            printSuccess("Connexion réussie ! Bienvenue " + pseudo);
            Thread.sleep(1000);
            userMenu(userId, pseudo);
        } catch (Exception e) {
            printError("Erreur de connexion : " + e.getMessage());
            pause();
        }
    }

    private static void userMenu(String userId, String pseudo) {
        boolean running = true;

        while (running) {
            clearScreen();
            printHeader("ESPACE JOUEUR - " + pseudo + " (ID: " + userId + ")");

            System.out.println(" 1. 📋 Consulter mon profil");
            System.out.println(" 2. 🛒 Voir le catalogue de jeux");
            System.out.println(" 3. 💰 Acheter un jeu");
            System.out.println(" 4. 📚 Ma bibliothèque");
            System.out.println(" 5. ⭐ Noter un jeu");
            System.out.println(" 6. 💫 Ma wishlist");
            System.out.println(" 7. 📰 Mon feed");
            System.out.println(" 8. 🎮 Mettre à jour temps de jeu");
            System.out.println(" 9. 🔍 Voir détails d'un jeu");
            System.out.println("10. 👍 Voter sur une évaluation");
            System.out.println("11. ⬅️  Se déconnecter");
            System.out.print("\n" + BOLD + "Votre choix : " + RESET);

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewUserProfile(userId);
                case "2" -> showCatalog();
                case "3" -> buyGame(userId);
                case "4" -> showMyLibrary(userId);
                case "5" -> rateGame(userId);
                case "6" -> manageWishlist(userId);
                case "7" -> viewFeed(userId);
                case "8" -> updatePlayTime(userId);
                case "9" -> showGameDetailsPage();
                case "10" -> voteOnReview(userId);
                case "11" -> {
                    printSuccess("Déconnexion réussie");
                    pause();
                    running = false;
                }
                default -> {
                    printError("Choix invalide.");
                    pause();
                }
            }
        }
    }

    // --- Fonctionnalités Utilisateur ---

    private static void viewUserProfile(String userId) {
        clearScreen();
        printHeader("MON PROFIL");

        String response = getRequest(USER_URL + "/users/" + userId + "/profile?requesterId=" + userId);

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode profile = mapper.readTree(response);
            System.out.println(CYAN + "═══════════════════════════════════════" + RESET);
            System.out.println("📝 Pseudo      : " + profile.get("username").asText());
            System.out.println("📧 Email       : " + profile.get("email").asText());
            System.out.println("📅 Inscrit le  : " + profile.get("joinedAt").asText());
            System.out.println("⏱️  Temps total : " + profile.get("totalPlayTime").asInt() + "h");
            System.out.println(CYAN + "═══════════════════════════════════════" + RESET);

            System.out.println("\n🎮 Jeux possédés :");
            JsonNode games = profile.get("gamesOwned");
            if (games.isEmpty()) {
                System.out.println("   (Aucun jeu)");
            } else {
                for (JsonNode game : games) {
                    System.out.printf("   - %s (%dh)%n",
                            game.get("gameName").asText(),
                            game.get("playTimeHours").asInt());
                }
            }

            System.out.println("\n⭐ Mes évaluations :");
            JsonNode reviews = profile.get("reviews");
            if (reviews.isEmpty()) {
                System.out.println("   (Aucune évaluation)");
            } else {
                for (JsonNode review : reviews) {
                    System.out.printf("   - Note: %d/5 - \"%s\"%n",
                            review.get("note").asInt(),
                            review.get("comment").asText());
                }
            }

        } catch (Exception e) {
            printError("Erreur d'affichage : " + e.getMessage());
        }

        pause();
    }

    private static void manageWishlist(String userId) {
        clearScreen();
        printHeader("MA WISHLIST");

        System.out.println("1. Voir ma wishlist");
        System.out.println("2. Ajouter un jeu");
        System.out.println("3. Retirer un jeu");
        System.out.print("\n" + BOLD + "Votre choix : " + RESET);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> viewWishlist(userId);
            case "2" -> addToWishlist(userId);
            case "3" -> removeFromWishlist(userId);
        }
    }

    private static void viewWishlist(String userId) {
        String response = getRequest(USER_URL + "/users/" + userId + "/wishlist");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode wishlist = mapper.readTree(response);

            if (wishlist.isEmpty()) {
                printInfo("Votre wishlist est vide");
            } else {
                System.out.println("\n💫 Votre Wishlist :");
                for (JsonNode item : wishlist) {
                    System.out.printf("   - %s (%s)%n",
                            item.get("gameName").asText(),
                            item.get("platform").asText());
                }
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void addToWishlist(String userId) {
        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();
        System.out.print("Nom du jeu : ");
        String gameName = scanner.nextLine();
        System.out.print("Plateforme : ");
        String platform = scanner.nextLine();

        String json = String.format(
                "{\"gameId\":\"%s\", \"gameName\":\"%s\", \"platform\":\"%s\"}",
                gameId, gameName, platform
        );

        System.out.println(postRequest(USER_URL + "/users/" + userId + "/wishlist", json));
        pause();
    }

    private static void removeFromWishlist(String userId) {
        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();
        System.out.print("Plateforme : ");
        String platform = scanner.nextLine();

        System.out.println(deleteRequest(USER_URL + "/users/" + userId + "/wishlist/" + gameId + "?platform=" + platform));
        pause();
    }

    private static void viewFeed(String userId) {
        clearScreen();
        printHeader("MON FEED");

        String response = getRequest(USER_URL + "/users/" + userId + "/feed");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode feed = mapper.readTree(response);

            if (feed.isEmpty()) {
                printInfo("Aucune notification");
            } else {
                for (JsonNode item : feed) {
                    System.out.println("\n📰 " + item.get("message").asText());
                    System.out.println("   🎮 " + item.get("gameName").asText());
                    System.out.printf("   👍 %d / 👎 %d%n",
                            item.get("usefulCount").asInt(),
                            item.get("uselessCount").asInt());
                }
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void updatePlayTime(String userId) {
        clearScreen();
        printHeader("METTRE À JOUR TEMPS DE JEU");

        showMyLibrary(userId);

        System.out.print("\nID du jeu : ");
        String gameId = scanner.nextLine();
        System.out.print("Heures à ajouter : ");
        String hours = scanner.nextLine();

        String json = String.format("{\"hours\": %s}", hours);

        System.out.println(putRequest(USER_URL + "/users/" + userId + "/library/" + gameId + "/playtime", json));
        pause();
    }

    private static void voteOnReview(String userId) {
        System.out.print("ID de l'évaluation : ");
        String rateId = scanner.nextLine();
        System.out.print("Utile ? (oui/non) : ");
        String useful = scanner.nextLine().toLowerCase().startsWith("o") ? "true" : "false";

        String json = String.format("{\"useful\": %s}", useful);

        System.out.println(postRequest(USER_URL + "/users/rates/" + rateId + "/vote?userId=" + userId, json));
        pause();
    }

    // =========================================================================
    // MODE ÉDITEUR
    // =========================================================================

    private static void editorModeMenu() {
        clearScreen();
        printHeader("MODE ÉDITEUR");

        System.out.println("1. 🔑 Se connecter (éditeur existant)");
        System.out.println("2. ⬅️  Retour");
        System.out.print("\n" + BOLD + "Votre choix : " + RESET);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> loginEditor();
            case "2" -> {}
            default -> printError("Choix invalide.");
        }
    }

    private static void loginEditor() {
        clearScreen();
        printHeader("CONNEXION ÉDITEUR");

        System.out.print("Nom de l'éditeur : ");
        String publisherName = scanner.nextLine();

        // Récupérer la liste des éditeurs
        String response = getRequest(EDITOR_URL + "/publishers");

        if (response.startsWith("❌")) {
            printError("Impossible de récupérer la liste des éditeurs");
            pause();
            return;
        }

        try {
            JsonNode publishers = mapper.readTree(response);
            String publisherId = null;

            for (JsonNode publisher : publishers) {
                if (publisher.get("name").asText().equalsIgnoreCase(publisherName)) {
                    publisherId = publisher.get("id").asText();
                    break;
                }
            }

            if (publisherId != null) {
                printSuccess("Connexion réussie !");
                Thread.sleep(1000);
                editorMenu(publisherId, publisherName);
            } else {
                printError("Éditeur introuvable. Éditeurs disponibles :");
                for (JsonNode publisher : publishers) {
                    System.out.println("   - " + publisher.get("name").asText());
                }
                pause();
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
            pause();
        }
    }

    private static void editorMenu(String publisherId, String publisherName) {
        boolean running = true;

        while (running) {
            clearScreen();
            printHeader("ESPACE ÉDITEUR - " + publisherName);

            System.out.println(" 1. 🎮 Voir mes jeux");
            System.out.println(" 2. 🔧 Publier un patch");
            System.out.println(" 3. 🔥 Voir les rapports d'incidents");
            System.out.println(" 4. 💬 Voir les commentaires");
            System.out.println(" 5. 📊 Statistiques");
            System.out.println(" 6. ⬅️  Se déconnecter");
            System.out.print("\n" + BOLD + "Votre choix : " + RESET);

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewMyGames(publisherName);
                case "2" -> publishPatchEditor();
                case "3" -> viewIncidents();
                case "4" -> viewComments();
                case "5" -> viewEditorStatistics(publisherName);
                case "6" -> {
                    printSuccess("Déconnexion réussie");
                    pause();
                    running = false;
                }
                default -> {
                    printError("Choix invalide.");
                    pause();
                }
            }
        }
    }

    // --- Fonctionnalités Éditeur ---

    private static void viewMyGames(String publisherName) {
        clearScreen();
        printHeader("MES JEUX");

        String response = getRequest(EDITOR_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);
            int count = 0;

            for (JsonNode game : games) {
                if (game.get("publisherName").asText().equals(publisherName)) {
                    count++;
                    System.out.println("\n🎮 " + game.get("name").asText());
                    System.out.println("   ID: " + game.get("gameId").asText());
                    System.out.println("   Plateforme: " + game.get("platform").asText());
                    System.out.println("   Genre: " + game.get("genre").asText());
                    System.out.println("   Version: " + game.get("currentVersion").asText());
                }
            }

            if (count == 0) {
                printInfo("Aucun jeu publié");
            } else {
                printSuccess(count + " jeu(x) trouvé(s)");
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void publishPatchEditor() {
        clearScreen();
        printHeader("PUBLIER UN PATCH");

        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();
        System.out.print("Nouvelle version (ex: 1.2.0) : ");
        String newVersion = scanner.nextLine();
        System.out.print("Description : ");
        String description = scanner.nextLine();
        System.out.print("Type (BUGFIX/FEATURE/OPTIMIZATION) : ");
        String type = scanner.nextLine().toUpperCase();

        System.out.println("\nChangements (entrez 'fin' pour terminer) :");
        List<String> changes = new ArrayList<>();
        while (true) {
            System.out.print("  - ");
            String change = scanner.nextLine();
            if (change.equalsIgnoreCase("fin")) break;
            if (!change.isBlank()) changes.add(change);
        }

        if (changes.isEmpty()) {
            printError("Au moins un changement est requis");
            pause();
            return;
        }

        try {
            String changesJson = mapper.writeValueAsString(changes);
            String json = String.format(
                    "{\"gameId\":\"%s\", \"newVersion\":\"%s\", \"description\":\"%s\", \"type\":\"%s\", \"changes\":%s}",
                    gameId, newVersion, description, type, changesJson
            );

            System.out.println(postRequest(EDITOR_URL + "/patches", json));
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewIncidents() {
        clearScreen();
        printHeader("RAPPORTS D'INCIDENTS");

        String response = getRequest(EDITOR_URL + "/incidents");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode incidents = mapper.readTree(response);

            if (incidents.isEmpty()) {
                printSuccess("Aucun incident non traité !");
            } else {
                System.out.println("📊 Total: " + incidents.size() + " incidents\n");

                for (JsonNode incident : incidents) {
                    System.out.println("🔥 " + incident.get("gameName").asText());
                    JsonNode error = incident.get("error");
                    System.out.println("   Message: " + error.get("message").asText());
                    System.out.println("   Gravité: " + error.get("severity").asText());
                    System.out.println("   Status: " + incident.get("status").asText());
                    System.out.println();
                }
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewComments() {
        clearScreen();
        printHeader("COMMENTAIRES ET ÉVALUATIONS");

        String response = getRequest(EDITOR_URL + "/reviews");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode reviews = mapper.readTree(response);

            if (reviews.isEmpty()) {
                printInfo("Aucun commentaire");
            } else {
                for (JsonNode review : reviews) {
                    System.out.println("\n🎮 " + review.get("gameName").asText());
                    System.out.println("   ⭐ " + review.get("rating").asInt() + "/5");
                    System.out.println("   💬 \"" + review.get("comment").asText() + "\"");
                    System.out.println("   Sentiment: " + review.get("sentiment").asText());
                    if (review.get("isProblematic").asBoolean()) {
                        System.out.println("   ⚠️ Commentaire problématique !");
                    }
                }
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewEditorStatistics(String publisherName) {
        clearScreen();
        printHeader("STATISTIQUES - " + publisherName);

        // Récupérer les jeux
        String gamesResponse = getRequest(EDITOR_URL + "/games");
        String incidentsResponse = getRequest(EDITOR_URL + "/incidents");
        String reviewsResponse = getRequest(EDITOR_URL + "/reviews");

        try {
            JsonNode allGames = mapper.readTree(gamesResponse);
            JsonNode incidents = mapper.readTree(incidentsResponse);
            JsonNode reviews = mapper.readTree(reviewsResponse);

            int myGamesCount = 0;
            for (JsonNode game : allGames) {
                if (game.get("publisherName").asText().equals(publisherName)) {
                    myGamesCount++;
                }
            }

            System.out.println("🎮 Nombre de jeux: " + myGamesCount);
            System.out.println("🔥 Incidents non traités: " + incidents.size());
            System.out.println("⭐ Nombre d'évaluations: " + reviews.size());

            if (!reviews.isEmpty()) {
                double totalRating = 0;
                int positiveCount = 0;
                int neutralCount = 0;
                int negativeCount = 0;

                for (JsonNode review : reviews) {
                    totalRating += review.get("rating").asDouble();
                    String sentiment = review.get("sentiment").asText();
                    switch (sentiment) {
                        case "POSITIVE" -> positiveCount++;
                        case "NEUTRAL" -> neutralCount++;
                        case "NEGATIVE" -> negativeCount++;
                    }
                }

                double avgRating = totalRating / reviews.size();
                System.out.printf("📈 Note moyenne: %.1f/5.0%n", avgRating);
                System.out.println("\n📊 Répartition des sentiments:");
                System.out.println("   👍 Positifs: " + positiveCount);
                System.out.println("   😐 Neutres: " + neutralCount);
                System.out.println("   👎 Négatifs: " + negativeCount);
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    // =========================================================================
    // MODE PLATEFORME
    // =========================================================================

    private static void platformModeMenu() {
        clearScreen();
        printHeader("MODE PLATEFORME");

        // ✅ AFFICHAGE DU MOT DE PASSE (pour projet académique)
        System.out.println(YELLOW + "ℹ️  Mot de passe: " + BOLD + "platform" + RESET);
        System.out.print("🔐 Entrez le mot de passe : ");
        String password = scanner.nextLine();

        if (!password.equals(PLATFORM_PASSWORD)) {
            printError("Mot de passe incorrect !");
            pause();
            return;
        }

        printSuccess("Authentification réussie !");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        platformMenu();
    }

    private static void platformMenu() {
        boolean running = true;

        while (running) {
            clearScreen();
            printHeader("ESPACE PLATEFORME");

            System.out.println(" 1. 📊 Vue d'ensemble du catalogue");
            System.out.println(" 2. 📈 Statistiques globales");
            System.out.println(" 3. 🏆 Top jeux par note");
            System.out.println(" 4. 💰 Top jeux par ventes");
            System.out.println(" 5. 💵 Gestion des prix");
            System.out.println(" 6. 🏢 Voir tous les éditeurs");
            System.out.println(" 7. 🔥 Simuler des crashs");
            System.out.println(" 8. 📜 Historique des patches");
            System.out.println(" 9. ⬅️  Se déconnecter");
            System.out.print("\n" + BOLD + "Votre choix : " + RESET);

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> viewCatalogOverview();
                case "2" -> viewGlobalStats();
                case "3" -> viewTopRated();
                case "4" -> viewTopSales();
                case "5" -> managePricing();
                case "6" -> viewAllPublishers();
                case "7" -> simulateCrashes();
                case "8" -> viewPatchHistory();
                case "9" -> {
                    printSuccess("Déconnexion réussie");
                    pause();
                    running = false;
                }
                default -> {
                    printError("Choix invalide.");
                    pause();
                }
            }
        }
    }

    // --- Fonctionnalités Plateforme ---

    private static void viewCatalogOverview() {
        clearScreen();
        printHeader("VUE D'ENSEMBLE DU CATALOGUE");

        String response = getRequest(PLATFORM_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);

            System.out.println("🎮 Nombre total de jeux: " + games.size());

            // Compter les plateformes
            java.util.Set<String> platforms = new java.util.HashSet<>();
            for (JsonNode game : games) {
                if (game.has("versions")) {
                    game.get("versions").fieldNames().forEachRemaining(platforms::add);
                }
            }
            System.out.println("📦 Plateformes: " + String.join(", ", platforms));

            // Compter les éditeurs
            java.util.Set<String> publishers = new java.util.HashSet<>();
            for (JsonNode game : games) {
                publishers.add(game.get("publisherName").asText());
            }
            System.out.println("🏢 Éditeurs: " + publishers.size());

            // Calculer ventes totales
            double totalSales = 0;
            for (JsonNode game : games) {
                if (game.has("salesCount")) {
                    totalSales += game.get("salesCount").asDouble();
                }
            }
            System.out.println("💰 Ventes totales: " + (long)totalSales);

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewGlobalStats() {
        clearScreen();
        printHeader("STATISTIQUES GLOBALES");

        String response = getRequest(PLATFORM_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);

            double totalRevenue = 0;
            double totalRating = 0;
            int gamesWithReviews = 0;
            int earlyAccess = 0;
            int dlcCount = 0;

            for (JsonNode game : games) {
                double sales = game.has("salesCount") ? game.get("salesCount").asDouble() : 0;
                double price = game.has("currentPrice") ? game.get("currentPrice").asDouble() : 0;
                totalRevenue += sales * price;

                if (game.has("reviewCount") && game.get("reviewCount").asInt() > 0) {
                    totalRating += game.get("averageRating").asDouble();
                    gamesWithReviews++;
                }

                if (game.has("isEarlyAccess") && game.get("isEarlyAccess").asBoolean()) {
                    earlyAccess++;
                }

                if (game.has("isDlc") && game.get("isDlc").asBoolean()) {
                    dlcCount++;
                }
            }

            System.out.printf("💰 Revenu total estimé: %.2f€%n", totalRevenue);
            if (gamesWithReviews > 0) {
                System.out.printf("⭐ Note moyenne: %.2f/5.0%n", totalRating / gamesWithReviews);
            }
            System.out.println("🚀 Jeux en early access: " + earlyAccess);
            System.out.println("📦 DLCs disponibles: " + dlcCount);

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewTopRated() {
        clearScreen();
        printHeader("TOP 10 JEUX PAR NOTE");

        String response = getRequest(PLATFORM_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);
            List<JsonNode> gamesList = new ArrayList<>();

            for (JsonNode game : games) {
                if (game.has("reviewCount") && game.get("reviewCount").asInt() >= 3) {
                    gamesList.add(game);
                }
            }

            gamesList.sort((a, b) ->
                    Double.compare(
                            b.get("averageRating").asDouble(),
                            a.get("averageRating").asDouble()
                    )
            );

            for (int i = 0; i < Math.min(10, gamesList.size()); i++) {
                JsonNode game = gamesList.get(i);
                System.out.printf("%d. %s%n", i + 1, game.get("title").asText());
                System.out.printf("   ⭐ Note: %.2f/5.0 (%d avis)%n",
                        game.get("averageRating").asDouble(),
                        game.get("reviewCount").asInt());
                System.out.println("   🏢 " + game.get("publisherName").asText());
                System.out.println();
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewTopSales() {
        clearScreen();
        printHeader("TOP 10 JEUX PAR VENTES");

        String response = getRequest(PLATFORM_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);
            List<JsonNode> gamesList = new ArrayList<>();
            games.forEach(gamesList::add);

            gamesList.sort((a, b) ->
                    Double.compare(
                            b.has("salesCount") ? b.get("salesCount").asDouble() : 0,
                            a.has("salesCount") ? a.get("salesCount").asDouble() : 0
                    )
            );

            for (int i = 0; i < Math.min(10, gamesList.size()); i++) {
                JsonNode game = gamesList.get(i);
                double sales = game.has("salesCount") ? game.get("salesCount").asDouble() : 0;
                double price = game.has("currentPrice") ? game.get("currentPrice").asDouble() : 0;

                System.out.printf("%d. %s%n", i + 1, game.get("title").asText());
                System.out.printf("   📊 Ventes: %d%n", (long)sales);
                System.out.printf("   💰 Revenu: %.2f€%n", sales * price);
                System.out.println();
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void managePricing() {
        clearScreen();
        printHeader("GESTION DES PRIX");

        System.out.print("ID du jeu à analyser : ");
        String gameId = scanner.nextLine();

        String response = getRequest(PLATFORM_URL + "/games/" + gameId);

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode game = mapper.readTree(response);

            double basePrice = game.has("basePrice") ? game.get("basePrice").asDouble() : 59.99;
            double currentPrice = game.has("currentPrice") ? game.get("currentPrice").asDouble() : basePrice;
            double avgRating = game.has("averageRating") ? game.get("averageRating").asDouble() : 0;
            double sales = game.has("salesCount") ? game.get("salesCount").asDouble() : 0;

            System.out.println("\n📊 ANALYSE DU JEU: " + game.get("title").asText());
            System.out.printf("💰 Prix de base: %.2f€%n", basePrice);
            System.out.printf("💵 Prix actuel: %.2f€%n", currentPrice);
            System.out.printf("⭐ Note: %.1f/5.0%n", avgRating);
            System.out.printf("📈 Ventes: %d%n", (long)sales);

            // Calculer prix suggéré
            double suggestedPrice = basePrice;

            if (avgRating >= 4.5) suggestedPrice *= 1.1;
            else if (avgRating >= 4.0) suggestedPrice *= 1.05;
            else if (avgRating < 3.0) suggestedPrice *= 0.85;

            if (sales > 10000) suggestedPrice *= 1.1;
            else if (sales > 5000) suggestedPrice *= 1.05;
            else if (sales < 1000) suggestedPrice *= 0.9;

            System.out.printf("%n💡 Prix suggéré: %.2f€%n", suggestedPrice);

            if (suggestedPrice > currentPrice) {
                System.out.println("   ↗️ Recommandation: Augmenter le prix");
            } else if (suggestedPrice < currentPrice) {
                System.out.println("   ↘️ Recommandation: Baisser le prix");
            } else {
                System.out.println("   ✓ Le prix actuel est optimal");
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void viewAllPublishers() {
        clearScreen();
        printHeader("TOUS LES ÉDITEURS");

        String response = getRequest(PLATFORM_URL + "/games");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode games = mapper.readTree(response);
            java.util.Map<String, java.util.List<JsonNode>> publisherGames = new java.util.HashMap<>();

            for (JsonNode game : games) {
                String publisher = game.get("publisherName").asText();
                publisherGames.computeIfAbsent(publisher, k -> new java.util.ArrayList<>()).add(game);
            }

            System.out.println("🏢 " + publisherGames.size() + " éditeurs actifs\n");

            for (var entry : publisherGames.entrySet()) {
                String publisher = entry.getKey();
                List<JsonNode> pGames = entry.getValue();

                double totalRating = 0;
                int gamesWithReviews = 0;

                for (JsonNode game : pGames) {
                    if (game.has("reviewCount") && game.get("reviewCount").asInt() > 0) {
                        totalRating += game.get("averageRating").asDouble();
                        gamesWithReviews++;
                    }
                }

                System.out.println("🏢 " + publisher);
                System.out.println("   🎮 " + pGames.size() + " jeux");
                if (gamesWithReviews > 0) {
                    System.out.printf("   ⭐ Note moyenne: %.1f/5.0%n", totalRating / gamesWithReviews);
                }
                System.out.println();
            }

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void simulateCrashes() {
        clearScreen();
        printHeader("SIMULATION DE CRASHS");

        System.out.println("1. Crash pour un jeu spécifique");
        System.out.println("2. Crashs aléatoires");
        System.out.println("3. Crash personnalisé");
        System.out.print("\n" + BOLD + "Votre choix : " + RESET);

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                System.out.print("ID du jeu : ");
                String gameId = scanner.nextLine();
                System.out.println(postRequest(PLATFORM_URL + "/monitoring/crash/" + gameId, "{}"));
            }
            case "2" -> {
                System.out.print("Nombre de crashs : ");
                String count = scanner.nextLine();
                System.out.println(postRequest(PLATFORM_URL + "/monitoring/crash/random?count=" + count, "{}"));
            }
            case "3" -> {
                System.out.print("ID du jeu : ");
                String gameId = scanner.nextLine();
                System.out.print("Plateforme : ");
                String platform = scanner.nextLine();
                System.out.print("Message d'erreur : ");
                String errorMessage = scanner.nextLine();
                System.out.print("Stack trace : ");
                String stackTrace = scanner.nextLine();

                String json = String.format(
                        "{\"gameId\":\"%s\", \"platform\":\"%s\", \"errorMessage\":\"%s\", \"stackTrace\":\"%s\"}",
                        gameId, platform, errorMessage, stackTrace
                );
                System.out.println(postRequest(PLATFORM_URL + "/monitoring/crash/custom", json));
            }
        }

        pause();
    }

    private static void viewPatchHistory() {
        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();

        String response = getRequest(PLATFORM_URL + "/games/" + gameId + "/patches");

        if (response.startsWith("❌")) {
            System.out.println(response);
            pause();
            return;
        }

        try {
            JsonNode patches = mapper.readTree(response);

            if (patches.isEmpty()) {
                printInfo("Aucun patch pour ce jeu");
            } else {
                System.out.println("\n📜 " + patches.size() + " patch(es)\n");
                for (JsonNode patch : patches) {
                    System.out.println("🔧 Version " + patch.get("version").asText());
                    System.out.println("   📝 " + patch.get("description").asText());
                    System.out.println("   📅 " + patch.get("releaseDate").asText());
                    System.out.println();
                }
            }
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    // =========================================================================
    // FONCTIONS COMMUNES
    // =========================================================================

    private static void showCatalog() {
        int page = 0;
        int size = 20;
        boolean browsing = true;

        while (browsing) {
            clearScreen();
            printHeader("CATALOGUE (Page " + (page + 1) + ")");

            String url = PLATFORM_URL + "/games/catalog?page=" + page + "&size=" + size;
            String jsonResponse = getRequest(url);

            if (jsonResponse.startsWith("❌")) {
                System.out.println(jsonResponse);
                pause();
                return;
            }

            try {
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode games = root.get("content");
                int totalPages = root.get("totalPages").asInt();
                long totalElements = root.get("totalElements").asLong();

                displayGamesTable(games);

                System.out.println("─────────────────────────────────────────────────────────────────");
                System.out.println("Page " + (page + 1) + "/" + totalPages + " (" + totalElements + " jeux au total)");
                System.out.println("[S]uivant | [P]récédent | [R]etour");
                System.out.print("Action : ");
                String action = scanner.nextLine().trim().toLowerCase();

                switch (action) {
                    case "s" -> {
                        if (page < totalPages - 1) page++;
                        else printWarning("Dernière page");
                    }
                    case "p" -> {
                        if (page > 0) page--;
                        else printWarning("Première page");
                    }
                    case "r" -> browsing = false;
                }
            } catch (Exception e) {
                printError("Erreur : " + e.getMessage());
                browsing = false;
            }
        }
    }

    private static void displayGamesTable(JsonNode games) {
        System.out.printf("%-38s | %-25s | %-8s | %-10s | %-15s%n",
                "ID (UUID)", "TITRE", "PLATF.", "PRIX", "NOTE (AVIS)");
        System.out.println("───────────────────────────────────────┼──────────────────────────┼──────────┼────────────┼────────────────");

        if (games.isEmpty()) {
            System.out.println("                       (Aucun jeu trouvé)                       ");
        } else {
            for (JsonNode game : games) {
                String id = game.get("id").asText();
                String title = game.get("title").asText();
                if (title.length() > 23) title = title.substring(0, 20) + "...";

                double price = game.has("currentPrice") ? game.get("currentPrice").asDouble() : 0.0;
                double rating = game.has("averageRating") ? game.get("averageRating").asDouble() : 0.0;
                int reviewCount = game.has("reviewCount") ? game.get("reviewCount").asInt() : 0;

                String platform = "N/A";
                if (game.has("versions") && !game.get("versions").isEmpty()) {
                    platform = game.get("versions").fieldNames().next();
                }

                System.out.printf("%-38s | %-25s | %-8s | %-10s | %-15s%n",
                        id, title, platform,
                        String.format("%.2f €", price),
                        String.format("%.1f/5 (%d)", rating, reviewCount));
            }
        }
    }

    private static void showGameDetailsPage() {
        System.out.print("ID du jeu (UUID) : ");
        String gameId = scanner.nextLine();

        clearScreen();
        printHeader("DÉTAILS DU JEU");

        String gameJson = getRequest(PLATFORM_URL + "/games/" + gameId);
        if (gameJson.startsWith("❌") || gameJson.contains("404")) {
            printError("Jeu introuvable");
            pause();
            return;
        }

        try {
            JsonNode g = mapper.readTree(gameJson);
            System.out.println(CYAN + "═══════════════════════════════════════════════════" + RESET);
            System.out.println(BOLD + "📀 " + g.get("title").asText().toUpperCase() + RESET);
            System.out.println(CYAN + "═══════════════════════════════════════════════════" + RESET);
            System.out.println("🏢 Éditeur : " + g.get("publisherName").asText());
            System.out.println("💰 Prix    : " + g.get("currentPrice").asDouble() + " €");
            System.out.println("⭐ Note    : " + g.get("averageRating").asDouble() + "/5 (" +
                    g.get("reviewCount").asInt() + " avis)");

            // DLCs
            System.out.println("\n" + BOLD + "📦 EXTENSIONS & DLCs" + RESET);
            String dlcsJson = getRequest(PLATFORM_URL + "/games/" + gameId + "/dlcs");
            JsonNode dlcs = mapper.readTree(dlcsJson);
            if (dlcs.isEmpty()) {
                System.out.println("   (Aucun DLC disponible)");
            } else {
                for (JsonNode dlc : dlcs) {
                    System.out.printf("   🔹 %s (%.2f€)%n",
                            dlc.get("title").asText(),
                            dlc.get("currentPrice").asDouble());
                }
            }

            // Patches
            System.out.println("\n" + BOLD + "🛠️ HISTORIQUE DES CORRECTIFS" + RESET);
            String patchesJson = getRequest(PLATFORM_URL + "/games/" + gameId + "/patches");
            JsonNode patches = mapper.readTree(patchesJson);
            if (patches.isEmpty()) {
                System.out.println("   (Aucun patch)");
            } else {
                for (JsonNode p : patches) {
                    System.out.printf("   🔸 v%s : %s%n",
                            p.get("version").asText(),
                            p.get("description").asText());
                }
            }

            // Avis
            System.out.println("\n" + BOLD + "🗣️ AVIS DES JOUEURS" + RESET);
            String reviewsJson = getRequest(USER_URL + "/users/rates/game/" + gameId);
            if (!reviewsJson.startsWith("❌")) {
                JsonNode reviews = mapper.readTree(reviewsJson);
                if (reviews.isEmpty()) {
                    System.out.println("   (Aucun avis)");
                } else {
                    for (JsonNode r : reviews) {
                        System.out.printf("   💬 %s/5 : \"%s\"%n",
                                r.get("note").asText(),
                                r.get("comment").asText());
                    }
                }
            }

            System.out.println(CYAN + "═══════════════════════════════════════════════════" + RESET);

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void buyGame(String userId) {
        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();

        String gameJson = getRequest(PLATFORM_URL + "/games/" + gameId);

        if (gameJson.startsWith("❌")) {
            printError("Jeu introuvable");
            pause();
            return;
        }

        try {
            JsonNode game = mapper.readTree(gameJson);
            String gameName = game.get("title").asText();
            JsonNode versions = game.get("versions");

            if (versions.isEmpty()) {
                printError("Ce jeu n'est disponible sur aucune plateforme");
                pause();
                return;
            }

            List<String> availablePlatforms = new ArrayList<>();
            versions.fieldNames().forEachRemaining(availablePlatforms::add);

            String selectedPlatform;
            if (availablePlatforms.size() == 1) {
                selectedPlatform = availablePlatforms.get(0);
                printInfo("Seule plateforme disponible : " + selectedPlatform);
            } else {
                System.out.println("Plateformes disponibles :");
                for (int i = 0; i < availablePlatforms.size(); i++) {
                    System.out.println("   " + (i + 1) + ". " + availablePlatforms.get(i));
                }
                System.out.print("Votre choix : ");
                int choice = Integer.parseInt(scanner.nextLine());
                selectedPlatform = availablePlatforms.get(choice - 1);
            }

            boolean isDlc = game.has("isDlc") && game.get("isDlc").asBoolean();
            String parentId = "";
            if (isDlc && game.has("parentGameId") && !game.get("parentGameId").isNull()) {
                parentId = game.get("parentGameId").asText();
            }

            double price = game.has("currentPrice") ? game.get("currentPrice").asDouble() : 59.99;

            String jsonBody = String.format(
                    "{\"gameId\":\"%s\", \"gameName\":\"%s\", \"platform\":\"%s\", \"price\":%s, \"isDlc\":%b, \"parentGameId\":\"%s\"}",
                    gameId, gameName, selectedPlatform, price, isDlc, parentId
            );

            System.out.println(postRequest(USER_URL + "/users/" + userId + "/buy", jsonBody));

        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }

        pause();
    }

    private static void showMyLibrary(String userId) {
        String jsonResponse = getRequest(USER_URL + "/users/" + userId + "/library");

        if (jsonResponse.startsWith("❌")) {
            System.out.println(jsonResponse);
            return;
        }

        try {
            JsonNode games = mapper.readTree(jsonResponse);
            System.out.println("\n" + BOLD + "═══ 📚 MA BIBLIOTHÈQUE ═══" + RESET);
            System.out.printf("%-38s | %-30s | %-8s | %-12s%n",
                    "ID DU JEU", "TITRE", "PLATF.", "TEMPS DE JEU");
            System.out.println("───────────────────────────────────────┼────────────────────────────────┼──────────┼──────────────");

            if (games.isEmpty()) {
                System.out.println("       (Vide. Allez acheter des jeux !)       ");
            } else {
                for (JsonNode item : games) {
                    String gId = item.get("gameId").asText();
                    String title = item.get("gameName").asText();
                    if (title.length() > 28) title = title.substring(0, 25) + "...";

                    String platform = item.get("platform").asText();
                    double hours = item.has("playTimeHours") ? item.get("playTimeHours").asDouble() : 0.0;

                    System.out.printf("%-38s | %-30s | %-8s | %-10s%n",
                            gId, title, platform, String.format("%.1fh", hours));
                }
            }
            System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────");
        } catch (Exception e) {
            printError("Erreur : " + e.getMessage());
        }
    }

    private static void rateGame(String userId) {
        System.out.print("ID du jeu : ");
        String gameId = scanner.nextLine();
        System.out.print("Nom du jeu : ");
        String gameName = scanner.nextLine();
        System.out.print("Note (1-5) : ");
        String note = scanner.nextLine();
        System.out.print("Commentaire : ");
        String comment = scanner.nextLine();

        String json = String.format(
                "{\"gameId\":\"%s\", \"gameName\":\"%s\", \"note\":%s, \"comment\":\"%s\"}",
                gameId, gameName, note, comment
        );

        System.out.println(postRequest(USER_URL + "/users/" + userId + "/rate", json));
        pause();
    }

    // =========================================================================
    // UTILITAIRES
    // =========================================================================

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void printHeader(String title) {
        System.out.println(CYAN + BOLD);
        System.out.println("╔" + "═".repeat(title.length() + 2) + "╗");
        System.out.println("║ " + title + " ║");
        System.out.println("╚" + "═".repeat(title.length() + 2) + "╝");
        System.out.println(RESET);
    }

    private static void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }

    private static void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }

    private static void printWarning(String message) {
        System.out.println(YELLOW + "⚠ " + message + RESET);
    }

    private static void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }

    private static void pause() {
        System.out.print("\n" + YELLOW + "Appuyez sur Entrée pour continuer..." + RESET);
        scanner.nextLine();
    }

    private static String getRequest(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return "❌ Erreur " + response.statusCode();
            return response.body();
        } catch (Exception e) {
            return "❌ Exception: " + e.getMessage();
        }
    }

    private static String postRequest(String uri, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return "❌ Erreur " + response.statusCode() + " : " + response.body();
            }
            return "✅ Succès : " + response.body();
        } catch (Exception e) {
            return "❌ Exception: " + e.getMessage();
        }
    }

    private static String putRequest(String uri, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return "❌ Erreur " + response.statusCode();
            return "✅ Succès : " + response.body();
        } catch (Exception e) {
            return "❌ Exception: " + e.getMessage();
        }
    }

    private static String deleteRequest(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return "❌ Erreur " + response.statusCode();
            return "✅ Succès";
        } catch (Exception e) {
            return "❌ Exception: " + e.getMessage();
        }
    }
}