package com.ahmed.Secure.Task.Management.System.system;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PageResponseDto <T>{

    private List<T> content;

    private Long totalElements;

    private int size;

    private int page;

    private int totalPages;

    private boolean isFirst;

    private boolean isLast;

}
