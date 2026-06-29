package com.banknet.controller;

import com.banknet.util.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class ExchangeRatesController {
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private Label subtitleLabel;
    
    @FXML
    private Button closeButton;
    
    @FXML
    private TableView<CurrencyRate> ratesTable;
    
    @FXML
    private TableColumn<CurrencyRate, String> currencyColumn;
    
    @FXML
    private TableColumn<CurrencyRate, String> rateColumn;
    
    @FXML
    private Label disclaimerLabel;
    
    private LanguageManager languageManager;
    private ObservableList<CurrencyRate> rates;
    
    // Taux de change fixes (en pratique, on récupérerait ces données depuis une API)
    private static final Map<String, BigDecimal> EXCHANGE_RATES = new HashMap<>();
    
    static {
        // 1 MAD = base
        EXCHANGE_RATES.put("EUR", new BigDecimal("0.091"));  // 1 MAD = 0.091 EUR (approximatif)
        EXCHANGE_RATES.put("USD", new BigDecimal("0.099"));  // 1 MAD = 0.099 USD (approximatif)
        EXCHANGE_RATES.put("MAD", BigDecimal.ONE);           // 1 MAD = 1 MAD
        EXCHANGE_RATES.put("GBP", new BigDecimal("0.078"));  // 1 MAD = 0.078 GBP (approximatif)
        EXCHANGE_RATES.put("JPY", new BigDecimal("14.75"));  // 1 MAD = 14.75 JPY (approximatif)
    }
    
    @FXML
    public void initialize() {
        languageManager = LanguageManager.getInstance();
        rates = FXCollections.observableArrayList();
        
        // Initialiser les colonnes avec CellFactory personnalisé pour éviter le texte coupé
        currencyColumn.setCellValueFactory(new PropertyValueFactory<>("currency"));
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        
        // CellFactory personnalisé pour la colonne Devise - affiche le texte complet
        currencyColumn.setCellFactory(column -> new TableCell<CurrencyRate, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(false); // Pas de retour à la ligne pour garder une ligne
                    setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-padding: 12px 20px;");
                }
            }
        });
        
        // CellFactory personnalisé pour la colonne Taux - affiche le texte complet
        rateColumn.setCellFactory(column -> new TableCell<CurrencyRate, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    setWrapText(false); // Pas de retour à la ligne pour garder une ligne
                    setStyle("-fx-font-size: 16px; -fx-font-weight: 500; -fx-padding: 12px 20px;");
                }
            }
        });
        
        // Configurer les largeurs des colonnes pour un meilleur affichage (surtout pour l'arabe)
        currencyColumn.setMinWidth(550);
        currencyColumn.setPrefWidth(600);
        currencyColumn.setMaxWidth(Double.MAX_VALUE);
        rateColumn.setMinWidth(450);
        rateColumn.setPrefWidth(500);
        rateColumn.setMaxWidth(Double.MAX_VALUE);
        
        // Configurer la hauteur des lignes pour une meilleure lisibilité
        ratesTable.setRowFactory(tv -> {
            javafx.scene.control.TableRow<CurrencyRate> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-min-height: 55px; -fx-pref-height: 55px;");
            return row;
        });
        
        // Désactiver le redimensionnement automatique pour garder les largeurs définies
        ratesTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Charger les taux de change
        loadExchangeRates();
        
        // Appliquer les traductions
        updateTranslations();
    }
    
    private void loadExchangeRates() {
        rates.clear();
        
        // Calculer les taux : 1 unité de devise = X MAD
        // Exemple : 1 EUR = 11 MAD (inverse de 0.091)
        BigDecimal oneEurInMad = BigDecimal.ONE.divide(EXCHANGE_RATES.get("EUR"), 4, RoundingMode.HALF_UP);
        BigDecimal oneUsdInMad = BigDecimal.ONE.divide(EXCHANGE_RATES.get("USD"), 4, RoundingMode.HALF_UP);
        BigDecimal oneGbpInMad = BigDecimal.ONE.divide(EXCHANGE_RATES.get("GBP"), 4, RoundingMode.HALF_UP);
        BigDecimal oneJpyInMad = BigDecimal.ONE.divide(EXCHANGE_RATES.get("JPY"), 4, RoundingMode.HALF_UP);
        
        // Créer les entrées pour chaque devise (utiliser les codes ISO pour la cohérence)
        rates.add(new CurrencyRate(
            languageManager.getTranslation("exchange.euro"),
            "1 EUR = " + oneEurInMad + " MAD"
        ));
        
        rates.add(new CurrencyRate(
            languageManager.getTranslation("exchange.dollar"),
            "1 USD = " + oneUsdInMad + " MAD"
        ));
        
        rates.add(new CurrencyRate(
            languageManager.getTranslation("exchange.dirham"),
            "1 MAD = 1.0000 MAD"
        ));
        
        rates.add(new CurrencyRate(
            languageManager.getTranslation("exchange.pound"),
            "1 GBP = " + oneGbpInMad + " MAD"
        ));
        
        rates.add(new CurrencyRate(
            languageManager.getTranslation("exchange.yen"),
            "100 JPY = " + oneJpyInMad.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP) + " MAD"
        ));
        
        ratesTable.setItems(rates);
    }
    
    private void updateTranslations() {
        titleLabel.setText(languageManager.getTranslation("exchange.title"));
        subtitleLabel.setText(languageManager.getTranslation("exchange.subtitle"));
        closeButton.setText(languageManager.getTranslation("exchange.close"));
        currencyColumn.setText(languageManager.getTranslation("exchange.currency"));
        rateColumn.setText(languageManager.getTranslation("exchange.rate") + " (MAD)");
        if (disclaimerLabel != null) {
            disclaimerLabel.setText(languageManager.getTranslation("exchange.disclaimer"));
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
    
    // Classe interne pour représenter un taux de change
    public static class CurrencyRate {
        private String currency;
        private String rate;
        
        public CurrencyRate(String currency, String rate) {
            this.currency = currency;
            this.rate = rate;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public void setCurrency(String currency) {
            this.currency = currency;
        }
        
        public String getRate() {
            return rate;
        }
        
        public void setRate(String rate) {
            this.rate = rate;
        }
    }
}

