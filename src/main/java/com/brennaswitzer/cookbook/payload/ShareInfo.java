package com.brennaswitzer.cookbook.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShareInfo {

    Long id;
    String slug;
    String secret;

}
