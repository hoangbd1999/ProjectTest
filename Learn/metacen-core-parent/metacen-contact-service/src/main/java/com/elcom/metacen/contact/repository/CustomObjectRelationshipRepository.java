package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectRelationship;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface CustomObjectRelationshipRepository extends BaseCustomRepository<ObjectRelationship> {

    List<ObjectRelationship> search(String sourceObjectId);
}
