package dev.coop.facturation.source;

import com.google.api.services.sheets.v4.model.Spreadsheet;

import java.util.List;

public class SpreadsheetSource implements Source {

    private final Spreadsheet spreadsheet;

    public SpreadsheetSource(Spreadsheet spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    public List<List<Object>> values(String table) {

        return null;
    }
}
