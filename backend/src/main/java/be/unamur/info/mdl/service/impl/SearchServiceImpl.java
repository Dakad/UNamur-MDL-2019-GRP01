package be.unamur.info.mdl.service.impl;

import be.unamur.info.mdl.dal.entity.ArticleEntity;
import be.unamur.info.mdl.dal.entity.AuthorEntity;
import be.unamur.info.mdl.dal.entity.StateOfTheArtEntity;
import be.unamur.info.mdl.dal.entity.UserEntity;
import be.unamur.info.mdl.dal.repository.ArticleRepository;
import be.unamur.info.mdl.dal.repository.AuthorRepository;
import be.unamur.info.mdl.dal.repository.StateOfTheArtRepository;
import be.unamur.info.mdl.dal.repository.UserRepository;
import be.unamur.info.mdl.dto.ArticleDTO;
import be.unamur.info.mdl.dto.AuthorDTO;
import be.unamur.info.mdl.dto.SearchQueryDTO;
import be.unamur.info.mdl.dto.SearchResultDTO;
import be.unamur.info.mdl.dto.SearchResultDTO.SearchResultMetaDTO;
import be.unamur.info.mdl.dto.SearchResultDTO.MetaField;
import be.unamur.info.mdl.dto.SearchResultDTO.SearchResultDTOBuilder;
import be.unamur.info.mdl.dto.StateOfTheArtDTO;
import be.unamur.info.mdl.dto.UserDTO;
import be.unamur.info.mdl.service.SearchService;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service("searchService")
@Transactional
public class SearchServiceImpl implements SearchService {

  public static final int PAGE_SIZE_MAX = 20;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final StateOfTheArtRepository stateOfTheArtRepository;
  private final AuthorRepository authorRepository;


  @Autowired
  public SearchServiceImpl(UserRepository userRepository, ArticleRepository articleRepository,
    StateOfTheArtRepository stateOfTheArtRepository,
    AuthorRepository authorRepository) {
    this.articleRepository = articleRepository;
    this.userRepository = userRepository;
    this.stateOfTheArtRepository = stateOfTheArtRepository;
    this.authorRepository = authorRepository;
  }

  @Override
  public SearchResultDTO getSearchResults(SearchQueryDTO searchQuery) {
    SearchResultDTOBuilder searchResult = SearchResultDTO.builder();

    // PAGEABLE
    int page = searchQuery.getPage();
    String searchTerm = searchQuery.getSearchTerm();
    SearchResultMetaDTO resultMeta = new SearchResultMetaDTO();
    Sort pageSort;
    Pageable pageable;

    // USERS

//    pageSort = this.getSortForUser(searchQuery.getSort(), searchQuery.getOrder());
//    pageable = PageRequest.of(page, PAGE_SIZE_MAX, pageSort);
//    Page<UserEntity> users = userRepository
//      .findDistinctByFirstnameContainingIgnoreCaseOrFirstnameContainingIgnoreCase(searchTerm,
//        searchTerm, pageable);
//    List<UserDTO> userList = users.stream().map(u -> u.toDTO())
//      .collect(Collectors.toList());
//
//    searchResult.users(userList);
//    resultMeta.setUsersMeta(this.createMeta(users, pageSort));

    // AUTHORS

    pageSort = this.getSortForAuthor(searchQuery.getSort(), searchQuery.getOrder());
    pageable = PageRequest.of(page, PAGE_SIZE_MAX, pageSort);
    Page<AuthorEntity> authors = authorRepository
      .findDistinctByNameContainingIgnoreCase(searchTerm, pageable);

    List<AuthorDTO> authorsList = authors.stream().map(a -> a.toDTO())
      .collect(Collectors.toList());

    searchResult.authors(authorsList);
    resultMeta.setAuthorsMeta(this.createMeta(authors, pageSort));

    // ARTICLES

    pageSort = this.getSortForArticle(searchQuery.getSort(), searchQuery.getOrder());
    pageable = PageRequest.of(page, PAGE_SIZE_MAX, pageSort);

    Page<ArticleEntity> articles = articleRepository
      .findDistinctByTitleContainingIgnoreCase(searchTerm, pageable);

    List<ArticleDTO> articleList = articles.stream().map(a -> a.toDTO())
      .collect(Collectors.toList());

    searchResult.articles(articleList);
    resultMeta.setArticlesMeta(this.createMeta(articles, pageSort));

    // SOTAS

    pageSort = this.getSortForSota(searchQuery.getSort(), searchQuery.getOrder());
    pageable = PageRequest.of(page, PAGE_SIZE_MAX, pageSort);

    Page<StateOfTheArtEntity> sotas = stateOfTheArtRepository
      .findDistinctByTitleContainingIgnoreCase(searchTerm, pageable);

    List<StateOfTheArtDTO> sotaList = sotas.get().map(s -> s.toDTO())
      .collect(Collectors.toList());

    searchResult.statesOfTheArt(sotaList);
    resultMeta.setSotasMeta(this.createMeta(sotas, pageSort));

    searchResult.metas(resultMeta);

    return searchResult.build();
  }

  private Sort getSortForUser(final String sort, final String order) {
    if (sort.equalsIgnoreCase("name") || sort.equalsIgnoreCase("title"))  {
      if (order.equalsIgnoreCase("ASC")) {
        return Sort.by("lastname", "firstname").ascending();
      }
      if (order.equalsIgnoreCase("DESC")) {
        return Sort.by(Sort.Order.desc("lastname"), Sort.Order.desc("firstname"));
      }
    }
    return this.getSort(sort, order);
  }

  private Sort getSortForAuthor(final String sort, final String order) {
    if (sort.equalsIgnoreCase("name") || sort.equalsIgnoreCase("title")) {
      if (order.equalsIgnoreCase("ASC")) {
        return Sort.by("name").ascending();
      }
      if (order.equalsIgnoreCase("DESC")) {
        return Sort.by("name").descending();
      }
    }
    return this.getSort(sort, order);
  }

  private Sort getSortForArticle(final String sort, final String order) {
    if (sort.equalsIgnoreCase("name")) {
      return this.getSort("title", order);
    }
    return this.getSort(sort, order);
  }


  private Sort getSortForSota(final String sort, final String order) {
    if (sort.equalsIgnoreCase("name")) {
      return this.getSort("title", order);
    }else{
      return this.getSort(sort, order);
    }

  }

  /**
   * Get the correct Sort based on the sortedBy term and sortedOrder
   *
   * @param searchSortedBy What to sort on
   * @param searchSortOrder Which order for the sort (ASC or DESC)
   * @return The correct Sort
   */
  private Sort getSort(String searchSortedBy, String searchSortOrder) {
    Sort pageSort = null;
    switch (searchSortedBy.toLowerCase()) {
      case "title":
        pageSort = Sort.by("title");
        break;
      case "name":
        break;
      case "date":
        pageSort = Sort.by("createdAt");
        break;
      default:
        break;
    }

    if(pageSort == null) {
      return Sort.unsorted();
    }

    if (searchSortOrder.equalsIgnoreCase("ASC")) {
      return pageSort.ascending();
    }
    if (searchSortOrder.equalsIgnoreCase("DESC")) {
      return pageSort.descending();
    }
    return pageSort;
  }

  /**
   * Create the meta data about the search result based on the provided Page result and its Sort
   *
   * @param page The provided Page used for the findBy
   * @param pageSort The Sort used with the Pageable
   * @return the {@link EnumMap} to store the specific meta
   */
  private EnumMap<MetaField, Object> createMeta(Page<?> page, Sort pageSort) {
    EnumMap<MetaField, Object> meta = new EnumMap<>(MetaField.class);

    meta.put(MetaField.CURRENT_PAGE, page.getNumber() + 1);
    meta.put(MetaField.TOTAL_PAGES, page.getTotalPages());

    int size = page.getSize();
    int totalSize = (int) page.getTotalElements();
    size = (totalSize < size) ? totalSize : size;

    meta.put(MetaField.SIZE, size);
    meta.put(MetaField.TOTAL_SIZE, totalSize);

    Order order = pageSort.get().findFirst().get();
    meta.put(MetaField.ORDER_BY, order.getProperty());
    meta.put(MetaField.SORT_BY, order.getDirection().name());

    return meta;
  }
}
