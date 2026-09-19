package me.eeshe.tempus.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record ImportCSVFilesRequest(List<MultipartFile> files) {
}
