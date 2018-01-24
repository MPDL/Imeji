package de.mpg.imeji.presentation.item.browse;

import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import de.mpg.imeji.exceptions.ImejiException;
import de.mpg.imeji.logic.config.Imeji;
import de.mpg.imeji.logic.core.collection.CollectionService;
import de.mpg.imeji.logic.core.facade.SearchAndRetrieveFacade;
import de.mpg.imeji.logic.core.item.ItemService;
import de.mpg.imeji.logic.doi.DoiService;
import de.mpg.imeji.logic.model.CollectionImeji;
import de.mpg.imeji.logic.model.Item;
import de.mpg.imeji.logic.model.Properties.Status;
import de.mpg.imeji.logic.search.model.SearchQuery;
import de.mpg.imeji.logic.search.model.SearchResult;
import de.mpg.imeji.logic.search.model.SortCriterion;
import de.mpg.imeji.logic.util.ObjectHelper;
import de.mpg.imeji.logic.util.StringHelper;
import de.mpg.imeji.logic.util.UrlHelper;
import de.mpg.imeji.presentation.collection.CollectionActionMenu;
import de.mpg.imeji.presentation.license.LicenseEditor;

/**
 * {@link ItemsBean} to browse {@link Item} of a {@link CollectionImeji}
 *
 * @author saquet (initial creation)
 * @author $Author$ (last modification)
 * @version $Revision$ $LastChangedDate$
 */
@ManagedBean(name = "CollectionItemsBean")
@ViewScoped
public class CollectionItemsBean extends ItemsBean {
  private static final long serialVersionUID = 2506992231592053506L;
  private String id = null;
  private URI uri;
  private CollectionImeji collection;
  private CollectionActionMenu actionMenu;
  private String authors = "";
  private String authorsShort = "";
  private int size;
  private boolean showUpload = false;
  private LicenseEditor licenseEditor;

  /**
   * Initialize the bean
   *
   * @throws ImejiException
   */
  public CollectionItemsBean() {
    super();
  }

  @Override
  public void initSpecific() {
    try {
      id = UrlHelper.getParameterValue("collectionId");
      uri = ObjectHelper.getURI(CollectionImeji.class, id);
      setShowUpload(UrlHelper.getParameterBoolean("showUpload"));
      collection = new CollectionService().retrieveLazy(uri, getSessionUser());
      browseContext = getNavigationString() + id;
      update();
      actionMenu = new CollectionActionMenu(collection, getSessionUser(), getLocale());
      collection.getPersons().stream()
          .forEach(a -> authors +=
              authors.equals("") ? a.getCompleteName() + " (" + a.getOrganizationString() + ")"
                  : ", " + a.getCompleteName() + " (" + a.getOrganizationString() + ")");
      authorsShort = collection.getPersons().iterator().next().getCompleteName();
      if (collection.getPersons().size() > 1) {
        authorsShort += " & " + (collection.getPersons().size() - 1) + " "
            + Imeji.RESOURCE_BUNDLE.getLabel("more_authors", getLocale());
      }
      size = StringHelper.isNullOrEmptyTrim(getQuery()) ? getTotalNumberOfRecords()
          : getCollectionSize();
      setLicenseEditor(
          new LicenseEditor(getLocale(), collection.getStatus().equals(Status.PENDING)));
    } catch (final Exception e) {
      LOGGER.error("Error initializing collectionItemsBean", e);
    }
  }

  private int getCollectionSize() {
    return new ItemService().search(collection.getId(), null, null, Imeji.adminUser, 1, -1)
        .getNumberOfRecords();
  }

  @Override
  public SearchResult search(SearchQuery searchQuery, SortCriterion sortCriterion, int offset,
      int limit) {
    final SearchAndRetrieveFacade facade = new SearchAndRetrieveFacade();
    return facade.search(searchQuery, collection, getSessionUser(), sortCriterion, limit, offset);
  }

  @Override
  public Collection<Item> loadImages(List<String> uris) throws ImejiException {
    final SearchAndRetrieveFacade facade = new SearchAndRetrieveFacade();
    return facade.retrieveItemsAndCollectionsAsItems(uris, getSessionUser());
  }


  @Override
  public String getNavigationString() {
    return "pretty:collectionBrowse";
  }

  /**
   * return the url of the collection
   */
  @Override
  public String getImageBaseUrl() {
    if (collection == null) {
      return "";
    }
    return getNavigation().getApplicationUrl() + "collection/" + this.id + "/";
  }

  /**
   * return the url of the collection
   */
  @Override
  public String getBackUrl() {
    return getNavigation().getBrowseUrl() + "/collection" + "/" + this.id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
    // @Ye set session value to share with CollectionItemsBean, another way is via injection
    FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
        .put("CollectionItemsBean.id", id);
  }

  public void setCollection(CollectionImeji collection) {
    this.collection = collection;
  }

  public CollectionImeji getCollection() {
    return collection;
  }


  @Override
  public String getType() {
    return PAGINATOR_TYPE.COLLECTION_ITEMS.name();
  }

  /**
   * @return the actionMenu
   */
  public CollectionActionMenu getActionMenu() {
    return actionMenu;
  }

  /**
   * @param actionMenu the actionMenu to set
   */
  public void setActionMenu(CollectionActionMenu actionMenu) {
    this.actionMenu = actionMenu;
  }

  @Override
  public String getCollectionId() {
    return collection.getId().toString();
  }

  public String getAuthors() {
    return authors;
  }

  public String getCitation() {
    final String url = getDoiUrl().isEmpty() ? getPageUrl() : getDoiUrl();
    return authors
        + (collection.getStatus().equals(Status.RELEASED)
            ? " (" + collection.getVersionDate().get(Calendar.YEAR) + ")" : "")
        + ". " + collection.getTitle() + ". " + Imeji.CONFIG.getDoiPublisher() + ". <a href=\""
        + url + "\">" + url + "</a>";
  }

  /**
   * The Url to view the DOI
   *
   * @return
   */
  public String getDoiUrl() {
    return collection.getDoi().isEmpty() ? "" : DoiService.DOI_URL_RESOLVER + collection.getDoi();
  }

  public String getPageUrl() {
    return getNavigation().getCollectionUrl() + id;
  }

  public int getSize() {
    return size;
  }

  public String getLogo() {
    return collection.getLogoUrl() != null ? collection.getLogoUrl().toString()
        : getCurrentPartList().get(0).getLink();
  }


  /**
   * If true, set to false to avoid to show the upload dialog on each ajax request
   * 
   * @return the showUpload
   */
  public boolean isShowUpload() {
    if (showUpload) {
      showUpload = false;
      return true;
    }
    return showUpload;
  }

  /**
   * @param showUpload the showUpload to set
   */
  public void setShowUpload(boolean showUpload) {
    this.showUpload = showUpload;
  }

  /**
   * @return the licenseEditor
   */
  public LicenseEditor getLicenseEditor() {
    return licenseEditor;
  }

  /**
   * @param licenseEditor the licenseEditor to set
   */
  public void setLicenseEditor(LicenseEditor licenseEditor) {
    this.licenseEditor = licenseEditor;
  }

  /**
   * @return the authorsShort
   */
  public String getAuthorsShort() {
    return authorsShort;
  }

  /**
   * @param authorsShort the authorsShort to set
   */
  public void setAuthorsShort(String authorsShort) {
    this.authorsShort = authorsShort;
  }

  public int getNumberOfItems() {
    return getSearchResult().getNumberOfItems();
  }

  public int getNumberOfSubCollections() {
    return getSearchResult().getNumberOfSubcollections();
  }

  public int getNumberOfItemsOfCollection() {
    return getSearchResult().getNumberOfItemsOfCollection();
  }
}

