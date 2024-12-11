package com.madeeasy.dto.request;

import lombok.Data;

@Data
public class AddressPartialRequestDTO {
    private String pin;
    private String city;
    private String state;
    private String country;
}
