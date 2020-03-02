package by.home.fileSender.service;


import by.home.fileSender.model.FileTransferModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static by.home.fileSender.component.Util.validate;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Value("${directory.path}")
    private String pathToDirectory;

    public static boolean isEmptyDirectory(Path pathFolder) throws IOException {
        return Files.newDirectoryStream(pathFolder).iterator().hasNext();
    }

    private List<Path> readFiles(Path path) throws IOException {
        validate(path == null, "directory.is.Empty");
        try (Stream<Path> streamPaths = Files.list(path)) {
            return streamPaths.collect(Collectors.toList());
        }
    }

    public List<FileTransferModel> getFiles() {
        List<FileTransferModel> fileTransferModels = new ArrayList<>();
        validate(pathToDirectory == null, "not.correct.path");
        Path path = Paths.get(pathToDirectory);
        validate(!Files.exists(path), "file.not.found");
        try {
            validate(!isEmptyDirectory(path), "directory.is.Empty");
            for (Path tempPath : readFiles(path)) {
                FileTransferModel fileTransferModel = new FileTransferModel();
                fileTransferModel.setName(tempPath.getFileName().toString());
                fileTransferModel.setBody(Files.readAllBytes(tempPath));
                fileTransferModels.add(fileTransferModel);
            }
            return fileTransferModels;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
