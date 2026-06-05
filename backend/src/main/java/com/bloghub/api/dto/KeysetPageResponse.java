package com.bloghub.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KeysetPageResponse<T> {

    private List<T> content;
    private Long nextCursor;
    private boolean hasNext;
    private int size;
}
