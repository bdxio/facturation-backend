package dev.coop.facturation.google;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.common.base.Preconditions;
import dev.coop.facturation.configuration.GoogleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 *
 * @author lfo
 */
@Service
public class GoogleSheets {
    private final Sheets service;

    @Autowired
    public GoogleSheets(GoogleConfiguration googleConfiguration) {
        service = new Sheets.Builder(googleConfiguration.getTransport(), googleConfiguration.getJsonFactory(), googleConfiguration.getHttpRequestInitializer())
                .setApplicationName(GoogleConfiguration.APPLICATION_NAME)
                .build();
    }

    public ValueRange findSheetDataBySheetTitle(Spreadsheet spreadsheet, String sheetTitle) {
        Preconditions.checkNotNull(sheetTitle);

        spreadsheet.getSheets()
                .stream()
                .filter(it -> sheetTitle.equals(it.getProperties().getTitle()))
                .findFirst()
                .orElseThrow(() -> new GsException(String.format("Cannot find sheets with title %s", sheetTitle)));

        try {
            // Fetch all sheets values
            return service.spreadsheets().values()
                    .get(spreadsheet.getSpreadsheetId(), sheetTitle)
                    .execute();
        } catch (IOException e) {
            throw new GsException(e);
        }
    }

    public Spreadsheet fetchSpreadsheet(String spreadsheetId) {
        try {
            return service.spreadsheets()
                    .get(spreadsheetId)
                    .setIncludeGridData(true)
                    .execute();
        } catch (IOException e) {
            throw new GsException(e);
        }
    }
}
