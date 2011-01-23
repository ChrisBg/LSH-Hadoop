package org.apache.mahout.math;
import org.apache.mahout.math.AbstractMatrix;

public abstract class FabricatedMatrix extends AbstractMatrix {

  public void cloneBindings(RandomMatrix clone) {
    clone.setRowLabelBindings(rowLabelBindings);
    clone.setColumnLabelBindings(columnLabelBindings);
  }

}
