package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.ringosham.objects.Settings;
import javafx.fxml.FXML;

public class Exporter {
    private Controller ui;
    private Settings settings;

    public Exporter(Controller ui, Settings settings) {
        this.ui = ui;
        this.settings = settings;
    }

    @FXML
    public void start() {
        ui.exportButton.setDisable(true);
        ui.progress.setProgress(-1);
        //TODO Use Task<> to do stuff in background
    }
}
