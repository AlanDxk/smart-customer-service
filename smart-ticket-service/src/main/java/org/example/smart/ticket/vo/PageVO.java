package org.example.smart.ticket.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PageVO<T> {
    private List<T> content;
    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPages;
}