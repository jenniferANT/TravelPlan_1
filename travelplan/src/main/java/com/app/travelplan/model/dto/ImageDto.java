package com.app.travelplan.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageDto {
    private long imageId;
    private String imageUrl;
}
