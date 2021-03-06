package be.unamur.info.mdl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "User", description = "Model representing an user data")
public class UserDTO extends CredentialDTO {

  @NotBlank(message = "The email is required")
  @Email(message = "The email provided is not valid")
  private String email;

  private String lastname;

  private String firstname;

  @JsonProperty("avatar")
  private String profilePicUrl = "https://www.w3schools.com/bootstrap/img_avatar1.png";

  private String domain = "Unknown";

  private String organization = "Not defined";

}
