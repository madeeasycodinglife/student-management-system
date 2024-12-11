package com.madeeasy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequestDTO {
    @NotBlank(message = "Pin is required.")
    private String pin;
    @NotBlank(message = "City is required.")
    private String city;
    @NotBlank(message = "State is required.")
    private String state;
    @NotBlank(message = "Country is required.")
    private String country;
}
