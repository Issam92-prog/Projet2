package com.projet2.platform.client;

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

    // --- CONFIGURATION DES PORTS ---
    private static final String EDITOR_URL = "http://localhost:8081/api";
    private static final String PLATFORM_URL = "http://localhost:8082/api";
    private static final String USER_URL = "http://localhost:8083/api";
    private static final String AUTH_URL = "http://localhost:8083/api/auth";

    // Outils
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Scanner scanner = new Scanner(System.in);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("🎮  PLATEFORME DE JEUX - CLIENT CONSOLE  🎮");
        System.out.println("=============================================");

        while (true) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1. 🆕 Créer un compte Joueur");
            System.out.println("2. 👤 Se connecter en tant que Joueur");
            System.out.println("-------------------------------------");
            System.out.println("3. 🆕 Enregistrer un Éditeur (Entreprise)");
            System.out.println("4. 🏢 Se connecter en tant qu'Éditeur");
            System.out.println("-------------------------------------");
            System.out.println("5. 🚪 Quitter");
            System.out.print("Votre choix : ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> createAccountUser();
                case "2" -> loginUserFlow();
                case "3" -> createAccountEditor();
                case "4" -> loginEditorFlow();
                case "5" -> {
                    System.out.println("Fermeture...");
                    return;
                }
                default -> System.out.println("❌ Choix invalide.");
            }
        }
    }

    // =========================================================================
    // 1. GESTION DES COMPTES
    // =========================================================================

    private static void createAccountUser() {
        System.out.println("\n--- 📝 INSCRIPTION JOUEUR ---");
        System.out.print("Pseudo : "); String pseudo = scanner.nextLine();
        System.out.print("Email : "); String email = scanner.nextLine();
        System.out.print("Mot de passe : "); String password = scanner.nextLine();
        System.out.print("Prénom : "); String firstName = scanner.nextLine();
        System.out.print("Nom : "); String lastName = scanner.nextLine();
        System.out.print("Date de naissance (YYYY-MM-DD) : "); String birthDate = scanner.nextLine();

        String json = String.format(
                "{\"pseudo\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"firstName\":\"%s\", \"lastName\":\"%s\", \"birthDate\":\"%s\"}",
                pseudo, email, password, firstName, lastName, birthDate
        );
        System.out.println(postRequest(AUTH_URL + "/register", json));
    }

    private static void createAccountEditor() {
        System.out.println("\n--- 📝 ENREGISTREMENT ÉDITEUR ---");
        System.out.print("Nom de la société : "); String name = scanner.nextLine();
        System.out.print("Email de contact : "); String email = scanner.nextLine();

        String json = String.format("{\"name\":\"%s\", \"contactEmail\":\"%s\"}", name, email);
        System.out.println(postRequest(EDITOR_URL + "/publishers", json));
    }

    // =========================================================================
    // 2. CONNEXION ET MENUS
    // =========================================================================

    private static void loginUserFlow() {
        System.out.print("\n🔑 Entrez votre ID Joueur (ex: 1) : ");
        String userId = scanner.nextLine();

        if (!checkUrlExists(USER_URL + "/users/" + userId + "/profile")) {
            System.out.println("⛔ ERREUR : Cet utilisateur n'existe pas (ou service injoignable).");
            return;
        }
        playerMenu(userId);
    }

    private static void playerMenu(String userId) {
        boolean inPlayerMode = true;
        while (inPlayerMode) {
            System.out.println("\n--- 👤 ESPACE JOUEUR (ID: " + userId + ") ---");
            System.out.println("1. 🔎 Consulter le Catalogue (Paginé)");
            System.out.println("2. ℹ️  Voir la Fiche d'un Jeu (Détails/DLC/Patchs/Avis)"); // ✅ RETOUR DE L'OPTION
            System.out.println("3. 💰 Acheter un jeu");
            System.out.println("4. 📚 Ma Bibliothèque");
            System.out.println("5. ⭐ Noter un jeu");
            System.out.println("6. ⬅️ Déconnexion");
            System.out.print("Choix : ");

            switch (scanner.nextLine()) {
                case "1" -> showCatalog();
                case "2" -> showGameDetailsPage(); // ✅ Appel de la méthode manquante
                case "3" -> buyGame(userId);
                case "4" -> showMyLibrary(userId);
                case "5" -> rateGame(userId);
                case "6" -> inPlayerMode = false;
                default -> System.out.println("❌ Invalide.");
            }
        }
    }

    private static void loginEditorFlow() {
        System.out.print("\n🔑 Entrez votre ID Éditeur (ex: 1) : ");
        String publisherId = scanner.nextLine();

        if (!checkUrlExists(EDITOR_URL + "/publishers/" + publisherId + "/games")) {
            System.out.println("⛔ ERREUR : Cet éditeur n'existe pas (ou service injoignable).");
            return;
        }
        editorMenu(publisherId);
    }

    private static void editorMenu(String publisherId) {
        boolean inEditorMode = true;
        while (inEditorMode) {
            System.out.println("\n--- 🏢 ESPACE ÉDITEUR (ID: " + publisherId + ") ---");
            System.out.println("1. 🚀 Publier un nouveau Jeu");
            System.out.println("2. 🔧 Publier un Patch");
            System.out.println("3. 📋 Voir mes jeux publiés");
            System.out.println("4. ⬅️ Déconnexion");
            System.out.print("Choix : ");

            switch (scanner.nextLine()) {
                case "1" -> publishGame(publisherId);
                case "2" -> publishPatch();
                case "3" -> showPublisherGames(publisherId);
                case "4" -> inEditorMode = false;
                default -> System.out.println("❌ Invalide.");
            }
        }
    }

    // =========================================================================
    // 3. FONCTIONNALITÉS MÉTIER
    // =========================================================================

    // --- A. CATALOGUE PAGINÉ ---
    private static void showCatalog() {
        int page = 0;
        int size = 20;
        boolean browsing = true;

        while (browsing) {
            System.out.println("\n--- 🛒 CATALOGUE (Page " + (page + 1) + ") ---");
            System.out.println("Chargement...");

            String url = PLATFORM_URL + "/games/catalog?page=" + page + "&size=" + size;
            String jsonResponse = getRequest(url);

            if (jsonResponse.startsWith("❌")) {
                System.out.println(jsonResponse);
                return;
            }

            try {
                JsonNode root = mapper.readTree(jsonResponse);
                JsonNode games = root.get("content");
                int totalPages = root.get("totalPages").asInt();
                long totalElements = root.get("totalElements").asLong();

                displayGamesTable(games);

                System.out.println("-------------------------------------------------------------");
                System.out.println("Page " + (page + 1) + "/" + totalPages + " (" + totalElements + " jeux au total)");
                System.out.println("[S]uivant | [P]récédent | [R]etour");
                System.out.print("Action : ");
                String action = scanner.nextLine().trim().toLowerCase();

                switch (action) {
                    case "s" -> { if (page < totalPages - 1) page++; else System.out.println("⚠️ Dernière page."); }
                    case "p" -> { if (page > 0) page--; else System.out.println("⚠️ Première page."); }
                    case "r" -> browsing = false;
                    default -> System.out.println("Commande inconnue.");
                }
            } catch (Exception e) {
                System.out.println("❌ Erreur lecture catalogue : " + e.getMessage());
                browsing = false;
            }
        }
    }

    private static void displayGamesTable(JsonNode games) {
        System.out.printf("%-38s | %-25s | %-8s | %-10s | %-15s%n", "ID (UUID)", "TITRE", "PLATF.", "PRIX", "NOTE (AVIS)");
        System.out.println("---------------------------------------+---------------------------+----------+------------+----------------");

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
                String ratingStr = String.format("%.1f/5 (%d)", rating, reviewCount);

                String platform = "N/A";
                if (game.has("versions") && !game.get("versions").isEmpty()) {
                    platform = game.get("versions").fieldNames().next();
                }

                System.out.printf("%-38s | %-25s | %-8s | %-10s | %-15s%n", id, title, platform, price + " €", ratingStr);
            }
        }
    }

    // --- B. FICHE JEU DÉTAILLÉE (AVEC DLC & AVIS) ---
    private static void showGameDetailsPage() {
        System.out.print("Entrez l'ID du jeu (UUID) : ");
        String gameId = scanner.nextLine();

        System.out.println("\n⏳ Chargement...");
        String gameJson = getRequest(PLATFORM_URL + "/games/" + gameId);
        if (gameJson.startsWith("❌") || gameJson.contains("404")) {
            System.out.println("❌ Jeu introuvable.");
            return;
        }

        try {
            JsonNode g = mapper.readTree(gameJson);
            System.out.println("\n=======================================================");
            System.out.println("📀 " + g.get("title").asText().toUpperCase());
            System.out.println("=======================================================");
            System.out.println("🏢 Éditeur : " + g.get("publisherName").asText());
            System.out.println("💰 Prix    : " + g.get("currentPrice").asDouble() + " €");
            System.out.println("⭐ Note    : " + g.get("averageRating").asDouble() + "/5 (" + g.get("reviewCount").asInt() + " avis)");
            System.out.println("🎮 Versions: " + g.get("versions").toString());

            // 1. DLCs
            System.out.println("\n--- 📦 EXTENSIONS & DLCs ---");
            String dlcsJson = getRequest(PLATFORM_URL + "/games/" + gameId + "/dlcs");
            JsonNode dlcs = mapper.readTree(dlcsJson);
            if (dlcs.isEmpty()) System.out.println("   (Aucun DLC disponible)");
            else {
                for (JsonNode dlc : dlcs) {
                    System.out.printf("   🔹 [%s] %s  (%s €)%n", dlc.get("id").asText(), dlc.get("title").asText(), dlc.get("currentPrice").asText());
                }
            }

            // 2. PATCHS
            System.out.println("\n--- 🛠️ HISTORIQUE DES CORRECTIFS ---");
            String patchesJson = getRequest(PLATFORM_URL + "/games/" + gameId + "/patches");
            JsonNode patches = mapper.readTree(patchesJson);
            if (patches.isEmpty()) System.out.println("   (Aucun patch)");
            else {
                for (JsonNode p : patches) {
                    System.out.printf("   🔸 v%s : %s%n", p.get("version").asText(), p.get("description").asText());
                }
            }

            // 3. AVIS
            System.out.println("\n--- 🗣️ AVIS DES JOUEURS ---");
            // Appel au User Service pour les commentaires texte
            String reviewsJson = getRequest(USER_URL + "/users/rates/game/" + gameId);
            if (reviewsJson.startsWith("❌")) {
                System.out.println("   (Impossible de charger les commentaires)");
            } else {
                JsonNode reviews = mapper.readTree(reviewsJson);
                if (reviews.isEmpty()) System.out.println("   (Aucun avis écrit)");
                else {
                    for (JsonNode r : reviews) {
                        System.out.printf("   💬 %s/5 : \"%s\"%n", r.get("note").asText(), r.get("comment").asText());
                    }
                }
            }
            System.out.println("=======================================================\n");

        } catch (Exception e) {
            System.out.println("❌ Erreur d'affichage : " + e.getMessage());
        }
    }

    // --- C. AUTRES ACTIONS JOUEUR ---
    private static void buyGame(String userId) {
        System.out.print("ID du jeu (ou DLC) à acheter : ");
        String gameId = scanner.nextLine();

        // 1. VÉRIFICATION ET RÉCUPÉRATION DES INFOS (Platform Service)
        System.out.println("🔍 Vérification du jeu...");
        String gameJson = getRequest(PLATFORM_URL + "/games/" + gameId);

        if (gameJson.startsWith("❌") || gameJson.contains("404")) {
            System.out.println("❌ Impossible d'acheter : Jeu introuvable !");
            return;
        }

        try {
            JsonNode game = mapper.readTree(gameJson);
            String gameName = game.get("title").asText();
            JsonNode versions = game.get("versions"); // Contient {"PC": "1.0", "PS5": "1.0"}

            // 2. VÉRIFICATION DES PLATEFORMES DISPONIBLES
            if (versions.isEmpty()) {
                System.out.println("❌ Ce jeu n'est disponible sur aucune plateforme pour l'instant.");
                return;
            }

            // On liste les clés du JSON (les plateformes)
            List<String> availablePlatforms = new ArrayList<>();
            versions.fieldNames().forEachRemaining(availablePlatforms::add);

            String selectedPlatform = "";

            // 3. SÉLECTION SÉCURISÉE
            System.out.println("✅ Jeu trouvé : " + gameName);
            if (availablePlatforms.size() == 1) {
                // Une seule plateforme : on la sélectionne d'office
                selectedPlatform = availablePlatforms.get(0);
                System.out.println("ℹ️ Seule plateforme disponible : " + selectedPlatform);
            } else {
                // Plusieurs plateformes : l'utilisateur doit choisir
                System.out.println("Sur quelle plateforme le voulez-vous ?");
                for (int i = 0; i < availablePlatforms.size(); i++) {
                    System.out.println("   " + (i + 1) + ". " + availablePlatforms.get(i));
                }

                int choice = 0;
                while (choice < 1 || choice > availablePlatforms.size()) {
                    System.out.print("Votre choix (1-" + availablePlatforms.size() + ") : ");
                    try {
                        choice = Integer.parseInt(scanner.nextLine());
                    } catch (NumberFormatException e) { choice = 0; }
                }
                selectedPlatform = availablePlatforms.get(choice - 1);
            }

            // 4. GESTION DES DLC (Si besoin)
            boolean isDlc = game.has("isDlc") && game.get("isDlc").asBoolean();
            String parentId = "";
            if (isDlc) {
                if (game.has("parentGameId") && !game.get("parentGameId").isNull()) {
                    parentId = game.get("parentGameId").asText();
                    System.out.println("📦 C'est un DLC (Jeu parent ID : " + parentId + ")");
                } else {
                    // Fallback si le parentId manque dans le JSON
                    System.out.print("⚠️ ID du jeu de base requis pour ce DLC : ");
                    parentId = scanner.nextLine();
                }
            }

            // 5. ENVOI DE L'ACHAT AU USER SERVICE
            // Plus besoin de demander le prix ou le nom, on les a !
            double price = game.has("currentPrice") ? game.get("currentPrice").asDouble() : 59.99;

            String jsonBody = String.format(
                    "{\"gameId\":\"%s\", \"gameName\":\"%s\", \"platform\":\"%s\", \"price\":%s, \"isDlc\":%b, \"parentGameId\":\"%s\"}",
                    gameId, gameName, selectedPlatform, price, isDlc, parentId
            );

            System.out.println(postRequest(USER_URL + "/users/" + userId + "/buy", jsonBody));

        } catch (Exception e) {
            System.out.println("❌ Erreur lors du processus d'achat : " + e.getMessage());
        }
    }

    // --- DANS LE MENU JOUEUR (playerMenu) ---
    // Ajoutez l'option 6 :
    // System.out.println("6. 🎮 Jouer (Ajouter temps)");
    // case "6" -> playGame(userId);

    // --- NOUVELLE MÉTHODE : JOUER ---
    private static void playGame(String userId) {
        System.out.println("\n--- 🎮 SIMULATEUR DE JEU ---");
        // On affiche d'abord la bibliothèque pour que le joueur voit ses IDs
        showMyLibrary(userId);

        System.out.print("\nEntrez l'ID du jeu auquel vous avez joué (UUID) : ");
        String gameId = scanner.nextLine();

        System.out.print("Combien d'heures avez-vous joué ? (ex: 2.5) : ");
        String hours = scanner.nextLine();

        String json = String.format("{\"hoursToAdd\": %s}", hours);

        // Appel PUT au User Service
        System.out.println(putRequest(USER_URL + "/users/" + userId + "/library/" + gameId + "/playtime", json));
    }

    // --- REMPLACEMENT : BIBLIOTHÈQUE PROPRE ---
    private static void showMyLibrary(String userId) {
        System.out.println("\n⏳ Chargement de la bibliothèque...");
        String jsonResponse = getRequest(USER_URL + "/users/" + userId + "/library");

        if (jsonResponse.startsWith("❌")) {
            System.out.println(jsonResponse);
            return;
        }

        try {
            JsonNode games = mapper.readTree(jsonResponse);
            System.out.println("\n=== 📚 MA BIBLIOTHÈQUE ===");
            // En-tête du tableau
            System.out.printf("%-38s | %-30s | %-8s | %-12s%n", "ID DU JEU", "TITRE", "PLATF.", "TEMPS DE JEU");
            System.out.println("---------------------------------------+--------------------------------+----------+--------------");

            if (games.isEmpty()) {
                System.out.println("       (Vide. Allez acheter des jeux !)       ");
            } else {
                for (JsonNode item : games) {
                    String gId = item.get("gameId").asText();
                    String title = item.get("gameName").asText();
                    if (title.length() > 28) title = title.substring(0, 25) + "...";

                    String platform = item.get("platform").asText();

                    // Gestion du temps de jeu (peut être null ou 0)
                    double hours = item.has("playTimeHours") ? item.get("playTimeHours").asDouble() : 0.0;

                    System.out.printf("%-38s | %-30s | %-8s | %-10s%n",
                            gId, title, platform, String.format("%.1fh", hours));
                }
            }
            System.out.println("----------------------------------------------------------------------------------");
        } catch (Exception e) {
            System.out.println("❌ Erreur affichage bibliothèque : " + e.getMessage());
        }
    }

    // Ajout utilitaire PUT si vous ne l'avez pas
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
        } catch (Exception e) { return "❌ Exception: " + e.getMessage(); }
    }

    private static void rateGame(String userId) {
        System.out.print("ID du jeu (UUID) : "); String gameId = scanner.nextLine();
        System.out.print("Nom du jeu : "); String gameName = scanner.nextLine();
        System.out.print("Note (0-5) : "); String note = scanner.nextLine();
        System.out.print("Commentaire : "); String comment = scanner.nextLine();

        String json = String.format(
                "{\"gameId\":\"%s\", \"gameName\":\"%s\", \"note\":%s, \"comment\":\"%s\"}",
                gameId, gameName, note, comment
        );
        System.out.println(postRequest(USER_URL + "/users/" + userId + "/rate", json));
    }

    // --- ACTIONS ÉDITEUR ---
    private static void publishGame(String publisherId) {
        System.out.print("Titre du jeu : "); String title = scanner.nextLine();
        String json = String.format(
                "{\"title\":\"%s\", \"publisherId\":%s, \"platforms\":[\"PC\",\"PS5\"], \"price\":59.99}",
                title, publisherId
        );
        System.out.println(postRequest(EDITOR_URL + "/games", json));
    }

    private static void publishPatch() {
        System.out.print("ID du jeu (UUID) : "); String gameId = scanner.nextLine();
        System.out.print("Nouvelle version (ex: 1.1.0) : "); String version = scanner.nextLine();
        System.out.print("Note de patch : "); String notes = scanner.nextLine();

        String json = String.format(
                "{\"gameId\":\"%s\", \"version\":\"%s\", \"patchNotes\":\"%s\", \"platform\":\"PC\"}",
                gameId, version, notes
        );
        System.out.println(postRequest(EDITOR_URL + "/patches", json));
    }

    private static void showPublisherGames(String publisherId) {
        String json = getRequest(EDITOR_URL + "/publishers/" + publisherId + "/games");
        System.out.println(formatJson(json));
    }

    // =========================================================================
    // 4. UTILITAIRES HTTP & JSON
    // =========================================================================

    private static boolean checkUrlExists(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
            return client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
        } catch (Exception e) { return false; }
    }

    private static String getRequest(String uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return "❌ Erreur " + response.statusCode();
            return response.body();
        } catch (Exception e) { return "❌ Exception: " + e.getMessage(); }
    }

    private static String postRequest(String uri, String json) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) return "❌ Erreur " + response.statusCode() + " : " + response.body();
            return "✅ Succès : " + response.body();
        } catch (Exception e) { return "❌ Exception: " + e.getMessage(); }
    }

    private static String formatJson(String json) {
        if (json == null) return "";
        return json.replace("{", "\n  {").replace("},", "},\n").replace("[", "[\n").replace("]", "\n]");
    }
}