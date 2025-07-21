package backend.api.mapper;

import backend.api.dataTransferObject.ExplorerObjectDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Collection;
import backend.api.model.CollectionType;
import backend.api.model.Obiect;
import backend.api.repository.CollectionRepository;
import backend.api.repository.ObjectLikeRepository;
import backend.api.repository.ObjectViewRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Optional;


public class ExplorerObjectMapper {

    private static final CollectionRepository collectionRepository = new CollectionRepository();
    private static final ObjectLikeRepository objectLikeRepository = new ObjectLikeRepository();
    private static final ObjectViewRepository objectViewRepository = new ObjectViewRepository();

    public static ExplorerObjectDTO toDTO(Obiect obiect) throws CustomException {
        if (obiect == null) {
            throw new Exception500.InternalServerErrorException(
                    "MappingError",
                    "ObjectMappingError",
                    "Nu se poate mapa un obiect null"
            );
        }

        try {

            ExplorerObjectDTO explorerObjectDTO = new ExplorerObjectDTO();

            explorerObjectDTO.setId(obiect.getId());
            explorerObjectDTO.setName(obiect.getName());
            explorerObjectDTO.setDescription(obiect.getDescriere());

            Long collectionId = Long.valueOf(obiect.getIdColectie());
            Collection cel= collectionRepository.findById(collectionId).orElse(null);
            explorerObjectDTO.setUsername(collectionRepository.getUsername(cel.getIdUser()));

            Optional<backend.api.model.Collection> collection = collectionRepository.findById(collectionId);
            if (collection.isPresent()) {
                CollectionType collectionType = CollectionType.getById(collection.get().getIdTip());
                explorerObjectDTO.setTipColectie(collectionType.toString());
            }

            processObjectImage(obiect.getImage(), explorerObjectDTO);

            explorerObjectDTO.setLikes(objectLikeRepository.getLikeCount(obiect.getId()));
            explorerObjectDTO.setViews(objectViewRepository.getViewCount(obiect.getId()));

            if (obiect.getPretAchizitie() != null) explorerObjectDTO.setValue(obiect.getPretAchizitie());

            if (obiect.getCreatedAt() != null) {
                explorerObjectDTO.setCreated(obiect.getCreatedAt().toLocalDateTime().toLocalDate());
            }

            return explorerObjectDTO;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "MappingError",
                    "ObjectMappingError",
                    "Eroare la transformarea obiectului: " + e.getMessage(),
                    e
            );
        }
    }

    private static void processObjectImage(String imagePath, ExplorerObjectDTO dto) {
        try {
            if (imagePath != null && !imagePath.isEmpty()) {
                Path file = Paths.get(imagePath);

                if (!Files.exists(file)) {
                    Logger.warning("Imaginea obiectului nu există la calea: " + imagePath);
                    dto.setImage("");
                    return;
                }

                byte[] imageBytes = Files.readAllBytes(file);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                dto.setImage(base64Image);

            } else {
                dto.setImage("");
            }
        } catch (IOException e) {
            Logger.warning("Nu s-a putut citi imaginea obiectului: " + e.getMessage());
            dto.setImage("");
        }
    }
}