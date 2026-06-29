package com.banknet.controller;

import com.banknet.MainApp;
import com.banknet.dao.CompteDAO;
import com.banknet.dao.TransactionDAO;
import com.banknet.exception.SoldeInsuffisantException;
import com.banknet.model.*;
import com.banknet.service.AuthService;
import com.banknet.service.StatistiqueService;
import com.banknet.service.TransactionService;
import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.chrono.Chronology;
import java.time.chrono.HijrahChronology;
import java.time.chrono.IsoChronology;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardClientController {
    
    @FXML
    private Label userLabel;
    
    @FXML
    private Label soldeTotalLabel;
    
    @FXML
    private Label soldeCourantLabel;
    
    @FXML
    private Label soldeEpargneLabel;
    
    @FXML
    private AreaChart<String, Number> financeChart;
    
    @FXML
    private CategoryAxis dateAxis;
    
    @FXML
    private NumberAxis amountAxis;
    
    @FXML
    private TableView<Transaction> transactionTable;
    
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    
    @FXML
    private TableColumn<Transaction, String> montantColumn;
    
    @FXML
    private TableColumn<Transaction, String> libelleColumn;
    
    @FXML
    private TableColumn<Transaction, String> compteSourceColumn;
    
    @FXML
    private TableColumn<Transaction, String> compteDestColumn;
    
    @FXML
    private TableColumn<Transaction, String> soldeApresColumn;
    
    @FXML
    private DatePicker dateDebutPicker;
    
    @FXML
    private DatePicker dateFinPicker;
    
    @FXML
    private TextField montantMinField;
    
    @FXML
    private TextField montantMaxField;
    
    // Labels et boutons pour traduction
    @FXML
    private Label headerTitleLabel;
    
    @FXML
    private Button logoutButton;
    
    @FXML
    private Label soldeTotalTitleLabel;
    
    @FXML
    private Label soldeCourantTitleLabel;
    
    @FXML
    private Label soldeEpargneTitleLabel;
    
    @FXML
    private Label evolutionTitleLabel;
    
    @FXML
    private Label transferTitleLabel;
    
    @FXML
    private Label servicesTitleLabel;
    
    @FXML
    private Button internalTransferButton;
    
    @FXML
    private Button externalTransferButton;
    
    @FXML
    private Button refreshButton;
    
    @FXML
    private Button exportButton;
    
    @FXML
    private Button objectifsButton;
    
    @FXML
    private Button messagerieButton;
    
    @FXML
    private Button chatbotButton;
    
    @FXML
    private Button changePasswordButton;
    
    @FXML
    private Button notificationsButton;
    
    @FXML
    private Label notificationBadge;
    
    @FXML
    private Label filterTitleLabel;
    
    @FXML
    private Label startDateLabel;
    
    @FXML
    private Label endDateLabel;
    
    @FXML
    private Label minAmountLabel;
    
    @FXML
    private Label maxAmountLabel;
    
    @FXML
    private Button applyFilterButton;
    
    @FXML
    private Button resetFilterButton;
    
    @FXML
    private Label transactionsTitleLabel;
    
    private AuthService authService;
    private TransactionService transactionService;
    private StatistiqueService statistiqueService;
    private com.banknet.service.NotificationService notificationService;
    private LanguageManager languageManager;
    private ObservableList<Transaction> allTransactions;
    private ObservableList<Transaction> filteredTransactions;
    private List<Compte> clientComptes;
    
    public void setAuthService(AuthService authService) {
        this.authService = authService;
        this.transactionService = new TransactionService();
        this.statistiqueService = new StatistiqueService();
            // Initialiser les données après que tous les champs FXML sont prêts
            // Utiliser Platform.runLater pour s'assurer que l'UI est complètement initialisée
            javafx.application.Platform.runLater(() -> {
                try {
                    updateTranslations(); // Mettre à jour les traductions avant d'initialiser les données
                    initializeData();
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'initialisation des données: " + e.getMessage());
                    e.printStackTrace();
                }
            });
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("DashboardClientController.initialize() - Début");
            
            languageManager = LanguageManager.getInstance();
            
            allTransactions = FXCollections.observableArrayList();
            filteredTransactions = FXCollections.observableArrayList();
            System.out.println("Lists initialisées");
            
            // Configurer la politique de redimensionnement du tableau
            if (transactionTable != null) {
                transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                System.out.println("Politique de redimensionnement configurée");
            } else {
                System.err.println("ATTENTION: transactionTable est null dans initialize()");
            }
            
            setupTableColumns();
            System.out.println("Colonnes configurées");
            
            setupChart();
            System.out.println("Graphique configuré");
            
            // Charger les traductions
            updateTranslations();
            
            // Configurer les DatePicker avec la locale appropriée
            configureDatePickers();
            
            // Configurer le double-clic pour le mode plein écran
            setupFullscreenToggle();
            
            System.out.println("DashboardClientController.initialize() - Succès");
        } catch (Exception e) {
            System.err.println("=== ERREUR DANS initialize() ===");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTranslations() {
        if (languageManager == null) {
            languageManager = LanguageManager.getInstance();
        }
        
        // Mettre à jour la locale système pour JavaFX
        try {
            Locale.setDefault(languageManager.getCurrentLocale());
        } catch (Exception e) {
            System.err.println("Erreur lors de la définition de la locale: " + e.getMessage());
        }
        
        try {
            // Header
            if (headerTitleLabel != null) {
                headerTitleLabel.setText("🏦 BANKNET - " + languageManager.getTranslation("dashboard.title"));
            }
            if (logoutButton != null) {
                logoutButton.setText(languageManager.getTranslation("dashboard.logout"));
            }
            
            // Cartes de solde
            if (soldeTotalTitleLabel != null) {
                soldeTotalTitleLabel.setText(languageManager.getTranslation("dashboard.total"));
            }
            if (soldeCourantTitleLabel != null) {
                soldeCourantTitleLabel.setText(languageManager.getTranslation("dashboard.current"));
            }
            if (soldeEpargneTitleLabel != null) {
                soldeEpargneTitleLabel.setText(languageManager.getTranslation("dashboard.savings"));
            }
            
            // Graphique
            if (evolutionTitleLabel != null) {
                evolutionTitleLabel.setText(languageManager.getTranslation("dashboard.evolution"));
            }
            
            // Virements
            if (transferTitleLabel != null) {
                transferTitleLabel.setText(languageManager.getTranslation("dashboard.transfer"));
            }
            if (internalTransferButton != null) {
                internalTransferButton.setText("🔄 " + languageManager.getTranslation("dashboard.transfer.internal"));
            }
            if (externalTransferButton != null) {
                externalTransferButton.setText("💸 " + languageManager.getTranslation("dashboard.transfer.external"));
            }
            if (servicesTitleLabel != null) {
                servicesTitleLabel.setText(languageManager.getTranslation("dashboard.services"));
            }
            if (objectifsButton != null) {
                objectifsButton.setText("🎯 " + languageManager.getTranslation("dashboard.objectifs"));
            }
            if (messagerieButton != null) {
                messagerieButton.setText("💬 " + languageManager.getTranslation("dashboard.messagerie"));
            }
            if (chatbotButton != null) {
                chatbotButton.setText("🤖 " + languageManager.getTranslation("dashboard.chatbot"));
            }
            if (changePasswordButton != null) {
                changePasswordButton.setText("🔑 " + languageManager.getTranslation("dashboard.changePassword"));
            }
            if (refreshButton != null) {
                refreshButton.setText("🔄 " + languageManager.getTranslation("dashboard.refresh"));
            }
            if (exportButton != null) {
                exportButton.setText("📥 " + languageManager.getTranslation("dashboard.export"));
            }
            
            // Filtres
            if (filterTitleLabel != null) {
                filterTitleLabel.setText("🔍 " + languageManager.getTranslation("dashboard.filter"));
            }
            if (startDateLabel != null) {
                startDateLabel.setText(languageManager.getTranslation("dashboard.filter.start") + ":");
            }
            if (endDateLabel != null) {
                endDateLabel.setText(languageManager.getTranslation("dashboard.filter.end") + ":");
            }
            if (minAmountLabel != null) {
                minAmountLabel.setText(languageManager.getTranslation("dashboard.filter.min") + " (MAD):");
            }
            if (maxAmountLabel != null) {
                maxAmountLabel.setText(languageManager.getTranslation("dashboard.filter.max") + " (MAD):");
            }
            if (applyFilterButton != null) {
                applyFilterButton.setText("✓ " + languageManager.getTranslation("dashboard.filter.apply"));
            }
            if (resetFilterButton != null) {
                resetFilterButton.setText("↺ " + languageManager.getTranslation("dashboard.filter.reset"));
            }
            
            // Transactions
            if (transactionsTitleLabel != null) {
                transactionsTitleLabel.setText(languageManager.getTranslation("dashboard.transactions"));
            }
            
            // Colonnes du tableau
            if (dateColumn != null) {
                dateColumn.setText(languageManager.getTranslation("dashboard.table.date"));
            }
            if (typeColumn != null) {
                typeColumn.setText(languageManager.getTranslation("dashboard.table.type"));
            }
            if (montantColumn != null) {
                montantColumn.setText(languageManager.getTranslation("dashboard.table.amount"));
            }
            if (libelleColumn != null) {
                libelleColumn.setText(languageManager.getTranslation("dashboard.table.label"));
            }
            if (compteSourceColumn != null) {
                compteSourceColumn.setText(languageManager.getTranslation("dashboard.table.source"));
            }
            if (compteDestColumn != null) {
                compteDestColumn.setText(languageManager.getTranslation("dashboard.table.destination"));
            }
            if (soldeApresColumn != null) {
                soldeApresColumn.setText(languageManager.getTranslation("dashboard.table.balance"));
            }
            
            // Titre de la fenêtre
            Stage stage = (Stage) (headerTitleLabel != null && headerTitleLabel.getScene() != null ? 
                                  headerTitleLabel.getScene().getWindow() : null);
            if (stage != null) {
                stage.setTitle("BANKNET - " + languageManager.getTranslation("dashboard.title"));
            }
            
            // Mettre à jour le graphique et ses axes
            updateChartTitle();
            updateChartAxes();
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour des traductions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void configureDatePickers() {
        try {
            if (languageManager == null) {
                languageManager = LanguageManager.getInstance();
            }
            
            Locale locale = languageManager.getCurrentLocale();
            
            // Configurer les DatePicker avec la locale
            if (dateDebutPicker != null) {
                configureDatePickerLocale(dateDebutPicker, locale);
            }
            if (dateFinPicker != null) {
                configureDatePickerLocale(dateFinPicker, locale);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des DatePicker: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupFullscreenToggle() {
        // Ajouter le double-clic sur la scène pour activer/désactiver le mode plein écran
        if (headerTitleLabel != null && headerTitleLabel.getScene() != null) {
            headerTitleLabel.getScene().setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Stage stage = (Stage) headerTitleLabel.getScene().getWindow();
                    stage.setFullScreen(!stage.isFullScreen());
                }
            });
        }
    }
    
    private void configureDatePickerLocale(DatePicker datePicker, Locale locale) {
        try {
            // JavaFX utilise automatiquement la locale par défaut, mais on peut forcer
            // en définissant la locale système pour ce thread
            // Pour un meilleur contrôle, on utilise un StringConverter personnalisé
            datePicker.setConverter(new javafx.util.StringConverter<LocalDate>() {
                private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(getDatePattern(locale), locale);
                
                @Override
                public String toString(LocalDate date) {
                    if (date != null) {
                        return formatter.format(date);
                    } else {
                        return "";
                    }
                }
                
                @Override
                public LocalDate fromString(String string) {
                    if (string != null && !string.isEmpty()) {
                        try {
                            return LocalDate.parse(string, formatter);
                        } catch (Exception e) {
                            // Essayer avec le format ISO si le format personnalisé échoue
                            return LocalDate.parse(string);
                        }
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration du DatePicker: " + e.getMessage());
        }
    }
    
    private String getDatePattern(Locale locale) {
        String lang = locale.getLanguage();
        switch (lang) {
            case "ar":
                return "dd/MM/yyyy"; // Format pour l'arabe
            case "en":
                return "MM/dd/yyyy"; // Format américain
            case "fr":
            default:
                return "dd/MM/yyyy"; // Format européen
        }
    }
    
    private void setupTableColumns() {
        try {
            if (dateColumn == null || typeColumn == null || montantColumn == null || 
                libelleColumn == null || compteSourceColumn == null || 
                compteDestColumn == null || soldeApresColumn == null || 
                transactionTable == null) {
                System.err.println("DashboardClientController: Les colonnes de la table ne sont pas initialisées");
                return;
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            
            dateColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && t.getDate() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            t.getDate().format(formatter)
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            // Colonne Type avec icônes et couleurs
            typeColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        if (transaction != null) {
                            boolean isCredit = isCreditForClient(transaction);
                            LanguageManager lm = LanguageManager.getInstance();
                            if (isCredit) {
                                setText("⬇ " + lm.getTranslation("dashboard.table.credit"));
                                setStyle("-fx-text-fill: #059669; -fx-font-weight: 600;");
                            } else {
                                setText("⬆ " + lm.getTranslation("dashboard.table.debit"));
                                setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 600;");
                            }
                        } else {
                            setText("-");
                            setStyle("");
                        }
                    }
                }
            });
            
            typeColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && t.getType() != null) {
                        String translated = com.banknet.util.TransactionTypeTranslator.translate(t.getType(), languageManager);
                        return new javafx.beans.property.SimpleStringProperty(translated);
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            // Colonne Montant avec couleurs
            montantColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        Transaction transaction = getTableView().getItems().get(getIndex());
                        if (transaction != null && transaction.getMontant() != null) {
                            boolean isCredit = isCreditForClient(transaction);
                            String amountText = String.format("%.2f MAD", transaction.getMontant());
                            if (isCredit) {
                                setText("+" + amountText);
                                setStyle("-fx-text-fill: #059669; -fx-font-weight: 600;");
                            } else {
                                setText("-" + amountText);
                                setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 600;");
                            }
                        } else {
                            setText("-");
                            setStyle("");
                        }
                    }
                }
            });
            
            montantColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && t.getMontant() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            t.getMontant().toString() + " MAD"
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            libelleColumn.setCellValueFactory(new PropertyValueFactory<>("libelle"));
            
            libelleColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                    }
                }
            });
            
            compteSourceColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && t.getCompteSource() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            t.getCompteSource().getNumeroCompte() != null ? 
                                t.getCompteSource().getNumeroCompte() : "-"
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            compteSourceColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                    }
                }
            });
            
            compteDestColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && t.getCompteDest() != null) {
                        return new javafx.beans.property.SimpleStringProperty(
                            t.getCompteDest().getNumeroCompte() != null ? 
                                t.getCompteDest().getNumeroCompte() : "-"
                        );
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            compteDestColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                    }
                }
            });
            
            soldeApresColumn.setCellValueFactory(cellData -> {
                try {
                    Transaction t = cellData.getValue();
                    if (t != null && clientComptes != null) {
                        Compte compte = getAffectedAccount(t);
                        if (compte != null && compte.getSolde() != null) {
                            BigDecimal solde = calculateSoldeAfterTransaction(compte, t);
                            return new javafx.beans.property.SimpleStringProperty(
                                String.format("%.2f MAD", solde)
                            );
                        }
                    }
                    return new javafx.beans.property.SimpleStringProperty("-");
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("-");
                }
            });
            
            soldeApresColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px; -fx-font-weight: 500;");
                    }
                }
            });
            
            dateColumn.setCellFactory(column -> new TableCell<Transaction, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setStyle("-fx-font-size: 14px; -fx-padding: 12px 15px;");
                    }
                }
            });
            
            // Configurer la hauteur des lignes
            transactionTable.setRowFactory(tv -> {
                javafx.scene.control.TableRow<Transaction> row = new javafx.scene.control.TableRow<>();
                row.setStyle("-fx-min-height: 40px; -fx-pref-height: 40px;");
                return row;
            });
            
            transactionTable.setItems(filteredTransactions);
        } catch (Exception e) {
            System.err.println("Erreur dans setupTableColumns: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Compte getAffectedAccount(Transaction transaction) {
        if (transaction == null || clientComptes == null || clientComptes.isEmpty()) {
            return null;
        }
        
        try {
            if (transaction.getCompteSource() != null && transaction.getCompteSource().getId() != null) {
                Long sourceId = transaction.getCompteSource().getId();
                if (clientComptes.stream().anyMatch(c -> c != null && c.getId() != null && c.getId().equals(sourceId))) {
                    return transaction.getCompteSource();
                }
            }
            if (transaction.getCompteDest() != null && transaction.getCompteDest().getId() != null) {
                Long destId = transaction.getCompteDest().getId();
                if (clientComptes.stream().anyMatch(c -> c != null && c.getId() != null && c.getId().equals(destId))) {
                    return transaction.getCompteDest();
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur dans getAffectedAccount: " + e.getMessage());
        }
        return null;
    }
    
    private BigDecimal calculateSoldeAfterTransaction(Compte compte, Transaction transaction) {
        // Pour simplifier, on retourne le solde actuel
        // Dans une vraie application, on calculerait le solde au moment de la transaction
        return compte.getSolde();
    }
    
    /**
     * Détermine si une transaction est un crédit pour le client actuel
     * (l'argent entre dans son compte)
     */
    private boolean isCreditForClient(Transaction transaction) {
        if (transaction == null || clientComptes == null || clientComptes.isEmpty()) {
            return false;
        }
        
        try {
            // Si le compte destination appartient au client, c'est un crédit
            if (transaction.getCompteDest() != null && transaction.getCompteDest().getId() != null) {
                Long destId = transaction.getCompteDest().getId();
                boolean isClientAccount = clientComptes.stream()
                    .anyMatch(c -> c != null && c.getId() != null && c.getId().equals(destId));
                if (isClientAccount) {
                    return true;
                }
            }
            // Si le compte source appartient au client, c'est un débit
            return false;
        } catch (Exception e) {
            System.err.println("Erreur dans isCreditForClient: " + e.getMessage());
            return false;
        }
    }
    
    private void setupChart() {
        try {
            if (financeChart == null) {
                System.err.println("DashboardClientController: Le graphique n'est pas initialisé");
                return;
            }
            
            financeChart.setAnimated(true); // Animations fluides activées
            financeChart.setLegendVisible(true);
            financeChart.setCreateSymbols(true); // Activer les symboles
            
            // Supprimer les lignes de grille
            financeChart.setHorizontalGridLinesVisible(false);
            financeChart.setVerticalGridLinesVisible(false);
            
            // Configurer les axes avec police moderne
            if (dateAxis != null) {
                dateAxis.setAnimated(false);
                dateAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 12));
                // Traduire l'axe X si nécessaire (les dates sont déjà formatées)
                // Rotation des labels de date pour éviter le chevauchement si nécessaire
                dateAxis.setTickLabelRotation(0);
            }
            
            if (amountAxis != null) {
                amountAxis.setAnimated(false);
                amountAxis.setTickLabelFont(javafx.scene.text.Font.font("Segoe UI", 12));
                // Traduire l'axe Y
                if (languageManager != null) {
                    amountAxis.setLabel(languageManager.getTranslation("chart.axis.amount"));
                } else {
                    amountAxis.setLabel("Montant (MAD)");
                }
                // Formatter pour afficher les montants de manière lisible
                amountAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                    @Override
                    public String toString(Number object) {
                        if (object.doubleValue() >= 1000) {
                            return String.format("%.1fk", object.doubleValue() / 1000);
                        }
                        return String.format("%.0f", object.doubleValue());
                    }
                    
                    @Override
                    public Number fromString(String string) {
                        return Double.parseDouble(string);
                    }
                });
            }
            
            // Le titre sera défini dans updateTranslations()
            updateChartTitle();
        } catch (Exception e) {
            System.err.println("Erreur dans setupChart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateChartTitle() {
        if (financeChart != null && languageManager != null) {
            financeChart.setTitle(languageManager.getTranslation("dashboard.evolution"));
        } else if (financeChart != null) {
            financeChart.setTitle("Évolution des Finances");
        }
    }
    
    private void updateChartAxes() {
        if (amountAxis != null && languageManager != null) {
            amountAxis.setLabel(languageManager.getTranslation("chart.axis.amount"));
        }
    }
    
    private void initializeData() {
        try {
            if (authService == null || authService.getCurrentUser() == null) {
                System.err.println("DashboardClientController: authService ou currentUser est null");
                return;
            }
            
            // Vérifier que les champs FXML sont initialisés
            if (userLabel == null || soldeTotalLabel == null) {
                System.err.println("DashboardClientController: Les champs FXML ne sont pas initialisés");
                return;
            }
            
            // Afficher l'utilisateur connecté
            UserAccount currentUser = authService.getCurrentUser();
            Client client = currentUser.getClient();
            
            if (client == null) {
                userLabel.setText("Connecté : " + currentUser.getLogin() + " (Client non associé)");
                System.err.println("DashboardClientController: Le client est null pour l'utilisateur " + currentUser.getLogin());
                // Initialiser avec des valeurs par défaut
                soldeTotalLabel.setText("0.00 MAD");
                soldeCourantLabel.setText("0.00 MAD");
                soldeEpargneLabel.setText("0.00 MAD");
                return;
            }
            
            String connectedText = languageManager != null ? 
                languageManager.getTranslation("dashboard.connected") : "Connecté";
            userLabel.setText(connectedText + " : " + client.getNomComplet() + " (" + currentUser.getLogin() + ")");
            
            // Charger les comptes du client
            try {
                clientComptes = new CompteDAO().findByClientId(client.getId());
                
                if (clientComptes == null) {
                    clientComptes = new java.util.ArrayList<>();
                }
                
                // Actualiser les données
                refreshStatistics();
                refreshTransactions();
                // Mettre à jour le graphique seulement si financeChart est initialisé
                if (financeChart != null) {
                    updateChart();
                } else {
                    System.err.println("DashboardClientController: financeChart est null, impossible de mettre à jour le graphique");
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du chargement des comptes: " + e.getMessage());
                e.printStackTrace();
                // Afficher des valeurs par défaut en cas d'erreur
                soldeTotalLabel.setText("0.00 MAD");
                soldeCourantLabel.setText("0.00 MAD");
                soldeEpargneLabel.setText("0.00 MAD");
            }
        } catch (Exception e) {
            System.err.println("Erreur dans initializeData: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshStatistics() {
        try {
            if (soldeTotalLabel == null || soldeCourantLabel == null || soldeEpargneLabel == null) {
                System.err.println("DashboardClientController: Les labels de solde ne sont pas initialisés");
                return;
            }
            
            if (clientComptes == null || clientComptes.isEmpty()) {
                soldeTotalLabel.setText("0.00 MAD");
                soldeCourantLabel.setText("0.00 MAD");
                soldeEpargneLabel.setText("0.00 MAD");
                return;
            }
            
            BigDecimal soldeTotal = clientComptes.stream()
                .filter(c -> c != null && c.getSolde() != null)
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal soldeCourant = clientComptes.stream()
                .filter(c -> c != null && c.getType() == TypeCompte.COURANT && c.getSolde() != null)
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal soldeEpargne = clientComptes.stream()
                .filter(c -> c != null && c.getType() == TypeCompte.EPARGNE && c.getSolde() != null)
                .map(Compte::getSolde)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            soldeTotalLabel.setText(String.format("%.2f MAD", soldeTotal));
            soldeCourantLabel.setText(String.format("%.2f MAD", soldeCourant));
            soldeEpargneLabel.setText(String.format("%.2f MAD", soldeEpargne));
            
            // Recharger les comptes pour avoir les soldes à jour
            try {
                clientComptes = clientComptes.stream()
                    .filter(c -> c != null && c.getId() != null)
                    .map(c -> new CompteDAO().findById(c.getId()))
                    .filter(c -> c != null)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("Erreur lors du rechargement des comptes: " + e.getMessage());
                // Continuer avec les comptes existants
            }
        } catch (Exception e) {
            System.err.println("Erreur dans refreshStatistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshTransactions() {
        try {
            if (clientComptes == null || clientComptes.isEmpty()) {
                System.out.println("refreshTransactions: Aucun compte client, vidage des transactions");
                allTransactions.clear();
                filteredTransactions.clear();
                return;
            }
            
            System.out.println("refreshTransactions: Début - " + clientComptes.size() + " compte(s) à vérifier");
            
            allTransactions.clear();
            
            // Utiliser un Set pour collecter les IDs déjà vus et un Map pour stocker les transactions uniques
            // Cela évite les doublons quand une transaction apparaît pour plusieurs comptes
            java.util.Set<Long> seenIds = new java.util.HashSet<>();
            java.util.List<Transaction> uniqueTransactionsList = new java.util.ArrayList<>();
            
            // Récupérer toutes les transactions des comptes du client
            for (Compte compte : clientComptes) {
                if (compte == null || compte.getId() == null) {
                    continue;
                }
                System.out.println("refreshTransactions: Recherche transactions pour compte ID=" + compte.getId());
                List<Transaction> transactions = statistiqueService.getTransactionsParCompte(compte.getId());
                System.out.println("refreshTransactions: Trouvé " + transactions.size() + " transaction(s) pour compte " + compte.getId());
                
                for (Transaction transaction : transactions) {
                    // Vérifier que la transaction est valide et qu'on ne l'a pas déjà ajoutée
                    if (transaction != null && transaction.getId() != null) {
                        if (!seenIds.contains(transaction.getId())) {
                            seenIds.add(transaction.getId());
                            uniqueTransactionsList.add(transaction);
                            System.out.println("refreshTransactions: Transaction ajoutée - ID=" + transaction.getId() + 
                                ", Date=" + transaction.getDate() + ", Montant=" + transaction.getMontant());
                        } else {
                            System.out.println("refreshTransactions: Transaction dupliquée ignorée - ID=" + transaction.getId());
                        }
                    }
                }
            }
            
            // Trier par date décroissante
            uniqueTransactionsList.sort(Comparator.comparing(Transaction::getDate).reversed());
            
            // Ajouter à la liste observable
            allTransactions.addAll(uniqueTransactionsList);
            
            System.out.println("refreshTransactions: " + allTransactions.size() + " transaction(s) unique(s) chargée(s)");
            if (!allTransactions.isEmpty()) {
                System.out.println("  Première transaction: ID=" + allTransactions.get(0).getId() + 
                    ", Date=" + allTransactions.get(0).getDate() + 
                    ", Montant=" + allTransactions.get(0).getMontant());
            }
            
            // Appliquer les filtres
            applyFilters();
            
            System.out.println("refreshTransactions: " + filteredTransactions.size() + " transaction(s) après filtrage");
            
            // Forcer la mise à jour du tableau sur le thread JavaFX
            javafx.application.Platform.runLater(() -> {
                if (transactionTable != null) {
                    System.out.println("refreshTransactions: Mise à jour du tableau - " + filteredTransactions.size() + " élément(s)");
                    transactionTable.refresh();
                }
            });
        } catch (Exception e) {
            System.err.println("Erreur dans refreshTransactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateChart() {
        try {
            if (financeChart == null) {
                System.err.println("DashboardClientController: financeChart est null dans updateChart()");
                return;
            }
            
            financeChart.getData().clear();
            
            if (clientComptes == null || clientComptes.isEmpty()) {
                // Afficher le solde actuel même sans transactions
                BigDecimal soldeActuel = BigDecimal.ZERO;
                XYChart.Series<String, Number> soldeSeries = new XYChart.Series<>();
                soldeSeries.setName(languageManager != null ? 
                    languageManager.getTranslation("dashboard.current") : "Compte Courant");
                soldeSeries.getData().add(new XYChart.Data<>(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    soldeActuel.doubleValue()
                ));
                financeChart.getData().add(soldeSeries);
                return;
            }
            
            // Trouver le compte courant du client
            Compte compteCourant = clientComptes.stream()
                .filter(c -> c.getType() == TypeCompte.COURANT)
                .findFirst()
                .orElse(null);
            
            if (compteCourant == null) {
                // Pas de compte courant, afficher zéro
                XYChart.Series<String, Number> soldeSeries = new XYChart.Series<>();
                soldeSeries.setName(languageManager != null ? 
                    languageManager.getTranslation("dashboard.current") : "Compte Courant");
                soldeSeries.getData().add(new XYChart.Data<>(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    0.0
                ));
                financeChart.getData().add(soldeSeries);
                return;
            }
            
            // Solde actuel du compte courant
            BigDecimal soldeActuelCourant = compteCourant.getSolde();
            
            // Grouper les transactions par date et calculer le solde cumulé
            XYChart.Series<String, Number> soldeSeries = new XYChart.Series<>();
            soldeSeries.setName(languageManager != null ? 
                languageManager.getTranslation("dashboard.current") : "Compte Courant");
            
            // Filtrer uniquement les transactions concernant le compte courant
            List<Transaction> transactionsCourant = allTransactions.stream()
                .filter(t -> (t.getCompteSource() != null && t.getCompteSource().getId().equals(compteCourant.getId())) ||
                            (t.getCompteDest() != null && t.getCompteDest().getId().equals(compteCourant.getId())))
                .sorted(Comparator.comparing(Transaction::getDate))
                .collect(Collectors.toList());
            
            // Si pas de transactions, afficher juste le solde actuel
            if (transactionsCourant.isEmpty()) {
                soldeSeries.getData().add(new XYChart.Data<>(
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    soldeActuelCourant.doubleValue()
                ));
                financeChart.getData().add(soldeSeries);
                return;
            }
            
            // Calculer le solde initial en remontant dans le temps (inverser l'effet des transactions)
            BigDecimal soldeInitial = soldeActuelCourant;
            for (Transaction t : transactionsCourant) {
                // Pour trouver le solde initial, on inverse l'effet de chaque transaction
                if (t.getCompteDest() != null && t.getCompteDest().getId().equals(compteCourant.getId())) {
                    // Transfert VERS le compte courant → crédit entrant → pour remonter, on soustrait
                    soldeInitial = soldeInitial.subtract(t.getMontant());
                } else if (t.getCompteSource() != null && t.getCompteSource().getId().equals(compteCourant.getId())) {
                    // Transfert DEPUIS le compte courant → débit sortant → pour remonter, on ajoute
                    soldeInitial = soldeInitial.add(t.getMontant());
                }
            }
            
            // Reconstruire l'évolution du solde du compte courant en avançant dans le temps
            BigDecimal soldeCumule = soldeInitial;
            String currentDate = "";
            
            for (Transaction t : transactionsCourant) {
                String dateKey = t.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                
                // Appliquer l'effet normal de chaque transaction sur le solde du compte courant
                if (t.getCompteDest() != null && t.getCompteDest().getId().equals(compteCourant.getId())) {
                    // Transfert VERS le compte courant → crédit entrant → le solde AUGMENTE
                    soldeCumule = soldeCumule.add(t.getMontant());
                } else if (t.getCompteSource() != null && t.getCompteSource().getId().equals(compteCourant.getId())) {
                    // Transfert DEPUIS le compte courant → débit sortant → le solde DIMINUE
                    soldeCumule = soldeCumule.subtract(t.getMontant());
                }
                
                if (!dateKey.equals(currentDate)) {
                    soldeSeries.getData().add(new XYChart.Data<>(dateKey, soldeCumule.doubleValue()));
                    currentDate = dateKey;
                } else {
                    // Mettre à jour le dernier point de cette date
                    if (!soldeSeries.getData().isEmpty()) {
                        soldeSeries.getData().get(soldeSeries.getData().size() - 1)
                            .setYValue(soldeCumule.doubleValue());
                    }
                }
            }
            
            // Ajouter le solde actuel du compte courant à la fin
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            if (!today.equals(currentDate)) {
                soldeSeries.getData().add(new XYChart.Data<>(today, soldeActuelCourant.doubleValue()));
            }
            
            financeChart.getData().add(soldeSeries);
            
        } catch (Exception e) {
            System.err.println("Erreur dans updateChart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void handleRefresh() {
        refreshStatistics();
        refreshTransactions();
        updateChart();
    }
    
    @FXML
    private void handleApplyFilters() {
        applyFilters();
    }
    
    private void applyFilters() {
        try {
            filteredTransactions.clear();
            
            if (allTransactions == null || allTransactions.isEmpty()) {
                System.out.println("applyFilters: Aucune transaction à filtrer");
                return;
            }
            
            LocalDate dateDebut = dateDebutPicker != null ? dateDebutPicker.getValue() : null;
            LocalDate dateFin = dateFinPicker != null ? dateFinPicker.getValue() : null;
            
            BigDecimal montantMin = null;
            BigDecimal montantMax = null;
            
            try {
                if (montantMinField != null && !montantMinField.getText().trim().isEmpty()) {
                    montantMin = new BigDecimal(montantMinField.getText().trim());
                }
            } catch (NumberFormatException e) {
                System.err.println("applyFilters: Montant minimum invalide");
                if (montantMinField != null) {
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                        languageManager != null ? languageManager.getTranslation("error.invalid.amount.min") : "Montant minimum invalide", 
                        Alert.AlertType.ERROR);
                }
                return;
            }
            
            try {
                if (montantMaxField != null && !montantMaxField.getText().trim().isEmpty()) {
                    montantMax = new BigDecimal(montantMaxField.getText().trim());
                }
            } catch (NumberFormatException e) {
                System.err.println("applyFilters: Montant maximum invalide");
                if (montantMaxField != null) {
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                        languageManager != null ? languageManager.getTranslation("error.invalid.amount.max") : "Montant maximum invalide", 
                        Alert.AlertType.ERROR);
                }
                return;
            }
            
            final LocalDate dateDebutFinal = dateDebut;
            final LocalDate dateFinFinal = dateFin;
            final BigDecimal montantMinFinal = montantMin;
            final BigDecimal montantMaxFinal = montantMax;
            
            List<Transaction> filtered = allTransactions.stream()
                .filter(t -> {
                    if (t == null) return false;
                    if (dateDebutFinal != null && t.getDate().toLocalDate().isBefore(dateDebutFinal)) {
                        return false;
                    }
                    if (dateFinFinal != null && t.getDate().toLocalDate().isAfter(dateFinFinal)) {
                        return false;
                    }
                    if (montantMinFinal != null && t.getMontant().compareTo(montantMinFinal) < 0) {
                        return false;
                    }
                    if (montantMaxFinal != null && t.getMontant().compareTo(montantMaxFinal) > 0) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            filteredTransactions.addAll(filtered);
            System.out.println("applyFilters: " + filtered.size() + " transaction(s) après filtrage sur " + allTransactions.size() + " total");
        } catch (Exception e) {
            System.err.println("Erreur dans applyFilters: " + e.getMessage());
            e.printStackTrace();
            // En cas d'erreur, afficher toutes les transactions
            filteredTransactions.clear();
            filteredTransactions.addAll(allTransactions);
        }
    }
    
    @FXML
    private void handleResetFilters() {
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        montantMinField.clear();
        montantMaxField.clear();
        filteredTransactions.clear();
        filteredTransactions.addAll(allTransactions);
    }
    
    @FXML
    private void handleVirementInterne() {
        try {
            System.out.println("handleVirementInterne() - Début");
            java.net.URL resource = MainApp.class.getResource("/com/banknet/view/VirementInterneView.fxml");
            if (resource == null) {
                throw new IOException("Le fichier VirementInterneView.fxml est introuvable");
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Stage virementStage = new Stage();
            Scene scene = new Scene(loader.load());
            virementStage.setScene(scene);
            virementStage.setTitle(languageManager != null ? languageManager.getTranslation("transfer.internal.title") : "Virement Interne");
            virementStage.setResizable(true);
            virementStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            virementStage.initOwner(transactionTable.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            scene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    virementStage.setFullScreen(!virementStage.isFullScreen());
                }
            });
            
            VirementInterneController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Le contrôleur VirementInterneController n'a pas pu être chargé");
            }
            controller.setAuthService(authService);
            
            System.out.println("handleVirementInterne() - Affichage de la fenêtre");
            virementStage.showAndWait();
            
            // Rafraîchir après fermeture
            handleRefresh();
        } catch (IOException e) {
            System.err.println("Erreur IO dans handleVirementInterne: " + e.getMessage());
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.load") : "Erreur lors du chargement") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Erreur inattendue dans handleVirementInterne: " + e.getMessage());
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.generic") : "Une erreur s'est produite") + " : " + e.getClass().getSimpleName() + " - " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleVirementTiers() {
        try {
            System.out.println("handleVirementTiers() - Début");
            java.net.URL resource = MainApp.class.getResource("/com/banknet/view/VirementTiersView.fxml");
            if (resource == null) {
                throw new IOException("Le fichier VirementTiersView.fxml est introuvable");
            }
            
            FXMLLoader loader = new FXMLLoader(resource);
            Stage virementStage = new Stage();
            Scene scene = new Scene(loader.load());
            virementStage.setScene(scene);
            virementStage.setTitle(languageManager != null ? languageManager.getTranslation("transfer.external.title") : "Virement vers Tiers");
            virementStage.setResizable(true);
            virementStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            virementStage.initOwner(transactionTable.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            scene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    virementStage.setFullScreen(!virementStage.isFullScreen());
                }
            });
            
            VirementTiersController controller = loader.getController();
            if (controller == null) {
                throw new IOException("Le contrôleur VirementTiersController n'a pas pu être chargé");
            }
            controller.setAuthService(authService);
            
            System.out.println("handleVirementTiers() - Affichage de la fenêtre");
            virementStage.showAndWait();
            
            // Rafraîchir après fermeture
            handleRefresh();
        } catch (IOException e) {
            System.err.println("Erreur IO dans handleVirementTiers: " + e.getMessage());
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.load") : "Erreur lors du chargement") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        } catch (Exception e) {
            System.err.println("Erreur inattendue dans handleVirementTiers: " + e.getMessage());
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.generic") : "Une erreur s'est produite") + " : " + e.getClass().getSimpleName() + " - " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleNotifications() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/NotificationsView.fxml"));
            Stage notificationsStage = new Stage();
            Scene notificationsScene = new Scene(loader.load());
            notificationsStage.setScene(notificationsScene);
            notificationsStage.setTitle("Centre de notifications");
            notificationsStage.setResizable(true);
            notificationsStage.setWidth(700);
            notificationsStage.setHeight(600);
            notificationsStage.initOwner(notificationsButton.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            notificationsScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    notificationsStage.setFullScreen(!notificationsStage.isFullScreen());
                }
            });
            
            NotificationsController controller = loader.getController();
            controller.setAuthService(authService);
            
            notificationsStage.showAndWait();
            
            // Rafraîchir le badge après fermeture
            updateNotificationBadge();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("notification.error") : "Impossible d'ouvrir le centre de notifications") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    private void updateNotificationBadge() {
        if (authService != null && authService.getCurrentUser() != null && authService.getCurrentUser().getClient() != null) {
            long count = notificationService.countNotificationsNonLues(authService.getCurrentUser().getClient().getId());
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        }
    }
    
    @FXML
    private void handleMessagerie() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/MessagerieView.fxml"));
            Stage messagerieStage = new Stage();
            Scene messagerieScene = new Scene(loader.load());
            messagerieStage.setScene(messagerieScene);
            messagerieStage.setTitle(languageManager != null ? languageManager.getTranslation("messagerie.title") : "Messagerie sécurisée");
            messagerieStage.setResizable(true);
            messagerieStage.setWidth(900);
            messagerieStage.setHeight(700);
            messagerieStage.initOwner(messagerieButton.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            messagerieScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    messagerieStage.setFullScreen(!messagerieStage.isFullScreen());
                }
            });
            
            MessagerieController controller = loader.getController();
            controller.setAuthService(authService);
            
            messagerieStage.showAndWait();
            
            // Rafraîchir le badge de notifications après fermeture
            updateNotificationBadge();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("messagerie.error") : "Impossible d'ouvrir la messagerie") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleChatbot() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ChatbotView.fxml"));
            Stage chatbotStage = new Stage();
            Scene chatbotScene = new Scene(loader.load());
            chatbotStage.setScene(chatbotScene);
            chatbotStage.setTitle(languageManager != null ? languageManager.getTranslation("chatbot.title") : "Chatbot bancaire");
            chatbotStage.setResizable(true);
            chatbotStage.setWidth(600);
            chatbotStage.setHeight(700);
            chatbotStage.initOwner(chatbotButton.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            chatbotScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    chatbotStage.setFullScreen(!chatbotStage.isFullScreen());
                }
            });
            
            chatbotStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("chatbot.error") : "Impossible d'ouvrir le chatbot") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleObjectifs() {
        try {
            // Vérifier si le client a un compte d'épargne
            if (authService == null || authService.getCurrentUser() == null || authService.getCurrentUser().getClient() == null) {
                showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                    languageManager != null ? languageManager.getTranslation("error.client.not.found") : "Impossible d'accéder aux informations du client", 
                    Alert.AlertType.ERROR);
                return;
            }
            
            Client client = authService.getCurrentUser().getClient();
            CompteDAO compteDAO = new CompteDAO();
            List<Compte> comptes = compteDAO.findByClientId(client.getId());
            
            // Chercher un compte d'épargne
            Compte compteEpargne = comptes.stream()
                .filter(c -> c.getType() == TypeCompte.EPARGNE)
                .findFirst()
                .orElse(null);
            
            if (compteEpargne == null) {
                showAlert(languageManager != null ? languageManager.getTranslation("alert.info") : "Information", 
                    languageManager != null ? languageManager.getTranslation("objectif.error.no.savings") : "Vous n'avez pas de compte d'épargne. Veuillez en créer un d'abord.", 
                    Alert.AlertType.INFORMATION);
                return;
            }
            
            // Afficher un dialogue de choix avec deux options
            Alert choiceAlert = new Alert(Alert.AlertType.NONE);
            choiceAlert.setTitle(languageManager != null ? languageManager.getTranslation("objectif.title") : "Objectifs d'épargne");
            choiceAlert.setHeaderText(languageManager != null ? languageManager.getTranslation("objectif.what") : "Que souhaitez-vous faire ?");
            choiceAlert.setContentText(languageManager != null ? languageManager.getTranslation("objectif.choose.action") : "Choisissez une action :");
            
            ButtonType voirObjectifsButton = new ButtonType("🎯 " + (languageManager != null ? languageManager.getTranslation("objectif.view.goals") : "Voir mes objectifs"));
            ButtonType ajouterObjectifButton = new ButtonType("➕ " + (languageManager != null ? languageManager.getTranslation("objectif.add.goal") : "Ajouter un objectif"));
            ButtonType cancelButton = new ButtonType(languageManager != null ? languageManager.getTranslation("objectif.cancel") : "Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            choiceAlert.getButtonTypes().setAll(voirObjectifsButton, ajouterObjectifButton, cancelButton);
            
            java.util.Optional<ButtonType> choiceResult = choiceAlert.showAndWait();
            
            if (choiceResult.isPresent()) {
                if (choiceResult.get() == voirObjectifsButton) {
                    // Ouvrir la vue des objectifs
                    openObjectifsView();
                } else if (choiceResult.get() == ajouterObjectifButton) {
                    // Créer un nouvel objectif
                    handleAddObjectif(client, compteEpargne);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.generic") : "Une erreur s'est produite") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    private void handleAddObjectif(Client client, Compte compteEpargne) {
        try {
            // Créer un dialogue pour saisir le montant cible
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(languageManager != null ? languageManager.getTranslation("objectif.new.title") : "Nouvel objectif d'épargne");
            dialog.setHeaderText(languageManager != null ? languageManager.getTranslation("objectif.define") : "Définir un objectif d'épargne");
            dialog.setContentText(languageManager != null ? languageManager.getTranslation("objectif.amount") + " :" : "Montant cible (MAD) :");
            dialog.getEditor().setPromptText("Ex: 50000");
            
            // Afficher le dialogue et obtenir le résultat
            java.util.Optional<String> result = dialog.showAndWait();
            
            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try {
                    BigDecimal montantCible = new BigDecimal(result.get().trim());
                    
                    if (montantCible.compareTo(BigDecimal.ZERO) <= 0) {
                        showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                            languageManager != null ? languageManager.getTranslation("objectif.error.invalid.amount") : "Le montant doit être supérieur à zéro", 
                            Alert.AlertType.ERROR);
                        return;
                    }
                    
                    // Créer un dialogue pour le libellé
                    TextInputDialog libelleDialog = new TextInputDialog("Épargne");
                    libelleDialog.setTitle(languageManager != null ? languageManager.getTranslation("objectif.label") : "Libellé de l'objectif");
                    libelleDialog.setHeaderText(languageManager != null ? languageManager.getTranslation("objectif.label.name") : "Donnez un nom à votre objectif");
                    libelleDialog.setContentText((languageManager != null ? languageManager.getTranslation("dashboard.table.label") : "Libellé") + " :");
                    libelleDialog.getEditor().setPromptText(languageManager != null ? languageManager.getTranslation("objectif.label.placeholder") : "Ex: Vacances, Maison, etc.");
                    
                    java.util.Optional<String> libelleResult = libelleDialog.showAndWait();
                    String libelle = libelleResult.isPresent() && !libelleResult.get().trim().isEmpty() 
                        ? libelleResult.get().trim() 
                        : "Épargne";
                    
                    // Créer l'objectif avec une date cible par défaut (6 mois)
                    com.banknet.service.ObjectifEpargneService objectifService = new com.banknet.service.ObjectifEpargneService();
                    objectifService.creerObjectif(
                        client,
                        compteEpargne.getId(),
                        libelle,
                        montantCible,
                        java.time.LocalDate.now().plusMonths(6)
                    );
                    
                    // Afficher un dialogue de confirmation avec un bouton pour voir les objectifs
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle(languageManager != null ? languageManager.getTranslation("objectif.created") : "Objectif créé");
                    successAlert.setHeaderText(languageManager != null ? languageManager.getTranslation("alert.success") : "Succès");
                    successAlert.setContentText(String.format((languageManager != null ? languageManager.getTranslation("objectif.created.success") : "Objectif d'épargne créé avec succès") + " :\n%s - %s MAD\n\n" + (languageManager != null ? languageManager.getTranslation("objectif.target.date") : "Date cible") + " : %s", 
                        libelle, montantCible, java.time.LocalDate.now().plusMonths(6)));
                    
                    ButtonType voirObjectifsButton = new ButtonType(languageManager != null ? languageManager.getTranslation("objectif.view.goals") : "Voir mes objectifs");
                    ButtonType okButton = new ButtonType(languageManager != null ? languageManager.getTranslation("objectif.ok") : "OK", ButtonBar.ButtonData.CANCEL_CLOSE);
                    successAlert.getButtonTypes().setAll(voirObjectifsButton, okButton);
                    
                    java.util.Optional<ButtonType> buttonResult = successAlert.showAndWait();
                    if (buttonResult.isPresent() && buttonResult.get() == voirObjectifsButton) {
                        // Ouvrir la vue des objectifs
                        openObjectifsView();
                    }
                    
                } catch (NumberFormatException e) {
                    showAlert("Erreur de saisie", "Veuillez entrer un montant valide.", Alert.AlertType.ERROR);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                        (languageManager != null ? languageManager.getTranslation("objectif.error.create") : "Erreur lors de la création de l'objectif") + " : " + e.getMessage(), 
                        Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("error.generic") : "Une erreur s'est produite") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    private void openObjectifsView() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ObjectifsEpargneView.fxml"));
            Stage objectifsStage = new Stage();
            Scene objectifsScene = new Scene(loader.load());
            objectifsStage.setScene(objectifsScene);
            objectifsStage.setTitle(languageManager != null ? languageManager.getTranslation("objectif.title") : "Objectifs d'épargne");
            objectifsStage.setResizable(true);
            objectifsStage.setWidth(800);
            objectifsStage.setHeight(700);
            if (objectifsButton != null && objectifsButton.getScene() != null) {
                objectifsStage.initOwner(objectifsButton.getScene().getWindow());
            }
            
            // Ajouter le double-clic pour le mode plein écran
            objectifsScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    objectifsStage.setFullScreen(!objectifsStage.isFullScreen());
                }
            });
            
            ObjectifsEpargneController controller = loader.getController();
            controller.setAuthService(authService);
            
            objectifsStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                String.format(languageManager != null ? languageManager.getTranslation("alert.error.objective.view") : "Impossible d'ouvrir la vue des objectifs : %s", e.getMessage()), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/ChangePasswordView.fxml"));
            Stage changePasswordStage = new Stage();
            Scene changePasswordScene = new Scene(loader.load());
            changePasswordStage.setScene(changePasswordScene);
            changePasswordStage.setTitle("Changer le mot de passe");
            changePasswordStage.setResizable(true);
            changePasswordStage.initOwner(changePasswordButton.getScene().getWindow());
            
            // Ajouter le double-clic pour le mode plein écran
            changePasswordScene.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    changePasswordStage.setFullScreen(!changePasswordStage.isFullScreen());
                }
            });
            
            ChangePasswordController controller = loader.getController();
            controller.setAuthService(authService);
            
            changePasswordStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                (languageManager != null ? languageManager.getTranslation("password.change.error") : "Impossible d'ouvrir la fenêtre de changement de mot de passe") + " : " + e.getMessage(), 
                Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleExportCSV() {
        if (filteredTransactions.isEmpty()) {
            showAlert(languageManager != null ? languageManager.getTranslation("alert.warning") : "Avertissement", 
                languageManager != null ? languageManager.getTranslation("export.no.transactions") : "Aucune transaction à exporter", 
                Alert.AlertType.WARNING);
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le relevé de compte");
        fileChooser.setInitialFileName("releve_compte_" + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        
        Stage stage = (Stage) transactionTable.getScene().getWindow();
        java.io.File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                // En-tête CSV
                writer.write("Date;Type;Montant;Libellé;Compte Source;Compte Destination\n");
                
                // Données
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                for (Transaction t : filteredTransactions) {
                    writer.write(String.format("%s;%s;%s;%s;%s;%s\n",
                        t.getDate().format(formatter),
                        t.getType().toString(),
                        t.getMontant().toString(),
                        t.getLibelle() != null ? t.getLibelle() : "",
                        t.getCompteSource() != null ? t.getCompteSource().getNumeroCompte() : "",
                        t.getCompteDest() != null ? t.getCompteDest().getNumeroCompte() : ""
                    ));
                }
                
                showAlert(languageManager != null ? languageManager.getTranslation("alert.success") : "Succès", 
                    String.format(languageManager != null ? languageManager.getTranslation("export.success") : "Relevé de compte exporté avec succès : %s", file.getName()), 
                    Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert(languageManager != null ? languageManager.getTranslation("alert.error") : "Erreur", 
                    (languageManager != null ? languageManager.getTranslation("export.error") : "Erreur lors de l'export") + " : " + e.getMessage(), 
                    Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void handleLogout() {
        authService.logout();
        
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/banknet/view/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setScene(loginScene);
            stage.setTitle("BANKNET - Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        // Utiliser les traductions si languageManager est disponible
        String translatedTitle = title;
        if (languageManager != null) {
            // Essayer de traduire le titre s'il s'agit d'une clé de traduction
            if (title.equals("Erreur")) {
                translatedTitle = languageManager.getTranslation("alert.error");
            } else if (title.equals("Succès")) {
                translatedTitle = languageManager.getTranslation("alert.success");
            } else if (title.equals("Avertissement")) {
                translatedTitle = languageManager.getTranslation("alert.warning");
            } else if (title.equals("Information")) {
                translatedTitle = languageManager.getTranslation("alert.info");
            }
        }
        
        Alert alert = new Alert(type);
        alert.setTitle(translatedTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
