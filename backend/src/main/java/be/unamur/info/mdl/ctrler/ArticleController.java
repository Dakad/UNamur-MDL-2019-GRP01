package be.unamur.info.mdl.ctrler;

import be.unamur.info.mdl.dto.ArticleDTO;
import be.unamur.info.mdl.dto.UserDTO;
import be.unamur.info.mdl.service.ArticleService;
import be.unamur.info.mdl.service.exceptions.ArticleAlreadyExistException;
import be.unamur.info.mdl.service.exceptions.ArticleNotFoundException;
import be.unamur.info.mdl.service.exceptions.BookmarkNotFoundException;
import be.unamur.info.mdl.service.exceptions.UsernameNotFoundException;
import io.swagger.annotations.*;

import java.security.Principal;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/article")
@Api(value = "Article endpoints", description = "Operations related to an Article such as add, get, delete ...")
public class ArticleController extends APIBaseController {

  @Autowired
  private ArticleService articleService;


  @ApiOperation(value = "Register a new article")
  @ApiResponses(value = {
    @ApiResponse(code = 201, message = "Successfully registered"),
    @ApiResponse(code = 400, message = "Some required fields are invalid or missing"),
    @ApiResponse(code = 409, message = "If the article already exists", response = String.class)
  })
  @RequestMapping(path = {"", "/add"}, method = RequestMethod.POST)
  public ResponseEntity<?> create(@Valid @RequestBody ArticleDTO articleData, Principal authUser) {
    try {
      String username = authUser.getName();
      UserDTO currentUser = new UserDTO();
      currentUser.setUsername(username);
      articleService.create(articleData, currentUser);
    } catch (ArticleAlreadyExistException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    return new ResponseEntity<>(articleData, HttpStatus.CREATED);
  }


  @ApiOperation(value = "Retrieve a specific article by it reference")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Successfully registered", response = ArticleDTO.class),
    @ApiResponse(code = 400, message = "The article reference is missing "),
    @ApiResponse(code = 404, message = "The provided reference doesn't exist")
  })
  @RequestMapping(path = "/{reference}", method = RequestMethod.GET)
  public ResponseEntity<?> get(@Valid @PathVariable String reference) {
    try {
      ArticleDTO articleData = articleService.getArticleByReference(reference);
      return new ResponseEntity<>(articleData, HttpStatus.OK);
    } catch (ArticleNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @RequestMapping(path="/{reference}/bookmark-add", method = RequestMethod.POST)
  public ResponseEntity addBookmark(
    @Valid @PathVariable String reference,
    Principal authUser,
    @ApiParam(name = "note", defaultValue = "No description added") @Valid @RequestBody String note) {
    try{
      if(articleService.addBookmark(reference, authUser.getName(),note)) return ResponseEntity.status(HttpStatus.OK).body("Bookmark added");
      else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Something, somewhere, has gone sideways.\nAnd basically, error...");
    }catch (ArticleNotFoundException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article not found");
    }
  }

  @RequestMapping(path="/{reference}/bookmark-remove", method = RequestMethod.DELETE)
  public ResponseEntity removeBookmark(@Valid @PathVariable String reference, Principal authUser){
    try {
      if(articleService.removeBookmark(reference,authUser.getName())) return ResponseEntity.status(HttpStatus.OK).body("Bookmark removed");
      else return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error");
    }catch (ArticleNotFoundException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article reference does not exist");
    }catch (BookmarkNotFoundException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("This bookmark does not exist");
    }
  }

  @RequestMapping(path ="/{reference}/bookmarked", method = RequestMethod.GET)
  public ResponseEntity isBookmarked(@Valid @PathVariable String reference, Principal authUser){
    try {
      return ResponseEntity.status(HttpStatus.OK).body(articleService.isBookmarked(reference, authUser.getName()));
    }catch (ArticleNotFoundException e){
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Article reference does not exist");
    }
  }




}
