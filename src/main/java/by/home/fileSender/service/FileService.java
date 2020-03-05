package by.home.fileSender.service;


import by.home.fileSender.dto.FileTransferDto;
import by.home.fileSender.model.FileTransferModel;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.*;
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

    @Value("${url.consumer}")
    private String url;

    @Value("${url.consumer.ping}")
    private String urlPing;

    private final RestTemplate restTemplate;

    private final Mapper mapper;

    public FileService(RestTemplate restTemplate, Mapper mapper) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;
    }

    /**
     * Delete all files
     *
     * @param paths list paths to directory
     * @throws IOException
     */
    private void deleteFiles(List<Path> paths) {
        paths.forEach(file -> {
            try {
                Files.delete(file);
            } catch (IOException e) {
                validate(true, "file.not.delete");
                LOGGER.error(e.getMessage());
            }
        });
    }

    /**
     * Reade all paths to files
     *
     * @param path to directory
     * @return list of paths
     * @throws IOException catch it in getFiles
     */
    private List<Path> readPathToFiles(Path path) throws IOException {
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
    public List<FileTransferModel> getFiles(List<Path> paths) {
        List<FileTransferModel> fileTransferModels = new ArrayList<>();
        try {
            for (Path tempPath : paths) {
                FileTransferModel fileTransferModel = new FileTransferModel();
                fileTransferModel.setName(tempPath.getFileName().toString());
                fileTransferModel.setBody(Files.readAllBytes(tempPath));
                fileTransferModel.setSize(FileChannel.open(tempPath).size());
                fileTransferModels.add(fileTransferModel);
            }
            return fileTransferModels;
        } catch (IOException e) {
            validate(true, "file.read.problem");
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * Read files from directory and send list of them to "file-consumer" by url
     */
    private List<Path> sendFiles() throws IOException {
        List<Path> listPaths = this.readPathToFiles(Paths.get(pathToDirectory));
        List<FileTransferModel> files = getFiles(listPaths);
        List<FileTransferDto> fileDtoList = files.stream()
                .map((fileModel) -> mapper.map(fileModel, FileTransferDto.class))
                .collect(Collectors.toList());
        HttpEntity<List<FileTransferDto>> entity = new HttpEntity<>(fileDtoList);
        restTemplate.exchange(url, HttpMethod.PUT, entity, new ParameterizedTypeReference<List<FileTransferDto>>() {
        });
        return listPaths;
    }

    /**
     * Checking the consumer part it working or not
     *
     * @return true working false not available
     */
    private boolean pingConsumer() {
        try {
            ResponseEntity<String> str = restTemplate.getForEntity(urlPing, String.class);
            if (str.getStatusCode().is2xxSuccessful()) {
                return true;
            }
        } catch (Exception ex) {
            LOGGER.warn(ex.getMessage());
            return false;
        }
        return false;
    }

    /**
     * Observe folder, if file appeared: send him, delete and keep watching
     */
    @PostConstruct
    public void observeFolder() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            validate(pathToDirectory == null, "not.correct.path");
            Path path = Paths.get(pathToDirectory);
            validate(!Files.isDirectory(path), "not.directory.path");
            path.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (pingConsumer()) {
                        LOGGER.info(event.kind().toString());
                        LOGGER.info(event.context().toString());
                        deleteFiles(sendFiles());
                    } else {
                        key.reset();
                    }
                }
                key.reset();
            }
        } catch (InterruptedException | IOException e) {
            validate(true, "file.read.problem");
            LOGGER.error(e.getMessage());
        }
    }
}
