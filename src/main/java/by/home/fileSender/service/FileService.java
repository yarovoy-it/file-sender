package by.home.fileSender.service;


import by.home.fileSender.model.FileTransferModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static by.home.fileSender.component.Util.validate;
import static by.home.fileSender.controller.ControllerSender.readyForTaking;

@Service
public class FileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);

    @Value("${directory.path}")
    private String pathToDirectory;

    /**
     * Defined directory empty or not
     *
     * @param pathFolder path to directory
     * @return is empty
     * @throws IOException catch it in getFiles
     */
    private boolean isEmptyDirectory(Path pathFolder) throws IOException {
        return Files.newDirectoryStream(pathFolder).iterator().hasNext();
    }

    /**
     * Reade all paths to files
     *
     * @param path to directory
     * @return list of paths
     * @throws IOException catch it in getFiles
     */
    private List<Path> readFiles(Path path) throws IOException {
        validate(path == null, "path.error");
        try (Stream<Path> streamPaths = Files.list(path)) {
            return streamPaths.collect(Collectors.toList());
        }
    }

    /**
     * Convert files in model FileTransferModel by support readFiles, path to directory take from pathToDirectory
     *
     * @return list of FileTransferModel
     */
    public List<FileTransferModel> getFiles() {
        List<FileTransferModel> fileTransferModels = new ArrayList<>();
        Path path = Paths.get(pathToDirectory);
        validate(!Files.exists(path), "file.not.found");
        try {
            for (Path tempPath : readFiles(path)) {
                FileTransferModel fileTransferModel = new FileTransferModel();
                fileTransferModel.setName(tempPath.getFileName().toString());
                fileTransferModel.setBody(Files.readAllBytes(tempPath));
                fileTransferModels.add(fileTransferModel);
            }
            return fileTransferModels;
        } catch (IOException e) {
            validate(true, "file.read.problem");
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    public void observeFolder() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            validate(pathToDirectory == null, "not.correct.path");
            Path path = Paths.get(pathToDirectory);
            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    LOGGER.info(event.kind().toString());
                    LOGGER.info(event.context().toString());
                    readyForTaking();
                }
                key.reset();
            }
        } catch (InterruptedException | IOException e) {
            validate(true, "file.read.problem");
            LOGGER.error(e.getMessage());
        }
    }
}
