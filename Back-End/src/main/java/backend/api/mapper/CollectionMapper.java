package backend.api.mapper;

import backend.api.dataTransferObject.CollectionDTO;
import backend.api.model.Collection;
import backend.api.model.CollectionType;

public class CollectionMapper {

    public static CollectionDTO toDTO(Collection collection, long likes, long views, double value, String username, long count) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(collection.getId());
        dto.setName(collection.getNume());
        dto.setTipColectie(CollectionType.getById(collection.getIdTip()).toString());
        dto.setLikes(likes);
        dto.setView(views);
        dto.setValue(value);
        dto.setUsername(username);
        dto.setCount(count);
        if (collection.getCreatedAt() != null) {
            dto.setCreated(collection.getCreatedAt().toLocalDateTime().toLocalDate());
        }
        return dto;
    }}