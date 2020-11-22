package com.brennaswitzer.cookbook.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlanBucket {

    private Object id;
    private String name;
    private LocalDate date;

}
