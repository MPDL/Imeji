package de.mpg.imeji.presentation.metadata.editSelected;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.mpg.imeji.logic.vo.Metadata;
import de.mpg.imeji.logic.vo.Statement;
import de.mpg.imeji.logic.vo.factory.ImejiFactory;
import de.mpg.imeji.presentation.metadata.MetadataInputComponent;

/**
 * A cell of the edit selected items page
 * 
 * @author saquet
 *
 */
public class CellComponent implements Serializable {
  private static final long serialVersionUID = 4617072974872823679L;
  private List<MetadataInputComponent> inputs = new ArrayList<>();
  private final Statement statement;

  public CellComponent(Statement statement, List<Metadata> metadata) {
    this.statement = statement;
    for (Metadata m : metadata) {
      inputs.add(new MetadataInputComponent(m, statement));
    }
  }

  public List<Metadata> toMetadataList() {
    List<Metadata> l = new ArrayList<>();
    for (MetadataInputComponent input : inputs) {
      l.add(input.getMetadata());
    }
    return l;
  }

  public void addValue() {
    inputs.add(new MetadataInputComponent(ImejiFactory.newMetadata(statement).build(), statement));
  }

  /**
   * @return the inputs
   */
  public List<MetadataInputComponent> getInputs() {
    return inputs;
  }

  /**
   * @param inputs the inputs to set
   */
  public void setInputs(List<MetadataInputComponent> inputs) {
    this.inputs = inputs;
  }

}
