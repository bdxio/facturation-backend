package dev.coop.facturation.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.client.spreadsheet.WorksheetQuery;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;
import java.io.IOException;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 *
 * @author lfo
 */
@Service
public class GoogleSheets {

    private SpreadsheetService service;

    @Autowired
    public GoogleSheets(GoogleCredential googleCredential) {
        Assert.notNull(googleCredential);
        service = new SpreadsheetService("compta-devcoop");
        service.setProtocolVersion(SpreadsheetService.Versions.V3);
        service.setOAuth2Credentials(googleCredential);
    }

    public WorksheetEntry getWorksheetEntry(WorksheetFeed feed, String worksheetName) {
        return feed.getEntries()
                .stream()
                .filter(w -> w.getTitle().getPlainText().equals(worksheetName))
                .findFirst().get();
    }

    public WorksheetFeed getWorksheetFeed(String spreadsheetId) {
        try {
            String path = String.format("https://spreadsheets.google.com/feeds/worksheets/%s/private/full", spreadsheetId);
            WorksheetQuery query = new WorksheetQuery(new URL(path));
            WorksheetFeed feed = service.query(query, WorksheetFeed.class);
            return feed;
        } catch (IOException | ServiceException ex) {
            throw new GsException(ex);
        }
    }

    public SpreadsheetService getService() {
        return service;
    }

}
