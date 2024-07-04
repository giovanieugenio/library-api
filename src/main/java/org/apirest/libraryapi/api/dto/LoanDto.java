package org.apirest.libraryapi.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoanDto {

    private Long id;

    @NotEmpty
    private String isbn;

    @NotEmpty
    private String customer;

    @NotEmpty
    private String email;

    private BookDto bookDto;
}
