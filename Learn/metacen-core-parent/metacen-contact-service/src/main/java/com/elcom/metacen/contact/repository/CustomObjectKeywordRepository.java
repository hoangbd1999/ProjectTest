package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.ObjectKeyword;
import java.util.List;

/**
 *
 * @author Admin
 */
public interface CustomObjectKeywordRepository extends BaseCustomRepository<ObjectKeyword> {

    List<ObjectKeyword> search(String objectId);
}
