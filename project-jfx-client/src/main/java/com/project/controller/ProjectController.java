package com.project.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.dao.ProjektDAO;
import com.project.dao.ProjektDAOImpl;
import com.project.model.Projekt;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    private static final DateTimeFormatter dateFormatter     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---- Stan stronicowania i wyszukiwania ----
    private String  search4;
    private Integer pageNo;
    private Integer pageSize;
    private Integer totalRows;

    // ---- Serwisy ----
    private ExecutorService wykonawca;
    private ProjektDAO projektDAO;

    // ---- Komponenty GUI wstrzykiwane przez JavaFX ----
    @FXML private ChoiceBox<Integer> cbPageSizes;
    @FXML private TableView<Projekt> tblProjekt;
    @FXML private TableColumn<Projekt, Integer>       colId;
    @FXML private TableColumn<Projekt, String>        colNazwa;
    @FXML private TableColumn<Projekt, String>        colOpis;
    @FXML private TableColumn<Projekt, LocalDateTime> colDataCzasUtworzenia;
    @FXML private TableColumn<Projekt, LocalDate>     colDataOddania;
    @FXML private TextField txtSzukaj;
    @FXML private Button    btnDalej;
    @FXML private Button    btnWstecz;
    @FXML private Button    btnPierwsza;
    @FXML private Button    btnOstatnia;
    @FXML private Label     lblStrona;

    private ObservableList<Projekt> projekty;

    // ---- Konstruktory ----

    public ProjectController() {
        // Bezparametrowy konstruktor wymagany przez JavaFX (używany gdy nie ma setControllerFactory)
    }

    public ProjectController(ProjektDAO projektDAO) {
        this.projektDAO = projektDAO;
        // Pula jednego wątku – zadania pobierania danych są kolejkowane i wykonywane sekwencyjnie
        wykonawca = Executors.newFixedThreadPool(1);
    }

    // ---- Inicjalizacja (wywoływana automatycznie przez JavaFX po wstrzyknięciu komponentów) ----

    @FXML
    public void initialize() {
        search4  = "";
        pageNo   = 0;
        pageSize = 10;

        cbPageSizes.getItems().addAll(5, 10, 20, 50, 100);
        cbPageSizes.setValue(pageSize);
        cbPageSizes.setOnAction(e -> {
            pageSize = cbPageSizes.getValue();
            pageNo   = 0;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        });

        // Inicjalizacja kolumn
        colId.setCellValueFactory(new PropertyValueFactory<>("projektId"));
        colNazwa.setCellValueFactory(new PropertyValueFactory<>("nazwa"));
        colOpis.setCellValueFactory(new PropertyValueFactory<>("opis"));
        colDataCzasUtworzenia.setCellValueFactory(new PropertyValueFactory<>("dataCzasUtworzenia"));
        colDataOddania.setCellValueFactory(new PropertyValueFactory<>("dataOddania"));

        // Formatowanie daty i czasu w kolumnie
        colDataCzasUtworzenia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : dateTimeFormatter.format(item));
            }
        });

        colDataOddania.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null || empty ? null : dateFormatter.format(item));
            }
        });

        // Kolumna z przyciskami akcji
        TableColumn<Projekt, Void> colEdit = new TableColumn<>("Edycja");
        colEdit.setCellFactory(col -> new TableCell<>() {
            private final GridPane pane;
            {
                Button btnTask   = new Button("Zadania");
                Button btnEdit   = new Button("Edytuj");
                Button btnRemove = new Button("Usuń");
                btnTask.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnEdit.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                btnRemove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                btnTask.setOnAction(e -> {
                    // TODO wywoływać metodę openZadanieFrame(getCurrentProjekt()); po jej utworzeniu
                });
                btnEdit.setOnAction(e -> edytujProjekt(getCurrentProjekt()));
                btnRemove.setOnAction(e -> usunProjekt(getCurrentProjekt()));

                pane = new GridPane();
                pane.setAlignment(Pos.CENTER);
                pane.setHgap(10);
                pane.setVgap(10);
                pane.setPadding(new Insets(5, 5, 5, 5));
                pane.add(btnTask,   0, 0);
                pane.add(btnEdit,   0, 1);
                pane.add(btnRemove, 0, 2);
            }

            private Projekt getCurrentProjekt() {
                return getTableView().getItems().get(getTableRow().getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        tblProjekt.getColumns().add(colEdit);

        // Szerokości relatywne kolumn
        colId.setMaxWidth(5000);
        colNazwa.setMaxWidth(10000);
        colOpis.setMaxWidth(10000);
        colDataCzasUtworzenia.setMaxWidth(9000);
        colDataOddania.setMaxWidth(7000);
        colEdit.setMaxWidth(7000);

        projekty = FXCollections.observableArrayList();
        tblProjekt.setItems(projekty);

        if (wykonawca == null) {
            // fallback gdy użyto konstruktora bezparametrowego (np. SceneBuilder preview)
            projektDAO = new ProjektDAOImpl();
            wykonawca  = Executors.newFixedThreadPool(1);
        }
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    // ---- Przyciski ----

    @FXML
    private void onActionBtnSzukaj(ActionEvent event) {
        search4 = txtSzukaj.getText().trim();
        pageNo  = 0;
        wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
    }

    @FXML
    private void onActionBtnDalej(ActionEvent event) {
        int maxPage = maxPage();
        if (pageNo < maxPage) {
            pageNo++;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnWstecz(ActionEvent event) {
        if (pageNo > 0) {
            pageNo--;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnPierwsza(ActionEvent event) {
        if (pageNo != 0) {
            pageNo = 0;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnOstatnia(ActionEvent event) {
        int last = maxPage();
        if (pageNo != last) {
            pageNo = last;
            wykonawca.execute(() -> loadPage(search4, pageNo, pageSize));
        }
    }

    @FXML
    private void onActionBtnDodaj(ActionEvent event) {
        edytujProjekt(new Projekt());
    }

    // ---- Ładowanie strony (wywoływane w wątku tła) ----

    private void loadPage(String search4, Integer pageNo, Integer pageSize) {
        try {
            final List<Projekt> projektList = new ArrayList<>();

            if (search4 != null && !search4.isEmpty()) {
                if (search4.matches("[0-9]+")) {
                    // Wyszukiwanie po identyfikatorze
                    Projekt p = projektDAO.getProjekt(Integer.parseInt(search4));
                    if (p != null) projektList.add(p);
                    totalRows = projektList.size();
                } else if (search4.matches("^[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$")) {
                    // Wyszukiwanie po dacie oddania
                    LocalDate date = LocalDate.parse(search4, dateFormatter);
                    projektList.addAll(projektDAO.getProjektyWhereDataOddaniaIs(date, pageNo * pageSize, pageSize));
                    totalRows = projektDAO.getRowsNumberWhereDataOddaniaIs(date);
                } else {
                    // Wyszukiwanie po nazwie
                    projektList.addAll(projektDAO.getProjektyWhereNazwaLike(search4, pageNo * pageSize, pageSize));
                    totalRows = projektDAO.getRowsNumberWhereNazwaLike(search4);
                }
            } else {
                projektList.addAll(projektDAO.getProjekty(pageNo * pageSize, pageSize));
                totalRows = projektDAO.getRowsNumber();
            }

            final int currentPage = pageNo + 1;
            final int pages       = Math.max(1, (int) Math.ceil((double) totalRows / pageSize));

            Platform.runLater(() -> {
                projekty.clear();
                projekty.addAll(projektList);
                if (lblStrona != null)
                    lblStrona.setText("strona " + currentPage + " / " + pages);
            });

        } catch (RuntimeException e) {
            String msg = "Błąd podczas pobierania listy projektów.";
            logger.error(msg, e);
            String details = e.getCause() != null
                    ? e.getMessage() + "\n" + e.getCause().getMessage()
                    : e.getMessage();
            Platform.runLater(() -> showError(msg, details));
        }
    }

    // ---- Edycja / dodawanie projektu ----

    private void edytujProjekt(Projekt projekt) {
        Dialog<Projekt> dialog = new Dialog<>();
        dialog.setTitle("Edycja");
        dialog.setHeaderText(projekt.getProjektId() != null ? "Edycja danych projektu" : "Dodawanie projektu");
        dialog.setResizable(true);

        Label txtId = new Label(projekt.getProjektId() != null ? projekt.getProjektId().toString() : "");
        TextField txtNazwa = new TextField(projekt.getNazwa() != null ? projekt.getNazwa() : "");
        TextArea  txtOpis  = new TextArea(projekt.getOpis() != null ? projekt.getOpis() : "");
        txtOpis.setPrefRowCount(5);
        txtOpis.setPrefColumnCount(40);
        txtOpis.setWrapText(true);
        Label txtDataUtw = new Label(projekt.getDataCzasUtworzenia() != null
                ? dateTimeFormatter.format(projekt.getDataCzasUtworzenia()) : "");

        DatePicker dtDataOddania = new DatePicker(projekt.getDataOddania());
        dtDataOddania.setPromptText("RRRR-MM-DD");
        dtDataOddania.setConverter(new StringConverter<>() {
            @Override public String toString(LocalDate d)   { return d != null ? dateFormatter.format(d) : ""; }
            @Override public LocalDate fromString(String s) {
                return (s == null || s.isBlank()) ? null : LocalDate.parse(s, dateFormatter);
            }
        });
        dtDataOddania.getEditor().focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) {
                try {
                    dtDataOddania.setValue(dtDataOddania.getConverter().fromString(
                            dtDataOddania.getEditor().getText()));
                } catch (DateTimeParseException e) {
                    dtDataOddania.getEditor().setText(
                            dtDataOddania.getConverter().toString(dtDataOddania.getValue()));
                }
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(5));
        grid.add(getRightLabel("Id: "),             0, 0); grid.add(txtId,          1, 0);
        grid.add(getRightLabel("Data utw.: "),      0, 1); grid.add(txtDataUtw,     1, 1);
        grid.add(getRightLabel("Nazwa: "),          0, 2); grid.add(txtNazwa,       1, 2);
        grid.add(getRightLabel("Opis: "),           0, 3); grid.add(txtOpis,        1, 3);
        grid.add(getRightLabel("Data oddania: "),   0, 4); grid.add(dtDataOddania,  1, 4);
        dialog.getDialogPane().setContent(grid);

        ButtonType btnOk     = new ButtonType("Zapisz", ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Anuluj", ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, btnCancel);

        dialog.setResultConverter(bt -> {
            if (bt == btnOk) {
                projekt.setNazwa(txtNazwa.getText().trim());
                projekt.setOpis(txtOpis.getText().trim());
                projekt.setDataOddania(dtDataOddania.getValue());
                return projekt;
            }
            return null;
        });

        Optional<Projekt> result = dialog.showAndWait();
        if (result.isPresent()) {
            wykonawca.execute(() -> {
                try {
                    projektDAO.setProjekt(projekt);
                    Platform.runLater(() -> {
                        if (tblProjekt.getItems().contains(projekt)) {
                            tblProjekt.refresh();
                        } else {
                            tblProjekt.getItems().add(0, projekt);
                        }
                    });
                } catch (RuntimeException e) {
                    String msg = "Błąd podczas zapisywania projektu!";
                    logger.error(msg, e);
                    String details = e.getCause() != null
                            ? e.getMessage() + "\n" + e.getCause().getMessage()
                            : e.getMessage();
                    Platform.runLater(() -> showError(msg, details));
                }
            });
        }
    }

    // ---- Usuwanie projektu ----

    private void usunProjekt(Projekt projekt) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setTitle("Potwierdzenie");
        confirm.setHeaderText("Usunięcie projektu");
        confirm.setContentText("Czy na pewno chcesz usunąć projekt: " + projekt.getNazwa() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            wykonawca.execute(() -> {
                try {
                    projektDAO.deleteProjekt(projekt.getProjektId());
                    Platform.runLater(() -> projekty.remove(projekt));
                } catch (RuntimeException e) {
                    String msg = "Błąd podczas usuwania projektu!";
                    logger.error(msg, e);
                    Platform.runLater(() -> showError(msg, e.getMessage()));
                }
            });
        }
    }

    // ---- Pomocnicze ----

    private int maxPage() {
        if (totalRows == null || totalRows == 0) return 0;
        return (int) Math.ceil((double) totalRows / pageSize) - 1;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Błąd");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Label getRightLabel(String text) {
        Label lbl = new Label(text);
        lbl.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        lbl.setAlignment(Pos.CENTER_RIGHT);
        return lbl;
    }

    /** Wywoływana przy zamykaniu aplikacji – kończy pulę wątków. */
    public void shutdown() {
        if (wykonawca != null) {
            wykonawca.shutdown();
            try {
                if (!wykonawca.awaitTermination(5, TimeUnit.SECONDS))
                    wykonawca.shutdownNow();
            } catch (InterruptedException e) {
                wykonawca.shutdownNow();
            }
        }
    }
}
