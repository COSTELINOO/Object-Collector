package backend.api.mapper;

import backend.api.dataTransferObject.UserDTO;
import backend.api.exception.CustomException;
import backend.api.exception.Exception500;
import backend.api.exception.Logger;
import backend.api.model.Collection;
import backend.api.model.User;
import backend.api.repository.CollectionRepository;
import backend.api.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class UserMapper {

    private static final UserRepository userRepository = new UserRepository();
    private static final CollectionRepository collectionRepository = new CollectionRepository();

    public static UserDTO toDTO(User user) throws CustomException {
        if (user == null) {
            throw new Exception500.InternalServerErrorException(
                    "MappingError",
                    "UserMappingError",
                    "Nu se poate mapa un utilizator null"
            );
        }

        try {
            UserDTO userDTO = new UserDTO();

            userDTO.setId(user.getId());
            userDTO.setUsername(user.getUsername());
            userDTO.setEmail(user.getEmail());

            userDTO.setCreated(userRepository.getCreatedAt(user.getId()));

            Long collectionsCount = collectionRepository.countAllCollectionsByUserId(user.getId());
            userDTO.setCountCollections(collectionsCount);

            List<Collection> userCollections = collectionRepository.findAllByUserId(user.getId());

            if (userCollections.isEmpty()) {
                userDTO.setCountObjects(0L);
                userDTO.setLikes(0L);
                userDTO.setViews(0L);
                userDTO.setValue(0.0);

                processProfileImage(user.getProfilePicture(), userDTO);
                return userDTO;
            }

            List<Long> collectionIds = userCollections.stream()
                    .map(Collection::getId)
                    .collect(Collectors.toList());

            Map<Long, Long> objectCounts;
            Map<Long, Long> likes;
            Map<Long, Long> views;
            Map<Long, Double> values;

            try {
                objectCounts = collectionRepository.getCountForCollectionIds(collectionIds);
                likes = collectionRepository.getLikesForCollectionIds(collectionIds);
                views = collectionRepository.getViewsForCollectionIds(collectionIds);
                values = collectionRepository.getValueForCollectionIds(collectionIds);

                long totalObjects = objectCounts.values().parallelStream().mapToLong(Long::longValue).sum();
                long totalLikes = likes.values().parallelStream().mapToLong(Long::longValue).sum();
                long totalViews = views.values().parallelStream().mapToLong(Long::longValue).sum();
                double totalValue = values.values().parallelStream().mapToDouble(Double::doubleValue).sum();

                userDTO.setCountObjects(totalObjects);
                userDTO.setLikes(totalLikes);
                userDTO.setViews(totalViews);
                userDTO.setValue(totalValue);
            } catch (CustomException e) {
                Logger.warning("Eroare la obținerea statisticilor: " + e.getMessage());
                userDTO.setCountObjects(0L);
                userDTO.setLikes(0L);
                userDTO.setViews(0L);
                userDTO.setValue(0.0);
            }

            processProfileImage(user.getProfilePicture(), userDTO);

            return userDTO;
        } catch (Exception e) {
            throw new Exception500.InternalServerErrorException(
                    "MappingError",
                    "UserMappingError",
                    "Eroare la transformarea utilizatorului: " + e.getMessage(),
                    e
            );
        }
    }

    private static void processProfileImage(String imagePath, UserDTO userDTO) throws CustomException {
        try {
            if (imagePath != null && !imagePath.isBlank()) {
                Path file = Paths.get(imagePath);

                if (!Files.exists(file)) {
                    Logger.warning("Imaginea de profil nu există la calea: " + imagePath);
                    return;
                }

                byte[] imageBytes = Files.readAllBytes(file);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                userDTO.setImage(base64Image);

                String imageName = file.getFileName().toString();
                userDTO.setImageName(imageName);
            }
        } catch (IOException e) {
            throw new Exception500.InternalServerErrorException(
                    "ImageProcessingError",
                    "ProfileImageError",
                    "Nu se poate accesa imaginea de profil: " + e.getMessage(),
                    e
            );
        }
    }
}