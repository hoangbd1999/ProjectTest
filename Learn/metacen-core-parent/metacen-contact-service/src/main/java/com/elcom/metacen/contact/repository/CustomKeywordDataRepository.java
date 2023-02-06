package com.elcom.metacen.contact.repository;

import com.elcom.metacen.contact.model.KeywordData;
import com.elcom.metacen.contact.model.dto.AggregateKeywordDataObjectGeneralInfoDTO;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public interface CustomKeywordDataRepository extends BaseCustomRepository<KeywordData> {
    AggregateKeywordDataObjectGeneralInfoDTO findByKeywordIdsAndType(List<String> keywordIds, Integer type, Long skip, Long limit);
}
