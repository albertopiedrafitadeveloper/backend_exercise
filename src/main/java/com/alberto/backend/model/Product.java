package com.alberto.backend.model;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product {

  private String id;
  private String name;
  private BigDecimal price;
  private Boolean availability;

}
