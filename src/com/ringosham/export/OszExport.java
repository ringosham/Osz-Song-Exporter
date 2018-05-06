package com.ringosham.export;

import com.ringosham.controllers.Controller;
import com.victorlaerte.asynctask.AsyncTask;

import java.awt.*;
import java.io.*;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OszExport extends AsyncTask<Void, Object, Void> {


    private Controller ui;
    private File exportDirectory;

    public OszExport(Controller ui, File exportDirectory) {
        this.ui = ui;
        this.exportDirectory = exportDirectory;
    }

    @Override
    public void onPreExecute() {
        ui.oszExport.setDisable(true);
        ui.exportButton.setDisable(true);
    }

    @Override
    public void progressCallback(Object... params) {
        String action = (String) params[0];
        switch (action) {
            case "text":
                String text = (String) params[1];
                ui.progressText.setText(text);
                break;
            case "progress":
                double workDone = ((Number) params[1]).doubleValue();
                double max = ((Number) params[2]).doubleValue();
                ui.progress.setProgress(workDone / max);
                break;
            case "console":
                String consoleText = (String) params[1];
                ui.consoleArea.appendText(consoleText + "\n");
                break;
            default:
        }
    }

    @Override
    public Void doInBackground(Void... params) {
        int workDone = 0;
        int progressMax = Controller.beatmapDir.listFiles(File::isDirectory).length;
        int failCount = 0;
        int successCount = 0;
        publishProgress("console", "Started exporting at " + Calendar.getInstance().getTime());
        publishProgress("console", "Export directory: " + exportDirectory.getAbsolutePath());
        publishProgress("text", "Analysing beatmaps...");
        for (File beatmap : Controller.beatmapDir.listFiles(File::isDirectory)) {
            try {
                publishProgress("text", "Exporting " + beatmap.getName());
                File outputFile = new File(exportDirectory, beatmap.getName() + ".osz");
                ZipOutputStream stream = new ZipOutputStream(new FileOutputStream(outputFile));
                ZipRecursive("", beatmap, stream);
                stream.close();
                successCount++;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                publishProgress("console", "Failed zipping beatmap. Skipping " + beatmap.getName());
                failCount++;
                e.printStackTrace();
            }
            workDone++;
            publishProgress("progress", workDone, progressMax);
        }
        publishProgress("console", "Total number of beatmaps exported: " + successCount);
        publishProgress("console", "Total number to failures: " + failCount);
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.open(exportDirectory);
        } catch (IOException ignored) {
        }
        publishProgress("progress", 0, 1);
        publishProgress("text", "Ready.");
        return null;
    }

    private void ZipRecursive(String basePath, File beatmap, ZipOutputStream stream) throws IOException {
        for (File file : beatmap.listFiles()) {
            if (file.isDirectory()) {
                if (basePath.isEmpty())
                    ZipRecursive(file.getName(), file, stream);
                else
                    ZipRecursive(basePath + "/" + file.getName(), file, stream);
                continue;
            }
            byte[] buffer = new byte[1024];
            FileInputStream in = new FileInputStream(file);
            if (basePath.isEmpty())
                stream.putNextEntry(new ZipEntry(file.getName()));
            else
                stream.putNextEntry(new ZipEntry(basePath + "/" + file.getName()));
            int length;
            while ((length = in.read(buffer)) > 0)
                stream.write(buffer, 0, length);
        }
    }

    @Override
    public void onPostExecute(Void params) {
        ui.exportButton.setDisable(false);
        ui.oszExport.setDisable(false);
    }
}
