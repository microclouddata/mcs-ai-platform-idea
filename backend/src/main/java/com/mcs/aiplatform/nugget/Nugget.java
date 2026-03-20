package com.mcs.aiplatform.nugget;

import com.mcs.aiplatform.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "nuggets")
@CompoundIndex(name = "nugget_agent_idx", def = "{'agentId': 1, 'createdAt': -1}")
public class Nugget extends BaseEntity {

    private String agentId;
    private String name;
    private String description;
    private String code;
    private NuggetLanguage language = NuggetLanguage.PYTHON;
    private NuggetStatus status = NuggetStatus.ACTIVE;
    private NuggetType nuggetType = NuggetType.CODE;
    private String docId;
    private List<String> controlFlags = new ArrayList<>();
    private Map<String, String> metadata = new LinkedHashMap<>();
    private List<String> tags = new ArrayList<>();
    private List<NuggetParameter> parameters = new ArrayList<>();
    private boolean modelTool = false;
}
