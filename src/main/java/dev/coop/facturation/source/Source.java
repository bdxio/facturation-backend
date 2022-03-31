package dev.coop.facturation.source;

import java.util.List;

public interface Source {
    List<List<Object>> values(String table);
}
