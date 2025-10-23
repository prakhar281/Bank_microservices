package com.newbank.accounts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(
        name = "Accounts",
        description = "Schema to hold Account information"
)
public class AccountsDto {
    @NotEmpty(message = "Account number cannot be empty")
    @Pattern(regexp = "^[0-9]{10}$", message = "Account number must be 10 digits")
    @Schema(
            description = "Account Number of Dev-Bank account", example = "3454433243"
    )
    private Long accountNumber;

    @NotEmpty(message = "Account type cannot be empty")
    @Schema(
            description = "Account type of Dev-Bank account", example = "Savings"
    )
    private String accountType;

    @NotEmpty(message = "Branch address cannot be empty")
    @Schema(
            description = "Dev-Bank branch address", example = "123 NewYork"
    )
    private String branchAddress;
}
