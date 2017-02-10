package de.mpg.imeji.logic.search.factory;


import java.util.List;

import de.mpg.imeji.exceptions.UnprocessableError;
import de.mpg.imeji.logic.search.Search;
import de.mpg.imeji.logic.search.Search.SearchObjectTypes;
import de.mpg.imeji.logic.search.elasticsearch.ElasticSearch;
import de.mpg.imeji.logic.search.jenasearch.JenaSearch;
import de.mpg.imeji.logic.search.model.SearchElement;
import de.mpg.imeji.logic.search.model.SearchGroup;
import de.mpg.imeji.logic.search.model.SearchLogicalRelation.LOGICAL_RELATIONS;
import de.mpg.imeji.logic.search.model.SearchMetadata;
import de.mpg.imeji.logic.search.model.SearchPair;
import de.mpg.imeji.logic.search.model.SearchQuery;
import de.mpg.imeji.logic.search.model.SearchTechnicalMetadata;

/**
 * Factory for {@link Search}
 *
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
public class SearchFactory {
  private static SEARCH_IMPLEMENTATIONS defaultSearch = SEARCH_IMPLEMENTATIONS.JENA;
  private SearchQuery query = new SearchQuery();

  public enum SEARCH_IMPLEMENTATIONS {
    JENA, ELASTIC;
  }

  /**
   * Build a {@link SearchQuery} of the Factory
   * 
   * @return
   */
  public SearchQuery build() {
    return query;
  }

  /**
   * Build a Search Group with the current factory
   * 
   * @return
   */
  public SearchGroup buildAsGroup() {
    SearchGroup group = new SearchGroup();
    group.setGroup(query.getElements());
    return group;
  }

  /**
   * Add the elements in the query with an AND relation
   * 
   * @param elements
   * @return
   * @throws UnprocessableError
   */
  public SearchFactory and(List<SearchElement> elements) throws UnprocessableError {
    for (SearchElement element : elements) {
      addElement(element, LOGICAL_RELATIONS.AND);
    }
    return this;
  }

  /**
   * Add the elements in the query with an OR relation
   * 
   * @param elements
   * @return
   * @throws UnprocessableError
   */
  public SearchFactory or(List<SearchElement> elements) throws UnprocessableError {
    for (SearchElement element : elements) {
      addElement(element, LOGICAL_RELATIONS.OR);
    }
    return this;
  }

  /**
   * Add the elements to the query as following: query REL (Element1 OR Element2 ...)
   * 
   * @param elements
   * @param rel
   * @return
   * @throws UnprocessableError
   */
  public SearchFactory addOrGroup(List<SearchElement> elements, LOGICAL_RELATIONS rel)
      throws UnprocessableError {
    query.addLogicalRelation(rel);
    query.addGroup(new SearchFactory().or(elements).buildAsGroup());
    return this;
  }

  /**
   * Add the elements to the query as following: query REL (Element1 AND Element2 ...)
   * 
   * @param elements
   * @param rel
   * @return
   * @throws UnprocessableError
   */
  public SearchFactory addAndGroup(List<SearchElement> elements, LOGICAL_RELATIONS rel)
      throws UnprocessableError {
    query.addLogicalRelation(rel);
    query.addGroup(new SearchFactory().and(elements).buildAsGroup());
    return this;
  }


  /**
   * Add a SearchElement to the query. Valid elements: {@link SearchPair}, {@link SearchGroup},
   * {@link SearchMetadata}, {@link SearchTechnicalMetadata}
   * 
   * @param element
   * @param rel
   * @return
   * @throws UnprocessableError
   */
  public SearchFactory addElement(SearchElement element, LOGICAL_RELATIONS rel)
      throws UnprocessableError {
    if (!element.isEmpty()) {
      query.addLogicalRelation(rel);
      if (element instanceof SearchPair) {
        query.addPair((SearchPair) element);
      } else if (element instanceof SearchGroup) {
        query.addGroup((SearchGroup) element);
      } else {
        throw new UnprocessableError("Invalid SearchElement type " + element.getClass());
      }
    }
    return this;
  }

  /**
   * Create a new {@link Search}
   *
   * @return
   */
  public static Search create() {
    return create(defaultSearch);
  }

  /**
   * Create A new {@link Search}
   *
   * @param impl
   * @return
   */
  public static Search create(SEARCH_IMPLEMENTATIONS impl) {
    return create(SearchObjectTypes.ALL, impl);
  }

  /**
   * Create a new {@link Search}
   *
   * @param type
   * @param impl TODO
   * @return
   */
  public static Search create(SearchObjectTypes type, SEARCH_IMPLEMENTATIONS impl) {
    switch (impl) {
      case JENA:
        return new JenaSearch(type, null);
      case ELASTIC:
        return new ElasticSearch(type);
    }
    return null;
  }

  /**
   * Create a new {@link Search} !!! Only for JENA Search !!!
   *
   * @param type
   * @param containerUri
   * @return
   */
  public static Search create(SearchObjectTypes type, String containerUri) {
    return new JenaSearch(type, containerUri);
  }
}
