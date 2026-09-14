package com.saka.qms.web.dto;

import com.saka.qms.model.Detail;
import com.saka.qms.model.NameOfItem;

import java.util.List;

public record ChecklistSaveResponse(NameOfItem item, List<Detail> details) {
}
