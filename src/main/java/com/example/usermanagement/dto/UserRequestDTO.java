package com.example.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDTO {
    @NotBlank(message = "Username khong duoc de trong")
    @Size(min = 3, max = 30, message = "Username phai tu 3 den 30 ky tu")
    private String username;

    @NotBlank(message = "Password khong duoc de trong")
    @Size(min = 6, message = "Password phai co it nhat 6 ky tu")
    private String password;

    @NotBlank(message = "Emial khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;
}
