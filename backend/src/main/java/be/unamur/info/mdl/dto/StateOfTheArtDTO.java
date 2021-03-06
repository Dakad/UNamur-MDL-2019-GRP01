package be.unamur.info.mdl.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.annotations.ApiModel;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "SOTA", description = "Model representing a State Of The Art")
public class StateOfTheArtDTO {

  @JsonIgnore
  private Long id;

  @NotBlank(message = "The title is required")
  private String title;

  private String reference;


  @JsonProperty(value = "subject")
  @Builder.Default
  private String category = "unknown";


  @JsonProperty("created_at")
  private LocalDate createdAt;


  private UserDTO creator;

  @JsonProperty(value = "keywords", access = Access.WRITE_ONLY)
  @Builder.Default
  private List<@NotBlank(message = "The keywords(s) must be defined") String> keywordList = new LinkedList<>();

  @JsonProperty(value = "keywords", access = Access.READ_ONLY)
  @Builder.Default
  private List<TagDTO> keywords = new LinkedList<>();


  @JsonProperty(value = "articles", access = Access.WRITE_ONLY)
  @Builder.Default
  private List<@NotBlank(message = "The article reference(s) must be defined") String> articleList = new LinkedList<>();

  @JsonProperty(value = "articles", access = Access.READ_ONLY)
  @Builder.Default
  private List<ArticleDTO> articles = new LinkedList<>();

}
