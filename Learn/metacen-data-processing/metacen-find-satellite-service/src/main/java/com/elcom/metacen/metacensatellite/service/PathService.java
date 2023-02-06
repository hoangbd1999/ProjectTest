package com.elcom.metacen.metacensatellite.service;

import com.elcom.metacen.metacensatellite.dto.FolderDTO;
import com.elcom.metacen.metacensatellite.dto.PointDTO;

import java.awt.geom.Path2D;
import java.util.List;

public interface PathService {
    public List<FolderDTO> getListImages(List<PointDTO> pointDTOS, List<FolderDTO> listFolderCheck);
    public List<FolderDTO> getListImages(PointDTO pointDTO, double distant, List<FolderDTO> listFolderCheck);
}
