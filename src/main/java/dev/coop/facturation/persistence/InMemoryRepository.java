package dev.coop.facturation.persistence;

import dev.coop.facturation.model.HasSocieteCodeKey;
import dev.coop.facturation.model.Societe;
import dev.coop.facturation.model.SocieteCodeKey;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

//TODO mutex for repo
public abstract class InMemoryRepository<T extends HasSocieteCodeKey> {

    Map<SocieteCodeKey, T> repo = new HashMap<>();

    protected abstract String getEntityName();

    public Optional<T> findById(final SocieteCodeKey id) {

        for(T item : repo.values()){
            if(item.getCodeValue() ==  id.getCodeValue() && id.getSociete().equals(item.getSociete().getNomCourt())){
                item.setCode(item.getCodeValue());//replace mongo db PersistenceConstructor trick :(
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public void save(final T entity) {
        repo.put(entity.getId(), entity);
    }

    public List<T> findBySociete(Societe societe){
        return repo.values().stream()
                .filter(a -> a.getSociete().getNomCourt().equals(societe.getNomCourt()))
                .sorted(Comparator.comparingInt(HasSocieteCodeKey::getCodeValue))
                .peek(item -> item.setCode(item.getCodeValue()))//replace mongo db PersistenceConstructor trick :(
                .collect(Collectors.toList());
    }

    public T findByIdOrThrow(SocieteCodeKey id) {
        return this.findById(id).orElseThrow(() -> new IllegalStateException(String.format("Unknown Entity [%s] with id:[%s]", getEntityName(), id)));
    }


    public void deleteAll() {
        repo = new HashMap<>();
    }
}
