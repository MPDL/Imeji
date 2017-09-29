package de.mpg.imeji.logic.search;

import java.util.List;

import de.mpg.imeji.logic.model.User;
import de.mpg.imeji.logic.search.facet.model.Facet;
import de.mpg.imeji.logic.search.model.SearchQuery;
import de.mpg.imeji.logic.search.model.SearchResult;
import de.mpg.imeji.logic.search.model.SortCriterion;

/**
 * Search Interface for imeji
 *
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public interface Search {
  /**
   * Types of search (What objects types are retuned)
   *
   * @author saquet (initial creation)
   * @author $Author$ (last modification)
   * @version $Revision$ $LastChangedDate$
   */
  public static enum SearchObjectTypes {
    ITEM, COLLECTION, USER, USERGROUPS, ALL, CONTENT, STATEMENT;
  }

  /**
   * Search for imeji objects
   *
   * @param query
   * @param sortCri
   * @param user
   * @param folderUri TODO
   * @param from
   * @param size
   * @return
   */
  public SearchResult search(SearchQuery query, SortCriterion sortCri, User user, String folderUri,
      int offset, int size);

  /**
   * Search and set {@link Facet} to the {@link SearchResult}
   * 
   * @param query
   * @param sortCri
   * @param user
   * @param folderUri
   * @param offset
   * @param size
   * @return
   */
  public SearchResult searchWithFacets(SearchQuery query, SortCriterion sortCri, User user,
      String folderUri, int offset, int size);

  /**
   * Get the {@link SearchIndexer} for this {@link Search} implementation
   *
   * @return
   */
  public SearchIndexer getIndexer();

  /**
   * Search for imeji objects belonging to a predefined list of possible results
   *
   * @param query
   * @param sortCri
   * @param user
   * @param uris
   * @return
   */
  public SearchResult search(SearchQuery query, SortCriterion sortCri, User user,
      List<String> uris);

  /**
   * Search with a Simple {@link String}
   *
   * @param query
   * @param sort
   * @param user
   * @param from
   * @param size
   * @return
   */
  public SearchResult searchString(String query, SortCriterion sort, User user, int from, int size);
}
