package by.home.fileSender.controller;

import by.home.fileSender.dto.FileTransferDto;
import by.home.fileSender.model.FileTransferModel;
import by.home.fileSender.service.FileService;
import org.dozer.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller Sender. Include one get methods which return list of FileTransferDto
 */
@RestController
@RequestMapping("/file")
public class ControllerSender {

    private final FileService fileService;

    private final Mapper mapper;

    public ControllerSender(FileService fileService, Mapper mapper) {
        this.fileService = fileService;
        this.mapper = mapper;
    }


    /**
     * Mapping from FileTransferModel to FileTransferDto wrapped it to ResponseEntity then send to client
     *
     * @return dto object FileTransferDto wrapped to ResponseEntity
     */

    @GetMapping(value = "getFile")
    public ResponseEntity<List<FileTransferDto>> getFiles() {
        List<FileTransferModel> files = fileService.getFiles();
        final List<FileTransferDto> fileDtoList = files.stream()
                .map((fileModel) -> mapper.map(fileModel, FileTransferDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(fileDtoList, HttpStatus.OK);
    }



}
