package by.home.fileSender.controller;

import by.home.fileSender.dto.FileTransferDto;
import by.home.fileSender.model.FileTransferModel;
import by.home.fileSender.service.FileService;
import org.dozer.Mapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class ControllerSender {

    private final FileService fileService;

    private final Mapper mapper;

    public ControllerSender(FileService fileService, Mapper mapper) {
        this.fileService = fileService;
        this.mapper = mapper;
    }

//    @RequestMapping(method = RequestMethod.GET)
//    public ResponseEntity<FileTransferDto> getFiles() {
//        List<FileTransferModel> files = fileService.getFiles();
//        FileTransferDto fileDtoList = files.stream()
//                .map((fileModel) -> mapper.map(fileModel, FileTransferDto.class))
//                .collect(Collectors.toList()).get(0);
//        return new ResponseEntity<>(fileDtoList, HttpStatus.OK);
//    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<FileTransferDto>> getFiles() {
        List<FileTransferModel> files = fileService.getFiles();
        final List<FileTransferDto> fileDtoList = files.stream()
                .map((fileModel) -> mapper.map(fileModel, FileTransferDto.class))
                .collect(Collectors.toList());
        return new ResponseEntity<>(fileDtoList, HttpStatus.OK);
    }

}
