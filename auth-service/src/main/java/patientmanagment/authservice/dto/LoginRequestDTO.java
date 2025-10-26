package patientmanagment.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO {
    @NotBlank(message = "Email is required!")
    @Email(message = "Email is not valid!")
    private String email;

    @NotBlank(message = "Password is required!")
    @Size(min = 8, message ="Password must be at least 8 characters!" )
    private String password;

    public @NotBlank(message = "Email is required!") @Email(message = "Email is not valid!") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required!") @Email(message = "Email is not valid!") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Password is required!") @Size(min = 8, message = "Password must be at least 8 characters!") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required!") @Size(min = 8, message = "Password must be at least 8 characters!") String password) {
        this.password = password;
    }
}
