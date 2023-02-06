package com.elcom.metacen.content.service;

import com.elcom.metacen.content.dto.FolderDTO;
import com.elcom.metacen.content.dto.PointDTO;

import java.util.List;

public interface PathService {
    public List<FolderDTO> getListImages(List<PointDTO> pointDTOS, List<FolderDTO> listFolderCheck);
    public List<FolderDTO> getListImages(PointDTO pointDTO, double distant, List<FolderDTO> listFolderCheck);
}
