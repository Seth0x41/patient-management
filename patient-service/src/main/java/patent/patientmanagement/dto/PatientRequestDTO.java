package patent.patientmanagement.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PatientRequestDTO {

    @NotBlank(message = "Name field is required!")
    @Size(min = 3, max = 100, message = "Name Cannot exceed 100 characters!")
    private String name;

    @NotBlank(message = "Email field is required!")
    @Email(message = "Email should be valid!")
    private String email;

    @NotBlank(message = "Address field is required!")
    private String address;

    @NotBlank(message = "Date of Birth field is required!")
    private String dateOfBirth;

    private String created_At;

    public @NotBlank @Size(min = 3, max = 100, message = "Name Cannot exceed 100 characters!") String getName() {
        return name;
    }

    public void setName(@NotBlank @Size(min = 3, max = 100, message = "Name Cannot exceed 100 characters!") String name) {
        this.name = name;
    }

    public @NotBlank @Email(message = "Email should be valid!") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank @Email(message = "Email should be valid!") String email) {
        this.email = email;
    }

    public @NotBlank String getAddress() {
        return address;
    }

    public void setAddress(@NotBlank String address) {
        this.address = address;
    }

    public @NotBlank String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(@NotBlank String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCreated_At() {
        return created_At;
    }

    public void setCreated_At( String created_At) {
        this.created_At = created_At;
    }
}
